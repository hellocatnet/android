package net.hellocat.auth;

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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.summertaker.cat.R;

public class FindPasswordActivity extends Activity {
	
	Context mContext;
	VolleyManager mVolleyManager;

	private String mEmail;
	private EditText mEtEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password);
		
		mContext = this;
		mVolleyManager = new VolleyManager(mContext, null);
		
		mEtEmail = (EditText) findViewById(R.id.etEmail);
		mEtEmail.setText(User.getEmail());
		
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
		getMenuInflater().inflate(R.menu.find_password, menu);
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

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields,
	 * etc.), the errors are presented and no actual login attempt is made.
	 */
	public void submitForm() {
		mEtEmail.setError(null);
		mEmail = mEtEmail.getText().toString();

		boolean error = false;
		View focusView = null;

		if (TextUtils.isEmpty(mEmail)) {
			mEtEmail.setError(getString(R.string.error_input_email));
			focusView = mEtEmail;
			error = true;
		} else if (!mEmail.contains("@") || !mEmail.contains(".")) {
			mEtEmail.setError(getString(R.string.error_invalid_email));
			focusView = mEtEmail;
			error = true;
		}

		if (error) {
			focusView.requestFocus();
		} else {
			doSubmit();
		}
	}
	
	private void doSubmit() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("email", mEmail);

		mVolleyManager.request(Config.URL_PASSWORD, params, true);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {

				Util.alert(mContext, null, getString(R.string.password_has_been_sent_via_email), new DialogInterface.OnClickListener() {
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
