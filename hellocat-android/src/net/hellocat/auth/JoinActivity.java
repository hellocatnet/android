package net.hellocat.auth;

import java.util.HashMap;
import java.util.Map;

import net.hellocat.common.Config;
import net.hellocat.util.Util;
import net.hellocat.util.VolleyListener;
import net.hellocat.util.VolleyManager;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.summertaker.cat.R;

public class JoinActivity extends Activity implements OnClickListener {

	Context mContext;
	SharedPreferences mPreference;
	VolleyManager mVolleyManager;

	private String mName;
	private String mEmail;
	private String mPassword;

	private EditText mEtName;
	private EditText mEtEmail;
	private EditText mEtPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);

		mContext = this;
		mPreference = getSharedPreferences(Config.USER_PREFERENCE_KEY, MODE_PRIVATE);
		mVolleyManager = new VolleyManager(mContext, null);

		mEtName = (EditText) findViewById(R.id.etKeyword);
		mEtEmail = (EditText) findViewById(R.id.etEmail);
		mEtPassword = (EditText) findViewById(R.id.etPassword);

		findViewById(R.id.btSubmit).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.join, menu);
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
			Util.hideKeyboard(mContext);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

		// 폼 제출 클릭 시
		case R.id.btSubmit:
			attemptSubmit();
			break;
		}

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Util.hideKeyboard(mContext);
	}

	/*
	 * 폼 검증하기
	 */
	public void attemptSubmit() {
		mEtName.setError(null);
		mEtEmail.setError(null);
		mEtPassword.setError(null);

		mName = mEtName.getText().toString();
		mEmail = mEtEmail.getText().toString();
		mPassword = mEtPassword.getText().toString();

		boolean error = false;
		View focusView = null;

		if (TextUtils.isEmpty(mName)) {
			mEtName.setError(getString(R.string.error_input_name));
			focusView = mEtName;
			error = true;
		} else if (mName.length() < 2) {
			mEtName.setError(getString(R.string.error_input_name_more_than_2));
			focusView = mEtName;
			error = true;
		} else if (TextUtils.isEmpty(mEmail)) {
			mEtEmail.setError(getString(R.string.error_input_email));
			focusView = mEtEmail;
			error = true;
		} else if (!mEmail.contains("@") || !mEmail.contains(".")) {
			mEtEmail.setError(getString(R.string.error_invalid_email));
			focusView = mEtEmail;
			error = true;
		} else if (TextUtils.isEmpty(mPassword)) {
			mEtPassword.setError(getString(R.string.error_input_new_password));
			focusView = mEtPassword;
			error = true;
		} else if (mPassword.length() < 4) {
			mEtPassword.setError(getString(R.string.error_input_password_more_than_4));
			focusView = mEtPassword;
			error = true;
		}

		if (error) {
			focusView.requestFocus();
		} else {
			Util.hideKeyboard(mContext);

			doSubmit();
		}
	}

	/*
	 * 폼 제출하기
	 */
	private void doSubmit() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("name", mName);
		params.put("email", mEmail);
		params.put("password", Util.md5(mPassword));

		mVolleyManager.request(Config.URL_JOIN, params, true);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {

				SharedPreferences.Editor editor = mPreference.edit();
				editor.putString("userEmail", mEmail);
				editor.putString("userPassword", Util.md5(mPassword));
				editor.commit();

				Util.alert(mContext, null, getString(R.string.account_is_created), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						setResult(Config.ACTIVITY_RESULT_CODE_CONTINUE);
						finish();
					}
				});
			}

			@Override
			public void onError(String response, String message) {

			}
		};
	}
}
