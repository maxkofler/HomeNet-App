package com.example.homenet.network;

import android.icu.text.MessagePattern;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HNNetworking {

    private String ip;
    private int port;

    public void syncAll(){
        NetworkHandler net = new NetworkHandler(ip, port, "@va");
        Thread netThread = new Thread(net);
        netThread.start();
        try {
            netThread.join();
            System.out.println("Message received: " + net.getMsg());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void setConf(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    private class NetworkHandler implements Runnable{

        private String ip;
        private int port;

        private String outMsg;
        private volatile String inMsg;

        NetworkHandler(String ip_, int port_, String msg_){
            this.ip = ip_;
            this.port = port_;
            this.outMsg = msg_;
        }

        String getMsg(){
            return this.inMsg;
        }

        @Override
        public void run() {

            try{
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress(ip, port), 1000);
                InputStream in = sock.getInputStream();
                OutputStreamWriter out  = new OutputStreamWriter(sock.getOutputStream());
                InputStreamReader reader = new InputStreamReader(in);

                StringBuilder data = new StringBuilder();
                out.write(outMsg);
                out.flush();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean end = false;
                while (!end){
                    int read = reader.read();
                    if (read != -1){
                        data.append(Character.toChars(read));
                        System.out.println("Read...");
                    }
                    else{
                        end = true;
                    }
                }

                System.err.print("Data: ");
                System.err.println(data.toString());

                inMsg = data.toString();

            } catch (UnknownHostException e) {
                System.err.println("Unknown host!");
            } catch (IOException e) {
                System.err.println("IOException!");
            }

        }
    }
}


