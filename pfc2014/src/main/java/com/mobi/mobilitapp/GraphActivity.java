package com.mobi.mobilitapp;

import java.util.Arrays;
import java.util.Set;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GraphActivity extends ActionBarActivity {

	Toolbar toolbar;
	SharedPreferences calo, co2;
	private float maxcal = 0, maxco2 = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphlay);
		toolbar = (Toolbar) findViewById(R.id.toolbargraph);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		calo = getSharedPreferences("data_calories", Context.MODE_PRIVATE);
		co2 = getSharedPreferences("data_co2", Context.MODE_PRIVATE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		maxcal = maxco2 = 0;
		int size = calo.getAll().size();
		
		if (size>0){
			
		
		final String[] labels = new String[size];

		Set<String> label_calo = calo.getAll().keySet();
		Set<String> label_co2 = co2.getAll().keySet();

		GraphView graphView = new LineGraphView(this, getResources().getString(R.string.graphtext));

		GraphViewData[] datacal = new GraphViewData[size];
		GraphViewData[] dataco2 = new GraphViewData[size];

		for (int i = 0; i < size; i++) {
			if (calo.getFloat((String) label_calo.toArray()[i], 0) > maxcal) {
				maxcal = calo.getFloat((String) label_calo.toArray()[i], 0);
			}

			labels[i] = (String) label_calo.toArray()[i];
			labels[i] = labels[i].substring(0, 5);
			
			Log.v("label: ", labels[i]);
		}

		for (int i = 0; i < size; i++) {
			if (co2.getFloat((String) label_co2.toArray()[i], 0) > maxco2)
				maxco2 = co2.getFloat((String) label_co2.toArray()[i], 0);
		}

		Arrays.sort(labels);

		for (int i = 0; i < size; i++)
			// numeros ordenados --> label
			for (int j = 0; j < size; j++) { //--> labelx
				if (label_calo.toArray()[j].toString().contains(labels[i])) {
					datacal[i] = new GraphViewData(i, calo.getFloat((String) label_calo.toArray()[j], 0));

					dataco2[i] = new GraphViewData(i, co2.getFloat((String) label_calo.toArray()[j], 0));
				}
			}
		if (maxcal > maxco2) {
			graphView.setManualYAxisBounds(maxcal + 20, 0);
		} else {
			graphView.setManualYAxisBounds(maxco2 + 20, 0);
		}
		graphView.addSeries(new GraphViewSeries("Calorías (kcal)", new GraphViewSeriesStyle(Color.GREEN, 3), datacal));
		graphView.addSeries(new GraphViewSeries("Co2 (gr)", new GraphViewSeriesStyle(Color.RED, 3), dataco2));
		
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.MIDDLE);
		graphView.getGraphViewStyle().setLegendWidth(300);
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {

			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {

					return labels[(int) value];
					//return labels[0];
				}

				return null;
			}
		});

		if (size > 7) {
			graphView.setViewPort(0, 7);
			
		} else {
			graphView.setViewPort(0, size-1);
			
		}
		graphView.setScrollable(true);
		graphView.setScalable(true);

		LinearLayout layout = (LinearLayout) findViewById(R.id.gl);
		layout.addView(graphView);

		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

}
