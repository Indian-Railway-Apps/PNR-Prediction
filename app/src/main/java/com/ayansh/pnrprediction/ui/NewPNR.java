package com.ayansh.pnrprediction.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.Constants;
import com.ayansh.pnrprediction.application.PNRStatusCommand;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import com.google.analytics.tracking.android.EasyTracker;

public class NewPNR extends Activity implements OnClickListener {

	private TextView pnrNo, trainNo, currentStatus, fromStation, toStation;
	private Button travel_date;
	private Spinner travelClass;
	private ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.new_pnr);
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);
		
		// Show Ads
		if (!Constants.isPremiumVersion()) {

			// Show Ad.
			AdRequest adRequest = new AdRequest.Builder().build();
			AdView adView = (AdView) findViewById(R.id.adView);

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
		}
		
		Button getPNR = (Button) findViewById(R.id.get_pnr_details);
		getPNR.setOnClickListener(this);
		
		travel_date = (Button) findViewById(R.id.travel_date);
		travel_date.setOnClickListener(this);
		
		GregorianCalendar c = new GregorianCalendar();
		int month = c.get(Calendar.MONTH) + 1;
		travel_date.setText(c.get(Calendar.DATE) + "-" + month + "-" + c.get(Calendar.YEAR));
		
		pnrNo = (TextView) findViewById(R.id.pnr_no);
		trainNo = (TextView) findViewById(R.id.train_no);
		currentStatus = (TextView) findViewById(R.id.current_status);
		fromStation = (TextView) findViewById(R.id.from_station);
		toStation = (TextView) findViewById(R.id.to_station);
		
		Button predictStatus = (Button) findViewById(R.id.predict_status);
		predictStatus.setOnClickListener(this);
		
		travelClass = (Spinner) findViewById(R.id.travel_class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		menu.findItem(R.id.show_debug_view).setVisible(false);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
		
		case R.id.buy:
			Intent buy = new Intent(NewPNR.this, ActivatePremiumFeatures.class);
    		NewPNR.this.startActivityForResult(buy,900);
    		break;
			
		case R.id.show_debug_view:
			Intent debug = new Intent(NewPNR.this, DebugView.class);
    		NewPNR.this.startActivity(debug);
    		break;
    		
		case R.id.Help:
			EasyTracker.getTracker().sendView("/Help");
			Intent help = new Intent(NewPNR.this, DisplayFile.class);
			help.putExtra("File", "help.html");
			help.putExtra("Title", "Help: ");
			NewPNR.this.startActivity(help);
    		break;
    		
    	case R.id.About:
    		EasyTracker.getTracker().sendView("/About");
    		Intent info = new Intent(NewPNR.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			NewPNR.this.startActivity(info);
    		break;
		
		}
		
		return true;
		
	}

	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
		
		case R.id.get_pnr_details:
			getPNRDetails();
			break;
			
		case R.id.travel_date:
			getTravelDateFromUser();
			break;
			
		case R.id.predict_status:
			predictStatus();
			break;
		
		}
		
	}

	private void getPNRDetails() {
		
		String pnr = pnrNo.getEditableText().toString();
		
		if(pnr.length() < 10){
			// Error !
			Toast.makeText(this, "Wrong PNR", Toast.LENGTH_LONG).show();
			return;
		}
		
		dialog = ProgressDialog.show(this, "Fetching PNR Details", "Please wait while we fetch PNR Details");
		
		CommandExecuter ce = new CommandExecuter();
		
		PNRStatusCommand command = new PNRStatusCommand(new Invoker(){

			@Override
			public void NotifyCommandExecuted(ResultObject result) {
				
				dialog.dismiss();
				if(result.isCommandExecutionSuccess()){
					
					String pnr = result.getData().getString("CurrentStatus");
					
					trainNo.setText(result.getData().getString("TrainNo"));
					currentStatus.setText(pnr);
					travel_date.setText(result.getData().getString("TravelDate"));
					fromStation.setText(result.getData().getString("FromStation"));
					toStation.setText(result.getData().getString("ToStation"));
					
					String[] tcl = getResources().getStringArray(R.array.travel_class);
					for(int i=0; i< tcl.length; i++){
						
						if(tcl[i].contentEquals(result.getData().getString("TravelClass"))){
							travelClass.setSelection(i);
							break;
						}
					}
					
				}
				else{
					Toast.makeText(getApplicationContext(), "Error while getting PNR Status", Toast.LENGTH_LONG).show();
				}
				
			}

			@Override
			public void ProgressUpdate(ProgressInfo progress) {				
			}}, pnr);
		
		ce.execute(command);
	}

	private void predictStatus() {
		
		// Validate input.
		try {
			
			validateInput();
			
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		
		String[] tclass = getResources().getStringArray(R.array.travel_class);
		
		String tNo = trainNo.getEditableText().toString();
		String tCl = tclass[travelClass.getSelectedItemPosition()];
		String cSt = currentStatus.getEditableText().toString();
		String pnr = pnrNo.getEditableText().toString();
		String fs = fromStation.getEditableText().toString();
		String ts = toStation.getEditableText().toString();
		
		CharSequence tDt = travel_date.getText();
		
		Intent probResult = new Intent(NewPNR.this, ProbabilityResult.class);
		
		probResult.putExtra("PNR", pnr);
		probResult.putExtra("TrainNo", tNo);
		probResult.putExtra("TravelDate", tDt);
		probResult.putExtra("TravelClass", tCl);
		probResult.putExtra("CurrentStatus", cSt);
		probResult.putExtra("FromStation", fs);
		probResult.putExtra("ToStation", ts);
		
		startActivity(probResult);
		
	}
	
	private void validateInput() throws Exception{
	
		// Check that train no is populated and is 5 char.
		String tNo = trainNo.getEditableText().toString();
		if(tNo == null || tNo.length() < 5){
			throw new Exception("Train number is not valid");
		}
		
		// Check the current status
		String cSt = currentStatus.getEditableText().toString();
		if(cSt.contains("WL") || cSt.contains("RAC")){
			// This is ok
		}
		else{
			throw new Exception("Current status is not in correct format.");
		}
		
		// Check From Station and to Station
		String fs = fromStation.getEditableText().toString();
		String ts = toStation.getEditableText().toString();
		
		if(fs == null || fs.contentEquals("")){
			throw new Exception("Please enter From Station Code");
		}
		
		if(ts == null || ts.contentEquals("")){
			throw new Exception("Please enter To Station Code");
		}
	}

	private void getTravelDateFromUser() {
		
		GregorianCalendar c = new GregorianCalendar();
		
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){

			@Override
			public void onDateSet(DatePicker view, int year, int month, int day) {
				
				travel_date.setText(day + "-" + ++month + "-" + year);
				
			}}, year, month, day);
		
		dialog.show();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case 900:
			if (data.getBooleanExtra("RestartApp", false)) {
				finish();
			}
			break;

		}
	}

	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}
}