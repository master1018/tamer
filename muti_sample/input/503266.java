public class MmsMessageSender implements MessageSender {
    private static final String TAG = "MmsMessageSender";
    private final Context mContext;
    private final Uri mMessageUri;
    private final long mMessageSize;
    private static final boolean DEFAULT_DELIVERY_REPORT_MODE  = false;
    private static final boolean DEFAULT_READ_REPORT_MODE      = false;
    private static final long    DEFAULT_EXPIRY_TIME     = 7 * 24 * 60 * 60;
    private static final int     DEFAULT_PRIORITY        = PduHeaders.PRIORITY_NORMAL;
    private static final String  DEFAULT_MESSAGE_CLASS   = PduHeaders.MESSAGE_CLASS_PERSONAL_STR;
    public MmsMessageSender(Context context, Uri location, long messageSize) {
        mContext = context;
        mMessageUri = location;
        mMessageSize = messageSize;
        if (mMessageUri == null) {
            throw new IllegalArgumentException("Null message URI.");
        }
    }
    public boolean sendMessage(long token) throws MmsException {
        PduPersister p = PduPersister.getPduPersister(mContext);
        GenericPdu pdu = p.load(mMessageUri);
        if (pdu.getMessageType() != PduHeaders.MESSAGE_TYPE_SEND_REQ) {
            throw new MmsException("Invalid message: " + pdu.getMessageType());
        }
        SendReq sendReq = (SendReq) pdu;
        updatePreferencesHeaders(sendReq);
        sendReq.setMessageClass(DEFAULT_MESSAGE_CLASS.getBytes());
        sendReq.setDate(System.currentTimeMillis() / 1000L);
        sendReq.setMessageSize(mMessageSize);
        p.updateHeaders(mMessageUri, sendReq);
        p.move(mMessageUri, Mms.Outbox.CONTENT_URI);
        SendingProgressTokenManager.put(ContentUris.parseId(mMessageUri), token);
        mContext.startService(new Intent(mContext, TransactionService.class));
        return true;
    }
    private void updatePreferencesHeaders(SendReq sendReq) throws MmsException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        sendReq.setExpiry(prefs.getLong(
                MessagingPreferenceActivity.EXPIRY_TIME, DEFAULT_EXPIRY_TIME));
        sendReq.setPriority(prefs.getInt(MessagingPreferenceActivity.PRIORITY, DEFAULT_PRIORITY));
        boolean dr = prefs.getBoolean(MessagingPreferenceActivity.MMS_DELIVERY_REPORT_MODE,
                DEFAULT_DELIVERY_REPORT_MODE);
        sendReq.setDeliveryReport(dr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);
        boolean rr = prefs.getBoolean(MessagingPreferenceActivity.READ_REPORT_MODE,
                DEFAULT_READ_REPORT_MODE);
        sendReq.setReadReport(rr?PduHeaders.VALUE_YES:PduHeaders.VALUE_NO);
    }
    public static void sendReadRec(Context context, String to, String messageId, int status) {
        EncodedStringValue[] sender = new EncodedStringValue[1];
        sender[0] = new EncodedStringValue(to);
        try {
            final ReadRecInd readRec = new ReadRecInd(
                    new EncodedStringValue(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR.getBytes()),
                    messageId.getBytes(),
                    PduHeaders.CURRENT_MMS_VERSION,
                    status,
                    sender);
            readRec.setDate(System.currentTimeMillis() / 1000);
            PduPersister.getPduPersister(context).persist(readRec, Mms.Outbox.CONTENT_URI);
            context.startService(new Intent(context, TransactionService.class));
        } catch (InvalidHeaderValueException e) {
            Log.e(TAG, "Invalide header value", e);
        } catch (MmsException e) {
            Log.e(TAG, "Persist message failed", e);
        }
    }
}
