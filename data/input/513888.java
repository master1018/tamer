public class CallCard extends FrameLayout
        implements CallTime.OnTickListener, CallerInfoAsyncQuery.OnQueryCompleteListener,
                   ContactsAsyncHelper.OnImageLoadCompleteListener, View.OnClickListener {
    private static final String LOG_TAG = "CallCard";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private InCallScreen mInCallScreen;
    private PhoneApp mApplication;
    private ViewGroup mPrimaryCallInfo;
    private ViewGroup mSecondaryCallInfo;
    private TextView mUpperTitle;
    private TextView mElapsedTime;
    private int mTextColorDefaultPrimary;
    private int mTextColorDefaultSecondary;
    private int mTextColorConnected;
    private int mTextColorConnectedBluetooth;
    private int mTextColorEnded;
    private int mTextColorOnHold;
    private ImageView mPhoto;
    private Button mManageConferencePhotoButton;  
    private TextView mName;
    private TextView mPhoneNumber;
    private TextView mLabel;
    private TextView mSocialStatus;
    private TextView mSecondaryCallName;
    private TextView mSecondaryCallStatus;
    private ImageView mSecondaryCallPhoto;
    private TextView mMenuButtonHint;
    private int mRotarySelectorHintTextResId;
    private int mRotarySelectorHintColorResId;
    private CallTime mCallTime;
    private ContactsAsyncHelper.ImageTracker mPhotoTracker;
    private float mDensity;
    public CallCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DBG) log("CallCard constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(
                R.layout.call_card,  
                this,                
                true);
        mApplication = PhoneApp.getInstance();
        mCallTime = new CallTime(this);
        mPhotoTracker = new ContactsAsyncHelper.ImageTracker();
        mDensity = getResources().getDisplayMetrics().density;
        if (DBG) log("- Density: " + mDensity);
    }
    void setInCallScreenInstance(InCallScreen inCallScreen) {
        mInCallScreen = inCallScreen;
    }
    public void onTickForCallTimeElapsed(long timeElapsed) {
        updateElapsedTimeWidget(timeElapsed);
    }
    void stopTimer() {
        mCallTime.cancelTimer();
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DBG) log("CallCard onFinishInflate(this = " + this + ")...");
        mPrimaryCallInfo = (ViewGroup) findViewById(R.id.primaryCallInfo);
        mSecondaryCallInfo = (ViewGroup) findViewById(R.id.secondaryCallInfo);
        mUpperTitle = (TextView) findViewById(R.id.upperTitle);
        mElapsedTime = (TextView) findViewById(R.id.elapsedTime);
        mTextColorDefaultPrimary =  
                getResources().getColor(android.R.color.primary_text_dark);
        mTextColorDefaultSecondary =  
                getResources().getColor(android.R.color.secondary_text_dark);
        mTextColorConnected = getResources().getColor(R.color.incall_textConnected);
        mTextColorConnectedBluetooth =
                getResources().getColor(R.color.incall_textConnectedBluetooth);
        mTextColorEnded = getResources().getColor(R.color.incall_textEnded);
        mTextColorOnHold = getResources().getColor(R.color.incall_textOnHold);
        mPhoto = (ImageView) findViewById(R.id.photo);
        mManageConferencePhotoButton = (Button) findViewById(R.id.manageConferencePhotoButton);
        mManageConferencePhotoButton.setOnClickListener(this);
        mName = (TextView) findViewById(R.id.name);
        mPhoneNumber = (TextView) findViewById(R.id.phoneNumber);
        mLabel = (TextView) findViewById(R.id.label);
        mSocialStatus = (TextView) findViewById(R.id.socialStatus);
        mSecondaryCallName = (TextView) findViewById(R.id.secondaryCallName);
        mSecondaryCallStatus = (TextView) findViewById(R.id.secondaryCallStatus);
        mSecondaryCallPhoto = (ImageView) findViewById(R.id.secondaryCallPhoto);
        mMenuButtonHint = (TextView) findViewById(R.id.menuButtonHint);
    }
    void updateState(Phone phone) {
        if (DBG) log("updateState(" + phone + ")...");
        Phone.State state = phone.getState();  
        if (state == Phone.State.RINGING) {
            updateRingingCall(phone);
        } else if (state == Phone.State.OFFHOOK) {
            updateForegroundCall(phone);
        } else {
            Call fgCall = phone.getForegroundCall();
            Call bgCall = phone.getBackgroundCall();
            if ((fgCall.getState() == Call.State.DISCONNECTED)
                || (bgCall.getState() == Call.State.DISCONNECTED)) {
                updateForegroundCall(phone);
            } else {
                updateNoCall(phone);
            }
        }
    }
    private void updateForegroundCall(Phone phone) {
        if (DBG) log("updateForegroundCall()...");
        Call fgCall = phone.getForegroundCall();
        Call bgCall = phone.getBackgroundCall();
        if (fgCall.isIdle() && !fgCall.hasConnections()) {
            if (DBG) log("updateForegroundCall: no active call, show holding call");
            fgCall = bgCall;
            bgCall = null;
        }
        displayMainCallStatus(phone, fgCall);
        int phoneType = phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if ((mApplication.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                displayOnHoldCallStatus(phone, fgCall);
            } else {
                displayOnHoldCallStatus(phone, bgCall);
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            displayOnHoldCallStatus(phone, bgCall);
        }
    }
    private void updateRingingCall(Phone phone) {
        if (DBG) log("updateRingingCall()...");
        Call ringingCall = phone.getRingingCall();
        Call fgCall = phone.getForegroundCall();
        Call bgCall = phone.getBackgroundCall();
        displayMainCallStatus(phone, ringingCall);
        displayOnHoldCallStatus(phone, null);
    }
    private void updateNoCall(Phone phone) {
        if (DBG) log("updateNoCall()...");
        displayMainCallStatus(phone, null);
        displayOnHoldCallStatus(phone, null);
    }
    private void displayMainCallStatus(Phone phone, Call call) {
        if (DBG) log("displayMainCallStatus(phone " + phone
                     + ", call " + call + ")...");
        if (call == null) {
            mPrimaryCallInfo.setVisibility(View.GONE);
            return;
        }
        mPrimaryCallInfo.setVisibility(View.VISIBLE);
        Call.State state = call.getState();
        if (DBG) log("  - call.state: " + call.getState());
        switch (state) {
            case ACTIVE:
            case DISCONNECTING:
                if (DBG) log("displayMainCallStatus: start periodicUpdateTimer");
                mCallTime.setActiveCallMode(call);
                mCallTime.reset();
                mCallTime.periodicUpdateTimer();
                break;
            case HOLDING:
                mCallTime.cancelTimer();
                break;
            case DISCONNECTED:
                mCallTime.cancelTimer();
                break;
            case DIALING:
            case ALERTING:
                mCallTime.cancelTimer();
                break;
            case INCOMING:
            case WAITING:
                mCallTime.cancelTimer();
                break;
            case IDLE:
                Log.w(LOG_TAG, "displayMainCallStatus: IDLE call in the main call card!");
                break;
            default:
                Log.w(LOG_TAG, "displayMainCallStatus: unexpected call state: " + state);
                break;
        }
        updateCardTitleWidgets(phone, call);
        if (PhoneUtils.isConferenceCall(call)) {
            updateDisplayForConference();
        } else {
            Connection conn = null;
            int phoneType = phone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                conn = call.getLatestConnection();
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                conn = call.getEarliestConnection();
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
            if (conn == null) {
                if (DBG) log("displayMainCallStatus: connection is null, using default values.");
                CallerInfo info = PhoneUtils.getCallerInfo(getContext(), null );
                updateDisplayForPerson(info, Connection.PRESENTATION_ALLOWED, false, call);
            } else {
                if (DBG) log("  - CONN: " + conn + ", state = " + conn.getState());
                int presentation = conn.getNumberPresentation();
                boolean runQuery = true;
                Object o = conn.getUserData();
                if (o instanceof PhoneUtils.CallerInfoToken) {
                    runQuery = mPhotoTracker.isDifferentImageRequest(
                            ((PhoneUtils.CallerInfoToken) o).currentInfo);
                } else {
                    runQuery = mPhotoTracker.isDifferentImageRequest(conn);
                }
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    Object obj = conn.getUserData();
                    String updatedNumber = conn.getAddress();
                    String updatedCnapName = conn.getCnapName();
                    CallerInfo info = null;
                    if (obj instanceof PhoneUtils.CallerInfoToken) {
                        info = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                    } else if (o instanceof CallerInfo) {
                        info = (CallerInfo) o;
                    }
                    if (info != null) {
                        if (updatedNumber != null && !updatedNumber.equals(info.phoneNumber)) {
                            if (DBG) log("- displayMainCallStatus: updatedNumber = "
                                    + updatedNumber);
                            runQuery = true;
                        }
                        if (updatedCnapName != null && !updatedCnapName.equals(info.cnapName)) {
                            if (DBG) log("- displayMainCallStatus: updatedCnapName = "
                                    + updatedCnapName);
                            runQuery = true;
                        }
                    }
                }
                if (runQuery) {
                    if (DBG) log("- displayMainCallStatus: starting CallerInfo query...");
                    PhoneUtils.CallerInfoToken info =
                            PhoneUtils.startGetCallerInfo(getContext(), conn, this, call);
                    updateDisplayForPerson(info.currentInfo, presentation, !info.isFinal, call);
                } else {
                    if (DBG) log("- displayMainCallStatus: using data we already have...");
                    if (o instanceof CallerInfo) {
                        CallerInfo ci = (CallerInfo) o;
                        ci.cnapName = conn.getCnapName();
                        ci.numberPresentation = conn.getNumberPresentation();
                        ci.namePresentation = conn.getCnapNamePresentation();
                        if (DBG) log("- displayMainCallStatus: CNAP data from Connection: "
                                + "CNAP name=" + ci.cnapName
                                + ", Number/Name Presentation=" + ci.numberPresentation);
                        if (DBG) log("   ==> Got CallerInfo; updating display: ci = " + ci);
                        updateDisplayForPerson(ci, presentation, false, call);
                    } else if (o instanceof PhoneUtils.CallerInfoToken){
                        CallerInfo ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                        if (DBG) log("- displayMainCallStatus: CNAP data from Connection: "
                                + "CNAP name=" + ci.cnapName
                                + ", Number/Name Presentation=" + ci.numberPresentation);
                        if (DBG) log("   ==> Got CallerInfoToken; updating display: ci = " + ci);
                        updateDisplayForPerson(ci, presentation, true, call);
                    } else {
                        Log.w(LOG_TAG, "displayMainCallStatus: runQuery was false, "
                              + "but we didn't have a cached CallerInfo object!  o = " + o);
                    }
                }
            }
        }
        updatePhotoForCallState(call);
        if (mRotarySelectorHintTextResId != 0) {
            mPhoneNumber.setText(mRotarySelectorHintTextResId);
            mPhoneNumber.setTextColor(getResources().getColor(mRotarySelectorHintColorResId));
            mPhoneNumber.setVisibility(View.VISIBLE);
            mLabel.setVisibility(View.GONE);
        }
    }
    public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
        if (DBG) log("onQueryComplete: token " + token + ", cookie " + cookie + ", ci " + ci);
        if (cookie instanceof Call) {
            if (DBG) log("callerinfo query complete, updating ui from displayMainCallStatus()");
            Call call = (Call) cookie;
            Connection conn = null;
            int phoneType = mApplication.phone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                conn = call.getLatestConnection();
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                conn = call.getEarliestConnection();
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
            PhoneUtils.CallerInfoToken cit =
                   PhoneUtils.startGetCallerInfo(getContext(), conn, this, null);
            int presentation = Connection.PRESENTATION_ALLOWED;
            if (conn != null) presentation = conn.getNumberPresentation();
            if (DBG) log("- onQueryComplete: presentation=" + presentation
                    + ", contactExists=" + ci.contactExists);
            if (ci.contactExists) {
                updateDisplayForPerson(ci, Connection.PRESENTATION_ALLOWED, false, call);
            } else {
                updateDisplayForPerson(cit.currentInfo, presentation, false, call);
            }
            updatePhotoForCallState(call);
        } else if (cookie instanceof TextView){
            if (DBG) log("callerinfo query complete, updating ui from ongoing or onhold");
            ((TextView) cookie).setText(PhoneUtils.getCompactNameFromCallerInfo(ci, mContext));
        }
    }
    public void onImageLoadComplete(int token, Object cookie, ImageView iView,
            boolean imagePresent){
        if (cookie != null) {
            updatePhotoForCallState((Call) cookie);
        }
    }
    private void updateCardTitleWidgets(Phone phone, Call call) {
        if (DBG) log("updateCardTitleWidgets(call " + call + ")...");
        Call.State state = call.getState();
        String cardTitle;
        int phoneType = mApplication.phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if (!PhoneApp.getInstance().notifier.getIsCdmaRedialCall()) {
                cardTitle = getTitleForCallCard(call);  
            } else {
                cardTitle = getContext().getString(R.string.card_title_redialing);
            }
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            cardTitle = getTitleForCallCard(call);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        if (DBG) log("updateCardTitleWidgets: " + cardTitle);
        switch (state) {
            case ACTIVE:
            case DISCONNECTING:
                final boolean bluetoothActive = mApplication.showBluetoothIndication();
                int ongoingCallIcon = bluetoothActive ? R.drawable.ic_incall_ongoing_bluetooth
                        : R.drawable.ic_incall_ongoing;
                int connectedTextColor = bluetoothActive
                        ? mTextColorConnectedBluetooth : mTextColorConnected;
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    if (mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                        setUpperTitle(cardTitle, mTextColorDefaultPrimary, state);
                    } else {
                        clearUpperTitle();
                    }
                } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                    if (state == Call.State.DISCONNECTING) {
                        setUpperTitle(cardTitle, mTextColorDefaultPrimary, state);
                    } else {  
                        clearUpperTitle();
                    }
                }
                mElapsedTime.setVisibility(View.VISIBLE);
                mElapsedTime.setTextColor(connectedTextColor);
                long duration = CallTime.getCallDuration(call);  
                updateElapsedTimeWidget(duration / 1000);
                break;
            case DISCONNECTED:
                setUpperTitle(cardTitle, mTextColorEnded, state);
                mElapsedTime.setVisibility(View.VISIBLE);
                mElapsedTime.setTextColor(mTextColorEnded);
                break;
            case HOLDING:
                clearUpperTitle();
                mElapsedTime.setText(cardTitle);
                mElapsedTime.setVisibility(View.VISIBLE);
                mElapsedTime.setTextColor(mTextColorOnHold);
                break;
            default:
                setUpperTitle(cardTitle, mTextColorDefaultPrimary, state);
                mElapsedTime.setVisibility(View.INVISIBLE);
                break;
        }
    }
    private void updateElapsedTimeWidget(long timeElapsed) {
        if (timeElapsed == 0) {
            mElapsedTime.setText("");
        } else {
            mElapsedTime.setText(DateUtils.formatElapsedTime(timeElapsed));
        }
    }
    private String getTitleForCallCard(Call call) {
        String retVal = null;
        Call.State state = call.getState();
        Context context = getContext();
        int resId;
        if (DBG) log("- getTitleForCallCard(Call " + call + ")...");
        switch (state) {
            case IDLE:
                break;
            case ACTIVE:
                int phoneType = mApplication.phone.getPhoneType();
                if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    if (mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                        retVal = context.getString(R.string.card_title_dialing);
                    } else {
                        retVal = context.getString(R.string.card_title_in_progress);
                    }
                } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                    retVal = context.getString(R.string.card_title_in_progress);
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
                break;
            case HOLDING:
                retVal = context.getString(R.string.card_title_on_hold);
                break;
            case DIALING:
            case ALERTING:
                retVal = context.getString(R.string.card_title_dialing);
                break;
            case INCOMING:
            case WAITING:
                retVal = context.getString(R.string.card_title_incoming_call);
                break;
            case DISCONNECTING:
                retVal = context.getString(R.string.card_title_hanging_up);
                break;
            case DISCONNECTED:
                retVal = getCallFailedString(call);
                break;
        }
        if (DBG) log("  ==> result: " + retVal);
        return retVal;
    }
    private void displayOnHoldCallStatus(Phone phone, Call call) {
        if (DBG) log("displayOnHoldCallStatus(call =" + call + ")...");
        if ((call == null) || (PhoneApp.getInstance().isOtaCallInActiveState())) {
            mSecondaryCallInfo.setVisibility(View.GONE);
            return;
        }
        boolean showSecondaryCallInfo = false;
        Call.State state = call.getState();
        switch (state) {
            case HOLDING:
                if (PhoneUtils.isConferenceCall(call)) {
                    if (DBG) log("==> conference call.");
                    mSecondaryCallName.setText(getContext().getString(R.string.confCall));
                    showImage(mSecondaryCallPhoto, R.drawable.picture_conference);
                } else {
                    if (DBG) log("==> NOT a conf call; call startGetCallerInfo...");
                    PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                            getContext(), call, this, mSecondaryCallName);
                    mSecondaryCallName.setText(
                            PhoneUtils.getCompactNameFromCallerInfo(infoToken.currentInfo,
                                                                    getContext()));
                    if (infoToken.isFinal) {
                        showCachedImage(mSecondaryCallPhoto, infoToken.currentInfo);
                    } else {
                        showImage(mSecondaryCallPhoto, R.drawable.picture_unknown);
                    }
                }
                showSecondaryCallInfo = true;
                break;
            case ACTIVE:
                if (mApplication.phone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
                    List<Connection> connections = call.getConnections();
                    if (connections.size() > 2) {
                        mSecondaryCallName.setText(
                                getContext().getString(R.string.card_title_in_call));
                        showImage(mSecondaryCallPhoto, R.drawable.picture_unknown);
                    } else {
                        Connection conn = call.getEarliestConnection();
                        PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                                getContext(), conn, this, mSecondaryCallName);
                        CallerInfo info = infoToken.currentInfo;
                        String name = PhoneUtils.getCompactNameFromCallerInfo(info, getContext());
                        boolean forceGenericPhoto = false;
                        if (info != null && info.numberPresentation !=
                                Connection.PRESENTATION_ALLOWED) {
                            name = getPresentationString(info.numberPresentation);
                            forceGenericPhoto = true;
                        }
                        mSecondaryCallName.setText(name);
                        if (!forceGenericPhoto && infoToken.isFinal) {
                            showCachedImage(mSecondaryCallPhoto, info);
                        } else {
                            showImage(mSecondaryCallPhoto, R.drawable.picture_unknown);
                        }
                    }
                    showSecondaryCallInfo = true;
                } else {
                    Log.w(LOG_TAG, "displayOnHoldCallStatus: ACTIVE state on non-CDMA device");
                    showSecondaryCallInfo = false;
                }
                break;
            default:
                showSecondaryCallInfo = false;
                break;
        }
        if (showSecondaryCallInfo) {
            mSecondaryCallInfo.setVisibility(View.VISIBLE);
            boolean okToShowLabels = TextUtils.isEmpty(mUpperTitle.getText());
            mSecondaryCallName.setVisibility(okToShowLabels ? View.VISIBLE : View.INVISIBLE);
            mSecondaryCallStatus.setVisibility(okToShowLabels ? View.VISIBLE : View.INVISIBLE);
        } else {
            mSecondaryCallInfo.setVisibility(View.GONE);
        }
    }
    private String getCallFailedString(Call call) {
        Connection c = call.getEarliestConnection();
        int resID;
        if (c == null) {
            if (DBG) log("getCallFailedString: connection is null, using default values.");
            resID = R.string.card_title_call_ended;
        } else {
            Connection.DisconnectCause cause = c.getDisconnectCause();
            switch (cause) {
                case BUSY:
                    resID = R.string.callFailed_userBusy;
                    break;
                case CONGESTION:
                    resID = R.string.callFailed_congestion;
                    break;
                case LOST_SIGNAL:
                case CDMA_DROP:
                    resID = R.string.callFailed_noSignal;
                    break;
                case LIMIT_EXCEEDED:
                    resID = R.string.callFailed_limitExceeded;
                    break;
                case POWER_OFF:
                    resID = R.string.callFailed_powerOff;
                    break;
                case ICC_ERROR:
                    resID = R.string.callFailed_simError;
                    break;
                case OUT_OF_SERVICE:
                    resID = R.string.callFailed_outOfService;
                    break;
                default:
                    resID = R.string.card_title_call_ended;
                    break;
            }
        }
        return getContext().getString(resID);
    }
    private void updateDisplayForPerson(CallerInfo info,
                                        int presentation,
                                        boolean isTemporary,
                                        Call call) {
        if (DBG) log("updateDisplayForPerson(" + info + ")\npresentation:" +
                     presentation + " isTemporary:" + isTemporary);
        mPhotoTracker.setPhotoRequest(info);
        mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);
        String name;
        String displayNumber = null;
        String label = null;
        Uri personUri = null;
        String socialStatusText = null;
        Drawable socialStatusBadge = null;
        if (info != null) {
            if (TextUtils.isEmpty(info.name)) {
                if (TextUtils.isEmpty(info.phoneNumber)) {
                    name =  getPresentationString(presentation);
                } else if (presentation != Connection.PRESENTATION_ALLOWED) {
                    name = getPresentationString(presentation);
                } else if (!TextUtils.isEmpty(info.cnapName)) {
                    name = info.cnapName;
                    info.name = info.cnapName;
                    displayNumber = info.phoneNumber;
                } else {
                    name = info.phoneNumber;
                }
            } else {
                if (presentation != Connection.PRESENTATION_ALLOWED) {
                    name = getPresentationString(presentation);
                } else {
                    name = info.name;
                    displayNumber = info.phoneNumber;
                    label = info.phoneLabel;
                }
            }
            personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, info.person_id);
        } else {
            name =  getPresentationString(presentation);
        }
        if (call.isGeneric()) {
            mName.setText(R.string.card_title_in_call);
        } else {
            mName.setText(name);
        }
        mName.setVisibility(View.VISIBLE);
        if (isTemporary && (info == null || !info.isCachedPhotoCurrent)) {
            mPhoto.setVisibility(View.INVISIBLE);
        } else if (info != null && info.photoResource != 0){
            showImage(mPhoto, info.photoResource);
        } else if (!showCachedImage(mPhoto, info)) {
            ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(
                info, 0, this, call, getContext(), mPhoto, personUri, R.drawable.picture_unknown);
        }
        mManageConferencePhotoButton.setVisibility(View.INVISIBLE);
        if (displayNumber != null && !call.isGeneric()) {
            mPhoneNumber.setText(displayNumber);
            mPhoneNumber.setTextColor(mTextColorDefaultSecondary);
            mPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            mPhoneNumber.setVisibility(View.GONE);
        }
        if (label != null && !call.isGeneric()) {
            mLabel.setText(label);
            mLabel.setVisibility(View.VISIBLE);
        } else {
            mLabel.setVisibility(View.GONE);
        }
        if ((socialStatusText != null) && call.isRinging() && !call.isGeneric()) {
            mSocialStatus.setVisibility(View.VISIBLE);
            mSocialStatus.setText(socialStatusText);
            mSocialStatus.setCompoundDrawablesWithIntrinsicBounds(
                    socialStatusBadge, null, null, null);
            mSocialStatus.setCompoundDrawablePadding((int) (mDensity * 6));
        } else {
            mSocialStatus.setVisibility(View.GONE);
        }
    }
    private String getPresentationString(int presentation) {
        String name = getContext().getString(R.string.unknown);
        if (presentation == Connection.PRESENTATION_RESTRICTED) {
            name = getContext().getString(R.string.private_num);
        } else if (presentation == Connection.PRESENTATION_PAYPHONE) {
            name = getContext().getString(R.string.payphone);
        }
        return name;
    }
    private void updateDisplayForConference() {
        if (DBG) log("updateDisplayForConference()...");
        int phoneType = mApplication.phone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_CDMA) {
            showImage(mPhoto, R.drawable.picture_dialing);
            mName.setText(R.string.card_title_in_call);
        } else if (phoneType == Phone.PHONE_TYPE_GSM) {
            if (mInCallScreen.isTouchUiEnabled()) {
                mManageConferencePhotoButton.setVisibility(View.VISIBLE);
                mPhoto.setVisibility(View.INVISIBLE);  
            } else {
                showImage(mPhoto, R.drawable.picture_conference);
            }
            mName.setText(R.string.card_title_conf_call);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        mName.setVisibility(View.VISIBLE);
        mPhoneNumber.setVisibility(View.GONE);
        mLabel.setVisibility(View.GONE);
        mSocialStatus.setVisibility(View.GONE);
    }
    private void updatePhotoForCallState(Call call) {
        if (DBG) log("updatePhotoForCallState(" + call + ")...");
        int photoImageResource = 0;
        Call.State state = call.getState();
        switch (state) {
            case DISCONNECTED:
                Connection c = call.getEarliestConnection();
                if (c != null) {
                    Connection.DisconnectCause cause = c.getDisconnectCause();
                    if ((cause == Connection.DisconnectCause.BUSY)
                        || (cause == Connection.DisconnectCause.CONGESTION)) {
                        photoImageResource = R.drawable.picture_busy;
                    }
                } else if (DBG) {
                    log("updatePhotoForCallState: connection is null, ignoring.");
                }
                break;
            case ALERTING:
            case DIALING:
            default:
                CallerInfo ci = null;
                {
                    Connection conn = null;
                    int phoneType = mApplication.phone.getPhoneType();
                    if (phoneType == Phone.PHONE_TYPE_CDMA) {
                        conn = call.getLatestConnection();
                    } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                        conn = call.getEarliestConnection();
                    } else {
                        throw new IllegalStateException("Unexpected phone type: " + phoneType);
                    }
                    if (conn != null) {
                        Object o = conn.getUserData();
                        if (o instanceof CallerInfo) {
                            ci = (CallerInfo) o;
                        } else if (o instanceof PhoneUtils.CallerInfoToken) {
                            ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                        }
                    }
                }
                if (ci != null) {
                    photoImageResource = ci.photoResource;
                }
                if (photoImageResource == 0) {
                    if (!PhoneUtils.isConferenceCall(call)) {
                        if (!showCachedImage(mPhoto, ci) && (mPhotoTracker.getPhotoState() ==
                                ContactsAsyncHelper.ImageTracker.DISPLAY_DEFAULT)) {
                            ContactsAsyncHelper.updateImageViewWithContactPhotoAsync(ci,
                                    getContext(), mPhoto, mPhotoTracker.getPhotoUri(), -1);
                            mPhotoTracker.setPhotoState(
                                    ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);
                        }
                    }
                } else {
                    showImage(mPhoto, photoImageResource);
                    mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);
                    return;
                }
                break;
        }
        if (photoImageResource != 0) {
            if (DBG) log("- overrriding photo image: " + photoImageResource);
            showImage(mPhoto, photoImageResource);
            mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_DEFAULT);
        }
    }
    private static final boolean showCachedImage(ImageView view, CallerInfo ci) {
        if ((ci != null) && ci.isCachedPhotoCurrent) {
            if (ci.cachedPhoto != null) {
                showImage(view, ci.cachedPhoto);
            } else {
                showImage(view, R.drawable.picture_unknown);
            }
            return true;
        }
        return false;
    }
    private static final void showImage(ImageView view, int resource) {
        view.setImageResource(resource);
        view.setVisibility(View.VISIBLE);
    }
    private static final void showImage(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
        view.setVisibility(View.VISIBLE);
    }
     TextView getMenuButtonHint() {
        return mMenuButtonHint;
    }
    private void setSideMargins(ViewGroup vg, int margin) {
        ViewGroup.MarginLayoutParams lp =
                (ViewGroup.MarginLayoutParams) vg.getLayoutParams();
        lp.leftMargin = margin;
        lp.rightMargin = margin;
        vg.setLayoutParams(lp);
    }
    private void setUpperTitle(String title, int color, Call.State state) {
        mUpperTitle.setText(title);
        mUpperTitle.setTextColor(color);
        int bluetoothIconId = 0;
        if (!TextUtils.isEmpty(title)
                && ((state == Call.State.INCOMING) || (state == Call.State.WAITING))
                && mApplication.showBluetoothIndication()) {
            bluetoothIconId = R.drawable.ic_incoming_call_bluetooth;
        }
        mUpperTitle.setCompoundDrawablesWithIntrinsicBounds(bluetoothIconId, 0, 0, 0);
        if (bluetoothIconId != 0) mUpperTitle.setCompoundDrawablePadding((int) (mDensity * 5));
    }
    private void clearUpperTitle() {
        setUpperTitle("", 0, Call.State.IDLE);  
    }
    public void hideCallCardElements() {
        mPrimaryCallInfo.setVisibility(View.GONE);
        mSecondaryCallInfo.setVisibility(View.GONE);
    }
     void setRotarySelectorHint(int hintTextResId, int hintColorResId) {
        mRotarySelectorHintTextResId = hintTextResId;
        mRotarySelectorHintColorResId = hintColorResId;
    }
    public void onClick(View view) {
        int id = view.getId();
        if (DBG) log("onClick(View " + view + ", id " + id + ")...");
        switch (id) {
            case R.id.manageConferencePhotoButton:
                mInCallScreen.handleOnscreenButtonClick(id);
                break;
            default:
                Log.w(LOG_TAG, "onClick: unexpected click: View " + view + ", id " + id);
                break;
        }
    }
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        dispatchPopulateAccessibilityEvent(event, mUpperTitle);
        dispatchPopulateAccessibilityEvent(event, mPhoto);
        dispatchPopulateAccessibilityEvent(event, mManageConferencePhotoButton);
        dispatchPopulateAccessibilityEvent(event, mName);
        dispatchPopulateAccessibilityEvent(event, mPhoneNumber);
        dispatchPopulateAccessibilityEvent(event, mLabel);
        dispatchPopulateAccessibilityEvent(event, mSocialStatus);
        dispatchPopulateAccessibilityEvent(event, mSecondaryCallName);
        dispatchPopulateAccessibilityEvent(event, mSecondaryCallStatus);
        dispatchPopulateAccessibilityEvent(event, mSecondaryCallPhoto);
        return true;
    }
    private void dispatchPopulateAccessibilityEvent(AccessibilityEvent event, View view) {
        List<CharSequence> eventText = event.getText();
        int size = eventText.size();
        view.dispatchPopulateAccessibilityEvent(event);
        if (size == eventText.size()) {
            eventText.add(null);
        }
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
