class InCallMenu {
    private static final String LOG_TAG = "PHONE/InCallMenu";
    private static final boolean DBG = false;
    private InCallScreen mInCallScreen;
    private InCallMenuView mInCallMenuView;
    InCallMenuItemView mManageConference;
    InCallMenuItemView mShowDialpad;
    InCallMenuItemView mEndCall;
    InCallMenuItemView mAddCall;
    InCallMenuItemView mSwapCalls;
    InCallMenuItemView mMergeCalls;
    InCallMenuItemView mBluetooth;
    InCallMenuItemView mSpeaker;
    InCallMenuItemView mMute;
    InCallMenuItemView mHold;
    InCallMenuItemView mAnswerAndHold;
    InCallMenuItemView mAnswerAndEnd;
    InCallMenuItemView mAnswer;
    InCallMenuItemView mIgnore;
    InCallMenu(InCallScreen inCallScreen) {
        if (DBG) log("InCallMenu constructor...");
        mInCallScreen = inCallScreen;
    }
    void clearInCallScreenReference() {
        mInCallScreen = null;
        if (mInCallMenuView != null) mInCallMenuView.clearInCallScreenReference();
    }
     InCallMenuView getView() {
        return mInCallMenuView;
    }
     void initMenu() {
        if (DBG) log("initMenu()...");
        Context wrappedContext = new ContextThemeWrapper(
                mInCallScreen,
                com.android.internal.R.style.Theme_IconMenu);
        mInCallMenuView = new InCallMenuView(wrappedContext, mInCallScreen);
        mManageConference = new InCallMenuItemView(wrappedContext);
        mManageConference.setId(R.id.menuManageConference);
        mManageConference.setOnClickListener(mInCallScreen);
        mManageConference.setText(R.string.menu_manageConference);
        mManageConference.setIconResource(com.android.internal.R.drawable.ic_menu_allfriends);
        mShowDialpad = new InCallMenuItemView(wrappedContext);
        mShowDialpad.setId(R.id.menuShowDialpad);
        mShowDialpad.setOnClickListener(mInCallScreen);
        mShowDialpad.setText(R.string.menu_showDialpad); 
        mShowDialpad.setIconResource(R.drawable.ic_menu_dial_pad);
        mEndCall = new InCallMenuItemView(wrappedContext);
        mEndCall.setId(R.id.menuEndCall);
        mEndCall.setOnClickListener(mInCallScreen);
        mEndCall.setText(R.string.menu_endCall);
        mEndCall.setIconResource(R.drawable.ic_menu_end_call);
        mAddCall = new InCallMenuItemView(wrappedContext);
        mAddCall.setId(R.id.menuAddCall);
        mAddCall.setOnClickListener(mInCallScreen);
        mAddCall.setText(R.string.menu_addCall);
        mAddCall.setIconResource(android.R.drawable.ic_menu_add);
        mSwapCalls = new InCallMenuItemView(wrappedContext);
        mSwapCalls.setId(R.id.menuSwapCalls);
        mSwapCalls.setOnClickListener(mInCallScreen);
        mSwapCalls.setText(R.string.menu_swapCalls);
        mSwapCalls.setIconResource(R.drawable.ic_menu_swap_calls);
        mMergeCalls = new InCallMenuItemView(wrappedContext);
        mMergeCalls.setId(R.id.menuMergeCalls);
        mMergeCalls.setOnClickListener(mInCallScreen);
        mMergeCalls.setText(R.string.menu_mergeCalls);
        mMergeCalls.setIconResource(R.drawable.ic_menu_merge_calls);
        mBluetooth = new InCallMenuItemView(wrappedContext);
        mBluetooth.setId(R.id.menuBluetooth);
        mBluetooth.setOnClickListener(mInCallScreen);
        mBluetooth.setText(R.string.menu_bluetooth);
        mBluetooth.setIndicatorVisible(true);
        mSpeaker = new InCallMenuItemView(wrappedContext);
        mSpeaker.setId(R.id.menuSpeaker);
        mSpeaker.setOnClickListener(mInCallScreen);
        mSpeaker.setText(R.string.menu_speaker);
        mSpeaker.setIndicatorVisible(true);
        mMute = new InCallMenuItemView(wrappedContext);
        mMute.setId(R.id.menuMute);
        mMute.setOnClickListener(mInCallScreen);
        mMute.setText(R.string.menu_mute);
        mMute.setIndicatorVisible(true);
        mHold = new InCallMenuItemView(wrappedContext);
        mHold.setId(R.id.menuHold);
        mHold.setOnClickListener(mInCallScreen);
        mHold.setText(R.string.menu_hold);
        mHold.setIndicatorVisible(true);
        mAnswerAndHold = new InCallMenuItemView(wrappedContext);
        mAnswerAndHold.setId(R.id.menuAnswerAndHold);
        mAnswerAndHold.setOnClickListener(mInCallScreen);
        mAnswerAndHold.setText(R.string.menu_answerAndHold);
        mAnswerAndEnd = new InCallMenuItemView(wrappedContext);
        mAnswerAndEnd.setId(R.id.menuAnswerAndEnd);
        mAnswerAndEnd.setOnClickListener(mInCallScreen);
        mAnswerAndEnd.setText(R.string.menu_answerAndEnd);
        mAnswer = new InCallMenuItemView(wrappedContext);
        mAnswer.setId(R.id.menuAnswer);
        mAnswer.setOnClickListener(mInCallScreen);
        mAnswer.setText(R.string.menu_answer);
        mIgnore = new InCallMenuItemView(wrappedContext);
        mIgnore.setId(R.id.menuIgnore);
        mIgnore.setOnClickListener(mInCallScreen);
        mIgnore.setText(R.string.menu_ignore);
        PhoneApp app = PhoneApp.getInstance();
        int phoneType = app.phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_GSM) {
            mInCallMenuView.addItemView(mManageConference, 0);
        }
        mInCallMenuView.addItemView(mShowDialpad, 0);
        mInCallMenuView.addItemView(mSwapCalls, 1);
        mInCallMenuView.addItemView(mMergeCalls, 1);
        mInCallMenuView.addItemView(mAddCall, 1);
        mInCallMenuView.addItemView(mEndCall, 1);
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            mInCallMenuView.addItemView(mAnswer, 2);
            mInCallMenuView.addItemView(mIgnore, 2);
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            mInCallMenuView.addItemView(mHold, 2);
            mInCallMenuView.addItemView(mAnswerAndHold, 2);
            mInCallMenuView.addItemView(mAnswerAndEnd, 2);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        mInCallMenuView.addItemView(mMute, 2);
        mInCallMenuView.addItemView(mSpeaker, 2);
        mInCallMenuView.addItemView(mBluetooth, 2);
        mInCallMenuView.dumpState();
    }
     boolean updateItems(Phone phone) {
        if (DBG) log("updateItems()...");
        if (phone.getState() == Phone.State.IDLE) {
            if (DBG) log("- Phone is idle!  Don't show the menu...");
            return false;
        }
        final boolean hasRingingCall = !phone.getRingingCall().isIdle();
        final boolean hasActiveCall = !phone.getForegroundCall().isIdle();
        final Call.State fgCallState = phone.getForegroundCall().getState();
        final boolean hasHoldingCall = !phone.getBackgroundCall().isIdle();
        if (hasActiveCall && (PhoneApp.getInstance().isOtaCallInActiveState())) {
            mAnswerAndHold.setVisible(false);
            mAnswerAndHold.setEnabled(false);
            mAnswerAndEnd.setVisible(false);
            mAnswerAndEnd.setEnabled(false);
            mManageConference.setVisible(false);
            mAddCall.setEnabled(false);
            mSwapCalls.setEnabled(false);
            mMergeCalls.setEnabled(false);
            mHold.setEnabled(false);
            mBluetooth.setEnabled(false);
            mMute.setEnabled(false);
            mAnswer.setVisible(false);
            mIgnore.setVisible(false);
            boolean inConferenceCall =
                    PhoneUtils.isConferenceCall(phone.getForegroundCall());
            boolean showShowDialpad = !inConferenceCall;
            boolean enableShowDialpad = showShowDialpad && mInCallScreen.okToShowDialpad();
            mShowDialpad.setVisible(showShowDialpad);
            mShowDialpad.setEnabled(enableShowDialpad);
            boolean isDtmfDialerOpened = mInCallScreen.isDialerOpened();
            mShowDialpad.setText(isDtmfDialerOpened
                                 ? R.string.menu_hideDialpad
                                 : R.string.menu_showDialpad);
            mEndCall.setVisible(true);
            mEndCall.setEnabled(true);
            mSpeaker.setVisible(true);
            mSpeaker.setEnabled(true);
            boolean speakerOn = PhoneUtils.isSpeakerOn(mInCallScreen.getApplicationContext());
            mSpeaker.setIndicatorState(speakerOn);
            mInCallMenuView.updateVisibility();
            return true;
        }
        if (hasRingingCall) {
            if (hasActiveCall && !hasHoldingCall) {
                int phoneType = phone.getPhoneType();
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    mAnswer.setVisible(true);
                    mAnswer.setEnabled(true);
                    mIgnore.setVisible(true);
                    mIgnore.setEnabled(true);
                    mAnswerAndHold.setVisible(false);
                    mAnswerAndEnd.setVisible(false);
                } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                    mAnswerAndHold.setVisible(true);
                    mAnswerAndHold.setEnabled(true);
                    mAnswerAndEnd.setVisible(true);
                    mAnswerAndEnd.setEnabled(true);
                    mAnswer.setVisible(false);
                    mIgnore.setVisible(false);
                    mManageConference.setVisible(false);
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
                mShowDialpad.setVisible(false);
                mEndCall.setVisible(false);
                mAddCall.setVisible(false);
                mSwapCalls.setVisible(false);
                mMergeCalls.setVisible(false);
                mBluetooth.setVisible(false);
                mSpeaker.setVisible(false);
                mMute.setVisible(false);
                mHold.setVisible(false);
                mInCallMenuView.updateVisibility();
                return true;
            } else {
                return false;
            }
        }
        InCallControlState inCallControlState = mInCallScreen.getUpdatedInCallControlState();
        mManageConference.setVisible(inCallControlState.manageConferenceVisible);
        mManageConference.setEnabled(inCallControlState.manageConferenceEnabled);
        boolean showShowDialpad = !inCallControlState.manageConferenceVisible;
        boolean enableShowDialpad = showShowDialpad && mInCallScreen.okToShowDialpad();
        mShowDialpad.setVisible(showShowDialpad);
        mShowDialpad.setEnabled(enableShowDialpad);
        mShowDialpad.setText(inCallControlState.dialpadVisible
                             ? R.string.menu_hideDialpad
                             : R.string.menu_showDialpad);
        mEndCall.setVisible(true);
        mEndCall.setEnabled(true);
        mAddCall.setVisible(true);
        mAddCall.setEnabled(inCallControlState.canAddCall);
        mSwapCalls.setVisible(true);
        mSwapCalls.setEnabled(inCallControlState.canSwap);
        mMergeCalls.setVisible(true);
        mMergeCalls.setEnabled(inCallControlState.canMerge);
        mBluetooth.setVisible(true);
        mBluetooth.setEnabled(inCallControlState.bluetoothEnabled);
        mBluetooth.setIndicatorState(inCallControlState.bluetoothIndicatorOn);
        mSpeaker.setVisible(true);
        mSpeaker.setEnabled(inCallControlState.speakerEnabled);
        mSpeaker.setIndicatorState(inCallControlState.speakerOn);
        mMute.setVisible(true);
        mMute.setEnabled(inCallControlState.canMute);
        mMute.setIndicatorState(inCallControlState.muteIndicatorOn);
        mHold.setVisible(inCallControlState.supportsHold);
        mHold.setIndicatorState(inCallControlState.onHold);
        mHold.setEnabled(inCallControlState.canHold);
        mAnswer.setVisible(false);
        mAnswer.setEnabled(false);
        mIgnore.setVisible(false);
        mIgnore.setEnabled(false);
        mAnswerAndHold.setVisible(false);
        mAnswerAndHold.setEnabled(false);
        mAnswerAndEnd.setVisible(false);
        mAnswerAndEnd.setEnabled(false);
        mInCallMenuView.updateVisibility();
        return true;
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
