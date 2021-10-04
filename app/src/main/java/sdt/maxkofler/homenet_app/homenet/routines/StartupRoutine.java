package sdt.maxkofler.homenet_app.homenet.routines;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.MainActivity;
import sdt.maxkofler.homenet_app.SettingsActivity;
import sdt.maxkofler.homenet_app.homenet.HomeNet;
import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkCallback;

public class StartupRoutine implements NetworkCallback {
    private static final String cN = "HomeNet-App:StartupRoutine";
    private Context context;
    private MainActivity mainActivity;
    private HomeNet homenet;

    private ErrorHandler errorHandlerThread;
    private Semaphore errorHandlerRunMutex;

    public StartupRoutine(Context context, HomeNet homenet, MainActivity mainActivity){
        this.homenet = homenet;
        this.context = context;
        this.mainActivity = mainActivity;
        this.errorHandlerRunMutex = new Semaphore(1);
        stopErrorHandler();
        this.errorHandlerThread = new ErrorHandler(this.context, this.errorHandlerRunMutex);
        this.errorHandlerThread.start();

        Log.d(cN, "Connecting to server...");
        this.homenet.connect(this);
    }


    @Override
    public void done(job_type job_type, String[] results) {

    }

    @Override
    public void error(job_type job_type, Exception e) {
        Log.w(cN + ".error()", "Releasing handler...");
        this.errorHandlerThread.handle(job_type, e);
    }

    private class ErrorHandler extends Thread{
        private static final String cN = "HomeNet-App:StartupRoutine:ErrorHandler";
        private Context context;
        private Semaphore runMutex;
        private boolean run;

        private NetworkCallback.job_type job_type;
        private Exception thrownException;

        public ErrorHandler(Context context, Semaphore runMutex){
            this.runMutex = runMutex;
            this.context = context;
            this.run = true;
        }

        public void handle(NetworkCallback.job_type job_type, Exception e){
            this.job_type = job_type;
            this.thrownException = e;

            //Release run
            startErrorHandler();
        }

        @Override
        public void run(){
            while (this.run){
                {
                    Log.d(cN + ".run()", "Waiting for work to be released...");
                    try {
                        this.runMutex.acquire();
                    } catch (InterruptedException e) {
                        Log.e(cN + ".run()", "Thread was interrupted, leaving run loop!");
                        return;
                    }
                    Log.d(cN + ".run()", "Starting to do work...");
                }

                {
                    switch (this.job_type){
                        case CONNECT:{
                            Log.d(cN + ".run()", "Error happened while connecting: " + this.thrownException.getMessage());
                            if (    this.thrownException.getMessage().equals(ConnectException.SERVER_IOEXCEPTION) ||
                                    this.thrownException.getMessage().equals(ConnectException.SERVER_TIMED_OUT) ||
                                    this.thrownException.getMessage().equals(ConnectException.SERVER_ADDRESS_NOT_SET) ||
                                    this.thrownException.getMessage().equals(ConnectException.SERVER_PORT_NOT_SET)){

                                Log.d(cN + ".run()", "Starting settings to let the user correct its mistake");
                                //Launch settings to adjust whatever went wrong
                                Intent intent = new Intent(this.context, SettingsActivity.class);
                                mainActivity.launchActivity(intent);
                            }
                        }
                    }
                }

                {
                    this.runMutex.release();
                    try {
                        this.runMutex.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void startErrorHandler(){
        this.errorHandlerRunMutex.release();
    }

    private void stopErrorHandler(){
        try{
            this.errorHandlerRunMutex.acquire();
        } catch(InterruptedException e){

        }
    }
}
