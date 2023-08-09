public class GpsNetInitiatedHandler {
    private static final String TAG = "GpsNetInitiatedHandler";
    private static final boolean DEBUG = true;
    private static final boolean VERBOSE = false;
    public static final String ACTION_NI_VERIFY = "android.intent.action.NETWORK_INITIATED_VERIFY";
    public static final String NI_INTENT_KEY_NOTIF_ID = "notif_id";
    public static final String NI_INTENT_KEY_TITLE = "title";
    public static final String NI_INTENT_KEY_MESSAGE = "message";
    public static final String NI_INTENT_KEY_TIMEOUT = "timeout";
    public static final String NI_INTENT_KEY_DEFAULT_RESPONSE = "default_resp";
    public static final String NI_RESPONSE_EXTRA_CMD = "send_ni_response";
    public static final String NI_EXTRA_CMD_NOTIF_ID = "notif_id";
    public static final String NI_EXTRA_CMD_RESPONSE = "response";
    public static final int GPS_NI_TYPE_VOICE = 1;
    public static final int GPS_NI_TYPE_UMTS_SUPL = 2;
    public static final int GPS_NI_TYPE_UMTS_CTRL_PLANE = 3;
    public static final int GPS_NI_RESPONSE_ACCEPT = 1;
    public static final int GPS_NI_RESPONSE_DENY = 2;
    public static final int GPS_NI_RESPONSE_NORESP = 3;    
    public static final int GPS_NI_NEED_NOTIFY = 0x0001;
    public static final int GPS_NI_NEED_VERIFY = 0x0002;
    public static final int GPS_NI_PRIVACY_OVERRIDE = 0x0004;
    public static final int GPS_ENC_NONE = 0;
    public static final int GPS_ENC_SUPL_GSM_DEFAULT = 1;
    public static final int GPS_ENC_SUPL_UTF8 = 2;
    public static final int GPS_ENC_SUPL_UCS2 = 3;
    public static final int GPS_ENC_UNKNOWN = -1;
    private final Context mContext;
    private final GpsLocationProvider mGpsLocationProvider;
    private boolean mPlaySounds = false;
    private boolean visible = true;
    private boolean mPopupImmediately = true;
    static private boolean mIsHexInput = true;
    public static class GpsNiNotification
    {
    	int notificationId;
    	int niType;
    	boolean needNotify;
    	boolean needVerify;
    	boolean privacyOverride;
    	int timeout;
    	int defaultResponse;
    	String requestorId;
    	String text;
    	int requestorIdEncoding;
    	int textEncoding;
    	Bundle extras;
    };
    public static class GpsNiResponse {
    	int userResponse;
    	Bundle extras;
    };
    private Notification mNiNotification;
    public GpsNetInitiatedHandler(Context context, GpsLocationProvider gpsLocationProvider) {
    	mContext = context;       
    	mGpsLocationProvider = gpsLocationProvider;
    }
    public void handleNiNotification(GpsNiNotification notif)
    {
    	if (DEBUG) Log.d(TAG, "handleNiNotification" + " notificationId: " + notif.notificationId 
    			+ " requestorId: " + notif.requestorId + " text: " + notif.text);
    	if (notif.needNotify && notif.needVerify && mPopupImmediately)
    	{
    		openNiDialog(notif);
    	}
    	if (notif.needNotify && !notif.needVerify ||
    		notif.needNotify && notif.needVerify && !mPopupImmediately) 
    	{
    		setNiNotification(notif);
    	}
    	if ( notif.needNotify && !notif.needVerify || 
    		!notif.needNotify && !notif.needVerify || 
    		 notif.privacyOverride)
    	{
    		try {
    			mGpsLocationProvider.getNetInitiatedListener().sendNiResponse(notif.notificationId, GPS_NI_RESPONSE_ACCEPT);
    		} 
    		catch (RemoteException e)
    		{
    			Log.e(TAG, e.getMessage());
    		}
    	}
    }
    private synchronized void setNiNotification(GpsNiNotification notif) {
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
    	String title = getNotifTitle(notif);
    	String message = getNotifMessage(notif);
        if (DEBUG) Log.d(TAG, "setNiNotification, notifyId: " + notif.notificationId +
        		", title: " + title +
        		", message: " + message);
    	if (mNiNotification == null) {
        	mNiNotification = new Notification();
        	mNiNotification.icon = com.android.internal.R.drawable.stat_sys_gps_on; 
        	mNiNotification.when = 0;
        }
        if (mPlaySounds) {
        	mNiNotification.defaults |= Notification.DEFAULT_SOUND;
        } else {
        	mNiNotification.defaults &= ~Notification.DEFAULT_SOUND;
        }        
        mNiNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNiNotification.tickerText = getNotifTicker(notif);
        Intent intent = !mPopupImmediately ? getDlgIntent(notif) : new Intent();    	        
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent, 0);                
        mNiNotification.setLatestEventInfo(mContext, title, message, pi);
        if (visible) {
            notificationManager.notify(notif.notificationId, mNiNotification);
        } else {
            notificationManager.cancel(notif.notificationId);
        }
    }
    private void openNiDialog(GpsNiNotification notif) 
    {
    	Intent intent = getDlgIntent(notif);
    	if (DEBUG) Log.d(TAG, "openNiDialog, notifyId: " + notif.notificationId + 
    			", requestorId: " + notif.requestorId + 
    			", text: " + notif.text);               	
    	mContext.startActivity(intent);
    }
    private Intent getDlgIntent(GpsNiNotification notif)
    {
    	Intent intent = new Intent();
    	String title = getDialogTitle(notif);
    	String message = getDialogMessage(notif);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.setClass(mContext, com.android.internal.app.NetInitiatedActivity.class);    	
    	intent.putExtra(NI_INTENT_KEY_NOTIF_ID, notif.notificationId);    	
    	intent.putExtra(NI_INTENT_KEY_TITLE, title);
    	intent.putExtra(NI_INTENT_KEY_MESSAGE, message);
    	intent.putExtra(NI_INTENT_KEY_TIMEOUT, notif.timeout);
    	intent.putExtra(NI_INTENT_KEY_DEFAULT_RESPONSE, notif.defaultResponse);
    	if (DEBUG) Log.d(TAG, "generateIntent, title: " + title + ", message: " + message +
    			", timeout: " + notif.timeout);
    	return intent;
    }
    static byte[] stringToByteArray(String original, boolean isHex)
    {
    	int length = isHex ? original.length() / 2 : original.length();
    	byte[] output = new byte[length];
    	int i;
    	if (isHex)
    	{
    		for (i = 0; i < length; i++)
    		{
    			output[i] = (byte) Integer.parseInt(original.substring(i*2, i*2+2), 16);
    		}
    	}
    	else {
    		for (i = 0; i < length; i++)
    		{
    			output[i] = (byte) original.charAt(i);
    		}
    	}
    	return output;
    }
    static String decodeGSMPackedString(byte[] input)
    {
    	final char CHAR_CR = 0x0D;
    	int nStridx = 0;
    	int nPckidx = 0;
    	int num_bytes = input.length;
    	int cPrev = 0;
    	int cCurr = 0;
    	byte nShift;
    	byte nextChar;
    	byte[] stringBuf = new byte[input.length * 2]; 
    	String result = "";
    	while(nPckidx < num_bytes)
    	{
    		nShift = (byte) (nStridx & 0x07);
    		cCurr = input[nPckidx++];
    		if (cCurr < 0) cCurr += 256;
    		nextChar = (byte) (( (cCurr << nShift) | (cPrev >> (8-nShift)) ) & 0x7F);
    		stringBuf[nStridx++] = nextChar;
    		if(nShift == 6)
    		{
    			if (nPckidx == num_bytes || (cCurr >> 1) == CHAR_CR)
    			{
    				break;
    			}
    			nextChar = (byte) (cCurr >> 1); 
    			stringBuf[nStridx++] = nextChar;
    		}
    		cPrev = cCurr;
    	}
    	try{
    		result = new String(stringBuf, 0, nStridx, "US-ASCII");
    	}
    	catch (UnsupportedEncodingException e)
    	{
    		Log.e(TAG, e.getMessage());
    	}
    	return result;
    }
    static String decodeUTF8String(byte[] input)
    {
    	String decoded = "";
    	try {
    		decoded = new String(input, "UTF-8");
    	}
    	catch (UnsupportedEncodingException e)
    	{ 
    		Log.e(TAG, e.getMessage());
    	} 
		return decoded;
    }
    static String decodeUCS2String(byte[] input)
    {
    	String decoded = "";
    	try {
    		decoded = new String(input, "UTF-16");
    	}
    	catch (UnsupportedEncodingException e)
    	{ 
    		Log.e(TAG, e.getMessage());
    	} 
		return decoded;
    }
    static private String decodeString(String original, boolean isHex, int coding)
    {
    	String decoded = original;
    	byte[] input = stringToByteArray(original, isHex);
    	switch (coding) {
    	case GPS_ENC_NONE:
    		decoded = original;
    		break;
    	case GPS_ENC_SUPL_GSM_DEFAULT:
    		decoded = decodeGSMPackedString(input);
    		break;
    	case GPS_ENC_SUPL_UTF8:
    		decoded = decodeUTF8String(input);
    		break;
    	case GPS_ENC_SUPL_UCS2:
    		decoded = decodeUCS2String(input);
    		break;
    	case GPS_ENC_UNKNOWN:
    		decoded = original;
    		break;
    	default:
    		Log.e(TAG, "Unknown encoding " + coding + " for NI text " + original);
    		break;
    	}
    	return decoded;
    }
    static private String getNotifTicker(GpsNiNotification notif)
    {
    	String ticker = String.format("Position request! ReqId: [%s] ClientName: [%s]",
    			decodeString(notif.requestorId, mIsHexInput, notif.requestorIdEncoding),
    			decodeString(notif.text, mIsHexInput, notif.textEncoding));
    	return ticker;
    }
    static private String getNotifTitle(GpsNiNotification notif)
    {
    	String title = String.format("Position Request");
    	return title;
    }
    static private String getNotifMessage(GpsNiNotification notif)
    {
    	String message = String.format(
    			"NI Request received from [%s] for client [%s]!", 
    			decodeString(notif.requestorId, mIsHexInput, notif.requestorIdEncoding),
    			decodeString(notif.text, mIsHexInput, notif.textEncoding));
    	return message;
    }       
    static public String getDialogTitle(GpsNiNotification notif)
    {
    	return getNotifTitle(notif);
    }
    static private String getDialogMessage(GpsNiNotification notif)
    {
    	return getNotifMessage(notif);
    }
}
