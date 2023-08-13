public abstract class IccRecords extends Handler implements IccConstants {
    protected static final boolean DBG = true;
    protected PhoneBase phone;
    protected RegistrantList recordsLoadedRegistrants = new RegistrantList();
    protected int recordsToLoad;  
    protected AdnRecordCache adnCache;
    protected boolean recordsRequested = false; 
    public String iccid;
    protected String msisdn = null;  
    protected String msisdnTag = null;
    protected String voiceMailNum = null;
    protected String voiceMailTag = null;
    protected String newVoiceMailNum = null;
    protected String newVoiceMailTag = null;
    protected boolean isVoiceMailFixed = false;
    protected int countVoiceMessages = 0;
    protected int mncLength = UNINITIALIZED;
    protected int mailboxIndex = 0; 
    protected String spn;
    protected int spnDisplayCondition;
    protected static final int UNINITIALIZED = -1;
    protected static final int UNKNOWN = 0;
    protected static final int SPN_RULE_SHOW_SPN  = 0x01;
    protected static final int SPN_RULE_SHOW_PLMN = 0x02;
    protected static final int EVENT_SET_MSISDN_DONE = 30;
    public IccRecords(PhoneBase p) {
        this.phone = p;
    }
    protected abstract void onRadioOffOrNotAvailable();
    public AdnRecordCache getAdnCache() {
        return adnCache;
    }
    public void registerForRecordsLoaded(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        recordsLoadedRegistrants.add(r);
        if (recordsToLoad == 0 && recordsRequested == true) {
            r.notifyRegistrant(new AsyncResult(null, null, null));
        }
    }
    public void unregisterForRecordsLoaded(Handler h) {
        recordsLoadedRegistrants.remove(h);
    }
    public String getMsisdnNumber() {
        return msisdn;
    }
    public void setMsisdnNumber(String alphaTag, String number,
            Message onComplete) {
        msisdn = number;
        msisdnTag = alphaTag;
        if(DBG) log("Set MSISDN: " + msisdnTag +" " + msisdn);
        AdnRecord adn = new AdnRecord(msisdnTag, msisdn);
        new AdnRecordLoader(phone).updateEF(adn, EF_MSISDN, EF_EXT1, 1, null,
                obtainMessage(EVENT_SET_MSISDN_DONE, onComplete));
    }
    public String getMsisdnAlphaTag() {
        return msisdnTag;
    }
    public String getVoiceMailNumber() {
        return voiceMailNum;
    }
    public String getServiceProviderName() {
        return spn;
    }
    public abstract void setVoiceMailNumber(String alphaTag, String voiceNumber,
            Message onComplete);
    public String getVoiceMailAlphaTag() {
        return voiceMailTag;
    }
    public abstract void setVoiceMessageWaiting(int line, int countWaiting);
    public boolean getVoiceMessageWaiting() {
        return countVoiceMessages != 0;
    }
    public int getVoiceMessageCount() {
        return countVoiceMessages;
    }
    public abstract void onRefresh(boolean fileChanged, int[] fileList);
    public boolean getRecordsLoaded() {
        if (recordsToLoad == 0 && recordsRequested == true) {
            return true;
        } else {
            return false;
        }
    }
    public abstract void handleMessage(Message msg);
    protected abstract void onRecordLoaded();
    protected abstract void onAllRecordsLoaded();
    protected abstract int getDisplayRule(String plmn);
    protected abstract void log(String s);
}
