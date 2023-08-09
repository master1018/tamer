public final class CdmaCallTracker extends CallTracker {
    static final String LOG_TAG = "CDMA";
    private static final boolean REPEAT_POLLING = false;
    private static final boolean DBG_POLL = false;
    static final int MAX_CONNECTIONS = 1;   
    static final int MAX_CONNECTIONS_PER_CALL = 1; 
    CdmaConnection connections[] = new CdmaConnection[MAX_CONNECTIONS];
    RegistrantList voiceCallEndedRegistrants = new RegistrantList();
    RegistrantList voiceCallStartedRegistrants = new RegistrantList();
    RegistrantList callWaitingRegistrants =  new RegistrantList();
    ArrayList<CdmaConnection> droppedDuringPoll
        = new ArrayList<CdmaConnection>(MAX_CONNECTIONS);
    CdmaCall ringingCall = new CdmaCall(this);
    CdmaCall foregroundCall = new CdmaCall(this);
    CdmaCall backgroundCall = new CdmaCall(this);
    CdmaConnection pendingMO;
    boolean hangupPendingMO;
    boolean pendingCallInEcm=false;
    boolean mIsInEmergencyCall = false;
    CDMAPhone phone;
    boolean desiredMute = false;    
    int pendingCallClirMode;
    Phone.State state = Phone.State.IDLE;
    private boolean mIsEcmTimerCanceled = false;
    CdmaCallTracker(CDMAPhone phone) {
        this.phone = phone;
        cm = phone.mCM;
        cm.registerForCallStateChanged(this, EVENT_CALL_STATE_CHANGE, null);
        cm.registerForOn(this, EVENT_RADIO_AVAILABLE, null);
        cm.registerForNotAvailable(this, EVENT_RADIO_NOT_AVAILABLE, null);
        cm.registerForCallWaitingInfo(this, EVENT_CALL_WAITING_INFO_CDMA, null);
        foregroundCall.setGeneric(false);
    }
    public void dispose() {
        cm.unregisterForCallStateChanged(this);
        cm.unregisterForOn(this);
        cm.unregisterForNotAvailable(this);
        cm.unregisterForCallWaitingInfo(this);
        for(CdmaConnection c : connections) {
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
    @Override
    protected void finalize() {
        Log.d(LOG_TAG, "CdmaCallTracker finalized");
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
    public void registerForCallWaiting(Handler h, int what, Object obj) {
        Registrant r = new Registrant (h, what, obj);
        callWaitingRegistrants.add(r);
    }
    public void unregisterForCallWaiting(Handler h) {
        callWaitingRegistrants.remove(h);
    }
    private void
    fakeHoldForegroundBeforeDial() {
        List<Connection> connCopy;
        connCopy = (List<Connection>) foregroundCall.connections.clone();
        for (int i = 0, s = connCopy.size() ; i < s ; i++) {
            CdmaConnection conn = (CdmaConnection)connCopy.get(i);
            conn.fakeHoldBeforeDial();
        }
    }
    Connection
    dial (String dialString, int clirMode) throws CallStateException {
        clearDisconnected();
        if (!canDial()) {
            throw new CallStateException("cannot dial in current state");
        }
        String inEcm=SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        boolean isPhoneInEcmMode = inEcm.equals("true");
        boolean isEmergencyCall = PhoneNumberUtils.isEmergencyNumber(dialString);
        if (isPhoneInEcmMode && isEmergencyCall) {
            handleEcmTimer(phone.CANCEL_ECM_TIMER);
        }
        foregroundCall.setGeneric(false);
        if (foregroundCall.getState() == CdmaCall.State.ACTIVE) {
            return dialThreeWay(dialString);
        }
        pendingMO = new CdmaConnection(phone.getContext(), dialString, this, foregroundCall);
        hangupPendingMO = false;
        if (pendingMO.address == null || pendingMO.address.length() == 0
            || pendingMO.address.indexOf(PhoneNumberUtils.WILD) >= 0) {
            pendingMO.cause = Connection.DisconnectCause.INVALID_NUMBER;
            pollCallsWhenSafe();
        } else {
            setMute(false);
            disableDataCallInEmergencyCall(dialString);
            if(!isPhoneInEcmMode || (isPhoneInEcmMode && isEmergencyCall)) {
                cm.dial(pendingMO.address, clirMode, obtainCompleteMessage());
            } else {
                phone.exitEmergencyCallbackMode();
                phone.setOnEcbModeExitResponse(this,EVENT_EXIT_ECM_RESPONSE_CDMA, null);
                pendingCallClirMode=clirMode;
                pendingCallInEcm=true;
            }
        }
        updatePhoneState();
        phone.notifyPreciseCallStateChanged();
        return pendingMO;
    }
    Connection
    dial (String dialString) throws CallStateException {
        return dial(dialString, CommandsInterface.CLIR_DEFAULT);
    }
    private Connection
    dialThreeWay (String dialString) {
        if (!foregroundCall.isIdle()) {
            disableDataCallInEmergencyCall(dialString);
            pendingMO = new CdmaConnection(phone.getContext(),
                                dialString, this, foregroundCall);
            cm.sendCDMAFeatureCode(pendingMO.address,
                obtainMessage(EVENT_THREE_WAY_DIAL_L2_RESULT_CDMA));
            return pendingMO;
        }
        return null;
    }
    void
    acceptCall() throws CallStateException {
        if (ringingCall.getState() == CdmaCall.State.INCOMING) {
            Log.i("phone", "acceptCall: incoming...");
            setMute(false);
            cm.acceptCall(obtainCompleteMessage());
        } else if (ringingCall.getState() == CdmaCall.State.WAITING) {
            CdmaConnection cwConn = (CdmaConnection)(ringingCall.getLatestConnection());
            cwConn.updateParent(ringingCall, foregroundCall);
            cwConn.onConnectedInOrOut();
            updatePhoneState();
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
        if (ringingCall.getState() == CdmaCall.State.INCOMING) {
            throw new CallStateException("cannot be in the incoming state");
        } else if (foregroundCall.getConnections().size() > 1) {
            flashAndSetGenericTrue();
        } else {
            cm.sendCDMAFeatureCode("", obtainMessage(EVENT_SWITCH_RESULT));
        }
    }
    void
    conference() throws CallStateException {
        flashAndSetGenericTrue();
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
        return foregroundCall.getState() == CdmaCall.State.ACTIVE
                && backgroundCall.getState() == CdmaCall.State.HOLDING
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
                    || (foregroundCall.getState() == CdmaCall.State.ACTIVE)
                    || !backgroundCall.getState().isAlive());
        return ret;
    }
    boolean
    canTransfer() {
        Log.e(LOG_TAG, "canTransfer: not possible in CDMA");
        return false;
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
            Log.e(LOG_TAG,"CdmaCallTracker.pendingOperations < 0");
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
        if (Phone.DEBUG_PHONE) {
            log("update phone state, old=" + oldState + " new="+ state);
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
            CdmaConnection conn = connections[i];
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
                        if (mIsEcmTimerCanceled) {
                            handleEcmTimer(phone.RESTART_ECM_TIMER);
                        }
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
                    if (Phone.DEBUG_PHONE) {
                        log("pendingMo=" + pendingMO + ", dc=" + dc);
                    }
                    newRinging = checkMtFindNewRinging(dc,i);
                    if (newRinging == null) {
                        unknownConnectionAppeared = true;
                    }
                    checkAndEnableDataCallAfterEmergencyCallDropped();
                }
                hasNonHangupStateChanged = true;
            } else if (conn != null && dc == null) {
                int count = foregroundCall.connections.size();
                for (int n = 0; n < count; n++) {
                    if (Phone.DEBUG_PHONE) log("adding fgCall cn " + n + " to droppedDuringPoll");
                    CdmaConnection cn = (CdmaConnection)foregroundCall.connections.get(n);
                    droppedDuringPoll.add(cn);
                }
                count = ringingCall.connections.size();
                for (int n = 0; n < count; n++) {
                    if (Phone.DEBUG_PHONE) log("adding rgCall cn " + n + " to droppedDuringPoll");
                    CdmaConnection cn = (CdmaConnection)ringingCall.connections.get(n);
                    droppedDuringPoll.add(cn);
                }
                foregroundCall.setGeneric(false);
                ringingCall.setGeneric(false);
                if (mIsEcmTimerCanceled) {
                    handleEcmTimer(phone.RESTART_ECM_TIMER);
                }
                checkAndEnableDataCallAfterEmergencyCallDropped();
                connections[i] = null;
            } else if (conn != null && dc != null) { 
                if (conn.isIncoming != dc.isMT) {
                    if (dc.isMT == true){
                        droppedDuringPoll.add(conn);
                        newRinging = checkMtFindNewRinging(dc,i);
                        if (newRinging == null) {
                            unknownConnectionAppeared = true;
                        }
                        checkAndEnableDataCallAfterEmergencyCallDropped();
                    } else {
                        Log.e(LOG_TAG,"Error in RIL, Phantom call appeared " + dc);
                    }
                } else {
                    boolean changed;
                    changed = conn.update(dc);
                    hasNonHangupStateChanged = hasNonHangupStateChanged || changed;
                }
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
            if( pendingCallInEcm) {
                pendingCallInEcm = false;
            }
        }
        if (newRinging != null) {
            phone.notifyNewRingingConnection(newRinging);
        }
        for (int i = droppedDuringPoll.size() - 1; i >= 0 ; i--) {
            CdmaConnection conn = droppedDuringPoll.get(i);
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
            } else if (conn.cause == Connection.DisconnectCause.INVALID_NUMBER) {
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
     void
    hangup (CdmaConnection conn) throws CallStateException {
        if (conn.owner != this) {
            throw new CallStateException ("CdmaConnection " + conn
                                    + "does not belong to CdmaCallTracker " + this);
        }
        if (conn == pendingMO) {
            if (Phone.DEBUG_PHONE) log("hangup: set hangupPendingMO to true");
            hangupPendingMO = true;
        } else if ((conn.getCall() == ringingCall)
                && (ringingCall.getState() == CdmaCall.State.WAITING)) {
            conn.onLocalDisconnect();
            updatePhoneState();
            phone.notifyPreciseCallStateChanged();
            return;
        } else {
            try {
                cm.hangupConnection (conn.getCDMAIndex(), obtainCompleteMessage());
            } catch (CallStateException ex) {
                Log.w(LOG_TAG,"CdmaCallTracker WARN: hangup() on absent connection "
                                + conn);
            }
        }
        conn.onHangupLocal();
    }
     void
    separate (CdmaConnection conn) throws CallStateException {
        if (conn.owner != this) {
            throw new CallStateException ("CdmaConnection " + conn
                                    + "does not belong to CdmaCallTracker " + this);
        }
        try {
            cm.separateConnection (conn.getCDMAIndex(),
                obtainCompleteMessage(EVENT_SEPARATE_RESULT));
        } catch (CallStateException ex) {
            Log.w(LOG_TAG,"CdmaCallTracker WARN: separate() on absent connection "
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
    hangup (CdmaCall call) throws CallStateException {
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
                hangup((CdmaConnection)(call.getConnections().get(0)));
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
            throw new RuntimeException ("CdmaCall " + call +
                    "does not belong to CdmaCallTracker " + this);
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
    void hangupConnectionByIndex(CdmaCall call, int index)
            throws CallStateException {
        int count = call.connections.size();
        for (int i = 0; i < count; i++) {
            CdmaConnection cn = (CdmaConnection)call.connections.get(i);
            if (cn.getCDMAIndex() == index) {
                cm.hangupConnection(index, obtainCompleteMessage());
                return;
            }
        }
        throw new CallStateException("no gsm index found");
    }
    void hangupAllConnections(CdmaCall call) throws CallStateException{
        try {
            int count = call.connections.size();
            for (int i = 0; i < count; i++) {
                CdmaConnection cn = (CdmaConnection)call.connections.get(i);
                cm.hangupConnection(cn.getCDMAIndex(), obtainCompleteMessage());
            }
        } catch (CallStateException ex) {
            Log.e(LOG_TAG, "hangupConnectionByIndex caught " + ex);
        }
    }
    CdmaConnection getConnectionByIndex(CdmaCall call, int index)
            throws CallStateException {
        int count = call.connections.size();
        for (int i = 0; i < count; i++) {
            CdmaConnection cn = (CdmaConnection)call.connections.get(i);
            if (cn.getCDMAIndex() == index) {
                return cn;
            }
        }
        return null;
    }
    private void flashAndSetGenericTrue() throws CallStateException {
        cm.sendCDMAFeatureCode("", obtainMessage(EVENT_SWITCH_RESULT));
        foregroundCall.setGeneric(true);
        phone.notifyPreciseCallStateChanged();
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
    private void handleRadioNotAvailable() {
        pollCallsWhenSafe();
    }
    private void notifyCallWaitingInfo(CdmaCallWaitingNotification obj) {
        if (callWaitingRegistrants != null) {
            callWaitingRegistrants.notifyRegistrants(new AsyncResult(null, obj, null));
        }
    }
    private void handleCallWaitingInfo (CdmaCallWaitingNotification cw) {
        if (foregroundCall.connections.size() > 1 ) {
            foregroundCall.setGeneric(true);
        }
        ringingCall.setGeneric(false);
        new CdmaConnection(phone.getContext(), cw, this, ringingCall);
        updatePhoneState();
        notifyCallWaitingInfo(cw);
    }
    public void
    handleMessage (Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case EVENT_POLL_CALLS_RESULT:{
                Log.d(LOG_TAG, "Event EVENT_POLL_CALLS_RESULT Received");
                ar = (AsyncResult)msg.obj;
                if(msg == lastRelevantPoll) {
                    if(DBG_POLL) log(
                            "handle EVENT_POLL_CALL_RESULT: set needsPoll=F");
                    needsPoll = false;
                    lastRelevantPoll = null;
                    handlePollCalls((AsyncResult)msg.obj);
                }
            }
            break;
            case EVENT_OPERATION_COMPLETE:
                operationComplete();
            break;
            case EVENT_SWITCH_RESULT:
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
                for (int i = 0, s =  droppedDuringPoll.size()
                        ; i < s ; i++
                ) {
                    CdmaConnection conn = droppedDuringPoll.get(i);
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
            case EVENT_EXIT_ECM_RESPONSE_CDMA:
               if (pendingCallInEcm) {
                   cm.dial(pendingMO.address, pendingCallClirMode, obtainCompleteMessage());
                   pendingCallInEcm = false;
               }
               phone.unsetOnEcbModeExitResponse(this);
            break;
            case EVENT_CALL_WAITING_INFO_CDMA:
               ar = (AsyncResult)msg.obj;
               if (ar.exception == null) {
                   handleCallWaitingInfo((CdmaCallWaitingNotification)ar.result);
                   Log.d(LOG_TAG, "Event EVENT_CALL_WAITING_INFO_CDMA Received");
               }
            break;
            case EVENT_THREE_WAY_DIAL_L2_RESULT_CDMA:
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    pendingMO.onConnectedInOrOut();
                    pendingMO = null;
                }
            break;
            default:{
               throw new RuntimeException("unexpected event not handled");
            }
        }
    }
    private void handleEcmTimer(int action) {
        phone.handleTimerInEmergencyCallbackMode(action);
        switch(action) {
        case CDMAPhone.CANCEL_ECM_TIMER: mIsEcmTimerCanceled = true; break;
        case CDMAPhone.RESTART_ECM_TIMER: mIsEcmTimerCanceled = false; break;
        default:
            Log.e(LOG_TAG, "handleEcmTimer, unsupported action " + action);
        }
    }
    private void disableDataCallInEmergencyCall(String dialString) {
        if (PhoneNumberUtils.isEmergencyNumber(dialString)) {
            if (Phone.DEBUG_PHONE) log("disableDataCallInEmergencyCall");
            mIsInEmergencyCall = true;
            phone.disableDataConnectivity();
        }
    }
    private void checkAndEnableDataCallAfterEmergencyCallDropped() {
        if (mIsInEmergencyCall) {
            mIsInEmergencyCall = false;
            String inEcm=SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
            if (Phone.DEBUG_PHONE) {
                log("checkAndEnableDataCallAfterEmergencyCallDropped,inEcm=" + inEcm);
            }
            if (inEcm.compareTo("false") == 0) {
                phone.mDataConnection.setDataEnabled(true);
            }
        }
    }
    private Connection checkMtFindNewRinging(DriverCall dc, int i) {
        Connection newRinging = null;
        connections[i] = new CdmaConnection(phone.getContext(), dc, this, i);
        if (connections[i].getCall() == ringingCall) {
            newRinging = connections[i];
            if (Phone.DEBUG_PHONE) log("Notify new ring " + dc);
        } else {
            Log.e(LOG_TAG,"Phantom call appeared " + dc);
            if (dc.state != DriverCall.State.ALERTING
                && dc.state != DriverCall.State.DIALING) {
                connections[i].connectTime = System.currentTimeMillis();
            }
        }
        return newRinging;
    }
    boolean isInEmergencyCall() {
        return mIsInEmergencyCall;
    }
    protected void log(String msg) {
        Log.d(LOG_TAG, "[CdmaCallTracker] " + msg);
    }
}
