package com.mobi.mobilitapp;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class TrafficFragment extends Fragment {

	private String ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			.getAbsolutePath() + "/MobilitApp/";

	private ArrayList<String> files;
	public RecyclerView rV;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.history_fragment, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		readFolder();
		
		rV = (RecyclerView) getActivity().findViewById(R.id.recicler);
		rV.setHasFixedSize(true);
		rV.setAdapter(new HistoryAdapter(getActivity(), files, R.layout.customadap));
		rV.setLayoutManager(new LinearLayoutManager(getActivity()));
		rV.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
	}

	private ArrayList<String> readFolder() {
		File dir = new File(ruta);
		File[] filelist = dir.listFiles();
		/*Arrays.sort(filelist, new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				if (((File) o1).lastModified() > ((File) o2).lastModified()) {
					return -1;
				} else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
					return +1;
				} else {
					return 0;
				}
			}

		});*/
		files = new ArrayList<String>();
// lista solo los ficheros acabados en .dat
		for (File file : filelist) {
	        if (file.isDirectory()) {
	            
	        } else {
	            if(file.getName().endsWith(".dat")){
	                files.add(file.getName());
	            }
	        }
	    }
		
		
		return files;
	}
	
	
}
