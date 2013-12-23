/**
 * 
 */
package com.ayansh.pnrprediction.application;

import org.varunverma.CommandExecuter.Command;

import android.content.Context;

/**
 * @author Varun Verma
 *
 */
public class PPApplication {

	private static PPApplication app;
	public static final String TAG = "PNR";
	
	private Context context;
	private Command executingCommand;
	
	public static PPApplication getInstance(){
		
		if(app == null){
			app = new PPApplication();
		}
		
		return app;
	}
	
	/**
	 * 
	 */
	private PPApplication() {
		// TODO Auto-generated constructor stub
	}

	public void setContext(Context c){
		
		if(context == null){
			context = c;
		}
	}
	
	public void setExecutingCommand(Command c){
		
		executingCommand = c;
		
	}
	
	public Command getExecutingCommand(){
		return executingCommand;
	}

	public boolean isEULAAccepted() {
		return true;
		//TODO - check if EULA is accepted
	}

	public void setEULAResult(boolean b) {
		// TODO Save EULA
		
	}
	
}