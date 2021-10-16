package sdt.maxkofler.homenet_app;

import android.util.Log;
import android.widget.LinearLayout;

import java.util.Vector;

import sdt.maxkofler.homenet_app.homenet.HomeNet;
import sdt.maxkofler.homenet_app.homenet.homenet.HNValue;

public class ValuesManager {
    private static final String cN = "HomeNet-App:ValuesManager";
    MainActivity mainActivity;
    LinearLayout valuesLayout;
    HomeNet homeNet;

    Vector<ValueView> values;

    public ValuesManager(MainActivity mainActivity, LinearLayout valuesLayout, HomeNet homeNet){
        this.mainActivity = mainActivity;
        this.valuesLayout = valuesLayout;
        this.homeNet = homeNet;
        this.values = new Vector<>();
    }

    public boolean addValues(int amount){
        for (int i = 0; i < amount; i++){
            this.values.add(new ValueView(this.mainActivity.getApplicationContext(), 1));
        }
        this.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < amount; i++){
                    valuesLayout.addView(values.elementAt(i));
                }
            }
        });
        return true;
    }

    public boolean setValue(int valueId, ValueView.Value pos, HNValue value){
        if (valueId < 0 || valueId >= this.values.size()) {
            Log.e(cN + ".setValue()", "Invalid valueId to set value: " + valueId);
            return false;
        }

        this.values.elementAt(valueId).setValue(pos, value);
        return true;
    }
}
