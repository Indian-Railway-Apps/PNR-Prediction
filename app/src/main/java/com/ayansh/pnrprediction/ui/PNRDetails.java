package com.ayansh.pnrprediction.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.CalculateProbabilityCommand;
import com.ayansh.pnrprediction.application.Constants;
import com.ayansh.pnrprediction.application.PNR;
import com.ayansh.pnrprediction.application.PNRStatusCommand;
import com.ayansh.pnrprediction.application.PPApplication;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.google.analytics.tracking.android.EasyTracker;

public class PNRDetails extends Activity implements OnClickListener, Invoker {

	private PNR pnr;
	private ProgressDialog pd;
	private TextView currentStatus, probResult, expectedStatus;
	
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.pnr_details);
		
		setTitle("PNR Details");
		
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
		
		int pos = getIntent().getIntExtra("Position", -1);
		if(pos < 0){
			return;
		}
		
		pnr = PPApplication.getInstance().getPNRList(false).get(pos);
		
		// Display
		TextView pnrNo = (TextView) findViewById(R.id.pnr_no);
		TextView pnrDet1 = (TextView) findViewById(R.id.pnr_det1);
		TextView pnrDet2 = (TextView) findViewById(R.id.pnr_det2);
		currentStatus = (TextView) findViewById(R.id.current_status);
		probResult = (TextView) findViewById(R.id.probability);
		expectedStatus = (TextView) findViewById(R.id.expected_status);
		
		pnrNo.setText(pnr.getPnr());
		pnrDet1.setText(pnr.getTrainNo() + " / " + pnr.getTrainClass());
		pnrDet2.setText("From " + pnr.getFromStation() + " to " + pnr.getToStation());
		currentStatus.setText("Status : " + pnr.getCurrentStatus());
		
		TextView travelDate = (TextView) findViewById(R.id.travel_date);
		TextView travelMonth = (TextView) findViewById(R.id.travel_month);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		
		try {
			
			Date dt = sdf.parse(pnr.getTravelDate());
			GregorianCalendar trDate = new GregorianCalendar();
			trDate.setTime(dt);
			
			travelDate.setText(String.valueOf(trDate.get(Calendar.DATE)));
			travelMonth.setText(trDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
			
		} catch (ParseException e) {
			// Nothing.
		}
		
		String probability = "CNF Probability - " + pnr.getCnfProb() + "\n"
							+ "RAC Probability - " + pnr.getRacProb() + "\n"
							+ "Optimistic CNF Probability - " + pnr.getOptCNFProb() + "\n"
							+ "Optimistic RAC Probability - " + pnr.getOptRACProb() + "\n";

		probResult.setText(probability);
		expectedStatus.setText("Expected Status : " + pnr.getExpectedStatus());
		
		Button predictPNR = (Button) findViewById(R.id.predict_pnr);
		predictPNR.setOnClickListener(this);
		
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
			Intent buy = new Intent(PNRDetails.this, ActivatePremiumFeatures.class);
    		PNRDetails.this.startActivityForResult(buy,900);
    		break;
			
		case R.id.show_debug_view:
			Intent debug = new Intent(PNRDetails.this, DebugView.class);
    		PNRDetails.this.startActivity(debug);
    		break;
    		
		case R.id.Help:
			EasyTracker.getTracker().sendView("/Help");
			Intent help = new Intent(PNRDetails.this, DisplayFile.class);
			help.putExtra("File", "help.html");
			help.putExtra("Title", "Help: ");
			PNRDetails.this.startActivity(help);
    		break;
    		
    	case R.id.About:
    		EasyTracker.getTracker().sendView("/About");
    		Intent info = new Intent(PNRDetails.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			PNRDetails.this.startActivity(info);
    		break;
		
		}
		
		return true;
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
			
		case R.id.predict_pnr:
			predictPNRStatus();
			break;
		
		}
		
	}

	private void predictPNRStatus() {
		
		pd = ProgressDialog.show(this, "Please wait...",
				"Step 1/2 : Fetching latest PNR Status...");
		
		// 1st get latest status
		CommandExecuter ce = new CommandExecuter();
		
		PNRStatusCommand command = new PNRStatusCommand(this, pnr.getPnr());
		
		ce.execute(command);
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		String message = "";
		
		if(result.getCommand().getCommandName().contains("PNRStatus")){
			
			if (result.isCommandExecutionSuccess()) {

				String current_status = result.getData().getString("CurrentStatus");
				
				pnr.setCurrentStatus(current_status);
				
				CalculateProbabilityCommand command = new CalculateProbabilityCommand(
						this, pnr.getPnr(), pnr.getTrainNo(),
						pnr.getTravelDate(), pnr.getTrainClass(),
						current_status, pnr.getFromStation(),
						pnr.getToStation());
				
				CommandExecuter ce = new CommandExecuter();
				ce.execute(command);
				
				pd.setMessage("Step 2/2 : Calculating Probability...");
				
			} else {
				// Command Execution Failed
				pd.dismiss();
				
				message = "Error while fetching PNR Status. Cannot calculate probability.\n" +
						"Please try again.";
				
				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}
			
		}
		
		else if (result.getCommand().getCommandName().contains("CalculateProbability")){
			
			pd.dismiss();
			
			if (result.isCommandExecutionSuccess()) {
				
				pnr.setCnfProb(Float.valueOf(result.getData().getString("CNF")));
				pnr.setRacProb(Float.valueOf(result.getData().getString("RAC")));
				pnr.setOptCNFProb(Float.valueOf(result.getData().getString("OptCNF")));
				pnr.setOptRACProb(Float.valueOf(result.getData().getString("OptRAC")));
				pnr.setExpectedStatus(result.getData().getString("ExpectedStatus"));
				
				currentStatus.setText("Status : " + pnr.getCurrentStatus());
				
				String probability = "CNF Probability - " + pnr.getCnfProb() + "\n"
									+ "RAC Probability - " + pnr.getRacProb() + "\n"
									+ "Optimistic CNF Probability - " + pnr.getOptCNFProb() + "\n"
									+ "Optimistic RAC Probability - " + pnr.getOptRACProb() + "\n";

				probResult.setText(probability);
				expectedStatus.setText("Expected Status : " + pnr.getExpectedStatus());
				
			}
			else{
				
				// Command Execution Failed
				message = "Error while calculating probability.\n" +
						"Please try again.";
				
				Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			}
			
		}
		
		
	}

	@Override
	public void ProgressUpdate(ProgressInfo pi) {
		// Nothing to do
	}
	
}