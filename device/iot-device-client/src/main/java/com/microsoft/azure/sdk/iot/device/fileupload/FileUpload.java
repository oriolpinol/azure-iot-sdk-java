// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.CustomLogger;

import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provide means to upload file in the Azure Storage using the IoTHub.
 */
public final class FileUpload
{
    private HttpsTransportManager httpsTransportManager;
    private static CustomLogger logger;

    /**
     * CONSTRUCTOR
     *
     * @param config is the set of device client configurations.
     * @throws IllegalArgumentException if one of the parameters is null.
     */
    public FileUpload(DeviceClientConfig config) throws IllegalArgumentException
    {
        /* Codes_SRS_FILEUPLOAD_21_001: [If the provided `config` is null, the constructor shall throw IllegalArgumentException.] */
        if(config == null)
        {
            throw new IllegalArgumentException("config is null");
        }

        // File upload will directly use the HttpsTransportManager, avoiding
        //  all extra async controls.
        // We can do that because File upload have its own async mechanism.
        /* Codes_SRS_FILEUPLOAD_21_002: [The constructor shall create a new instance of `HttpsTransportManager` with the provided `config`.] */
        /* Codes_SRS_FILEUPLOAD_21_003: [If the constructor fail to create the new instance of the `HttpsTransportManager`, it shall bypass the exception.] */
        this.httpsTransportManager = new HttpsTransportManager(config);

        logger = new CustomLogger(this.getClass());
        logger.LogInfo("FileUpload object is created successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Upload the file to container, which was associated to the iothub.
     * This function will start the upload process, and back the execution
     * to the caller. The upload process will be executed in background.
     * When it is completed, the background thread will trigger the
     * callback with the upload status.
     *
     * @param blobName is the name of the file in the container.
     * @param inputStream is the input stream.
     * @param streamLength is the stream length.
     * @param statusCallback is the callback to notify that the upload is completed (with status).
     * @param statusCallbackContext is the context of the callback, allowing multiple uploads in parallel.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     *              blobName is {@code null} or empty,
     *              inputStream is {@code null},
     *              streamLength is negative,
     *              statusCallback is {@code null}
     */
    public synchronized void uploadToBlobAsync(
            String blobName, InputStream inputStream, long streamLength,
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws IllegalArgumentException
    {
        /* Codes_SRS_FILEUPLOAD_21_005: [If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if((blobName == null) || blobName.isEmpty())
        {
            throw new IllegalArgumentException("blobName is null or empty");
        }

        /* Codes_SRS_FILEUPLOAD_21_006: [If the `inputStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if(inputStream == null)
        {
            throw new IllegalArgumentException("inputStream is null or empty");
        }

        /* Codes_SRS_FILEUPLOAD_21_007: [If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if(streamLength < 0)
        {
            throw new IllegalArgumentException("streamLength is negative");
        }

        /* Codes_SRS_FILEUPLOAD_21_008: [If the `userCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if(statusCallback == null)
        {
            throw new IllegalArgumentException("statusCallback is null");
        }

        /* Codes_SRS_FILEUPLOAD_21_004: [The uploadToBlobAsync shall asynchronously upload the InputStream `inputStream` to the blob in `blobName`.] */
        /* Codes_SRS_FILEUPLOAD_21_009: [The uploadToBlobAsync shall create a `FileUploadTask` to control this file upload.] */
        FileUploadTask fileUploadTask = new FileUploadTask(blobName, inputStream, streamLength, httpsTransportManager, statusCallback, statusCallbackContext);

        /* Codes_SRS_FILEUPLOAD_21_010: [The uploadToBlobAsync shall schedule the task `FileUploadTask` to immediately start.] */
        ScheduledExecutorService taskScheduler = Executors.newScheduledThreadPool(1);
        taskScheduler.schedule(fileUploadTask,0, TimeUnit.SECONDS);
    }
}
