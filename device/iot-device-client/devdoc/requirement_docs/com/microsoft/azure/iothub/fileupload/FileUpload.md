# FileUpload Requirements

## Overview

Provide means to upload file in the Azure Storage using the IoTHub.

## References

[File uploads with IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload)  
[FileUploadTask](./FileUploadTask.md)

## Exposed API

```java
public final class FileUpload
{
    public FileUpload(DeviceClientConfig config) throws IllegalArgumentException;
    
    public synchronized void uploadToBlobAsync(
            String blobName, InputStream inputStream, long streamLength,
            IotHubEventCallback statusCallback, Object statusCallbackContext)
            throws IllegalArgumentException;    
}
```


### FileUpload
```java
public FileUpload(DeviceClientConfig config) throws IllegalArgumentException;
```
**SRS_FILEUPLOAD_21_001: [**If the provided `config` is null, the constructor shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_002: [**The constructor shall create a new instance of `HttpsTransportManager` with the provided `config`.**]**  
**SRS_FILEUPLOAD_21_003: [**If the constructor fail to create the new instance of the `HttpsTransportManager`, it shall bypass the exception.**]**  
 
 
### UploadToBlobAsync
```java
public synchronized void uploadToBlobAsync(
        String blobName, InputStream inputStream, long streamLength,
        IotHubEventCallback statusCallback, Object statusCallbackContext)
        throws IllegalArgumentException;
```
**SRS_FILEUPLOAD_21_004: [**The uploadToBlobAsync shall asynchronously upload the InputStream `inputStream` to the blob in `blobName`.**]**  
**SRS_FILEUPLOAD_21_005: [**If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_006: [**If the `inputStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_007: [**If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_008: [**If the `userCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  
**SRS_FILEUPLOAD_21_009: [**The uploadToBlobAsync shall create a `FileUploadTask` to control this file upload.**]**  
**SRS_FILEUPLOAD_21_010: [**The uploadToBlobAsync shall schedule the task `FileUploadTask` to immediately start.**]**  
