package net.hellocat.article;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.hellocat.common.Config;
import net.hellocat.common.DataUtil;
import net.hellocat.common.User;
import net.hellocat.setting.SettingActivity;
import net.hellocat.util.Util;
import net.hellocat.util.VolleyListener;
import net.hellocat.util.VolleyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.summertaker.cat.R;

public class ArticleListActivity extends Activity {

	private Context mContext;
	private ActionBar mActionBar;
	private VolleyManager mVolleyManager;

	String mActionBarTitle;

	private ArrayList<ArticleListItem> mItemList;
	private ArticleListAdapter mListAdapter;
	private ListView mListView;
	ArticleListItem mItem;
	int mPosition;

	int mTotal = -1;
	int mPage = 1;
	boolean IS_PAGING_MODE = true;
	
	String mBaseId; // 페이징 기준이 되는 데이터 아이디

	PullToRefreshListView mPullRefreshListView;

	AlertDialog mMenuDialog;
	AlertDialog mMyMenuDialog;

	TextView mTvArtcleListHelp;
	LinearLayout mLoArticleListInfo;
	TextView mTvArticleListInfoValue;
	EditText mEtContent;
	Button mBtnSend;

	boolean mIsOnProcessing = false;
	boolean mIsVehicleSearchCanceled = false;

	final int REQUEST_CODE_REPLY = 200;
	final int REQUEST_CODE_SETTING = 900;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_article_list);

		mContext = ArticleListActivity.this;
		mActionBar = getActionBar();

		mVolleyManager = new VolleyManager(mContext, null);

		// 안내 메세지
		mTvArtcleListHelp = (TextView) findViewById(R.id.tvArticleListHelp);
		
		// 리스트 정보
		mLoArticleListInfo = (LinearLayout) findViewById(R.id.loArticleListInfo);
		mTvArticleListInfoValue = (TextView) findViewById(R.id.tvArticleListInfoValue);

		// 리스트 데이터 설정
		mItemList = new ArrayList<ArticleListItem>();

		// 리스트 어답터 설정
		mListAdapter = new ArticleListAdapter(mContext, mItemList);
		mListAdapter.listener = new ArticleListListener() {
			@Override
			public void likeOnClick(int position) { // 좋아요
				doVote("like", position);
			}

			@Override
			public void dislikeOnClick(int position) { // 싫어요
				doVote("dislike", position);
			}

			@Override
			public void replyOnClick(int position) { // 답글
				doReply(position);
			}

			@Override
			public void moreOnClick(int position) { // 기능 더 보기
				mPosition = position;
				mItem = (ArticleListItem) mListAdapter.getItem(position);

				if (mItem.getUserId().equalsIgnoreCase(User.getId())) {
					mMyMenuDialog.show(); // 본인이 작성한 글인 경우
				} else {
					mMenuDialog.show(); // 타인의 글인 경우
				}
			}
		};

		// PULL-TO-REFRESH 설정
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.lvArticleList);

		// 새로 고침 - 최신 데이터 불러오기
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);				
				loadNew();
			}
		});

		// 더 보기 - 이전 데이터 불러오기
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				loadMore();
			}
		});

		// 리스트 뷰 설정
		mListView = mPullRefreshListView.getRefreshableView();
		mListView.setAdapter(mListAdapter);

		// 리스트 뷰 아이템 클릭 시 팝업 메뉴
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.app_name);
		builder.setItems(R.array.article_list_menu_arrays, new DialogInterface.OnClickListener() {
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
		builder2.setTitle(R.string.app_name);
		builder2.setItems(R.array.article_list_my_menu_arrays, new DialogInterface.OnClickListener() {
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
		mEtContent = (EditText) findViewById(R.id.etArticleListContent);

		// 데이터 전송 버튼
		mBtnSend = (Button) findViewById(R.id.btArticleListSubmit);
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

		setUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.article, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_refresh:
			loadNew();
			return true;
		case R.id.action_settings:
			goActivity(SettingActivity.class, REQUEST_CODE_SETTING);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_REPLY) {
			if (resultCode == Activity.RESULT_OK) {
				//--------------------------
				// 답글 갯수 업데이트하기
				//--------------------------
				String articleId = data.getStringExtra("articleId");
				String replyCount = data.getStringExtra("replyCount");
				//Log.i(">>", "articleId: " + articleId + ", replyCount: " + replyCount);

				findItem(articleId);

				mItem.setReplyCount(replyCount);
				mItemList.set(mPosition, mItem);

				mListAdapter.notifyDataSetChanged();
				mPullRefreshListView.onRefreshComplete();
			}
		}
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	private void setUI() {
		//mActionBarTitle = Vehicle.getName();
		mActionBarTitle = getString(R.string.app_name);

		doLoad();
	}

	/*
	 * 목록 로드하기
	 */
	private void doLoad() {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}

		//-------------------------------------------------------------------
		// 페이징 모드일 때 모든 데이터를 다 가져왔으면 실행하지 않는다.
		//-------------------------------------------------------------------
		if (IS_PAGING_MODE && mTotal > 0) {
			if (mItemList.size() >= mTotal) {
				return;
			}
		}

		mIsOnProcessing = true;
		setProgressBarIndeterminateVisibility(Boolean.TRUE);

		String action = IS_PAGING_MODE ? "paging" : "";

		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", User.getId());
		params.put("action", action);
		params.put("base_id", mBaseId);
		params.put("page", mPage + "");

		Util.debug(params);

		mVolleyManager.request(Config.URL_ARTICLE_LIST, params, false);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {
				//Log.i(Config.TAG, response);

				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);

				//mItemList.clear();
				//mListAdapter.notifyDataSetChanged();

				JSONObject json = new JSONObject(response);

				String action = json.getString("action");
				String total = json.getString("total");

				JSONArray array = json.getJSONArray("result");
				if (array.length() == 0) {
					//----------------------
					// 데이터가 없는 경우
					//----------------------
					if ("paging".equalsIgnoreCase(action) && mPage == 1) {
						mTvArtcleListHelp.setVisibility(View.VISIBLE);
						mLoArticleListInfo.setVisibility(View.GONE);
						mListView.setVisibility(View.GONE);

						//Util.showKeyboard(mContext);
					}
				} else {
					//--------------------------
					// 데이터가 존재하는 경우
					//--------------------------
					mTvArtcleListHelp.setVisibility(View.GONE);
					mLoArticleListInfo.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.VISIBLE);

					for (int i = 0; i < array.length(); i++) {
						JSONObject jsonObject = (JSONObject) array.getJSONObject(i);
						ArticleListItem item = createItem(jsonObject);

						if (mBaseId == null) {
							mBaseId = item.getId();
						}

						if ("paging".equalsIgnoreCase(action)) {
							mItemList.add(item);
						} else {
							// 신규 데이터를 추가하는 경우
							mItemList.add(0, item);
							mTotal++;
						}
					}
					mListAdapter.notifyDataSetChanged();
				}

				mPullRefreshListView.onRefreshComplete();

				//----------------------
				// 액션바 제목 설정
				//----------------------
				if ("paging".equalsIgnoreCase(action)) {
					mTotal = Integer.parseInt(total);
					if (mItemList.size() < mTotal) {
						mPage++;
					}
				}

				if (mItemList.size() > 0) {
					DecimalFormat df = new DecimalFormat("###,###,###");
					//mActionBar.setTitle(mActionBarTitle + " (" + mItemList.size() + " / " + df.format(mTotal) + ")");
					mTvArticleListInfoValue.setText(mItemList.size() + " / " + df.format(mTotal));
				}
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
	private ArticleListItem createItem(JSONObject json) {
		ArticleListItem item = new ArticleListItem();

		try {
			item.setId(json.getString("id"));
			item.setUserId(json.getString("user_id"));
			item.setUserName(json.getString("user_name"));
			item.setUserEmail(json.getString("user_email"));
			item.setUserPicture(json.getString("user_picture"));
			item.setDate(json.getString("insert_date"));
			item.setContent(Util.getDbToText(json.getString("content")));
			item.setDeleted(json.getString("deleted"));
			item.setLikeCount(json.getString("like_count"));
			item.setMyLikeCount(json.getString("my_like_count"));
			item.setDislikeCount(json.getString("dislike_count"));
			item.setMyDislikeCount(json.getString("my_dislike_count"));
			item.setReplyCount(json.getString("reply_count"));
			item.setMyReplyCount(json.getString("my_reply_count"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return item;
	}

	/*
	 * 새로고침
	 */
	private void loadNew() {
		IS_PAGING_MODE = false;
		doLoad();
	}

	private void loadMore() {
		IS_PAGING_MODE = true;
		doLoad();
	}

	// 저장하기
	private void doSave(String articleId, String content) {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}

		final String modifyId = articleId;

		if (content == null) {
			content = mEtContent.getText().toString();
		}

		if (content.length() == 0) {
			Util.alert(mContext, getString(R.string.app_name), getString(R.string.input_content), null);
			return;
		}

		mIsOnProcessing = true;
		setProgressBarIndeterminateVisibility(Boolean.TRUE);

		Map<String, String> params = new HashMap<String, String>();
		params.put("article_id", articleId);
		params.put("user_id", User.getId());
		params.put("content", Util.getTextToDb(content));
		params.put("insert_date", Util.getDateTimeString());

		mVolleyManager.request(Config.URL_ARTICLE_SAVE, params, false);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {
				//Log.i(Config.TAG, response);
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);

				mTvArtcleListHelp.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);

				JSONObject json = new JSONObject(response);
				JSONObject result = new JSONObject(json.getString("result"));

				ArticleListItem item = createItem(result);

				if (modifyId == null) {
					// 등록하기
					mItemList.add(0, item);
					mListAdapter.notifyDataSetChanged();
					mListView.setSelection(0);
					mListView.setSelectionAfterHeaderView();
					
					mBaseId = item.getId(); // 리스트 시작 아이디
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
	 * 투표하기
	 */
	private void doVote(String action, int position) {
		if (mIsOnProcessing) {
			Util.showOnProecssingAlert(mContext);
			return;
		}
		mIsOnProcessing = true;
		setProgressBarIndeterminateVisibility(Boolean.TRUE);

		mPosition = position;
		mItem = (ArticleListItem) mListAdapter.getItem(mPosition);

		Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", User.getId());
		params.put("data_name", "article");
		params.put("data_id", mItem.getId());
		params.put("action", action);

		mVolleyManager.request(Config.URL_VOTE, params, false);

		mVolleyManager.listener = new VolleyListener() {
			@Override
			public void onSuccess(String response) throws JSONException {
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);

				JSONObject json = new JSONObject(response);
				JSONObject result = new JSONObject(json.getString("result"));

				findItem(result.getString("id"));

				String likeCount = result.getString("like_count");
				String myLikeCount = result.getString("my_like_count");
				String dislikeCount = result.getString("dislike_count");
				String myDislikeCount = result.getString("my_dislike_count");

				mItem.setLikeCount(likeCount);
				mItem.setMyLikeCount(myLikeCount);
				mItem.setDislikeCount(dislikeCount);
				mItem.setMyDislikeCount(myDislikeCount);

				mItemList.set(mPosition, mItem);
				mListAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(String response, String message) {
				mIsOnProcessing = false;
				setProgressBarIndeterminateVisibility(Boolean.FALSE);
			}
		};
	}

	/*
	 * 답글쓰기
	 */
	private void doReply(int position) {
		mPosition = position;
		mItem = (ArticleListItem) mListAdapter.getItem(mPosition);

		Intent intent = new Intent(mContext, ReplyListActivity.class);
		intent.putExtra("dataName", "article");
		intent.putExtra("dataId", mItem.getId());

		intent.putExtra("name", mItem.getUserName());
		intent.putExtra("date", mItem.getDate());
		intent.putExtra("picture", mItem.getUserPicture());
		intent.putExtra("content", mItem.getContent());

		startActivityForResult(intent, REQUEST_CODE_REPLY);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

		Util.confirm(mContext, getString(R.string.delete), getString(R.string.sure_to_delete), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				mIsOnProcessing = true;
				setProgressBarIndeterminateVisibility(Boolean.TRUE);

				Map<String, String> params = new HashMap<String, String>();
				params.put("user_id", User.getId());
				params.put("article_id", mItem.getId());

				mVolleyManager.request(Config.URL_ARTICLE_DELETE, params, false);

				mVolleyManager.listener = new VolleyListener() {
					@Override
					public void onSuccess(String response) throws JSONException {
						mIsOnProcessing = false;
						setProgressBarIndeterminateVisibility(Boolean.FALSE);

						JSONObject json = new JSONObject(response);
						JSONObject result = new JSONObject(json.getString("result"));

						String articleId = result.getString("article_id");
						String deletedFlag = result.getString("deleted");

						findItem(articleId);
						mItemList.remove(mPosition);

						if ("y".equalsIgnoreCase(deletedFlag)) {
							//-------------------------------
							// 삭제되었음으로 표시된 경우
							//-------------------------------
							mItem.setDeleted("y");
							mItemList.add(mPosition, mItem);
						}

						mListAdapter.notifyDataSetChanged();

						if (mItemList.size() == 0) {
							mTvArtcleListHelp.setVisibility(View.VISIBLE);
							mListView.setVisibility(View.GONE);
						} else {
							//mItemList.clear();
							//mListAdapter.notifyDataSetChanged();
							
							//mBaseId = null;
							
							//loadNew();
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

		DataUtil.doReport(mContext, mVolleyManager, "article", mItem.getId());
	}

	/*
	 * 주어진 "id" 값으로 목록에서 선택된 데이터 찾기
	 */
	private ArticleListItem findItem(String id) {
		ArticleListItem item = null;

		for (int position = 0; position < mItemList.size(); position++) {
			item = (ArticleListItem) mItemList.get(position);
			String dataId = item.getId();
			if (id.equalsIgnoreCase(dataId)) {
				mItem = item;
				mPosition = position;
				//Log.d(Config.TAG, "content: " + data.getContent());
				break;
			}
		}

		return item;
	}

	/*
	 * 액티비티 이동
	 */
	private void goActivity(Class<?> cls, int requestCode) {
		Intent intent = new Intent(getApplicationContext(), cls);
		startActivityForResult(intent, requestCode);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
}
