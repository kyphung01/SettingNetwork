package vn.net.lad.settingnetwork;

import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void setNetwork(View v) {
//        Toast.makeText(getApplicationContext(), "setNetwork ", Toast.LENGTH_SHORT).show();
        WifiConfiguration wifiConf = null;
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId == connectionInfo.getNetworkId()) {
                wifiConf = conf;
                break;
            }
        }
        final DhcpInfo info = wifiManager.getDhcpInfo();

        // TextView tv;
        // tv = (TextView)findViewById(R.id.txtInfo);
        // tv.setText(info.toString());

        int int_gg1 = 134744072;
        int int_gg2 = 67373064;
        final String dns1 = "8.8.8.8";
        final String dns2 = "8.8.4.4";

        Log.d("ladtest", " 001");


        if (info.dns1 != int_gg1 && info.dns1 != int_gg2) {
            Log.d("ladtest", " 002" + info.toString());

            final AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setTitle("Đổi DNS?");
            b.setMessage("Bạn có muốn thay đổi cấu hình DNS để đọc truyện với chất lượng tốt hơn");

            final WifiConfiguration finalWifiConf = wifiConf;
            b.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("MainActivity", "onClick (line 71):123123 ip = " + intToInetAddress(info.ipAddress));

                    // Check nếu API>=21
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        if (finalWifiConf == null) {
                            // wifi is not connected
                            return;
                        }
                        try {
                            String ip = intToInetAddress(info.ipAddress).getHostAddress();
                            String gateway = intToInetAddress(info.gateway).getHostAddress();

                            Class<?> ipAssignment = finalWifiConf.getClass().getMethod("getIpAssignment").invoke(finalWifiConf).getClass();
                            Object staticConf = finalWifiConf.getClass().getMethod("getStaticIpConfiguration").invoke(finalWifiConf);

                            finalWifiConf.getClass().getMethod("setIpAssignment", ipAssignment).invoke(finalWifiConf, Enum.valueOf((Class<Enum>) ipAssignment, "STATIC"));
                            if (staticConf == null) {
                                Class<?> staticConfigClass = Class.forName("android.net.StaticIpConfiguration");
                                staticConf = staticConfigClass.newInstance();
                            }

                            // STATIC IP AND MASK PREFIX
                            Constructor<?> laConstructor = LinkAddress.class.getConstructor(InetAddress.class, int.class);
                            LinkAddress linkAddress = (LinkAddress) laConstructor.newInstance(
                                    InetAddress.getByName(ip),
                                    24);
                            staticConf.getClass().getField("ipAddress").set(staticConf, linkAddress);
                            // GATEWAY
                            staticConf.getClass().getField("gateway").set(staticConf, InetAddress.getByName(gateway));

                            // DNS
                            List<InetAddress> dnsServers = (List<InetAddress>) staticConf.getClass().getField("dnsServers").get(staticConf);
                            dnsServers.clear();
                            dnsServers.add(InetAddress.getByName(dns1));
//                          dnsServers.add(InetAddress.getByName("8.8.4.4")); // DNS2
                            // apply the new static configuration
                            finalWifiConf.getClass().getMethod("setStaticIpConfiguration", staticConf.getClass()).invoke(finalWifiConf, staticConf);
                            wifiManager.updateNetwork(finalWifiConf);
                            wifiManager.saveConfiguration();
                            wifiManager.disconnect();
                            wifiManager.reconnect();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                                List<WifiConfiguration> networks = wm.getConfiguredNetworks();
                                Iterator<WifiConfiguration> iterator = networks.iterator();
                                while (iterator.hasNext()) {
                                    WifiConfiguration wifiConfig = iterator.next();
                                    if (wifiConfig.SSID.replace("\"", "").equals(wifiConfig.SSID.replace("\"", "")))
                                        wm.enableNetwork(wifiConfig.networkId, true);
                                    else
                                        wm.disableNetwork(wifiConfig.networkId);
                                }
                                wm.reconnect();
                            }


                        } catch (Exception e) {

                            e.printStackTrace();
                        }

                    } else {
                        try {

                            Log.d("ladtest", " 003");

                            Setting_DNS myDNS = new Setting_DNS();
                            Log.d("ladtest", " 0030x");

                            myDNS.setIpAssignment("STATIC", finalWifiConf); //or "DHCP" for dynamic setting
                            //myDNS.setIpAssignment("DHCP", finalWifiConf); //or "DHCP" for dynamic setting
                            Log.d("ladtest", " 0031" + myDNS.toString());

                            myDNS.setDNS(InetAddress.getByName(dns1), InetAddress.getByName(dns2), finalWifiConf);
                            Log.d("ladtest", " 0032");

                            wifiManager.updateNetwork(finalWifiConf); //apply the setting
                            Log.d("ladtest", " 0033");
//                            wifiManager.saveConfiguration();
                            if (wifiManager.saveConfiguration()) { //Save it
                                Log.d("ladtest", " saveConfiguration OK");
                            } else
                                Log.d("ladtest", "  NOT DONE saveConfiguration");

                            wifiManager.disconnect();
                            wifiManager.reconnect();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }

                }

            });
            b.setNegativeButton("Không", new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialog, int which)

                {
                    Log.d("ladtest", " 004");

                    dialog.cancel();
                }

            });
            b.create().show();
        } else
            Toast.makeText(getApplicationContext(), "Bạn đã set DNS GG rồi?", Toast.LENGTH_SHORT).show();
    }

    public void getNetworkInfo(View v) {

//        Toast.makeText(getApplicationContext(), "setNetwork ", Toast.LENGTH_SHORT).show();
        WifiConfiguration wifiConf = null;
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId == connectionInfo.getNetworkId()) {
                wifiConf = conf;
                break;
            }
        }
        DhcpInfo info = wifiManager.getDhcpInfo();

        TextView tv;
        tv = (TextView) findViewById(R.id.txtInfo);
        tv.setText(info.toString());
        Toast.makeText(getApplicationContext(), "getNetworkInfo ", Toast.LENGTH_SHORT).show();
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }


}
