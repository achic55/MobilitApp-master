package com.mobi.mobilitapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;


public class Help extends Activity implements View.OnClickListener{

    private ShowcaseView showcaseView;
    private int contador=0;
    private Target t1,t2,t3,t4,t5,t6,t7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        t1 = new ViewTarget(R.id.button,this);
        t2 = new ViewTarget(R.id.button2,this);
        t3 = new ViewTarget(R.id.button3,this);
        t4 = new ViewTarget(R.id.button4,this);
        t5 = new ViewTarget(R.id.button5,this);
        t6 = new ViewTarget(R.id.button6,this);
        t7 = new ViewTarget(R.id.button7,this);



        showcaseView = new ShowcaseView.Builder(this,true)
                .setTarget(Target.NONE)
                .setOnClickListener(this)
                .setContentTitle(R.string.help)
                .setContentText(R.string.content_help)
                .setStyle(R.style.Transparencia)

                .build();
        showcaseView.setButtonText (getResources().getString(R.string.next));



    }


    @Override
    public void onClick(View v) {

        Log.v("aki", "contador" + contador);
        switch (contador){
            case 0:
                showcaseView.setShowcase(t1,true);
                showcaseView.setContentTitle(getResources().getString(R.string.prefes));
                showcaseView.setContentText(getResources().getString(R.string.text_prefes));
                break;
            case 1:
                showcaseView.setShowcase(t2,true);
                showcaseView.setContentTitle(getResources().getString(R.string.turn_off));
                showcaseView.setContentText(getResources().getString(R.string.text_turn_off));
                break;
            case 2:
                showcaseView.setShowcase(t3,true);
                showcaseView.setContentTitle(getResources().getString(R.string.history_menu));
                showcaseView.setContentText(getResources().getString(R.string.text_history_menu));
                break;
            case 3:
                showcaseView.setShowcase(t4,true);
                showcaseView.setContentTitle(getResources().getString(R.string.graph_menu));
                showcaseView.setContentText(getResources().getString(R.string.text_graph_menu));
                break;
            case 4:
                showcaseView.setShowcase(t5,true);
                showcaseView.setContentTitle(getResources().getString(R.string.clean_menu));
                showcaseView.setContentText(getResources().getString(R.string.text_clean_menu));
                break;
            case 5:
                showcaseView.setShowcase(t6,true);
                showcaseView.setContentTitle(getResources().getString(R.string.profile_menu));
                showcaseView.setContentText(getResources().getString(R.string.text_profile_menu));
                break;
            case 6:
                showcaseView.setShowcase(t7,true);
                showcaseView.setContentTitle(getResources().getString(R.string.traffic_menu));
                showcaseView.setContentText(getResources().getString(R.string.text_traffic_menu));
                break;
            case 7:
                this.finish();
                break;
        }
        contador++;
    }
}
