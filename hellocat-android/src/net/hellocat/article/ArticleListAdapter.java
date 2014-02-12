package net.hellocat.article;

import java.util.ArrayList;

import net.hellocat.common.DataUtil;
import net.hellocat.util.Util;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.summertaker.cat.R;

public class ArticleListAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;
	private Resources mRes;
	private LayoutInflater mLayoutInflater;
	private ArrayList<ArticleListItem> mDataList = null;

	ArticleListListener listener;

	public ArticleListAdapter(Context context, ArrayList<ArticleListItem> dataList) {
		this.mContext = context;
		this.mRes = mContext.getResources();
		this.mLayoutInflater = LayoutInflater.from(context);
		this.mDataList = dataList;
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		ArticleListItem data = (ArticleListItem) mDataList.get(position);

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.article_list_item, null);

			holder = new ViewHolder();

			// 사진
			holder.ivUserPicture = (ImageView) convertView.findViewById(R.id.ivUserPicture);
			holder.ivUserPicture.setOnClickListener(this);

			holder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			holder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);

			// 좋아요
			holder.cmdLike = (LinearLayout) convertView.findViewById(R.id.cmdLike);
			holder.cmdLike.setOnClickListener(this);
			holder.tvLike = (TextView) convertView.findViewById(R.id.tvLike);

			// 싫어요
			holder.cmdDislike = (LinearLayout) convertView.findViewById(R.id.cmdDislike);
			holder.cmdDislike.setOnClickListener(this);
			holder.tvDislike = (TextView) convertView.findViewById(R.id.tvDislike);

			// 답글
			holder.cmdReply = (LinearLayout) convertView.findViewById(R.id.cmdReply);
			holder.cmdReply.setOnClickListener(this);
			holder.tvReply = (TextView) convertView.findViewById(R.id.tvReply);

			// 더 보기
			holder.cmdMore = (LinearLayout) convertView.findViewById(R.id.cmdMore);
			holder.cmdMore.setOnClickListener(this);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (data.getDeleted() == null || data.getDeleted().isEmpty()) {
			DataUtil.setUserPicture(mContext, holder.ivUserPicture, data.getUserPicture(), true);
			
			holder.tvUserName.setText(data.getUserName());
			holder.tvUserName.setTextColor(mRes.getColor(R.color.list_view_item_body_name));
			
			holder.tvContent.setText(data.getContent());
			holder.tvContent.setTextColor(mRes.getColor(R.color.list_view_item_body_content));
		} else {
			//----------------------------
			// 삭제된 글로 표시된 경우
			//----------------------------
			Picasso.with(mContext).load(R.drawable.user_picture_round).into(holder.ivUserPicture);			
			
			holder.tvUserName.setText(mRes.getString(R.string.deleted));
			holder.tvUserName.setTextColor(mRes.getColor(R.color.silver));
			
			holder.tvContent.setText(mRes.getString(R.string.this_is_a_deleted_article_by_writer));
			holder.tvContent.setTextColor(mRes.getColor(R.color.silver));
		}
		holder.tvDate.setText(Util.getEllapsedTime(data.getDate()));

		// 좋아요
		holder.cmdLike.setTag(position);
		holder.tvLike.setText(mRes.getString(R.string.like) + "(" + data.getLikeCount() + ")");
		//holder.tvLike.setText(data.getLikeCount());
		if (data.getMyLikeCount().equalsIgnoreCase("0")) {
			holder.tvLike.setTextColor(mRes.getColor(R.color.list_card_item_button_text_inactive)); // 투표하지 않은 경우
		} else {
			holder.tvLike.setTextColor(mRes.getColor(R.color.list_card_item_button_text_active)); // 투표한 경우
		}

		// 싫어요
		holder.cmdDislike.setTag(position);
		holder.tvDislike.setText(mRes.getString(R.string.dislike) + "(" + data.getDislikeCount() + ")");
		//holder.tvDislike.setText(data.getDislikeCount());
		if (data.getMyDislikeCount().equalsIgnoreCase("0")) {
			holder.tvDislike.setTextColor(mRes.getColor(R.color.list_card_item_button_text_inactive)); // 투표하지 않은 경우
		} else {
			holder.tvDislike.setTextColor(mRes.getColor(R.color.list_card_item_button_text_active)); // 투표한 경우
		}

		// 댓글
		holder.cmdReply.setTag(position);
		holder.tvReply.setText(mRes.getString(R.string.replies) + "(" + data.getReplyCount() + ")");
		//holder.tvReply.setText(data.getReplyCount());
		if (data.getMyReplyCount().equalsIgnoreCase("0")) {
			holder.tvReply.setTextColor(mRes.getColor(R.color.list_card_item_button_text_inactive)); // 댓글쓰지 않은 경우
		} else {
			holder.tvReply.setTextColor(mRes.getColor(R.color.list_card_item_button_text_active)); // 댓글 쓴 경우
		}

		// 기능 더 보기
		holder.cmdMore.setTag(position);

		return convertView;
	}

	static class ViewHolder {
		ImageView ivUserPicture;
		TextView tvUserName;
		TextView tvDate;
		TextView tvContent;

		LinearLayout cmdLike;
		TextView tvLike;
		LinearLayout cmdDislike;
		TextView tvDislike;
		LinearLayout cmdReply;
		TextView tvReply;
		LinearLayout cmdMore;
		TextView tvMore;
	}

	@Override
	public void onClick(final View view) {
		int position = Integer.parseInt(view.getTag().toString());

		switch (view.getId()) {
		case R.id.cmdLike: // 좋아요			
			listener.likeOnClick(position);
			break;

		case R.id.cmdDislike: // 싫어요			
			listener.dislikeOnClick(position);
			break;

		case R.id.cmdReply: // 답글	
			listener.replyOnClick(position);
			break;

		case R.id.cmdMore: // 기능 더 보기
			listener.moreOnClick(position);
			break;

		default:
			break;
		}
	}
}