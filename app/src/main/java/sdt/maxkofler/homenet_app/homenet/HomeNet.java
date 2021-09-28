package sdt.maxkofler.homenet_app.homenet;

import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectException;
import sdt.maxkofler.homenet_app.homenet.exceptions.ConnectRuntimeException;

/**
 *
 * This class represents the interface to the HomeNet server.
 *
 */
public class HomeNet {
    NetworkInterface networkInterface;

    public HomeNet(){
        this.networkInterface = new NetworkInterface();
    }

    public void connect() throws ConnectException {
        this.networkInterface.connect();
    }
}
