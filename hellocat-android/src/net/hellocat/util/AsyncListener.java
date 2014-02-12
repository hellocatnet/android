package net.hellocat.util;

import org.json.JSONException;

public interface AsyncListener {

	void onSuccess(String response) throws JSONException;

	void onError(Exception exception, String message);
}
