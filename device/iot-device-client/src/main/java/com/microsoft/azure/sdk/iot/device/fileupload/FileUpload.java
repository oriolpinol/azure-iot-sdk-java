// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadRequestParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadStatusParser;
import com.microsoft.azure.sdk.iot.device.*;

import com.microsoft.azure.sdk.iot.device.transport.https.*;
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;

/**
 * Provide means to upload file in the Azure Storage using the IoTHub.
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
public final class FileUpload
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private HttpsIotHubConnection httpsIotHubConnection;
    private static CustomLogger logger;

    /**
     * CONSTRUCTOR
     *
     * @param config is the set of device client configurations.
     * @throws IOException if the config FileUpload cannot create a new instance of the transport.
     * @throws IllegalArgumentException if one of the parameters is null.
     */
    public FileUpload(DeviceClientConfig config) throws IllegalArgumentException, IOException
    {
        if(config == null)
        {
            throw new IllegalArgumentException("config is null");
        }

        // File upload will directly use the httpsIotHubConnection, avoiding
        //  all extra async controls.
        // We can do that because File upload have its own async mechanism.
        this.httpsIotHubConnection = new HttpsIotHubConnection(config);

        logger = new CustomLogger(this.getClass());
        logger.LogInfo("FileUpload object is created successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Upload the file to container, which was associated to the iothub.
     * This function will start the upload process, and back the execution
     * to the caller. The upload process will be executed in background.
     * When it is completed, it will trigger the callback with the upload status.
     *
     * @param blobName is the name of the file in the container.
     * @param inputStream is the input stream.
     * @param streamLength is the stream length.
     * @param statusCallback is the callback to notify that the upload is completed (with status).
     * @param statusCallbackContext is the context of the callback, allowing multiple uploads in parallel.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     *              blobName is {@code null} or empty,
     *              inputStream is {@code null},
     *              statusCallback is {@code null}
     */
    public synchronized void uploadToBlobAsync(
            String blobName, InputStream inputStream, long streamLength,
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws IllegalArgumentException
    {
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        if(inputStream == null)
        {
            throw new IllegalArgumentException("inputStream is null or empty");
        }

        if(statusCallback == null)
        {
            throw new IllegalArgumentException("statusCallback is null");
        }

        new Thread(new UploadToCloudTask(blobName, inputStream, streamLength, statusCallback, statusCallbackContext)).start();
    }


    /**
     * This runnable will effectively upload the file to the blob.
     */
    public final class UploadToCloudTask implements Runnable
    {
        private String blobName;
        private InputStream inputStream;
        private long streamLength;
        private IotHubEventCallback userCallback;
        private Object userCallbackContext;

        private String correlationId;
        private String hostName;
        private String containerName;
        private String sasToken;
        private URI blobURI;

        UploadToCloudTask(String blobName, InputStream inputStream, long streamLength,
                    IotHubEventCallback userCallback, Object userCallbackContext)
        {
            this.blobName = blobName;
            this.inputStream = inputStream;
            this.streamLength = streamLength;
            this.userCallback = userCallback;
            this.userCallbackContext = userCallbackContext;
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

        void addBlobInformation(Message responseMessage) throws IllegalArgumentException, URISyntaxException, UnsupportedEncodingException
        {
            String json = new String(responseMessage.getBytes(), DEFAULT_IOTHUB_MESSAGE_CHARSET);
            FileUploadResponseParser fileUploadResponseParser = new FileUploadResponseParser(json);

            this.correlationId = fileUploadResponseParser.getCorrelationId();
            this.blobName = fileUploadResponseParser.getBlobName();
            this.hostName = fileUploadResponseParser.getHostName();
            this.containerName = fileUploadResponseParser.getContainerName();
            this.sasToken = fileUploadResponseParser.getSasToken();

            String putString = "https://" +
                    this.hostName + "/" +
                    this.containerName + "/" +
                    URLEncoder.encode(this.blobName, "UTF-8") + // Pass URL encoded device name and blob name to support special characters
                    this.sasToken;

            this.blobURI = new URI(putString);
        }

        private void getContainer() throws IOException, IllegalArgumentException, URISyntaxException
        {
            FileUploadRequestParser fileUploadRequestParser = new FileUploadRequestParser(blobName);
            Message message = new Message(fileUploadRequestParser.toJson());

            HttpsMessage httpsMessage = FileUploadHttpsMessage.parseHttpsMessage(message);

            ResponseMessage responseMessage = httpsIotHubConnection.sendHttpsMessage(httpsMessage, HttpsMethod.POST, "/files");

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
                HttpsMessage httpsMessage = FileUploadHttpsMessage.parseHttpsMessage(message);

                ResponseMessage responseMessage = httpsIotHubConnection.sendHttpsMessage(httpsMessage, HttpsMethod.POST, "/notifications");

                userCallback.execute(responseMessage.getStatus(), userCallbackContext);
            }
            catch (Exception e) //Nobody will handel exception from this thread, so, convert it to an failed code in the user callback.
            {
                logger.LogError("File upload failed to report status to the iothub. " + e.toString());
                userCallback.execute(IotHubStatusCode.ERROR, userCallbackContext);
            }
        }
    }
}
