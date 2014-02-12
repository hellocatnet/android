package net.hellocat.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.summertaker.cat.R;

public class AsyncManager extends AsyncTask<String, Void, String> {

	private Context mContext;
	private String mUrl;
	private List<NameValuePair> mData;

	public AsyncListener listener;

	private ProgressDialog mProgressDialog;
	private boolean mShowProgress;

	public AsyncManager(Context context, String url, List<NameValuePair> data) {
		mContext = context;
		mUrl = url;
		mData = data;
		mShowProgress = true;
	}

	public AsyncManager(Context context, String url, List<NameValuePair> data, boolean showProgress) {
		mContext = context;
		mUrl = url;
		mData = data;
		mShowProgress = showProgress;
	}

	@Override
	protected void onPreExecute() {
		if (mShowProgress) {
			mProgressDialog = new ProgressDialog(mContext);
			mProgressDialog.setMessage(mContext.getResources().getString(R.string.wait_a_second));
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setCancelable(true);
			mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getResources().getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							cancel();
						}
					});
			mProgressDialog.show();
		}
	}
	
	@Override
	protected String doInBackground(String... params) {
		String response = null;

		String errorMessage = mContext.getResources().getString(R.string.error_network_request);

		try {
			HttpPost post = new HttpPost(mUrl);
			post.setEntity(new UrlEncodedFormEntity(mData, HTTP.UTF_8));

			HttpClient client = new DefaultHttpClient();
			HttpResponse responsePost = client.execute(post);
			HttpEntity resEntity = responsePost.getEntity();

			if (resEntity != null) {
				//Log.d(Config.TAG, response);
				response = EntityUtils.toString(resEntity);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e("AsyncManager", response);
			listener.onError(e, errorMessage + "\n" + e.getLocalizedMessage());
		} catch (ClientProtocolException e) {
			Log.e("AsyncManager", response);
			e.printStackTrace();
			listener.onError(e, errorMessage + "\n" + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("AsyncManager", response);
			e.printStackTrace();
			listener.onError(e, errorMessage + "\n" + e.getLocalizedMessage());
		}

		return response;
	}

	@Override
	protected void onPostExecute(String response) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		
		try {
			JSONObject json = new JSONObject(response);
			String status = json.getString("status") + "";

			if (status.equalsIgnoreCase("200")) {
				listener.onSuccess(response);
			} else {
				String message = json.getString("message") + "";
				if (message.isEmpty()) {
					message = mContext.getResources().getString(R.string.error_occured) + "\nServer message is null.";
				}
				listener.onError(null, message);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("AsyncManager", response);
			listener.onError(e, mContext.getResources().getString(R.string.error_json_response));
		}
	}

	@Override
	protected void onCancelled() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		
		listener.onError(null, mContext.getResources().getString(R.string.cancelled));
	}

	protected void cancel() {
		//this.cancel();
	}
}
