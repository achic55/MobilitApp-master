package com.example.gerardmarrugat.accelerometer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Start_BK extends AppCompatActivity {


    public void onClick(View view){
        // Start Button
        //When we call the Main activity we send a boolean message with the Intent
        Intent i = new Intent(this,DataScreen.class);
        // onStartMessage helps us to know if the action is started or not
        i.putExtra("onStartMessage",true);
        // the activity starts
        startActivity(i);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        //Intent´s extras from the Main activity
        Bundle MainMessage = getIntent().getExtras();

        //Instance the onclick_text to show the MainMessage
        TextView onclick_text = (TextView) findViewById(R.id.onclick_text);

        if(MainMessage == null){

            onclick_text.setText(Boolean.toString(false));

        }
        else {

           onclick_text.setText(Boolean.toString(MainMessage.getBoolean("MainMessage")));

        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
