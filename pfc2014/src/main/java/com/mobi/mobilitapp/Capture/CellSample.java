package com.mobi.mobilitapp.Capture;

/**
 * Created by Mahmoud Elbayoumy on 10/29/2017.
 */

public class CellSample {

    long timestamp;
    String mccmnc;
    int lac;
    int cellid;

    public CellSample(long timestamp, String mccmnc, int lac, int cellid){
        this.timestamp = timestamp;
        this.mccmnc = mccmnc;
        this.lac = lac;
        this.cellid = cellid;
    }

}
