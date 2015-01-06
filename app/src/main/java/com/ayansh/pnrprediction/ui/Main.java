package com.ayansh.pnrprediction.ui;

import java.util.ArrayList;
import java.util.List;

import org.varunverma.CommandExecuter.Command;
import org.varunverma.CommandExecuter.CommandExecuter;
import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.MultiCommand;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.Constants;
import com.ayansh.pnrprediction.application.PNR;
import com.ayansh.pnrprediction.application.PPApplication;
import com.ayansh.pnrprediction.application.SaveRegIdCommand;
import com.ayansh.pnrprediction.avail.FetchAvailabilityCommand;
import com.ayansh.pnrprediction.billingutil.IabHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;

public class Main extends Activity implements OnItemClickListener {

	private ListView listView;
	private PNRListAdapter adapter;
	private List<PNR> pnrList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		setTitle("PNR List");
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);

        // Start Fetch Availability.
		fetchAvailability();
		
		// Show Ads
		if (!Constants.isPremiumVersion()) {

			// Show Ad.
			AdRequest adRequest = new AdRequest.Builder().build();
			AdView adView = (AdView) findViewById(R.id.adView);

			// Start loading the ad in the background.
			adView.loadAd(adRequest);
		}
		
		pnrList = new ArrayList<PNR>();
		
		pnrList.addAll(PPApplication.getInstance().getPNRList(true));
		
		pnrList.add(0, new PNR("DUMMY"));	// Dummy Entry
		
		listView = (ListView) findViewById(R.id.pnr_list);
		
		adapter = new PNRListAdapter(this, R.layout.pnrlistrow, R.id.pnr_no, pnrList);
		
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		listView.setOnItemClickListener(this);
		
	}

	private void fetchAvailability() {
		
		CommandExecuter ce = new CommandExecuter();
		
		Invoker dummy = new Invoker(){

			@Override
			public void NotifyCommandExecuted(ResultObject result) {
				// Nothing
			}

			@Override
			public void ProgressUpdate(ProgressInfo progress) {
				// Nothing
			}};
		
		MultiCommand command = new MultiCommand(dummy);
			
		PPApplication app = PPApplication.getInstance();
		
		String regStatus = app.getOptions().get("RegistrationStatus");
		String regId = app.getRegistrationId();
		
		if(regId == null || regId.contentEquals("")){
			// Nothing to do.
		}
		else{
			if(regStatus == null || regStatus.contentEquals("")){
				SaveRegIdCommand saveRegId = new SaveRegIdCommand(dummy,regId);
				command.addCommand(saveRegId);
			}
		}
		
		Command availCommand = new FetchAvailabilityCommand(dummy);
		command.addCommand(availCommand);
		
		ce.execute(command);
		
		PPApplication.getInstance().setExecutingCommand(command);
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
			Intent buy = new Intent(Main.this, ActivatePremiumFeatures.class);
    		Main.this.startActivityForResult(buy,900);
    		break;
			
		case R.id.show_debug_view:
			Intent debug = new Intent(Main.this, DebugView.class);
    		Main.this.startActivity(debug);
    		break;
    		
		case R.id.Help:
			EasyTracker.getTracker().sendView("/Help");
			Intent help = new Intent(Main.this, DisplayFile.class);
			help.putExtra("File", "help.html");
			help.putExtra("Title", "Help: ");
			Main.this.startActivity(help);
    		break;
    		
    	case R.id.About:
    		EasyTracker.getTracker().sendView("/About");
    		Intent info = new Intent(Main.this, DisplayFile.class);
			info.putExtra("File", "about.html");
			info.putExtra("Title", "About: ");
			Main.this.startActivity(info);
    		break;
		
		}
		
		return true;
		
	}

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case 100:
			
			pnrList.clear();
			pnrList.addAll(PPApplication.getInstance().getPNRList(true));
			pnrList.add(0, new PNR("DUMMY"));
			
			adapter.notifyDataSetChanged();
			break;
			
		case 900:
			if (data.getBooleanExtra("RestartApp", false)) {
				finish();
			}
			break;
			
		}
	}

    @Override
	protected void onDestroy() {

		// Close billing helper

		if (isFinishing()) {

			try {
				IabHelper.getInstance().dispose();
			} catch (Exception e) {
				Log.w(PPApplication.TAG, e.getMessage(), e);
			}

			PPApplication.getInstance().close();

		}

		super.onDestroy();
	}

	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		
		if(pos == 0){
			// New PNR
			
			Intent newPNR = new Intent(Main.this, NewPNR.class);
			Main.this.startActivityForResult(newPNR, 100);
			
		}
		else{
			
			Intent pnrDetails = new Intent(Main.this, PNRDetails.class);
			pnrDetails.putExtra("Position", pos-1);
			Main.this.startActivityForResult(pnrDetails,100);
		}
		
	}
	
}