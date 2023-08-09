public final class GsmCallTracker extends CallTracker {
    static final String LOG_TAG = "GSM";
    private static final boolean REPEAT_POLLING = false;
    private static final boolean DBG_POLL = false;
    static final int MAX_CONNECTIONS = 7;   
    static final int MAX_CONNECTIONS_PER_CALL = 5; 
    GsmConnection connections[] = new GsmConnection[MAX_CONNECTIONS];
    RegistrantList voiceCallEndedRegistrants = new RegistrantList();
    RegistrantList voiceCallStartedRegistrants = new RegistrantList();
    ArrayList<GsmConnection> droppedDuringPoll
        = new ArrayList<GsmConnection>(MAX_CONNECTIONS);
    GsmCall ringingCall = new GsmCall(this);
    GsmCall foregroundCall = new GsmCall(this);
    GsmCall backgroundCall = new GsmCall(this);
    GsmConnection pendingMO;
    boolean hangupPendingMO;
    GSMPhone phone;
    boolean desiredMute = false;    
    Phone.State state = Phone.State.IDLE;
    GsmCallTracker (GSMPhone phone) {
        this.phone = phone;
        cm = phone.mCM;
        cm.registerForCallStateChanged(this, EVENT_CALL_STATE_CHANGE, null);
        cm.registerForOn(this, EVENT_RADIO_AVAILABLE, null);
        cm.registerForNotAvailable(this, EVENT_RADIO_NOT_AVAILABLE, null);
    }
    public void dispose() {
        cm.unregisterForCallStateChanged(this);
        cm.unregisterForOn(this);
        cm.unregisterForNotAvailable(this);
        for(GsmConnection c : connections) {
            try {
                if(c != null) hangup(c);
            } catch (CallStateException ex) {
                Log.e(LOG_TAG, "unexpected error on hangup during dispose");
            }
        }
        try {
            if(pendingMO != null) hangup(pendingMO);
        } catch (CallStateException ex) {
            Log.e(LOG_TAG, "unexpected error on hangup during dispose");
        }
        clearDisconnected();
    }
    protected void finalize() {
        Log.d(LOG_TAG, "GsmCallTracker finalized");
    }
    public void registerForVoiceCallStarted(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        voiceCallStartedRegistrants.add(r);
    }
    public void unregisterForVoiceCallStarted(Handler h) {
        voiceCallStartedRegistrants.remove(h);
    }
    public void registerForVoiceCallEnded(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        voiceCallEndedRegistrants.add(r);
    }
    public void unregisterForVoiceCallEnded(Handler h) {
        voiceCallEndedRegistrants.remove(h);
    }
    private void
    fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy;
        connCopy = (List<Connection>) foregroundCall.connections.clone();
        for (int i = 0, s = connCopy.size() ; i < s ; i++) {
            GsmConnection conn = (GsmConnection)connCopy.get(i);
            conn.fakeHoldBeforeDial();
        }
    }
    Connection
    dial (String dialString, int clirMode) throws CallStateException {
        clearDisconnected();
        if (!canDial()) {
            throw new CallStateException("cannot dial in current state");
        }
        if (foregroundCall.getState() == GsmCall.State.ACTIVE) {
            switchWaitingOrHoldingAndActive();
            fakeHoldForegroundBeforeDial();
        }
        if (foregroundCall.getState() != GsmCall.State.IDLE) {
            throw new CallStateException("cannot dial in current state");
        }
        pendingMO = new GsmConnection(phone.getContext(), dialString, this, foregroundCall);
        hangupPendingMO = false;
        if (pendingMO.address == null || pendingMO.address.length() == 0
            || pendingMO.address.indexOf(PhoneNumberUtils.WILD) >= 0
        ) {
            pendingMO.cause = Connection.DisconnectCause.INVALID_NUMBER;
            pollCallsWhenSafe();
        } else {
            setMute(false);
            cm.dial(pendingMO.address, clirMode, obtainCompleteMessage());
        }
        updatePhoneState();
        phone.notifyPreciseCallStateChanged();
        return pendingMO;
    }
    Connection
    dial (String dialString) throws CallStateException {
        return dial(dialString, CommandsInterface.CLIR_DEFAULT);
    }
    void
    acceptCall () throws CallStateException {
        if (ringingCall.getState() == GsmCall.State.INCOMING) {
            Log.i("phone", "acceptCall: incoming...");
            setMute(false);
            cm.acceptCall(obtainCompleteMessage());
        } else if (ringingCall.getState() == GsmCall.State.WAITING) {
            setMute(false);
            switchWaitingOrHoldingAndActive();
        } else {
            throw new CallStateException("phone not ringing");
        }
    }
    void
    rejectCall () throws CallStateException {
        if (ringingCall.getState().isRinging()) {
            cm.rejectCall(obtainCompleteMessage());
        } else {
            throw new CallStateException("phone not ringing");
        }
    }
    void
    switchWaitingOrHoldingAndActive() throws CallStateException {
        if (ringingCall.getState() == GsmCall.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else {
            cm.switchWaitingOrHoldingAndActive(
                    obtainCompleteMessage(EVENT_SWITCH_RESULT));
        }
    }
    void
    conference() throws CallStateException {
        cm.conference(obtainCompleteMessage(EVENT_CONFERENCE_RESULT));
    }
    void
    explicitCallTransfer() throws CallStateException {
        cm.explicitCallTransfer(obtainCompleteMessage(EVENT_ECT_RESULT));
    }
    void
    clearDisconnected() {
        internalClearDisconnected();
        updatePhoneState();
        phone.notifyPreciseCallStateChanged();
    }
    boolean
    canConference() {
        return foregroundCall.getState() == GsmCall.State.ACTIVE
                && backgroundCall.getState() == GsmCall.State.HOLDING
                && !backgroundCall.isFull()
                && !foregroundCall.isFull();
    }
    boolean
    canDial() {
        boolean ret;
        int serviceState = phone.getServiceState().getState();
        String disableCall = SystemProperties.get(
                TelephonyProperties.PROPERTY_DISABLE_CALL, "false");
        ret = (serviceState != ServiceState.STATE_POWER_OFF)
                && pendingMO == null
                && !ringingCall.isRinging()
                && !disableCall.equals("true")
                && (!foregroundCall.getState().isAlive()
                    || !backgroundCall.getState().isAlive());
        return ret;
    }
    boolean
    canTransfer() {
        return foregroundCall.getState() == GsmCall.State.ACTIVE
                && backgroundCall.getState() == GsmCall.State.HOLDING;
    }
    private void
    internalClearDisconnected() {
        ringingCall.clearDisconnected();
        foregroundCall.clearDisconnected();
        backgroundCall.clearDisconnected();
    }
    private Message
    obtainCompleteMessage() {
        return obtainCompleteMessage(EVENT_OPERATION_COMPLETE);
    }
    private Message
    obtainCompleteMessage(int what) {
        pendingOperations++;
        lastRelevantPoll = null;
        needsPoll = true;
        if (DBG_POLL) log("obtainCompleteMessage: pendingOperations=" +
                pendingOperations + ", needsPoll=" + needsPoll);
        return obtainMessage(what);
    }
    private void
    operationComplete() {
        pendingOperations--;
        if (DBG_POLL) log("operationComplete: pendingOperations=" +
                pendingOperations + ", needsPoll=" + needsPoll);
        if (pendingOperations == 0 && needsPoll) {
            lastRelevantPoll = obtainMessage(EVENT_POLL_CALLS_RESULT);
            cm.getCurrentCalls(lastRelevantPoll);
        } else if (pendingOperations < 0) {
            Log.e(LOG_TAG,"GsmCallTracker.pendingOperations < 0");
            pendingOperations = 0;
        }
    }
    private void
    updatePhoneState() {
        Phone.State oldState = state;
        if (ringingCall.isRinging()) {
            state = Phone.State.RINGING;
        } else if (pendingMO != null ||
                !(foregroundCall.isIdle() && backgroundCall.isIdle())) {
            state = Phone.State.OFFHOOK;
        } else {
            state = Phone.State.IDLE;
        }
        if (state == Phone.State.IDLE && oldState != state) {
            voiceCallEndedRegistrants.notifyRegistrants(
                new AsyncResult(null, null, null));
        } else if (oldState == Phone.State.IDLE && oldState != state) {
            voiceCallStartedRegistrants.notifyRegistrants (
                    new AsyncResult(null, null, null));
        }
        if (state != oldState) {
            phone.notifyPhoneStateChanged();
        }
    }
    protected void
    handlePollCalls(AsyncResult ar) {
        List polledCalls;
        if (ar.exception == null) {
            polledCalls = (List)ar.result;
        } else if (isCommandExceptionRadioNotAvailable(ar.exception)) {
            polledCalls = new ArrayList();
        } else {
            pollCallsAfterDelay();
            return;
        }
        Connection newRinging = null; 
        boolean hasNonHangupStateChanged = false;   
        boolean needsPollDelay = false;
        boolean unknownConnectionAppeared = false;
        for (int i = 0, curDC = 0, dcSize = polledCalls.size()
                ; i < connections.length; i++) {
            GsmConnection conn = connections[i];
            DriverCall dc = null;
            if (curDC < dcSize) {
                dc = (DriverCall) polledCalls.get(curDC);
                if (dc.index == i+1) {
                    curDC++;
                } else {
                    dc = null;
                }
            }
            if (DBG_POLL) log("poll: conn[i=" + i + "]=" +
                    conn+", dc=" + dc);
            if (conn == null && dc != null) {
                if (pendingMO != null && pendingMO.compareTo(dc)) {
                    if (DBG_POLL) log("poll: pendingMO=" + pendingMO);
                    connections[i] = pendingMO;
                    pendingMO.index = i;
                    pendingMO.update(dc);
                    pendingMO = null;
                    if (hangupPendingMO) {
                        hangupPendingMO = false;
                        try {
                            if (Phone.DEBUG_PHONE) log(
                                    "poll: hangupPendingMO, hangup conn " + i);
                            hangup(connections[i]);
                        } catch (CallStateException ex) {
                            Log.e(LOG_TAG, "unexpected error on hangup");
                        }
                        return;
                    }
                } else {
                    connections[i] = new GsmConnection(phone.getContext(), dc, this, i);
                    if (connections[i].getCall() == ringingCall) {
                        newRinging = connections[i];
                    } else {
                        Log.i(LOG_TAG,"Phantom call appeared " + dc);
                        if (dc.state != DriverCall.State.ALERTING
                                && dc.state != DriverCall.State.DIALING) {
                            connections[i].connectTime = System.currentTimeMillis();
                        }
                        unknownConnectionAppeared = true;
                    }
                }
                hasNonHangupStateChanged = true;
            } else if (conn != null && dc == null) {
                droppedDuringPoll.add(conn);
                connections[i] = null;
            } else if (conn != null && dc != null && !conn.compareTo(dc)) {
                droppedDuringPoll.add(conn);
                connections[i] = new GsmConnection (phone.getContext(), dc, this, i);
                if (connections[i].getCall() == ringingCall) {
                    newRinging = connections[i];
                } 
                hasNonHangupStateChanged = true;
            } else if (conn != null && dc != null) { 
                boolean changed;
                changed = conn.update(dc);
                hasNonHangupStateChanged = hasNonHangupStateChanged || changed;
            }
            if (REPEAT_POLLING) {
                if (dc != null) {
                    if ((dc.state == DriverCall.State.DIALING
                            )
                        || (dc.state == DriverCall.State.ALERTING
                            )
                        || (dc.state == DriverCall.State.INCOMING
                            )
                        || (dc.state == DriverCall.State.WAITING
                            )
                    ) {
                        needsPollDelay = true;
                    }
                }
            }
        }
        if (pendingMO != null) {
            Log.d(LOG_TAG,"Pending MO dropped before poll fg state:"
                            + foregroundCall.getState());
            droppedDuringPoll.add(pendingMO);
            pendingMO = null;
            hangupPendingMO = false;
        }
        if (newRinging != null) {
            phone.notifyNewRingingConnection(newRinging);
        }
        for (int i = droppedDuringPoll.size() - 1; i >= 0 ; i--) {
            GsmConnection conn = droppedDuringPoll.get(i);
            if (conn.isIncoming() && conn.getConnectTime() == 0) {
                Connection.DisconnectCause cause;
                if (conn.cause == Connection.DisconnectCause.LOCAL) {
                    cause = Connection.DisconnectCause.INCOMING_REJECTED;
                } else {
                    cause = Connection.DisconnectCause.INCOMING_MISSED;
                }
                if (Phone.DEBUG_PHONE) {
                    log("missed/rejected call, conn.cause=" + conn.cause);
                    log("setting cause to " + cause);
                }
                droppedDuringPoll.remove(i);
                conn.onDisconnect(cause);
            } else if (conn.cause == Connection.DisconnectCause.LOCAL) {
                droppedDuringPoll.remove(i);
                conn.onDisconnect(Connection.DisconnectCause.LOCAL);
            } else if (conn.cause ==
                Connection.DisconnectCause.INVALID_NUMBER) {
                droppedDuringPoll.remove(i);
                conn.onDisconnect(Connection.DisconnectCause.INVALID_NUMBER);
            }
        }
        if (droppedDuringPoll.size() > 0) {
            cm.getLastCallFailCause(
                obtainNoPollCompleteMessage(EVENT_GET_LAST_CALL_FAIL_CAUSE));
        }
        if (needsPollDelay) {
            pollCallsAfterDelay();
        }
        if (newRinging != null || hasNonHangupStateChanged) {
            internalClearDisconnected();
        }
        updatePhoneState();
        if (unknownConnectionAppeared) {
            phone.notifyUnknownConnection();
        }
        if (hasNonHangupStateChanged || newRinging != null) {
            phone.notifyPreciseCallStateChanged();
        }
    }
    private void
    handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }
    private void
    dumpState() {
        List l;
        Log.i(LOG_TAG,"Phone State:" + state);
        Log.i(LOG_TAG,"Ringing call: " + ringingCall.toString());
        l = ringingCall.getConnections();
        for (int i = 0, s = l.size(); i < s; i++) {
            Log.i(LOG_TAG,l.get(i).toString());
        }
        Log.i(LOG_TAG,"Foreground call: " + foregroundCall.toString());
        l = foregroundCall.getConnections();
        for (int i = 0, s = l.size(); i < s; i++) {
            Log.i(LOG_TAG,l.get(i).toString());
        }
        Log.i(LOG_TAG,"Background call: " + backgroundCall.toString());
        l = backgroundCall.getConnections();
        for (int i = 0, s = l.size(); i < s; i++) {
            Log.i(LOG_TAG,l.get(i).toString());
        }
    }
     void
    hangup (GsmConnection conn) throws CallStateException {
        if (conn.owner != this) {
            throw new CallStateException ("GsmConnection " + conn
                                    + "does not belong to GsmCallTracker " + this);
        }
        if (conn == pendingMO) {
            if (Phone.DEBUG_PHONE) log("hangup: set hangupPendingMO to true");
            hangupPendingMO = true;
        } else {
            try {
                cm.hangupConnection (conn.getGSMIndex(), obtainCompleteMessage());
            } catch (CallStateException ex) {
                Log.w(LOG_TAG,"GsmCallTracker WARN: hangup() on absent connection "
                                + conn);
            }
        }
        conn.onHangupLocal();
    }
     void
    separate (GsmConnection conn) throws CallStateException {
        if (conn.owner != this) {
            throw new CallStateException ("GsmConnection " + conn
                                    + "does not belong to GsmCallTracker " + this);
        }
        try {
            cm.separateConnection (conn.getGSMIndex(),
                obtainCompleteMessage(EVENT_SEPARATE_RESULT));
        } catch (CallStateException ex) {
            Log.w(LOG_TAG,"GsmCallTracker WARN: separate() on absent connection "
                          + conn);
        }
    }
     void
    setMute(boolean mute) {
        desiredMute = mute;
        cm.setMute(desiredMute, null);
    }
     boolean
    getMute() {
        return desiredMute;
    }
     void
    hangup (GsmCall call) throws CallStateException {
        if (call.getConnections().size() == 0) {
            throw new CallStateException("no connections in call");
        }
        if (call == ringingCall) {
            if (Phone.DEBUG_PHONE) log("(ringing) hangup waiting or background");
            cm.hangupWaitingOrBackground(obtainCompleteMessage());
        } else if (call == foregroundCall) {
            if (call.isDialingOrAlerting()) {
                if (Phone.DEBUG_PHONE) {
                    log("(foregnd) hangup dialing or alerting...");
                }
                hangup((GsmConnection)(call.getConnections().get(0)));
            } else {
                hangupForegroundResumeBackground();
            }
        } else if (call == backgroundCall) {
            if (ringingCall.isRinging()) {
                if (Phone.DEBUG_PHONE) {
                    log("hangup all conns in background call");
                }
                hangupAllConnections(call);
            } else {
                hangupWaitingOrBackground();
            }
        } else {
            throw new RuntimeException ("GsmCall " + call +
                    "does not belong to GsmCallTracker " + this);
        }
        call.onHangupLocal();
        phone.notifyPreciseCallStateChanged();
    }
    void hangupWaitingOrBackground() {
        if (Phone.DEBUG_PHONE) log("hangupWaitingOrBackground");
        cm.hangupWaitingOrBackground(obtainCompleteMessage());
    }
    void hangupForegroundResumeBackground() {
        if (Phone.DEBUG_PHONE) log("hangupForegroundResumeBackground");
        cm.hangupForegroundResumeBackground(obtainCompleteMessage());
    }
    void hangupConnectionByIndex(GsmCall call, int index)
            throws CallStateException {
        int count = call.connections.size();
        for (int i = 0; i < count; i++) {
            GsmConnection cn = (GsmConnection)call.connections.get(i);
            if (cn.getGSMIndex() == index) {
                cm.hangupConnection(index, obtainCompleteMessage());
                return;
            }
        }
        throw new CallStateException("no gsm index found");
    }
    void hangupAllConnections(GsmCall call) throws CallStateException{
        try {
            int count = call.connections.size();
            for (int i = 0; i < count; i++) {
                GsmConnection cn = (GsmConnection)call.connections.get(i);
                cm.hangupConnection(cn.getGSMIndex(), obtainCompleteMessage());
            }
        } catch (CallStateException ex) {
            Log.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
        }
    }
    GsmConnection getConnectionByIndex(GsmCall call, int index)
            throws CallStateException {
        int count = call.connections.size();
        for (int i = 0; i < count; i++) {
            GsmConnection cn = (GsmConnection)call.connections.get(i);
            if (cn.getGSMIndex() == index) {
                return cn;
            }
        }
        return null;
    }
    private Phone.SuppService getFailedService(int what) {
        switch (what) {
            case EVENT_SWITCH_RESULT:
                return Phone.SuppService.SWITCH;
            case EVENT_CONFERENCE_RESULT:
                return Phone.SuppService.CONFERENCE;
            case EVENT_SEPARATE_RESULT:
                return Phone.SuppService.SEPARATE;
            case EVENT_ECT_RESULT:
                return Phone.SuppService.TRANSFER;
        }
        return Phone.SuppService.UNKNOWN;
    }
    public void
    handleMessage (Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case EVENT_POLL_CALLS_RESULT:
                ar = (AsyncResult)msg.obj;
                if (msg == lastRelevantPoll) {
                    if (DBG_POLL) log(
                            "handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                    needsPoll = false;
                    lastRelevantPoll = null;
                    handlePollCalls((AsyncResult)msg.obj);
                }
            break;
            case EVENT_OPERATION_COMPLETE:
                ar = (AsyncResult)msg.obj;
                operationComplete();
            break;
            case EVENT_SWITCH_RESULT:
            case EVENT_CONFERENCE_RESULT:
            case EVENT_SEPARATE_RESULT:
            case EVENT_ECT_RESULT:
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    phone.notifySuppServiceFailed(getFailedService(msg.what));
                }
                operationComplete();
            break;
            case EVENT_GET_LAST_CALL_FAIL_CAUSE:
                int causeCode;
                ar = (AsyncResult)msg.obj;
                operationComplete();
                if (ar.exception != null) {
                    causeCode = CallFailCause.NORMAL_CLEARING;
                    Log.i(LOG_TAG,
                            "Exception during getLastCallFailCause, assuming normal disconnect");
                } else {
                    causeCode = ((int[])ar.result)[0];
                }
                if (causeCode == CallFailCause.NO_CIRCUIT_AVAIL ||
                    causeCode == CallFailCause.TEMPORARY_FAILURE ||
                    causeCode == CallFailCause.SWITCHING_CONGESTION ||
                    causeCode == CallFailCause.CHANNEL_NOT_AVAIL ||
                    causeCode == CallFailCause.QOS_NOT_AVAIL ||
                    causeCode == CallFailCause.BEARER_NOT_AVAIL ||
                    causeCode == CallFailCause.ERROR_UNSPECIFIED) {
                    GsmCellLocation loc = ((GsmCellLocation)phone.getCellLocation());
                    EventLog.writeEvent(EventLogTags.CALL_DROP,
                            causeCode, loc != null ? loc.getCid() : -1,
                            TelephonyManager.getDefault().getNetworkType());
                }
                for (int i = 0, s =  droppedDuringPoll.size()
                        ; i < s ; i++
                ) {
                    GsmConnection conn = droppedDuringPoll.get(i);
                    conn.onRemoteDisconnect(causeCode);
                }
                updatePhoneState();
                phone.notifyPreciseCallStateChanged();
                droppedDuringPoll.clear();
            break;
            case EVENT_REPOLL_AFTER_DELAY:
            case EVENT_CALL_STATE_CHANGE:
                pollCallsWhenSafe();
            break;
            case EVENT_RADIO_AVAILABLE:
                handleRadioAvailable();
            break;
            case EVENT_RADIO_NOT_AVAILABLE:
                handleRadioNotAvailable();
            break;
        }
    }
    protected void log(String msg) {
        Log.d(LOG_TAG, "[GsmCallTracker] " + msg);
    }
}
