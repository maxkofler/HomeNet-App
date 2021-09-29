package sdt.maxkofler.homenet_app.homenet;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.MainActivity;
import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkCallback;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkInterface;
import sdt.maxkofler.homenet_app.homenet.networking.Networking;

/**
 *
 * This class represents the interface to the HomeNet server.
 *
 */
public class HomeNet implements NetworkCallback {
    private static final String cN = "HomeNet-App:HomeNet";
    private NetworkInterface networkInterface;

    private NetworkCallback sync_callback;

    private Semaphore syncMutex;

    //Container for error messages for inner classes
    private String eMessage = "";

    public HomeNet(String address, int port){
        this.syncMutex = new Semaphore(1);
        this.networkInterface = new NetworkInterface();
        this.networkInterface.setServerAddress(address);
        this.networkInterface.setServerPort(port);
        this.networkInterface.setServerTimeout(5000);
    }

    public void connect(@Nullable NetworkCallback callback){
        if (!this.lock())
            return;
        this.networkInterface.connect(callback);
        this.unlock();
    }

    public void disconnect(@Nullable NetworkCallback callback){
        if (!this.lock())
            return;
        this.networkInterface.disconnect(callback);
        this.unlock();
    }

    public void sync(@Nullable NetworkCallback callback){
        if (!this.lock())
            return;
        this.sync_callback = callback;
        this.networkInterface.sendForAnswer("@va", this);
        this.unlock();
    }

    public void stopWorker(){
        this.networkInterface.stopWorker();
    }

    @Override
    public void error(Exception e) {

    }

    @Override
    public void done(NetworkCallback.job_type job_type, String[] results) {
        if (job_type == NetworkCallback.job_type.SEND_FOR_RESPONSE){
            //@NETWORKING_LOGLog.v(cN + ".done()", "Received \"" + results[0] + "\"");

            this.sync_callback.done(job_type, results);
        }
    }

    private boolean lock(){
        try{
            Log.v(cN + ".lock()", "Locking sync mutex...");
            this.syncMutex.acquire();
            return true;
        } catch (InterruptedException e){
            Log.e(cN + ".lock()", "Failed to lock sync mutex!!!");
            return false;
        }
    }

    private void unlock(){
        this.syncMutex.release();
    }
}
