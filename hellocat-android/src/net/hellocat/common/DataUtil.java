package net.hellocat.common;

import java.util.HashMap;
import java.util.Map;

import net.hellocat.util.CircleTransform;
import net.hellocat.util.Util;
import net.hellocat.util.VolleyListener;
import net.hellocat.util.VolleyManager;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.summertaker.cat.R;
import com.summertaker.cat.R.drawable;
import com.summertaker.cat.R.id;
import com.summertaker.cat.R.layout;
import com.summertaker.cat.R.string;

public class DataUtil {

	/*
	 * 사용자 사진 전체 URL 리턴하기
	 */
	public static String getPictureUrl(String url) {
		if (url != null && !url.isEmpty()) {
			String domain = url.substring(0, 4).equalsIgnoreCase("http") ? "" : Config.SERVER_DOMAIN;
			url = domain + url;
		}
		return url;
	}

	/*
	 * 사용자 사진 출력하기
	 */
	public static void setUserPicture(Context context, ImageView imageView, String imageUrl, boolean circleMode) {
		if (imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
			if (circleMode) {
				Picasso.with(context).load(imageUrl).transform(new CircleTransform())
						.placeholder(R.drawable.user_picture_round).into(imageView);
			} else {
				Picasso.with(context).load(imageUrl).placeholder(R.drawable.user_picture).into(imageView);
			}
		}
	}

	/*
	 * 신고하기
	 */
	public static void doReport(final Context context, final VolleyManager volleyManager, final String dataName,
			final String dataId) {
		final Resources r = context.getResources();

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(r.getString(R.string.report));

		LayoutInflater inflater = LayoutInflater.from(context);
		View dialogview = inflater.inflate(R.layout.dialog_edittext, null);
		alertDialog.setView(dialogview);

		final EditText etContent = (EditText) dialogview.findViewById(R.id.etContent);
		etContent.setHint(r.getString(R.string.report_hint));

		alertDialog.setPositiveButton(r.getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String content = etContent.getText().toString();
				Util.hideKeyboard(context, etContent);
				dialog.cancel();
				
				if (content.length() == 0) {
					Util.alert(context, r.getString(R.string.report), r.getString(R.string.report_hint), null);
				} else {
					Map<String, String> params = new HashMap<String, String>();
					params.put("user_id", User.getId());
					params.put("data_name", dataName);
					params.put("data_id", dataId);
					params.put("content", content);
					params.put("insert_date", Util.getDateTimeString());

					volleyManager.request(Config.URL_REPORT, params, true);

					volleyManager.listener = new VolleyListener() {
						@Override
						public void onSuccess(String response) throws JSONException {
							Util.alert(context, r.getString(R.string.report),
									r.getString(R.string.report_is_registered), null);
						}

						@Override
						public void onError(String response, String message) {
							//Log.i(">>response", response);
						}
					};
				}
			}
		});
		alertDialog.setNegativeButton(r.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Util.hideKeyboard(context, etContent);
				dialog.cancel();
			}
		});
		alertDialog.show();
		Util.showKeyboard(context);
	}
}
