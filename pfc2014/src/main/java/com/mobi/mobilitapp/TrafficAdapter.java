package com.mobi.mobilitapp;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TrafficAdapter extends
		RecyclerView.Adapter<TrafficAdapter.ViewHolder> {

	private static ArrayList<String> data;
	private String ruta = Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
			+ "/MobilitApp/";
	private int idlay;
	private Context context;
	public View antbot = null;
	AlertDialog.Builder builder;

	public TrafficAdapter(Context c, ArrayList<String> data, int idlay) {
		this.context = c;
		TrafficAdapter.data = data;
		this.idlay = idlay;
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		public Button b;
		public TextView tv;

		public ViewHolder(View itemView) {
			super(itemView);
		
			tv = (TextView) itemView.findViewById(R.id.tvfiles);
			b = (Button) itemView.findViewById(R.id.bDelete);		
		}
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public void onBindViewHolder(final ViewHolder vh, final int position) {
		
		vh.tv.setText(" "+" "+" "+data.get(position));
		vh.b.setVisibility(Button.INVISIBLE);

		vh.itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(context, MainActivity.class);
				i.putExtra("archivo", data.get(position));
				((Activity) context).setResult(Activity.RESULT_OK, i);
				((Activity) context).finish();

			}
		});
		
		vh.itemView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if (antbot != null)
					antbot.setVisibility(Button.INVISIBLE);
				antbot = vh.b;
				vh.b.setVisibility(Button.VISIBLE);
				return true;
			}
		});

		vh.b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final File f = new File(ruta, data.get(position));

				if (System.currentTimeMillis() - f.lastModified() > 259200000) {
					builder.setMessage(R.string.segur)
							.setTitle(R.string.borrar);

					builder.setPositiveButton(R.string.acep,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									f.delete();
									data.remove(position);
									notifyItemRemoved(position);
								}
							});

					builder.setNegativeButton(R.string.cancel, null);
					builder.create();
					builder.show();
				} else {
					builder.setMessage(R.string.rec).setTitle(R.string.borrar);
					builder.create().show();
				}
			}

		});

	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
		builder = new AlertDialog.Builder(context);
		View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
				idlay, parent, false);
		ViewHolder viewHolder = new ViewHolder(itemLayoutView);
		return viewHolder;
	}

}

