package com.example.homenet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.homenet.weathersens.WSValueserver;

public class ValueView extends ConstraintLayout {



    private TextView tv_desc_bigValue;
    private TextView tv_bigValue;
    private TextView tv_desc_smValue1;
    private TextView tv_smValue1;
    private TextView tv_desc_smValue2;
    private TextView tv_smValue2;

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;

    private int ID = -1;

    private int bigVID = 0;
    private int sV1ID = 0;
    private int sV2ID = 0;

    private String bigVName;
    private String sV1Name;
    private String sV2Name;

    private String bigVUnit;
    private String sV1Unit;
    private String sV2Unit;

    String ip;
    int port;

    private View.OnClickListener listener;

    Context context;

    public ValueView(Context context) {
        super(context);
        init(context);
    }

    public ValueView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ValueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ValueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(final Context context){
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.layout_valueview, this);

        tv_desc_bigValue = (TextView)findViewById(R.id.desc_bigValue);
        tv_bigValue = (TextView)findViewById(R.id.bigValue);
        tv_desc_smValue1 = (TextView)findViewById(R.id.desc_sValue1);
        tv_smValue1 = (TextView)findViewById(R.id.sValue1);
        tv_desc_smValue2 = (TextView)findViewById(R.id.desc_sValue2);
        tv_smValue2 = (TextView)findViewById(R.id.sValue2);

        tv_bigValue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory(bigVID, bigVName, bigVUnit);
            }
        });
        tv_bigValue.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openChooseDialog();
                return false;
            }
        });

        tv_smValue1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory(sV1ID, sV1Name, sV1Unit);
            }
        });
        tv_smValue1.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openChooseDialog();
                return false;
            }
        });

        tv_smValue2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory(sV2ID, sV2Name, sV2Unit);
            }
        });
        tv_smValue2.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openChooseDialog();
                return false;
            }
        });


        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openChooseDialog();
                return false;
            }
        });
    }

    public void initialize(int id, String ip, int port){
        this.ip = ip;
        this.port = port;
        this.ID = id;
        preferences = getContext().getSharedPreferences("valueView" + ID, Context.MODE_PRIVATE);
        prefseditor = preferences.edit();

        bigVID = preferences.getInt("bigVID", 0);
        sV1ID = preferences.getInt("sV1ID", 1);
        sV2ID = preferences.getInt("sV2ID", 2);
    }

    public void setBigValue(String text){ tv_bigValue.setText(text); }
    public void setBigValueDesc(String text){ tv_desc_bigValue.setText(text); }

    public void setSValue1(String text){ tv_smValue1.setText(text); }
    public void setSValue1Desc(String text){ tv_desc_smValue1.setText(text); }

    public void setSValue2(String text){ tv_smValue2.setText(text); }
    public void setSValue2Desc(String text){ tv_desc_smValue2.setText(text); }

    public void setValues(WSValueserver vServer){
        if (vServer.getDataAvailable()){
            try {
                bigVName = vServer.getValueName(bigVID);
                bigVUnit = vServer.getValueUnit(bigVID);

                sV1Name = vServer.getValueName(sV1ID);
                sV1Unit = vServer.getValueUnit(sV1ID);

                sV2Name = vServer.getValueName(sV2ID);
                sV2Unit = vServer.getValueUnit(sV2ID);

                setBigValueDesc(vServer.getValueName(bigVID));
                setBigValue(vServer.getValue(bigVID) + " " + vServer.getValueUnit(bigVID));

                setSValue1Desc(vServer.getValueName(sV1ID));
                setSValue1(vServer.getValue(sV1ID) + " " + vServer.getValueUnit(sV1ID));

                setSValue2Desc(vServer.getValueName(sV2ID));
                setSValue2(vServer.getValue(sV2ID) + " " + vServer.getValueUnit(sV2ID));
            }catch(ArrayIndexOutOfBoundsException e){
                Log.e("homenet-setValues()", "Unable to get Value contents!");
                e.printStackTrace();
            }
        }else{
            System.err.println("Not refreshing tiles! -> no data available!");
        }
    }


    public void openChooseDialog(){
        Intent intent = new Intent(getContext(), ConfigureValueDisplay.class);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        intent.putExtra("ID", ID);
        context.startActivity(intent);
    }

    public void openHistory(int id, String name, String unit){
        Intent intent = new Intent(getContext(), History.class);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        intent.putExtra("ID", id);
        intent.putExtra("name", name);
        intent.putExtra("unit", unit);
        context.startActivity(intent);
    }
}
