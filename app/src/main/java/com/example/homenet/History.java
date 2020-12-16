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
    private WSValueserver vServer;
    private boolean connectedToServer;

    private ProgressDialog dialog;

    int id = 0;

    TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportActionBar().setTitle(getString(R.string.title_history));

        Intent intent = getIntent();

        preferences = getSharedPreferences(getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
        prefseditor = preferences.edit();

        id = intent.getIntExtra("ID", 0);

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

                vServer = new WSValueserver(ip, port);

                try{
                    vServer.init(false);
                    connectedToServer = true;
                }catch (NoConnectionToWSServer e){
                    Toast.makeText(getApplicationContext(), getString(R.string.err_no_connection_to_server), Toast.LENGTH_LONG).show();
                }

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvName.setText(vServer.getValueName(id));

                System.out.println("ID: " + id);

                History_Manager hManager = new History_Manager(ip, port, id, getApplicationContext());
                History_Manager.History_entry[] history_entries = hManager.smoothcourve(hManager.getHistory_entries(), preferences.getInt(getString(R.string.key_countValuesHistory), 100));
                if (history_entries == null){
                    return;
                }

                AnyChartView anyChartView = findViewById(R.id.any_chart_view);
                anyChartView.setZoomEnabled(true);

                //anyChartView.clear();
                Cartesian cartesian = AnyChart.line();

                cartesian.animation(true);


                //cartesian.yAxis(0).title("Number of Bottles Sold (thousands)");
                //cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

                List<DataEntry> seriesData = new ArrayList<>();
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


                Set set = Set.instantiate();
                set.data(seriesData);
                Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");


                Line series1 = cartesian.line(series1Mapping);
                series1.name(vServer.getValueName(id) + " in " + vServer.getValueUnit(id));


                cartesian.legend().enabled(true);
                cartesian.legend().fontSize(10d);

                anyChartView.setChart(cartesian);
                anyChartView.invalidate();
            }
        });


    }
}