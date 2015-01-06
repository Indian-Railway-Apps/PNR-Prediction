package com.ayansh.pnrprediction.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.PNR;

public class PNRListAdapter extends ArrayAdapter<PNR> {

	List<PNR> pnrList;
	
	public PNRListAdapter(Context context, int resource, int textViewResourceId, List<PNR> pnrList) {
		
		super(context, resource, textViewResourceId, pnrList);
		
		this.pnrList = pnrList;
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View rowView;
		
		PNR pnr = pnrList.get(position);
		
		if (convertView == null) {
			rowView = LayoutInflater.from(getContext()).inflate(R.layout.pnrlistrow, parent, false);
			rowView.setTag(pnr.getPnr());
		}

		else {
			if(convertView.getTag().toString().contentEquals(pnr.getPnr())){
				rowView = convertView;
			}
			else{
				rowView = LayoutInflater.from(getContext()).inflate(R.layout.pnrlistrow, parent, false);
				rowView.setTag(pnr.getPnr());
			}
		}
		
		TextView pnrNo = (TextView) rowView.findViewById(R.id.pnr_no);
		TextView currentStatus = (TextView) rowView.findViewById(R.id.current_status);
		TextView lastUpdate = (TextView) rowView.findViewById(R.id.last_update);
		
		TextView travelDate = (TextView) rowView.findViewById(R.id.travel_date);
		TextView travelMonth = (TextView) rowView.findViewById(R.id.travel_month);
		
		if(pnr.getPnr().contentEquals("DUMMY")){
			
			LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.trdate);
			ll.setBackgroundResource(R.drawable.new_pnr);
			
			travelDate.setVisibility(View.GONE);
			travelMonth.setVisibility(View.GONE);
			lastUpdate.setVisibility(View.GONE);
			
			pnrNo.setText("New PNR");
			currentStatus.setText("Click to predict status for new PNR");
			
		}
		else{
		
			pnrNo.setText(pnr.getPnr());
			currentStatus.setText("Status: " + pnr.getCurrentStatus());
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			
			Date date = new Date();
			date.setTime(pnr.getLastUpdate());
			
			lastUpdate.setText("Last Update: " + sdf.format(date));
			
			try {
				
				Date dt = sdf.parse(pnr.getTravelDate());
				GregorianCalendar trDate = new GregorianCalendar();
				trDate.setTime(dt);
				
				travelDate.setText(String.valueOf(trDate.get(Calendar.DATE)));
				travelMonth.setText(trDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
				
			} catch (ParseException e) {
				// Nothing.
			}
			
		}
		
		return rowView;
		
	}
	
}