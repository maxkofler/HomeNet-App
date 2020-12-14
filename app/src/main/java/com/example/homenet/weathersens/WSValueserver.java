package com.example.homenet.weathersens;

import com.example.homenet.ExceptionClasses.NoConnectionToWSServer;
import com.example.homenet.network.HNNetworking;

public class WSValueserver {
    HNNetworking.Value[] values;
    HNNetworking net;

    private boolean error = false;

    private int valuesCount;


    public WSValueserver(String ip, int port){
        net = new HNNetworking();
        net.setConf(ip, port);
    }

    public void init(boolean writeOutput) throws NoConnectionToWSServer {
        syncAll(writeOutput);
    }

    void syncAll(boolean writeOutput) throws NoConnectionToWSServer {
        try {
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
