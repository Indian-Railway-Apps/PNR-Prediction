/**
 * 
 */
package com.ayansh.pnrprediction.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
import com.ayansh.pnrprediction.application.PPApplication;

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
		
		resultView = (TextView) findViewById(R.id.result_view);
		
		if(checkLimit()){
		
			pd = ProgressDialog.show(this, "Calculating", "Please wait... the system is simulating...");
			
			String tNo = getIntent().getStringExtra("TrainNo");
			String tCl = getIntent().getStringExtra("TravelClass");
			String cSt = getIntent().getStringExtra("CurrentStatus");
			String tDt = getIntent().getStringExtra("TravelDate");;
			
			CalculateProbabilityCommand command = new CalculateProbabilityCommand(this, tNo, tDt, tCl, cSt);

			CommandExecuter ce = new CommandExecuter();
			ce.execute(command);

			resultView.append("Current Status is: " + cSt);
			resultView.append("\nTrain No is: " + tNo);
			resultView.append("\nTravel Date is: " + tDt);
			resultView.append("\nTrain Class is: " + tCl);
			
		}
		else{
			
			String error = "You have crossed the montly limit of 30 queries.\n" +
					"Sorry, but in order to ensure that the system is not overloaded, " +
					"we have to limit the number of queries from each mobile\n\n" +
					"In order to get un-limited access, please upgrade to premium version.";
			
			resultView.append(error);
		}
		
	}

	private boolean checkLimit() {

		String counterName = getCounterName();
		String counterValue = PPApplication.getInstance().getOptions().get(counterName);
		
		if(Integer.getInteger(counterValue) >= 30){
			return false;
		}
		else{
			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		if(false){
			menu.findItem(R.id.show_debug_view).setVisible(false);
		}
		
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
		
		if(result.isCommandExecutionSuccess() && result.getData().getInt("ResultCode") == 0){
			
			resultView.append("\n\nProbability of getting confirmed ticket is: " + result.getData().getString("CNF"));
			resultView.append("\nProbability of getting RAC ticket is: " + result.getData().getString("RAC"));
			
			resultView.append("\nThe accuracy of this calculation is: 90%");
			
			resultView.append("\n\nHope you liked it");
			
			// Success. Increment the counter for this month.
			String counterName = getCounterName();
			PPApplication.getInstance().incrementCounter(counterName);
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
	
	private String getCounterName(){
		
		GregorianCalendar today = new GregorianCalendar();
		int year = today.get(Calendar.YEAR);
		int month = today.get(Calendar.MONTH);
		String counterName = year + month + "_Counter";
		
		return counterName;
	}

}
