public final class RuimRecords extends IccRecords {
    static final String LOG_TAG = "CDMA";
    private static final boolean DBG = true;
    private boolean  m_ota_commited=false;
    private String mImsi;
    private String mMyMobileNumber;
    private String mMin2Min1;
    private String mPrlVersion;
    private static final int EVENT_RUIM_READY = 1;
    private static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 2;
    private static final int EVENT_GET_DEVICE_IDENTITY_DONE = 4;
    private static final int EVENT_GET_ICCID_DONE = 5;
    private static final int EVENT_GET_CDMA_SUBSCRIPTION_DONE = 10;
    private static final int EVENT_UPDATE_DONE = 14;
    private static final int EVENT_GET_SST_DONE = 17;
    private static final int EVENT_GET_ALL_SMS_DONE = 18;
    private static final int EVENT_MARK_SMS_READ_DONE = 19;
    private static final int EVENT_SMS_ON_RUIM = 21;
    private static final int EVENT_GET_SMS_DONE = 22;
    private static final int EVENT_RUIM_REFRESH = 31;
    RuimRecords(CDMAPhone p) {
        super(p);
        adnCache = new AdnRecordCache(phone);
        recordsRequested = false;  
        recordsToLoad = 0;
        p.mCM.registerForRUIMReady(this, EVENT_RUIM_READY, null);
        p.mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        p.mCM.setOnIccRefresh(this, EVENT_RUIM_REFRESH, null);
        onRadioOffOrNotAvailable();
    }
    public void dispose() {
        phone.mCM.unregisterForRUIMReady(this);
        phone.mCM.unregisterForOffOrNotAvailable( this);
        phone.mCM.unSetOnIccRefresh(this);
    }
    @Override
    protected void finalize() {
        if(DBG) Log.d(LOG_TAG, "RuimRecords finalized");
    }
    @Override
    protected void onRadioOffOrNotAvailable() {
        countVoiceMessages = 0;
        mncLength = UNINITIALIZED;
        iccid = null;
        adnCache.reset();
        recordsRequested = false;
    }
    public String getMdnNumber() {
        return mMyMobileNumber;
    }
    public String getCdmaMin() {
         return mMin2Min1;
    }
    public String getPrlVersion() {
        return mPrlVersion;
    }
    @Override
    public void setVoiceMailNumber(String alphaTag, String voiceNumber, Message onComplete){
        AsyncResult.forMessage((onComplete)).exception =
                new IccException("setVoiceMailNumber not implemented");
        onComplete.sendToTarget();
        Log.e(LOG_TAG, "method setVoiceMailNumber is not implemented");
    }
    @Override
    public void onRefresh(boolean fileChanged, int[] fileList) {
        if (fileChanged) {
            fetchRuimRecords();
        }
    }
    public String getRUIMOperatorNumeric() {
        if (mImsi == null) {
            return null;
        }
        if (mncLength != UNINITIALIZED && mncLength != UNKNOWN) {
            return mImsi.substring(0, 3 + mncLength);
        }
        int mcc = Integer.parseInt(mImsi.substring(0,3));
        return mImsi.substring(0, 3 + MccTable.smallestDigitsMccForMnc(mcc));
    }
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        byte data[];
        boolean isRecordLoadResponse = false;
        try { switch (msg.what) {
            case EVENT_RUIM_READY:
                onRuimReady();
            break;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:
                onRadioOffOrNotAvailable();
            break;
            case EVENT_GET_DEVICE_IDENTITY_DONE:
                Log.d(LOG_TAG, "Event EVENT_GET_DEVICE_IDENTITY_DONE Received");
            break;
            case EVENT_GET_CDMA_SUBSCRIPTION_DONE:
                ar = (AsyncResult)msg.obj;
                String localTemp[] = (String[])ar.result;
                if (ar.exception != null) {
                    break;
                }
                mMyMobileNumber = localTemp[0];
                mMin2Min1 = localTemp[3];
                mPrlVersion = localTemp[4];
                Log.d(LOG_TAG, "MDN: " + mMyMobileNumber + " MIN: " + mMin2Min1);
            break;
            case EVENT_GET_ICCID_DONE:
                isRecordLoadResponse = true;
                ar = (AsyncResult)msg.obj;
                data = (byte[])ar.result;
                if (ar.exception != null) {
                    break;
                }
                iccid = IccUtils.bcdToString(data, 0, data.length);
                Log.d(LOG_TAG, "iccid: " + iccid);
            break;
            case EVENT_UPDATE_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    Log.i(LOG_TAG, "RuimRecords update failed", ar.exception);
                }
            break;
            case EVENT_GET_ALL_SMS_DONE:
            case EVENT_MARK_SMS_READ_DONE:
            case EVENT_SMS_ON_RUIM:
            case EVENT_GET_SMS_DONE:
                Log.w(LOG_TAG, "Event not supported: " + msg.what);
                break;
            case EVENT_GET_SST_DONE:
                Log.d(LOG_TAG, "Event EVENT_GET_SST_DONE Received");
            break;
            case EVENT_RUIM_REFRESH:
                isRecordLoadResponse = false;
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    handleRuimRefresh((int[])(ar.result));
                }
                break;
        }}catch (RuntimeException exc) {
            Log.w(LOG_TAG, "Exception parsing RUIM record", exc);
        } finally {
            if (isRecordLoadResponse) {
                onRecordLoaded();
            }
        }
    }
    @Override
    protected void onRecordLoaded() {
        recordsToLoad -= 1;
        if (recordsToLoad == 0 && recordsRequested == true) {
            onAllRecordsLoaded();
        } else if (recordsToLoad < 0) {
            Log.e(LOG_TAG, "RuimRecords: recordsToLoad <0, programmer error suspected");
            recordsToLoad = 0;
        }
    }
    @Override
    protected void onAllRecordsLoaded() {
        Log.d(LOG_TAG, "RuimRecords: record load complete");
        recordsLoadedRegistrants.notifyRegistrants(
            new AsyncResult(null, null, null));
        ((CDMAPhone) phone).mRuimCard.broadcastIccStateChangedIntent(
                RuimCard.INTENT_VALUE_ICC_LOADED, null);
    }
    private void onRuimReady() {
        ((CDMAPhone) phone).mRuimCard.broadcastIccStateChangedIntent(
                RuimCard.INTENT_VALUE_ICC_READY, null);
        fetchRuimRecords();
        phone.mCM.getCDMASubscription(obtainMessage(EVENT_GET_CDMA_SUBSCRIPTION_DONE));
    }
    private void fetchRuimRecords() {
        recordsRequested = true;
        Log.v(LOG_TAG, "RuimRecords:fetchRuimRecords " + recordsToLoad);
        phone.getIccFileHandler().loadEFTransparent(EF_ICCID,
                obtainMessage(EVENT_GET_ICCID_DONE));
        recordsToLoad++;
    }
    @Override
    protected int getDisplayRule(String plmn) {
        return 0;
    }
    @Override
    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (line != 1) {
            return;
        }
        if (countWaiting < 0) {
            countWaiting = -1;
        } else if (countWaiting > 0xff) {
            countWaiting = 0xff;
        }
        countVoiceMessages = countWaiting;
        ((CDMAPhone) phone).notifyMessageWaitingIndicator();
    }
    private void handleRuimRefresh(int[] result) {
        if (result == null || result.length == 0) {
            if (DBG) log("handleRuimRefresh without input");
            return;
        }
        switch ((result[0])) {
            case CommandsInterface.SIM_REFRESH_FILE_UPDATED:
                if (DBG) log("handleRuimRefresh with SIM_REFRESH_FILE_UPDATED");
                adnCache.reset();
                fetchRuimRecords();
                break;
            case CommandsInterface.SIM_REFRESH_INIT:
                if (DBG) log("handleRuimRefresh with SIM_REFRESH_INIT");
                fetchRuimRecords();
                break;
            case CommandsInterface.SIM_REFRESH_RESET:
                if (DBG) log("handleRuimRefresh with SIM_REFRESH_RESET");
                phone.mCM.setRadioPower(false, null);
                break;
            default:
                if (DBG) log("handleRuimRefresh with unknown operation");
                break;
        }
    }
    @Override
    protected void log(String s) {
        Log.d(LOG_TAG, "[RuimRecords] " + s);
    }
}
