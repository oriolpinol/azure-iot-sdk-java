// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 * Device Method Sample for an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class FileUploadSample
{
    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_HUNG = 300;
    private static final int METHOD_NOT_FOUND = 404;
    private static final int METHOD_NOT_DEFINED = 404;

    protected static class FileUploadStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to file upload operation with status " + status.name());
        }
    }

    /**
     * Receives method calls from IotHub. Default protocol is to use
     * use MQTT transport.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     */
  
    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");


        if (args.length != 3)
        {
            System.out.format(
                    "Expected the following argument but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "[Device connection string] - String containing Hostname, Device Id & Device Key in the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "[Blob name] - String containing the file name in the blob.\n"
                            + "[File to upload Path] - String containing the full path for the file to upload.\n",
                    args.length);
            return;
        }

        String connString = args[0];
        String blobName = args[1];
        String fileName = args[2];

        IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);

        System.out.println("Successfully created an IoT Hub client.");
        
        try
        {
            File file = new File(fileName);
            InputStream inputStream = new FileInputStream(file);
            long streamLength = file.length();

            client.uploadToBlobAsync(blobName, inputStream, streamLength, new FileUploadStatusCallBack(), null);

            System.out.println("File upload started with success");

            System.out.println("Waiting for file upload callback with the status...");
        }
        catch (Exception e)
        {
            System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" +  e.getMessage());
            System.out.println("Shutting down...");
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        System.out.println("Shutting down...");

    }
}
