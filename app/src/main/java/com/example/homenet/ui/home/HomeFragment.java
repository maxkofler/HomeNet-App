package com.example.homenet.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.homenet.ExceptionClasses.NoConnectionToWSServer;
import com.example.homenet.R;
import com.example.homenet.ValueView;
import com.example.homenet.network.HNNetworking;
import com.example.homenet.weathersens.WSValueserver;

import org.w3c.dom.Text;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefseditor;

    private String ip = "192.168.1.24";
    private int port = 8090;

    private LinearLayout ll;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = this.getActivity().getSharedPreferences(getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
        prefseditor = preferences.edit();

        ip = preferences.getString(getString(R.string.key_ServerIP), "192.168.1.24");
        port = preferences.getInt(getString(R.string.key_ServerPort), 8090);

        WSValueserver vServer = new WSValueserver(ip, port);
        try{
            vServer.init();
        }catch (NoConnectionToWSServer e){
            Toast.makeText(getContext(), getString(R.string.err_no_connection_to_server), Toast.LENGTH_LONG).show();
        }


        ll = root.findViewById(R.id.ll_values);
        int countViews = preferences.getInt(getString(R.string.key_countTiles), 2);

        ValueView[] vs = new ValueView[countViews];

        for (int i = 0; i < countViews; i++){
            vs[i] = new ValueView(getContext());
            vs[i].initialize(i, ip, port);
            vs[i].setValues(vServer);
            ll.addView(vs[i]);
        }

        return root;
    }
}