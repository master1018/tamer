public final class RuimFileHandler extends IccFileHandler {
    static final String LOG_TAG = "CDMA";
    RuimFileHandler(CDMAPhone phone) {
        super(phone);
    }
    public void dispose() {
    }
    protected void finalize() {
        Log.d(LOG_TAG, "RuimFileHandler finalized");
    }
    @Override
    public void loadEFImgTransparent(int fileid, int highOffset, int lowOffset,
            int length, Message onLoaded) {
        Message response = obtainMessage(EVENT_READ_ICON_DONE, fileid, 0,
                onLoaded);
        phone.mCM.iccIO(COMMAND_GET_RESPONSE, fileid, "img", 0, 0,
                GET_RESPONSE_EF_IMG_SIZE_BYTES, null, null, response);
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }
    protected String getEFPath(int efid) {
        switch(efid) {
        case EF_SMS:
        case EF_CST:
        case EF_RUIM_SPN:
            return MF_SIM + DF_CDMA;
        }
        return getCommonIccEFPath(efid);
    }
    protected void logd(String msg) {
        Log.d(LOG_TAG, "[RuimFileHandler] " + msg);
    }
    protected void loge(String msg) {
        Log.e(LOG_TAG, "[RuimFileHandler] " + msg);
    }
}
