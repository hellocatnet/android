package net.hellocat.util;

import java.util.Map;

import net.hellocat.common.Config;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.summertaker.cat.R;

public class VolleyManager {

	private Context mContext;
	private Activity mActivity;

	private RequestQueue mQueue;
	private Listener<String> mSuccessListener;
	private ErrorListener mErrorListener;
	private Map<String, String> mParams;
	private String mTag = "tag";

	public VolleyListener listener;

	private ProgressDialog mProgressDialog;
	private boolean mShowProgress = true;
	private boolean mDismissProgress = true;
	private boolean mAalertOnError = true;
	private boolean mFinishOnError = true;

	public VolleyManager(Context context, RequestQueue queue) {
		mContext = context;
		mActivity = (Activity) context;

		if (queue == null) {
			mQueue = Volley.newRequestQueue(context);
		} else {
			mQueue = queue;
		}
	}

	/*
	 * 요청하기
	 */
	public void request(String url, Map<String, String> params, boolean showProgress) {
		request(url, params, showProgress, showProgress, true, true, null, null);
	}
	
	public void request(String url, Map<String, String> params, boolean showProgress, boolean dismissProgress) {
		request(url, params, showProgress, dismissProgress, true, true, null, null);
	}

	public void request(String url, Map<String, String> params, boolean showProgress, boolean dismissProgress,
			boolean alertOnError, boolean finishOnError) {
		request(url, params, showProgress, dismissProgress, alertOnError, finishOnError, null, null);
	}

	public void request(String url, Map<String, String> params, boolean showProgress, boolean dismissProgress,
			boolean alertOnError, boolean finishOnError, Listener<String> successListener, ErrorListener errorListener) {

		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = (entry.getValue() == null) ? "" : entry.getValue();
				entry.setValue(value);
			}
		}

		mShowProgress = showProgress;
		mDismissProgress = dismissProgress;
		mAalertOnError = alertOnError;
		mFinishOnError = finishOnError;

		if (mShowProgress) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setMessage(mContext.getResources().getString(R.string.wait_a_second));
				mProgressDialog.setCanceledOnTouchOutside(false);
				mProgressDialog.setCancelable(true);
				mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						mContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								mQueue.cancelAll("tag");
								//listener.onError(null, mContext.getResources().getString(R.string.cancelled));
								
								mActivity.finish();
							}
						});
			}
			mProgressDialog.show();
		}

		mParams = params;

		if (successListener == null) {
			mSuccessListener = successListener();
		} else {
			mSuccessListener = successListener;
		}

		if (errorListener == null) {
			mErrorListener = errorListener();
		} else {
			mErrorListener = errorListener;
		}

		StringRequest request = new StringRequest(Method.POST, url, mSuccessListener, mErrorListener) {
			protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
				return mParams;
			};
		};

		request.setTag(mTag);
		mQueue.add(request);
	}

	/*
	 * 요청 성공 리스너
	 */
	private Response.Listener<String> successListener() {
		return new Response.Listener<String>() {
			@Override
			public void onResponse(final String response) {
				//Log.e(Config.tag, response);

				JSONObject json = null;
				String status = null;
				String message = null;

				boolean error = false;

				try {
					json = new JSONObject(response);
				} catch (JSONException e) {
					Log.e(Config.TAG, response);
					error = true;
					jsonError(e);
				}

				if (!error) {

					// 응답필드가 빠져있을 수 있기에 필드별로 각각 예외처리한다.
					try {
						status = json.getString("status");
					} catch (JSONException e) {
						Log.e(Config.TAG, response);
						error = true;
						jsonError(e);
					}

					if (!error) {

						try {
							message = json.getString("message");
						} catch (JSONException e) {

						}

						if (status.equals("200")) {
							//--------------
							// 처리 성공
							//--------------
							if (mDismissProgress) {
								if (mProgressDialog != null) {
									mProgressDialog.dismiss();
								}
							}

							try {
								listener.onSuccess(response);
							} catch (JSONException e) {
								jsonError(e);
							}
						} else {
							//--------------
							// 처리 실패
							//--------------
							//Log.e(Config.tag, response);
							if (mProgressDialog != null) {
								mProgressDialog.dismiss();
							}

							if (mAalertOnError) {
								Util.alert(mContext, null, message, new DialogInterface.OnClickListener() {									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										listener.onError(response, null);
										//mActivity.finish();
									}
								});
							} else {
								listener.onError(response, message);
							}
						}
					}
				}
			}
		};
	}

	/*
	 * 요청 실패 리스너
	 */
	private Response.ErrorListener errorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				error.printStackTrace();
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}

				if (mAalertOnError) {
					DialogInterface.OnClickListener clickListener = mFinishOnError ? finishListener : continueListener;
					Util.error(mContext, error, mContext.getResources().getString(R.string.error_network_request),
							clickListener);
				} else {
					listener.onError(null, error.getLocalizedMessage());
				}
			}
		};
	}

	public void cancel() {
		mQueue.cancelAll(mTag);
	}
	
	public void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	private void jsonError(JSONException exception) {
		exception.printStackTrace();

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}

		if (mAalertOnError) {
			DialogInterface.OnClickListener clickListener = mFinishOnError ? finishListener : continueListener;
			Util.error(mContext, exception, mContext.getResources().getString(R.string.error_json_response), clickListener);
		} else {
			listener.onError(null, exception.getLocalizedMessage());
		}
	}
	
	DialogInterface.OnClickListener continueListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			listener.onError(null, null);
		}
	};

	DialogInterface.OnClickListener finishListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Activity activity = (Activity) mContext;
			activity.finish();
		}
	};
};
