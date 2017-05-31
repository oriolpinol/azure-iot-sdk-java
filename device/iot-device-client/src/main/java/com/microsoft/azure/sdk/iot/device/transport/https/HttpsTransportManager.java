// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportManager;

import java.io.IOException;

/**
 * Implementation of the transport manager for https.
 */
public class HttpsTransportManager implements IotHubTransportManager
{
    DeviceClientConfig config;
    HttpsIotHubConnection httpsIotHubConnection;

    public HttpsTransportManager(DeviceClientConfig config)
    {
        this.config = config;
    }

    public void open()
    {
        httpsIotHubConnection = new HttpsIotHubConnection(config);
    }

    public void open(String[] topics) throws IOException
    {
        httpsIotHubConnection = new HttpsIotHubConnection(config);
    }


    public void close()
    {
        httpsIotHubConnection = null;
    }

    public ResponseMessage send(Message message) throws IOException
    {
        HttpsMessage httpsMessage = HttpsSingleMessage.parseHttpsJsonMessage(message);

        HttpsMethod httpsMethod;
        switch (message.getIotHubMethod())
        {
            case GET:
                httpsMethod = HttpsMethod.GET;
                break;
            case POST:
                httpsMethod = HttpsMethod.POST;
                break;
            default:
                throw new IllegalArgumentException("Unknown IoT Hub type " + message.getIotHubMethod().toString());
        }

        String httpsPath = message.getUriPath();

        return httpsIotHubConnection.sendHttpsMessage(httpsMessage, httpsMethod, httpsPath);
    }

    public Message receive()
    {
        Message message = null;

        return message;
    }
}
