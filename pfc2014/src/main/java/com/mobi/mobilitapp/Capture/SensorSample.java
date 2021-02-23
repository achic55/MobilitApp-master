package com.mobi.mobilitapp.Capture;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by rokslamek on 04/04/17.
 */

public class SensorSample implements Parcelable {

    long timestamp;
    float x;
    float y;
    float z;



    public SensorSample(long timestamp, float x, float y, float z) {

        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(timestamp);
        parcel.writeFloat(x);
        parcel.writeFloat(y);
        parcel.writeFloat(z);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
    public static final Parcelable.Creator<SensorSample> CREATOR
            = new Parcelable.Creator<SensorSample>() {
        public SensorSample createFromParcel(Parcel in) {
            return new SensorSample(in);
        }

        public SensorSample[] newArray(int size) {
            return new SensorSample[size];
        }
    };

    private SensorSample(Parcel in) {
        timestamp = in.readLong();
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }
}
