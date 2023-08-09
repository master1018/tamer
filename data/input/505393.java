public class AdnRecordLoader extends Handler {
    static String LOG_TAG;
    PhoneBase phone;
    int ef;
    int extensionEF;
    int pendingExtLoads;
    Message userResponse;
    String pin2;
    int recordNumber;
    ArrayList<AdnRecord> adns; 
    Object result;
    static final int EVENT_ADN_LOAD_DONE = 1;
    static final int EVENT_EXT_RECORD_LOAD_DONE = 2;
    static final int EVENT_ADN_LOAD_ALL_DONE = 3;
    static final int EVENT_EF_LINEAR_RECORD_SIZE_DONE = 4;
    static final int EVENT_UPDATE_RECORD_DONE = 5;
    public AdnRecordLoader(PhoneBase phone) {
        super(phone.getHandler().getLooper());
        this.phone = phone;
        LOG_TAG = phone.getPhoneName();
    }
    public void
    loadFromEF(int ef, int extensionEF, int recordNumber,
                Message response) {
        this.ef = ef;
        this.extensionEF = extensionEF;
        this.recordNumber = recordNumber;
        this.userResponse = response;
        phone.mIccFileHandler.loadEFLinearFixed(
                    ef, recordNumber,
                    obtainMessage(EVENT_ADN_LOAD_DONE));
    }
    public void
    loadAllFromEF(int ef, int extensionEF,
                Message response) {
        this.ef = ef;
        this.extensionEF = extensionEF;
        this.userResponse = response;
        phone.mIccFileHandler.loadEFLinearFixedAll(
                    ef,
                    obtainMessage(EVENT_ADN_LOAD_ALL_DONE));
    }
    public void
    updateEF(AdnRecord adn, int ef, int extensionEF, int recordNumber,
            String pin2, Message response) {
        this.ef = ef;
        this.extensionEF = extensionEF;
        this.recordNumber = recordNumber;
        this.userResponse = response;
        this.pin2 = pin2;
        phone.mIccFileHandler.getEFLinearRecordSize( ef,
            obtainMessage(EVENT_EF_LINEAR_RECORD_SIZE_DONE, adn));
    }
    public void
    handleMessage(Message msg) {
        AsyncResult ar;
        byte data[];
        AdnRecord adn;
        try {
            switch (msg.what) {
                case EVENT_EF_LINEAR_RECORD_SIZE_DONE:
                    ar = (AsyncResult)(msg.obj);
                    adn = (AdnRecord)(ar.userObj);
                    if (ar.exception != null) {
                        throw new RuntimeException("get EF record size failed",
                                ar.exception);
                    }
                    int[] recordSize = (int[])ar.result;
                   if (recordSize.length != 3 || recordNumber > recordSize[2]) {
                        throw new RuntimeException("get wrong EF record size format",
                                ar.exception);
                    }
                    data = adn.buildAdnString(recordSize[0]);
                    if(data == null) {
                        throw new RuntimeException("worong ADN format",
                                ar.exception);
                    }
                    phone.mIccFileHandler.updateEFLinearFixed(ef, recordNumber,
                            data, pin2, obtainMessage(EVENT_UPDATE_RECORD_DONE));
                    pendingExtLoads = 1;
                    break;
                case EVENT_UPDATE_RECORD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    if (ar.exception != null) {
                        throw new RuntimeException("update EF adn record failed",
                                ar.exception);
                    }
                    pendingExtLoads = 0;
                    result = null;
                    break;
                case EVENT_ADN_LOAD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    data = (byte[])(ar.result);
                    if (ar.exception != null) {
                        throw new RuntimeException("load failed", ar.exception);
                    }
                    if (false) {
                        Log.d(LOG_TAG,"ADN EF: 0x"
                            + Integer.toHexString(ef)
                            + ":" + recordNumber
                            + "\n" + IccUtils.bytesToHexString(data));
                    }
                    adn = new AdnRecord(ef, recordNumber, data);
                    result = adn;
                    if (adn.hasExtendedRecord()) {
                        pendingExtLoads = 1;
                        phone.mIccFileHandler.loadEFLinearFixed(
                            extensionEF, adn.extRecord,
                            obtainMessage(EVENT_EXT_RECORD_LOAD_DONE, adn));
                    }
                break;
                case EVENT_EXT_RECORD_LOAD_DONE:
                    ar = (AsyncResult)(msg.obj);
                    data = (byte[])(ar.result);
                    adn = (AdnRecord)(ar.userObj);
                    if (ar.exception != null) {
                        throw new RuntimeException("load failed", ar.exception);
                    }
                    Log.d(LOG_TAG,"ADN extention EF: 0x"
                        + Integer.toHexString(extensionEF)
                        + ":" + adn.extRecord
                        + "\n" + IccUtils.bytesToHexString(data));
                    adn.appendExtRecord(data);
                    pendingExtLoads--;
                break;
                case EVENT_ADN_LOAD_ALL_DONE:
                    ar = (AsyncResult)(msg.obj);
                    ArrayList<byte[]> datas = (ArrayList<byte[]>)(ar.result);
                    if (ar.exception != null) {
                        throw new RuntimeException("load failed", ar.exception);
                    }
                    adns = new ArrayList<AdnRecord>(datas.size());
                    result = adns;
                    pendingExtLoads = 0;
                    for(int i = 0, s = datas.size() ; i < s ; i++) {
                        adn = new AdnRecord(ef, 1 + i, datas.get(i));
                        adns.add(adn);
                        if (adn.hasExtendedRecord()) {
                            pendingExtLoads++;
                            phone.mIccFileHandler.loadEFLinearFixed(
                                extensionEF, adn.extRecord,
                                obtainMessage(EVENT_EXT_RECORD_LOAD_DONE, adn));
                        }
                    }
                break;
            }
        } catch (RuntimeException exc) {
            if (userResponse != null) {
                AsyncResult.forMessage(userResponse)
                                .exception = exc;
                userResponse.sendToTarget();
                userResponse = null;
            }
            return;
        }
        if (userResponse != null && pendingExtLoads == 0) {
            AsyncResult.forMessage(userResponse).result
                = result;
            userResponse.sendToTarget();
            userResponse = null;
        }
    }
}
