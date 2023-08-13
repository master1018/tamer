public class InCallControlState {
    private static final String LOG_TAG = "InCallControlState";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private InCallScreen mInCallScreen;
    private Phone mPhone;
    public boolean manageConferenceVisible;
    public boolean manageConferenceEnabled;
    public boolean canAddCall;
    public boolean canSwap;
    public boolean canMerge;
    public boolean bluetoothEnabled;
    public boolean bluetoothIndicatorOn;
    public boolean speakerEnabled;
    public boolean speakerOn;
    public boolean canMute;
    public boolean muteIndicatorOn;
    public boolean dialpadEnabled;
    public boolean dialpadVisible;
    public boolean supportsHold;
    public boolean onHold;
    public boolean canHold;
    public InCallControlState(InCallScreen inCallScreen, Phone phone) {
        if (DBG) log("InCallControlState constructor...");
        mInCallScreen = inCallScreen;
        mPhone = phone;
    }
    public void update() {
        final boolean hasRingingCall = !mPhone.getRingingCall().isIdle();
        final Call fgCall = mPhone.getForegroundCall();
        final Call.State fgCallState = fgCall.getState();
        final boolean hasActiveForegroundCall = (fgCallState == Call.State.ACTIVE);
        final boolean hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
        int phoneType = mPhone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_GSM) {
            manageConferenceVisible = PhoneUtils.isConferenceCall(fgCall);
            manageConferenceEnabled =
                    manageConferenceVisible && !mInCallScreen.isManageConferenceMode();
        } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
            manageConferenceVisible = false;
            manageConferenceEnabled = false;
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        canAddCall = PhoneUtils.okToAddCall(mPhone);
        canSwap = PhoneUtils.okToSwapCalls(mPhone);
        canMerge = PhoneUtils.okToMergeCalls(mPhone);
        if (mInCallScreen.isBluetoothAvailable()) {
            bluetoothEnabled = true;
            bluetoothIndicatorOn = mInCallScreen.isBluetoothAudioConnectedOrPending();
        } else {
            bluetoothEnabled = false;
            bluetoothIndicatorOn = false;
        }
        speakerEnabled = true;
        speakerOn = PhoneUtils.isSpeakerOn(mInCallScreen);
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            Connection c = fgCall.getLatestConnection();
            boolean isEmergencyCall = false;
            if (c != null) isEmergencyCall = PhoneNumberUtils.isEmergencyNumber(c.getAddress());
            if (isEmergencyCall) { 
                canMute = false;
                muteIndicatorOn = false;
            } else {
                canMute = hasActiveForegroundCall;
                muteIndicatorOn = PhoneUtils.getMute(mPhone);
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            canMute = hasActiveForegroundCall;
            muteIndicatorOn = PhoneUtils.getMute(mPhone);
        }
        dialpadEnabled = mInCallScreen.okToShowDialpad();
        dialpadVisible = mInCallScreen.isDialerOpened();
        if (phoneType == Phone.PHONE_TYPE_GSM) {
            supportsHold = true;
            onHold = hasHoldingCall && (fgCallState == Call.State.IDLE);
            boolean okToHold = hasActiveForegroundCall && !hasHoldingCall;
            boolean okToUnhold = onHold;
            canHold = okToHold || okToUnhold;
        } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
            supportsHold = false;
            onHold = false;
            canHold = false;
        }
        if (DBG) dumpState();
    }
    public void dumpState() {
        log("InCallControlState:");
        log("  manageConferenceVisible: " + manageConferenceVisible);
        log("  manageConferenceEnabled: " + manageConferenceEnabled);
        log("  canAddCall: " + canAddCall);
        log("  canSwap: " + canSwap);
        log("  canMerge: " + canMerge);
        log("  bluetoothEnabled: " + bluetoothEnabled);
        log("  bluetoothIndicatorOn: " + bluetoothIndicatorOn);
        log("  speakerEnabled: " + speakerEnabled);
        log("  speakerOn: " + speakerOn);
        log("  canMute: " + canMute);
        log("  muteIndicatorOn: " + muteIndicatorOn);
        log("  dialpadEnabled: " + dialpadEnabled);
        log("  dialpadVisible: " + dialpadVisible);
        log("  onHold: " + onHold);
        log("  canHold: " + canHold);
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
