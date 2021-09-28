package sdt.maxkofler.homenet_app.homenet;

import androidx.annotation.Nullable;

import java.util.concurrent.Semaphore;

import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkCallback;
import sdt.maxkofler.homenet_app.homenet.networking.NetworkInterface;
import sdt.maxkofler.homenet_app.homenet.networking.Networking;

/**
 *
 * This class represents the interface to the HomeNet server.
 *
 */
public class HomeNet {
    private static final String cN = "HomeNet-App:HomeNet";
    private NetworkInterface networkInterface;



    //Container for error messages for inner classes
    private String eMessage = "";

    public HomeNet(String address, int port){
        this.networkInterface = new NetworkInterface();
        this.networkInterface.setServerAddress(address);
        this.networkInterface.setServerPort(port);
    }

    public void connect(@Nullable NetworkCallback callback){
        this.networkInterface.connect(callback);
    }


}
