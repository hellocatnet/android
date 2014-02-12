package net.hellocat.common;

public class Config {
	public final static String TAG = "TAG";

	public final static String PACKAGE_NAME = "net.taxilove";
	public final static String USER_PREFERENCE_KEY = "taxilove";

	public final static String URL_GOOGLE_PLAY = "market://details?id=com.google.android.apps.maps";

	/*
	 * 구글 장소 검색 (구글 콘솔 > Key for server applications)
	 */
	public final static String GOOGLE_SERVER_KEY = "AIzaSyA6X-mbN6I0xuDzvmHpJx7hHhFnBXlrd7Y";

	// 구글 Static Maps
	public final static String GOOGLE_BROWSER_KEY = "AIzaSyCM_xmy1HMuqa8918hwnittScPhikCH7mM";

	/*
	 * 구글 클라우드 메세징
	 */
	public final static String GCM_SENDER_ID = "368714862198"; // 구글 콘솔 > 프로젝트 번호
	public final static String GCM_DISPLAY_MESSAGE_ACTION = "net.taxilove.DISPLAY_MESSAGE";

	/*
	 * Server Domain
	 */
	public final static String SERVER_DOMAIN = "http://121.78.237.117";

	/*
	 * 인증
	 */
	public final static String URL_LOGIN = SERVER_DOMAIN + "/user/login"; // 로그인
	public final static String URL_LOGOUT = SERVER_DOMAIN + "/user/logout"; // 로그아웃
	public final static String URL_PASSWORD = SERVER_DOMAIN + "/user/password"; // 비밀번호 찾기
	
	/*
	 * 홈 (요약 정보)
	 */
	public final static String URL_SUMMARY = SERVER_DOMAIN + "/user/summary"; // 요약 정보

	/*
	 * 회원
	 */
	public final static String URL_JOIN = SERVER_DOMAIN + "/user/join"; // 회원 가입
	public final static String URL_SECEDE = SERVER_DOMAIN + "/user/secede"; // 회원 탈퇴
	public final static String URL_USER_UPDATE = SERVER_DOMAIN + "/user/update"; // 회원 수정
	public final static String URL_USER_SEARCH = SERVER_DOMAIN + "/user/search"; // 회원 검색
	
	/*
	 * 파일 업로드
	 */
	public final static String URL_UPLOAD = SERVER_DOMAIN + "/photo/upload_android"; // 사진 업로드

	/*
	 * 아티클
	 */
	public final static String URL_ARTICLE_LIST = SERVER_DOMAIN + "/article/list"; // 아티클 목록
	public final static String URL_ARTICLE_SAVE = SERVER_DOMAIN + "/article/save"; // 아티클 저장
	public final static String URL_ARTICLE_DELETE = SERVER_DOMAIN + "/article/delete"; // 아티클 삭제
	
	/*
	 * 댓글
	 */
	public final static String URL_REPLY_LIST = SERVER_DOMAIN + "/reply/list"; // 댓글 목록
	public final static String URL_REPLY_SAVE = SERVER_DOMAIN + "/reply/save"; // 댓글 저장
	public final static String URL_REPLY_DELETE = SERVER_DOMAIN + "/reply/delete"; // 댓글 삭제

	/*
	 * 투표 (좋아요, 싫어요)
	 */
	public final static String URL_VOTE = SERVER_DOMAIN + "/vote"; // 투표
	
	/*
	 * 신고
	 */
	public final static String URL_REPORT = SERVER_DOMAIN + "/report"; // 신고
	
	/*
	 * 액티비티 리턴 결과
	 */
	public final static int ACTIVITY_RESULT_CODE_CONTINUE = 900; // 현재 액티비티 종료 > 이전 액티비티에서 다음 액티비티로 이동
	public final static int ACTIVITY_RESULT_CODE_FINISH = 900; // 현재 액티비티 종료 > 이전 액티비티 종료 후 이전 액티비티로 이동
	public final static int ACTIVITY_RESULT_CODE_SECEDE = 990; // 회원 탈퇴
}
