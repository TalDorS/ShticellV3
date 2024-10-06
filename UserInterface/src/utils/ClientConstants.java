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
    public static final  String GET_CELL_BY_ID = FULL_SERVER_PATH + "/getCellById";
    public static final  String GET_CELLDTO_BY_ID = FULL_SERVER_PATH + "/getCellDTOById";
    public static final String UPDATE_CELL_VALUE = FULL_SERVER_PATH + "/updateCellValue";
    public static final String GET_SPREADSHEET_BY_VERSION = FULL_SERVER_PATH + "/getSpreadsheetByVersion";
    public static final String GET_VERSIONS = FULL_SERVER_PATH + "/getVersions";
    public static final String ADD_RANGE = FULL_SERVER_PATH + "/addRange";
    public static final String IS_SPREADSHEET_LOADED = FULL_SERVER_PATH + "/isSpreadsheetLoaded";

}
