package sdt.maxkofler.homenet_app.homenet.networking;

import androidx.annotation.Nullable;

public interface NetworkCallback {

    void done(Networking.job_type job_type, String[] results);
    void error(Exception e);
}
