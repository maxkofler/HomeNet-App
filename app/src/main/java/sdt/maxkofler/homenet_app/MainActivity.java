package sdt.maxkofler.homenet_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.HomeNet;
import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.exceptions.NetworkException;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkCallback;
import sdt.maxkofler.homenet_app.homenet.networking.Networking;
import sdt.maxkofler.homenet_app.homenet.routines.StartupRoutine;

//Search for "@NETWORKING_LOG" to find commented out networking log calls (autoreplace with "" to enable all)

public class MainActivity extends Activity implements NetworkCallback{
    private static final String cN = "HomeNet-App:Main";

    // Used to load the 'homenet_app' library on application startup.
    static {
        System.loadLibrary("homenet_app");
    }

    /**
     * A native method that is implemented by the 'homenet_app' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private MainActivity self;

    private LinearLayout valuesLayout;

    private HomeNet homeNet;

    private Semaphore wait;
    private ProgressDialog progress;

    private StartupRoutine startupRoutine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.self = this;
        this.wait = new Semaphore(1);


        this.homeNet = new HomeNet("10.8.0.34", 8080);

        //homeNet.connect(null);

        this.startupRoutine = new StartupRoutine(getApplicationContext(), this.homeNet, this);

        this.valuesLayout = findViewById(R.id.values_layout);
    }

    @Override
    public void done(job_type job_type, String[] results) {
        if (job_type.equals(NetworkCallback.job_type.DISCONNECT)){
            this.wait.release();
        }
    }

    @Override
    public void error(job_type job_type, Exception e) {
        if (job_type.equals(NetworkCallback.job_type.DISCONNECT)){
            this.wait.release();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(cN + ".onDestroy()", "Quiting HomeNet-App started...");

        try {
            this.wait.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.homeNet.disconnect(this);
        Log.d(cN + ".onDestroy()", "Waiting for disconnect to finish...");
        try {
            this.wait.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(cN + ".onDestroy()", "Disconnect succeeded, continuing to finish...");

        this.homeNet.stopWorker();
        this.homeNet = null;

        //Run a final garbage collector
        System.gc();

        Log.i(cN + ".onDestroy()", "Quited HomeNet-App, bye!");
        super.onDestroy();
    }

    public void launchActivity(Intent intent){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                System.out.println("Done launching activity!");
            }
        });
    }
}