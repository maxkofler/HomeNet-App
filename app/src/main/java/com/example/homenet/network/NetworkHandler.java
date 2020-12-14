package com.example.homenet.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkHandler implements Runnable{

    private String ip;
    private int port;

    private String outMsg;
    private volatile String inMsg;

    boolean output;

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
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(ip, port), 1000);
            InputStream in = sock.getInputStream();
            OutputStreamWriter out  = new OutputStreamWriter(sock.getOutputStream());
            InputStreamReader reader = new InputStreamReader(in);

            StringBuilder data = new StringBuilder();
            out.write(outMsg);
            out.flush();

            int its = 0;
            while (its < 3000){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                its ++;
                System.out.print(in.available());
                if (in.available() > 1){
                    break;
                }
            }

            if(output){
                System.out.println("Waited for " + its + "ms");
            }


            boolean end = false;
            while (!end){
                int read = reader.read();
                if (read != -1){
                    data.append(Character.toChars(read));
                }
                else{
                    end = true;
                }
            }

            inMsg = data.toString();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException!");
            e.printStackTrace();
        }

    }
}