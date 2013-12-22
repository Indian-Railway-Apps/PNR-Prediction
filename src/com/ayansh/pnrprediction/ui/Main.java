package com.ayansh.pnrprediction.ui;

import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.PPApplication;
import com.ayansh.pnrprediction.avail.FetchAvailabilityCommand;

public class Main extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		// Set up Application.
		PPApplication app = PPApplication.getInstance();
		
		app.setContext(getApplicationContext());
				
		fetchAvailability();
		
	}

	private void fetchAvailability() {
		
		CommandExecuter ce = new CommandExecuter();
		
		Command availCommand = new FetchAvailabilityCommand(new Invoker(){

			@Override
			public void NotifyCommandExecuted(ResultObject result) {
				// Nothing
			}

			@Override
			public void ProgressUpdate(ProgressInfo progress) {
				// Nothing
			}});
		
		ce.execute(availCommand);
		
		PPApplication.getInstance().setExecutingCommand(availCommand);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		return true;
		
	}
}