package sdt.maxkofler.homenet_app.homenet.networking;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.exceptions.NetworkException;
import sdt.maxkofler.homenet_app.homenet.networking.Networking;

public class NetworkRunnable implements Runnable {
    private static final String cN = "HomeNet-App:NetworkRunnable";
    private Networking networking;

    private NetworkCallback.job_type current_job;
    private NetworkCallback callback;

    private Semaphore mutexGo;
    private Semaphore mutexWIP;
    private boolean run;
    private String[] args;
    private String[] results;

    public NetworkRunnable(Networking networking){
        this.networking = networking;

        {//Initialize mutexes
            this.run = true;
            this.mutexGo = new Semaphore(1);
            this.mutexWIP = new Semaphore(1);
            try{
                this.mutexGo.acquire();
            } catch (InterruptedException e){}
            this.mutexWIP.release();
        }

    }

    public void deployConnect(@Nullable NetworkCallback callback){
        waitForFinish("CONNECT");
        Log.v(cN + ".deploy()", "Deploying job: CONNECT");
        current_job = NetworkCallback.job_type.CONNECT;
        this.callback = callback;
        this.mutexGo.release();
        Log.v(cN + ".deploy()", "Deployed job: CONNECT");
    }

    public void deployDisconnect(@Nullable NetworkCallback callback){
        waitForFinish("DISCONNECT");
        Log.v(cN + ".deploy()", "Deploying job: DISCONNECT");
        current_job = NetworkCallback.job_type.DISCONNECT;
        this.callback = callback;
        this.mutexGo.release();
        Log.v(cN + ".deploy()", "Deployed job: DISCONNECT");
    }

    public void deploySendForResponse(String message, @Nullable NetworkCallback callback){
        waitForFinish("SEND_FOR_RESPONSE");
        Log.v(cN + ".deploy()", "Deploying job: SEND_FOR_RESPONSE");
        this.current_job = NetworkCallback.job_type.SEND_FOR_RESPONSE;
        this.args = new String[1];
        this.args[0] = new String(message);
        this.callback = callback;
        Log.v(cN + ".deploy()", "Deployed job: SEND_FOR_RESPONSE");
        this.mutexGo.release();
    }

    public void stopWorker(){
        waitForFinish("Stop worker");
        run = false;
        this.mutexGo.release();
    }

    @Override
    public void run(){
        while(this.run){
            {//Wait for some work to come in and set work in progress mutex
                try { this.mutexGo.acquire(); } catch (InterruptedException e) {}

                if (!this.run) {
                    Log.d(cN + ".run()", "Exiting worker");
                    return;
                }

                Log.d(cN + ".run()", "Starting to do some work...");
                try { this.mutexWIP.acquire(); } catch (InterruptedException e) {}
            }

            boolean ok = false;
            {//Do the actual work
                try{
                    switch (this.current_job){
                        case CONNECT:{
                            Log.d(cN + ".run()", "Job: CONNECT executing");
                            this.networking.connect();
                            this.results = new String[]{"OK"};
                            break;
                        }
                        case SEND_FOR_RESPONSE:{
                            Log.d(cN + ".run()", "Job: SEND_FOR_RESPONSE executing");
                            this.results = new String[1];
                            this.results[0] = this.networking.sendForResponse(args[0]);
                            break;
                        }
                        case DISCONNECT:{
                            Log.d(cN + ".run()", "Job: DISCONNECT executing");
                            this.networking.disconnect();
                            break;
                        }
                    }
                    ok = true;
                } catch (ConnectException e){
                    //Try calling the callback, else output the error
                    if (this.callback != null) {
                        this.callback.error(this.current_job, e);
                        Log.d(cN + ".run(exception)", "Error callback joined!");
                    }
                    else{
                        Log.e(cN + ".run()", "UNHANDLED CONNECT EXCEPTION: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (NetworkException e){
                    //Try calling the callback, else output the error
                    if (this.callback != null) {
                        this.callback.error(this.current_job, e);
                        Log.d(cN + ".run(exception)", "Error callback joined!");
                    }
                    else{
                        Log.e(cN + ".run()", "UNHANDLED NETWORK EXCEPTION: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            if (ok){//Call the callback and release mutexes
                Log.d(cN + ".run()", "Job done!");
                if (this.callback != null)
                    if (this.results != null)
                        this.callback.done(this.current_job, this.results.clone());
                    else
                        this.callback.done(this.current_job, null);

                //this.mutexGo.release();
                //this.mutexWIP.release();
            }

            {//Lock this loop again until a deploy function releases it and this loop can do work
                this.mutexWIP.release();
                this.mutexGo.release();
                try {
                    this.mutexGo.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void resetMutexes(){

    }

    private void waitForFinish(String nextJob){
        Log.d(cN + ".deploy()", "Waiting for previous workload to finish, next job: " + nextJob + " current job: " + this.current_job);
        try {
            this.mutexWIP.acquire();
            Log.d(cN + ".deploy()", "Previous workload finished!");
            this.mutexWIP.release();
        } catch (InterruptedException e) {
            Log.e(cN + ".waitForFinish()", "Failed to acquire mutex: InterruptedException: " + e.getMessage());
        }
    }
}
