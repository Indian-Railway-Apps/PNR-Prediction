package org.varunverma.pnrprediction;

import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.pnrprediction.avail.FetchAvailabilityCommand;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class Main extends Activity implements Invoker {

	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		tv = (TextView) findViewById(R.id.textView);
		
		fetchAvailability();
		
	}

	private void fetchAvailability() {
		
		CommandExecuter ce = new CommandExecuter();
		
		Command availCommand = new FetchAvailabilityCommand(this);
		
		ce.execute(availCommand);
		tv.append("Command Execution Started");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void NotifyCommandExecuted(ResultObject result) {
		
		tv.append("\nCommand Execution completed");
		
		if(result.isCommandExecutionSuccess()){
		}
		else{
			tv.append("\n" + result.getErrorMessage());
		}
		
	}

	@Override
	public void ProgressUpdate(ProgressInfo pi) {
		
		if(!pi.getProgressMessage().contentEquals("")){
			tv.append("\n" + pi.getProgressMessage());
		}
		
	}

}