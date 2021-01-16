package com.example.homenet.ExceptionClasses;

public class NoValuesToProcess extends Exception{
    private String msg;

    public NoValuesToProcess(String msg){
        this.msg = msg;
    }

    public String getMsg(){return msg;}
}
