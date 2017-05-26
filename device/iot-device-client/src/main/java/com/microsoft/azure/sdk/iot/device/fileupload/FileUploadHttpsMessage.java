// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMessage;

import java.util.Arrays;

/**
 * A single file upload message,
 */
public class FileUploadHttpsMessage implements HttpsMessage
{
    private static final String HTTPS_SINGLE_MESSAGE_CONTENT_TYPE =
            "application/json;charset=utf-8";
    private static final String SYSTEM_PROPERTY_MESSAGE_ID = "messageid";

    private byte[] body;
    private MessageProperty[] properties;

    /**
     * Returns the HTTPS message represented by the service-bound message.
     *
     * @param message the service-bound message to be mapped to its HTTPS message
     * equivalent.
     *
     * @return the HTTPS message represented by the service-bound message.
     * @throws IllegalArgumentException if the provided message is {@code null}.
     */
    public static FileUploadHttpsMessage parseHttpsMessage(Message message) throws IllegalArgumentException
    {
        if(message == null)
        {
            /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_001: [If the provided message is null, the parsed FileUploadHttpsMessage shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Null message");
        }

        FileUploadHttpsMessage httpsMsg = new FileUploadHttpsMessage();
        int systemPropertyLength = 0;

        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_002: [The parsed FileUploadHttpsMessage shall have a copy of the original message body as its body.] */
        byte[] msgBody = message.getBytes();
        httpsMsg.body = Arrays.copyOf(msgBody, msgBody.length);

        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_003: [The parsed FileUploadHttpsMessage shall create a list with all properties to include in the https message.] */
        if(message.getMessageId() != null)
        {
            systemPropertyLength ++;
        }
        MessageProperty[] msgProperties = message.getProperties();
        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_004: [If the parsed FileUploadHttpsMessage failed to create a list of properties, it shall bypass the exception.] */
        httpsMsg.properties = new MessageProperty[msgProperties.length + systemPropertyLength];

        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_005: [The parsed FileUploadHttpsMessage shall add the prefix 'iothub-app-' to each of the message properties.] */
        int countProperty;
        for (countProperty = 0; countProperty < msgProperties.length; ++countProperty)
        {
            MessageProperty property = msgProperties[countProperty];

            httpsMsg.properties[countProperty] = new MessageProperty(
                    HTTPS_APP_PROPERTY_PREFIX + property.getName(),
                    property.getValue());
        }

        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_006: [If the message contains messageId, the parsed FileUploadHttpsMessage shall add the property 'iothub-messageid' with the messageId value.] */
        if(message.getMessageId() != null)
        {
            httpsMsg.properties[countProperty++] = new MessageProperty(
                    HTTPS_SYSTEM_PROPERTY_PREFIX + SYSTEM_PROPERTY_MESSAGE_ID,
                    message.getMessageId());
        }

        return httpsMsg;
    }

    /**
     * Returns a copy of the message body.
     *
     * @return a copy of the message body.
     */
    public byte[] getBody()
    {
        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_007: [The getBody shall return a copy of the message body.] */
        return Arrays.copyOf(this.body, this.body.length);
    }

    /**
     * Returns the message content-type as 'binary/octet-stream'.
     *
     * @return the message content-type as 'binary/octet-stream'.
     */
    public String getContentType()
    {
        /* Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_008: [The getContentType shall return the message content-type as 'binary/octet-stream'.] */
        return HTTPS_SINGLE_MESSAGE_CONTENT_TYPE;
    }

    /**
     * Returns a copy of the message properties.
     *
     * @return a copy of the message properties.
     */
    public MessageProperty[] getProperties()
    {
        // Codes_SRS_FILEUPLOADHTTPSMESSAGE_21_009: [The getProperties shall return a copy of the message properties.]
        int propertiesSize = this.properties.length;
        MessageProperty[] propertiesCopy =
                new MessageProperty[propertiesSize];

        for (int i = 0; i < propertiesSize; ++i)
        {
            MessageProperty property = this.properties[i];
            MessageProperty propertyCopy =
                    new MessageProperty(property.getName(),
                            property.getValue());
            propertiesCopy[i] = propertyCopy;
        }

        return propertiesCopy;
    }

    private FileUploadHttpsMessage()
    {
    }
}
