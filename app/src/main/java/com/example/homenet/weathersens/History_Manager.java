package com.example.homenet.weathersens;

import android.content.Intent;
import android.text.format.DateFormat;

import com.anychart.scales.DateTime;
import com.example.homenet.History;
import com.example.homenet.network.HNNetworking;
import com.example.homenet.network.NetworkHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

public class History_Manager {

    NetworkHandler net;
    private String ip;
    private int port;

    History_entry[] history_entries;

    public History_entry[] getHistory_entries() {
        return history_entries;
    }

    public History_Manager(String ip, int port, int ID){
        this.ip = ip;
        this.port = port;

        System.out.println("Starting net!");

        net = new NetworkHandler(ip, port, "@vh"+ID, true);
        Thread netHandler = new Thread(net);
        netHandler.start();

        try {
            netHandler.join();
            String msg = net.getMsg();

            BufferedReader reader = new BufferedReader(new StringReader(msg));
            String buffer;

            boolean run = true;
            int cEntries = 0;
            while(run){
                buffer = reader.readLine();
                if (buffer.isEmpty()){
                    run = false;
                }else{
                    cEntries++;
                }
            }

            reader = new BufferedReader(new StringReader(msg));
            history_entries = new History_entry[cEntries];
            int hist_pos = 0;

            run = true;
            while(run){
                buffer = reader.readLine();
                if (buffer.isEmpty()){
                    break;
                }

                int p1 = buffer.indexOf(";");
                int p2 = buffer.indexOf(";", p1+1);

                StringBuilder timeb = new StringBuilder();
                StringBuilder typeb = new StringBuilder();
                StringBuilder valueb = new StringBuilder();

                for (int i = 0; i < p1; i++){
                    timeb.append(buffer.charAt(i));
                }
                for (int i = p1+1; i < p2; i++){
                    typeb.append(buffer.charAt(i));
                }
                for (int i = p2+1; i < buffer.length(); i++){
                    valueb.append(buffer.charAt(i));
                }



                history_entries[hist_pos] = new History_entry(Integer.parseInt(timeb.toString()), typeb.toString(), valueb.toString());
                hist_pos++;

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public class History_entry{
        private int time;
        private String type;
        private String value;

        private int dd;
        private int mm;
        private int yyyy;

        public History_entry(int time, String type, String value){
            this.time = time;
            this.type = type;
            this.value = value;

            Date date = new Date(time);

            this.dd = Integer.parseInt((String)DateFormat.format("dd",   date));
            this.mm = Integer.parseInt((String)DateFormat.format("mm",   date));
            this.yyyy = Integer.parseInt((String)DateFormat.format("yyyy",   date));
        }

        public int getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }
}
