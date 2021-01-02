package com.example.homenet.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

public class NetworkHandler implements Runnable{

    private String ip;
    private int port;

    private String outMsg;
    private static String inMsg;

    boolean output;

    Vector<String> inMsgV;

    public NetworkHandler(String ip_, int port_, String msg_, boolean writeOutput){
        this.ip = ip_;
        this.port = port_;
        this.outMsg = msg_;
        this.output = writeOutput;
    }

    public String getMsg(){
        return this.inMsg;
    }

    @Override
    public void run() {

        try{
            Log.i("homenet-NetworkHandler", "Opening TCP socket!");
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(ip, port), 1000);
            InputStream in = sock.getInputStream();
            OutputStreamWriter out  = new OutputStreamWriter(sock.getOutputStream());
            InputStreamReader reader = new InputStreamReader(in);

            StringBuilder data = new StringBuilder();
            out.write(outMsg);
            Log.i("homenet-NetworkHandler", "Written message to server: \"" + outMsg + "\"");
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

            if(output){
                System.out.println("Bytes currently available to read: " + in.available());
                System.out.println("Waited for the mesage for " + its + "ms...");
            }


            boolean end = false;
            inMsgV = new Vector<>();
            long bytesReceived = 0;
            int sCount = 1;
            int read;
            while (!end){
                read = reader.read();
                if (read != -1){
                    data.append(Character.toChars(read));
                    if (data.length() > 100000){
                        inMsgV.add(data.toString());
                        sCount++;
                        bytesReceived += data.length();
                        data = new StringBuilder();
                    }
                }
                else{
                    inMsgV.add(data.toString());
                    bytesReceived += data.length();
                    end = true;
                }
            }

            for (int i = 0; i < sCount; i++){
                inMsg += inMsgV.elementAt(i);
            }
            inMsgV = null;
            if (output){
                Log.i("homenet", "Received " + sCount + " chunks of 100.000 B totalling to " + bytesReceived + "B!");
            }


            sock.close();
            in.close();
            out.close();
            Log.i("homenet-NetworkHandler", "Closed socket!");

        } catch (UnknownHostException e) {
            System.err.println("Unknown host!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException!");
            e.printStackTrace();
        }

    }
}