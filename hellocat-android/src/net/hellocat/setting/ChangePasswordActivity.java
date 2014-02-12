package net.hellocat.setting;

import java.util.HashMap;
import java.util.Map;

import net.hellocat.common.Config;
import net.hellocat.common.User;
import net.hellocat.util.Util;
import net.hellocat.util.VolleyListener;
import net.hellocat.util.VolleyManager;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.summertaker.cat.R;

public class ChangePasswordActivity extends Activity implements OnClickListener {

	Context mContext;
	SharedPreferences mPreference;
	VolleyManager mVolleyManager;

	private String mPassword1;
	private String mPassword2;
	private String mPassword3;

	private EditText mEtPassword1;
	private EditText mEtPassword2;
	private EditText mEtPassword3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);

		mContext = this;
		mPreference = getSharedPreferences(Config.USER_PREFERENCE_KEY, MODE_PRIVATE);
		mVolleyManager = new VolleyManager(mContext, null);

		mEtPassword1 = (EditText) findViewById(R.id.etPassword1);
		mEtPassword2 = (EditText) findViewById(R.id.etPassword2);
		mEtPassword3 = (EditText) findViewById(R.id.etPassword3);

		findViewById(R.id.btChangePasswordSubmit).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.change_password, menu);
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		// 폼 제출 클릭 시
		case R.id.btChangePasswordSubmit:
			attemptSubmit();
			break;
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields,
	 * etc.), the errors are presented and no actual login attempt is made.
	 */
	public void attemptSubmit() {
		mEtPassword1.setError(null);
		mEtPassword2.setError(null);
		mEtPassword3.setError(null);

		mPassword1 = mEtPassword1.getText().toString();
		mPassword2 = mEtPassword2.getText().toString();
		mPassword3 = mEtPassword3.getText().toString();

		boolean error = false;
		View focusView = null;

		if (mPassword1.length() < 4) {
			mEtPassword1.setError(getString(R.string.error_input_password_more_than_4));
			focusView = mEtPassword1;
			error = true;
		} else if (mPassword2.length() < 4) {
			mEtPassword2.setError(getString(R.string.error_input_password_more_than_4));
			focusView = mEtPassword2;
			error = true;
		} else if (mPassword3.length() < 4) {
			mEtPassword3.setError(getString(R.string.error_input_password_more_than_4));
			focusView = mEtPassword3;
			error = true;
		} else if (!mPassword2.equalsIgnoreCase(mPassword3)) {
			mEtPassword3.setError(getString(R.string.error_input_same_password_with_confirm_password));
			focusView = mEtPassword3;
			error = true;
		}

		if (error) {
			focusView.requestFocus();
		} else {
			doSubmit();
		}
	}

	/*
	 * 폼 제출하기
	 */
	private void doSubmit() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", User.getId());
		params.put("action", "password");
		params.put("password", Util.md5(mPassword1));
		params.put("password_new", Util.md5(mPassword2));

		mVolleyManager.request(Config.URL_USER_UPDATE, params, true);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {

				SharedPreferences.Editor editor = mPreference.edit();
				editor.putString("userPassword", Util.md5(mPassword2));
				editor.commit();

				Util.alert(mContext, null, getString(R.string.changed), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
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
