package net.hellocat.auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.hellocat.article.ArticleListActivity;
import net.hellocat.common.Config;
import net.hellocat.common.DataUtil;
import net.hellocat.common.User;
import net.hellocat.util.GoogleMomentUtil;
import net.hellocat.util.Util;
import net.hellocat.util.VolleyListener;
import net.hellocat.util.VolleyManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.summertaker.cat.R;

public class LoginActivity extends Activity implements OnClickListener, PlusClient.ConnectionCallbacks,
		PlusClient.OnConnectionFailedListener, PlusClient.OnAccessRevokedListener {

	private Context mContext;
	private SharedPreferences mPreference;
	private VolleyManager mVolleyManager;
	private ProgressDialog mProgressDialog;

	// 페이스북
	private UiLifecycleHelper uiHelper; // for Facebook
	private static final int REQUEST_CODE_FACEBOOK_LOGIN = 64206;

	// 구글 변수
	private static final int REQUEST_CODE_GOOGLE_LOGIN = 1;
	private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 2;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;

	LinearLayout mLoLoginLoading;
	ScrollView mSvLoginView;

	boolean mIsFirstLoading = true;
	boolean mIsFacebookCheckFinished = false;
	boolean mIsGoogleCheckFinished = false;
	boolean mIsLoginStarted = false;

	private String mEmail;
	private String mPassword;
	private String mMd5Password;

	private EditText mEtEmail;
	private EditText mEtPassword;

	private final int ACTIVITY_REQUEST_CODE_PASSWORD = 540;
	private final int ACTIVITY_REQUEST_CODE_JOIN = 550;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mContext = LoginActivity.this;
		mPreference = getSharedPreferences(Config.USER_PREFERENCE_KEY, MODE_PRIVATE);
		mVolleyManager = new VolleyManager(mContext, null);

		uiHelper = new UiLifecycleHelper(LoginActivity.this, callback);
		uiHelper.onCreate(savedInstanceState);

		//Utils.printHashKey(this); // Hash Key for Facebook

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage(mContext.getResources().getString(R.string.wait_a_second));
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						mVolleyManager.cancel();
					}
				});

		getLoginInfo();

		mLoLoginLoading = (LinearLayout) findViewById(R.id.loLoginLoading);
		mSvLoginView = (ScrollView) findViewById(R.id.svLoginView);

		mEtEmail = (EditText) findViewById(R.id.etEmail);
		mEtEmail.setText(mEmail);
		/*
		mEtEmail.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				isEmailLoginMode = true;
				showLoginFormOnly();
				return false;
			}
		});
		*/

		mEtPassword = (EditText) findViewById(R.id.etPassword);
		mEtPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					trySubmit();
					handled = true;
				}
				return handled;
			}
		});

		findViewById(R.id.btEmailLogin).setOnClickListener(this); // 로그인 버튼
		findViewById(R.id.btnGoogleLogin).setOnClickListener(this); // 구글플러스로 로그인

		findViewById(R.id.tvJoinTitle).setOnClickListener(this); // 회원가입 버튼
		findViewById(R.id.tvJoinDescription).setOnClickListener(this); // 회원가입 버튼

		findViewById(R.id.tvFindPasswordTitle).setOnClickListener(this); // 비밀번호찾기 버튼
		findViewById(R.id.tvFindPasswordDescription).setOnClickListener(this); // 비밀번호찾기 버튼		

		//-------------------
		// 페이스북 로그인
		//-------------------
		LoginButton authButton = (LoginButton) findViewById(R.id.btFacebookLogin);
		authButton.setReadPermissions(Arrays.asList("email"));
		Session session = Session.getActiveSession();
		if (session != null) {
			if (session.isOpened()) {
				Session.openActiveSession(this, true, callback);
			} else {
				mIsFacebookCheckFinished = true;
			}

		} else {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, null, savedInstanceState);
			} else {
				mIsFacebookCheckFinished = true;
			}
		}

		//----------------------
		// 구글 플러스 로그인
		//----------------------
		if (mIsFacebookCheckFinished) {
			mPlusClient = new PlusClient.Builder(this, this, this).setActions(GoogleMomentUtil.ACTIONS).build();
			mPlusClient.connect();
		}
	}

	@Override
	public void onClick(View view) {

		Intent intent = null;

		switch (view.getId()) {

		// 로그인
		case R.id.btEmailLogin:
			trySubmit();
			break;

		// 구글 로그인
		case R.id.btnGoogleLogin:
			showLoading();

			try {
				if (mConnectionResult == null) {
					mPlusClient.connect();
				} else {
					mConnectionResult.startResolutionForResult(this, REQUEST_CODE_GOOGLE_LOGIN);
				}
			} catch (IntentSender.SendIntentException e) {
				mPlusClient.connect();
			}
			break;

		// 회원 가입
		case R.id.tvJoinTitle:
		case R.id.tvJoinDescription:
			intent = new Intent(getApplicationContext(), JoinActivity.class);
			startActivityForResult(intent, ACTIVITY_REQUEST_CODE_JOIN);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;

		// 비밀번호 찾기
		case R.id.tvFindPasswordTitle:
		case R.id.tvFindPasswordDescription:
			intent = new Intent(getApplicationContext(), FindPasswordActivity.class);
			startActivityForResult(intent, ACTIVITY_REQUEST_CODE_PASSWORD);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);

		Log.d(">>onActivityResult()", "requestCode: " + requestCode + ", resultCode: " + resultCode);

		switch (requestCode) {

		// 비밀번호 찾기
		case ACTIVITY_REQUEST_CODE_PASSWORD:
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;

		// 회원가입
		case ACTIVITY_REQUEST_CODE_JOIN:
			if (resultCode == Config.ACTIVITY_RESULT_CODE_CONTINUE) {
				getLoginInfo();
				doSubmit();
			}
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			break;

		// 페이스북
		case REQUEST_CODE_FACEBOOK_LOGIN:
			//Session.openActiveSession(this, true, callback);
			//uiHelper.onResume();
			break;

		// 구글
		case REQUEST_CODE_GOOGLE_LOGIN:
		case REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES:
			if (resultCode == RESULT_OK && !mPlusClient.isConnected() && !mPlusClient.isConnecting()) {
				// This time, connect should succeed.
				mPlusClient.connect();
			} else {
				hideLoading();
			}
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mPlusClient != null) {
			mPlusClient.disconnect();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@SuppressWarnings("deprecation")
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			//Log.i(">>fb", "Logged in...");

			Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (user != null) {
						//Log.i(">>fb", user.toString());

						String id = user.getId();
						String name = user.getName();

						GraphObject graph = response.getGraphObject();
						JSONObject json = graph.getInnerJSONObject();

						try {
							mEmail = json.getString("email");
							if (mEmail == null || mEmail.isEmpty()) {
								hideLoading();
								Util.alert(mContext, null, getString(R.string.failed_to_get_facebook_email), null);
							} else {
								User.setUser(mEmail, name, "facebook", id);
								doSubmit();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			});
		} else if (state.isClosed()) {
			//Log.i(">>fb", "Logged out...");
		}
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	/*
	 * 로그인 정보 가져오기
	 */
	private void getLoginInfo() {
		mEmail = mPreference.getString("userEmail", "");
		mMd5Password = mPreference.getString("userPassword", "");
	}

	/*
	 * 구글 플러스
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		//Util.toast(mContext, "onConnected()");
		// String userName = getString(R.string.unknown_person);

		Person person = mPlusClient.getCurrentPerson();

		if (person != null) {
			//Log.d(Config.TAG, currentPerson.toString());
			mEmail = mPlusClient.getAccountName(); // 이메일
			User.setUser(mEmail, person.getDisplayName(), "google", person.getId());

			doSubmit();
		} else {
			mPlusClient.clearDefaultAccount();
			mPlusClient.disconnect();
			//mPlusClient.revokeAccessAndDisconnect(this);
			Util.alert(mContext, null, getString(R.string.getting_google_user_information_is_failed), null);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		//Util.toast(mContext, "onConnectionFailed()");
		mConnectionResult = result;

		if (mIsFirstLoading) {
			mIsGoogleCheckFinished = true;
			mIsFirstLoading = false;
			tryAutoLogin();
		}
	}

	@Override
	public void onAccessRevoked(ConnectionResult status) {
		if (!status.isSuccess()) {
			mPlusClient.disconnect();
		}
	}

	@Override
	public void onDisconnected() {
		//toast("onDisconnected()");
		if (mPlusClient != null) {
			mPlusClient.connect();
		}
	}

	private void showLoading() {
		mLoLoginLoading.setVisibility(View.VISIBLE);
		mSvLoginView.setVisibility(View.GONE);
	}

	private void hideLoading() {
		mLoLoginLoading.setVisibility(View.GONE);
		mSvLoginView.setVisibility(View.VISIBLE);
	}

	/*
	 * 이메일 로그인
	 */
	private void tryAutoLogin() {
		if (!mEmail.isEmpty() && !mMd5Password.isEmpty()) {
			doSubmit();
		} else {
			hideLoading();
		}
	}

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields,
	 * etc.), the errors are presented and no actual login attempt is made.
	 */
	public void trySubmit() {
		//Log.i(Config.TAG, "attemptLogin()");

		mEmail = mEtEmail.getText().toString();
		mPassword = mEtPassword.getText().toString();

		mEtEmail.setError(null);
		mEtPassword.setError(null);

		boolean error = false;
		String errorMessage = "";
		EditText focusView = null;

		if (TextUtils.isEmpty(mPassword)) {
			error = true;
			focusView = mEtPassword;
			errorMessage = getString(R.string.error_input_password);
		} else if (TextUtils.isEmpty(mEmail)) {
			error = true;
			focusView = mEtEmail;
			errorMessage = getString(R.string.error_input_email);
		} else if (!mEmail.contains("@") || !mEmail.contains(".")) {
			error = true;
			focusView = mEtEmail;
			errorMessage = getString(R.string.error_invalid_email);
		}

		if (error) {
			focusView.setError(errorMessage);
			focusView.requestFocus();
		} else {
			mMd5Password = Util.md5(mPassword);
			User.setEmail(mEmail);

			Util.hideKeyboard(mContext);

			doSubmit();
		}
	}

	/*
	 * 로그인 요청하기
	 */
	private void doSubmit() {
		if (!mIsLoginStarted) {
			mIsLoginStarted = true;
			showLoading();

			Map<String, String> params = new HashMap<String, String>();
			params.put("email", mEmail + "");
			params.put("password", mMd5Password + "");
			params.put("name", User.getName() + "");
			params.put("auth_site", User.getAuthSite() + "");
			params.put("auth_id", User.getAuthId() + "");
			params.put("gcm_id", User.getGcmId() + "");

			mVolleyManager.request(Config.URL_LOGIN, params, false);

			mVolleyManager.listener = new VolleyListener() {
				@Override
				public void onSuccess(String response) throws JSONException {

					mEtEmail.setText(mEmail);

					JSONObject json = new JSONObject(response);
					JSONObject result = json.getJSONObject("result");

					User.setId(result.getString("id"));
					User.setName(result.getString("name"));
					User.setEmail(result.getString("email"));
					User.setPicture(DataUtil.getPictureUrl(result.getString("picture_thumb")));
					User.setBackground(DataUtil.getPictureUrl(result.getString("background_thumb")));
					User.setAuthSite(result.getString("auth_site"));
					User.setAuthId(result.getString("auth_id"));

					SharedPreferences.Editor editor = mPreference.edit();
					editor.putString("userEmail", mEmail);
					editor.putString("userPassword", mMd5Password);
					editor.commit();

					goNext();
				}

				@Override
				public void onError(String response, String message) {
					mIsLoginStarted = false;
					hideLoading();

					SharedPreferences.Editor editor = mPreference.edit();
					editor.putString("userPassword", null);
					editor.commit();

					/*
					Session session = Session.getActiveSession();
					if (session != null) {
						if (!session.isClosed()) {
							session.closeAndClearTokenInformation();
						}
					}
					if (mPlusClient != null) {
						mPlusClient.disconnect();
					}
					*/
				}
			};
		}
	}

	private void goNext() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				//Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
				Intent intent = new Intent(getApplicationContext(), ArticleListActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		}, 500);
	}
}
