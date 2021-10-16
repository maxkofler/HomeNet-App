package sdt.maxkofler.homenet_app.homenet;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.MainActivity;
import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.homenet.HNParser;
import sdt.maxkofler.homenet_app.homenet.homenet.HNValue;
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

    private Vector<HNValue> values;

    //Container for error messages for inner classes
    private String eMessage = "";

    public HomeNet(String address, int port){
        this.values = new Vector<>();
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
    public void error(job_type job, Exception e) {

    }

    @Override
    public void done(NetworkCallback.job_type job_type, String[] results) {
        if (job_type == NetworkCallback.job_type.SEND_FOR_RESPONSE && results.length == 1){
            //@NETWORKING_LOGLog.v(cN + ".done()", "Received \"" + results[0] + "\"");

            //The sync() call results in this piece of code
            HNParser parser = new HNParser();
            parser.parse(new BufferedReader(new StringReader(results[0])));

            this.values.clear();
            for (Vector<String> line : parser.getBlocks()){
                HNValue newValue = new HNValue();
                if (newValue.fetch(line)){
                    this.values.add(newValue);
                }
            }

            if (this.sync_callback != null)
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

    public int getValuesCount(){
        return this.values.size();
    }

    public Vector<HNValue> getValues(){
        return this.values;
    }
}
