package com.example.homenet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

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

    private void init(Context context){
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.layout_valueview, this);

        tv_desc_bigValue = findViewById(R.id.desc_bigValue);
        tv_bigValue = findViewById(R.id.bigValue);
        tv_desc_smValue1 = findViewById(R.id.desc_sValue1);
        tv_smValue1 = findViewById(R.id.sValue1);
        tv_desc_smValue2 = findViewById(R.id.desc_sValue2);
        tv_smValue2 = findViewById(R.id.sValue2);
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
            setBigValueDesc(vServer.getValueName(bigVID));
            setBigValue(vServer.getValue(bigVID) + " " + vServer.getValueUnit(bigVID));

            setSValue1Desc(vServer.getValueName(sV1ID));
            setSValue1(vServer.getValue(sV1ID) + " " + vServer.getValueUnit(sV1ID));

            setSValue2Desc(vServer.getValueName(sV2ID));
            setSValue2(vServer.getValue(sV2ID) + " " + vServer.getValueUnit(sV2ID));
        }else{
            System.err.println("Not refreshing tiles! -> no data available!");
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        System.out.println("Event!");
        openChooseDialog();
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("Event!");
        openChooseDialog();
        return super.dispatchKeyEvent(event);
    }

    public void openChooseDialog(){
        System.out.println("Opening Choose dialog!");
        Intent intent = new Intent(getContext(), ConfigureValueDisplay.class);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        intent.putExtra("ID", ID);
        context.startActivity(intent);
    }
}
