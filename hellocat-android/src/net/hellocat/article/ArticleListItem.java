package net.hellocat.article;

import java.io.Serializable;

public class ArticleListItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String userId;
	private String userName;
	private String userEmail;
	private String userSite;
	private String userPicture;
	private String date;
	private String content;
	private int distance;
	private String likeCount;
	private String myLikeCount;
	private String dislikeCount;
	private String myDislikeCount;
	private String replyCount;
	private String myReplyCount;
	private String deleted;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserSite() {
		return userSite;
	}

	public void setUserSite(String userSite) {
		this.userSite = userSite;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUserPicture() {
		return userPicture;
	}

	public void setUserPicture(String userPicture) {
		this.userPicture = userPicture;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public String getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(String likeCount) {
		this.likeCount = likeCount;
	}

	public String getDislikeCount() {
		return dislikeCount;
	}

	public void setDislikeCount(String dislikeCount) {
		this.dislikeCount = dislikeCount;
	}

	public String getMyLikeCount() {
		return myLikeCount;
	}

	public void setMyLikeCount(String myLikeCount) {
		this.myLikeCount = myLikeCount;
	}

	public String getMyDislikeCount() {
		return myDislikeCount;
	}

	public void setMyDislikeCount(String myDislikeCount) {
		this.myDislikeCount = myDislikeCount;
	}

	public String getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(String replyCount) {
		this.replyCount = replyCount;
	}

	public String getMyReplyCount() {
		return myReplyCount;
	}

	public void setMyReplyCount(String myReplyCount) {
		this.myReplyCount = myReplyCount;
	}

	public String getDeleted() {
		return deleted;
	}

	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", userId=" + userId + ", userName=" + userName + ", date=" + date + "]";
	}
}