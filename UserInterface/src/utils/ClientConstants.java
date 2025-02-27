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
    public static final  String GET_CELLDTO_BY_ID = FULL_SERVER_PATH + "/getCellDTOById";
    public static final String UPDATE_CELL_VALUE = FULL_SERVER_PATH + "/updateCellValue";
    public static final String GET_SPREADSHEET_BY_VERSION = FULL_SERVER_PATH + "/getSpreadsheetByVersion";
    public static final String GET_VERSIONS = FULL_SERVER_PATH + "/getVersions";
    public static final String ADD_RANGE = FULL_SERVER_PATH + "/addRange";
    public static final String IS_SPREADSHEET_LOADED = FULL_SERVER_PATH + "/isSpreadsheetLoaded";
    public static final String GET_RANGES = FULL_SERVER_PATH + "/getRanges";
    public static final String REMOVE_RANGE = FULL_SERVER_PATH + "/removeRange";
    public static final String SORT_SPREADSHEET = FULL_SERVER_PATH + "/sortSpreadsheet";
    public static final String GET_SPREADSHEET = FULL_SERVER_PATH + "/getSpreadsheet";
    public static final String GET_COLUMN_INDEX = FULL_SERVER_PATH + "/getColumnIndex";
    public static final String GET_COLUMN_NAME = FULL_SERVER_PATH + "/getColumnName";
    public static final String FILTER_TABLE_MULTIPLE_COLUMNS = FULL_SERVER_PATH + "/filterTableMultipleColumns";
    public static final String CHECK_CIRCULAR_REFERENCES = FULL_SERVER_PATH + "/checkCircularReferences";
    public static final String PARSE_EXPRESSION = FULL_SERVER_PATH + "/parseExpression";
    public static final String DYNAMIC_ANALYSIS = FULL_SERVER_PATH + "/dynamicAnalysis";
    public static final String GET_USER_PERMISSIONS = FULL_SERVER_PATH + "/getUserPermissions";
    public static final String GET_PERMISSIONS = FULL_SERVER_PATH + "/getPermissions";
    public static final String GET_CHAT_DATA_LIST = FULL_SERVER_PATH + "/getChatDataList";
    public static final String ADD_CHAT_MESSAGE = FULL_SERVER_PATH + "/addChatMessage";
    public static final String GET_SHEET_DETAILS = FULL_SERVER_PATH + "/getSheetDetails";
    public static final String REQUEST_PERMISSION = FULL_SERVER_PATH + "/requestPermission";
    public static final String HANDLE_PERMISSION_REQUEST = FULL_SERVER_PATH + "/handlePermissionRequest";
    public static final String CAN_DYNAMIC_ANALYSIS_BE_DONE = FULL_SERVER_PATH + "/canDynamicAnalysisBeDone";
    public static final String GET_FUNCTION_NAMES = FULL_SERVER_PATH + "/get-function-names";
    public static final String GET_NUMBER_OF_ARGUMENTS = FULL_SERVER_PATH + "/get-number-of-arguments";
}
