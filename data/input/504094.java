public final class SIMFileHandler extends IccFileHandler implements IccConstants {
    static final String LOG_TAG = "GSM";
    private Phone mPhone;
    SIMFileHandler(GSMPhone phone) {
        super(phone);
        mPhone = phone;
    }
    public void dispose() {
        super.dispose();
    }
    protected void finalize() {
        Log.d(LOG_TAG, "SIMFileHandler finalized");
    }
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }
    protected String getEFPath(int efid) {
        switch(efid) {
        case EF_SMS:
            return MF_SIM + DF_TELECOM;
        case EF_EXT6:
        case EF_MWIS:
        case EF_MBI:
        case EF_SPN:
        case EF_AD:
        case EF_MBDN:
        case EF_PNN:
        case EF_SPDI:
        case EF_SST:
        case EF_CFIS:
            return MF_SIM + DF_GSM;
        case EF_MAILBOX_CPHS:
        case EF_VOICE_MAIL_INDICATOR_CPHS:
        case EF_CFF_CPHS:
        case EF_SPN_CPHS:
        case EF_SPN_SHORT_CPHS:
        case EF_INFO_CPHS:
            return MF_SIM + DF_GSM;
        case EF_PBR:
            return MF_SIM + DF_TELECOM + DF_PHONEBOOK;
        }
        String path = getCommonIccEFPath(efid);
        if (path == null) {
            IccCard card = phone.getIccCard();
            if (card != null && card.isApplicationOnIcc(IccCardApplication.AppType.APPTYPE_USIM)) {
                return MF_SIM + DF_TELECOM + DF_PHONEBOOK;
            }
            Log.e(LOG_TAG, "Error: EF Path being returned in null");
        }
        return path;
    }
    protected void logd(String msg) {
        Log.d(LOG_TAG, "[SIMFileHandler] " + msg);
    }
    protected void loge(String msg) {
        Log.e(LOG_TAG, "[SIMFileHandler] " + msg);
    }
}
