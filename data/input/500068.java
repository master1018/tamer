public final class BluetoothShare implements BaseColumns {
    private BluetoothShare() {
    }
    public static final String PERMISSION_ACCESS = "android.permission.ACCESS_BLUETOOTH_SHARE";
    public static final Uri CONTENT_URI = Uri.parse("content:
    public static final String TRANSFER_COMPLETED_ACTION = "android.btopp.intent.action.TRANSFER_COMPLETE";
    public static final String INCOMING_FILE_CONFIRMATION_REQUEST_ACTION = "android.btopp.intent.action.INCOMING_FILE_NOTIFICATION";
    public static final String USER_CONFIRMATION_TIMEOUT_ACTION = "android.btopp.intent.action.USER_CONFIRMATION_TIMEOUT";
    public static final String URI = "uri";
    public static final String FILENAME_HINT = "hint";
    public static final String _DATA = "_data";
    public static final String MIMETYPE = "mimetype";
    public static final String DIRECTION = "direction";
    public static final String DESTINATION = "destination";
    public static final String VISIBILITY = "visibility";
    public static final String USER_CONFIRMATION = "confirm";
    public static final String STATUS = "status";
    public static final String TOTAL_BYTES = "total_bytes";
    public static final String CURRENT_BYTES = "current_bytes";
    public static final String TIMESTAMP = "timestamp";
    public static final int DIRECTION_OUTBOUND = 0;
    public static final int DIRECTION_INBOUND = 1;
    public static final int USER_CONFIRMATION_PENDING = 0;
    public static final int USER_CONFIRMATION_CONFIRMED = 1;
    public static final int USER_CONFIRMATION_AUTO_CONFIRMED = 2;
    public static final int USER_CONFIRMATION_DENIED = 3;
    public static final int USER_CONFIRMATION_TIMEOUT = 4;
    public static final int VISIBILITY_VISIBLE = 0;
    public static final int VISIBILITY_HIDDEN = 1;
    public static boolean isStatusInformational(int status) {
        return (status >= 100 && status < 200);
    }
    public static boolean isStatusSuspended(int status) {
        return (status == STATUS_PENDING);
    }
    public static boolean isStatusSuccess(int status) {
        return (status >= 200 && status < 300);
    }
    public static boolean isStatusError(int status) {
        return (status >= 400 && status < 600);
    }
    public static boolean isStatusClientError(int status) {
        return (status >= 400 && status < 500);
    }
    public static boolean isStatusServerError(int status) {
        return (status >= 500 && status < 600);
    }
    public static boolean isStatusCompleted(int status) {
        return (status >= 200 && status < 300) || (status >= 400 && status < 600);
    }
    public static final int STATUS_PENDING = 190;
    public static final int STATUS_RUNNING = 192;
    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_ACCEPTABLE = 406;
    public static final int STATUS_LENGTH_REQUIRED = 411;
    public static final int STATUS_PRECONDITION_FAILED = 412;
    public static final int STATUS_CANCELED = 490;
    public static final int STATUS_UNKNOWN_ERROR = 491;
    public static final int STATUS_FILE_ERROR = 492;
    public static final int STATUS_ERROR_NO_SDCARD = 493;
    public static final int STATUS_ERROR_SDCARD_FULL = 494;
    public static final int STATUS_UNHANDLED_OBEX_CODE = 495;
    public static final int STATUS_OBEX_DATA_ERROR = 496;
    public static final int STATUS_CONNECTION_ERROR = 497;
}
