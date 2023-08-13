class CallInfo {
    enum State {
        ACTIVE(0),
        HOLDING(1),
        DIALING(2),    
        ALERTING(3),   
        INCOMING(4),   
        WAITING(5);    
        State (int value) {this.value = value;}
        private final int value;
        public int value() {return value;};
    };
    boolean isMT;
    State state;
    boolean isMpty;
    String number;
    int TOA;
    CallInfo (boolean isMT, State state, boolean isMpty, String number) {
        this.isMT = isMT;
        this.state = state;
        this.isMpty = isMpty;
        this.number = number;
        if (number.length() > 0 && number.charAt(0) == '+') {
            TOA = PhoneNumberUtils.TOA_International;
        } else {
            TOA = PhoneNumberUtils.TOA_Unknown;
        }
    }
    static CallInfo
    createOutgoingCall(String number) {
        return new CallInfo (false, State.DIALING, false, number);
    }
    static CallInfo
    createIncomingCall(String number) {
        return new CallInfo (true, State.INCOMING, false, number);
    }
    String
    toCLCCLine(int index) {
        return
            "+CLCC: "
            + index + "," + (isMT ? "1" : "0") +","
            + state.value() + ",0," + (isMpty ? "1" : "0")
            + ",\"" + number + "\"," + TOA;
    }
    DriverCall
    toDriverCall(int index) {
        DriverCall ret;
        ret = new DriverCall();
        ret.index = index;
        ret.isMT = isMT;
        try {
            ret.state = DriverCall.stateFromCLCC(state.value());
        } catch (ATParseEx ex) {
            throw new RuntimeException("should never happen", ex);
        }
        ret.isMpty = isMpty;
        ret.number = number;
        ret.TOA = TOA;
        ret.isVoice = true;
        ret.als = 0;
        return ret;
    }
    boolean
    isActiveOrHeld() {
        return state == State.ACTIVE || state == State.HOLDING;
    }
    boolean
    isConnecting() {
        return state == State.DIALING || state == State.ALERTING;
    }
    boolean
    isRinging() {
        return state == State.INCOMING || state == State.WAITING;
    }
}
class InvalidStateEx extends Exception {
    InvalidStateEx() {
    }
}
class SimulatedGsmCallState extends Handler {
    CallInfo calls[] = new CallInfo[MAX_CALLS];
    private boolean autoProgressConnecting = true;
    private boolean nextDialFailImmediately;
    static final int EVENT_PROGRESS_CALL_STATE = 1;
    static final int MAX_CALLS = 7;
    static final int CONNECTING_PAUSE_MSEC = 5 * 100;
    public SimulatedGsmCallState(Looper looper) {
        super(looper);
    }
    public void
    handleMessage(Message msg) {
        synchronized(this) { switch (msg.what) {
            case EVENT_PROGRESS_CALL_STATE:
                progressConnectingCallState();
            break;
        }}
    }
    public boolean
    triggerRing(String number) {
        synchronized (this) {
            int empty = -1;
            boolean isCallWaiting = false;
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call == null && empty < 0) {
                    empty = i;
                } else if (call != null
                    && (call.state == CallInfo.State.INCOMING
                        || call.state == CallInfo.State.WAITING)
                ) {
                    Log.w("ModelInterpreter",
                        "triggerRing failed; phone already ringing");
                    return false;
                } else if (call != null) {
                    isCallWaiting = true;
                }
            }
            if (empty < 0 ) {
                Log.w("ModelInterpreter", "triggerRing failed; all full");
                return false;
            }
            calls[empty] = CallInfo.createIncomingCall(
                PhoneNumberUtils.extractNetworkPortion(number));
            if (isCallWaiting) {
                calls[empty].state = CallInfo.State.WAITING;
            }
        }
        return true;
    }
    public void
    progressConnectingCallState() {
        synchronized (this)  {
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call != null && call.state == CallInfo.State.DIALING) {
                    call.state = CallInfo.State.ALERTING;
                    if (autoProgressConnecting) {
                        sendMessageDelayed(
                                obtainMessage(EVENT_PROGRESS_CALL_STATE, call),
                                CONNECTING_PAUSE_MSEC);
                    }
                    break;
                } else if (call != null
                        && call.state == CallInfo.State.ALERTING
                ) {
                    call.state = CallInfo.State.ACTIVE;
                    break;
                }
            }
        }
    }
    public void
    progressConnectingToActive() {
        synchronized (this)  {
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call != null && (call.state == CallInfo.State.DIALING
                    || call.state == CallInfo.State.ALERTING)
                ) {
                    call.state = CallInfo.State.ACTIVE;
                    break;
                }
            }
        }
    }
    public void
    setAutoProgressConnectingCall(boolean b) {
        autoProgressConnecting = b;
    }
    public void
    setNextDialFailImmediately(boolean b) {
        nextDialFailImmediately = b;
    }
    public boolean
    triggerHangupForeground() {
        synchronized (this) {
            boolean found;
            found = false;
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call != null
                    && (call.state == CallInfo.State.INCOMING
                        || call.state == CallInfo.State.WAITING)
                ) {
                    calls[i] = null;
                    found = true;
                }
            }
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call != null
                    && (call.state == CallInfo.State.DIALING
                        || call.state == CallInfo.State.ACTIVE
                        || call.state == CallInfo.State.ALERTING)
                ) {
                    calls[i] = null;
                    found = true;
                }
            }
            return found;
        }
    }
    public boolean
    triggerHangupBackground() {
        synchronized (this) {
            boolean found = false;
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call != null && call.state == CallInfo.State.HOLDING) {
                    calls[i] = null;
                    found = true;
                }
            }
            return found;
        }
    }
    public boolean
    triggerHangupAll() {
        synchronized(this) {
            boolean found = false;
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (calls[i] != null) {
                    found = true;
                }
                calls[i] = null;
            }
            return found;
        }
    }
    public boolean
    onAnswer() {
        synchronized (this) {
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo call = calls[i];
                if (call != null
                    && (call.state == CallInfo.State.INCOMING
                        || call.state == CallInfo.State.WAITING)
                ) {
                    return switchActiveAndHeldOrWaiting();
                }
            }
        }
        return false;
    }
    public boolean
    onHangup() {
        boolean found = false;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo call = calls[i];
            if (call != null && call.state != CallInfo.State.WAITING) {
                calls[i] = null;
                found = true;
            }
        }
        return found;
    }
    public boolean
    onChld(char c0, char c1) {
        boolean ret;
        int callIndex = 0;
        if (c1 != 0) {
            callIndex = c1 - '1';
            if (callIndex < 0 || callIndex >= calls.length) {
                return false;
            }
        }
        switch (c0) {
            case '0':
                ret = releaseHeldOrUDUB();
            break;
            case '1':
                if (c1 <= 0) {
                    ret = releaseActiveAcceptHeldOrWaiting();
                } else {
                    if (calls[callIndex] == null) {
                        ret = false;
                    } else {
                        calls[callIndex] = null;
                        ret = true;
                    }
                }
            break;
            case '2':
                if (c1 <= 0) {
                    ret = switchActiveAndHeldOrWaiting();
                } else {
                    ret = separateCall(callIndex);
                }
            break;
            case '3':
                ret = conference();
            break;
            case '4':
                ret = explicitCallTransfer();
            break;
            case '5':
                if (true) { 
                    ret = false;
                }
            break;
            default:
                ret = false;
        }
        return ret;
    }
    public boolean
    releaseHeldOrUDUB() {
        boolean found = false;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null && c.isRinging()) {
                found = true;
                calls[i] = null;
                break;
            }
        }
        if (!found) {
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo c = calls[i];
                if (c != null && c.state == CallInfo.State.HOLDING) {
                    found = true;
                    calls[i] = null;
                }
            }
        }
        return true;
    }
    public boolean
    releaseActiveAcceptHeldOrWaiting() {
        boolean foundHeld = false;
        boolean foundActive = false;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null && c.state == CallInfo.State.ACTIVE) {
                calls[i] = null;
                foundActive = true;
            }
        }
        if (!foundActive) {
            for (int i = 0 ; i < calls.length ; i++) {
                CallInfo c = calls[i];
                if (c != null
                        && (c.state == CallInfo.State.DIALING
                            || c.state == CallInfo.State.ALERTING)
                ) {
                    calls[i] = null;
                    foundActive = true;
                }
            }
        }
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null && c.state == CallInfo.State.HOLDING) {
                c.state = CallInfo.State.ACTIVE;
                foundHeld = true;
            }
        }
        if (foundHeld) {
            return true;
        }
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null && c.isRinging()) {
                c.state = CallInfo.State.ACTIVE;
                return true;
            }
        }
        return true;
    }
    public boolean
    switchActiveAndHeldOrWaiting() {
        boolean hasHeld = false;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null && c.state == CallInfo.State.HOLDING) {
                hasHeld = true;
                break;
            }
        }
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null) {
                if (c.state == CallInfo.State.ACTIVE) {
                    c.state = CallInfo.State.HOLDING;
                } else if (c.state == CallInfo.State.HOLDING) {
                    c.state = CallInfo.State.ACTIVE;
                } else if (!hasHeld && c.isRinging())  {
                    c.state = CallInfo.State.ACTIVE;
                }
            }
        }
        return true;
    }
    public boolean
    separateCall(int index) {
        try {
            CallInfo c;
            c = calls[index];
            if (c == null || c.isConnecting() || countActiveLines() != 1) {
                return false;
            }
            c.state = CallInfo.State.ACTIVE;
            c.isMpty = false;
            for (int i = 0 ; i < calls.length ; i++) {
                int countHeld=0, lastHeld=0;
                if (i != index) {
                    CallInfo cb = calls[i];
                    if (cb != null && cb.state == CallInfo.State.ACTIVE) {
                        cb.state = CallInfo.State.HOLDING;
                        countHeld++;
                        lastHeld = i;
                    }
                }
                if (countHeld == 1) {
                    calls[lastHeld].isMpty = false;
                }
            }
            return true;
        } catch (InvalidStateEx ex) {
            return false;
        }
    }
    public boolean
    conference() {
        int countCalls = 0;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null) {
                countCalls++;
                if (c.isConnecting()) {
                    return false;
                }
            }
        }
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null) {
                c.state = CallInfo.State.ACTIVE;
                if (countCalls > 0) {
                    c.isMpty = true;
                }
            }
        }
        return true;
    }
    public boolean
    explicitCallTransfer() {
        int countCalls = 0;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null) {
                countCalls++;
                if (c.isConnecting()) {
                    return false;
                }
            }
        }
        return triggerHangupAll();
    }
    public boolean
    onDial(String address) {
        CallInfo call;
        int freeSlot = -1;
        Log.d("GSM", "SC> dial '" + address + "'");
        if (nextDialFailImmediately) {
            nextDialFailImmediately = false;
            Log.d("GSM", "SC< dial fail (per request)");
            return false;
        }
        String phNum = PhoneNumberUtils.extractNetworkPortion(address);
        if (phNum.length() == 0) {
            Log.d("GSM", "SC< dial fail (invalid ph num)");
            return false;
        }
        if (phNum.startsWith("*99") && phNum.endsWith("#")) {
            Log.d("GSM", "SC< dial ignored (gprs)");
            return true;
        }
        try {
            if (countActiveLines() > 1) {
                Log.d("GSM", "SC< dial fail (invalid call state)");
                return false;
            }
        } catch (InvalidStateEx ex) {
            Log.d("GSM", "SC< dial fail (invalid call state)");
            return false;
        }
        for (int i = 0 ; i < calls.length ; i++) {
            if (freeSlot < 0 && calls[i] == null) {
                freeSlot = i;
            }
            if (calls[i] != null && !calls[i].isActiveOrHeld()) {
                Log.d("GSM", "SC< dial fail (invalid call state)");
                return false;
            } else if (calls[i] != null && calls[i].state == CallInfo.State.ACTIVE) {
                calls[i].state = CallInfo.State.HOLDING;
            }
        }
        if (freeSlot < 0) {
            Log.d("GSM", "SC< dial fail (invalid call state)");
            return false;
        }
        calls[freeSlot] = CallInfo.createOutgoingCall(phNum);
        if (autoProgressConnecting) {
            sendMessageDelayed(
                    obtainMessage(EVENT_PROGRESS_CALL_STATE, calls[freeSlot]),
                    CONNECTING_PAUSE_MSEC);
        }
        Log.d("GSM", "SC< dial (slot = " + freeSlot + ")");
        return true;
    }
    public List<DriverCall>
    getDriverCalls() {
        ArrayList<DriverCall> ret = new ArrayList<DriverCall>(calls.length);
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null) {
                DriverCall dc;
                dc = c.toDriverCall(i + 1);
                ret.add(dc);
            }
        }
        Log.d("GSM", "SC< getDriverCalls " + ret);
        return ret;
    }
    public List<String>
    getClccLines() {
        ArrayList<String> ret = new ArrayList<String>(calls.length);
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo c = calls[i];
            if (c != null) {
                ret.add((c.toCLCCLine(i + 1)));
            }
        }
        return ret;
    }
    private int
    countActiveLines() throws InvalidStateEx {
        boolean hasMpty = false;
        boolean hasHeld = false;
        boolean hasActive = false;
        boolean hasConnecting = false;
        boolean hasRinging = false;
        boolean mptyIsHeld = false;
        for (int i = 0 ; i < calls.length ; i++) {
            CallInfo call = calls[i];
            if (call != null) {
                if (!hasMpty && call.isMpty) {
                    mptyIsHeld = call.state == CallInfo.State.HOLDING;
                } else if (call.isMpty && mptyIsHeld
                    && call.state == CallInfo.State.ACTIVE
                ) {
                    Log.e("ModelInterpreter", "Invalid state");
                    throw new InvalidStateEx();
                } else if (!call.isMpty && hasMpty && mptyIsHeld
                    && call.state == CallInfo.State.HOLDING
                ) {
                    Log.e("ModelInterpreter", "Invalid state");
                    throw new InvalidStateEx();
                }
                hasMpty |= call.isMpty;
                hasHeld |= call.state == CallInfo.State.HOLDING;
                hasActive |= call.state == CallInfo.State.ACTIVE;
                hasConnecting |= call.isConnecting();
                hasRinging |= call.isRinging();
            }
        }
        int ret = 0;
        if (hasHeld) ret++;
        if (hasActive) ret++;
        if (hasConnecting) ret++;
        if (hasRinging) ret++;
        return ret;
    }
}
