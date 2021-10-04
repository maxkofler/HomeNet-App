package sdt.maxkofler.homenet_app.homenet.networking;

public interface NetworkCallback {

    public enum job_type{
        CONNECT,
        DISCONNECT,
        SEND_FOR_RESPONSE
    }

    void done(NetworkCallback.job_type job_type, String[] results);
    void error(NetworkCallback.job_type job_type, Exception e);
}
