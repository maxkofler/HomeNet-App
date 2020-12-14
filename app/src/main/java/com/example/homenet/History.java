package com.example.homenet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        vServer = new WSValueserver(ip, port);

        try{
            vServer.init(false);
            connectedToServer = true;
        }catch (NoConnectionToWSServer e){
            Toast.makeText(getApplicationContext(), getString(R.string.err_no_connection_to_server), Toast.LENGTH_LONG).show();
        }

        tvName = findViewById(R.id.tvValueName);
        setChart(id);
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

                History_Manager hManager = new History_Manager(ip, port, id);
                History_Manager.History_entry[] history_entries = hManager.getHistory_entries();

                AnyChartView anyChartView = findViewById(R.id.any_chart_view);
                anyChartView.setZoomEnabled(true);

                //anyChartView.clear();
                Cartesian cartesian = AnyChart.line();

                cartesian.animation(true);

                cartesian.crosshair().enabled(true);
                cartesian.crosshair()
                        .yLabel(true)
                        // TODO ystroke
                        .yStroke((Stroke) null, null, null, (String) null, (String) null);


                //cartesian.yAxis(0).title("Number of Bottles Sold (thousands)");
                //cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

                List<DataEntry> seriesData = new ArrayList<>();
                int time;
                String value;

                for (int i = 0; i < history_entries.length; i++){
                    time = history_entries[i].getTime();
                    value = history_entries[i].getValue();
                    seriesData.add(new CustomDataEntry(Integer.toString(time), Float.parseFloat(value)));
                }


                Set set = Set.instantiate();
                set.data(seriesData);
                Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");


                Line series1 = cartesian.line(series1Mapping);
                series1.name(vServer.getValueName(id) + " in " + vServer.getValueUnit(id));


                cartesian.legend().enabled(true);
                cartesian.legend().fontSize(13d);
                cartesian.legend().padding(0d, 0d, 0d, 0d);

                anyChartView.setChart(cartesian);
                anyChartView.invalidate();
            }
        });


    }
}