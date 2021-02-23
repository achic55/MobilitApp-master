package com.mobi.mobilitapp.Capture;

/**
 * Created by Mahmoud Elbayoumy on 10/28/2017.
 */

public class WifiSample {

    long timestamp;
    String ssid;
    String bssid;
    int rssi;
    int networdId;

    public WifiSample(long timestamp,String ssid, String bssid, int rssi, int networdId){
        this.timestamp = timestamp;
        this.ssid = ssid;
        this.bssid = bssid;
        this.rssi = rssi;
        this.networdId = networdId;
    }
}
