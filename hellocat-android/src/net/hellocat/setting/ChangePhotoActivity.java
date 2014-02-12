package net.hellocat.setting;

import java.io.File;
import java.io.FileNotFoundException;

import net.hellocat.common.Config;
import net.hellocat.common.DataUtil;
import net.hellocat.common.User;
import net.hellocat.util.Util;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.summertaker.cat.R;

public class ChangePhotoActivity extends Activity implements OnClickListener {

	final int REQUEST_CODE_CAMERA_CAPTURE = 1;
	final int REQUEST_CODE_CHOOSE_PHOTO = 2;
	final int REQUEST_CODE_PHOTO_CROP = 3;

	private Context mContext;

	ImageView mIvPicture;
	ImageView mIvBackground;

	LinearLayout mLoLoadingPicture;
	LinearLayout mLoLoadingBackground;

	private String mPhotoSelectMode;
	private String mFileName;

	/*
	private UploadTask mUploadTask = null;
	private UpdateTask mUpdateTask = null;
	*/

	//AlertDialog.Builder mAlertDialogBuilder = null;
	SharedPreferences mUserPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_photo);

		mContext = this;

		mUserPreferences = getApplicationContext().getSharedPreferences(Config.USER_PREFERENCE_KEY, 0);

		String imageUrl = "";

		// 사용자 프로필 사진
		mLoLoadingPicture = (LinearLayout) findViewById(R.id.loLoadingPicture);
		mIvPicture = (ImageView) findViewById(R.id.ivPicture);
		imageUrl = User.getUserPicture();
		if (imageUrl != null && !imageUrl.isEmpty()) {
			loadPhoto(imageUrl, mIvPicture, mLoLoadingPicture);
		} else {
			mLoLoadingPicture.setVisibility(View.GONE);
			mIvPicture.setVisibility(View.VISIBLE);
		}

		// 사용자 배경 사진
		mLoLoadingBackground = (LinearLayout) findViewById(R.id.loLoadingBackground);
		mIvBackground = (ImageView) findViewById(R.id.ivBackground);
		imageUrl = User.getBackground();
		if (imageUrl != null && !imageUrl.isEmpty()) {
			loadPhoto(imageUrl, mIvBackground, mLoLoadingBackground);
		} else {
			mLoLoadingBackground.setVisibility(View.GONE);
			mIvBackground.setVisibility(View.VISIBLE);
		}

		findViewById(R.id.btChangePicture).setOnClickListener(this);
		findViewById(R.id.btChangeBackground).setOnClickListener(this);

		/*
		AsyncHttpClient client = new AsyncHttpClient();
		client.get("http://www.google.com", new AsyncHttpResponseHandler() {
		    @Override
		    public void onSuccess(String response) {
		        System.out.println(response);
		    }
		});
		*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.change_photo, menu);
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
		case R.id.btChangePicture:
			mPhotoSelectMode = "picture";
			selectImage();
			break;
		case R.id.btChangeBackground:
			mPhotoSelectMode = "background";
			selectImage();
			break;
		}
	}

	private void selectImage() {
		/*
		final CharSequence[] options = { getString(R.string.take_photo), getString(R.string.choose_photo),
				getString(R.string.cancel) };

		String title = getString(R.string.profile_photo);
		if (mPhotoSelectMode.equalsIgnoreCase("background")) {
			title = getString(R.string.background_photo);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(ChangePhotoActivity.this);
		builder.setTitle(title);
		builder.setItems(options, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					try {
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(intent, REQUEST_CODE_CAMERA_CAPTURE);
					} catch (ActivityNotFoundException anfe) {
						//display an error message
						String errorMessage = "사진찍기 기능을 지원하지 않습니다.";
						Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
					}
				} else if (item == 1) {
					Intent intent = new Intent(Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(intent, REQUEST_CODE_CHOOSE_PHOTO);
				} else if (item == 2) {
					dialog.dismiss();
				}
			}
		});

		builder.show();
		*/

		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_CODE_CHOOSE_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		boolean isUploadMode = false;

		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_PHOTO_CROP) {
				isUploadMode = true;
			} else {
				mFileName = getRealPathFromURI(getApplicationContext(), data.getData());
				isUploadMode = true;
			}
		} else if (resultCode == RESULT_CANCELED) {

		}

		if (isUploadMode) {
			//File file = new File(mFileName);
			//mUploadTask = new UploadTask(mContext, Config.URL_UPLOAD + "_" + mPhotoSelectMode, file);
			//mUploadTask.execute();

			upload();
		}
	}

	/*
	 * 파일 업로드하기
	 */
	private void upload() {
		if ("picture".equalsIgnoreCase(mPhotoSelectMode)) {
			mIvPicture.setVisibility(View.GONE);
			mLoLoadingPicture.setVisibility(View.VISIBLE);
		} else {
			mIvBackground.setVisibility(View.GONE);
			mLoLoadingBackground.setVisibility(View.VISIBLE);
		}

		RequestParams params = new RequestParams();
		params.put("user_id", User.getId());
		params.put("target", mPhotoSelectMode);

		try {
			File file = new File(mFileName); // "/path/to/file.png"
			params.put("picture", file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//----------------------------------------
		// http://loopj.com/android-async-http/
		//----------------------------------------
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(Config.URL_UPLOAD, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				String response = new String(responseBody);
				//Log.i(">> response", response);

				try {
					JSONObject json = new JSONObject(response);
					JSONObject result = new JSONObject(json.getString("result"));

					String fileName = DataUtil.getPictureUrl(result.getString("file_name"));
					//Log.i(">> fileName", fileName);

					if ("picture".equalsIgnoreCase(mPhotoSelectMode)) {
						User.setPicture(fileName);
						loadPhoto(fileName, mIvPicture, mLoLoadingPicture);
					} else {
						User.setBackground(fileName);
						loadPhoto(fileName, mIvBackground, mLoLoadingBackground);
					}

					//if ("picture".equalsIgnoreCase(mPhotoSelectMode)) {
					//	mIvPicture.setVisibility(View.VISIBLE);
					//	mLoLoadingPicture.setVisibility(View.GONE);
					//} else {
					//	mIvBackground.setVisibility(View.VISIBLE);
					//	mLoLoadingBackground.setVisibility(View.GONE);
					//}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
				e.printStackTrace();
				if (errorResponse != null) {
					Log.e(">>", new String(errorResponse));
				}
				Util.alert(mContext, null, getString(R.string.error_json_response),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
						});
			}
		});
	}

	/*
	 * 사진 로드하기
	 */
	private void loadPhoto(String url, final ImageView imageView, final LinearLayout loading) {
		String domain = url.substring(0, 4).equalsIgnoreCase("http") ? "" : Config.SERVER_DOMAIN;

		imageView.setVisibility(View.GONE);
		loading.setVisibility(View.VISIBLE);

		Picasso.with(mContext).load(domain + url).into(imageView, new Callback() {
			@Override
			public void onSuccess() {
				imageView.setVisibility(View.VISIBLE);
				loading.setVisibility(View.GONE);
			}

			@Override
			public void onError() {
			}
		});
	}

	/*
	 * 사진의 실제 경로 리턴하기
	 */
	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/*
	 * 사진 업로드 + 업로드 진행 상태 바
	 */
	/*
	private class UploadTask extends AsyncTask<Void, Integer, String> implements DialogInterface.OnCancelListener {

		private Context context;
		private ProgressDialog progressDialog;
		private String url;
		private File file;

		public UploadTask(Context context, String url, File file) {
			this.context = context;
			this.url = url;
			this.file = file;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(context);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("사진 업로드 중...");
			progressDialog.setCancelable(false);
			progressDialog.setMax((int) file.length());
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... v) {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection connection = null;
			String fileName = file.getName();

			String response = null;

			try {
				connection = (HttpURLConnection) new URL(url).openConnection();

				// Allow Inputs & Outputs
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.setUseCaches(false);

				// Enable POST method
				connection.setRequestMethod("POST");

				connection.setRequestProperty("Connection", "Keep-Alive");

				String boundary = "---------------------------boundary";
				String tail = "\r\n--" + boundary + "--\r\n";
				connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

				String metadataPart = "--" + boundary + "\r\n"
						+ "Content-Disposition: form-data; name=\"metadata\"\r\n\r\n" + "" + "\r\n";

				String fileHeader1 = "--" + boundary + "\r\n"
						+ "Content-Disposition: form-data; name=\"uploadfile\"; filename=\"" + fileName + "\"\r\n"
						+ "Content-Type: application/octet-stream\r\n" + "Content-Transfer-Encoding: binary\r\n";

				long fileLength = file.length() + tail.length();
				String fileHeader2 = "Content-length: " + fileLength + "\r\n";
				String fileHeader = fileHeader1 + fileHeader2 + "\r\n";
				String stringData = metadataPart + fileHeader;

				long requestLength = stringData.length() + fileLength;
				connection.setRequestProperty("Content-length", "" + requestLength);
				connection.setFixedLengthStreamingMode((int) requestLength);
				connection.connect();

				DataOutputStream out = new DataOutputStream(connection.getOutputStream());
				out.writeBytes(stringData);
				out.flush();

				int progress = 0;
				int bytesRead = 0;
				byte buf[] = new byte[1024];
				BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
				while ((bytesRead = bufInput.read(buf)) != -1) {
					// write output
					out.write(buf, 0, bytesRead);
					out.flush();
					progress += bytesRead;
					// update progress bar
					publishProgress(progress);
				}

				bufInput.close();
				bufInput = null;

				// Write closing boundary and close stream
				out.writeBytes(tail);
				out.flush();
				out.close();

				// Get server response
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = "";
				StringBuilder builder = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				reader.close();
				reader = null;

				response = builder.toString();
				builder = null;
				//Log.d(Config.TAG, ">> doInBackground(): " + response);

				connection.disconnect();
				connection = null;

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
					connection = null;
				}
			}

			return response;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress((int) (progress[0]));
		}

		@Override
		protected void onPostExecute(String result) {
			mUploadTask = null;
			progressDialog.dismiss();

			String title = getString(R.string.error);
			String message = getString(R.string.try_again_later);

			if (result == null) {
				Util.confirm(mContext, title, message, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						finish();
					}
				});
			} else {
				JSONObject responseJson = null;

				try {
					responseJson = new JSONObject(result);
					String status = responseJson.getString("status");
					String jsonMessage = responseJson.getString("message");
					if (status == null) {
						status = "";
					}
					if (jsonMessage != null) {
						message = jsonMessage;
					}

					if (status.equalsIgnoreCase("200")) {
						//---------------------------
						// 성공
						//---------------------------
						JSONObject resultJson = responseJson.getJSONObject("result");
						String fileName = resultJson.getString("file_name");
						if (mPhotoSelectMode.equalsIgnoreCase("picture")) {
							UserData.setUserPicture(fileName);
						} else {
							UserData.setUserBackground(fileName);
						}

						// 사진 정보 DB 업데이트 시작
						mUpdateTask = new UpdateTask();
						mUpdateTask.execute("");
					} else {
						//---------------------------
						// 실패
						//---------------------------
						Util.confirm(mContext, title, message, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
								finish();
							}
						});
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			mUploadTask = null;
			cancel(true);
			dialog.dismiss();
		}
	}
	*/

	/*
	 * 사진정보 업데이트
	 */
	/*
	class UpdateTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String response = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(Config.URL_USER_UPDATE);

				//Log.d(Config.TAG, ShareData.value());

				// 전송될 데이터 값
				List<NameValuePair> data = new ArrayList<NameValuePair>(3);
				data.add(new BasicNameValuePair("id", UserData.getUserId()));
				data.add(new BasicNameValuePair("action", mPhotoSelectMode));
				data.add(new BasicNameValuePair("picture", UserData.getUserPicture()));
				data.add(new BasicNameValuePair("background", UserData.getUserBackground()));

				UrlEncodedFormEntity ent = new UrlEncodedFormEntity(data, HTTP.UTF_8);
				post.setEntity(ent);
				HttpResponse responsePost = client.execute(post);
				HttpEntity resEntity = responsePost.getEntity();

				if (resEntity == null) {
					//Log.d(Config.TAG, ">> resEntity is NULL.");
				} else {
					response = EntityUtils.toString(resEntity);
					//Log.d(Config.TAG, ">> PhotoUpdateTask(): " + response);
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
			mUpdateTask = null;

			String title = getString(R.string.error);
			String message = getString(R.string.try_again_later);

			if (result == null) {
				Util.confirm(mContext, title, message, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
						finish();
					}
				});
			} else {
				JSONObject responseJson = null;

				try {
					responseJson = new JSONObject(result);
					String status = responseJson.getString("status");
					String jsonMessage = responseJson.getString("message");

					if (status == null) {
						status = "";
					}
					if (jsonMessage != null) {
						message = jsonMessage;
					}

					if (status.equalsIgnoreCase("200")) {
						//--------
						// 성공
						//--------
						if (mPhotoSelectMode == "picture") {
							loadPhoto(UserData.getUserPicture(), mIvPicture, mLoLoadingPicture);
						} else {
							loadPhoto(UserData.getUserBackground(), mIvBackground, mLoLoadingBackground);
						}
					} else {
						//--------
						// 실패
						/---------
						Util.confirm(mContext, title, message, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
								finish();
							}
						});
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onCancelled() {
			mUpdateTask = null;
		}
	}
	*/
}
