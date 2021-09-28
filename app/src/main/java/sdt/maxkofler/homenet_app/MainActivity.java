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

public class MainActivity extends Activity implements NetworkCallback {

    // Used to load the 'homenet_app' library on application startup.
    static {
        System.loadLibrary("homenet_app");
    }

    private LinearLayout valuesLayout;

    private HomeNet homeNet;

    private Semaphore waitUntilConnect;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.homeNet = new HomeNet("10.8.0.3", 8080);

        homeNet.connect(this);

        this.valuesLayout = findViewById(R.id.values_layout);
    }

    /**
     * A native method that is implemented by the 'homenet_app' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void done(Networking.job_type job, String[] res) {
        if (job == Networking.job_type.CONNECT){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 3; i++){
                        valuesLayout.addView(new ValueView(getApplicationContext(), 0));
                    }
                }
            });
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