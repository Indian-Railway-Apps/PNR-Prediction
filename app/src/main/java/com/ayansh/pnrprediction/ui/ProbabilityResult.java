/**
 * 
 */
package com.ayansh.pnrprediction.ui;

import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.CalculateProbabilityCommand;
import com.ayansh.pnrprediction.application.Constants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * @author I041474
 * 
 */
public class ProbabilityResult extends Activity implements Invoker {

	private ProgressDialog pd;
	private TextView resultView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.probability_details);
		
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
		
		resultView = (TextView) findViewById(R.id.result_view);
				
		pd = ProgressDialog.show(this, "Calculating",
				"Please wait... the system is simulating...");

		String tNo = getIntent().getStringExtra("TrainNo");
		String tCl = getIntent().getStringExtra("TravelClass");
		String cSt = getIntent().getStringExtra("CurrentStatus");
		String tDt = getIntent().getStringExtra("TravelDate");
		String pnr = getIntent().getStringExtra("PNR");
		String fs = getIntent().getStringExtra("FromStation");
		String ts = getIntent().getStringExtra("ToStation");

		CalculateProbabilityCommand command = new CalculateProbabilityCommand(
				this, pnr, tNo, tDt, tCl, cSt, fs, ts);

		CommandExecuter ce = new CommandExecuter();
		ce.execute(command);

		resultView.append("Current Status is: " + cSt);
		resultView.append("\nTrain No is: " + tNo);
		resultView.append("\nTravel Date is: " + tDt);
		resultView.append("\nTrain Class is: " + tCl);
		
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
			Intent buy = new Intent(ProbabilityResult.this, ActivatePremiumFeatures.class);
			ProbabilityResult.this.startActivityForResult(buy,900);
    		break;
			
		case R.id.show_debug_view:
			Intent debug = new Intent(ProbabilityResult.this, DebugView.class);
			ProbabilityResult.this.startActivity(debug);
    		break;
		
		}
		
		return true;
		
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
	public void NotifyCommandExecuted(ResultObject result) {
		
		pd.dismiss();
		
		if(result.isCommandExecutionSuccess()){
			
			if(result.getData().getInt("ResultCode") == 0){
				
				resultView.append("\n\nProbability of getting confirmed ticket is: " + result.getData().getString("CNF"));
				resultView.append("\nProbability of getting RAC ticket is: " + result.getData().getString("RAC"));
				resultView.append("\nOptimistically speaking, Probability of getting confirmed ticket is: " + result.getData().getString("OptCNF"));
				resultView.append("\nOptimistically speaking, Probability of getting RAC ticket is: " + result.getData().getString("OptRAC"));
				resultView.append("\nOn the date of travel, expected status is: " + result.getData().getString("ExpectedStatus"));
				
				//resultView.append("\nThe accuracy of this calculation is: 90%");
				
				resultView.append("\n\nHope you liked it");
				
			}
			else{
				
				resultView.append("\n\nError occured while calculating probability.");
				resultView.append("\n" + result.getData().getString("Message"));
			}
			
		}
		else{
			
			resultView.append("\n\nError occured while calculating probability.");
			resultView.append("\n" + result.getErrorMessage());
		}
	}

	@Override
	public void ProgressUpdate(ProgressInfo progress) {
		// Nothing to do
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}

}