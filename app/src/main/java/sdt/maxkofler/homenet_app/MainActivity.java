package sdt.maxkofler.homenet_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

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
    private ValuesManager valuesManager;

    SharedPreferences preferences;
    SharedPreferences.Editor prefseditor;

    private Button buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.prefseditor = preferences.edit();

        this.self = this;
        this.wait = new Semaphore(1);

        this.buttonSettings = findViewById(R.id.main_btn_settings);
        this.buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });


        {
            String ip = this.preferences.getString(getString(R.string.key_server_ip), "localhost");
            int port = Integer.parseInt(this.preferences.getString(getString(R.string.key_server_port), "8080"));
            this.homeNet = new HomeNet(ip, port);
        }

        this.valuesLayout = findViewById(R.id.values_layout);

        this.valuesManager = new ValuesManager(this, this.valuesLayout, this.homeNet);

        this.startupRoutine = new StartupRoutine(getApplicationContext(), this.homeNet, this, this.valuesManager);
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