package com.example.homenet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.homenet.ui.home.HomeFragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class Settings extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;

    EditText et_server_ip;
    EditText et_server_port;
    EditText et_cTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle(getString(R.string.title_settings));

        preferences = getSharedPreferences(getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
        prefseditor = preferences.edit();

        et_server_ip = findViewById(R.id.et_s_ip);
        et_server_port = findViewById(R.id.et_s_port);
        et_cTiles = findViewById(R.id.et_cTiles);

        loadChanges();

    }


    private void saveChanges(){
        prefseditor.putString(getString(R.string.key_ServerIP), et_server_ip.getText().toString());
        prefseditor.putInt(getString(R.string.key_ServerPort), Integer.parseInt(et_server_port.getText().toString()));
        prefseditor.putInt(getString(R.string.key_countTiles), Integer.parseInt(et_cTiles.getText().toString()));
        prefseditor.commit();
    }

    public static void triggerRebirth(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        //intent.putExtra(KEY_RESTART_INTENT, nextIntent);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }

    private void loadChanges(){
        et_server_ip.setText(preferences.getString(getString(R.string.key_ServerIP), "192.168.1.24"));
        et_server_port.setText(Integer.toString(preferences.getInt(getString(R.string.key_ServerPort), 8090)));
        et_cTiles.setText(Integer.toString(preferences.getInt(getString(R.string.key_countTiles), 2)));
    }

    @Override
    public void onBackPressed() {
        System.out.println("Back pressed, promting dialog!");
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.s_exit_title))
                .setMessage(getString(R.string.s_exit_text))
                .setNegativeButton(R.string.s_exit_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.err.println("Discarding changes!");
                        finish();
                    }
                })
                .setPositiveButton(R.string.s_exit_yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        System.out.println("Saving changes!");
                        saveChanges();
                        finish();
                    }
                }).create().show();
    }
}