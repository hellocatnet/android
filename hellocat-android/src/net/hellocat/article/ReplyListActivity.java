package net.hellocat.article;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.hellocat.common.Config;
import net.hellocat.common.DataUtil;
import net.hellocat.common.User;
import net.hellocat.util.Util;
import net.hellocat.util.VolleyListener;
import net.hellocat.util.VolleyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.summertaker.cat.R;

public class ReplyListActivity extends Activity {

	private Context mContext;
	private VolleyManager mVolleyManager;

	private ArrayList<ReplyListItem> mItemList;
	private ReplyListAdapter mListAdapter;
	private ListView mListView;
	ReplyListItem mItem;
	int mPosition;

	PullToRefreshListView mPullRefreshListView;

	TextView mTvHelp;

	AlertDialog mMenuDialog;
	AlertDialog mMyMenuDialog;

	private EditText mEtContent;
	private Button mBtnSend;

	boolean mIsOnProcessing = false;

	String mDataName;
	String mDataId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_reply_list);

		mContext = ReplyListActivity.this;
		mVolleyManager = new VolleyManager(mContext, null);

		// 파라미터
		Bundle bundle = getIntent().getExtras();
		mDataName = bundle.getString("dataName");
		mDataId = bundle.getString("dataId");

		// 리뷰 상세
		String picture = bundle.getString("picture");
		ImageView ivArticleUserPicture = (ImageView) findViewById(R.id.ivArticleUserPicture);
		DataUtil.setUserPicture(this, ivArticleUserPicture, picture, false);

		String name = bundle.getString("name");
		TextView tvArticleUserName = (TextView) findViewById(R.id.tvArticleUserName);
		tvArticleUserName.setText(name);

		String date = bundle.getString("date");
		TextView tvArticleDate = (TextView) findViewById(R.id.tvArticleDate);
		tvArticleDate.setText(Util.getEllapsedTime(date));

		String content = bundle.getString("content");
		TextView tvArticleContent = (TextView) findViewById(R.id.tvArticleContent);
		tvArticleContent.setText(content);

		// 도움말
		mTvHelp = (TextView) findViewById(R.id.tvReplyListHelp);

		// 리스트 데이터 설정
		mItemList = new ArrayList<ReplyListItem>();

		// 리스트 어답터 설정
		mListAdapter = new ReplyListAdapter(mContext, mItemList);

		// PULL-TO-REFRESH 설정
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.lvReply);

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				doLoad();
			}
		});

		// Add an end-of-list listener
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				Toast.makeText(ReplyListActivity.this, "End of List!", Toast.LENGTH_SHORT).show();
			}
		});

		// 리스트 뷰 설정
		mListView = mPullRefreshListView.getRefreshableView();
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(">>position", position + "");
				mPosition = position;
				mItem = (ReplyListItem) parent.getAdapter().getItem(position);

				if (mItem.getUserId().equalsIgnoreCase(User.getId())) {
					mMyMenuDialog.show(); // 본인이 작성한 글인 경우
				} else {
					mMenuDialog.show(); // 타인의 글인 경우
				}
			}
		});

		// 리스트 뷰 아이템 클릭 시 팝업 메뉴
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.replies);
		builder.setItems(R.array.reply_list_menu_arrays, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				// 신고하기
				case 0:
					doReport();
					break;
				}
			}
		});
		mMenuDialog = builder.create();

		// 리스트 뷰 아이템 클릭 시 팝업 메뉴
		AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
		builder2.setTitle(R.string.replies);
		builder2.setItems(R.array.reply_list_my_menu_arrays, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				// 수정하기
				case 0:
					doModify();
					break;

				// 삭제하기
				case 1:
					doDelete();
					break;
				}
			}
		});
		mMyMenuDialog = builder2.create();

		// 데이터 입력 폼
		mEtContent = (EditText) findViewById(R.id.etReplyListContent);

		// 데이터 전송 버튼
		mBtnSend = (Button) findViewById(R.id.btReplyListSubmit);
		mBtnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = mEtContent.getText().toString();
				if (content.length() > 0) {
					//setProgressBarIndeterminateVisibility(Boolean.TRUE);

					Util.hideKeyboard(mContext);
					doSave(null, null);
				}
			}
		});

		doLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reply, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			doFinish();
			return true;
		case R.id.action_navigation_back:
			doFinish();
			return true;
		case R.id.action_refresh:
			refreshData();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			doFinish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/*
	 * 목록 로드하기
	 */
	private void doLoad() {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}
		mIsOnProcessing = true;
		setProgressBarIndeterminateVisibility(Boolean.TRUE);

		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", User.getId());
		params.put("data_name", mDataName);
		params.put("data_id", mDataId);

		//Util.debug(params);

		mVolleyManager.request(Config.URL_REPLY_LIST, params, false);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {
				//Log.i(Config.TAG, response);
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);

				mItemList.clear();
				mListAdapter.notifyDataSetChanged();

				JSONObject json = new JSONObject(response);
				JSONArray array = json.getJSONArray("result");

				if (array.length() == 0) {
					// 데이터가 없는 경우
					mTvHelp.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.GONE);

					Util.showKeyboard(mContext);
				} else {
					// 데이터가 존재하는 경우
					mTvHelp.setVisibility(View.GONE);
					mListView.setVisibility(View.VISIBLE);

					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonObject = (JSONObject) array.getJSONObject(i);
						ReplyListItem item = createItem(jsonObject);
						mItemList.add(item);
					}
					mListAdapter.notifyDataSetChanged();					
				}
				mPullRefreshListView.onRefreshComplete();
			}

			@Override
			public void onError(String response, String message) {
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);
			}
		};
	}

	/*
	 * 데이터 항목 만들기
	 */
	private ReplyListItem createItem(JSONObject jsonObject) {
		ReplyListItem item = new ReplyListItem();

		try {
			item.setId(jsonObject.getString("id"));
			item.setUserId(jsonObject.getString("user_id"));
			item.setUserName(jsonObject.getString("user_name"));
			item.setUserEmail(jsonObject.getString("user_email"));
			item.setUserPicture(jsonObject.getString("user_picture"));
			item.setDate(jsonObject.getString("insert_date"));
			item.setContent(jsonObject.getString("content"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return item;
	}

	/*
	 * 새로고침
	 */
	private void refreshData() {
		doLoad();
	}

	// 저장하기
	private void doSave(String replyId, String content) {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}

		final String modifyId = replyId;

		if (content == null) {
			content = mEtContent.getText().toString();
		}

		if (content.length() == 0) {
			Util.alert(mContext, getString(R.string.replies), getString(R.string.input_content), null);
			return;
		}

		mIsOnProcessing = true;
		setProgressBarIndeterminateVisibility(Boolean.TRUE);

		Map<String, String> params = new HashMap<String, String>();
		params.put("reply_id", replyId);
		params.put("user_id", User.getId());
		params.put("data_name", mDataName);
		params.put("data_id", mDataId);
		params.put("content", Util.getTextToDb(content));
		params.put("insert_date", Util.getDateTimeString());

		mVolleyManager.request(Config.URL_REPLY_SAVE, params, false);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {
				//Log.i(Config.TAG, response);
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);

				JSONObject json = new JSONObject(response);
				JSONObject result = new JSONObject(json.getString("result"));

				mTvHelp.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);

				ReplyListItem item = createItem(result);

				if (modifyId == null) {
					// 등록하기
					mItemList.add(0, item);
					mListAdapter.notifyDataSetChanged();
					mListView.setSelection(0);
					mListView.setSelectionAfterHeaderView();
				} else {
					// 수정하기
					findItem(modifyId);
					mItemList.set(mPosition, item);
					mListAdapter.notifyDataSetChanged();
				}

				mEtContent.setText("");
			}

			@Override
			public void onError(String response, String message) {
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);
			}
		};
	}

	/*
	 * 수정하기
	 */
	private void doModify() {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle(getString(R.string.modify_long));

		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogview = inflater.inflate(R.layout.dialog_edittext, null);
		alertDialog.setView(dialogview);

		final EditText etContent = (EditText) dialogview.findViewById(R.id.etContent);
		etContent.setText(mItem.getContent());

		alertDialog.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String content = etContent.getText().toString();
				Util.hideKeyboard(mContext, etContent);
				dialog.cancel();

				doSave(mItem.getId(), content);
			}
		});
		alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Util.hideKeyboard(mContext, etContent);
				dialog.cancel();
			}
		});
		alertDialog.show();
		Util.showKeyboard(mContext);
	}

	/*
	 * 삭제하기
	 */
	private void doDelete() {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}

		Util.confirm(mContext, getString(R.string.delete), getString(R.string.sure_to_delete),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						mIsOnProcessing = true;
						setProgressBarIndeterminateVisibility(Boolean.TRUE);

						Map<String, String> params = new HashMap<String, String>();
						params.put("user_id", User.getId());
						params.put("reply_id", mItem.getId());

						Util.debug(params);

						mVolleyManager.request(Config.URL_REPLY_DELETE, params, false);

						mVolleyManager.listener = new VolleyListener() {
							@Override
							public void onSuccess(String response) throws JSONException {
								Log.i(">>response", response);

								mIsOnProcessing = false;
								setProgressBarIndeterminateVisibility(Boolean.FALSE);

								JSONObject json = new JSONObject(response);
								JSONObject result = new JSONObject(json.getString("result"));

								findItem(result.getString("reply_id"));

								mItemList.remove(mPosition);
								//mItemList.remove(mItem);
								mListAdapter.notifyDataSetChanged();
								
								if (mItemList.size() == 0) {
									mTvHelp.setVisibility(View.VISIBLE);
									mListView.setVisibility(View.GONE);
								}
							}

							@Override
							public void onError(String response, String message) {
								mIsOnProcessing = false;
								setProgressBarIndeterminateVisibility(Boolean.FALSE);
							}
						};
					}
				});
	}

	/*
	 * 신고하기
	 */
	private void doReport() {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}

		DataUtil.doReport(mContext, mVolleyManager, "reply", mItem.getId());
	}

	/*
	 * 주어진 "id" 값으로 목록에서 선택된 데이터 찾기
	 */
	private ReplyListItem findItem(String id) {
		ReplyListItem item = null;

		for (int i = 0; i < mItemList.size(); i++) {
			item = (ReplyListItem) mItemList.get(i);
			if (id.equalsIgnoreCase(item.getId())) {
				mItem = item;
				mPosition = i;
				//Log.d(Config.TAG, "content: " + data.getContent());
				break;
			}
		}

		return item;
	}

	/*
	 * 종료하기
	 */
	private void doFinish() {
		Util.hideKeyboard(mContext);

		Intent intent = new Intent();
		intent.putExtra("articleId", mDataId);
		intent.putExtra("replyCount", mItemList.size() + "");

		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}
