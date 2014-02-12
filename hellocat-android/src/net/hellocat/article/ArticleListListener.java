package net.hellocat.article;

public interface ArticleListListener {
	
	public void likeOnClick(int position);
	
	public void dislikeOnClick(int position);
	
	public void replyOnClick(int position);
	
	public void moreOnClick(int position);
}
