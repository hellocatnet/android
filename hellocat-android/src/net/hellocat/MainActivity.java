package net.hellocat;

import java.io.IOException;

import net.hellocat.auth.LoginActivity;
import net.hellocat.util.Util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.summertaker.cat.R;

public class MainActivity extends Activity {

	private Context mContext;
	private SharedPreferences mPrefs;

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private GoogleCloudMessaging mGCM;
	private String mRegId;
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = this; //getApplicationContext();
		
		//Util.printKeyHash(mContext); // for Facebook

		/// Check device for Play Services APK. If check succeeds, proceed with GCM registration.
		if (checkPlayServices()) {
			mGCM = GoogleCloudMessaging.getInstance(this);
			mRegId = getRegistrationId(mContext);

			if (mRegId.isEmpty()) {
				registerInBackground();
			} else {
				goNext();
			}
		} else {
			//Log.i(Config.TAG, "No valid Google Play Services APK found.");
			finish();
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows
	 * users to download the APK from the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		if (isConnected) {
			int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
			if (resultCode == ConnectionResult.SUCCESS) {
				return true;
			} else {
				if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
					GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
				} else {
					//Log.i(Config.TAG, "This device is not supported.");
					Util.alert(mContext, null, getString(R.string.google_play_service_is_not_available), null);
				}
				//return false;
				return true; // 푸쉬 기능 없이도 프로그램 사용은 가능하도록
			}
		} else {
			Util.alert(mContext, null, getString(R.string.check_internet_connection), null);
			return false;
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			//Log.i(Config.TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			//Log.i(Config.TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (mGCM == null) {
						mGCM = GoogleCloudMessaging.getInstance(mContext);
					}
					mRegId = mGCM.register(GCMConfig.SENDER_ID);
					//msg = "Device registered, registration ID=" + mRegId;

					// You should send the registration ID to your server over HTTP,
					// so it can use GCM/HTTP or CCS to send messages to your app.
					// The request to your server should be authenticated if your app
					// is using accounts.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the device
					// will send upstream messages to a server that echo back the
					// message using the 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(mContext, mRegId);
				} catch (IOException exception) {
					msg = exception.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				//Log.d(Config.TAG, msg);
				if (msg.isEmpty()) {
					goNext();
				} else {
					Util.alert(mContext, null, getString(R.string.failed_to_regist_gcm_id), new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						}
					});
				}
			}
		}.execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send messages to your app.
	 * Not needed for this demo since the device sends upstream messages to a server that echoes back the message using
	 * the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		// Your implementation here.
		//Log.d(Config.TAG, ">> sendRegistrationIdToBackend()");
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		//Log.i(Config.TAG, "Saving regId on app version " + appVersion);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {
		super.onResume();
		if (!checkPlayServices()) {
			finish();
		}
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void goNext() {
		//UserData.setUserGcmId(mRegId);

		//Handler handler = new Handler();
		//handler.postDelayed(new Runnable() {
		//	public void run() {
		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		finish();
		//	}
		//}, 1000);
	}
}
