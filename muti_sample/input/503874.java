public class ManageConferenceUtils
        implements CallerInfoAsyncQuery.OnQueryCompleteListener {
    private static final String LOG_TAG = "ManageConferenceUtils";
    private static final boolean DBG =
            (PhoneApp.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private InCallScreen mInCallScreen;
    private Phone mPhone;
    private ViewGroup mManageConferencePanel;
    private Button mButtonManageConferenceDone;
    private ViewGroup[] mConferenceCallList;
    private int mNumCallersInConference;
    private Chronometer mConferenceTime;
    private static final int MAX_CALLERS_IN_CONFERENCE = 5;
    public ManageConferenceUtils(InCallScreen inCallScreen, Phone phone) {
        if (DBG) log("ManageConferenceUtils constructor...");
        mInCallScreen = inCallScreen;
        mPhone = phone;
    }
    public void initManageConferencePanel() {
        if (DBG) log("initManageConferencePanel()...");
        if (mManageConferencePanel == null) {
            if (DBG) log("initManageConferencePanel: first-time initialization!");
            ViewStub stub = (ViewStub) mInCallScreen.findViewById(R.id.manageConferencePanelStub);
            stub.inflate();
            mManageConferencePanel =
                    (ViewGroup) mInCallScreen.findViewById(R.id.manageConferencePanel);
            if (mManageConferencePanel == null) {
                throw new IllegalStateException("Couldn't find manageConferencePanel!");
            }
            mConferenceTime =
                    (Chronometer) mInCallScreen.findViewById(R.id.manageConferencePanelHeader);
            mConferenceTime.setFormat(mInCallScreen.getString(R.string.caller_manage_header));
            mConferenceCallList = new ViewGroup[MAX_CALLERS_IN_CONFERENCE];
            final int[] viewGroupIdList = { R.id.caller0, R.id.caller1, R.id.caller2,
                                            R.id.caller3, R.id.caller4 };
            for (int i = 0; i < MAX_CALLERS_IN_CONFERENCE; i++) {
                mConferenceCallList[i] =
                        (ViewGroup) mInCallScreen.findViewById(viewGroupIdList[i]);
            }
            mButtonManageConferenceDone = (Button) mInCallScreen.findViewById(R.id.manage_done);
            mButtonManageConferenceDone.setOnClickListener(mInCallScreen);
        }
    }
    public void setPanelVisible(boolean visible) {
        if (mManageConferencePanel != null) {
            mManageConferencePanel.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
    public void startConferenceTime(long base) {
        if (mConferenceTime != null) {
            mConferenceTime.setBase(base);
            mConferenceTime.start();
        }
    }
    public void stopConferenceTime() {
        if (mConferenceTime != null) {
            mConferenceTime.stop();
        }
    }
    public int getNumCallersInConference() {
        return mNumCallersInConference;
    }
    public void updateManageConferencePanel(List<Connection> connections) {
        mNumCallersInConference = connections.size();
        if (DBG) log("updateManageConferencePanel()... num connections in conference = "
                      + mNumCallersInConference);
        final boolean hasActiveCall = !mPhone.getForegroundCall().isIdle();
        final boolean hasHoldingCall = !mPhone.getBackgroundCall().isIdle();
        boolean canSeparate = !(hasActiveCall && hasHoldingCall);
        for (int i = 0; i < MAX_CALLERS_IN_CONFERENCE; i++) {
            if (i < mNumCallersInConference) {
                Connection connection = (Connection) connections.get(i);
                updateManageConferenceRow(i, connection, canSeparate);
            } else {
                updateManageConferenceRow(i, null, false);
            }
        }
    }
    public void updateManageConferenceRow(final int i,
                                          final Connection connection,
                                          boolean canSeparate) {
        if (DBG) log("updateManageConferenceRow(" + i + ")...  connection = " + connection);
        if (connection != null) {
            mConferenceCallList[i].setVisibility(View.VISIBLE);
            ImageButton endButton = (ImageButton) mConferenceCallList[i].findViewById(
                    R.id.conferenceCallerDisconnect);
            ImageButton separateButton = (ImageButton) mConferenceCallList[i].findViewById(
                    R.id.conferenceCallerSeparate);
            TextView nameTextView = (TextView) mConferenceCallList[i].findViewById(
                    R.id.conferenceCallerName);
            TextView numberTextView = (TextView) mConferenceCallList[i].findViewById(
                    R.id.conferenceCallerNumber);
            TextView numberTypeTextView = (TextView) mConferenceCallList[i].findViewById(
                    R.id.conferenceCallerNumberType);
            if (DBG) log("- button: " + endButton + ", nameTextView: " + nameTextView);
            View.OnClickListener endThisConnection = new View.OnClickListener() {
                    public void onClick(View v) {
                        endConferenceConnection(i, connection);
                        PhoneApp.getInstance().pokeUserActivity();
                    }
                };
            endButton.setOnClickListener(endThisConnection);
            if (canSeparate) {
                View.OnClickListener separateThisConnection = new View.OnClickListener() {
                        public void onClick(View v) {
                            separateConferenceConnection(i, connection);
                            PhoneApp.getInstance().pokeUserActivity();
                        }
                    };
                separateButton.setOnClickListener(separateThisConnection);
                separateButton.setVisibility(View.VISIBLE);
            } else {
                separateButton.setVisibility(View.INVISIBLE);
            }
            PhoneUtils.CallerInfoToken info =
                    PhoneUtils.startGetCallerInfo(mInCallScreen,
                                                  connection,
                                                  this,
                                                  mConferenceCallList[i]);
            if (DBG) log("  - got info from startGetCallerInfo(): " + info);
            displayCallerInfoForConferenceRow(info.currentInfo, nameTextView,
                                              numberTypeTextView, numberTextView);
        } else {
            mConferenceCallList[i].setVisibility(View.GONE);
        }
    }
    public final void displayCallerInfoForConferenceRow(CallerInfo ci,
                                                        TextView nameTextView,
                                                        TextView numberTypeTextView,
                                                        TextView numberTextView) {
        String callerName = "";
        String callerNumber = "";
        String callerNumberType = "";
        if (ci != null) {
            callerName = ci.name;
            if (TextUtils.isEmpty(callerName)) {
                callerName = ci.phoneNumber;
                if (TextUtils.isEmpty(callerName)) {
                    callerName = mInCallScreen.getString(R.string.unknown);
                }
            } else {
                callerNumber = ci.phoneNumber;
                callerNumberType = ci.phoneLabel;
            }
        }
        nameTextView.setText(callerName);
        if (TextUtils.isEmpty(callerNumber)) {
            numberTextView.setVisibility(View.GONE);
            numberTypeTextView.setVisibility(View.GONE);
        } else {
            numberTextView.setVisibility(View.VISIBLE);
            numberTextView.setText(callerNumber);
            numberTypeTextView.setVisibility(View.VISIBLE);
            numberTypeTextView.setText(callerNumberType);
        }
    }
    public void endConferenceConnection(int i, Connection connection) {
        if (DBG) log("===> ENDING conference connection " + i
                      + ": Connection " + connection);
        PhoneUtils.hangup(connection);
    }
    public void separateConferenceConnection(int i, Connection connection) {
        if (DBG) log("===> SEPARATING conference connection " + i
                      + ": Connection " + connection);
        PhoneUtils.separateCall(connection);
    }
    public void onQueryComplete(int token, Object cookie, CallerInfo ci) {
        if (DBG) log("callerinfo query complete, updating UI." + ci);
        ViewGroup vg = (ViewGroup) cookie;
        vg.setVisibility(View.VISIBLE);
        displayCallerInfoForConferenceRow(ci,
                (TextView) vg.findViewById(R.id.conferenceCallerName),
                (TextView) vg.findViewById(R.id.conferenceCallerNumberType),
                (TextView) vg.findViewById(R.id.conferenceCallerNumber));
    }
    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
