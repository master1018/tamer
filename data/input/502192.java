public class ReadRecTransaction extends Transaction {
    private static final String TAG = "ReadRecTransaction";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private final Uri mReadReportURI;
    public ReadRecTransaction(Context context,
            int transId,
            TransactionSettings connectionSettings,
            String uri) {
        super(context, transId, connectionSettings);
        mReadReportURI = Uri.parse(uri);
        mId = uri;
        attach(RetryScheduler.getInstance(context));
    }
    @Override
    public void process() {
        PduPersister persister = PduPersister.getPduPersister(mContext);
        try {
            ReadRecInd readRecInd = (ReadRecInd) persister.load(mReadReportURI);
            String lineNumber = MessageUtils.getLocalNumber();
            readRecInd.setFrom(new EncodedStringValue(lineNumber));
            byte[] postingData = new PduComposer(mContext, readRecInd).make();
            sendPdu(postingData);
            Uri uri = persister.move(mReadReportURI, Sent.CONTENT_URI);
            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (IOException e) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed to send M-Read-Rec.Ind.", e);
            }
        } catch (MmsException e) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed to load message from Outbox.", e);
            }
        } catch (RuntimeException e) {
            if (LOCAL_LOGV) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mReadReportURI);
            }
            notifyObservers();
        }
    }
    @Override
    public int getType() {
        return READREC_TRANSACTION;
    }
}
