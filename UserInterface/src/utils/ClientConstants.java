package utils;


import okhttp3.MediaType;

public class ClientConstants {
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/Server_Web_exploded";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String LOAD_SPREADSHEET = FULL_SERVER_PATH + "/loadSpreadsheet";
    public final static String GET_ENGINE_DATA = FULL_SERVER_PATH + "/getEngineData";
}
