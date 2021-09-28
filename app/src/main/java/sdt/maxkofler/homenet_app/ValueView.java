package sdt.maxkofler.homenet_app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import sdt.maxkofler.homenet_app.R;

public class ValueView extends ConstraintLayout {
    Context context;
    LayoutInflater viewInflater;

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
    }

}
