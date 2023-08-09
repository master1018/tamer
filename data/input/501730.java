public class TelephonyManager {
    private static final String TAG = "TelephonyManager";
    private Context mContext;
    private ITelephonyRegistry mRegistry;
    public TelephonyManager(Context context) {
        mContext = context;
        mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
    }
    private TelephonyManager() {
    }
    private static TelephonyManager sInstance = new TelephonyManager();
    public static TelephonyManager getDefault() {
        return sInstance;
    }
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_PHONE_STATE_CHANGED =
            "android.intent.action.PHONE_STATE";
    public static final String EXTRA_STATE = Phone.STATE_KEY;
    public static final String EXTRA_STATE_IDLE = Phone.State.IDLE.toString();
    public static final String EXTRA_STATE_RINGING = Phone.State.RINGING.toString();
    public static final String EXTRA_STATE_OFFHOOK = Phone.State.OFFHOOK.toString();
    public static final String EXTRA_INCOMING_NUMBER = "incoming_number";
    public String getDeviceSoftwareVersion() {
        try {
            return getSubscriberInfo().getDeviceSvn();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public String getDeviceId() {
        try {
            return getSubscriberInfo().getDeviceId();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public CellLocation getCellLocation() {
        try {
            Bundle bundle = getITelephony().getCellLocation();
            CellLocation cl = CellLocation.newFromBundle(bundle);
            if (cl.isEmpty())
                return null;
            return cl;
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public void enableLocationUpdates() {
        try {
            getITelephony().enableLocationUpdates();
        } catch (RemoteException ex) {
        } catch (NullPointerException ex) {
        }
    }
    public void disableLocationUpdates() {
        try {
            getITelephony().disableLocationUpdates();
        } catch (RemoteException ex) {
        } catch (NullPointerException ex) {
        }
    }
    public List<NeighboringCellInfo> getNeighboringCellInfo() {
        try {
            return getITelephony().getNeighboringCellInfo();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public static final int PHONE_TYPE_NONE = Phone.PHONE_TYPE_NONE;
    public static final int PHONE_TYPE_GSM = Phone.PHONE_TYPE_GSM;
    public static final int PHONE_TYPE_CDMA = Phone.PHONE_TYPE_CDMA;
    public int getPhoneType() {
        try{
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getActivePhoneType();
            } else {
                return getPhoneTypeFromProperty();
            }
        } catch (RemoteException ex) {
            return getPhoneTypeFromProperty();
        } catch (NullPointerException ex) {
            return getPhoneTypeFromProperty();
        }
    }
    private int getPhoneTypeFromProperty() {
        int type =
            SystemProperties.getInt(TelephonyProperties.CURRENT_ACTIVE_PHONE,
                    getPhoneTypeFromNetworkType());
        return type;
    }
    private int getPhoneTypeFromNetworkType() {
        int mode = SystemProperties.getInt("ro.telephony.default_network", -1);
        if (mode == -1)
            return PHONE_TYPE_NONE;
        return PhoneFactory.getPhoneType(mode);
    }
    public String getNetworkOperatorName() {
        return SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ALPHA);
    }
    public String getNetworkOperator() {
        return SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_NUMERIC);
    }
    public boolean isNetworkRoaming() {
        return "true".equals(SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ISROAMING));
    }
    public String getNetworkCountryIso() {
        return SystemProperties.get(TelephonyProperties.PROPERTY_OPERATOR_ISO_COUNTRY);
    }
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final int NETWORK_TYPE_GPRS = 1;
    public static final int NETWORK_TYPE_EDGE = 2;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_CDMA = 4;
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    public static final int NETWORK_TYPE_EVDO_A = 6;
    public static final int NETWORK_TYPE_1xRTT = 7;
    public static final int NETWORK_TYPE_HSDPA = 8;
    public static final int NETWORK_TYPE_HSUPA = 9;
    public static final int NETWORK_TYPE_HSPA = 10;
    public static final int NETWORK_TYPE_IDEN = 11;
    public int getNetworkType() {
        try{
            ITelephony telephony = getITelephony();
            if (telephony != null) {
                return telephony.getNetworkType();
            } else {
                return NETWORK_TYPE_UNKNOWN;
            }
        } catch(RemoteException ex) {
            return NETWORK_TYPE_UNKNOWN;
        } catch (NullPointerException ex) {
            return NETWORK_TYPE_UNKNOWN;
        }
    }
    public String getNetworkTypeName() {
        switch (getNetworkType()) {
            case NETWORK_TYPE_GPRS:
                return "GPRS";
            case NETWORK_TYPE_EDGE:
                return "EDGE";
            case NETWORK_TYPE_UMTS:
                return "UMTS";
            case NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case NETWORK_TYPE_HSPA:
                return "HSPA";
            case NETWORK_TYPE_CDMA:
                return "CDMA";
            case NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            default:
                return "UNKNOWN";
        }
    }
    public static final int SIM_STATE_UNKNOWN = 0;
    public static final int SIM_STATE_ABSENT = 1;
    public static final int SIM_STATE_PIN_REQUIRED = 2;
    public static final int SIM_STATE_PUK_REQUIRED = 3;
    public static final int SIM_STATE_NETWORK_LOCKED = 4;
    public static final int SIM_STATE_READY = 5;
    public boolean hasIccCard() {
        try {
            return getITelephony().hasIccCard();
        } catch (RemoteException ex) {
            return false;
        } catch (NullPointerException ex) {
            return false;
        }
    }
    public int getSimState() {
        String prop = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);
        if ("ABSENT".equals(prop)) {
            return SIM_STATE_ABSENT;
        }
        else if ("PIN_REQUIRED".equals(prop)) {
            return SIM_STATE_PIN_REQUIRED;
        }
        else if ("PUK_REQUIRED".equals(prop)) {
            return SIM_STATE_PUK_REQUIRED;
        }
        else if ("NETWORK_LOCKED".equals(prop)) {
            return SIM_STATE_NETWORK_LOCKED;
        }
        else if ("READY".equals(prop)) {
            return SIM_STATE_READY;
        }
        else {
            return SIM_STATE_UNKNOWN;
        }
    }
    public String getSimOperator() {
        return SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
    }
    public String getSimOperatorName() {
        return SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ALPHA);
    }
    public String getSimCountryIso() {
        return SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_ISO_COUNTRY);
    }
    public String getSimSerialNumber() {
        try {
            return getSubscriberInfo().getIccSerialNumber();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public String getSubscriberId() {
        try {
            return getSubscriberInfo().getSubscriberId();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public String getLine1Number() {
        try {
            return getSubscriberInfo().getLine1Number();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public String getLine1AlphaTag() {
        try {
            return getSubscriberInfo().getLine1AlphaTag();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public String getVoiceMailNumber() {
        try {
            return getSubscriberInfo().getVoiceMailNumber();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public String getCompleteVoiceMailNumber() {
        try {
            return getSubscriberInfo().getCompleteVoiceMailNumber();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    public int getVoiceMessageCount() {
        try {
            return getITelephony().getVoiceMessageCount();
        } catch (RemoteException ex) {
            return 0;
        } catch (NullPointerException ex) {
            return 0;
        }
    }
    public String getVoiceMailAlphaTag() {
        try {
            return getSubscriberInfo().getVoiceMailAlphaTag();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
    private IPhoneSubInfo getSubscriberInfo() {
        return IPhoneSubInfo.Stub.asInterface(ServiceManager.getService("iphonesubinfo"));
    }
    public static final int CALL_STATE_IDLE = 0;
    public static final int CALL_STATE_RINGING = 1;
    public static final int CALL_STATE_OFFHOOK = 2;
    public int getCallState() {
        try {
            return getITelephony().getCallState();
        } catch (RemoteException ex) {
            return CALL_STATE_IDLE;
        } catch (NullPointerException ex) {
          return CALL_STATE_IDLE;
      }
    }
    public static final int DATA_ACTIVITY_NONE = 0x00000000;
    public static final int DATA_ACTIVITY_IN = 0x00000001;
    public static final int DATA_ACTIVITY_OUT = 0x00000002;
    public static final int DATA_ACTIVITY_INOUT = DATA_ACTIVITY_IN | DATA_ACTIVITY_OUT;
    public static final int DATA_ACTIVITY_DORMANT = 0x00000004;
    public int getDataActivity() {
        try {
            return getITelephony().getDataActivity();
        } catch (RemoteException ex) {
            return DATA_ACTIVITY_NONE;
        } catch (NullPointerException ex) {
          return DATA_ACTIVITY_NONE;
      }
    }
    public static final int DATA_DISCONNECTED   = 0;
    public static final int DATA_CONNECTING     = 1;
    public static final int DATA_CONNECTED      = 2;
    public static final int DATA_SUSPENDED      = 3;
    public int getDataState() {
        try {
            return getITelephony().getDataState();
        } catch (RemoteException ex) {
            return DATA_DISCONNECTED;
        } catch (NullPointerException ex) {
            return DATA_DISCONNECTED;
        }
    }
    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
    }
    public void listen(PhoneStateListener listener, int events) {
        String pkgForDebug = mContext != null ? mContext.getPackageName() : "<unknown>";
        try {
            Boolean notifyNow = (getITelephony() != null);
            mRegistry.listen(pkgForDebug, listener.callback, events, notifyNow);
        } catch (RemoteException ex) {
        } catch (NullPointerException ex) {
        }
    }
    public int getCdmaEriIconIndex() {
        try {
            return getITelephony().getCdmaEriIconIndex();
        } catch (RemoteException ex) {
            return -1;
        } catch (NullPointerException ex) {
            return -1;
        }
    }
    public int getCdmaEriIconMode() {
        try {
            return getITelephony().getCdmaEriIconMode();
        } catch (RemoteException ex) {
            return -1;
        } catch (NullPointerException ex) {
            return -1;
        }
    }
    public String getCdmaEriText() {
        try {
            return getITelephony().getCdmaEriText();
        } catch (RemoteException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }
}
