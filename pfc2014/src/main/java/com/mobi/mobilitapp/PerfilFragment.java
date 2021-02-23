package com.mobi.mobilitapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PerfilFragment extends Fragment {

    private LinearLayout ll,ll2;
    private TextView tV;
    private ImageView iV,iV2;
    private GridView hlv;
	private GridView gvprof;


	private String[] titulo = { "", "", "", "", "", "" };
	private String[] valores = { "", "", "", "", "", "" };
	SharedPreferences prefsGoogle, prefsFace, first, prefs;
	SharedPreferences.Editor editor;
	String age, genFace;
	int agenum, gen;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.perfil_lay, container, false);
		return rootView;
	}


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);


            ll = (LinearLayout) getActivity().findViewById(R.id.troll);
            ll2 = (LinearLayout) getActivity().findViewById(R.id.troll2);
            tV = (TextView) getActivity().findViewById(R.id.name);
            iV = (ImageView) getActivity().findViewById(R.id.image);
            iV2 = (ImageView) getActivity().findViewById(R.id.image2);
            hlv = (GridView) getActivity().findViewById(R.id.gridView1);

            iV.setImageResource(R.drawable.ic_logo);
            iV2.setImageResource(R.drawable.t10);

//        ll2.setVisibility(View.VISIBLE);

            iV.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //do magic
                    Intent i = new Intent(getActivity(), PrefAct.class);
                    startActivity(i);
                }
            });


            prefsGoogle = getActivity().getSharedPreferences("google_login", Context.MODE_PRIVATE);
            prefsFace = getActivity().getSharedPreferences("face_login", Context.MODE_PRIVATE);
            first = getActivity().getSharedPreferences("prefs", 0);
            prefs = getActivity().getSharedPreferences("com.mobi.mobilitapp_preferences", 0);

            gvprof = (GridView) getActivity().findViewById(R.id.gvprofile);

            titulo[0] = getActivity().getResources().getString(R.string.name) + "		";
            titulo[1] = getActivity().getResources().getString(R.string.surname) + "		";
            titulo[2] = getActivity().getResources().getString(R.string.gender) + "		";
            titulo[3] = getActivity().getResources().getString(R.string.age) + "		";
            titulo[4] = getActivity().getResources().getString(R.string.weight) + "		";
            titulo[5] = getActivity().getResources().getString(R.string.mail) + "		";

            if (!first.getBoolean("firstRun", false)) {

                if (!prefsGoogle.getString("nombre", "Nombre").equalsIgnoreCase("Nombre")) { // Se ha logueado mediante Google
                    valores[0] = prefsGoogle.getString("nombre", "Nombre");
                    valores[1] = prefsGoogle.getString("apellidos", "Apellidos");

                    if (!prefs.getString("genero", "2").equalsIgnoreCase("2")) {
                        gen = Integer.parseInt(prefs.getString("genero", "2"));
                    } else {
                        gen = prefsGoogle.getInt("genero", 2);
                    }
                    if (gen == 0) {
                        valores[2] = getActivity().getResources().getString(R.string.male);
                    } else if (gen == 1) {
                        valores[2] = getActivity().getResources().getString(R.string.female);
                    }

                    age = prefsGoogle.getString("birthday", "Edad");

                    if (!age.equalsIgnoreCase("Edad")) {
                        agenum = AppUtils.getYear() - Integer.parseInt(age.substring(0, 4));
                        age = Integer.toString(agenum);
                    } else {
                        age = prefs.getString("born", "1900");
                        agenum = AppUtils.getYear() - Integer.parseInt(age);



                        if (!age.equals("1900")&& !age.equals("0")){
                            valores[3] = Integer.toString(agenum) + " a\u00f1os";
                        }
                        else{
                            valores[3] = "";
                        }

                    }


                    if (!prefs.getString("peso", "0").equalsIgnoreCase("0")) {
                        valores[4] = prefs.getString("peso", "0") + " kg";

                    } else {
                        valores[4] = "";
                    }
                    valores[5] = prefsGoogle.getString("correo", "");
                } else {
                    if (!prefsFace.getString("nombre", "Nombre").equalsIgnoreCase("Nombre")) { // Se ha logueado mediante Facebook
                        valores[0] = prefsFace.getString("nombre", "Nombre");
                        valores[1] = prefsFace.getString("apellidos", "Apellidos");

                        if (!prefs.getString("genero", "2").equalsIgnoreCase("2")) {
                            gen = Integer.parseInt(prefs.getString("genero", "2"));
                            if (gen == 0) {
                                valores[2] = getActivity().getResources().getString(R.string.male);
                            } else if (gen == 1) {
                                valores[2] = getActivity().getResources().getString(R.string.female);
                            }
                        } else {
                            genFace = prefsFace.getString("genero", "unknown");

                            if (genFace.equalsIgnoreCase("male")) {
                                valores[2] = getActivity().getResources().getString(R.string.male);
                            } else if (genFace.equalsIgnoreCase("female")) {
                                valores[2] = getActivity().getResources().getString(R.string.female);
                            }
                        }
                        age = prefsFace.getString("birthday", "Edad");
                        if (!age.equalsIgnoreCase("Edad") && !age.equalsIgnoreCase("null")) {
                            agenum = AppUtils.getYear() - Integer.parseInt(age.substring(6, 10));
                            valores[3] = Integer.toString(agenum) + "  a\u00f1os";



                        } else {
                            age = prefs.getString("born", "1900");
                            agenum = AppUtils.getYear() - Integer.parseInt(age);

                            if (!age.equals("1900")&& !age.equals("0")) {
                                valores[3] = Integer.toString(agenum) + "  a\u00f1os";
                            }
                            else{
                                valores[3] = "";
                            }
                        }


                        if (!prefs.getString("peso", "0").equalsIgnoreCase("0")) {
                            valores[4] = prefs.getString("peso", "0") + " kg";
                        } else {
                            valores[4] = "";
                        }
                        valores[5] = prefsFace.getString("email", "");
                    } else { //No está logeado: mostrar solo peso y edad si la ha puesto en preferencias


                        age = prefs.getString("born", "1900");
                        agenum = AppUtils.getYear() - Integer.parseInt(age);
                        if (!age.equals("1900")&& !age.equals("0")) {
                            valores[3] = Integer.toString(agenum) + "  a\u00f1os";
                        }
                        else{
                            valores[3] = "";
                        }

                        if (!prefs.getString("peso", "0").equalsIgnoreCase("0")) {
                            valores[4] = prefs.getString("peso", "0") + " kg";
                        } else {
                            valores[4] = "";
                        }
                    }

                }
                double valuepercent = 5;

                for (String s : valores) {

                    Log.v("valores", s);

                    if (s.equals("not_set") || s.equals("Nombre")) {
                        valuepercent--;
                    }
                    if (s.equals("Apellidos")) {
                        valuepercent--;
                    }
                    if (s.equals("2") || s.equals("unknown")) {
                        valuepercent--;
                    }
                    if (s.equals("1900  a\u00f1os")) {
                        valuepercent--;
                    }
                    if (s.equals("")) {
                        valuepercent--;
                    }
                    if (s.equals("0")) {
                        valuepercent--;
                    }


                }
                Log.v("valores: ", valuepercent + "");

                double percent = (valuepercent / 5) * 100;

                tV.setText("Tu perfil esta completado un " + percent + "%");
                ll.setVisibility(View.VISIBLE);

                if (percent ==100){

                    ll2.setVisibility(View.VISIBLE);
                    ll2.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            //do magic
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mobilitat.upc.edu/sorteot10.php"));
                            startActivity(browserIntent);
                        }
                    });
                }

                gvprof.setAdapter(new AnotherAdapter(getActivity(), R.layout.dostvh, titulo, valores));

            }
        }catch (NumberFormatException e){
            Log.v("aki",e.toString());
        }

    }
}