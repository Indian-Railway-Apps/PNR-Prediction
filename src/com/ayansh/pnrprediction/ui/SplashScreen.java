package com.ayansh.pnrprediction.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.ayansh.pnrprediction.R;
import com.ayansh.pnrprediction.application.Constants;
import com.ayansh.pnrprediction.application.PPApplication;
import com.ayansh.pnrprediction.billingutil.IabHelper;
import com.ayansh.pnrprediction.billingutil.IabHelper.OnIabSetupFinishedListener;
import com.ayansh.pnrprediction.billingutil.IabHelper.QueryInventoryFinishedListener;
import com.ayansh.pnrprediction.billingutil.IabResult;
import com.ayansh.pnrprediction.billingutil.Inventory;
import com.ayansh.pnrprediction.billingutil.Purchase;
import com.google.analytics.tracking.android.EasyTracker;

public class SplashScreen extends Activity implements
		OnIabSetupFinishedListener, QueryInventoryFinishedListener {

	private PPApplication app;
	private IabHelper billingHelper;
	private TextView statusView;
	private boolean appStarted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_screen);
		
		statusView = (TextView) findViewById(R.id.status);
		statusView.setText("Initializing");
			
        // Get Application Instance.
        app = PPApplication.getInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);
        
		// Accept my Terms
        if (!app.isEULAAccepted()) {
			
			Intent eula = new Intent(SplashScreen.this, Eula.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Aggrement: ");
			SplashScreen.this.startActivityForResult(eula, 100);
			
		} else {
			// Start the Main Activity
			startSplashScreenActivity();
		}
		
	}
	
	private void startSplashScreenActivity() {
			
		// Instantiate billing helper class
		billingHelper = IabHelper.getInstance(this, Constants.getPublicKey());
		
		if(billingHelper.isSetupComplete()){
			// Set up is already done... so Initialize app.
			startApp();
		}
		else{
			// Set up
			try{
				billingHelper.startSetup(this);
			}
			catch(Exception e){
				// Oh Fuck !
				Log.w(PPApplication.TAG, e.getMessage(), e);
				billingHelper.dispose();
				finish();
			}
		}

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {

		case 100:
			if (!app.isEULAAccepted()) {
				finish();
			} else {
				// Start Main Activity
				startSplashScreenActivity();
			}
			break;
		}
	}
	
	private void startApp() {
		
		if(appStarted){
			return;
		}
		
		appStarted = true;
		
		// Start the Quiz List
		Log.i(PPApplication.TAG, "Start Main");
		Intent start = new Intent(SplashScreen.this, Main.class);
		SplashScreen.this.startActivity(start);
		
		// Kill this activity.
		Log.i(PPApplication.TAG, "Kill Splash screen");
		SplashScreen.this.finish();
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		
		if (!result.isSuccess()) {
			
			// Log error ! Now I don't know what to do
			Log.w(PPApplication.TAG, result.getMessage());
			
			Constants.setPremiumVersion(false);
			
			// Initialize the app
			startApp();
			
			
		} else {
			
			// Check if the user has purchased premium service			
			// Query for Product Details
			
			List<String> productList = new ArrayList<String>();
			productList.add(Constants.getProductKey());
			
			try{
				billingHelper.queryInventoryAsync(true, productList, this);
			}
			catch(Exception e){
				Log.w(PPApplication.TAG, e.getMessage(), e);
			}
			
		}
		
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		
		if (result.isFailure()) {
			
			// Log error ! Now I don't know what to do
			Log.w(PPApplication.TAG, result.getMessage());
			
			Constants.setPremiumVersion(false);
			
		} else {
			
			String productKey = Constants.getProductKey();
			
			Purchase item = inv.getPurchase(productKey);
			
			if (item != null) {
				// Has user purchased this premium service ???
				Constants.setPremiumVersion(inv.hasPurchase(productKey));
				
			}
			else{
				Constants.setPremiumVersion(false);
			}
			
			if(inv.getSkuDetails(productKey) != null){
				
				Constants.setProductTitle(inv.getSkuDetails(productKey).getTitle());
				Constants.setProductDescription(inv.getSkuDetails(productKey).getDescription());
				Constants.setProductPrice(inv.getSkuDetails(productKey).getPrice());
			}
			
		}
		
		// Initialize the app
		startApp();
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance().activityStop(this);
	}
	
	@Override
	protected void onDestroy(){
		
		if(Constants.isPremiumVersion()){
			try{
				IabHelper.getInstance().dispose();
			}
			catch(Exception e){
				Log.w(PPApplication.TAG, e.getMessage(), e);
			}
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	
    		return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
}