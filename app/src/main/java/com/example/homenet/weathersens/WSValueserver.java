package com.example.homenet.weathersens;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.example.homenet.network.HNNetworking;

public class WSValueserver {
    HNNetworking.Value[] values;
    HNNetworking net;

    private int valuesCount;


    public WSValueserver(String ip, int port){
        net = new HNNetworking();
        net.setConf(ip, port);
    }

    public void init(){
        syncAll();
    }

    void syncAll(){
        net.syncAll();
        valuesCount = net.getValuesCount();
        values = new HNNetworking.Value[valuesCount];
        for (int i = 0; i < valuesCount; i++){
            values[i] = net.getValueInstance(i);
        }
    }

    public String getValueName(int id) { return values[id].vName; }
    public String getValueUnit(int id) { return values[id].vUnit; }
    public String getValue(int id) { return values[id].value; }
    public int getValuesCount(){ return valuesCount; }



}
