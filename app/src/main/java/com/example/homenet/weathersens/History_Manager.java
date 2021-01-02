package com.example.homenet.weathersens;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.data.Mapping;
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
import java.util.Vector;

public class History_Manager {

    NetworkHandler net;
    private String ip;
    private int port;

    private static History_entry[] history_entries;

    public History_entry[] getHistory_entries() {
        if (history_entries == null){
            Log.w("homenet-getHistory_entries", "No entries saved, returning NULL!");
        }
        return history_entries;
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;
    int entries_to_display;

    int entriesParseFailed = 0;

    public History_Manager(String ip, int port, int ID, int lookback, Context context){
        preferences = context.getSharedPreferences(context.getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
        prefseditor = preferences.edit();
        entries_to_display = preferences.getInt(context.getString(R.string.key_countValuesHistory), 100);

        this.ip = ip;
        this.port = port;

        System.out.println("Starting net!");

        net = new NetworkHandler(ip, port, "@vht"+ID+";"+lookback, true);
        //net = new NetworkHandler(ip, port, "@vh"+ID, true);
        Thread netHandler = new Thread(net);
        netHandler.start();

        try {
            long startTime = System.nanoTime();

            netHandler.join();
            final String msg = net.getMsg();
            net = null;
            System.gc();
            if (msg == null){
                Log.e("homenet-History_Manager", "Returning message from Networkhandler is NULL!");
                return;
            }else{
                //Log.i("homenet-History_Manager", "Incoming msg: \"" + msg + "\"");
            }

            Log.d("homenet-History_Manager", "History_Manager(getValuesFromServer) took " + (System.nanoTime()-startTime)/1000000.0 + " ms");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    history_entries = extractHistoryEntries(msg);
                }
            });
            thread.start();
            thread.join();


            Log.d("homenet-History_Manager", "History_Manager() took " + (System.nanoTime()-startTime)/1000000.0 + " ms");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public History_entry[] extractHistoryEntries(String message){
        if (message != null){
            long startTime = System.nanoTime();

            BufferedReader reader = new BufferedReader(new StringReader(message));
            String buffer;
            reader = new BufferedReader(new StringReader(message));
            Vector<History_entry> Vhistory_entries = new Vector<>();
            int hist_pos = 0;

            //1608459886;int;2.09
            String time;
            String type;
            String value;

            boolean run = true;
            while(run) {
                try {
                    buffer = reader.readLine();
                } catch (IOException e) {
                    break;
                }

                if (buffer == null){
                    break;
                }
                if (buffer.isEmpty()) {
                    break;
                }

                int p1 = buffer.indexOf(";");
                int p2 = buffer.indexOf(";", p1 + 1);

                if (p1 > 0 && p2 > p1){
                    time = buffer.substring(0, p1);
                    type = buffer.substring(p1+1, p2);
                    value = buffer.substring(p2+1, buffer.length()-1);

                    try{
                        Vhistory_entries.add(new History_entry(Integer.parseInt(time), type, value));
                    }catch(NumberFormatException e){
                        Log.e("homenet-extractHistoryEntries", "Time format of entry is invalid: " + buffer);
                    }

                }else{
                    Log.w("homenet-extractHistoryEntries", "Found invalid line: " + buffer);
                }


            }

            History_entry newHistory[] = new History_entry[Vhistory_entries.size()];
            for (int i = 0; i < Vhistory_entries.size(); i++){
                newHistory[i] = Vhistory_entries.elementAt(i);
            }

            Log.d("homenet-extractHistoryEntries", "extractHistoryEntries() took " + (System.nanoTime()-startTime)/1000000.0 + " ms");
            return newHistory;
        }else{
            return null;
        }
    }

    public History_entry[] stripInvalidEntries(History_entry history[]){
        long startTime = System.nanoTime();

        Vector<History_entry> VnewHistory = new Vector<>();
        int entriesInvalid = 0;

        for (int i = 0; i < history.length; i++){
            if (history[i].isValidNumber()){
                VnewHistory.add(history[i]);
            }
        }

        History_entry newHistory[] = new History_entry[VnewHistory.size()];
        for (int i = 0; i < VnewHistory.size(); i++){
            newHistory[i] = VnewHistory.elementAt(i);
        }

        Log.i("homenet-stripInvalidEntries", "Stripped " + entriesInvalid + " invalid entries!");
        Log.d("homenet-stripInvalidEntries", "stripInvalidEntries took " + (System.nanoTime()-startTime)/1000000.0 + " ms");
        return newHistory;
    }

    public History_entry[] cutToLookbackWindow(History_entry history[], int secLookback, int ammountValues){
        //First check if the array is valid:
        if (history != null){
            long startTime = System.nanoTime();

            int len_history = history.length;
            int newestUNIXTime = history[len_history-1].time;
            int oldestUNIXTime = history[0].time;
            int searchedUnixTime  = newestUNIXTime - secLookback;

            //Check if there is something to do by checking if the time difference is bigger than the lookback window
            if (newestUNIXTime - oldestUNIXTime < secLookback || len_history <= ammountValues){
                Log.i("homenet", "Nothing to do to cut to right size, returning.");
                return history;
            }

            Log.i("homenet-cutToLookbackWindow", "Seconds to look back: " + secLookback);
            Log.i("homenet-cutToLookbackWindow", "Total entries: " + len_history);

            Log.i("homenet-cutToLookbackWindow", "Searching for ideal UNIX-timestamp in database...");

            int curUNIXTime = oldestUNIXTime;
            int valuesToSkip = 0;
            while (curUNIXTime < searchedUnixTime){
                curUNIXTime = history[valuesToSkip].time;
                valuesToSkip++;
            }
            valuesToSkip--;
            int valuesInWindow = len_history - valuesToSkip;

            Log.d("homenet-cutToLookbackWindow", "Found fit, entries to skip: " + valuesToSkip);
            Log.d("homenet-cutToLookbackWindow", "Values in window before fitting: " + valuesInWindow);

            int valuesPerEntryToFit = (valuesInWindow/ammountValues)+1;

            valuesInWindow = valuesPerEntryToFit*ammountValues;
            valuesToSkip = len_history - valuesInWindow;

            Log.i("homenet-cutToLookbackWindow", "Entries needed for perfect fit for smoothing: " + valuesInWindow);


            History_entry newHistory[] = new History_entry[valuesInWindow];
            int newHistoryIndex = 0;
            for (int i = valuesToSkip; i < len_history; i++){
                newHistory[newHistoryIndex] = history[i];
                newHistoryIndex++;
            }

            Log.i("homenet-cutToLookbackWindow", "Entries in new history: " + newHistory.length);
            if (newHistory.length != valuesInWindow){
                Log.wtf("homenet-cutToLookbackWindow", "The calculated ammount of history entries and the effective ammount of entries don't match!");
                return null;
            }

            Log.d("homenet-cutToLookbackWindow", "cutToLookbackWindow() took " + (System.nanoTime()-startTime)/1000000.0 + " ms");
            return newHistory;

        }else{
            Log.e("homenet", "Given value array is NULL, not cutting to right size!");
            return null;
        }
    }

    public History_entry[] smoothcurve(History_entry history[], int ammountValues){
        if(history != null){
            long startTime = System.nanoTime();

            Log.i("homenet-smoothcurve", "Smooting curve with " + history.length + " entries to " + ammountValues + " entries!");
            if (history_entries.length < ammountValues){
                System.out.println("Not enough values to smooth the chart!");
                return history;
            }
            long history_len = history.length;
            float valuesPerEntryF = (float)history_len / ammountValues;
            //To get an int  number of divisions
            int valuesPerEntry = (int)valuesPerEntryF;

            int time;
            float value;

            int entriesFailed = 0;
            int entryOldIndex = 0;
            int entriesThisPart = 0;
            Vector<History_entry> VnewHistory = new Vector<>();

            while (entryOldIndex < history_len){
                time = 0;
                value = 0;
                entriesThisPart = 0;
                for (int i = 0; i < valuesPerEntry; i++){
                    if (entryOldIndex < history_len){
                        time += history[entryOldIndex].getTime();
                        value += history[entryOldIndex].getValueF();
                        entriesThisPart++;
                        entryOldIndex++;
                    }else{

                    }

                }
                VnewHistory.add(new History_entry(time/entriesThisPart, history[entryOldIndex-1].type, Float.toString(value/valuesPerEntry)));
            }


            History_entry newHistory[] = new History_entry[VnewHistory.size()];
            for (int i = 0; i < VnewHistory.size(); i++){
                newHistory[i] = VnewHistory.elementAt(i);
            }

            Log.i("homenet-smoothcurve", "Done! Entries in new history: " + newHistory.length + " entries failed to summarize: " + entriesFailed);

            Log.d("homenet-smoothcurve", "smoothcurve() took " + (System.nanoTime()-startTime)/1000000.0 + " ms");
            return newHistory;
        }else{
            System.err.print("Failed to open array!");
            return null;
        }
    }

    public class History_entry{
        private int time;
        private String type;
        private String value;
        private float valueF;
        private boolean isValidNumber = false;

        private String timeStamp;

        public History_entry(int time, String type, String value) {
            this.time = time;
            this.type = type;
            this.value = value;

            try{
                this.valueF = Float.parseFloat(value);
                this.isValidNumber = true;
            }catch (NumberFormatException e){
                this.isValidNumber = false;
            }

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

        public float getValueF() {
            return valueF;
        }

        public boolean isValidNumber() {
            return isValidNumber;
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
