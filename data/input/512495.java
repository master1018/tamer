public class DefaultPhoneNotifier implements PhoneNotifier {
    static final String LOG_TAG = "GSM";
    private static final boolean DBG = true;
    private ITelephonyRegistry mRegistry;
    DefaultPhoneNotifier() {
        mRegistry = ITelephonyRegistry.Stub.asInterface(ServiceManager.getService(
                    "telephony.registry"));
    }
    public void notifyPhoneState(Phone sender) {
        Call ringingCall = sender.getRingingCall();
        String incomingNumber = "";
        if (ringingCall != null && ringingCall.getEarliestConnection() != null){
            incomingNumber = ringingCall.getEarliestConnection().getAddress();
        }
        try {
            mRegistry.notifyCallState(convertCallState(sender.getState()), incomingNumber);
        } catch (RemoteException ex) {
        }
    }
    public void notifyServiceState(Phone sender) {
        try {
            mRegistry.notifyServiceState(sender.getServiceState());
        } catch (RemoteException ex) {
        }
    }
    public void notifySignalStrength(Phone sender) {
        try {
            mRegistry.notifySignalStrength(sender.getSignalStrength());
        } catch (RemoteException ex) {
        }
    }
    public void notifyMessageWaitingChanged(Phone sender) {
        try {
            mRegistry.notifyMessageWaitingChanged(sender.getMessageWaitingIndicator());
        } catch (RemoteException ex) {
        }
    }
    public void notifyCallForwardingChanged(Phone sender) {
        try {
            mRegistry.notifyCallForwardingChanged(sender.getCallForwardingIndicator());
        } catch (RemoteException ex) {
        }
    }
    public void notifyDataActivity(Phone sender) {
        try {
            mRegistry.notifyDataActivity(convertDataActivityState(sender.getDataActivityState()));
        } catch (RemoteException ex) {
        }
    }
    public void notifyDataConnection(Phone sender, String reason) {
        TelephonyManager telephony = TelephonyManager.getDefault();
        try {
            mRegistry.notifyDataConnection(
                    convertDataState(sender.getDataConnectionState()),
                    sender.isDataConnectivityPossible(), reason,
                    sender.getActiveApn(),
                    sender.getActiveApnTypes(),
                    sender.getInterfaceName(null),
                    ((telephony!=null) ? telephony.getNetworkType() :
                    TelephonyManager.NETWORK_TYPE_UNKNOWN));
        } catch (RemoteException ex) {
        }
    }
    public void notifyDataConnectionFailed(Phone sender, String reason) {
        try {
            mRegistry.notifyDataConnectionFailed(reason);
        } catch (RemoteException ex) {
        }
    }
    public void notifyCellLocation(Phone sender) {
        Bundle data = new Bundle();
        sender.getCellLocation().fillInNotifierBundle(data);
        try {
            mRegistry.notifyCellLocation(data);
        } catch (RemoteException ex) {
        }
    }
    private void log(String s) {
        Log.d(LOG_TAG, "[PhoneNotifier] " + s);
    }
    public static int convertCallState(Phone.State state) {
        switch (state) {
            case RINGING:
                return TelephonyManager.CALL_STATE_RINGING;
            case OFFHOOK:
                return TelephonyManager.CALL_STATE_OFFHOOK;
            default:
                return TelephonyManager.CALL_STATE_IDLE;
        }
    }
    public static Phone.State convertCallState(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                return Phone.State.RINGING;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return Phone.State.OFFHOOK;
            default:
                return Phone.State.IDLE;
        }
    }
    public static int convertDataState(Phone.DataState state) {
        switch (state) {
            case CONNECTING:
                return TelephonyManager.DATA_CONNECTING;
            case CONNECTED:
                return TelephonyManager.DATA_CONNECTED;
            case SUSPENDED:
                return TelephonyManager.DATA_SUSPENDED;
            default:
                return TelephonyManager.DATA_DISCONNECTED;
        }
    }
    public static Phone.DataState convertDataState(int state) {
        switch (state) {
            case TelephonyManager.DATA_CONNECTING:
                return Phone.DataState.CONNECTING;
            case TelephonyManager.DATA_CONNECTED:
                return Phone.DataState.CONNECTED;
            case TelephonyManager.DATA_SUSPENDED:
                return Phone.DataState.SUSPENDED;
            default:
                return Phone.DataState.DISCONNECTED;
        }
    }
    public static int convertDataActivityState(Phone.DataActivityState state) {
        switch (state) {
            case DATAIN:
                return TelephonyManager.DATA_ACTIVITY_IN;
            case DATAOUT:
                return TelephonyManager.DATA_ACTIVITY_OUT;
            case DATAINANDOUT:
                return TelephonyManager.DATA_ACTIVITY_INOUT;
            case DORMANT:
                return TelephonyManager.DATA_ACTIVITY_DORMANT;
            default:
                return TelephonyManager.DATA_ACTIVITY_NONE;
        }
    }
    public static Phone.DataActivityState convertDataActivityState(int state) {
        switch (state) {
            case TelephonyManager.DATA_ACTIVITY_IN:
                return Phone.DataActivityState.DATAIN;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return Phone.DataActivityState.DATAOUT;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return Phone.DataActivityState.DATAINANDOUT;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                return Phone.DataActivityState.DORMANT;
            default:
                return Phone.DataActivityState.NONE;
        }
    }
}
