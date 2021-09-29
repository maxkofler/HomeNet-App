package sdt.maxkofler.homenet_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.HomeNet;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkCallback;
import sdt.maxkofler.homenet_app.homenet.networking.Networking;

//Search for "@NETWORKING_LOG" to find commented out networking log calls (autoreplace with "" to enable all)

public class MainActivity extends Activity implements NetworkCallback {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.self = this;
        this.wait = new Semaphore(1);

        this.homeNet = new HomeNet("10.8.0.3", 8080);

        homeNet.connect(this);

        this.valuesLayout = findViewById(R.id.values_layout);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(cN + ".onStop()", "Quiting HomeNet-App started...");

        try {
            this.wait.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.homeNet.disconnect(this);
        Log.d(cN + ".onStop()", "Waiting for disconnect to finish...");
        try {
            this.wait.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(cN + ".onStop()", "Disconnect succeeded, continuing to finish...");

        this.homeNet.stopWorker();
        this.homeNet = null;


        //Run a final garbage collector
        System.gc();

        Log.i(cN + ".onStop()", "Quited HomeNet-App, bye!");
    }

    @Override
    public void done(NetworkCallback.job_type job, String[] res) {
        if (job == NetworkCallback.job_type.CONNECT){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 3; i++){
                        valuesLayout.addView(new ValueView(getApplicationContext(), 0));
                    }
                }
            });

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    homeNet.sync(self);
                }
            });
            t.start();
        }

        if (job == job_type.DISCONNECT){
            this.wait.release();
        }
    }

    @Override
    public void error(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Failed to connect!", Toast.LENGTH_LONG).show();
            }
        });
        finish();
    }
}