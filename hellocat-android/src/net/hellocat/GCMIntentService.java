package net.hellocat;

import net.hellocat.common.User;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.summertaker.cat.R;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(GCMConfig.SENDER_ID);
	}

	/**
	 * Method called on device registered.
	 * */
	@Override
	protected void onRegistered(Context context, String registrationId) {
		//Log.i(TAG, ">> Device registered: registrationId = " + registrationId);
		GCMRegistrar.setRegisteredOnServer(context, true);
	}

	/**
	 * Method called on device un registred
	 * */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		//Log.i(TAG, "Device unregistered");
		GCMRegistrar.setRegisteredOnServer(context, false);
	}

	/**
	 * Method called on receiving a deleted message
	 * */
	@Override
	protected void onDeletedMessages(Context context, int total) {
		//Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		generateNotification(context, message);
	}

	/**
	 * Method called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {
		//Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		//Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Method called on Receiving a new message
	 * */
	@Override
	protected void onMessage(Context context, Intent intent) {
		//Log.i(TAG, "Received message");
		
		/*
		Bundle bundle = intent.getExtras();
        Set<String> setKey = bundle.keySet();
        Iterator<String> iterKey = setKey.iterator();
        while (iterKey.hasNext()){
            String key = iterKey.next();
            String value = bundle.getString(key);
            Log.d(Config.TAG, ">> onMessage(): key = " + key + ", value = " + value);
        }
        */
		
		Bundle bundle = intent.getExtras();
		String message = bundle.getString("message");
		User.setGcmMessage(message);		
		//Log.d(Config.TAG, ">> GCM Message: " + message);
		
		generateNotification(context, message);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	@SuppressWarnings("deprecation")
	private void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);

		// 실행시킬 인텐트
		Intent callbackIntent = new Intent(context, MainActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("message", message);
		callbackIntent.putExtras(bundle);

		// set intent so it does not start a new activity
		callbackIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, callbackIntent, 0);

		notification.setLatestEventInfo(context, title, message, pendingIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		//notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "your_sound_file_name.mp3");

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);
	}
}
