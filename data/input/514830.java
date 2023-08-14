public final class Downloads {
    private Downloads() {}
    public static final String PERMISSION_ACCESS = "android.permission.ACCESS_DOWNLOAD_MANAGER";
    public static final String PERMISSION_ACCESS_ADVANCED =
            "android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED";
    public static final String PERMISSION_CACHE = "android.permission.ACCESS_CACHE_FILESYSTEM";
    public static final String PERMISSION_SEND_INTENTS =
            "android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";
    public static final Uri CONTENT_URI =
        Uri.parse("content:
    public static final String ACTION_DOWNLOAD_COMPLETED =
            "android.intent.action.DOWNLOAD_COMPLETED";
    public static final String ACTION_NOTIFICATION_CLICKED =
            "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_APP_DATA = "entity";
    public static final String COLUMN_NO_INTEGRITY = "no_integrity";
    public static final String COLUMN_FILE_NAME_HINT = "hint";
    public static final String _DATA = "_data";
    public static final String COLUMN_MIME_TYPE = "mimetype";
    public static final String COLUMN_DESTINATION = "destination";
    public static final String COLUMN_VISIBILITY = "visibility";
    public static final String COLUMN_CONTROL = "control";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_LAST_MODIFICATION = "lastmod";
    public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
    public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
    public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";
    public static final String COLUMN_COOKIE_DATA = "cookiedata";
    public static final String COLUMN_USER_AGENT = "useragent";
    public static final String COLUMN_REFERER = "referer";
    public static final String COLUMN_TOTAL_BYTES = "total_bytes";
    public static final String COLUMN_CURRENT_BYTES = "current_bytes";
    public static final String COLUMN_OTHER_UID = "otheruid";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final int DESTINATION_EXTERNAL = 0;
    public static final int DESTINATION_CACHE_PARTITION = 1;
    public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;
    public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;
    public static final int CONTROL_RUN = 0;
    public static final int CONTROL_PAUSED = 1;
    public static boolean isStatusInformational(int status) {
        return (status >= 100 && status < 200);
    }
    public static boolean isStatusSuspended(int status) {
        return (status == STATUS_PENDING_PAUSED || status == STATUS_RUNNING_PAUSED);
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
    public static final int STATUS_PENDING_PAUSED = 191;
    public static final int STATUS_RUNNING = 192;
    public static final int STATUS_RUNNING_PAUSED = 193;
    public static final int STATUS_SUCCESS = 200;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_NOT_ACCEPTABLE = 406;
    public static final int STATUS_LENGTH_REQUIRED = 411;
    public static final int STATUS_PRECONDITION_FAILED = 412;
    public static final int STATUS_CANCELED = 490;
    public static final int STATUS_UNKNOWN_ERROR = 491;
    public static final int STATUS_FILE_ERROR = 492;
    public static final int STATUS_UNHANDLED_REDIRECT = 493;
    public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
    public static final int STATUS_HTTP_DATA_ERROR = 495;
    public static final int STATUS_HTTP_EXCEPTION = 496;
    public static final int STATUS_TOO_MANY_REDIRECTS = 497;
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;
    public static final int VISIBILITY_VISIBLE = 0;
    public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
    public static final int VISIBILITY_HIDDEN = 2;
    public static final class Impl implements BaseColumns {
        private Impl() {}
        public static final String PERMISSION_ACCESS = "android.permission.ACCESS_DOWNLOAD_MANAGER";
        public static final String PERMISSION_ACCESS_ADVANCED =
                "android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED";
        public static final String PERMISSION_CACHE = "android.permission.ACCESS_CACHE_FILESYSTEM";
        public static final String PERMISSION_SEND_INTENTS =
                "android.permission.SEND_DOWNLOAD_COMPLETED_INTENTS";
        public static final String PERMISSION_SEE_ALL_EXTERNAL =
                "android.permission.SEE_ALL_EXTERNAL";
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String ACTION_DOWNLOAD_COMPLETED =
                "android.intent.action.DOWNLOAD_COMPLETED";
        public static final String ACTION_NOTIFICATION_CLICKED =
                "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_APP_DATA = "entity";
        public static final String COLUMN_NO_INTEGRITY = "no_integrity";
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        public static final String _DATA = "_data";
        public static final String COLUMN_MIME_TYPE = "mimetype";
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_VISIBILITY = "visibility";
        public static final String COLUMN_CONTROL = "control";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_LAST_MODIFICATION = "lastmod";
        public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
        public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
        public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";
        public static final String COLUMN_COOKIE_DATA = "cookiedata";
        public static final String COLUMN_USER_AGENT = "useragent";
        public static final String COLUMN_REFERER = "referer";
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";
        public static final String COLUMN_OTHER_UID = "otheruid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final int DESTINATION_EXTERNAL = 0;
        public static final int DESTINATION_CACHE_PARTITION = 1;
        public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;
        public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;
        public static final int CONTROL_RUN = 0;
        public static final int CONTROL_PAUSED = 1;
        public static boolean isStatusInformational(int status) {
            return (status >= 100 && status < 200);
        }
        public static boolean isStatusSuspended(int status) {
            return (status == STATUS_PENDING_PAUSED || status == STATUS_RUNNING_PAUSED);
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
        public static final int STATUS_PENDING_PAUSED = 191;
        public static final int STATUS_RUNNING = 192;
        public static final int STATUS_RUNNING_PAUSED = 193;
        public static final int STATUS_SUCCESS = 200;
        public static final int STATUS_BAD_REQUEST = 400;
        public static final int STATUS_NOT_ACCEPTABLE = 406;
        public static final int STATUS_LENGTH_REQUIRED = 411;
        public static final int STATUS_PRECONDITION_FAILED = 412;
        public static final int STATUS_CANCELED = 490;
        public static final int STATUS_UNKNOWN_ERROR = 491;
        public static final int STATUS_FILE_ERROR = 492;
        public static final int STATUS_UNHANDLED_REDIRECT = 493;
        public static final int STATUS_UNHANDLED_HTTP_CODE = 494;
        public static final int STATUS_HTTP_DATA_ERROR = 495;
        public static final int STATUS_HTTP_EXCEPTION = 496;
        public static final int STATUS_TOO_MANY_REDIRECTS = 497;
        public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;
        public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;
        public static final int VISIBILITY_VISIBLE = 0;
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
        public static final int VISIBILITY_HIDDEN = 2;
    }
}
