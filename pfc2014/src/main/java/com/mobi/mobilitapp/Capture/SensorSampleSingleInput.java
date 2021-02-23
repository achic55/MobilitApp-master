package com.mobi.mobilitapp.Capture;

/**
 * Created by rokslamek on 04/04/17.
 */

public class SensorSampleSingleInput {

    long timestamp;
    float x;

    public SensorSampleSingleInput(long timestamp, float x) {

        this.timestamp = timestamp;
        this.x = x;
    }
}
