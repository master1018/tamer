public class SendTransaction extends Transaction implements Runnable {
    private static final String TAG = "SendTransaction";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private Thread mThread;
    private final Uri mSendReqURI;
    public SendTransaction(Context context,
            int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        mSendReqURI = Uri.parse(uri);
        mId = uri;
        attach(RetryScheduler.getInstance(context));
    }
    @Override
    public void process() {
        mThread = new Thread(this);
        mThread.start();
    }
    public void run() {
        try {
            RateController rateCtlr = RateController.getInstance();
            if (rateCtlr.isLimitSurpassed() && !rateCtlr.isAllowedByUser()) {
                Log.e(TAG, "Sending rate limit surpassed.");
                return;
            }
            PduPersister persister = PduPersister.getPduPersister(mContext);
            SendReq sendReq = (SendReq) persister.load(mSendReqURI);
            long date = System.currentTimeMillis() / 1000L;
            sendReq.setDate(date);
            ContentValues values = new ContentValues(1);
            values.put(Mms.DATE, date);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mSendReqURI, values, null, null);
            String lineNumber = MessageUtils.getLocalNumber();
            if (!TextUtils.isEmpty(lineNumber)) {
                sendReq.setFrom(new EncodedStringValue(lineNumber));
            }
            long tokenKey = ContentUris.parseId(mSendReqURI);
            byte[] response = sendPdu(SendingProgressTokenManager.get(tokenKey),
                                      new PduComposer(mContext, sendReq).make());
            SendingProgressTokenManager.remove(tokenKey);
            if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
                String respStr = new String(response);
                Log.d(TAG, "[SendTransaction] run: send mms msg (" + mId + "), resp=" + respStr);
            }
            SendConf conf = (SendConf) new PduParser(response).parse();
            if (conf == null) {
                Log.e(TAG, "No M-Send.conf received.");
            }
            byte[] reqId = sendReq.getTransactionId();
            byte[] confId = conf.getTransactionId();
            if (!Arrays.equals(reqId, confId)) {
                Log.e(TAG, "Inconsistent Transaction-ID: req="
                        + new String(reqId) + ", conf=" + new String(confId));
                return;
            }
            values = new ContentValues(2);
            int respStatus = conf.getResponseStatus();
            values.put(Mms.RESPONSE_STATUS, respStatus);
            if (respStatus != PduHeaders.RESPONSE_STATUS_OK) {
                SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                     mSendReqURI, values, null, null);
                Log.e(TAG, "Server returned an error code: " + respStatus);
                return;
            }
            String messageId = PduPersister.toIsoString(conf.getMessageId());
            values.put(Mms.MESSAGE_ID, messageId);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                                 mSendReqURI, values, null, null);
            Uri uri = persister.move(mSendReqURI, Sent.CONTENT_URI);
            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (Throwable t) {
            Log.e(TAG, Log.getStackTraceString(t));
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mSendReqURI);
                Log.e(TAG, "Delivery failed.");
            }
            notifyObservers();
        }
    }
    @Override
    public int getType() {
        return SEND_TRANSACTION;
    }
}
