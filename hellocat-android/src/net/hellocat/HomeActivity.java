package net.hellocat;

import net.hellocat.common.Config;
import net.hellocat.common.User;
import net.hellocat.setting.SettingActivity;
import net.hellocat.util.VolleyManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.summertaker.cat.R;

public class HomeActivity extends Activity implements OnClickListener {

	private Context mContext;
	private VolleyManager mVolleyManager;

	private final int REQUEST_CODE_SEARCH = 100;
	private final int REQUEST_CODE_SETTING = 900;

	ImageView mIvUserBackground;
	ImageView mIvUserPicture;
	TextView tvVehicleId;

	TextView mTvSafetyNotification;
	Button mBtSafetyNotifiaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_home);

		if (User.getId() == null) {
			logout();
		} else {
			mContext = this;
			mVolleyManager = new VolleyManager(mContext, null);

			mIvUserPicture = (ImageView) findViewById(R.id.ivUserPicture);
			mIvUserBackground = (ImageView) findViewById(R.id.ivBackground);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_settings:
			goActivity(SettingActivity.class, REQUEST_CODE_SETTING);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {

		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {

		case REQUEST_CODE_SEARCH:
			//-------------
			// 차량검색
			//-------------
			/*
			if (data == null) {
				tvVehicleId.setText(getString(R.string.please_search_vehicle));
			} else {
				String id = data.getStringExtra("id");
				if (id != null) {
					tvVehicleId.setText(ShareData.getVehicleName());
				} else {
					tvVehicleId.setText(getString(R.string.please_search_vehicle));
				}
			}
			*/
			break;
		case REQUEST_CODE_SETTING:
			//--------------
			// 환경설정
			//--------------
			if (resultCode == Config.ACTIVITY_RESULT_CODE_FINISH) {
				setResult(Config.ACTIVITY_RESULT_CODE_FINISH);
				logout();
			}

			break;
		}
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	protected void onResume() {
		super.onResume();

		/*
		 * GCM 메세지 from GCMIntentService
		 */
		String gcmMessage = User.getGcmMessage();
		if (gcmMessage != null) {
			//toast("GCM Message: " + gcmMessage);
			User.setGcmMessage(null);
		}

		doLoad();
	}

	/*
	 * 요약 정보 로드하기
	 */
	private void doLoad() {
		updatePhoto();
	}

	/*
	 * 사진 업데이트
	 */
	private void updatePhoto() {
		String imageUrl = "";

		// 프로필 사진 로드하기
		imageUrl = User.getUserPicture();
		if (imageUrl != null && !imageUrl.isEmpty()) {
			// http://square.github.io/picasso/
			Picasso.with(getApplicationContext()).load(imageUrl).placeholder(R.drawable.icon_home_picture)
					.error(R.drawable.icon_home_picture).into(mIvUserPicture);
			//Picasso.with(getApplicationContext()).load(imageUrl).transform(new CircleTransform()).placeholder(R.drawable.icon_home_picture)
			//		.error(R.drawable.icon_home_picture).into(mIvUserPicture);
		}

		// 배경 사진 로드하기
		imageUrl = User.getBackground();
		if (imageUrl != null && !imageUrl.isEmpty()) {
			// http://square.github.io/picasso/
			Picasso.with(getApplicationContext()).load(imageUrl).placeholder(R.drawable.pic_home_background)
					.error(R.drawable.pic_home_background).into(mIvUserBackground);
		}
	}

	/*
	 * 로그아웃 - 액티비티 종료, 메인화면으로 이동
	 */
	private void logout() {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(intent);
		finish();
	}

	/*
	 * 차량검색 경고창 표시
	 */
	/*
	private void showVehicleSearchAlert() {
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
		adb.setTitle(getString(R.string.app_name));
		adb.setMessage(getString(R.string.please_search_vehicle_first));
		adb.setCancelable(false);
		adb.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		adb.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					dialog.dismiss();
				}
				return true;
			}
		});
		AlertDialog ad = adb.create();
		ad.show();
	}
	*/

	/*
	 * 액티비티 이동
	 */
	private void goActivity(Class<?> cls, int requestCode) {
		Intent intent = new Intent(getApplicationContext(), cls);
		startActivityForResult(intent, requestCode);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
