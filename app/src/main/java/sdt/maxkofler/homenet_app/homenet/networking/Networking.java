package sdt.maxkofler.homenet_app.homenet.networking;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;

public class Networking {
    private static final String cN = "HomeNet-App:Networking";
    private Socket socket;
    private String serverAddress;
    private int serverPort;
    private int timeout;

    public enum job_type{
        CONNECT,
        SEND_FOR_RESPONSE
    }

    public Networking(){
        this.timeout = 1000;
        this.serverAddress = "";
        this.serverPort = 0;
    }

    public void connect() throws ConnectException {

        {//Check critical values
            if (this.serverAddress.isEmpty())
                throw new ConnectException(ConnectException.SERVER_ADDRESS_NOT_SET);

            if (this.serverPort == 0)
                throw new ConnectException(ConnectException.SERVER_PORT_NOT_SET);
        }

        {//Try connecting
            this.socket = new Socket();
            SocketAddress address = new InetSocketAddress(this.serverAddress, this.serverPort);
            try{
                this.socket.connect(address, this.timeout);
            } catch (SocketTimeoutException e) {
                Log.e(cN + ".connect()", "Socket timed out after " + this.timeout + "ms: " + e.getMessage());
                throw new ConnectException(ConnectException.SERVER_TIMED_OUT + ": " + e.getMessage());
            } catch (IOException e){
                Log.e(cN + ".connect()", "Socket connection had IOException: " + e.getMessage());
                throw new ConnectException(ConnectException.SERVER_IOEXCEPTION + ": " + e.getMessage());
            }
        }
    }
    public void connect(String address, int port, int timeout) throws ConnectException{
        {//Set values
            this.setServerAddress(address);
            this.setServerPort(port);
            this.setTimeout(timeout);
        }

        {//Try connecting
            this.connect();
        }
    }

    public String sendForResponse(String message){
        return "";
    }

    public void setServerAddress(String address){
        this.serverAddress = address;
    }

    public void setServerPort(int port){
        this.serverPort = port;
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getTimeout() {
        return timeout;
    }
}
