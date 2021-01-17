package com.example.homenet.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

public class NetworkHandler implements Runnable{

    private String ip;
    private int port;

    private String outMsg;
    private static String inMsg;


    public NetworkHandler(String ip_, int port_, String msg_){
        this.ip = ip_;
        this.port = port_;
        this.outMsg = msg_;
    }

    public String getMsg(){
        return this.inMsg;
    }

    @Override
    public void run() {

        try{
            Log.i("homenet-NetworkHandler", "Opening TCP socket (" + ip + ":" + port + ")");
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(ip, port), 1000);
            InputStream in = sock.getInputStream();
            OutputStreamWriter out  = new OutputStreamWriter(sock.getOutputStream());
            //InputStreamReader reader = new InputStreamReader(in);


            StringBuilder data = new StringBuilder();
            out.write(outMsg);
            Log.i("homenet-NetworkHandler", "Written message to server: \"" + outMsg + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            out.flush();


            int its = 0;
            while (its < 3000){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                its ++;
                if (in.available() > 1){
                    break;
                }
            }

            Log.i("homenet-NetworkHandler", "Waited for response for " + its + " ms, bytes to read: " + in.available());

            boolean end = false;
            long bytesReceived = 0;
            String line = "";
            inMsg = "";
            StringBuilder inMsgBuilder = new StringBuilder();
            while (!end && reader.ready()){
                line = reader.readLine() + "\n";
                if (line != null && !line.isEmpty()){
                    inMsgBuilder.append(line);
                    bytesReceived += line.length();
                }else{
                    end = true;
                }
            }
            inMsg = inMsgBuilder.toString();

            sock.close();
            in.close();
            out.close();
            Log.i("homenet-NetworkHandler", "Closed socket, read " + bytesReceived + " Bytes!");

        } catch (UnknownHostException e) {
            Log.e("homenet-NetworkHandler", "Host is unknown or not reachable!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException!");
            e.printStackTrace();
        }

    }
}