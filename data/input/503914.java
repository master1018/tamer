public class Eas {
    public static boolean WAIT_DEBUG = false;   
    public static boolean DEBUG = false;         
    public static boolean USER_LOG = false;     
    public static boolean PARSER_LOG = false;   
    public static boolean FILE_LOG = false;     
    public static final int DEBUG_BIT = 1;
    public static final int DEBUG_EXCHANGE_BIT = 2;
    public static final int DEBUG_FILE_BIT = 4;
    public static final String VERSION = "0.3";
    public static final String ACCOUNT_MAILBOX_PREFIX = "__eas";
    public static final String SUPPORTED_PROTOCOL_EX2003 = "2.5";
    public static final double SUPPORTED_PROTOCOL_EX2003_DOUBLE = 2.5;
    public static final String SUPPORTED_PROTOCOL_EX2007 = "12.0";
    public static final double SUPPORTED_PROTOCOL_EX2007_DOUBLE = 12.0;
    public static final String DEFAULT_PROTOCOL_VERSION = SUPPORTED_PROTOCOL_EX2003;
    public static final String FILTER_ALL = "0";
    public static final String FILTER_1_DAY = "1";
    public static final String FILTER_3_DAYS = "2";
    public static final String FILTER_1_WEEK = "3";
    public static final String FILTER_2_WEEKS = "4";
    public static final String FILTER_1_MONTH = "5";
    public static final String FILTER_3_MONTHS = "6";
    public static final String FILTER_6_MONTHS = "7";
    public static final String BODY_PREFERENCE_TEXT = "1";
    public static final String BODY_PREFERENCE_HTML = "2";
    public static final String EAS12_TRUNCATION_SIZE = "200000";
    public static final String EAS2_5_TRUNCATION_SIZE = "7";
    public static final int FOLDER_STATUS_OK = 1;
    public static final int FOLDER_STATUS_INVALID_KEY = 9;
    public static final int EXCHANGE_ERROR_NOTIFICATION = 0x10;
    public static void setUserDebug(int state) {
        if (!DEBUG) {
            USER_LOG = (state & DEBUG_BIT) != 0;
            PARSER_LOG = (state & DEBUG_EXCHANGE_BIT) != 0;
            FILE_LOG = (state & DEBUG_FILE_BIT) != 0;
            if (FILE_LOG || PARSER_LOG) {
                USER_LOG = true;
            }
            Log.d("Eas Debug", "Logging: " + (USER_LOG ? "User " : "") +
                    (PARSER_LOG ? "Parser " : "") + (FILE_LOG ? "File" : ""));
        }
     }
}
