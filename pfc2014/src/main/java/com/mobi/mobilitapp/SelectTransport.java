package com.mobi.mobilitapp;

/**
 * Created by Gerard Marrugat on 19/03/2016.
 */
public class SelectTransport {
    private static SelectTransport ourInstance = new SelectTransport();
    public static String selected_transport;


    public static SelectTransport getInstance() {
        return ourInstance;
    }

    private SelectTransport() {
    }

    public String getSelected_transport(){
        return this.selected_transport;
    }

    public void setSelected_transport(String value){
        this.selected_transport=value;
    }
}
