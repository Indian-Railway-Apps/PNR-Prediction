package com.ayansh.pnrprediction.ui;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.PPApplication;

public class DebugView extends Activity implements Invoker {

	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.debug_view);
		
		tv = (TextView) findViewById(R.id.textView);
		
		PPApplication.getInstance().getExecutingCommand().updateInvoker(this);
				
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
	
	@Override
	protected void onDestroy(){
		
		PPApplication.getInstance().getExecutingCommand().updateInvoker(new Invoker(){

			@Override
			public void NotifyCommandExecuted(ResultObject result) {}

			@Override
			public void ProgressUpdate(ProgressInfo progress) {}
		});
		
		super.onDestroy();
		
	}

}