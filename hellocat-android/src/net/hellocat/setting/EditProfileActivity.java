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

public class EditProfileActivity extends Activity implements OnClickListener {

	Context mContext;
	SharedPreferences mPreference;
	VolleyManager mVolleyManager;

	private String mUserName;

	private EditText mEtUserName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		mContext = this;
		mPreference = getSharedPreferences(Config.USER_PREFERENCE_KEY, MODE_PRIVATE);
		mVolleyManager = new VolleyManager(mContext, null);

		mEtUserName = (EditText) findViewById(R.id.etUserName);
		mEtUserName.setText(User.getName());

		findViewById(R.id.btProfileSubmit).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.edit_profile, menu);
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
		case R.id.btProfileSubmit:
			trySubmit();
			break;
		}
	}
	
	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email,
	 * missing fields, etc.), the errors are presented and no actual login attempt is made.
	 */
	public void trySubmit() {

		mEtUserName.setError(null);

		mUserName = mEtUserName.getText().toString();

		boolean error = false;
		View focusView = null;
		
		if (mUserName.length() < 2) {
			mEtUserName.setError(getString(R.string.error_input_name_more_than_2));
			focusView = mEtUserName;
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
		params.put("user_id", User.getId());
		params.put("action", "profile");
		params.put("name", mUserName);

		mVolleyManager.request(Config.URL_USER_UPDATE, params, true);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {

				User.setName(mUserName);
				
				SharedPreferences.Editor editor = mPreference.edit();
				editor.putString("userName", mUserName);
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
