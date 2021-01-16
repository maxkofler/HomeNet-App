package com.example.homenet.weathersens;

import android.util.Log;

import com.example.homenet.ExceptionClasses.NoConnectionToWSServer;
import com.example.homenet.network.HNNetworking;

public class WSValueserver {
    HNNetworking.Value[] values;
    HNNetworking net;

    private boolean error = false;

    private int valuesCount;

    private String ip;
    private int port;


    public WSValueserver(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public boolean init(boolean writeOutput) {
        try{
            syncAll(writeOutput);
            return true;
        }catch (NoConnectionToWSServer e){
            return false;
        }

    }

    public void closeNet(){
        net = null;
        System.gc();
        Log.i("homenet-closeNet()", "Removed network objects!");
    }

    void syncAll(boolean writeOutput) throws NoConnectionToWSServer {
        try {
            if (net == null){
                net = new HNNetworking();
                net.setConf(ip, port);
            }
            net.syncAll(writeOutput);
            valuesCount = net.getValuesCount();
            values = new HNNetworking.Value[valuesCount];
            for (int i = 0; i < valuesCount; i++) {
                values[i] = net.getValueInstance(i);
            }
        }catch (NoConnectionToWSServer e){
            error = true;
            throw e;
        }
    }

    public boolean getDataAvailable(){return !error;}
    public String getValueName(int id) { return values[id].vName; }
    public String getValueUnit(int id) { return values[id].vUnit; }
    public String getValue(int id) { return values[id].value; }
    public int getValuesCount(){ return valuesCount; }
}
