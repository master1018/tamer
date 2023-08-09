public class TransactionSettings {
    private static final String TAG = "TransactionSettings";
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private String mServiceCenter;
    private String mProxyAddress;
    private int mProxyPort = -1;
    private static final String[] APN_PROJECTION = {
            Telephony.Carriers.TYPE,            
            Telephony.Carriers.MMSC,            
            Telephony.Carriers.MMSPROXY,        
            Telephony.Carriers.MMSPORT          
    };
    private static final int COLUMN_TYPE         = 0;
    private static final int COLUMN_MMSC         = 1;
    private static final int COLUMN_MMSPROXY     = 2;
    private static final int COLUMN_MMSPORT      = 3;
    public TransactionSettings(Context context, String apnName) {
        String selection = (apnName != null)?
                Telephony.Carriers.APN + "='"+apnName+"'": null;
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current"),
                            APN_PROJECTION, selection, null, null);
        if (cursor == null) {
            Log.e(TAG, "Apn is not found in Database!");
            return;
        }
        boolean sawValidApn = false;
        try {
            while (cursor.moveToNext() && TextUtils.isEmpty(mServiceCenter)) {
                if (isValidApnType(cursor.getString(COLUMN_TYPE), Phone.APN_TYPE_MMS)) {
                    sawValidApn = true;
                    mServiceCenter = cursor.getString(COLUMN_MMSC);
                    mProxyAddress = cursor.getString(COLUMN_MMSPROXY);
                    if (isProxySet()) {
                        String portString = cursor.getString(COLUMN_MMSPORT);
                        try {
                            mProxyPort = Integer.parseInt(portString);
                        } catch (NumberFormatException e) {
                            if (TextUtils.isEmpty(portString)) {
                                Log.w(TAG, "mms port not set!");
                            } else {
                                Log.e(TAG, "Bad port number format: " + portString, e);
                            }
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
        if (sawValidApn && TextUtils.isEmpty(mServiceCenter)) {
            Log.e(TAG, "Invalid APN setting: MMSC is empty");
        }
    }
    public TransactionSettings(String mmscUrl, String proxyAddr, int proxyPort) {
        mServiceCenter = mmscUrl;
        mProxyAddress = proxyAddr;
        mProxyPort = proxyPort;
    }
    public String getMmscUrl() {
        return mServiceCenter;
    }
    public String getProxyAddress() {
        return mProxyAddress;
    }
    public int getProxyPort() {
        return mProxyPort;
    }
    public boolean isProxySet() {
        return (mProxyAddress != null) && (mProxyAddress.trim().length() != 0);
    }
    static private boolean isValidApnType(String types, String requestType) {
        if (TextUtils.isEmpty(types)) {
            return true;
        }
        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals(Phone.APN_TYPE_ALL)) {
                return true;
            }
        }
        return false;
    }
}
