package com.mobi.mobilitapp.Capture;

/**
 * Created by Mahmoud Elbayoumy on 10/26/2017.
 */

public class ARSample {

    long timestamp;
    int vehicle;
    int bicycle;
    int foot;
    int running;
    int still;
    int tilting;
    int walking;
    int unknown;

    public ARSample(long timestamp, int vehicle, int bicycle, int foot, int running,
                    int still, int tilting, int walking, int unknown){
        this.timestamp = timestamp;
        this.vehicle = vehicle;
        this.bicycle = bicycle;
        this.foot = foot;
        this.running = running;
        this.still = still;
        this.tilting = tilting;
        this.walking = walking;
        this.unknown = unknown;
    }

}
