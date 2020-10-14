package com.example.homenet.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.homenet.R;
import com.example.homenet.weathersens.WSValueserver;

public class ConfigureValueDisplay extends AppCompatActivity {

    Spinner spBigV;
    int ID = 0;

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_value_display);

        spBigV = findViewById(R.id.sp_bigID);

        Intent intent = getIntent();

        ID = intent.getIntExtra("ID", 0);

        WSValueserver vServer = new WSValueserver(intent.getStringExtra("ip"), intent.getIntExtra("port", 8090));
        vServer.init();

        String[] valueNames = new String[vServer.getValuesCount()];
        for (int i = 0; i < vServer.getValuesCount(); i++){
            valueNames[i] = vServer.getValueName(i);
        }

        preferences = getSharedPreferences("valueView" + ID, Context.MODE_PRIVATE);
        prefseditor = preferences.edit();
        int bigVID = preferences.getInt("bigVID", 0);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valueNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spBigV.setAdapter(adapter);
        spBigV.setSelection(bigVID);

        spBigV.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefseditor.putInt("bigVID", position);
                prefseditor.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}