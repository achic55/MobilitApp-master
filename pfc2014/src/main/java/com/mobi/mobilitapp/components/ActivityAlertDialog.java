package com.mobi.mobilitapp.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.mobi.mobilitapp.R;

public class ActivityAlertDialog extends DialogFragment {


    public ActivityAlertDialog() {
    }

    public static ActivityAlertDialog newInstance(String message) {
        ActivityAlertDialog f = new ActivityAlertDialog();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("content",message );

        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString("content"))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }



}
