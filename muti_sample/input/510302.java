public final class SimulatedCommands extends BaseCommands
        implements CommandsInterface, SimulatedRadioControl {
    private final static String LOG_TAG = "SIM";
    private enum SimLockState {
        NONE,
        REQUIRE_PIN,
        REQUIRE_PUK,
        SIM_PERM_LOCKED
    }
    private enum SimFdnState {
        NONE,
        REQUIRE_PIN2,
        REQUIRE_PUK2,
        SIM_PERM_LOCKED
    }
    private final static SimLockState INITIAL_LOCK_STATE = SimLockState.NONE;
    private final static String DEFAULT_SIM_PIN_CODE = "1234";
    private final static String SIM_PUK_CODE = "12345678";
    private final static SimFdnState INITIAL_FDN_STATE = SimFdnState.NONE;
    private final static String DEFAULT_SIM_PIN2_CODE = "5678";
    private final static String SIM_PUK2_CODE = "87654321";
    SimulatedGsmCallState simulatedCallState;
    HandlerThread mHandlerThread;
    SimLockState mSimLockedState;
    boolean mSimLockEnabled;
    int mPinUnlockAttempts;
    int mPukUnlockAttempts;
    String mPinCode;
    SimFdnState mSimFdnEnabledState;
    boolean mSimFdnEnabled;
    int mPin2UnlockAttempts;
    int mPuk2UnlockAttempts;
    int mNetworkType;
    String mPin2Code;
    boolean mSsnNotifyOn = false;
    int pausedResponseCount;
    ArrayList<Message> pausedResponses = new ArrayList<Message>();
    int nextCallFailCause = CallFailCause.NORMAL_CLEARING;
    public
    SimulatedCommands() {
        super(null);  
        mHandlerThread = new HandlerThread("SimulatedCommands");
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        simulatedCallState = new SimulatedGsmCallState(looper);
        setRadioState(RadioState.RADIO_OFF);
        mSimLockedState = INITIAL_LOCK_STATE;
        mSimLockEnabled = (mSimLockedState != SimLockState.NONE);
        mPinCode = DEFAULT_SIM_PIN_CODE;
        mSimFdnEnabledState = INITIAL_FDN_STATE;
        mSimFdnEnabled = (mSimFdnEnabledState != SimFdnState.NONE);
        mPin2Code = DEFAULT_SIM_PIN2_CODE;
    }
    public void getIccCardStatus(Message result) {
        unimplemented(result);
    }
    public void supplyIccPin(String pin, Message result)  {
        if (mSimLockedState != SimLockState.REQUIRE_PIN) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPin: wrong state, state=" +
                    mSimLockedState);
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
            return;
        }
        if (pin != null && pin.equals(mPinCode)) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPin: success!");
            setRadioState(RadioState.SIM_READY);
            mPinUnlockAttempts = 0;
            mSimLockedState = SimLockState.NONE;
            if (result != null) {
                AsyncResult.forMessage(result, null, null);
                result.sendToTarget();
            }
            return;
        }
        if (result != null) {
            mPinUnlockAttempts ++;
            Log.i(LOG_TAG, "[SimCmd] supplyIccPin: failed! attempt=" +
                    mPinUnlockAttempts);
            if (mPinUnlockAttempts >= 3) {
                Log.i(LOG_TAG, "[SimCmd] supplyIccPin: set state to REQUIRE_PUK");
                mSimLockedState = SimLockState.REQUIRE_PUK;
            }
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
    public void supplyIccPuk(String puk, String newPin, Message result)  {
        if (mSimLockedState != SimLockState.REQUIRE_PUK) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPuk: wrong state, state=" +
                    mSimLockedState);
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
            return;
        }
        if (puk != null && puk.equals(SIM_PUK_CODE)) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPuk: success!");
            setRadioState(RadioState.SIM_READY);
            mSimLockedState = SimLockState.NONE;
            mPukUnlockAttempts = 0;
            if (result != null) {
                AsyncResult.forMessage(result, null, null);
                result.sendToTarget();
            }
            return;
        }
        if (result != null) {
            mPukUnlockAttempts ++;
            Log.i(LOG_TAG, "[SimCmd] supplyIccPuk: failed! attempt=" +
                    mPukUnlockAttempts);
            if (mPukUnlockAttempts >= 10) {
                Log.i(LOG_TAG, "[SimCmd] supplyIccPuk: set state to SIM_PERM_LOCKED");
                mSimLockedState = SimLockState.SIM_PERM_LOCKED;
            }
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
    public void supplyIccPin2(String pin2, Message result)  {
        if (mSimFdnEnabledState != SimFdnState.REQUIRE_PIN2) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPin2: wrong state, state=" +
                    mSimFdnEnabledState);
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
            return;
        }
        if (pin2 != null && pin2.equals(mPin2Code)) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPin2: success!");
            mPin2UnlockAttempts = 0;
            mSimFdnEnabledState = SimFdnState.NONE;
            if (result != null) {
                AsyncResult.forMessage(result, null, null);
                result.sendToTarget();
            }
            return;
        }
        if (result != null) {
            mPin2UnlockAttempts ++;
            Log.i(LOG_TAG, "[SimCmd] supplyIccPin2: failed! attempt=" +
                    mPin2UnlockAttempts);
            if (mPin2UnlockAttempts >= 3) {
                Log.i(LOG_TAG, "[SimCmd] supplyIccPin2: set state to REQUIRE_PUK2");
                mSimFdnEnabledState = SimFdnState.REQUIRE_PUK2;
            }
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
    public void supplyIccPuk2(String puk2, String newPin2, Message result)  {
        if (mSimFdnEnabledState != SimFdnState.REQUIRE_PUK2) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPuk2: wrong state, state=" +
                    mSimLockedState);
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
            return;
        }
        if (puk2 != null && puk2.equals(SIM_PUK2_CODE)) {
            Log.i(LOG_TAG, "[SimCmd] supplyIccPuk2: success!");
            mSimFdnEnabledState = SimFdnState.NONE;
            mPuk2UnlockAttempts = 0;
            if (result != null) {
                AsyncResult.forMessage(result, null, null);
                result.sendToTarget();
            }
            return;
        }
        if (result != null) {
            mPuk2UnlockAttempts ++;
            Log.i(LOG_TAG, "[SimCmd] supplyIccPuk2: failed! attempt=" +
                    mPuk2UnlockAttempts);
            if (mPuk2UnlockAttempts >= 10) {
                Log.i(LOG_TAG, "[SimCmd] supplyIccPuk2: set state to SIM_PERM_LOCKED");
                mSimFdnEnabledState = SimFdnState.SIM_PERM_LOCKED;
            }
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
    public void changeIccPin(String oldPin, String newPin, Message result)  {
        if (oldPin != null && oldPin.equals(mPinCode)) {
            mPinCode = newPin;
            if (result != null) {
                AsyncResult.forMessage(result, null, null);
                result.sendToTarget();
            }
            return;
        }
        if (result != null) {
            Log.i(LOG_TAG, "[SimCmd] changeIccPin: pin failed!");
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
    public void changeIccPin2(String oldPin2, String newPin2, Message result)  {
        if (oldPin2 != null && oldPin2.equals(mPin2Code)) {
            mPin2Code = newPin2;
            if (result != null) {
                AsyncResult.forMessage(result, null, null);
                result.sendToTarget();
            }
            return;
        }
        if (result != null) {
            Log.i(LOG_TAG, "[SimCmd] changeIccPin2: pin2 failed!");
            CommandException ex = new CommandException(
                    CommandException.Error.PASSWORD_INCORRECT);
            AsyncResult.forMessage(result, null, ex);
            result.sendToTarget();
        }
    }
    public void
    changeBarringPassword(String facility, String oldPwd, String newPwd, Message result) {
        unimplemented(result);
    }
    public void
    setSuppServiceNotifications(boolean enable, Message result) {
        resultSuccess(result, null);
        if (enable && mSsnNotifyOn) {
            Log.w(LOG_TAG, "Supp Service Notifications already enabled!");
        }
        mSsnNotifyOn = enable;
    }
    public void queryFacilityLock (String facility, String pin,
                                   int serviceClass, Message result) {
        if (facility != null &&
                facility.equals(CommandsInterface.CB_FACILITY_BA_SIM)) {
            if (result != null) {
                int[] r = new int[1];
                r[0] = (mSimLockEnabled ? 1 : 0);
                Log.i(LOG_TAG, "[SimCmd] queryFacilityLock: SIM is " +
                        (r[0] == 0 ? "unlocked" : "locked"));
                AsyncResult.forMessage(result, r, null);
                result.sendToTarget();
            }
            return;
        } else if (facility != null &&
                facility.equals(CommandsInterface.CB_FACILITY_BA_FD)) {
            if (result != null) {
                int[] r = new int[1];
                r[0] = (mSimFdnEnabled ? 1 : 0);
                Log.i(LOG_TAG, "[SimCmd] queryFacilityLock: FDN is " +
                        (r[0] == 0 ? "disabled" : "enabled"));
                AsyncResult.forMessage(result, r, null);
                result.sendToTarget();
            }
            return;
        }
        unimplemented(result);
    }
    public void setFacilityLock (String facility, boolean lockEnabled,
                                 String pin, int serviceClass,
                                 Message result) {
        if (facility != null &&
                facility.equals(CommandsInterface.CB_FACILITY_BA_SIM)) {
            if (pin != null && pin.equals(mPinCode)) {
                Log.i(LOG_TAG, "[SimCmd] setFacilityLock: pin is valid");
                mSimLockEnabled = lockEnabled;
                if (result != null) {
                    AsyncResult.forMessage(result, null, null);
                    result.sendToTarget();
                }
                return;
            }
            if (result != null) {
                Log.i(LOG_TAG, "[SimCmd] setFacilityLock: pin failed!");
                CommandException ex = new CommandException(
                        CommandException.Error.GENERIC_FAILURE);
                AsyncResult.forMessage(result, null, ex);
                result.sendToTarget();
            }
            return;
        }  else if (facility != null &&
                facility.equals(CommandsInterface.CB_FACILITY_BA_FD)) {
            if (pin != null && pin.equals(mPin2Code)) {
                Log.i(LOG_TAG, "[SimCmd] setFacilityLock: pin2 is valid");
                mSimFdnEnabled = lockEnabled;
                if (result != null) {
                    AsyncResult.forMessage(result, null, null);
                    result.sendToTarget();
                }
                return;
            }
            if (result != null) {
                Log.i(LOG_TAG, "[SimCmd] setFacilityLock: pin2 failed!");
                CommandException ex = new CommandException(
                        CommandException.Error.GENERIC_FAILURE);
                AsyncResult.forMessage(result, null, ex);
                result.sendToTarget();
            }
            return;
        }
        unimplemented(result);
    }
    public void supplyNetworkDepersonalization(String netpin, Message result)  {
        unimplemented(result);
    }
    public void getCurrentCalls (Message result) {
        if (mState == RadioState.SIM_READY) {
            resultSuccess(result, simulatedCallState.getDriverCalls());
        } else {
            resultFail(result,
                new CommandException(
                    CommandException.Error.RADIO_NOT_AVAILABLE));
        }
    }
    public void getPDPContextList(Message result) {
        getDataCallList(result);
    }
    public void getDataCallList(Message result) {
        resultSuccess(result, new ArrayList<DataCallState>(0));
    }
    public void dial (String address, int clirMode, Message result) {
        simulatedCallState.onDial(address);
        resultSuccess(result, null);
    }
    public void getIMSI(Message result) {
        resultSuccess(result, "012345678901234");
    }
    public void getIMEI(Message result) {
        resultSuccess(result, "012345678901234");
    }
    public void getIMEISV(Message result) {
        resultSuccess(result, "99");
    }
    public void hangupConnection (int gsmIndex, Message result) {
        boolean success;
        success = simulatedCallState.onChld('1', (char)('0'+gsmIndex));
        if (!success){
            Log.i("GSM", "[SimCmd] hangupConnection: resultFail");
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            Log.i("GSM", "[SimCmd] hangupConnection: resultSuccess");
            resultSuccess(result, null);
        }
    }
    public void hangupWaitingOrBackground (Message result) {
        boolean success;
        success = simulatedCallState.onChld('0', '\0');
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void hangupForegroundResumeBackground (Message result) {
        boolean success;
        success = simulatedCallState.onChld('1', '\0');
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void switchWaitingOrHoldingAndActive (Message result) {
        boolean success;
        success = simulatedCallState.onChld('2', '\0');
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void conference (Message result) {
        boolean success;
        success = simulatedCallState.onChld('3', '\0');
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void explicitCallTransfer (Message result) {
        boolean success;
        success = simulatedCallState.onChld('4', '\0');
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void separateConnection (int gsmIndex, Message result) {
        boolean success;
        char ch = (char)(gsmIndex + '0');
        success = simulatedCallState.onChld('2', ch);
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void acceptCall (Message result) {
        boolean success;
        success = simulatedCallState.onAnswer();
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void rejectCall (Message result) {
        boolean success;
        success = simulatedCallState.onChld('0', '\0');
        if (!success){
            resultFail(result, new RuntimeException("Hangup Error"));
        } else {
            resultSuccess(result, null);
        }
    }
    public void getLastCallFailCause (Message result) {
        int[] ret = new int[1];
        ret[0] = nextCallFailCause;
        resultSuccess(result, ret);
    }
    public void getLastPdpFailCause (Message result) {
        unimplemented(result);
    }
    public void getLastDataCallFailCause(Message result) {
        unimplemented(result);
    }
    public void setMute (boolean enableMute, Message result) {unimplemented(result);}
    public void getMute (Message result) {unimplemented(result);}
    public void getSignalStrength (Message result) {
        int ret[] = new int[2];
        ret[0] = 23;
        ret[1] = 0;
        resultSuccess(result, ret);
    }
    public void setBandMode (int bandMode, Message result) {
        resultSuccess(result, null);
    }
    public void queryAvailableBandMode (Message result) {
        int ret[] = new int [4];
        ret[0] = 4;
        ret[1] = Phone.BM_US_BAND;
        ret[2] = Phone.BM_JPN_BAND;
        ret[3] = Phone.BM_AUS_BAND;
        resultSuccess(result, ret);
    }
    public void sendTerminalResponse(String contents, Message response) {
        resultSuccess(response, null);
    }
    public void sendEnvelope(String contents, Message response) {
        resultSuccess(response, null);
    }
    public void handleCallSetupRequestFromSim(
            boolean accept, Message response) {
        resultSuccess(response, null);
    }
    public void getRegistrationState (Message result) {
        String ret[] = new String[14];
        ret[0] = "5"; 
        ret[1] = null;
        ret[2] = null;
        ret[3] = null;
        ret[4] = null;
        ret[5] = null;
        ret[6] = null;
        ret[7] = null;
        ret[8] = null;
        ret[9] = null;
        ret[10] = null;
        ret[11] = null;
        ret[12] = null;
        ret[13] = null;
        ret[14] = null;
        resultSuccess(result, ret);
    }
    public void getGPRSRegistrationState (Message result) {
        String ret[] = new String[4];
        ret[0] = "5"; 
        ret[1] = null;
        ret[2] = null;
        ret[3] = "2";
        resultSuccess(result, ret);
    }
    public void getOperator(Message result) {
        String[] ret = new String[3];
        ret[0] = "El Telco Loco";
        ret[1] = "Telco Loco";
        ret[2] = "001001";
        resultSuccess(result, ret);
    }
    public void sendDtmf(char c, Message result) {
        resultSuccess(result, null);
    }
    public void startDtmf(char c, Message result) {
        resultSuccess(result, null);
    }
    public void stopDtmf(Message result) {
        resultSuccess(result, null);
    }
    public void sendBurstDtmf(String dtmfString, int on, int off, Message result) {
        resultSuccess(result, null);
    }
    public void sendSMS (String smscPDU, String pdu, Message result) {unimplemented(result);}
    public void deleteSmsOnSim(int index, Message response) {
        Log.d(LOG_TAG, "Delete message at index " + index);
        unimplemented(response);
    }
    public void deleteSmsOnRuim(int index, Message response) {
        Log.d(LOG_TAG, "Delete RUIM message at index " + index);
        unimplemented(response);
    }
    public void writeSmsToSim(int status, String smsc, String pdu, Message response) {
        Log.d(LOG_TAG, "Write SMS to SIM with status " + status);
        unimplemented(response);
    }
    public void writeSmsToRuim(int status, String pdu, Message response) {
        Log.d(LOG_TAG, "Write SMS to RUIM with status " + status);
        unimplemented(response);
    }
    public void setupDefaultPDP(String apn, String user, String password, Message result) {
        unimplemented(result);
    }
    public void setupDataCall(String radioTechnology, String profile, String apn, String user,
            String password, String authType, Message result) {
        unimplemented(result);
    }
    public void deactivateDataCall(int cid, Message result) {unimplemented(result);}
    public void deactivateDefaultPDP(int cid, Message result) {unimplemented(result);}
    public void setPreferredNetworkType(int networkType , Message result) {
        mNetworkType = networkType;
        resultSuccess(result, null);
    }
    public void getPreferredNetworkType(Message result) {
        int ret[] = new int[1];
        ret[0] = mNetworkType;
        resultSuccess(result, ret);
    }
    public void getNeighboringCids(Message result) {
        int ret[] = new int[7];
        ret[0] = 6;
        for (int i = 1; i<7; i++) {
            ret[i] = i;
        }
        resultSuccess(result, ret);
    }
    public void setLocationUpdates(boolean enable, Message response) {
        unimplemented(response);
    }
    public void getSmscAddress(Message result) {
        unimplemented(result);
    }
    public void setSmscAddress(String address, Message result) {
        unimplemented(result);
    }
    public void reportSmsMemoryStatus(boolean available, Message result) {
        unimplemented(result);
    }
    public void reportStkServiceIsRunning(Message result) {
        resultSuccess(result, null);
    }
    private boolean isSimLocked() {
        if (mSimLockedState != SimLockState.NONE) {
            return true;
        }
        return false;
    }
    public void setRadioPower(boolean on, Message result) {
        if(on) {
            if (isSimLocked()) {
                Log.i("SIM", "[SimCmd] setRadioPower: SIM locked! state=" +
                        mSimLockedState);
                setRadioState(RadioState.SIM_LOCKED_OR_ABSENT);
            }
            else {
                setRadioState(RadioState.SIM_READY);
            }
        } else {
            setRadioState(RadioState.RADIO_OFF);
        }
    }
    public void acknowledgeLastIncomingGsmSms(boolean success, int cause, Message result) {
        unimplemented(result);
    }
    public void acknowledgeLastIncomingCdmaSms(boolean success, int cause, Message result) {
        unimplemented(result);
    }
    public void iccIO (int command, int fileid, String path, int p1, int p2,
                       int p3, String data, String pin2, Message result) {
        unimplemented(result);
    }
    public void queryCLIP(Message response) { unimplemented(response); }
    public void getCLIR(Message result) {unimplemented(result);}
    public void setCLIR(int clirMode, Message result) {unimplemented(result);}
    public void queryCallWaiting(int serviceClass, Message response) {
        unimplemented(response);
    }
    public void setCallWaiting(boolean enable, int serviceClass,
            Message response) {
        unimplemented(response);
    }
    public void setCallForward(int action, int cfReason, int serviceClass,
            String number, int timeSeconds, Message result) {unimplemented(result);}
    public void queryCallForwardStatus(int cfReason, int serviceClass,
            String number, Message result) {unimplemented(result);}
    public void setNetworkSelectionModeAutomatic(Message result) {unimplemented(result);}
    public void exitEmergencyCallbackMode(Message result) {unimplemented(result);}
    public void setNetworkSelectionModeManual(
            String operatorNumeric, Message result) {unimplemented(result);}
    public void getNetworkSelectionMode(Message result) {
        int ret[] = new int[1];
        ret[0] = 0;
        resultSuccess(result, ret);
    }
    public void getAvailableNetworks(Message result) {unimplemented(result);}
    public void getBasebandVersion (Message result) {
        resultSuccess(result, "SimulatedCommands");
    }
    public void triggerIncomingUssd(String statusCode, String message) {
        if (mUSSDRegistrant != null) {
            String[] result = {statusCode, message};
            mUSSDRegistrant.notifyResult(result);
        }
    }
    public void sendUSSD (String ussdString, Message result) {
        if (ussdString.equals("#646#")) {
            resultSuccess(result, null);
            triggerIncomingUssd("0", "You have NNN minutes remaining.");
        } else {
            resultSuccess(result, null);
            triggerIncomingUssd("0", "All Done");
        }
    }
    public void cancelPendingUssd (Message response) {
        resultSuccess(response, null);
    }
    public void resetRadio(Message result) {
        unimplemented(result);
    }
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        if (response != null) {
            AsyncResult.forMessage(response).result = data;
            response.sendToTarget();
        }
    }
    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        if (response != null) {
            AsyncResult.forMessage(response).result = strings;
            response.sendToTarget();
        }
    }
    public void
    triggerRing(String number) {
        simulatedCallState.triggerRing(number);
        mCallStateRegistrants.notifyRegistrants();
    }
    public void
    progressConnectingCallState() {
        simulatedCallState.progressConnectingCallState();
        mCallStateRegistrants.notifyRegistrants();
    }
    public void
    progressConnectingToActive() {
        simulatedCallState.progressConnectingToActive();
        mCallStateRegistrants.notifyRegistrants();
    }
    public void
    setAutoProgressConnectingCall(boolean b) {
        simulatedCallState.setAutoProgressConnectingCall(b);
    }
    public void
    setNextDialFailImmediately(boolean b) {
        simulatedCallState.setNextDialFailImmediately(b);
    }
    public void
    setNextCallFailCause(int gsmCause) {
        nextCallFailCause = gsmCause;
    }
    public void
    triggerHangupForeground() {
        simulatedCallState.triggerHangupForeground();
        mCallStateRegistrants.notifyRegistrants();
    }
    public void
    triggerHangupBackground() {
        simulatedCallState.triggerHangupBackground();
        mCallStateRegistrants.notifyRegistrants();
    }
    public void triggerSsn(int type, int code) {
        SuppServiceNotification not = new SuppServiceNotification();
        not.notificationType = type;
        not.code = code;
        mSsnRegistrant.notifyRegistrant(new AsyncResult(null, not, null));
    }
    public void
    shutdown() {
        setRadioState(RadioState.RADIO_UNAVAILABLE);
        Looper looper = mHandlerThread.getLooper();
        if (looper != null) {
            looper.quit();
        }
    }
    public void
    triggerHangupAll() {
        simulatedCallState.triggerHangupAll();
        mCallStateRegistrants.notifyRegistrants();
    }
    public void
    triggerIncomingSMS(String message) {
    }
    public void
    pauseResponses() {
        pausedResponseCount++;
    }
    public void
    resumeResponses() {
        pausedResponseCount--;
        if (pausedResponseCount == 0) {
            for (int i = 0, s = pausedResponses.size(); i < s ; i++) {
                pausedResponses.get(i).sendToTarget();
            }
            pausedResponses.clear();
        } else {
            Log.e("GSM", "SimulatedCommands.resumeResponses < 0");
        }
    }
    private void unimplemented(Message result) {
        if (result != null) {
            AsyncResult.forMessage(result).exception
                = new RuntimeException("Unimplemented");
            if (pausedResponseCount > 0) {
                pausedResponses.add(result);
            } else {
                result.sendToTarget();
            }
        }
    }
    private void resultSuccess(Message result, Object ret) {
        if (result != null) {
            AsyncResult.forMessage(result).result = ret;
            if (pausedResponseCount > 0) {
                pausedResponses.add(result);
            } else {
                result.sendToTarget();
            }
        }
    }
    private void resultFail(Message result, Throwable tr) {
        if (result != null) {
            AsyncResult.forMessage(result).exception = tr;
            if (pausedResponseCount > 0) {
                pausedResponses.add(result);
            } else {
                result.sendToTarget();
            }
        }
    }
    public void
    getDeviceIdentity(Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void
    getCDMASubscription(Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void
    setCdmaSubscription(int cdmaSubscriptionType, Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void queryCdmaRoamingPreference(Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void
    setPhoneType(int phoneType) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
    }
    public void getPreferredVoicePrivacy(Message result) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(result);
    }
    public void setPreferredVoicePrivacy(boolean enable, Message result) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(result);
    }
    public void setTTYMode(int ttyMode, Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void queryTTYMode(Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void sendCDMAFeatureCode(String FeatureCode, Message response) {
        Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
        unimplemented(response);
    }
    public void sendCdmaSms(byte[] pdu, Message response){
       Log.w(LOG_TAG, "CDMA not implemented in SimulatedCommands");
    }
    public void setCdmaBroadcastActivation(boolean activate, Message response) {
        unimplemented(response);
    }
    public void getCdmaBroadcastConfig(Message response) {
        unimplemented(response);
    }
    public void setCdmaBroadcastConfig(int[] configValuesArray, Message response) {
        unimplemented(response);
    }
    public void forceDataDormancy(Message response) {
        unimplemented(response);
    }
    public void setGsmBroadcastActivation(boolean activate, Message response) {
        unimplemented(response);
    }
    public void setGsmBroadcastConfig(SmsBroadcastConfigInfo[] config, Message response) {
        unimplemented(response);
    }
    public void getGsmBroadcastConfig(Message response) {
        unimplemented(response);
    }
}
