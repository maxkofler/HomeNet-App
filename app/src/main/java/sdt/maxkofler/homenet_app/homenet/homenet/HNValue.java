package sdt.maxkofler.homenet_app.homenet.homenet;

import android.util.Log;

import java.util.Vector;

public class HNValue {
    private static final String cN = "HomeNet-App:HNValue";

    private String driverName;
    private int driverId;
    private String description;
    private int valueId;
    private char dataType;
    private String displayType;
    private String value;
    private String unit;

    public HNValue(String description, String value, String unit){
        this.description = description;
        this.value = value;
        this.unit = unit;
    }

    public HNValue(){

    }

    public boolean fetch(Vector<String> args){
        if (args.size() != 8){
            Log.e(cN + ".fetch()", "Invalid string array length to fetch: " + args.size() + "/8");
            return false;
        }

        this.driverName = args.elementAt(0);
        this.driverId = Integer.parseInt(args.elementAt(1));
        this.description = args.elementAt(2);
        this.valueId = Integer.parseInt(args.elementAt(3));
        this.dataType = args.elementAt(4).charAt(0);
        this.displayType = args.elementAt(5);
        this.value = args.elementAt(6);
        this.unit = args.elementAt(7);

        Log.v(cN + ".fetch()", "Fetched this value: " + this);

        return true;
    }

    public String getValueForDisplay(){
        return this.value + this.unit;
    }

    public String getDescription(){
        return this.description;
    }

    @Override
    public String toString(){
        return this.description + " " + this.value + " " + this.unit;
    }
}

