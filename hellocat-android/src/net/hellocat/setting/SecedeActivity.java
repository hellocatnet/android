package net.hellocat.setting;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import net.hellocat.common.Config;
import net.hellocat.common.User;
import net.hellocat.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.summertaker.cat.R;

public class SecedeActivity extends Activity {

	Context mContext;
	SharedPreferences mPreference;

	private SecedeTask mAsyncTask = null;

	private String mPassword;

	private ProgressDialog mProgressDialog;
	
	private TextView mEtName;
	private TextView mEtEmail;
	private EditText mEtPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_secede);

		mContext = this;
		mPreference = getApplicationContext().getSharedPreferences(Config.USER_PREFERENCE_KEY, MODE_PRIVATE);
		
		mProgressDialog = new ProgressDialog(SecedeActivity.this);
		mProgressDialog.setMessage(getString(R.string.wait_a_second));
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setCancelable(false);

		mEtName = (TextView) findViewById(R.id.etKeyword);
		mEtName.setKeyListener(null);
		mEtName.setText(User.getName());

		mEtEmail = (TextView) findViewById(R.id.etEmail);
		mEtEmail.setKeyListener(null);
		mEtEmail.setText(User.getEmail());

		mEtPassword = (EditText) findViewById(R.id.etPassword);

		//Log.d(Config.TAG, ShareData.value());

		if (User.getAuthSite().isEmpty()) {
			/*
			 * 일반 회원인 경우 비빌번호 입력상자 보이기
			 */
			mEtPassword.setVisibility(View.VISIBLE);
		} else {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); // 키보드 숨기기
		}

		findViewById(R.id.btSubmit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				submitForm();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.secede, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_navigation_back:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email,
	 * missing fields, etc.), the errors are presented and no actual login attempt is made.
	 */
	public void submitForm() {
		if (mAsyncTask != null) {
			return;
		}

		mEtPassword.setError(null);
		mPassword = mEtPassword.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (mEtPassword.getVisibility() == View.VISIBLE) {
			if (TextUtils.isEmpty(mPassword)) {
				mEtPassword.setError(getString(R.string.error_input_password));
				focusView = mEtPassword;
				cancel = true;
			} else if (mPassword.length() < 4) {
				mEtPassword.setError(getString(R.string.error_invalid_password));
				focusView = mEtPassword;
				cancel = true;
			}
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0);

			mProgressDialog.show();
			mAsyncTask = new SecedeTask();
			mAsyncTask.execute("");
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate the user.
	 */
	public class SecedeTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Config.URL_SECEDE);

				//Log.d(TAG, ShareData.value());

				// 전송될 데이터 값
				List<NameValuePair> data = new ArrayList<NameValuePair>(2);
				data.add(new BasicNameValuePair("id", User.getId()));
				data.add(new BasicNameValuePair("password", mPassword));

				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(data, HTTP.UTF_8);
				post.setEntity(ent);
				HttpResponse responsePost = client.execute(post);
				HttpEntity resEntity = responsePost.getEntity();

				if (resEntity == null) {
					//Log.d(Config.TAG, ">> resEntity is NULL.");
				} else {
					response = EntityUtils.toString(resEntity);
					//Log.d(Config.TAG, response);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			mAsyncTask = null;
			mProgressDialog.dismiss();

			JSONObject responseJson = null;

			try {
				responseJson = new JSONObject(result);
				
				String title = getString(R.string.app_name);
				String status = responseJson.getString("status");
				String message = responseJson.getString("message");
				
				if (status == null) {
					status = "";
				}				
				if (message == null) {
					message = getString(R.string.try_again_later);
				}
				
				if (status.equalsIgnoreCase("200")) {
					/* --------
					 * 성공
					 * -------- */
					Util.alert(mContext, title, message, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
							
							SharedPreferences.Editor editor = mPreference.edit();
							editor.clear();
							editor.commit();
							
							setResult(Config.ACTIVITY_RESULT_CODE_SECEDE);
							finish();
						}
					});
				} else {
					/* --------
					 * 실패
					 * -------- */
					Util.alert(mContext, title, message, null);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onCancelled() {
			mAsyncTask = null;
			mProgressDialog.dismiss();
		}
	}
}
