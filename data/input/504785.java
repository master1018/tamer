public class CdmaConnection extends Connection {
    static final String LOG_TAG = "CDMA";
    CdmaCallTracker owner;
    CdmaCall parent;
    String address;             
    String dialString;          
    String postDialString;      
    boolean isIncoming;
    boolean disconnected;
    String cnapName;
    int index;          
    long createTime;
    long connectTime;
    long disconnectTime;
    long connectTimeReal;
    long duration;
    long holdingStartTime;  
    int nextPostDialChar;       
    DisconnectCause cause = DisconnectCause.NOT_DISCONNECTED;
    PostDialState postDialState = PostDialState.NOT_STARTED;
    int numberPresentation = Connection.PRESENTATION_ALLOWED;
    int cnapNamePresentation  = Connection.PRESENTATION_ALLOWED;
    Handler h;
    private PowerManager.WakeLock mPartialWakeLock;
    static final int EVENT_DTMF_DONE = 1;
    static final int EVENT_PAUSE_DONE = 2;
    static final int EVENT_NEXT_POST_DIAL = 3;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    static final int WAKE_LOCK_TIMEOUT_MILLIS = 60*1000;
    static final int PAUSE_DELAY_MILLIS = 2 * 1000;
    class MyHandler extends Handler {
        MyHandler(Looper l) {super(l);}
        public void
        handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_NEXT_POST_DIAL:
                case EVENT_DTMF_DONE:
                case EVENT_PAUSE_DONE:
                    processNextPostDialChar();
                    break;
                case EVENT_WAKE_LOCK_TIMEOUT:
                    releaseWakeLock();
                    break;
            }
        }
    }
    CdmaConnection (Context context, DriverCall dc, CdmaCallTracker ct, int index) {
        createWakeLock(context);
        acquireWakeLock();
        owner = ct;
        h = new MyHandler(owner.getLooper());
        address = dc.number;
        isIncoming = dc.isMT;
        createTime = System.currentTimeMillis();
        cnapName = dc.name;
        cnapNamePresentation = dc.namePresentation;
        numberPresentation = dc.numberPresentation;
        this.index = index;
        parent = parentFromDCState (dc.state);
        parent.attach(this, dc);
    }
    CdmaConnection(Context context, String dialString, CdmaCallTracker ct, CdmaCall parent) {
        createWakeLock(context);
        acquireWakeLock();
        owner = ct;
        h = new MyHandler(owner.getLooper());
        this.dialString = dialString;
        Log.d(LOG_TAG, "[CDMAConn] CdmaConnection: dialString=" + dialString);
        dialString = formatDialString(dialString);
        Log.d(LOG_TAG, "[CDMAConn] CdmaConnection:formated dialString=" + dialString);
        this.address = PhoneNumberUtils.extractNetworkPortionAlt(dialString);
        this.postDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        index = -1;
        isIncoming = false;
        cnapName = null;
        cnapNamePresentation = Connection.PRESENTATION_ALLOWED;
        numberPresentation = Connection.PRESENTATION_ALLOWED;
        createTime = System.currentTimeMillis();
        if (parent != null) {
            this.parent = parent;
            if (parent.state == CdmaCall.State.ACTIVE) {
                parent.attachFake(this, CdmaCall.State.ACTIVE);
            } else {
                parent.attachFake(this, CdmaCall.State.DIALING);
            }
        }
    }
    CdmaConnection(Context context, CdmaCallWaitingNotification cw, CdmaCallTracker ct,
            CdmaCall parent) {
        createWakeLock(context);
        acquireWakeLock();
        owner = ct;
        h = new MyHandler(owner.getLooper());
        address = cw.number;
        numberPresentation = cw.numberPresentation;
        cnapName = cw.name;
        cnapNamePresentation = cw.namePresentation;
        index = -1;
        isIncoming = true;
        createTime = System.currentTimeMillis();
        connectTime = 0;
        this.parent = parent;
        parent.attachFake(this, CdmaCall.State.WAITING);
    }
    public void dispose() {
    }
    static boolean
    equalsHandlesNulls (Object a, Object b) {
        return (a == null) ? (b == null) : a.equals (b);
    }
     boolean
    compareTo(DriverCall c) {
        if (! (isIncoming || c.isMT)) return true;
        String cAddress = PhoneNumberUtils.stringFromStringAndTOA(c.number, c.TOA);
        return isIncoming == c.isMT && equalsHandlesNulls(address, cAddress);
    }
    public String getOrigDialString(){
        return dialString;
    }
    public String getAddress() {
        return address;
    }
    public String getCnapName() {
        return cnapName;
    }
    public int getCnapNamePresentation() {
        return cnapNamePresentation;
    }
    public CdmaCall getCall() {
        return parent;
    }
    public long getCreateTime() {
        return createTime;
    }
    public long getConnectTime() {
        return connectTime;
    }
    public long getDisconnectTime() {
        return disconnectTime;
    }
    public long getDurationMillis() {
        if (connectTimeReal == 0) {
            return 0;
        } else if (duration == 0) {
            return SystemClock.elapsedRealtime() - connectTimeReal;
        } else {
            return duration;
        }
    }
    public long getHoldDurationMillis() {
        if (getState() != CdmaCall.State.HOLDING) {
            return 0;
        } else {
            return SystemClock.elapsedRealtime() - holdingStartTime;
        }
    }
    public DisconnectCause getDisconnectCause() {
        return cause;
    }
    public boolean isIncoming() {
        return isIncoming;
    }
    public CdmaCall.State getState() {
        if (disconnected) {
            return CdmaCall.State.DISCONNECTED;
        } else {
            return super.getState();
        }
    }
    public void hangup() throws CallStateException {
        if (!disconnected) {
            owner.hangup(this);
        } else {
            throw new CallStateException ("disconnected");
        }
    }
    public void separate() throws CallStateException {
        if (!disconnected) {
            owner.separate(this);
        } else {
            throw new CallStateException ("disconnected");
        }
    }
    public PostDialState getPostDialState() {
        return postDialState;
    }
    public void proceedAfterWaitChar() {
        if (postDialState != PostDialState.WAIT) {
            Log.w(LOG_TAG, "CdmaConnection.proceedAfterWaitChar(): Expected "
                + "getPostDialState() to be WAIT but was " + postDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        processNextPostDialChar();
    }
    public void proceedAfterWildChar(String str) {
        if (postDialState != PostDialState.WILD) {
            Log.w(LOG_TAG, "CdmaConnection.proceedAfterWaitChar(): Expected "
                + "getPostDialState() to be WILD but was " + postDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        if (false) {
            boolean playedTone = false;
            int len = (str != null ? str.length() : 0);
            for (int i=0; i<len; i++) {
                char c = str.charAt(i);
                Message msg = null;
                if (i == len-1) {
                    msg = h.obtainMessage(EVENT_DTMF_DONE);
                }
                if (PhoneNumberUtils.is12Key(c)) {
                    owner.cm.sendDtmf(c, msg);
                    playedTone = true;
                }
            }
            if (!playedTone) {
                processNextPostDialChar();
            }
        } else {
            StringBuilder buf = new StringBuilder(str);
            buf.append(postDialString.substring(nextPostDialChar));
            postDialString = buf.toString();
            nextPostDialChar = 0;
            if (Phone.DEBUG_PHONE) {
                log("proceedAfterWildChar: new postDialString is " +
                        postDialString);
            }
            processNextPostDialChar();
        }
    }
    public void cancelPostDial() {
        setPostDialState(PostDialState.CANCELLED);
    }
    void
    onHangupLocal() {
        cause = DisconnectCause.LOCAL;
    }
    DisconnectCause
    disconnectCauseFromCode(int causeCode) {
        switch (causeCode) {
            case CallFailCause.USER_BUSY:
                return DisconnectCause.BUSY;
            case CallFailCause.NO_CIRCUIT_AVAIL:
                return DisconnectCause.CONGESTION;
            case CallFailCause.ACM_LIMIT_EXCEEDED:
                return DisconnectCause.LIMIT_EXCEEDED;
            case CallFailCause.CALL_BARRED:
                return DisconnectCause.CALL_BARRED;
            case CallFailCause.FDN_BLOCKED:
                return DisconnectCause.FDN_BLOCKED;
            case CallFailCause.CDMA_LOCKED_UNTIL_POWER_CYCLE:
                return DisconnectCause.CDMA_LOCKED_UNTIL_POWER_CYCLE;
            case CallFailCause.CDMA_DROP:
                return DisconnectCause.CDMA_DROP;
            case CallFailCause.CDMA_INTERCEPT:
                return DisconnectCause.CDMA_INTERCEPT;
            case CallFailCause.CDMA_REORDER:
                return DisconnectCause.CDMA_REORDER;
            case CallFailCause.CDMA_SO_REJECT:
                return DisconnectCause.CDMA_SO_REJECT;
            case CallFailCause.CDMA_RETRY_ORDER:
                return DisconnectCause.CDMA_RETRY_ORDER;
            case CallFailCause.CDMA_ACCESS_FAILURE:
                return DisconnectCause.CDMA_ACCESS_FAILURE;
            case CallFailCause.CDMA_PREEMPTED:
                return DisconnectCause.CDMA_PREEMPTED;
            case CallFailCause.CDMA_NOT_EMERGENCY:
                return DisconnectCause.CDMA_NOT_EMERGENCY;
            case CallFailCause.CDMA_ACCESS_BLOCKED:
                return DisconnectCause.CDMA_ACCESS_BLOCKED;
            case CallFailCause.ERROR_UNSPECIFIED:
            case CallFailCause.NORMAL_CLEARING:
            default:
                CDMAPhone phone = owner.phone;
                int serviceState = phone.getServiceState().getState();
                if (serviceState == ServiceState.STATE_POWER_OFF) {
                    return DisconnectCause.POWER_OFF;
                } else if (serviceState == ServiceState.STATE_OUT_OF_SERVICE
                        || serviceState == ServiceState.STATE_EMERGENCY_ONLY) {
                    return DisconnectCause.OUT_OF_SERVICE;
                } else if (phone.mCM.getRadioState() != CommandsInterface.RadioState.NV_READY
                        && phone.getIccCard().getState() != RuimCard.State.READY) {
                    return DisconnectCause.ICC_ERROR;
                } else if (causeCode==CallFailCause.NORMAL_CLEARING) {
                    return DisconnectCause.NORMAL;
                } else {
                    return DisconnectCause.ERROR_UNSPECIFIED;
                }
        }
    }
     void
    onRemoteDisconnect(int causeCode) {
        onDisconnect(disconnectCauseFromCode(causeCode));
    }
     void
    onDisconnect(DisconnectCause cause) {
        this.cause = cause;
        if (!disconnected) {
            doDisconnect();
            if (Config.LOGD) Log.d(LOG_TAG,
                    "[CDMAConn] onDisconnect: cause=" + cause);
            owner.phone.notifyDisconnect(this);
            if (parent != null) {
                parent.connectionDisconnected(this);
            }
        }
        releaseWakeLock();
    }
     void
    onLocalDisconnect() {
        if (!disconnected) {
            doDisconnect();
            if (Config.LOGD) Log.d(LOG_TAG,
                    "[CDMAConn] onLoalDisconnect" );
            if (parent != null) {
                parent.detach(this);
            }
        }
        releaseWakeLock();
    }
     boolean
    update (DriverCall dc) {
        CdmaCall newParent;
        boolean changed = false;
        boolean wasConnectingInOrOut = isConnectingInOrOut();
        boolean wasHolding = (getState() == CdmaCall.State.HOLDING);
        newParent = parentFromDCState(dc.state);
        if (Phone.DEBUG_PHONE) log("parent= " +parent +", newParent= " + newParent);
        if (!equalsHandlesNulls(address, dc.number)) {
            if (Phone.DEBUG_PHONE) log("update: phone # changed!");
            address = dc.number;
            changed = true;
        }
        if (TextUtils.isEmpty(dc.name)) {
            if (!TextUtils.isEmpty(cnapName)) {
                changed = true;
                cnapName = "";
            }
        } else if (!dc.name.equals(cnapName)) {
            changed = true;
            cnapName = dc.name;
        }
        if (Phone.DEBUG_PHONE) log("--dssds----"+cnapName);
        cnapNamePresentation = dc.namePresentation;
        numberPresentation = dc.numberPresentation;
        if (newParent != parent) {
            if (parent != null) {
                parent.detach(this);
            }
            newParent.attach(this, dc);
            parent = newParent;
            changed = true;
        } else {
            boolean parentStateChange;
            parentStateChange = parent.update (this, dc);
            changed = changed || parentStateChange;
        }
        if (Phone.DEBUG_PHONE) log(
                "Update, wasConnectingInOrOut=" + wasConnectingInOrOut +
                ", wasHolding=" + wasHolding +
                ", isConnectingInOrOut=" + isConnectingInOrOut() +
                ", changed=" + changed);
        if (wasConnectingInOrOut && !isConnectingInOrOut()) {
            onConnectedInOrOut();
        }
        if (changed && !wasHolding && (getState() == CdmaCall.State.HOLDING)) {
            onStartedHolding();
        }
        return changed;
    }
    void
    fakeHoldBeforeDial() {
        if (parent != null) {
            parent.detach(this);
        }
        parent = owner.backgroundCall;
        parent.attachFake(this, CdmaCall.State.HOLDING);
        onStartedHolding();
    }
     int
    getCDMAIndex() throws CallStateException {
        if (index >= 0) {
            return index + 1;
        } else {
            throw new CallStateException ("CDMA connection index not assigned");
        }
    }
    void
    onConnectedInOrOut() {
        connectTime = System.currentTimeMillis();
        connectTimeReal = SystemClock.elapsedRealtime();
        duration = 0;
        if (Phone.DEBUG_PHONE) {
            log("onConnectedInOrOut: connectTime=" + connectTime);
        }
        if (!isIncoming) {
            processNextPostDialChar();
        } else {
            releaseWakeLock();
        }
    }
    private void
    doDisconnect() {
       index = -1;
       disconnectTime = System.currentTimeMillis();
       duration = SystemClock.elapsedRealtime() - connectTimeReal;
       disconnected = true;
    }
    private void
    onStartedHolding() {
        holdingStartTime = SystemClock.elapsedRealtime();
    }
    private boolean
    processPostDialChar(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            owner.cm.sendDtmf(c, h.obtainMessage(EVENT_DTMF_DONE));
        } else if (c == PhoneNumberUtils.PAUSE) {
            setPostDialState(PostDialState.PAUSE);
            h.sendMessageDelayed(h.obtainMessage(EVENT_PAUSE_DONE),
                                            PAUSE_DELAY_MILLIS);
        } else if (c == PhoneNumberUtils.WAIT) {
            setPostDialState(PostDialState.WAIT);
        } else if (c == PhoneNumberUtils.WILD) {
            setPostDialState(PostDialState.WILD);
        } else {
            return false;
        }
        return true;
    }
    public String getRemainingPostDialString() {
        if (postDialState == PostDialState.CANCELLED
                || postDialState == PostDialState.COMPLETE
                || postDialString == null
                || postDialString.length() <= nextPostDialChar) {
            return "";
        }
        String subStr = postDialString.substring(nextPostDialChar);
        if (subStr != null) {
            int wIndex = subStr.indexOf(PhoneNumberUtils.WAIT);
            int pIndex = subStr.indexOf(PhoneNumberUtils.PAUSE);
            if (wIndex > 0 && (wIndex < pIndex || pIndex <= 0)) {
                subStr = subStr.substring(0, wIndex);
            } else if (pIndex > 0) {
                subStr = subStr.substring(0, pIndex);
            }
        }
        return subStr;
    }
    public void updateParent(CdmaCall oldParent, CdmaCall newParent){
        if (newParent != oldParent) {
            if (oldParent != null) {
                oldParent.detach(this);
            }
            newParent.attachFake(this, CdmaCall.State.ACTIVE);
            parent = newParent;
        }
    }
    @Override
    protected void finalize()
    {
        if (mPartialWakeLock.isHeld()) {
            Log.e(LOG_TAG, "[CdmaConn] UNEXPECTED; mPartialWakeLock is held when finalizing.");
        }
        releaseWakeLock();
    }
    void processNextPostDialChar() {
        char c = 0;
        Registrant postDialHandler;
        if (postDialState == PostDialState.CANCELLED) {
            releaseWakeLock();
            return;
        }
        if (postDialString == null ||
                postDialString.length() <= nextPostDialChar) {
            setPostDialState(PostDialState.COMPLETE);
            releaseWakeLock();
            c = 0;
        } else {
            boolean isValid;
            setPostDialState(PostDialState.STARTED);
            c = postDialString.charAt(nextPostDialChar++);
            isValid = processPostDialChar(c);
            if (!isValid) {
                h.obtainMessage(EVENT_NEXT_POST_DIAL).sendToTarget();
                Log.e("CDMA", "processNextPostDialChar: c=" + c + " isn't valid!");
                return;
            }
        }
        postDialHandler = owner.phone.mPostDialHandler;
        Message notifyMessage;
        if (postDialHandler != null &&
                (notifyMessage = postDialHandler.messageForRegistrant()) != null) {
            PostDialState state = postDialState;
            AsyncResult ar = AsyncResult.forMessage(notifyMessage);
            ar.result = this;
            ar.userObj = state;
            notifyMessage.arg1 = c;
            notifyMessage.sendToTarget();
        }
    }
    private boolean
    isConnectingInOrOut() {
        return parent == null || parent == owner.ringingCall
            || parent.state == CdmaCall.State.DIALING
            || parent.state == CdmaCall.State.ALERTING;
    }
    private CdmaCall
    parentFromDCState (DriverCall.State state) {
        switch (state) {
            case ACTIVE:
            case DIALING:
            case ALERTING:
                return owner.foregroundCall;
            case HOLDING:
                return owner.backgroundCall;
            case INCOMING:
            case WAITING:
                return owner.ringingCall;
            default:
                throw new RuntimeException("illegal call state: " + state);
        }
    }
    private void setPostDialState(PostDialState s) {
        if (s == PostDialState.STARTED ||
                s == PostDialState.PAUSE) {
            synchronized (mPartialWakeLock) {
                if (mPartialWakeLock.isHeld()) {
                    h.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
                } else {
                    acquireWakeLock();
                }
                Message msg = h.obtainMessage(EVENT_WAKE_LOCK_TIMEOUT);
                h.sendMessageDelayed(msg, WAKE_LOCK_TIMEOUT_MILLIS);
            }
        } else {
            h.removeMessages(EVENT_WAKE_LOCK_TIMEOUT);
            releaseWakeLock();
        }
        postDialState = s;
    }
    private void createWakeLock(Context context) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOG_TAG);
    }
    private void acquireWakeLock() {
        log("acquireWakeLock");
        mPartialWakeLock.acquire();
    }
    private void releaseWakeLock() {
        synchronized (mPartialWakeLock) {
            if (mPartialWakeLock.isHeld()) {
                log("releaseWakeLock");
                mPartialWakeLock.release();
            }
        }
    }
    private static boolean isPause(char c) {
        return c == PhoneNumberUtils.PAUSE;
    }
    private static boolean isWait(char c) {
        return c == PhoneNumberUtils.WAIT;
    }
    private static int
    findNextPCharOrNonPOrNonWCharIndex(String phoneNumber, int currIndex) {
        boolean wMatched = isWait(phoneNumber.charAt(currIndex));
        int index = currIndex + 1;
        int length = phoneNumber.length();
        while (index < length) {
            char cNext = phoneNumber.charAt(index);
            if (isWait(cNext)) {
                wMatched = true;
            }
            if (!isWait(cNext) && !isPause(cNext)) {
                break;
            }
            index++;
        }
        if ((index < length) && (index > (currIndex + 1))  &&
            ((wMatched == false) && isPause(phoneNumber.charAt(currIndex)))) {
            return (currIndex + 1);
        }
        return index;
    }
    private static char
    findPOrWCharToAppend(String phoneNumber, int currPwIndex, int nextNonPwCharIndex) {
        char c = phoneNumber.charAt(currPwIndex);
        char ret;
        ret = (isPause(c)) ? PhoneNumberUtils.PAUSE : PhoneNumberUtils.WAIT;
        if (nextNonPwCharIndex > (currPwIndex + 1)) {
            ret = PhoneNumberUtils.WAIT;
        }
        return ret;
    }
    public static String formatDialString(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int length = phoneNumber.length();
        StringBuilder ret = new StringBuilder();
        char c;
        int currIndex = 0;
        while (currIndex < length) {
            c = phoneNumber.charAt(currIndex);
            if (isPause(c) || isWait(c)) {
                if (currIndex < length - 1) {
                    int nextIndex = findNextPCharOrNonPOrNonWCharIndex(phoneNumber, currIndex);
                    if (nextIndex < length) {
                        char pC = findPOrWCharToAppend(phoneNumber, currIndex, nextIndex);
                        ret.append(pC);
                        if (nextIndex > (currIndex + 1)) {
                            currIndex = nextIndex - 1;
                        }
                    } else if (nextIndex == length) {
                        currIndex = length - 1;
                    }
                }
            } else {
                ret.append(c);
            }
            currIndex++;
        }
        return PhoneNumberUtils.cdmaCheckAndProcessPlusCode(ret.toString());
    }
    private void log(String msg) {
        Log.d(LOG_TAG, "[CDMAConn] " + msg);
    }
    @Override
    public int getNumberPresentation() {
        return numberPresentation;
    }
}
