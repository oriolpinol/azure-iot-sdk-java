# FileUpload Requirements

## Overview

Provide means to upload file in the Azure Storage using the IoTHub.

## References

## Exposed API

```java
public final class FileUpload
{
    public FileUpload(DeviceClientConfig config)  throws IOException, IllegalArgumentException;
    
    public synchronized void uploadToBlobAsync(
            String blobName, String filePath, 
            IotHubEventCallback userCallback, Object userCallbackContext)
            throws FileNotFoundException;
    
    public synchronized void UploadToBlobAsync(
            String blobName, InputStream fileStream, 
            IotHubEventCallback userCallback, Object userCallbackContext);
}
```


### FileUpload
```java
public FileUpload(DeviceClientConfig config)  throws IOException, IllegalArgumentException
```
**SRS_FILEUPLOAD_21_001: [**The constructor shall create a instance of the FileUpload class.**]**  
**SRS_FILEUPLOAD_21_002: [**If the provided `config` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_003: [**The constructor shall create a new instance of `DeviceIO` with connectionString of the provided `config` and HTTPS as the protocol.**]**  
**SRS_FILEUPLOAD_21_004: [**The constructor shall store the new instance of `DeviceIO` as the file upload iothub deviceIO.**]**  
**SRS_FILEUPLOAD_21_005: [**If the constructor fail to create the new instance of the `DeviceIO`, it shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_006: [**The constructor shall create a instance of the `OnBlobRequestStatus` that implements `IotHubResponseCallback`, where any further status for blob request shall be delivered.**]**  
**SRS_FILEUPLOAD_21_007: [**If the constructor fail to create the new instance of the `OnBlobRequestStatus`, it shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_008: [**The constructor shall create a instance of the `OnBlobNotificationStatus` that implements `IotHubResponseCallback`, where any further status for blob notification shall be delivered.**]**  
**SRS_FILEUPLOAD_21_009: [**If the constructor fail to create the new instance of the `OnBlobNotificationStatus`, it shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_010: [**The constructor shall open the connection with the IotHub using the created deviceIO.**]**  
**SRS_FILEUPLOAD_21_011: [**If open connection fail, the uploadToBlobAsync shall throw IOException.**]**  
 
 
### UploadToBlobAsync
```java
public synchronized void uploadToBlobAsync(
        String blobName, InputStream fileStream, 
        IotHubEventCallback userCallback, Object userCallbackContext)
```
**SRS_FILEUPLOAD_21_023: [**The uploadToBlobAsync shall asynchronously upload the FileInputStream `fileStream` to the blob in `blobName`.**]**  
**SRS_FILEUPLOAD_21_024: [**If the `fileStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_025: [**If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_026: [**If the `userCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_027: [**The uploadToBlobAsync shall create a `BlobContext` to control this file upload, and store the `blobName`, `fileStream`, `userCallback`, and the `userCallbackContext`.**]**  
**SRS_FILEUPLOAD_21_028: [**The uploadToBlobAsync shall create a FileUpload request message, by using the FileUploadRequestParser.**]**  
Json request example:
```json
{ 
    "blobName": "[name of the file for which a SAS URI will be generated]" 
} 
```
**SRS_FILEUPLOAD_21_029: [**The uploadToBlobAsync shall send the blob request message to the iothub, using the deviceIO sendEventAsync.**]**  
**SRS_FILEUPLOAD_21_030: [**The uploadToBlobAsync shall use the `OnBlobRequestStatus`, and `BlobContext` as the callback for the sendEventAsync.**]**  
**SRS_FILEUPLOAD_21_031: [**If send throw an exception, the uploadToBlobAsync shall throw IOException.**]**  


### OnBlobRequestStatus
DeviceIO will trigger this callback to inform the status of the request message. At this point, the expected 
status is 200 (OK) together with a response message. Any other status besides that is a communication or 
iothub error, in both cases, FileUpload failed and the status shall be bypassed to the user.
```java
private final class OnBlobRequestStatus implements IotHubResponseCallback
{
    @Override
    public void execute(IotHubStatusCode responseStatus, Message responseMessage, Object callbackContext);
};
```
**SRS_FILEUPLOAD_21_032: [**If the `callbackContext` is null or not a valid `BlobContext`, the OnBlobRequestStatus shall log an error and ignore the callback.**]**  
**SRS_FILEUPLOAD_21_033: [**If `responseStatus` is not 200 (OK), the OnBlobRequestStatus shall call the `userCallback` forwarding the status in the response, and delete the file upload context.**]**  
**SRS_FILEUPLOAD_21_034: [**If the iothub accepts the request, it shall provide a `responseMessage` with the blob information with a correlationId.**]**  
Json response example:
```json
{ 
    "correlationId": "somecorrelationid", 
    "hostname": "contoso.azure-devices.net", 
    "containerName": "testcontainer", 
    "blobName": "test-device1/image.jpg", 
    "sasToken": "1234asdfSAStoken" 
} 
```
**SRS_FILEUPLOAD_21_035: [**If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the OnBlobRequestStatus shall call the `userCallback` reporting the error, and delete the file upload context.**]**  
**SRS_FILEUPLOAD_21_036: [**The OnBlobRequestStatus shall parse and store the content of the response in the `BlobContext`, by use the FileUploadResponseParser.**]**  
**SRS_FILEUPLOAD_21_037: [**The OnBlobRequestStatus shall create a thread `UploadToCloudTask` to upload the `fileStream` to the blob using `CloudBlockBlob`.**]**  


### UploadToCloudTask
This runnable will effectively upload the file to the blob.
```java
public final class UploadToCloudTask implements Runnable
{
    public UploadToCloudTask(BlobContext context);
    public void run();
}
```
**SRS_FILEUPLOAD_21_038: [**The UploadToCloudTask shall create a `CloudBlockBlob` using the blob information in the `BlobContext`.**]**  
**SRS_FILEUPLOAD_21_039: [**If the upload to blob throw an exception, the UploadToCloudTask shall call the `userCallback` reporting an error status, and delete the `BlobContext` for that file.**]**  
**SRS_FILEUPLOAD_21_040: [**The UploadToCloudTask shall send the notification to the iothub reporting upload succeed or failed, using the deviceIO sendEventAsync.**]**  
**SRS_FILEUPLOAD_21_041: [**The UploadToCloudTask shall create a FileUpload status notification message, by using the FileUploadStatusParser.**]**  
Json notification example:
```json
{ 
    "correlationId": "[correlation ID returned by the initial request]", 
    "isSuccess": True, 
    "statusCode": 1234, 
    "statusDescription": "Description of the status" 
} 
```
**SRS_FILEUPLOAD_21_042: [**If send throw an exception, the UploadToCloudTask shall call the `userCallback` reporting an error status, and delete the `BlobContext` for that file.**]**  


### OnBlobNotificationStatus
DeviceIO will trigger this callback to inform the status of the notification message. At this point, the expected 
status are 200 (OK) or 204 (OK_EMPTY). Any other status besides that is a communication or iothub error, in 
both cases, FileUpload failed and the status shall be bypassed to the user.
```java
private final class OnBlobNotificationStatus implements IotHubEventCallback
{
    @Override
    void execute(IotHubStatusCode responseStatus, Object callbackContext);
}
```
**SRS_FILEUPLOAD_21_043: [**If the `callbackContext` is not a valid `BlobContext`, the OnBlobNotificationStatus shall throw IOException.**]**  
**SRS_FILEUPLOAD_21_044: [**The OnBlobNotificationStatus shall call the `userCallback` forwarding the status in the response.**]**  
**SRS_FILEUPLOAD_21_045: [**The OnBlobNotificationStatus shall destroy the `BlobContext` that control this file upload.**]**  

