// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.fileupload;


import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadHttpsMessage;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.sql.Array;
import java.util.Arrays;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Unit tests for file upload https message class.
 */
public class FileUploadHttpsMessageTest
{
    @Mocked
    private Message mockMessage;

    private final String messageId = "validMessageId";
    private final String body = "{\"blobName\":\"fileTest.txt\"}";
    private final byte[] bodyArray = body.getBytes();
    private final long bodyLength = body.length();
    private final MessageProperty[] msgProperties = new MessageProperty[]
            {
                    new MessageProperty("name1", "value1"),
                    new MessageProperty("name2", "value2"),
                    new MessageProperty("name3", "value3"),
            };

    private void parserExpectations()
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockMessage, "getBytes");
                result = bodyArray;
                Deencapsulation.invoke(mockMessage, "getMessageId");
                result = messageId;
                Deencapsulation.invoke(mockMessage, "getProperties");
                result = msgProperties;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADHTTPSMESSAGE_21_001: [If the provided message is null, the parsed FileUploadHttpsMessage shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void parseHttpsMessageNullMessageThrows()
    {
        // act
        FileUploadHttpsMessage.parseHttpsMessage(null);
    }

    /* Tests_SRS_FILEUPLOADHTTPSMESSAGE_21_002: [The parsed FileUploadHttpsMessage shall have a copy of the original message body as its body.] */
    @Test
    public void parseHttpsMessageNewFileUploadHttpsMessageSucceed()
    {
        // arrange
        parserExpectations();

        // act
        FileUploadHttpsMessage fileUploadHttpsMessage = FileUploadHttpsMessage.parseHttpsMessage(mockMessage);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMessage, "getBytes");
                times = 1;
            }
        };
        assertThat(new String((byte[]) Deencapsulation.getField(fileUploadHttpsMessage, "body")), is(body));
    }

}

