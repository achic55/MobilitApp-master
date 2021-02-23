package com.mobi.mobilitapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * Created by Gerard Marrugat on 05/03/2016.
 */
public class SelectTransportFragment_Fail extends DialogFragment {
    //This class is the dialog list which allows the user to select between 4 means of transport

    String [] mtransports = {"Car","Motorbike","Metro","Bus","Train","Other"};
    String MyTAG = "MyTAG";
    SharedPreferences sharedPref = getActivity().getSharedPreferences("transport_selected", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();

    ListView listview = new ListView(MainActivity.c);

    //this variable is used to pass the transport selected to the DataStorage
    //private static String selectedTransport;

   // public String getTransportName(){
  //      return selectedTransport;
   // }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.transportfragment,container,false);

       // ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.rowlayout,R.id.txtitem);

       // listview.setAdapter(adapter);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ViewGroup vg = (ViewGroup) view;
         //       TextView txt = (TextView) vg.findViewById(R.id.txtitem);
         //       Toast.makeText(getActivity(),txt.getText().toString(),Toast.LENGTH_LONG).show();
            }
        });

        return viewGroup;

    }




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(listview)
                .setItems(mtransports, (DialogInterface dialog, int which) -> {
                    String selectedTransport = mtransports[which];
                        //Log.v(MyTAG,"Selected Transport is  " + selectedTransport);
                        //editor.putString(getString(R.string.transport_selected), selectedTransport);
                        //editor.commit();
                });

        return builder.create();
    }
}
