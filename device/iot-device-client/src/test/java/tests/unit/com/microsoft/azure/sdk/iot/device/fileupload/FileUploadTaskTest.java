package tests.unit.com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadRequestParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadStatusParser;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadTask;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for file upload task class.
 */
public class FileUploadTaskTest
{
    @Mocked
    private InputStream mockInputStream;

    @Mocked
    private IotHubEventCallback mockIotHubEventCallback;

    @Mocked
    private HttpsTransportManager mockHttpsTransportManager;

    @Mocked
    private FileUploadRequestParser mockFileUploadRequestParser;

    @Mocked
    private FileUploadResponseParser mockFileUploadResponseParser;

    @Mocked
    private FileUploadStatusParser mockFileUploadStatusParser;

    @Mocked
    private Message mockMessage;

    @Mocked
    private ResponseMessage mockResponseMessage;

    @Mocked
    private CloudBlockBlob mockCloudBlockBlob;

    /* Tests_SRS_FILEUPLOADTASK_21_001: [If the `blobName` is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullBlobNameThrows()
    {
        // arrange
        final String blobName = null;
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_001: [If the `blobName` is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorEmptyBlobNameThrows()
    {
        // arrange
        final String blobName = "";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_002: [If the `inputStream` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullInputStreamThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, null, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_003: [If the `streamLength` is negative, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNegativeStreamLengthThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = -100;
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_004: [If the `httpsTransportManager` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullHttpsTransportManagerThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, null, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_005: [If the `userCallback` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullUserCallbackThrows()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, mockHttpsTransportManager, null, context);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_006: [The constructor shall store all the provided parameters.] */
    @Test
    public void constructorStoreParametersSucceed()
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        // act
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);

        // assert
        assertEquals(Deencapsulation.getField(fileUploadTask, "blobName"), blobName);
        assertEquals(Deencapsulation.getField(fileUploadTask, "inputStream"), mockInputStream);
        assertEquals(Deencapsulation.getField(fileUploadTask, "streamLength"), streamLength);
        assertEquals(Deencapsulation.getField(fileUploadTask, "userCallback"), mockIotHubEventCallback);
        assertEquals(Deencapsulation.getField(fileUploadTask, "userCallbackContext"), context);
        assertEquals(Deencapsulation.getField(fileUploadTask, "httpsTransportManager"), mockHttpsTransportManager);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_007: [The run shall create a FileUpload request message, by using the FileUploadRequestParser.] */
    @Test
    public void runCreateRequest() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        final String blobName = "test-device1/image.jpg";
        final String correlationId = "somecorrelationid";
        final String hostName = "contoso.azure-devices.net";
        final String containerName = "testcontainer";
        final String sasToken = "1234asdfSAStoken";
        final String requestJson = "{\"blobName\":\"" + blobName + "\"}";
        final String responseJson =
                "{ \n" +
                "    \"correlationId\": \"" + correlationId + "\", \n" +
                "    \"hostname\": \"" + hostName + "\", \n" +
                "    \"containerName\": \"" + containerName + "\", \n" +
                "    \"blobName\": \"" + blobName + "\", \n" +
                "    \"sasToken\": \"" + sasToken + "\" \n" +
                "}";
        final String putString = "https://" + hostName + "/" + containerName + "/" + blobName + sasToken;
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, context);

        new NonStrictExpectations()
        {
            {
                new FileUploadRequestParser(blobName);
                result = mockFileUploadRequestParser;
                mockFileUploadRequestParser.toJson();
                result = requestJson;
                new Message((String)any);
                result = mockMessage;
                mockHttpsTransportManager.send(mockMessage);
                result = mockResponseMessage;
            }
        };
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK;
                mockResponseMessage.getBytes();
                result = responseJson.getBytes();
                new FileUploadResponseParser(responseJson);
                result = mockFileUploadResponseParser;
            }
        };
        new NonStrictExpectations()
        {
            {
                mockFileUploadResponseParser.getBlobName();
                result = blobName;
                mockFileUploadResponseParser.getCorrelationId();
                result = correlationId;
                mockFileUploadResponseParser.getHostName();
                result = hostName;
                mockFileUploadResponseParser.getContainerName();
                result = containerName;
                mockFileUploadResponseParser.getSasToken();
                result = sasToken;
            }
        };
        new NonStrictExpectations()
        {
            {
                new CloudBlockBlob((URI)any);
                result = mockCloudBlockBlob;
            }
        };
        new NonStrictExpectations()
        {
            {
                new FileUploadStatusParser(correlationId, true, 0, (String)any);
                result = mockFileUploadStatusParser;
            }
        };

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.OK, context);
                times = 1;
            }
        };

    }

    /* Tests_SRS_FILEUPLOADTASK_21_008: [The run shall set the message method as `POST`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_009: [The run shall set the message URI path as `/files`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_010: [The run shall open the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_011: [The run shall send the blob request message to the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_012: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_013: [If result status for the blob request is not `OK`, or `OK_EMPTY`, the run shall call the userCallback bypassing the received status, and abort the upload.] */
    /* Tests_SRS_FILEUPLOADTASK_21_014: [If result status for the blob request is `OK_EMPTY`, the run shall call the userCallback with the stratus `BAD_FORMAT`, and abort the upload.] */
    /* Tests_SRS_FILEUPLOADTASK_21_015: [If the iothub accepts the request, it shall provide a `responseMessage` with the blob information with a correlationId.] */
    /* Tests_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    /* Tests_SRS_FILEUPLOADTASK_21_017: [The run shall parse and store the blobName and correlationId in the response, by use the FileUploadResponseParser.] */
    /* Tests_SRS_FILEUPLOADTASK_21_018: [The run shall create a blob URI `blobUri` with the format `https://[hostName]/[containerName]/[blobName,UTF-8][sasToken]`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_019: [The run shall create a `CloudBlockBlob` using the `blobUri`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_020: [The run shall upload the `inputStream` with the `stramLength` to the created `CloudBlockBlob`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_021: [If the upload to blob succeed, the run shall create a notification the IoT Hub with `isSuccess` equals true, `statusCode` equals 0.] */
    /* Tests_SRS_FILEUPLOADTASK_21_022: [If the upload to blob failed, the run shall create a notification the IoT Hub with `isSuccess` equals false, `statusCode` equals -1.] */
    /* Tests_SRS_FILEUPLOADTASK_21_023: [The run shall create a FileUpload status notification message, by using the FileUploadStatusParser.] */
    /* Tests_SRS_FILEUPLOADTASK_21_024: [The run shall set the message method as `POST`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_025: [The run shall set the message URI path as `/notifications`.] */
    /* Tests_SRS_FILEUPLOADTASK_21_026: [The run shall open the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_027: [The run shall send the blob request message to the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_028: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_029: [The run shall call the `userCallback` with the final response status.] */
    /* Tests_SRS_FILEUPLOADTASK_21_030: [If the upload to blob failed, the run shall call the `userCallback` reporting an error status `ERROR`.] */

}
