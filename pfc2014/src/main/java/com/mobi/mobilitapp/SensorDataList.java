package com.mobi.mobilitapp;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Gerard Marrugat on 02/03/2016.
 */
public class SensorDataList extends ArrayList<SensorData>  implements Parcelable {

    private String sensorname;

    public SensorDataList(String sensorname){

        this.sensorname = sensorname;

    }

    public String getSensorName(){
        return sensorname;
    }

    public SensorDataList(Parcel in){

        readFromParcel(in);

    }

    public static final Creator<SensorDataList> CREATOR = new Creator<SensorDataList>() {
        @Override
        public SensorDataList createFromParcel(Parcel in) {
            return new SensorDataList(in);
        }

        @Override
        public SensorDataList[] newArray(int size) {
            return new SensorDataList[size];
        }
    };

    private void readFromParcel(Parcel in) {

        this.clear();

        //First we have to read the list size

        int size = in.readInt();

        //Reading SensorData attributes

        //Order is fundamental

        for (int i = 0; i < size; i++) {

            SensorData data = new SensorData();

            data.setTimestamp(in.readString());

            data.setX(in.readDouble());

            data.setY(in.readDouble());

            data.setZ(in.readDouble());

            this.add(data);

        }

    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {

        int size = this.size();

        dest.writeInt(size);

        for (int i = 0; i < size; i++) {

            SensorData data = this.get(i);

            dest.writeString(data.getTimestamp());

            dest.writeDouble(data.getX());

            dest.writeDouble(data.getY());

            dest.writeDouble(data.getZ());

        }

    }

}
