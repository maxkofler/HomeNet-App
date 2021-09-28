package sdt.maxkofler.homenet_app.homenet.networking;

import androidx.annotation.Nullable;

public class NetworkInterface{
    private static final String cN = "HomeNet-App:NetworkInterface";

    private NetworkRunnable networkRunnable;
    private Thread networkThread;

    private Networking networking;

    public NetworkInterface(){
        this.networking = new Networking();
        this.networkRunnable = new NetworkRunnable(this.networking);
        this.networkThread = new Thread(this.networkRunnable);
        this.networkThread.start();
    }

    public void connect(@Nullable NetworkCallback callback){
        this.networkRunnable.deployConnect(callback);
    }

    public void setServerAddress(String address){
        this.networking.setServerAddress(address);
    }

    public void setServerPort(int port){
        this.networking.setServerPort(port);
    }

}
