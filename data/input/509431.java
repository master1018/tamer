public class Constants {
    public static final String TAG = "BluetoothOpp";
    public static final String ACTION_RETRY = "android.btopp.intent.action.RETRY";
    public static final String ACTION_OPEN = "android.btopp.intent.action.OPEN";
    public static final String ACTION_OPEN_OUTBOUND_TRANSFER = "android.btopp.intent.action.OPEN_OUTBOUND";
    public static final String ACTION_OPEN_INBOUND_TRANSFER = "android.btopp.intent.action.OPEN_INBOUND";
    public static final String ACTION_LIST = "android.btopp.intent.action.LIST";
    public static final String ACTION_HIDE = "android.btopp.intent.action.HIDE";
    public static final String ACTION_COMPLETE_HIDE = "android.btopp.intent.action.HIDE_COMPLETE";
    public static final String ACTION_INCOMING_FILE_CONFIRM = "android.btopp.intent.action.CONFIRM";
    public static final String THIS_PACKAGE_NAME = "com.android.bluetooth";
    public static final String MEDIA_SCANNED = "scanned";
    public static final int MEDIA_SCANNED_NOT_SCANNED = 0;
    public static final int MEDIA_SCANNED_SCANNED_OK = 1;
    public static final int MEDIA_SCANNED_SCANNED_FAILED = 2;
    public static final String[] ACCEPTABLE_SHARE_OUTBOUND_TYPES = new String[] {
        "image
    public static final String[] UNACCEPTABLE_SHARE_OUTBOUND_TYPES = new String[] {
        "virus
    public static final String[] ACCEPTABLE_SHARE_INBOUND_TYPES = new String[] {
        "image
    public static final String[] UNACCEPTABLE_SHARE_INBOUND_TYPES = new String[] {
        "text/x-vcalendar",
    };
    public static final String DEFAULT_STORE_SUBDIR = "/bluetooth";
    public static final boolean DEBUG = false;
    public static final boolean VERBOSE = false;
    public static final boolean USE_TCP_DEBUG = false;
    public static final boolean USE_TCP_SIMPLE_SERVER = false;
    public static final int TCP_DEBUG_PORT = 6500;
    public static final boolean USE_EMULATOR_DEBUG = false;
    public static final int MAX_RECORDS_IN_DATABASE = 1000;
    public static final int BATCH_STATUS_PENDING = 0;
    public static final int BATCH_STATUS_RUNNING = 1;
    public static final int BATCH_STATUS_FINISHED = 2;
    public static final int BATCH_STATUS_FAILED = 3;
    public static final String BLUETOOTHOPP_NAME_PREFERENCE = "btopp_names";
    public static final String BLUETOOTHOPP_CHANNEL_PREFERENCE = "btopp_channels";
    public static String filename_SEQUENCE_SEPARATOR = "-";
    public static void updateShareStatus(Context context, int id, int status) {
        Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + id);
        ContentValues updateValues = new ContentValues();
        updateValues.put(BluetoothShare.STATUS, status);
        context.getContentResolver().update(contentUri, updateValues, null, null);
        Constants.sendIntentIfCompleted(context, contentUri, status);
    }
    public static void sendIntentIfCompleted(Context context, Uri contentUri, int status) {
        if (BluetoothShare.isStatusCompleted(status)) {
            Intent intent = new Intent(BluetoothShare.TRANSFER_COMPLETED_ACTION);
            intent.setClassName(THIS_PACKAGE_NAME, BluetoothOppReceiver.class.getName());
            intent.setData(contentUri);
            context.sendBroadcast(intent);
        }
    }
    public static boolean mimeTypeMatches(String mimeType, String[] matchAgainst) {
        for (String matchType : matchAgainst) {
            if (mimeTypeMatches(mimeType, matchType)) {
                return true;
            }
        }
        return false;
    }
    public static boolean mimeTypeMatches(String mimeType, String matchAgainst) {
        Pattern p = Pattern.compile(matchAgainst.replaceAll("\\*", "\\.\\*"),
                Pattern.CASE_INSENSITIVE);
        return p.matcher(mimeType).matches();
    }
    public static void logHeader(HeaderSet hs) {
        Log.v(TAG, "Dumping HeaderSet " + hs.toString());
        try {
            Log.v(TAG, "COUNT : " + hs.getHeader(HeaderSet.COUNT));
            Log.v(TAG, "NAME : " + hs.getHeader(HeaderSet.NAME));
            Log.v(TAG, "TYPE : " + hs.getHeader(HeaderSet.TYPE));
            Log.v(TAG, "LENGTH : " + hs.getHeader(HeaderSet.LENGTH));
            Log.v(TAG, "TIME_ISO_8601 : " + hs.getHeader(HeaderSet.TIME_ISO_8601));
            Log.v(TAG, "TIME_4_BYTE : " + hs.getHeader(HeaderSet.TIME_4_BYTE));
            Log.v(TAG, "DESCRIPTION : " + hs.getHeader(HeaderSet.DESCRIPTION));
            Log.v(TAG, "TARGET : " + hs.getHeader(HeaderSet.TARGET));
            Log.v(TAG, "HTTP : " + hs.getHeader(HeaderSet.HTTP));
            Log.v(TAG, "WHO : " + hs.getHeader(HeaderSet.WHO));
            Log.v(TAG, "OBJECT_CLASS : " + hs.getHeader(HeaderSet.OBJECT_CLASS));
            Log.v(TAG, "APPLICATION_PARAMETER : " + hs.getHeader(HeaderSet.APPLICATION_PARAMETER));
        } catch (IOException e) {
            Log.e(TAG, "dump HeaderSet error " + e);
        }
    }
}
