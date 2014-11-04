package com.bsp.comicapp.util;

import android.graphics.drawable.Drawable;

import com.bsp.comicapp.model.User;

public class Config {
	public static float screenWidth = 0;
	public static float screenHeight = 0;

	public static int TIME_OUT_GET_DATA = 25000;
	public static int TIME_OUT_CONNECTION = 20000;

	public static Drawable Bg_Landscape = null;
	public static Drawable Bg_Portrait = null;

	public static String IdUser = " ";
	public static String AVATAR = "AVATAR";
	public static String AvatarUser = " ";
	public static User USER = new User();

	public static String INTERNET_DETECT = "internet_detect";
	public static String EXTRA_ISCONNECTED = "isConnectedInternet";
	public static Boolean isConnectedInternet = true;

	// -------API
	public static String HOST_SERVER = "http://baraahgroup.com/admin/user/";
	public static String GETBOOK_URL = "http://baraahgroup.com/admin/api/getBooks?lang=%s&type=%s";
	public static String GETUPLOADBOOK_URL = "http://baraahgroup.com/admin/api/getUpload";
	public static String GET_USER = "http://baraahgroup.com/admin/api/getuser";
	public static String CREATE_FAN_COMIC = "http://baraahgroup.com/admin/api/uploadInfo";
	public static String UPLOAD_PHOTO = "http://baraahgroup.com/admin/api/uploadImage";
	public static String API_GETBACKGRUOND = "http://baraahgroup.com/admin/api/GetBackGround";

	/**
	 * register email,pass,avatar,name
	 */
	public static String API_REGISTER = HOST_SERVER + "adduser";
	/**
	 * login - params: email, pass
	 */
	public static String API_LOGIN_EMAIL = HOST_SERVER + "login";

	/**
	 * login type - params: username, type, avar
	 */
	public static String API_LOGIN_TYPE = HOST_SERVER + "logintype";

	/**
	 * get data comment - params: bookID, row, content, userID
	 */
	public static String API_GET_COMMENT = HOST_SERVER + "listcommentbybookID";

	/**
	 * post rating - params: bookID, userID, num
	 */
	public static String API_POST_RATING = HOST_SERVER + "raiting";

	/**
	 * upload image - params: files
	 */
	public static String API_UPLOAD_FILE = HOST_SERVER + "upload";

	/**
	 * ListcommentbybookID? bookID=2&row=0&content=&userID=58&type=none
	 */
	public static String API_COMMENT = "http://baraahgroup.com/admin/comment/"
			+ "ListcommentbybookID";

	public static String LANGUAGE_PREFERENCE_NAME = "language_preference";
	public static String PREFERENCE_KEY_LANGUAGE = "key_language";
	public static String ENGLISH_LANGUAGUE = "eng";
	public static String ARABIC_LANGUAGE = "arabic";
}
