package com.example.homenet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;

import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.homenet.ExceptionClasses.NoConnectionToWSServer;
import com.example.homenet.weathersens.History_Manager;
import com.example.homenet.weathersens.WSValueserver;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;

    private String ip = "192.168.1.24";
    private int port = 8090;

    private ProgressDialog dialog;

    int id = 0;

    TextView tvName;

    private String name;
    private String unit;

    private History_Manager.History_entry history_entries[];
    List<DataEntry> seriesData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().setTitle(getString(R.string.title_history));

        Intent intent = getIntent();

        preferences = getSharedPreferences(getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
        prefseditor = preferences.edit();

        id = intent.getIntExtra("ID", 0);
        name = intent.getStringExtra("name");
        unit = intent.getStringExtra("unit");

        ip = preferences.getString(getString(R.string.key_ServerIP), "192.168.1.24");
        port = preferences.getInt(getString(R.string.key_ServerPort), 8090);

        dialog=new ProgressDialog(History.this);
        dialog.setMessage("Lade...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvName = findViewById(R.id.tvValueName);
                setChart(id);
                dialog.hide();
            }
        }, 100);

    }



    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value) {
            super(x, value);
        }

    }

    private void setChart(final int id){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int countHistoryValues = preferences.getInt(getString(R.string.key_countValuesHistory), 100);
                int countSecLookBack = preferences.getInt(getString(R.string.key_historyLookBackSec), 24*3600);
                History_Manager hManager = new History_Manager(ip, port, id, countSecLookBack, getApplicationContext());

                history_entries = hManager.getHistory_entries();
                history_entries = hManager.stripInvalidEntries(history_entries);
                history_entries = hManager.cutToLookbackWindow(history_entries, countSecLookBack, countHistoryValues);
                history_entries = hManager.smoothcurve(history_entries, countHistoryValues);
                if (history_entries == null){
                    Log.e("homenet", "History entries returned from HistoryManager.getHistory_entries() are NULL!");
                    Toast.makeText(getApplicationContext(), getString(R.string.err_recString_not_smoothable), Toast.LENGTH_LONG).show();
                    return;
                }


                seriesData = new ArrayList<>();
                String time;
                String value;
                String type;
                float valueF;

                type = history_entries[history_entries.length-1].getType();
                if (type == "int" && type == "float"){

                }else{
                    System.out.println("Type is not int or float, trying anyway to convert...");
                }

                for (int i = 0; i < history_entries.length; i++){
                    time = history_entries[i].getTimeStamp();
                    value = history_entries[i].getValue();
                    try{
                        valueF = Float.parseFloat(value);
                    }catch(NumberFormatException e){
                        System.out.println("Conversion failed! Breaking...");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.err_conversion_not_possible), Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
                    seriesData.add(new CustomDataEntry(time, valueF));
                }
            }
        });
        thread.start();
        thread.setPriority(Thread.MAX_PRIORITY);
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvName.setText(name);

                System.out.println("ID: " + id);

                AnyChartView anyChartView = findViewById(R.id.any_chart_view);
                anyChartView.setZoomEnabled(true);

                //anyChartView.clear();
                Cartesian cartesian = AnyChart.line();

                cartesian.animation(false);



                Set set = Set.instantiate();
                set.data(seriesData);
                Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");


                Line series1 = cartesian.line(series1Mapping);
                series1.name(name + " in " + unit);


                cartesian.legend().enabled(true);
                cartesian.legend().fontSize(10d);

                anyChartView.setChart(cartesian);
                anyChartView.invalidate();
            }
        });


    }
}