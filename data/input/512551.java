public class RuimPhoneBookInterfaceManager extends IccPhoneBookInterfaceManager {
    static final String LOG_TAG = "CDMA";
    public RuimPhoneBookInterfaceManager(CDMAPhone phone) {
        super(phone);
        adnCache = phone.mRuimRecords.getAdnCache();
    }
    public void dispose() {
        super.dispose();
    }
    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable throwable) {
            Log.e(LOG_TAG, "Error while finalizing:", throwable);
        }
        if(DBG) Log.d(LOG_TAG, "RuimPhoneBookInterfaceManager finalized");
    }
    public int[] getAdnRecordsSize(int efid) {
        if (DBG) logd("getAdnRecordsSize: efid=" + efid);
        synchronized(mLock) {
            checkThread();
            recordSize = new int[3];
            Message response = mBaseHandler.obtainMessage(EVENT_GET_SIZE_DONE);
            phone.getIccFileHandler().getEFLinearRecordSize(efid, response);
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                logd("interrupted while trying to load from the RUIM");
            }
        }
        return recordSize;
    }
    protected void logd(String msg) {
        Log.d(LOG_TAG, "[RuimPbInterfaceManager] " + msg);
    }
    protected void loge(String msg) {
        Log.e(LOG_TAG, "[RuimPbInterfaceManager] " + msg);
    }
}
