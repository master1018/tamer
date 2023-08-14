public class InCallTouchUi extends FrameLayout
        implements View.OnClickListener, SlidingTab.OnTriggerListener {
    private static final int IN_CALL_WIDGET_TRANSITION_TIME = 250; 
    private static final String LOG_TAG = "InCallTouchUi";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private InCallScreen mInCallScreen;
    private PhoneApp mApplication;
    private SlidingTab mIncomingCallWidget;  
    private View mInCallControls;  
    private Button mAddButton;
    private Button mMergeButton;
    private Button mEndButton;
    private Button mDialpadButton;
    private ToggleButton mBluetoothButton;
    private ToggleButton mMuteButton;
    private ToggleButton mSpeakerButton;
    private View mHoldButtonContainer;
    private ImageButton mHoldButton;
    private TextView mHoldButtonLabel;
    private View mSwapButtonContainer;
    private ImageButton mSwapButton;
    private TextView mSwapButtonLabel;
    private View mCdmaMergeButtonContainer;
    private ImageButton mCdmaMergeButton;
    private Drawable mHoldIcon;
    private Drawable mUnholdIcon;
    private Drawable mShowDialpadIcon;
    private Drawable mHideDialpadIcon;
    private long mLastIncomingCallActionTime;  
    private boolean mAllowIncomingCallTouchUi;
    private boolean mAllowInCallTouchUi;
    public InCallTouchUi(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DBG) log("InCallTouchUi constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(
                R.layout.incall_touch_ui,  
                this,                      
                true);
        mApplication = PhoneApp.getInstance();
        mAllowIncomingCallTouchUi = getResources().getBoolean(R.bool.allow_incoming_call_touch_ui);
        if (DBG) log("- incoming call touch UI: "
                     + (mAllowIncomingCallTouchUi ? "ENABLED" : "DISABLED"));
        mAllowInCallTouchUi = getResources().getBoolean(R.bool.allow_in_call_touch_ui);
        if (DBG) log("- regular in-call touch UI: "
                     + (mAllowInCallTouchUi ? "ENABLED" : "DISABLED"));
    }
    void setInCallScreenInstance(InCallScreen inCallScreen) {
        mInCallScreen = inCallScreen;
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DBG) log("InCallTouchUi onFinishInflate(this = " + this + ")...");
        mIncomingCallWidget = (SlidingTab) findViewById(R.id.incomingCallWidget);
        mIncomingCallWidget.setLeftTabResources(
                R.drawable.ic_jog_dial_answer,
                com.android.internal.R.drawable.jog_tab_target_green,
                com.android.internal.R.drawable.jog_tab_bar_left_answer,
                com.android.internal.R.drawable.jog_tab_left_answer
                );
        mIncomingCallWidget.setRightTabResources(
                R.drawable.ic_jog_dial_decline,
                com.android.internal.R.drawable.jog_tab_target_red,
                com.android.internal.R.drawable.jog_tab_bar_right_decline,
                com.android.internal.R.drawable.jog_tab_right_decline
                );
        mIncomingCallWidget.setLeftHintText(R.string.slide_to_answer_hint);
        mIncomingCallWidget.setRightHintText(R.string.slide_to_decline_hint);
        mIncomingCallWidget.setOnTriggerListener(this);
        mInCallControls = findViewById(R.id.inCallControls);
        mAddButton = (Button) mInCallControls.findViewById(R.id.addButton);
        mAddButton.setOnClickListener(this);
        mMergeButton = (Button) mInCallControls.findViewById(R.id.mergeButton);
        mMergeButton.setOnClickListener(this);
        mEndButton = (Button) mInCallControls.findViewById(R.id.endButton);
        mEndButton.setOnClickListener(this);
        mDialpadButton = (Button) mInCallControls.findViewById(R.id.dialpadButton);
        mDialpadButton.setOnClickListener(this);
        mBluetoothButton = (ToggleButton) mInCallControls.findViewById(R.id.bluetoothButton);
        mBluetoothButton.setOnClickListener(this);
        mMuteButton = (ToggleButton) mInCallControls.findViewById(R.id.muteButton);
        mMuteButton.setOnClickListener(this);
        mSpeakerButton = (ToggleButton) mInCallControls.findViewById(R.id.speakerButton);
        mSpeakerButton.setOnClickListener(this);
        mHoldButtonContainer = mInCallControls.findViewById(R.id.holdButtonContainer);
        mHoldButton = (ImageButton) mInCallControls.findViewById(R.id.holdButton);
        mHoldButton.setOnClickListener(this);
        mHoldButtonLabel = (TextView) mInCallControls.findViewById(R.id.holdButtonLabel);
        mSwapButtonContainer = mInCallControls.findViewById(R.id.swapButtonContainer);
        mSwapButton = (ImageButton) mInCallControls.findViewById(R.id.swapButton);
        mSwapButton.setOnClickListener(this);
        mSwapButtonLabel = (TextView) mInCallControls.findViewById(R.id.swapButtonLabel);
        if (PhoneApp.getInstance().phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            mSwapButtonLabel.setText(R.string.onscreenManageCallsText);
        } else {
            mSwapButtonLabel.setText(R.string.onscreenSwapCallsText);
        }
        mCdmaMergeButtonContainer = mInCallControls.findViewById(R.id.cdmaMergeButtonContainer);
        mCdmaMergeButton = (ImageButton) mInCallControls.findViewById(R.id.cdmaMergeButton);
        mCdmaMergeButton.setOnClickListener(this);
        View.OnTouchListener smallerHitTargetTouchListener = new SmallerHitTargetTouchListener();
        mAddButton.setOnTouchListener(smallerHitTargetTouchListener);
        mMergeButton.setOnTouchListener(smallerHitTargetTouchListener);
        mDialpadButton.setOnTouchListener(smallerHitTargetTouchListener);
        mBluetoothButton.setOnTouchListener(smallerHitTargetTouchListener);
        mSpeakerButton.setOnTouchListener(smallerHitTargetTouchListener);
        mHoldButton.setOnTouchListener(smallerHitTargetTouchListener);
        mSwapButton.setOnTouchListener(smallerHitTargetTouchListener);
        mCdmaMergeButton.setOnTouchListener(smallerHitTargetTouchListener);
        mSpeakerButton.setOnTouchListener(smallerHitTargetTouchListener);
        mHoldIcon = getResources().getDrawable(R.drawable.ic_in_call_touch_round_hold);
        mUnholdIcon = getResources().getDrawable(R.drawable.ic_in_call_touch_round_unhold);
        mShowDialpadIcon = getResources().getDrawable(R.drawable.ic_in_call_touch_dialpad);
        mHideDialpadIcon = getResources().getDrawable(R.drawable.ic_in_call_touch_dialpad_close);
    }
    void updateState(Phone phone) {
        if (DBG) log("updateState(" + phone + ")...");
        if (mInCallScreen == null) {
            log("- updateState: mInCallScreen has been destroyed; bailing out...");
            return;
        }
        Phone.State state = phone.getState();  
        if (DBG) log("- updateState: phone state is " + state);
        boolean showIncomingCallControls = false;
        boolean showInCallControls = false;
        if (state == Phone.State.RINGING) {
            if (mAllowIncomingCallTouchUi) {
                final Call ringingCall = phone.getRingingCall();
                if (ringingCall.getState().isAlive()) {
                    if (DBG) log("- updateState: RINGING!  Showing incoming call controls...");
                    showIncomingCallControls = true;
                }
                long now = SystemClock.uptimeMillis();
                if (now < mLastIncomingCallActionTime + 500) {
                    log("updateState: Too soon after last action; not drawing!");
                    showIncomingCallControls = false;
                }
            }
        } else {
            if (mAllowInCallTouchUi) {
                if (mInCallScreen.okToShowInCallTouchUi()) {
                    showInCallControls = true;
                } else {
                    if (DBG) log("- updateState: NOT OK to show touch UI; disabling...");
                }
            }
        }
        if (showInCallControls) {
            updateInCallControls(phone);
        }
        if (showIncomingCallControls && showInCallControls) {
            throw new IllegalStateException(
                "'Incoming' and 'in-call' touch controls visible at the same time!");
        }
        if (showIncomingCallControls) {
            showIncomingCallWidget();
        } else {
            hideIncomingCallWidget();
        }
        mInCallControls.setVisibility(showInCallControls ? View.VISIBLE : View.GONE);
    }
    public void onClick(View view) {
        int id = view.getId();
        if (DBG) log("onClick(View " + view + ", id " + id + ")...");
        switch (id) {
            case R.id.addButton:
            case R.id.mergeButton:
            case R.id.endButton:
            case R.id.dialpadButton:
            case R.id.bluetoothButton:
            case R.id.muteButton:
            case R.id.speakerButton:
            case R.id.holdButton:
            case R.id.swapButton:
            case R.id.cdmaMergeButton:
                mInCallScreen.handleOnscreenButtonClick(id);
                break;
            default:
                Log.w(LOG_TAG, "onClick: unexpected click: View " + view + ", id " + id);
                break;
        }
    }
    void updateInCallControls(Phone phone) {
        int phoneType = phone.getPhoneType();
        InCallControlState inCallControlState = mInCallScreen.getUpdatedInCallControlState();
        if (inCallControlState.canAddCall) {
            mAddButton.setVisibility(View.VISIBLE);
            mAddButton.setEnabled(true);
            mMergeButton.setVisibility(View.GONE);
        } else if (inCallControlState.canMerge) {
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                mMergeButton.setVisibility(View.GONE);
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                mMergeButton.setVisibility(View.VISIBLE);
                mMergeButton.setEnabled(true);
                mAddButton.setVisibility(View.GONE);
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        } else {
            mAddButton.setVisibility(View.VISIBLE);
            mAddButton.setEnabled(false);
            mMergeButton.setVisibility(View.GONE);
        }
        if (inCallControlState.canAddCall && inCallControlState.canMerge) {
            if (phoneType == Phone.PHONE_TYPE_GSM) {
                Log.w(LOG_TAG, "updateInCallControls: Add *and* Merge enabled," +
                        " but can't show both!");
            } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                if (DBG) log("updateInCallControls: CDMA: Add and Merge both enabled");
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }
        mEndButton.setEnabled(true);
        mDialpadButton.setEnabled(inCallControlState.dialpadEnabled);
        if (inCallControlState.dialpadVisible) {
            mDialpadButton.setText(R.string.onscreenHideDialpadText);
            mDialpadButton.setCompoundDrawablesWithIntrinsicBounds(
                null, mHideDialpadIcon, null, null);
        } else {
            mDialpadButton.setText(R.string.onscreenShowDialpadText);
            mDialpadButton.setCompoundDrawablesWithIntrinsicBounds(
                    null, mShowDialpadIcon, null, null);
        }
        mBluetoothButton.setEnabled(inCallControlState.bluetoothEnabled);
        mBluetoothButton.setChecked(inCallControlState.bluetoothIndicatorOn);
        mMuteButton.setEnabled(inCallControlState.canMute);
        mMuteButton.setChecked(inCallControlState.muteIndicatorOn);
        mSpeakerButton.setEnabled(inCallControlState.speakerEnabled);
        mSpeakerButton.setChecked(inCallControlState.speakerOn);
        mHoldButtonContainer.setVisibility(
                inCallControlState.canHold ? View.VISIBLE : View.GONE);
        if (inCallControlState.canHold) {
            if (inCallControlState.onHold) {
                mHoldButton.setImageDrawable(mUnholdIcon);
                mHoldButtonLabel.setText(R.string.onscreenUnholdText);
            } else {
                mHoldButton.setImageDrawable(mHoldIcon);
                mHoldButtonLabel.setText(R.string.onscreenHoldText);
            }
        }
        mSwapButtonContainer.setVisibility(
                inCallControlState.canSwap ? View.VISIBLE : View.GONE);
        if (phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            mCdmaMergeButtonContainer.setVisibility(
                    inCallControlState.canMerge ? View.VISIBLE : View.GONE);
        }
        if (inCallControlState.canSwap && inCallControlState.canHold) {
            Log.w(LOG_TAG, "updateInCallControls: Hold *and* Swap enabled, but can't show both!");
        }
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if (inCallControlState.canSwap && inCallControlState.canMerge) {
                Log.w(LOG_TAG, "updateInCallControls: Merge *and* Swap" +
                        "enabled, but can't show both!");
            }
        }
        if (inCallControlState.dialpadVisible) {
            mHoldButtonContainer.setVisibility(View.GONE);
            mSwapButtonContainer.setVisibility(View.GONE);
            mCdmaMergeButtonContainer.setVisibility(View.GONE);
        }
    }
     boolean isTouchUiEnabled() {
        return mAllowInCallTouchUi;
    }
     boolean isIncomingCallTouchUiEnabled() {
        return mAllowIncomingCallTouchUi;
    }
    public void onTrigger(View v, int whichHandle) {
        log("onDialTrigger(whichHandle = " + whichHandle + ")...");
        switch (whichHandle) {
            case SlidingTab.OnTriggerListener.LEFT_HANDLE:
                if (DBG) log("LEFT_HANDLE: answer!");
                hideIncomingCallWidget();
                mLastIncomingCallActionTime = SystemClock.uptimeMillis();
                if (mInCallScreen != null) {
                    mInCallScreen.handleOnscreenButtonClick(R.id.answerButton);
                } else {
                    Log.e(LOG_TAG, "answer trigger: mInCallScreen is null");
                }
                break;
            case SlidingTab.OnTriggerListener.RIGHT_HANDLE:
                if (DBG) log("RIGHT_HANDLE: reject!");
                hideIncomingCallWidget();
                mLastIncomingCallActionTime = SystemClock.uptimeMillis();
                if (mInCallScreen != null) {
                    mInCallScreen.handleOnscreenButtonClick(R.id.rejectButton);
                } else {
                    Log.e(LOG_TAG, "reject trigger: mInCallScreen is null");
                }
                break;
            default:
                Log.e(LOG_TAG, "onDialTrigger: unexpected whichHandle value: " + whichHandle);
                break;
        }
        mInCallScreen.updateSlidingTabHint(0, 0);
    }
    private void hideIncomingCallWidget() {
        if (mIncomingCallWidget.getVisibility() != View.VISIBLE
                || mIncomingCallWidget.getAnimation() != null) {
            return;
        }
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(IN_CALL_WIDGET_TRANSITION_TIME);
        anim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }
            public void onAnimationRepeat(Animation animation) {
            }
            public void onAnimationEnd(Animation animation) {
                mIncomingCallWidget.clearAnimation();
                mIncomingCallWidget.setVisibility(View.GONE);
            }
        });
        mIncomingCallWidget.startAnimation(anim);
    }
    private void showIncomingCallWidget() {
        Animation anim = mIncomingCallWidget.getAnimation();
        if (anim != null) {
            anim.reset();
            mIncomingCallWidget.clearAnimation();
        }
        mIncomingCallWidget.reset(false);
        mIncomingCallWidget.setVisibility(View.VISIBLE);
    }
    public void onGrabbedStateChange(View v, int grabbedState) {
        if (mInCallScreen != null) {
            int hintTextResId, hintColorResId;
            switch (grabbedState) {
                case SlidingTab.OnTriggerListener.NO_HANDLE:
                    hintTextResId = 0;
                    hintColorResId = 0;
                    break;
                case SlidingTab.OnTriggerListener.LEFT_HANDLE:
                    hintTextResId = R.string.slide_to_answer;
                    hintColorResId = R.color.incall_textConnected;  
                    break;
                case SlidingTab.OnTriggerListener.RIGHT_HANDLE:
                    hintTextResId = R.string.slide_to_decline;
                    hintColorResId = R.color.incall_textEnded;  
                    break;
                default:
                    Log.e(LOG_TAG, "onGrabbedStateChange: unexpected grabbedState: "
                          + grabbedState);
                    hintTextResId = 0;
                    hintColorResId = 0;
                    break;
            }
            mInCallScreen.updateSlidingTabHint(hintTextResId, hintColorResId);
        }
    }
    class SmallerHitTargetTouchListener implements View.OnTouchListener {
        private static final int HIT_TARGET_PERCENT_X = 50;
        private static final int HIT_TARGET_PERCENT_Y = 80;
        private static final int X_EDGE = (100 - HIT_TARGET_PERCENT_X) / 2;
        private static final int Y_EDGE = (100 - HIT_TARGET_PERCENT_Y) / 2;
        private static final int X_HIT_MIN = X_EDGE;
        private static final int X_HIT_MAX = 100 - X_EDGE;
        private static final int Y_HIT_MIN = Y_EDGE;
        private static final int Y_HIT_MAX = 100 - Y_EDGE;
        boolean mDownEventHit;
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int touchX = (int) event.getX();
                int touchY = (int) event.getY();
                int viewWidth = v.getWidth();
                int viewHeight = v.getHeight();
                int touchXPercent = (int) ((float) (touchX * 100) / (float) viewWidth);
                int touchYPercent = (int) ((float) (touchY * 100) / (float) viewHeight);
                if (touchXPercent < X_HIT_MIN || touchXPercent > X_HIT_MAX
                        || touchYPercent < Y_HIT_MIN || touchYPercent > Y_HIT_MAX) {
                    mDownEventHit = false;
                    return true;  
                } else {
                    mDownEventHit = true;
                    return false;  
                }
            } else {
                return !mDownEventHit;
            }
        }
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
