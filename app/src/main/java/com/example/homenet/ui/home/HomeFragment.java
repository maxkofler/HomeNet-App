package com.example.homenet.ui.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.tv.TvInputService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    int widgets = 0;
    View root;

    ValueView[] vs;
    WSValueserver vServer;

    boolean connectedToServer = false;
    SwipeRefreshLayout swipeRefresh;
    ProgressDialog dialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);


        swipeRefresh = root.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(false);
                refresh(false, false);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog=new ProgressDialog(getContext());
                dialog.setMessage("Lade...");
                dialog.setCancelable(false);
                dialog.setInverseBackgroundForced(false);
                dialog.show();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh(true, false);
                dialog.hide();
            }
        }, 100);

    }

    private void refresh(boolean waitForEnd, final boolean showLoading){

        class RefreshClass implements Runnable{
            @Override
            public void run() {
                if (showLoading){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.show();
                        }
                    });
                }

                connectedToServer = false;

                preferences = getActivity().getSharedPreferences(getString(R.string.key_hnSavesFile), Context.MODE_PRIVATE);
                prefseditor = preferences.edit();

                ip = preferences.getString(getString(R.string.key_ServerIP), "192.168.1.24");
                port = preferences.getInt(getString(R.string.key_ServerPort), 8090);

                vServer = new WSValueserver(ip, port);
                connectedToServer = vServer.init(false);

                if (connectedToServer){

                    ll = root.findViewById(R.id.ll_values);

                    //Variable to check how many widgets there are to create
                    final int countViews = preferences.getInt(getString(R.string.key_countTiles), 2);

                    loadWidgets(countViews);


                    widgets = countViews;
                }
                vServer.closeNet();
                if (showLoading){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.hide();
                        }
                    });
                }
            }

        }

        Thread refreshThread = new Thread(new RefreshClass());
        refreshThread.start();
        if (waitForEnd)
        {
            try {
                refreshThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void loadWidgets(int countViews){

        //Determine if there are to create new widgets

        //If there are a other ammount of widgets, force the user to restart the app
        if (countViews != widgets && widgets != 0){
            Toast.makeText(getContext(), getString(R.string.pls_restart), Toast.LENGTH_LONG).show();
            //System.exit(0);
        }
        //If there are equal widgets, refresh them
        else{
            if (widgets == 0){
                System.out.println("Creating new widgets!");

                vs = new ValueView[countViews];

                for (int i = 0; i < countViews; i++){
                    vs[i] = new ValueView(getContext());
                    vs[i].initialize(i, ip, port);
                    vs[i].setValues(vServer);
                    final int ix = i;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ll.addView(vs[ix]);
                        }
                    });

                }
            }else{
                System.out.println("Refreshing old widgets!");
                for (int i = 0; i < countViews; i++){
                    vs[i].initialize(i, ip, port);
                    final int ii = i;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vs[ii].setValues(vServer);
                        }
                    });

                }
            }
        }
    }
}