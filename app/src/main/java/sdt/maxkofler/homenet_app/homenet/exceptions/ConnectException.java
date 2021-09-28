package sdt.maxkofler.homenet_app.homenet.exceptions;

public class ConnectException extends Exception{

    public static final String SERVER_ADDRESS_NOT_SET = "The server address was not set";
    public static final String SERVER_PORT_NOT_SET = "The server port was not set";
    public static final String SERVER_TIMED_OUT = "The server connection timed out";
    public static final String SERVER_IOEXCEPTION = "The server connection threw IOException";

    public ConnectException(String msg){
        super(msg);
    }

}
