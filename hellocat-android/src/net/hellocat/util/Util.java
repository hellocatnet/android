package net.hellocat.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.hellocat.common.Config;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.summertaker.cat.R;


public class Util {

	private static final int MESSAGE_ALERT = 1;
	private static final int CONFIRM_ALERT = 2;

	/*
	 * 경고 다이얼로그 표시
	 */
	public static void alert(Context ctx, String title, String message, DialogInterface.OnClickListener callback) {
		showAlertDialog(MESSAGE_ALERT, ctx, title, message, callback, ctx.getResources().getString(R.string.confirm));
	}

	/*
	 * 확인 다이얼로그 표시
	 */
	public static void confirm(Context ctx, String title, String message, DialogInterface.OnClickListener callback,
			String... buttonNames) {
		showAlertDialog(CONFIRM_ALERT, ctx, title, message, callback, buttonNames);
	}

	/*
	 * 에러 다이얼로그 표시
	 */
	public static void error(Context ctx, Exception exception, String message, DialogInterface.OnClickListener callback) {
		if (message == null) {
			message = ctx.getResources().getString(R.string.error_occured);
			if (exception.getCause() != null) {
				message = message + "\n" + exception.getLocalizedMessage();
			}
		}
		showAlertDialog(MESSAGE_ALERT, ctx, null, message, callback, ctx.getResources().getString(R.string.confirm));
	}

	/*
	 * 다이얼로그 표시
	 */
	public static void showAlertDialog(int alertType, Context ctx, String title, String message,
			DialogInterface.OnClickListener callback, String... buttonNames) {
		if (title == null) {
			title = ctx.getResources().getString(R.string.app_name);
		}
		if (message == null) {
			message = ctx.getResources().getString(R.string.try_again_later);
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setTitle(title).setMessage(message);

		switch (alertType) {

		case MESSAGE_ALERT:
			builder.setCancelable(false); // false = pressing back button won't dismiss this alert
			if (callback != null) {
				builder.setPositiveButton(buttonNames[0], callback);
			} else {
				builder.setPositiveButton(buttonNames[0], dismissDialog);
			}
			break;

		case CONFIRM_ALERT:
			String yes = ctx.getResources().getString(R.string.yes);
			String no = ctx.getResources().getString(R.string.no);
			if (buttonNames != null) {
				if (buttonNames.length > 0) {
					yes = buttonNames[0];
				}
				if (buttonNames.length > 1) {
					no = buttonNames[1];
				}
			}
			builder.setPositiveButton(yes, callback);
			builder.setNegativeButton(no, dismissDialog);
			/*
			builder.setOnKeyListener(new Dialog.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						dialogInterface.dismiss();
					}
					return true;
				}
			});
			*/
			break;
		}
		builder.create().show();
	}

	public static DialogInterface.OnClickListener dismissDialog = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			dialogInterface.dismiss();
		}
	};

	/*
	 * 문자열 합치기
	 */
	public static String join(String delimiter, String... data) {
		StringBuilder sb = new StringBuilder();
		if (data.length > 0) {
			if (data[0] != null && !data[0].isEmpty()) {
				sb.append(data[0]);
			}
			for (int i = 1; i < data.length; i++) {
				if (data[i] != null && !data[i].isEmpty()) {
					sb.append(delimiter);
					sb.append(data[i]);
				}
			}
		}
		return sb.toString();
	}

	/*
	 * 문자열 합치기
	 */
	static public String join(List<String> list, String conjunction) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (item == null || item.isEmpty()) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(conjunction);
			}
			sb.append(item);
		}
		return sb.toString();
	}

	/*
	 * 서버에 저장할 문자열 리턴하기 
	 */
	public static String getTextToDb(String s) {
		return s.replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\\s+", " ");
	}

	public static String getDbToText(String s) {
		return s.replaceAll("&amp;", "&").replaceAll("&quot;", "\"").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
	}

	/*
	 * 현재 시간 문자열 리턴하기
	 */
	public static String getDateTimeString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		return sdf.format(new Date());
	}

	//1 minute = 60 seconds
	//1 hour = 60 x 60 = 3600
	//1 day = 3600 x 24 = 86400
	public static String getEllapsedTime(String dateTimeString) {
		String result = "";

		if (dateTimeString == null) {
			return result;
		}

		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		Date startDate = null;

		try {
			startDate = sdf1.parse(dateTimeString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (startDate != null) {
			//milliseconds
			Date endDate = new Date();
			long different = endDate.getTime() - startDate.getTime();

			//System.out.println("startDate : " + startDate);
			//System.out.println("endDate : " + endDate);
			//System.out.println("different : " + different);

			long secondsInMilli = 1000;
			long minutesInMilli = secondsInMilli * 60;
			long hoursInMilli = minutesInMilli * 60;
			long daysInMilli = hoursInMilli * 24;

			long elapsedDays = different / daysInMilli;
			different = different % daysInMilli;

			long elapsedHours = different / hoursInMilli;
			different = different % hoursInMilli;

			long elapsedMinutes = different / minutesInMilli;
			different = different % minutesInMilli;

			long elapsedSeconds = different / secondsInMilli;

			//List<String> list = new ArrayList<String>();

			if (elapsedDays > 0) {
				//result = elapsedDays + "일 전";
				result = dateTimeString;
			} else {
				if (elapsedHours > 0) {
					if (elapsedMinutes > 0) {
						result = elapsedHours + "시간 " + elapsedMinutes + "분";
					} else {
						result = elapsedHours + "시간";
					}
				} else {
					if (elapsedMinutes > 0) {
						result = elapsedMinutes + "분";
					} else {
						if (elapsedSeconds > 0) {
							result = elapsedSeconds + "초";
						} else {
							result = "조금";
						}
					}
				}

				result = result + " 전";
			}
		}

		return result;
	}

	/*
	 * 작업 중 안내 메세지 표시하기
	 */
	public static void showOnProecssingAlert(Context context) {
		Toast.makeText(context, context.getResources().getString(R.string.on_processing_please_wait),
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * 토스트 메시지 표시하기
	 */
	public static void toast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	/*
	 * 키보드 보이기
	 */
	public static void showKeyboard(Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	/*
	 * 키보드 숨기기
	 */
	public static void hideKeyboard(Context context) {
		View target = ((Activity) context).getCurrentFocus();
		if (target != null) {
			IBinder ibinder = ((Activity) context).getCurrentFocus().getWindowToken();
			if (ibinder != null) {
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(ibinder, 0);
			}
		}
	}

	public static void hideKeyboard(Context context, View view) {
		InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/*
	 * MD5 암호화하기
	 */
	public static final String md5(final String string) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(string.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	/*
	 * 이미지 회전시키기
	 */
	public static void rotateImageView(ImageView imageView, boolean rotate) {
		if (imageView == null) {
			return;
		}

		if (rotate) {
			RotateAnimation animation = new RotateAnimation(0.0f, 350f, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
					RotateAnimation.RELATIVE_TO_SELF, 0.5f);

			animation.setInterpolator(new LinearInterpolator());
			animation.setRepeatCount(Animation.INFINITE);
			animation.setDuration(1200);
			imageView.startAnimation(animation);
		} else {
			imageView.setAnimation(null);
		}
	}

	/*
	 * JSON 객체에서 필드에 해당하는 값 리턴하기
	 */
	public static String getString(JSONObject json, String field) {
		String value = "";
		try {
			value = json.getString(field);
			if (value == null || "null".equals(value)) {
				value = "";
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return value;
	}

	/*
	 * 0으로 채우기
	 */
	public static String zeroFill(String format, String value) {
		if (value == null || value.isEmpty()) {
			return format;
		} else {
			return zeroFill(format, Integer.parseInt(value));
		}
	}

	public static String zeroFill(String format, int value) {
		// format = "00";
		DecimalFormat df = new DecimalFormat(format);
		return df.format(value);
	}

	/*
	 * 파라미터 디버깅
	 */
	public static void debug(Map<String, String> params) {
		if (params == null) {
			Log.i(">Util.debugParams()", "params is null.");
		} else {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				String value = (entry.getValue() == null) ? "" : entry.getValue();
				Log.i(">" + entry.getKey(), value);
			}
		}
	}

	/*
	 * 버튼 비활성화시키기
	 */
	public static void disableButton(Context context, Button button) {
		disableButton(context, button, null);
	}

	public static void disableButton(Context context, Button button, String buttonText) {
		Resources r = context.getResources();
		if (buttonText == null) {
			buttonText = r.getString(R.string.progressing);
		}
		button.setText(buttonText);
		button.setTextColor(r.getColor(R.color.gray));
		button.setShadowLayer(0, 0, 0, r.getColor(R.color.transparent));
		button.setOnClickListener(null);
		button.setBackgroundResource(R.drawable.button_disabled);
	}

	/*
	 * Bitmap 이미지에 경계선 그려서 리턴하기
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color, int cornerDips, int borderDips,
			Context context) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) borderDips,
				context.getResources().getDisplayMetrics());
		final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) cornerDips,
				context.getResources().getDisplayMetrics());
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		// prepare canvas for transfer
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

		// draw bitmap
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		// draw border
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth((float) borderSizePx);
		canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

		return output;
	}

	/**
	 * Take screenshot of the activity including the action bar
	 * 
	 * @param activity
	 * @return The screenshot of the activity including the action bar
	 */
	public static Bitmap takeScreenshot(Activity activity) {
		ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
		ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
		decorChild.setDrawingCacheEnabled(true);
		decorChild.buildDrawingCache();
		Bitmap drawingCache = decorChild.getDrawingCache(true);
		Bitmap bitmap = Bitmap.createBitmap(drawingCache);
		decorChild.setDrawingCacheEnabled(false);
		return bitmap;
	}

	/**
	 * Print hash key
	 */
	public static void printKeyHash(Context context) {
		try {
			String TAG = Config.PACKAGE_NAME;
			PackageInfo info = context.getPackageManager().getPackageInfo(TAG, PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
				Log.e(TAG, "Key Hash: " + keyHash);
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
	}

	/**
	 * Update language
	 * 
	 * @param code
	 *            The language code. Like: en, cz, iw, ...
	 */
	public static void updateLanguage(Context context, String code) {
		Locale locale = new Locale(code);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
	}

	public static int pixelToDp(Context context, int pixel) {
		int density = (int) context.getResources().getDisplayMetrics().density;
		return pixel / density;
	}
}
