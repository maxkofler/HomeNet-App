package com.example.homenet.network;

import android.icu.text.MessagePattern;
import android.util.Log;
import android.widget.Toast;

import com.example.homenet.ExceptionClasses.NoConnectionToWSServer;
import com.example.homenet.ExceptionClasses.NoValuesToProcess;
import com.example.homenet.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;

public class HNNetworking {

    private String ip;
    private int port;

    private boolean criticalError = false;

    private Value[] values;
    private int valuesCount;

    public boolean getError(){return criticalError;}

    public void syncAll(boolean writeOutput) throws NoConnectionToWSServer {
        NetworkHandler net = new NetworkHandler(ip, port, "@va");
        Thread netThread = new Thread(net);
        netThread.start();
        String recMsg = null;
        try {
            netThread.join();
            recMsg = net.getMsg();
            fetchValues(recMsg, writeOutput);
            if (recMsg == null){
                System.err.println("Fatal error: Server is not reachable!");
                throw new NoConnectionToWSServer();
            }
            Log.i("homenet-syncAll()", "Incoming msg:\n" + recMsg);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoValuesToProcess e){
            Log.wtf("homenet-syncAll", "Failed to parse string \"" + e.getMsg() + "\"!");
            e.printStackTrace();
        }



    }

    public void setConf(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    private void fetchValues(String msg, boolean output) throws NoValuesToProcess{
        if (msg == null || msg.isEmpty()){
            throw new NoValuesToProcess(msg);
        }

        BufferedReader sr = new BufferedReader(new StringReader(msg));
        boolean end = false;
        int lines = 0;
        String curLine = null;

        //First count the lines
        while (!end){
            try {
                curLine = sr.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (curLine == null){
                end = true;
            }else{
                lines++;
            }
        }

        //Reassign the Buffered Reader to read from the line
        sr = new BufferedReader(new StringReader(msg));
        values = new Value[lines];

        System.out.println("Received " + lines + " values!");
        valuesCount = lines;

        for (int i = 0; i < lines; i++){
            try {
                curLine = sr.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (curLine != null){
                values[i] = new Value();
                if(values[i].fetchFromTransmissionLine(curLine))
                {
                    values[i].gvID = i;
                    if (output){
                        values[i].logContents();
                    }
                }else{
                    i--;
                }

            }else{
                break;
            }
        }
    }

    public class Value {
        public String dName;
        public int dID;
        public String vName;
        public String vUnit;
        public String vType;
        public int vID;
        public int gvID;
        public String value;
        public String vDataType;

        int lastPos = 0;

        public boolean fetchFromTransmissionLine(String line) {
            try{
                lastPos = -1;
                dName = fetchArg(line);
                dID = Integer.parseInt(fetchArg(line));
                vName = fetchArg(line);
                vUnit = fetchArg(line);
                vType = fetchArg(line);
                vID = Integer.parseInt(fetchArg(line));
                value = fetchArg(line);
                vDataType = fetchArg(line);
                return true;
            }catch (NumberFormatException e){
                return false;
            }
        }

        public void logContents() {
            System.out.println("----------Value Overview----------");
            System.out.println("dName: " + dName);
            System.out.println("dID: " + dID);
            System.out.println("vName: " + vName);
            System.out.println("vUnit: " + vUnit);
            System.out.println("vType: " + vType);
            System.out.println("vID: " + vID);
            System.out.println("gvID: " + gvID);
            System.out.println("value: " + value);
            System.out.println("vDataType: " + vDataType);
        }

        private String fetchArg(String line) {
            if (line == "" || line == null){
                Log.e("homenet-fetchArg()", "Error in fetching argunemt: \"" + line + "\"");
                return null;
            }
            int begPos = line.indexOf("<", lastPos);
            int endPos = line.indexOf(">", begPos);
            lastPos = endPos;
            StringBuilder sb = new StringBuilder();
            for (int i = begPos + 1; i < endPos; i++) {
                sb.append(line.charAt(i));
            }
            return sb.toString();
        }
    }

    public int getValuesCount(){return valuesCount;}
    public Value getValueInstance(int id){return values[id];}
}




