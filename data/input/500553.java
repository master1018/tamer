@Deprecated public final class SmsManager {
    private static SmsManager sInstance;
    private android.telephony.SmsManager mSmsMgrProxy;
    @Deprecated
    public static final SmsManager getDefault() {
        if (sInstance == null) {
            sInstance = new SmsManager();
        }
        return sInstance;
    }
    @Deprecated
    private SmsManager() {
        mSmsMgrProxy = android.telephony.SmsManager.getDefault();
    }
    @Deprecated
    public final void sendTextMessage(
            String destinationAddress, String scAddress, String text,
            PendingIntent sentIntent, PendingIntent deliveryIntent) {
        mSmsMgrProxy.sendTextMessage(destinationAddress, scAddress, text,
                sentIntent, deliveryIntent);
    }
    @Deprecated
    public final ArrayList<String> divideMessage(String text) {
        return mSmsMgrProxy.divideMessage(text);
    }
    @Deprecated
    public final void sendMultipartTextMessage(
            String destinationAddress, String scAddress, ArrayList<String> parts,
            ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        mSmsMgrProxy.sendMultipartTextMessage(destinationAddress, scAddress, parts,
                sentIntents, deliveryIntents);
    }
    @Deprecated
    public final void sendDataMessage(
            String destinationAddress, String scAddress, short destinationPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        mSmsMgrProxy.sendDataMessage(destinationAddress, scAddress, destinationPort,
                data, sentIntent, deliveryIntent);
    }
    @Deprecated
    public final boolean copyMessageToSim(byte[] smsc, byte[] pdu, int status) {
        return mSmsMgrProxy.copyMessageToIcc(smsc, pdu, status);
    }
    @Deprecated
    public final boolean deleteMessageFromSim(int messageIndex) {
        return mSmsMgrProxy.deleteMessageFromIcc(messageIndex);
    }
    @Deprecated
    public final boolean updateMessageOnSim(int messageIndex, int newStatus, byte[] pdu) {
        return mSmsMgrProxy.updateMessageOnIcc(messageIndex, newStatus, pdu);
    }
    @Deprecated
    public final ArrayList<android.telephony.SmsMessage> getAllMessagesFromSim() {
        return mSmsMgrProxy.getAllMessagesFromIcc();
    }
    @Deprecated static public final int STATUS_ON_SIM_FREE      = 0;
    @Deprecated static public final int STATUS_ON_SIM_READ      = 1;
    @Deprecated static public final int STATUS_ON_SIM_UNREAD    = 3;
    @Deprecated static public final int STATUS_ON_SIM_SENT      = 5;
    @Deprecated static public final int STATUS_ON_SIM_UNSENT    = 7;
    @Deprecated static public final int RESULT_ERROR_GENERIC_FAILURE    = 1;
    @Deprecated static public final int RESULT_ERROR_RADIO_OFF          = 2;
    @Deprecated static public final int RESULT_ERROR_NULL_PDU           = 3;
    @Deprecated static public final int RESULT_ERROR_NO_SERVICE         = 4;
}
