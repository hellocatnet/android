package net.hellocat.util;

import org.json.JSONException;

public interface VolleyListener {

	void onSuccess(String response) throws JSONException;

	void onError(String response, String message);
}
