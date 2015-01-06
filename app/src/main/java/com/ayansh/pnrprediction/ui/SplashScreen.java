package com.ayansh.pnrprediction.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class SplashScreen extends Activity implements
		OnIabSetupFinishedListener, QueryInventoryFinishedListener {

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
		PPApplication app = PPApplication.getInstance();
        
        // Set the context of the application
        app.setContext(getApplicationContext());
		
		// Tracking.
        EasyTracker.getInstance().activityStart(this);
        
		// Accept my Terms
        if (!app.isEULAAccepted()) {
			
			Intent eula = new Intent(SplashScreen.this, Eula.class);
        	eula.putExtra("File", "eula.html");
			eula.putExtra("Title", "End User License Agreement: ");
			SplashScreen.this.startActivityForResult(eula, 100);
			
		} else {
			// Start the Main Activity
			startSplashScreenActivity();
		}
		
	}

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 420).show();

            } else {

                Log.i(PPApplication.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }

        return true;

    }
	
	private void startSplashScreenActivity() {

        // Check device for Play Services APK.
        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
        }
        else{
            // Don't know what to do.
            return;
        }

		// Register application.
		PPApplication.getInstance().registerAppForGCM();
        
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
		
		PPApplication app = PPApplication.getInstance();
		
		switch (requestCode) {

		case 100:
			if (!app.isEULAAccepted()) {
				finish();
			} else {
				// Start Main Activity
				startSplashScreenActivity();
			}
			break;
			
		case 900:
			app.addParameter("FirstLaunch", "Completed");
			startApp();
			break;
			
		case 901:
			startApp();
			break;
		}
	}
	
	private void startApp() {
		
		PPApplication app = PPApplication.getInstance();
		
		if(appStarted){
			return;
		}
		
		// Show help for the 1st launch
		if(app.getOptions().get("FirstLaunch") == null){
			// This is first launch !
			showHelp();
			return;
		}
		
		// Check if version is updated.
		int oldAppVersion = app.getOldAppVersion();
		int newAppVersion;
		try {
			newAppVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			newAppVersion = 0;
			Log.e(PPApplication.TAG, e.getMessage(), e);
		}
		
		if(newAppVersion > oldAppVersion ){
			// Update App Version
			app.updateVersion();
			
			showWhatsNew();
			return;
		}
		
		appStarted = true;
		
		// Start the Main
		Log.i(PPApplication.TAG, "Start Main");
		Intent start = new Intent(SplashScreen.this, Main.class);
		SplashScreen.this.startActivity(start);
		
		// Kill this activity.
		Log.i(PPApplication.TAG, "Kill Splash screen");
		SplashScreen.this.finish();
	}

	private void showWhatsNew() {
		
		Intent newFeatures = new Intent(SplashScreen.this, DisplayFile.class);
		newFeatures.putExtra("File", "NewFeatures.html");
		newFeatures.putExtra("Title", "New Features: ");
		SplashScreen.this.startActivityForResult(newFeatures, 901);
	}

	private void showHelp() {
		
		Intent help = new Intent(SplashScreen.this, DisplayFile.class);
		help.putExtra("File", "help.html");
		help.putExtra("Title", "Help: ");
		SplashScreen.this.startActivityForResult(help, 900);
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

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
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