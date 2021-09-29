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

    public void disconnect(@Nullable NetworkCallback callback){
        this.networkRunnable.deployDisconnect(callback);
    }

    public void sendForAnswer(String message, @Nullable NetworkCallback callback){
        this.networkRunnable.deploySendForResponse(message, callback);
    }

    public void stopWorker(){
        this.networkRunnable.stopWorker();
        try {
            this.networkThread.join();
        } catch (InterruptedException e) { }
        this.networkRunnable = null;
        this.networkRunnable = null;
        System.gc();
    }

    public void setServerAddress(String address){
        this.networking.setServerAddress(address);
    }

    public void setServerPort(int port){
        this.networking.setServerPort(port);
    }

    public void setServerTimeout(int timeout){
        this.networking.setTimeout(timeout);
    }

}
