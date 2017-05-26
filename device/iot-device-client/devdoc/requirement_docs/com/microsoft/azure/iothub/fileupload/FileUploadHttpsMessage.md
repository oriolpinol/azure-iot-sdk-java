# FileUploadHttpsMessage Requirements

## Overview

Provide means to create a file upload message for Https, implementing the HttpsMessage interface.

## References

## Exposed API

```java
public class FileUploadHttpsMessage implements HttpsMessage
{
    public static FileUploadHttpsMessage parseHttpsMessage(Message message);

    public byte[] getBody();
    public String getContentType();

    public MessageProperty[] getProperties();

}
```

### FileUploadHttpsMessage
```java
public static FileUploadHttpsMessage parseHttpsMessage(Message message);
```
**SRS_FILEUPLOADHTTPSMESSAGE_21_001: [**If the provided message is null, the parsed FileUploadHttpsMessage shall throws IllegalArgumentException.**]**  
**SRS_FILEUPLOADHTTPSMESSAGE_21_002: [**The parsed FileUploadHttpsMessage shall have a copy of the original message body as its body.**]**  
**SRS_FILEUPLOADHTTPSMESSAGE_21_003: [**The parsed FileUploadHttpsMessage shall create a list with all properties to include in the https message.**]**  
**SRS_FILEUPLOADHTTPSMESSAGE_21_004: [**If the parsed FileUploadHttpsMessage failed to create a list of properties, it shall bypass the exception.**]**  
**SRS_FILEUPLOADHTTPSMESSAGE_21_005: [**The parsed FileUploadHttpsMessage shall add the prefix 'iothub-app-' to each of the message properties.**]**  
**SRS_FILEUPLOADHTTPSMESSAGE_21_006: [**If the message contains messageId, the parsed FileUploadHttpsMessage shall add the property 'iothub-messageid' with the messageId value.**]**  

### getBody
```java
public byte[] getBody();
```
**SRS_FILEUPLOADHTTPSMESSAGE_21_007: [**The getBody shall return a copy of the message body.**]**  

### getContentType
```java
public String getContentType();
```
**SRS_FILEUPLOADHTTPSMESSAGE_21_008: [**The getContentType shall return the message content-type as 'binary/octet-stream'.**]**  

### getProperties
```java
public MessageProperty[] getProperties();
```
**SRS_FILEUPLOADHTTPSMESSAGE_21_009: [**The getProperties shall return a copy of the message properties.**]**  
