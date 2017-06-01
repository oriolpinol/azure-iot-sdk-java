// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadTask;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for file upload class.
 */
public class FileUploadTest
{
    @Mocked
    private DeviceClientConfig mockConfig;

    @Mocked
    private HttpsTransportManager mockHttpsTransportManager;

    @Mocked
    private InputStream mockInputStream;

    @Mocked
    private IotHubEventCallback mockIotHubEventCallback;

    @Mocked
    private FileUploadTask mockFileUploadTask;

    @Mocked
    private Executors mockExecutors;

    @Mocked
    private ScheduledExecutorService mockScheduler;


    private void constructorExpectations()
    {
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;
            }
        };
    }

    /* Tests_SRS_FILEUPLOAD_21_001: [If the provided `config` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullConfigThrows()
    {
        // act
        FileUpload fileUpload = new FileUpload(null);
    }

    /* Tests_SRS_FILEUPLOAD_21_002: [The constructor shall create a new instance of `HttpsTransportManager` with the provided `config`.] */
    @Test
    public void constructorSuccess()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;
                times = 1;
            }
        };

        // act
        FileUpload fileUpload = new FileUpload(mockConfig);

        // assert
        assertNotNull(fileUpload);
    }

    /* Tests_SRS_FILEUPLOAD_21_003: [If the constructor fail to create the new instance of the `HttpsTransportManager`, it shall bypass the exception.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorHttpsTransportManagerThrows()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        FileUpload fileUpload = new FileUpload(mockConfig);
    }

    /* Tests_SRS_FILEUPLOAD_21_004: [The uploadToBlobAsync shall asynchronously upload the InputStream `inputStream` to the blob in `blobName`.] */
    /* Tests_SRS_FILEUPLOAD_21_009: [The uploadToBlobAsync shall create a `FileUploadTask` to control this file upload.] */
    /* Tests_SRS_FILEUPLOAD_21_010: [The uploadToBlobAsync shall schedule the task `FileUploadTask` to immediately start.] */
    @Test
    public void uploadToBlobAsyncSuccess()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // assert
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(FileUploadTask.class,
                        new Class[] { String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                        blobName, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);
                result = mockFileUploadTask;
                times = 1;
                mockExecutors.newScheduledThreadPool(1);
                result = mockScheduler;
                times = 1;
                mockScheduler.schedule(mockFileUploadTask, 0, TimeUnit.SECONDS);
                times = 1;
            }
        };

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_005: [If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNullBlobNameThrows()
    {
        // arrange
        final String blobName = null;
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_005: [If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncEmptyBlobNameThrows()
    {
        // arrange
        final String blobName = "";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_006: [If the `inputStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNullImputStreamThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, null, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_007: [If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNegativeStreamLenghtThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = -100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_008: [If the `userCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNullUserCallbackThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, null, context);
    }

}
