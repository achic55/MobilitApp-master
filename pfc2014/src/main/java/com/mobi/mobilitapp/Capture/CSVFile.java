package com.mobi.mobilitapp.Capture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by rokslamek on 04/04/17.
 */

public class CSVFile {

    private String dir;
    private String filename;

    FileWriter outputStream;
    BufferedWriter bw;
    BufferedReader br = null;
    String line = "";
    String csvSplitBy = ",";
    String[] s;
    File folder;
    File file;

    public CSVFile(String dir, String filename) {

        this.dir = dir;
        this.filename = filename;
    }

    public long getSize(){
        long size = file.length();
        return size;
    }

    public boolean open() {

        folder = new File(dir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        file = new File(dir, filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream = new FileWriter(file, true);
            bw = new BufferedWriter(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean writeLine(String line) {

        try {
            bw.write(line+"\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean close() {

        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean createSensorDataFile(List<SensorSample> data){
        try {
            open();
            //writeLine("sensor,timestamp,x,y,z");
            for (SensorSample p : data)
                writeLine(Long.valueOf(p.timestamp) + "," + p.x + "," + p.y + "," + p.z);
            close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", e.toString());
        }

        return true;
    }


    public boolean createSensorDataFileSingleInput(List<SensorSampleSingleInput> data){
        try {
            open();
           // writeLine("sensor,timestamp,x,y,z");
            for (SensorSampleSingleInput p : data)
                writeLine(Long.valueOf(p.timestamp) + "," + p.x);
            close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", e.toString());
        }

        return true;
    }

    public boolean createGpsDataFile(List<LocationCoordinates> data){
        try {
            open();
            //writeLine("sensor,timestamp,lat,lon");
            for (LocationCoordinates p : data)
                writeLine(Long.valueOf(p.timestamp) + "," + p.lat + "," + p.lon);
            close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", "GPS: " + e.toString());
        }

        return true;
    }

    public boolean createARDataFile(List<ARSample> data){
        try {
            open();
            for (ARSample p : data)
                writeLine(Long.valueOf(p.timestamp)
                        + "," + p.vehicle + "," + p.bicycle + "," + p.foot + "," + p.running
                        + "," + p.still + "," + p.tilting + "," + p.walking + "," + p.unknown);
            close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", "AR : " + e.toString());
        }

        return true;
    }

    public boolean createWifiDataFile(List<WifiSample> data){
        try {
            open();
            for (WifiSample p : data)
                writeLine(Long.valueOf(p.timestamp) + "," + p.ssid + "," + p.bssid + "," + p.rssi + "," + p.networdId);
            close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", "AR : " + e.toString());
        }

        return true;
    }

    public boolean createCellDataFile(List<CellSample> data){
        try {
            open();
            for (CellSample p : data)
                writeLine(Long.valueOf(p.timestamp) + "," + p.mccmnc + "," + p.lac + "," + p.cellid);
            close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("BAYO CSV", "AR : " + e.toString());
        }

        return true;
    }

}
