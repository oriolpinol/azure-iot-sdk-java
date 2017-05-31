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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provide means to upload file in the Azure Storage using the IoTHub.
 */
public final class FileUpload
{
    private static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private HttpsTransportManager httpsTransportManager;
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

        // File upload will directly use the HttpsTransportManager, avoiding
        //  all extra async controls.
        // We can do that because File upload have its own async mechanism.
        this.httpsTransportManager = new HttpsTransportManager(config);

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

        FileUploadTask fileUploadTask = new FileUploadTask(blobName, inputStream, streamLength, httpsTransportManager, statusCallback, statusCallbackContext);
        ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(1);
        taskScheduler.schedule(fileUploadTask,0, TimeUnit.SECONDS);
    }
}
