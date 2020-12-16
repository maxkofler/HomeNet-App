package com.example.homenet.weathersens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.anychart.scales.DateTime;
import com.example.homenet.History;
import com.example.homenet.R;
import com.example.homenet.network.HNNetworking;
import com.example.homenet.network.NetworkHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class History_Manager {

    NetworkHandler net;
    private String ip;
    private int port;

    History_entry[] history_entries;

    public History_entry[] getHistory_entries() {
        return history_entries;
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;
    int entries_to_display;

    public History_Manager(String ip, int port, int ID, Context context){
        preferences = context.getSharedPreferences(context.getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
        prefseditor = preferences.edit();
        entries_to_display = preferences.getInt(context.getString(R.string.key_countValuesHistory), 100);

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



                try {
                    history_entries[hist_pos] = new History_entry(Integer.parseInt(timeb.toString()), typeb.toString(), valueb.toString());
                }
                catch(NumberFormatException e){
                    Toast.makeText(context, context.getString(R.string.err_recString_not_readable), Toast.LENGTH_LONG).show();
                    history_entries = null;
                    run = false;
                    break;
                }
                hist_pos++;

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public History_entry[] smoothcourve(History_entry history[], int ammountValues){
        History_entry newHistory[] = new History_entry[history.length];
        if (history_entries.length < ammountValues){
            System.out.println("Not enough values to smooth the chart!");
            return history;
        }
        int history_len = history.length;
        float valuesPerEntryF = (float)history_len / ammountValues;
        //To get an int  number of divisions
        int valuesPerEntry = (int)valuesPerEntryF;
        int valuesRemaining = Math.round((valuesPerEntryF - valuesPerEntry) * ammountValues);
        System.out.println("Values to display: " + ammountValues + " Total values: " + history_len + " Values per entry float: " + valuesPerEntryF + " valuesPerEntry int: " + valuesPerEntry + " remaining: " + valuesRemaining);

        //Create the new array
        newHistory = new History_entry[ammountValues + valuesRemaining];

        int entriesFailed = 0;
        int history_new_index = 0;
        int history_old_index = 0;
        while (history_new_index < ammountValues){
            float d = 0;
            float t = 0;
            for (int i = 0; i < valuesPerEntry; i++){
                try{
                    d += Float.parseFloat(history[history_old_index].value);
                    t += history[history_old_index].time;
                }catch(Exception e){
                    entriesFailed++;
                }
                history_old_index++;
            }
            System.out.print("failed: " + entriesFailed);
            System.out.print(" D = " + d);
            newHistory[history_new_index] = new History_entry();
            newHistory[history_new_index].value = (Float.toString(d / valuesPerEntry));
            newHistory[history_new_index].time = (int)(t / valuesPerEntry);
            newHistory[history_new_index].type = history[history_old_index].type;
            //newHistory[history_new_index].print();
            history_new_index++;
        }

        for (int i = 0; i < valuesRemaining; i++){
            newHistory[history_new_index] = new History_entry();
            newHistory[history_new_index].value = history[history_old_index].value;
            newHistory[history_new_index].time = history[history_old_index].time;
            newHistory[history_new_index].type = history[history_old_index].type;
            //newHistory[history_new_index].print();
            history_old_index++;
            history_new_index++;
        }

        return newHistory;
    }

    public class History_entry{
        private int time;
        private String type;
        private String value;

        private String timeStamp;

        public History_entry(){

        }

        public History_entry(int time, String type, String value) {
            this.time = time;
            this.type = type;
            this.value = value;

            calcTimeStamp();
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

        public String getTimeStamp() {
            if (timeStamp == null){
                calcTimeStamp();
            }
            return timeStamp;
        }

        private void calcTimeStamp(){
            // convert seconds to milliseconds
            Date date = new java.util.Date(time*1000L);
            // the format of your date
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yy HH:mm:ss");
            // give a timezone reference for formatting (see comment at the bottom)
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+1"));
            timeStamp = sdf.format(date);
        }

        public void print(){
            calcTimeStamp();
            System.out.print("\t\tTime: " + time + " - " + timeStamp);
            System.out.print(" Type: " + type);
            System.out.println(" Value: " + value);
        }
    }
}
