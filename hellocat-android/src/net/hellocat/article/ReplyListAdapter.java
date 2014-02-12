package net.hellocat.article;

import java.util.ArrayList;

import net.hellocat.common.DataUtil;
import net.hellocat.util.Util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.summertaker.cat.R;

public class ReplyListAdapter extends BaseAdapter implements OnClickListener {

	private Context mContext;
	private LayoutInflater mLayoutInflater;
	private ArrayList<ReplyListItem> mDataList = null;

	ReplyListListener listener;

	public ReplyListAdapter(Context context, ArrayList<ReplyListItem> dataList) {
		this.mContext = context;
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

		ReplyListItem data = (ReplyListItem) mDataList.get(position);

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.reply_list_item, null);

			holder = new ViewHolder();

			// 답글 항목
			holder.ivUserPicture = (ImageView) convertView.findViewById(R.id.ivUserPicture);
			holder.ivUserPicture.setOnClickListener(this);
			holder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			holder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		DataUtil.setUserPicture(mContext, holder.ivUserPicture, data.getUserPicture(), true);
		holder.tvUserName.setText(data.getUserName());
		holder.tvDate.setText(Util.getEllapsedTime(data.getDate()));
		holder.tvContent.setText(data.getContent());

		return convertView;
	}

	static class ViewHolder {
		ImageView ivUserPicture;
		TextView tvUserName;
		TextView tvDate;
		TextView tvContent;

		ImageView ivMenu;
	}

	@Override
	public void onClick(final View view) {
		int position = Integer.parseInt(view.getTag().toString());

		switch (view.getId()) {

		default:
			break;
		}
	}
}