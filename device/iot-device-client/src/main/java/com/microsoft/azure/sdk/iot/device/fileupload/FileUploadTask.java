// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadRequestParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadStatusParser;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Provide means to  asynchronous upload file in the Azure Storage using the IoTHub.
 *
 *   +--------------+      +---------------+    +---------------+    +---------------+
 *   |    Device    |      |    Iot Hub    |    |    Storage    |    |    Service    |
 *   +--------------+      +---------------+    +---------------+    +---------------+
 *           |                     |                    |                    |
 *           |                     |                    |                    |
 *       REQUEST_BLOB              |                    |                    |
 *           +--- request blob --->|                    |                    |
 *           |<-- blob SAS token --+                    |                    |
 *           |                     |                    |                    |
 *       UPLOAD_FILE               |                    |                    |
 *           +---- upload file to the provided blob --->|                    |
 *           +<------ end of upload with `status` ------+                    |
 *           |                     |                    |                    |
 *       NOTIFY_IOTHUB             |                    |                    |
 *           +--- notify status -->|                    |                    |
 *           |                     +------ notify new file available ------->|
 *           |                     |                    |                    |
 *
 */
public final class FileUploadTask implements Runnable
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;
    private HttpsTransportManager httpsTransportManager;
    private static CustomLogger logger;

    private String blobName;
    private InputStream inputStream;
    private long streamLength;
    private IotHubEventCallback userCallback;
    private Object userCallbackContext;

    private String correlationId;
    private URI blobURI;

    private static final ObjectLock FILE_UPLOAD_LOCK = new ObjectLock();

    FileUploadTask(String blobName, InputStream inputStream, long streamLength, HttpsTransportManager httpsTransportManager,
                    IotHubEventCallback userCallback, Object userCallbackContext)
    {
        this.blobName = blobName;
        this.inputStream = inputStream;
        this.streamLength = streamLength;
        this.userCallback = userCallback;
        this.userCallbackContext = userCallbackContext;

        this.httpsTransportManager = httpsTransportManager;

        logger = new CustomLogger(this.getClass());
        logger.LogInfo("HttpsFileUpload object is created successfully, method name is %s ", logger.getMethodName());
    }

    public void run()
    {

        FileUploadStatusParser fileUploadStatusParser = null;

        try
        {
            getContainer();
        }
        catch (Exception e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
        {
            logger.LogError("File upload failed to upload the stream to the blob. " + e.toString());
            userCallback.execute(IotHubStatusCode.ERROR, userCallbackContext);
        }

        if(correlationId != null)
        {
            try
            {
                CloudBlockBlob blob = new CloudBlockBlob(blobURI);
                blob.upload(inputStream, streamLength);
                fileUploadStatusParser = new FileUploadStatusParser(correlationId, true, 0, "Succeed to upload to storage.");
            }
            catch (Exception e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
            {
                logger.LogError("File upload failed to upload the stream to the blob. " + e.toString());
                userCallback.execute(IotHubStatusCode.ERROR, userCallbackContext);
                fileUploadStatusParser = new FileUploadStatusParser(correlationId, false, -1, "Failed to upload to storage.");
            }
            finally
            {
                sendNotification(fileUploadStatusParser);
            }
        }
    }

    private void addBlobInformation(Message responseMessage) throws IllegalArgumentException, URISyntaxException, UnsupportedEncodingException
    {
        String json = new String(responseMessage.getBytes(), DEFAULT_IOTHUB_MESSAGE_CHARSET);
        FileUploadResponseParser fileUploadResponseParser = new FileUploadResponseParser(json);

        this.correlationId = fileUploadResponseParser.getCorrelationId();
        this.blobName = fileUploadResponseParser.getBlobName();
        String hostName = fileUploadResponseParser.getHostName();
        String containerName = fileUploadResponseParser.getContainerName();
        String sasToken = fileUploadResponseParser.getSasToken();

        String putString = "https://" +
                hostName + "/" +
                containerName + "/" +
                URLEncoder.encode(blobName, "UTF-8") + // Pass URL encoded device name and blob name to support special characters
                sasToken;

        this.blobURI = new URI(putString);
    }

    private void getContainer() throws IOException, IllegalArgumentException, URISyntaxException
    {
        FileUploadRequestParser fileUploadRequestParser = new FileUploadRequestParser(blobName);

        Message message = new Message(fileUploadRequestParser.toJson());
        message.setIotHubMethod(IotHubMethod.POST);
        message.setUriPath("/files");

        ResponseMessage responseMessage;
        synchronized (FILE_UPLOAD_LOCK)
        {
            httpsTransportManager.open();
            responseMessage = httpsTransportManager.send(message);
            httpsTransportManager.close();
        }

        if(responseMessage.getStatus() == IotHubStatusCode.OK)
        {
            addBlobInformation(responseMessage);
        }
        else if(responseMessage.getStatus() == IotHubStatusCode.OK_EMPTY)
        {
            userCallback.execute(IotHubStatusCode.BAD_FORMAT, userCallbackContext);
        }
        else
        {
            userCallback.execute(responseMessage.getStatus(), userCallbackContext);
        }
    }

    private void sendNotification(FileUploadStatusParser fileUploadStatusParser)
    {
        try
        {
            Message message = new Message(fileUploadStatusParser.toJson());
            message.setIotHubMethod(IotHubMethod.POST);
            message.setUriPath("/notifications");

            ResponseMessage responseMessage;
            synchronized (FILE_UPLOAD_LOCK)
            {
                httpsTransportManager.open();
                responseMessage = httpsTransportManager.send(message);
                httpsTransportManager.close();
            }

            userCallback.execute(responseMessage.getStatus(), userCallbackContext);
        }
        catch (Exception e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
        {
            logger.LogError("File upload failed to report status to the iothub. " + e.toString());
            userCallback.execute(IotHubStatusCode.ERROR, userCallbackContext);
        }
    }
}
