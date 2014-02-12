package net.hellocat.common;

public class User {

	public static String id;
	public static String name;
	public static String email;
	public static String oicture;
	public static String background;
	public static String authSite;
	public static String authId;

	public static String gcmId;
	public static String gcmMessage;

	public static String nfcData;

	public static void setUser(String email, String name, String authSite, String authId) {
		User.email = email;
		User.name = name;
		User.authSite = authSite;
		User.authId = authId;
	}

	public static String getId() {
		return (id == null) ? "" : id;
	}

	public static void setId(String id) {
		User.id = id;
	}

	public static String getEmail() {
		return email;
	}

	public static void setEmail(String email) {
		User.email = email;
	}

	public static String getAuthSite() {
		return authSite;
	}

	public static void setAuthSite(String authSite) {
		User.authSite = authSite;
	}

	public static String getAuthId() {
		return authId;
	}

	public static void setAuthId(String authId) {
		User.authId = authId;
	}

	public static String getGcmId() {
		return gcmId;
	}

	public static void setGcmId(String gcmId) {
		User.gcmId = gcmId;
	}

	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		User.name = name;
	}

	public static String getUserPicture() {
		return oicture;
	}

	public static void setPicture(String picture) {
		User.oicture = picture;
	}

	public static String getBackground() {
		return background;
	}

	public static void setBackground(String background) {
		User.background = background;
	}

	public static String getGcmMessage() {
		return gcmMessage;
	}

	public static void setGcmMessage(String gcmMessage) {
		User.gcmMessage = gcmMessage;
	}

	public static String getNfcData() {
		return nfcData;
	}

	public static void setNfcData(String nfcData) {
		User.nfcData = nfcData;
	}

	public static void clear() {
		User.id = null;
		User.name = null;
		//UserData.email = null;
		User.oicture = null;
		User.background = null;
		User.authSite = null;
		User.authId = null;
		User.gcmId = null;
	}

	public static String value() {
		return ">>id: " + id + "\n- email: " + email + "\n- name: " + name + "\n- authSite: " + authSite
				+ "\n- authId: " + authId;
	}
}
