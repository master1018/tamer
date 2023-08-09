public class PhoneFactory {
    static final String LOG_TAG = "PHONE";
    static final int SOCKET_OPEN_RETRY_MILLIS = 2 * 1000;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    static private Phone sProxyPhone = null;
    static private CommandsInterface sCommandsInterface = null;
    static private boolean sMadeDefaults = false;
    static private PhoneNotifier sPhoneNotifier;
    static private Looper sLooper;
    static private Context sContext;
    static final int preferredNetworkMode = RILConstants.PREFERRED_NETWORK_MODE;
    static final int preferredCdmaSubscription = RILConstants.PREFERRED_CDMA_SUBSCRIPTION;
    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }
    public static void makeDefaultPhone(Context context) {
        synchronized(Phone.class) {
            if (!sMadeDefaults) {
                sLooper = Looper.myLooper();
                sContext = context;
                if (sLooper == null) {
                    throw new RuntimeException(
                        "PhoneFactory.makeDefaultPhone must be called from Looper thread");
                }
                int retryCount = 0;
                for(;;) {
                    boolean hasException = false;
                    retryCount ++;
                    try {
                        new LocalServerSocket("com.android.internal.telephony");
                    } catch (java.io.IOException ex) {
                        hasException = true;
                    }
                    if ( !hasException ) {
                        break;
                    } else if (retryCount > SOCKET_OPEN_MAX_RETRY) {
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);
                        } catch (InterruptedException er) {
                        }
                    }
                }
                sPhoneNotifier = new DefaultPhoneNotifier();
                int networkMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
                Log.i(LOG_TAG, "Network Mode set to " + Integer.toString(networkMode));
                int cdmaSubscription = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.PREFERRED_CDMA_SUBSCRIPTION, preferredCdmaSubscription);
                Log.i(LOG_TAG, "Cdma Subscription set to " + Integer.toString(cdmaSubscription));
                sCommandsInterface = new RIL(context, networkMode, cdmaSubscription);
                int phoneType = getPhoneType(networkMode);
                if (phoneType == Phone.PHONE_TYPE_GSM) {
                    sProxyPhone = new PhoneProxy(new GSMPhone(context,
                            sCommandsInterface, sPhoneNotifier));
                    Log.i(LOG_TAG, "Creating GSMPhone");
                } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    sProxyPhone = new PhoneProxy(new CDMAPhone(context,
                            sCommandsInterface, sPhoneNotifier));
                    Log.i(LOG_TAG, "Creating CDMAPhone");
                }
                sMadeDefaults = true;
            }
        }
    }
    public static int getPhoneType(int networkMode) {
        switch(networkMode) {
        case RILConstants.NETWORK_MODE_CDMA:
        case RILConstants.NETWORK_MODE_CDMA_NO_EVDO:
        case RILConstants.NETWORK_MODE_EVDO_NO_CDMA:
            return Phone.PHONE_TYPE_CDMA;
        case RILConstants.NETWORK_MODE_WCDMA_PREF:
        case RILConstants.NETWORK_MODE_GSM_ONLY:
        case RILConstants.NETWORK_MODE_WCDMA_ONLY:
        case RILConstants.NETWORK_MODE_GSM_UMTS:
            return Phone.PHONE_TYPE_GSM;
        case RILConstants.NETWORK_MODE_GLOBAL:
            return Phone.PHONE_TYPE_CDMA;
        default:
            return Phone.PHONE_TYPE_GSM;
        }
    }
    public static Phone getDefaultPhone() {
        if (sLooper != Looper.myLooper()) {
            throw new RuntimeException(
                "PhoneFactory.getDefaultPhone must be called from Looper thread");
        }
        if (!sMadeDefaults) {
            throw new IllegalStateException("Default phones haven't been made yet!");
        }
       return sProxyPhone;
    }
    public static Phone getCdmaPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            Phone phone = new CDMAPhone(sContext, sCommandsInterface, sPhoneNotifier);
            return phone;
        }
    }
    public static Phone getGsmPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            Phone phone = new GSMPhone(sContext, sCommandsInterface, sPhoneNotifier);
            return phone;
        }
    }
}
