package com.agn.vpnshare.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agn.vpnshare.R;

import com.agn.vpnshare.service.ProxyService;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;


public class HomeFragment extends Fragment {


    private InputMethodManager imm;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000;
    private static final int port = 8964;
    private TextView proxy;
    private TextView ports;
    private TextView prInfo;
    private Button vshareEnable;
    private Button vshareDisable;
    private TextView proxyStatusTextView;
    private Button wifiTetherButton;
    private SharedPreferences sp;
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        wifiTetherButton = (Button) view.findViewById(R.id.WiFiTetherButton);
        proxyStatusTextView = (TextView) view.findViewById(R.id.proxyStatus);
        prInfo = (TextView) view.findViewById(R.id.proxyURL);
        proxy = (TextView) view.findViewById(R.id.proxy);
        ports = (TextView) view.findViewById(R.id.ports);
        vshareEnable = (Button) view.findViewById(R.id.cb_enable);
        vshareDisable = (Button) view.findViewById(R.id.cb_disable);
        sp = getActivity().getSharedPreferences("Wifi_Tethering",Context.MODE_PRIVATE);
        vshareEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putBoolean("vpnshare", true).commit();
                Intent intent = new Intent(getActivity(), ProxyService.class);
                getActivity().startService(intent);
                ProxyService.getInstance().start();
                vshareEnable.setVisibility(View.GONE);
                vshareDisable.setVisibility(View.VISIBLE);
                proxy.setText(String.format("IP Proxy: " + getIPAddress(true)));
                ports.setText("Port: " + port);
                proxy.setVisibility(View.VISIBLE);
                ports.setVisibility(View.VISIBLE);
                proxyStatusTextView.setVisibility(View.VISIBLE);
                prInfo.setVisibility(View.VISIBLE);
                proxyStatusTextView.setText(getString(R.string.proxy_is_running));
                prInfo.setText(String.format("%s:%d", getIPAddress(true), port));
                Toast.makeText(getActivity(), getResources().getString(R.string.proxy_started),
                        Toast.LENGTH_SHORT).show();
            }
        });

        vshareDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putBoolean("vpnshare", false).commit();
                vshareEnable.setVisibility(View.VISIBLE);
                vshareDisable.setVisibility(View.GONE);
                getActivity().stopService(new Intent(getActivity(), ProxyService.class));
                ProxyService.getInstance().stop();
                proxyStatusTextView.setVisibility(View.GONE);
                prInfo.setVisibility(View.GONE);
                proxy.setVisibility(View.GONE);
                ports.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getResources().getString(R.string.proxy_stopped),
                        Toast.LENGTH_SHORT).show();
            }
        });

        boolean isrunning = sp.getBoolean("vpnshare", false);
        if (isrunning){
            vshareEnable.setVisibility(View.GONE);
            vshareDisable.setVisibility(View.VISIBLE);
            proxy.setText(String.format("IP Proxy: " + getIPAddress(true)));
            ports.setText("Port: " + port);
            proxy.setVisibility(View.VISIBLE);
            ports.setVisibility(View.VISIBLE);
            proxyStatusTextView.setVisibility(View.VISIBLE);
            prInfo.setVisibility(View.VISIBLE);
            proxyStatusTextView.setText(getString(R.string.proxy_is_running));
            prInfo.setText(String.format("%s:%d", getIPAddress(true), port));
        }else {
            proxyStatusTextView.setVisibility(View.GONE);
            vshareEnable.setVisibility(View.VISIBLE);
            vshareDisable.setVisibility(View.GONE);
            prInfo.setVisibility(View.GONE);
            proxy.setVisibility(View.GONE);
            ports.setVisibility(View.GONE);
        }

        wifiTetherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHotspotSettings();
            }
        });

        return view;
    }

    public String getIPAddress(boolean useIPv4) {
        try {
            boolean isIPv4;
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

    private void launchHotspotSettings() {
        Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        startActivity(tetherSettings);
    }

}
