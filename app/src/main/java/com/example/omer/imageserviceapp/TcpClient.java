package com.example.omer.imageserviceapp;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {

    File file;

    /**
     * constructor that receives a file for the server.
     * @param file - server file
     */
    public TcpClient(File file) {
        this.file = file;
    }

    /**
     * startCommunication function.
     * creates the commiuncation to the server
     * @throws Exception
     */
    public void startCommunication() throws Exception {
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName("10.0.2.2");
            try {
                //create a socket to make the connection with the server
                Socket socket = new Socket(serverAddr, 7999);
                OutputStream output = socket.getOutputStream();
                InputStream input = socket.getInputStream();
                output.write(file.getName().getBytes());
                byte[] confirmation = new byte[1];
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(file);
                int i;
                if (input.read(confirmation) == 1) {
                    try {
                        while ((i = fis.read(buffer)) != -1) {
                            stream.write(buffer, 0, i);
                        }
                    } catch (IOException ex) {
                        Log.e("TCP", "S: Error", ex);
                    }

                    output.write(stream.toByteArray());
                }
                output.flush();
            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }

}
