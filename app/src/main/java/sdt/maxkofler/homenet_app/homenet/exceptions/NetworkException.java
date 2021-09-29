package sdt.maxkofler.homenet_app.homenet.exceptions;

public class NetworkException extends Exception{
    public static final String NOT_CONNECTED = "The socket was is not connected";

    public NetworkException(String msg){
        super(msg);
    }
}
