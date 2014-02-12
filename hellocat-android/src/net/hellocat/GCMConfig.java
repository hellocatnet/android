package net.hellocat;

import net.hellocat.common.Config;

import android.content.Context;
import android.content.Intent;

public final class GCMConfig {

	public static final String SENDER_ID = Config.GCM_SENDER_ID;

	public static final String DISPLAY_MESSAGE_ACTION = Config.GCM_DISPLAY_MESSAGE_ACTION;
	public static final String EXTRA_MESSAGE = "message";

	/**
	 * Notifies UI to display a message.
	 * 
	 * This method is defined in the common helper because it's used both by the UI and the background service.
	 * 
	 * @param context application's context.
	 * @param message message to be displayed.
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}
