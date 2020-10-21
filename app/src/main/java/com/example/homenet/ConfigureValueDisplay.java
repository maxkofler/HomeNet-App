package com.example.homenet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.homenet.ExceptionClasses.NoConnectionToWSServer;
import com.example.homenet.R;
import com.example.homenet.weathersens.WSValueserver;

public class ConfigureValueDisplay extends AppCompatActivity {

    Spinner spBigV;
    Spinner spSV1;
    Spinner spSV2;
    int ID = 0;

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_value_display);

        spBigV = findViewById(R.id.sp_bigID);
        spSV1 = findViewById(R.id.sp_sID1);
        spSV2 = findViewById(R.id.sp_sID2);

        Intent intent = getIntent();

        ID = intent.getIntExtra("ID", 0);

        WSValueserver vServer = new WSValueserver(intent.getStringExtra("ip"), intent.getIntExtra("port", 8090));
        try {
            vServer.init();
        } catch (NoConnectionToWSServer noConnectionToWSServer) {
            Toast.makeText(getApplicationContext(), getString(R.string.err_no_connection_to_server), Toast.LENGTH_LONG).show();
        }

        String[] valueNames = new String[vServer.getValuesCount()];
        for (int i = 0; i < vServer.getValuesCount(); i++){
            valueNames[i] = vServer.getValueName(i);
        }

        preferences = getSharedPreferences("valueView" + ID, Context.MODE_PRIVATE);
        prefseditor = preferences.edit();
        int bigVID = preferences.getInt("bigVID", 0);
        int sV1ID = preferences.getInt("sV1ID", 1);
        int sV2ID = preferences.getInt("sV2ID", 2);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, valueNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spBigV.setAdapter(adapter);
        spBigV.setSelection(bigVID);

        spSV1.setAdapter(adapter);
        spSV1.setSelection(sV1ID);
        spSV2.setAdapter(adapter);
        spSV2.setSelection(sV2ID);

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