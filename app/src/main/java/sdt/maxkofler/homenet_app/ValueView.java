package sdt.maxkofler.homenet_app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import sdt.maxkofler.homenet_app.R;
import sdt.maxkofler.homenet_app.homenet.homenet.HNValue;

public class ValueView extends ConstraintLayout {
    private static final String cN = "HomeNet-App:ValueView";
    Context context;
    LayoutInflater viewInflater;

    TextView tv_sV1;
    TextView tv_d_sV1;
    TextView tv_sV2;
    TextView tv_d_sV2;
    TextView tv_bV;
    TextView tv_d_bV;

    public ValueView(Context context, int rot) {
        super(context);
        init(context, rot);
    }

    public ValueView(Context context, @Nullable AttributeSet attrs, int rot) {
        super(context, attrs);
        init(context, rot);
    }

    public ValueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int rot) {
        super(context, attrs, defStyleAttr);
        init(context, rot);
    }

    public ValueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes, int rot) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, rot);
    }

    private void init(final Context context, int orientation){
        this.context = context;
        this.viewInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.viewInflater.inflate(R.layout.layout_valueview_protrait, this);

        this.tv_sV1 = findViewById(R.id.tv_sValue1);
        this.tv_d_sV1 = findViewById(R.id.tv_d_sValue1);

        this.tv_sV2 = findViewById(R.id.tv_sValue2);
        this.tv_d_sV2 = findViewById(R.id.tv_d_sValue2);

        this.tv_bV = findViewById(R.id.tv_bValue);
        this.tv_d_bV = findViewById(R.id.tv_d_bValue);
    }

    public enum Value{
        sV1,
        sV2,
        bV
    }

    public void setValue(Value position, HNValue value){
        Log.v(cN + ".setValue()", "Setting value at pos " + position.name() + ": " + value);
        switch (position){
            case sV1:{
                this.tv_sV1.setText(value.getValueForDisplay());
                this.tv_d_sV1.setText(value.getDescription());
                break;
            }
            case sV2:{
                this.tv_sV2.setText(value.getValueForDisplay());
                this.tv_d_sV2.setText(value.getDescription());
                break;
            }
            case bV:{
                this.tv_bV.setText(value.getValueForDisplay());
                this.tv_d_bV.setText(value.getDescription());
                break;
            }
            default:{
                Log.e(cN + ".setValue()", "Invalid value position: " + position.ordinal());
            }
        }
    }

}
