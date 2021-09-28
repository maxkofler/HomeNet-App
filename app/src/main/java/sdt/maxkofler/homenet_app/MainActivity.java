package sdt.maxkofler.homenet_app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import sdt.maxkofler.homenet_app.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    // Used to load the 'homenet_app' library on application startup.
    static {
        System.loadLibrary("homenet_app");
    }

    private LinearLayout valuesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.valuesLayout = findViewById(R.id.values_layout);

        for (int i = 0; i < 3; i++){
            this.valuesLayout.addView(new ValueView(getApplicationContext(), 0));
        }
    }

    /**
     * A native method that is implemented by the 'homenet_app' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}