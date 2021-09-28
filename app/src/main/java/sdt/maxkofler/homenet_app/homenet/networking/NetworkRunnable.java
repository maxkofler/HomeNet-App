package sdt.maxkofler.homenet_app.homenet.networking;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.networking.Networking;

public class NetworkRunnable implements Runnable {
    private static final String cN = "HomeNet-App:NetworkRunnable";
    private Networking networking;

    private Networking.job_type current_job;
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
        waitForFinish();
        current_job = Networking.job_type.CONNECT;
        this.callback = callback;
        this.mutexGo.release();
    }

    public void deploySendForResponse(String message, @Nullable NetworkCallback callback){
        waitForFinish();
        this.current_job = Networking.job_type.SEND_FOR_RESPONSE;
        this.args = new String[]{message};
        this.callback = callback;
        this.mutexGo.release();
    }

    @Override
    public void run(){
        {//Wait for some work to come in and set work in progress mutex
            try { this.mutexGo.acquire(); } catch (InterruptedException e) {}

            if (!this.run)
                return;

            try { this.mutexWIP.acquire(); } catch (InterruptedException e) {}
        }

        boolean ok = false;
        {//Do the actual work
            try{
                switch (this.current_job){
                    case CONNECT:{
                        Log.d(cN + ".run()", "Job: CONNECT");
                        this.networking.connect();
                        this.results = new String[]{"OK"};
                        Log.d(cN + ".run()", "Job: CONNECT done!");
                    }
                    case SEND_FOR_RESPONSE:{
                        Log.d(cN + ".run()", "Job: SEND_FOR_RESPONSE");
                        this.results = new String[]{this.networking.sendForResponse(args[0])};
                        Log.d(cN + ".run()", "Job: SEND_FOR_RESPONSE done!");
                    }
                }
                ok = true;
            } catch (ConnectException e){
                //Try calling the callback, else output the error
                if (this.callback != null)
                    this.callback.error(e);
                else{
                    Log.e(cN + ".run()", "UNHANDLED EXCEPTION: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        }

        if (ok){//Call the callback and release mutexes
            if (this.callback != null)
                this.callback.done(this.current_job, this.results.clone());

            this.mutexGo.release();
            this.mutexWIP.release();
        }
    }

    private void waitForFinish(){
        Log.d(cN + ".deploy", "Waiting for previous workload to finish...");
        try {
            this.mutexWIP.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.mutexWIP.release();
        }
        Log.d(cN + ".deploy", "Previous workload finished!");
    }
}
