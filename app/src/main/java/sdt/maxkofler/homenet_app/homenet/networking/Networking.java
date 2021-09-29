package sdt.maxkofler.homenet_app.homenet.networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.exceptions.NetworkException;

public class Networking {
    private static final String cN = "HomeNet-App:Networking";
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String serverAddress;
    private int serverPort;
    private int timeout;

    public Networking(){
        this.timeout = 1000;
        this.serverAddress = "";
        this.serverPort = 0;
    }

    public void connect() throws ConnectException {
        long start = System.currentTimeMillis();

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

        {//Get writer and reader
            try{
                this.out = new PrintWriter(this.socket.getOutputStream());
                this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d(cN + ".connect()", "Done after " + (System.currentTimeMillis() - start) + " ms");
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

    public void disconnect() {
        long start = System.currentTimeMillis();

        if (this.socket != null){
            try {
                this.socket.close();
            } catch (IOException e) {
                Log.e(cN + ".disconnect()", "Failed to close socket to server: " + this.socket.toString());
            } finally {
                this.socket = null;
                this.serverAddress = "";
                this.serverPort = 0;
                System.gc();
            }
        }else{
            Log.w(cN + ".disconnect()", "Socket was already closed or never connected");
        }

        Log.d(cN + ".disconnect()", "Done after " + (System.currentTimeMillis() - start) + " ms");
    }

    public String sendForResponse(String message) throws NetworkException{
        long start = System.currentTimeMillis();

        String ret = "";

        {//Check for connection
            if(!this.socket.isConnected())
                throw new NetworkException(NetworkException.NOT_CONNECTED);
        }

        {//
            //Wait for response

            Log.d(cN + ".sendForResponse()", "Sending \"" + message + "\" for response...");

            this.out.println(message);
            this.out.flush();

            String rec;
            boolean end = false;
            do{
                try {
                    rec = this.in.readLine();

                    if (rec.equals("<eot>")) {
                        end = true;
                        Log.d(cN + ".sendForResponse()", "Received end of transmission, length: " + ret.length() + " bytes");
                    }
                    else{
                        ret += rec + "\n";
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }while(!end);
        }

        Log.d(cN + ".sendForResponse()", "Done after " + (System.currentTimeMillis() - start) + " ms");
        return ret;
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
