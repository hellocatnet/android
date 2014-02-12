package net.hellocat.setting;

import net.hellocat.common.Config;
import net.hellocat.common.User;
import net.hellocat.util.GoogleMomentUtil;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;
import com.squareup.picasso.Picasso;
import com.summertaker.cat.R;

public class SettingActivity extends Activity implements PlusClient.ConnectionCallbacks, PlusClient.OnConnectionFailedListener,
		PlusClient.OnAccessRevokedListener, OnClickListener {

	Context mContext;
	SharedPreferences mPreference;

	private PlusClient mPlusClient;

	private final int ACTIVITY_REQUEST_CODE_ACCOUNT = 200;
	private final int ACTIVITY_REQUEST_CODE_SECEDE = 900;

	private ProgressDialog mProgressDialog;

	ImageView mIvUserPicture;
	TextView mTvUserName;
	TextView mTvUserEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mContext = SettingActivity.this;

		//ActionBar actionBar = getActionBar();
		//actionBar.setDisplayHomeAsUpEnabled(true);

		if (User.getId().isEmpty()) {
			logout();
		} else {

			mPlusClient = new PlusClient.Builder(this, this, this).setActions(GoogleMomentUtil.ACTIONS).build();

			mPreference = getSharedPreferences(Config.USER_PREFERENCE_KEY, MODE_PRIVATE);

			mProgressDialog = new ProgressDialog(SettingActivity.this);
			mProgressDialog.setMessage(getString(R.string.wait_a_second));
			//progressDialog.setCancelable(false);

			//---------------
			// 사용자 사진
			//---------------
			mIvUserPicture = (ImageView) findViewById(R.id.ivUserPicture); // 프로필 사진
			mTvUserName = (TextView) findViewById(R.id.tvUserName); // 사용자 이름		
			mTvUserEmail = (TextView) findViewById(R.id.tvUserEmail); // 사용자 이메일

			findViewById(R.id.btnChangePhoto).setOnClickListener(this); // 사진 변경하기		
			findViewById(R.id.btnEditProfile).setOnClickListener(this); // 프로필 수정하기
			findViewById(R.id.btnLogout).setOnClickListener(this); // 로그아웃
			findViewById(R.id.btnSecede).setOnClickListener(this); // 계정 삭제하기

			Button btnChangePassword = (Button) findViewById(R.id.btnChangePassword); // 비밀번호 수정	
			if (User.getAuthSite().isEmpty()) {
				btnChangePassword.setVisibility(View.VISIBLE);
				btnChangePassword.setOnClickListener(this);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
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

		Intent intent = null;

		switch (view.getId()) {

		// 사진 변경하기
		case R.id.btnChangePhoto:
			intent = new Intent(mContext, ChangePhotoActivity.class);
			startActivityForResult(intent, 0);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;

		// 프로필 수정하기
		case R.id.btnEditProfile:
			intent = new Intent(mContext, EditProfileActivity.class);
			startActivityForResult(intent, ACTIVITY_REQUEST_CODE_ACCOUNT);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;

		// 비밀번호 변경하기
		case R.id.btnChangePassword:
			intent = new Intent(mContext, ChangePasswordActivity.class);
			startActivityForResult(intent, 0);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;

		// 로그아웃
		case R.id.btnLogout:
			logout();
			break;

		// 계정 삭제하기
		case R.id.btnSecede:
			intent = new Intent(getApplicationContext(), SecedeActivity.class);
			startActivityForResult(intent, ACTIVITY_REQUEST_CODE_SECEDE);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case ACTIVITY_REQUEST_CODE_ACCOUNT:
			//updateUserPhoto();
			break;
		case ACTIVITY_REQUEST_CODE_SECEDE:
			//------------
			// 회원탈퇴
			//------------
			ActionBar actionBar = getActionBar();
			actionBar.setTitle(R.string.app_name);
			if (resultCode == Config.ACTIVITY_RESULT_CODE_SECEDE) {
				logout();
			}
			break;
		}

		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!mPlusClient.isConnected()) {
			mPlusClient.connect();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		updateUserPhoto();

		mTvUserName.setText(User.getName());
		mTvUserEmail.setText(User.getEmail());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mProgressDialog.dismiss();
	}

	private void updateUserPhoto() {
		String imageUrl = User.getUserPicture();
		if (imageUrl != null && !imageUrl.isEmpty()) {
			// http://square.github.io/picasso/
			Picasso.with(getApplicationContext()).load(imageUrl).into(mIvUserPicture);
			//Picasso.with(getApplicationContext()).load(imageUrl).transform(new CircleTransform()).into(mIvUserPicture);
		}
	}

	/*
	* 로그아웃
	*/
	private void logout() {
		Session session = Session.getActiveSession();
		if (!session.isClosed()) {
			session.closeAndClearTokenInformation();
		}

		if (mPlusClient != null) {
			if (mPlusClient.isConnected()) {
				mProgressDialog.show();
				mPlusClient.revokeAccessAndDisconnect(this);
			} else {
				mPlusClient.disconnect();
				clear();
			}
		}
	}

	/*
	 * 사용자 데이터 초기화
	 */
	private void clear() {
		User.clear();

		SharedPreferences.Editor editor = mPreference.edit();
		//editor.clear();
		//editor.putString("userEmail", mEmail);
		editor.putString("userPassword", null);
		editor.commit();

		setResult(Config.ACTIVITY_RESULT_CODE_FINISH);
		finish();
	}

	/*
	* 구글 설정
	*/
	@Override
	public void onAccessRevoked(ConnectionResult status) {
		mPlusClient.disconnect();
		clear();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
	}

	@Override
	public void onDisconnected() {
	}
}
