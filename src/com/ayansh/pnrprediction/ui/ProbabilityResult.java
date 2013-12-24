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
import android.os.Bundle;
import android.widget.TextView;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.CalculateProbabilityCommand;

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

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		pd.dismiss();
		
		if(result.isCommandExecutionSuccess()){
			
			resultView.append("\n\nProbability of getting confirmed ticket is: " + result.getData().getString("CNF"));
			resultView.append("\nProbability of getting RAC ticket is: " + result.getData().getString("RAC"));
			
			resultView.append("\nThe accuracy of this calculation is: 90%");
			
			resultView.append("\n\nHope you liked it");
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

}
