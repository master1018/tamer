public class CallFeaturesSetting extends PreferenceActivity
        implements DialogInterface.OnClickListener,
        Preference.OnPreferenceChangeListener,
        EditPhoneNumberPreference.OnDialogClosedListener,
        EditPhoneNumberPreference.GetDefaultNumberListener{
    public static final String ACTION_ADD_VOICEMAIL =
        "com.android.phone.CallFeaturesSetting.ADD_VOICEMAIL";
    public static final String ACTION_CONFIGURE_VOICEMAIL =
        "com.android.phone.CallFeaturesSetting.CONFIGURE_VOICEMAIL";
    public static final String VM_NUMBER_EXTRA = "com.android.phone.VoicemailNumber";
    public static final String FWD_NUMBER_EXTRA = "com.android.phone.ForwardingNumber";
    public static final String FWD_NUMBER_TIME_EXTRA = "com.android.phone.ForwardingNumberTime";
    public static final String SIGNOUT_EXTRA = "com.android.phone.Signout";
    public static final CallForwardInfo[] FWD_SETTINGS_DONT_TOUCH = null;
    public static final String VM_NUMBER_TAG = "#VMNumber";
    public static final String FWD_SETTINGS_TAG = "#FWDSettings";
    public static final String FWD_SETTINGS_LENGTH_TAG = "#Length";
    public static final String FWD_SETTING_TAG = "#Setting";
    public static final String FWD_SETTING_STATUS = "#Status";
    public static final String FWD_SETTING_REASON = "#Reason";
    public static final String FWD_SETTING_NUMBER = "#Number";
    public static final String FWD_SETTING_TIME = "#Time";
    public static final String DEFAULT_VM_PROVIDER_KEY = "";
    public static final String IGNORE_PROVIDER_EXTRA = "com.android.phone.ProviderToIgnore";
    private static final String LOG_TAG = "CallFeaturesSetting";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private static final String NUM_PROJECTION[] = {CommonDataKinds.Phone.NUMBER};
    private static final String BUTTON_VOICEMAIL_KEY = "button_voicemail_key";
    private static final String BUTTON_VOICEMAIL_PROVIDER_KEY = "button_voicemail_provider_key";
    private static final String BUTTON_VOICEMAIL_SETTING_KEY = "button_voicemail_setting_key";
    private static final String BUTTON_FDN_KEY   = "button_fdn_key";
    private static final String BUTTON_DTMF_KEY   = "button_dtmf_settings";
    private static final String BUTTON_RETRY_KEY  = "button_auto_retry_key";
    private static final String BUTTON_TTY_KEY    = "button_tty_mode_key";
    private static final String BUTTON_HAC_KEY    = "button_hac_key";
    private static final String BUTTON_GSM_UMTS_OPTIONS = "button_gsm_more_expand_key";
    private static final String BUTTON_CDMA_OPTIONS = "button_cdma_more_expand_key";
    private static final String VM_NUMBERS_SHARED_PREFERENCES_NAME = "vm_numbers";
    private Intent mContactListIntent;
    private static final int EVENT_VOICEMAIL_CHANGED        = 500;
    private static final int EVENT_FORWARDING_CHANGED       = 501;
    private static final int EVENT_FORWARDING_GET_COMPLETED = 502;
    static final int preferredTtyMode = Phone.TTY_MODE_OFF;
    static final int DTMF_TONE_TYPE_NORMAL = 0;
    static final int DTMF_TONE_TYPE_LONG   = 1;
    public static final String HAC_KEY = "HACSetting";
    public static final String HAC_VAL_ON = "ON";
    public static final String HAC_VAL_OFF = "OFF";
    private static final int VOICEMAIL_PREF_ID = 1;
    private static final int VOICEMAIL_PROVIDER_CFG_ID = 2;
    private Phone mPhone;
    private AudioManager mAudioManager;
    private static final int VM_NOCHANGE_ERROR = 400;
    private static final int VM_RESPONSE_ERROR = 500;
    private static final int FW_SET_RESPONSE_ERROR = 501;
    private static final int FW_GET_RESPONSE_ERROR = 502;
    private static final int VOICEMAIL_DIALOG_CONFIRM = 600;
    private static final int VOICEMAIL_FWD_SAVING_DIALOG = 601;
    private static final int VOICEMAIL_FWD_READING_DIALOG = 602;
    private static final int VOICEMAIL_REVERTING_DIALOG = 603;
    private static final int MSG_OK = 100;
    private static final int MSG_VM_EXCEPTION = 400;
    private static final int MSG_FW_SET_EXCEPTION = 401;
    private static final int MSG_FW_GET_EXCEPTION = 402;
    private static final int MSG_VM_OK = 600;
    private static final int MSG_VM_NOCHANGE = 700;
    private EditPhoneNumberPreference mSubMenuVoicemailSettings;
    private CheckBoxPreference mButtonAutoRetry;
    private CheckBoxPreference mButtonHAC;
    private ListPreference mButtonDTMF;
    private ListPreference mButtonTTY;
    private ListPreference mVoicemailProviders;
    private PreferenceScreen mVoicemailSettings;
    private class VoiceMailProvider {
        public VoiceMailProvider(String name, Intent intent) {
            this.name = name;
            this.intent = intent;
        }
        public String name;
        public Intent intent;
    }
    static final int [] FORWARDING_SETTINGS_REASONS = new int[] {
        CommandsInterface.CF_REASON_UNCONDITIONAL,
        CommandsInterface.CF_REASON_BUSY,
        CommandsInterface.CF_REASON_NO_REPLY,
        CommandsInterface.CF_REASON_NOT_REACHABLE
    };
    private class VoiceMailProviderSettings {
        public VoiceMailProviderSettings(String voicemailNumber, String forwardingNumber,
                int timeSeconds) {
            this.voicemailNumber = voicemailNumber;
            if (forwardingNumber == null || forwardingNumber.length() == 0) {
                this.forwardingSettings = FWD_SETTINGS_DONT_TOUCH;
            } else {
                this.forwardingSettings = new CallForwardInfo[FORWARDING_SETTINGS_REASONS.length];
                for (int i = 0; i < this.forwardingSettings.length; i++) {
                    CallForwardInfo fi = new CallForwardInfo();
                    this.forwardingSettings[i] = fi;
                    fi.reason = FORWARDING_SETTINGS_REASONS[i];
                    fi.status = (fi.reason == CommandsInterface.CF_REASON_UNCONDITIONAL) ? 0 : 1;
                    fi.serviceClass = CommandsInterface.SERVICE_CLASS_VOICE;
                    fi.toa = PhoneNumberUtils.TOA_International;
                    fi.number = forwardingNumber;
                    fi.timeSeconds = timeSeconds;
                }
            }
        }
        public VoiceMailProviderSettings(String voicemailNumber, CallForwardInfo[] infos) {
            this.voicemailNumber = voicemailNumber;
            this.forwardingSettings = infos;
        }
        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof VoiceMailProviderSettings)) return false;
            final VoiceMailProviderSettings v = (VoiceMailProviderSettings)o;
            return ((this.voicemailNumber == null &&
                        v.voicemailNumber == null) ||
                    this.voicemailNumber != null &&
                        this.voicemailNumber.equals(v.voicemailNumber))
                    &&
                    forwardingSettingsEqual(this.forwardingSettings,
                            v.forwardingSettings);
        }
        private boolean forwardingSettingsEqual(CallForwardInfo[] infos1,
                CallForwardInfo[] infos2) {
            if (infos1 == infos2) return true;
            if (infos1 == null || infos2 == null) return false;
            if (infos1.length != infos2.length) return false;
            for (int i = 0; i < infos1.length; i++) {
                CallForwardInfo i1 = infos1[i];
                CallForwardInfo i2 = infos2[i];
                if (i1.status != i2.status ||
                    i1.reason != i2.reason ||
                    i1.serviceClass != i2.serviceClass ||
                    i1.toa != i2.toa ||
                    i1.number != i2.number ||
                    i1.timeSeconds != i2.timeSeconds) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public String toString() {
            return voicemailNumber + ((forwardingSettings != null ) ? (", " +
                    forwardingSettings.toString()) : "");
        }
        public String voicemailNumber;
        public CallForwardInfo[] forwardingSettings;
    }
    SharedPreferences mPerProviderSavedVMNumbers;
    CallForwardInfo[] mForwardingReadResults = null;
    private Map<Integer, AsyncResult> mForwardingChangeResults = null;
    private Collection<Integer> mExpectedChangeResultReasons = null;
    AsyncResult mVoicemailChangeResult = null;
    String mPreviousVMProviderKey = null;
    int mCurrentDialogId = 0;
    boolean mVMProviderSettingsForced = false;
    boolean mChangingVMorFwdDueToProviderChange = false;
    boolean mVMChangeCompletedSuccesfully = false;
    boolean mFwdChangesRequireRollback = false;
    int mVMOrFwdSetError = 0;
    private final Map<String, VoiceMailProvider> mVMProvidersData =
        new HashMap<String, VoiceMailProvider>();
    private String mOldVmNumber;
    private CallForwardInfo[] mNewFwdSettings;
    String mNewVMNumber;
    private boolean mForeground;
    @Override
    public void onPause() {
        super.onPause();
        mForeground = false;
    }
    private boolean mReadingSettingsForDefaultProvider = false;
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSubMenuVoicemailSettings) {
            return true;
        } else if (preference == mButtonDTMF) {
            return true;
        } else if (preference == mButtonTTY) {
            return true;
        } else if (preference == mButtonAutoRetry) {
            android.provider.Settings.System.putInt(mPhone.getContext().getContentResolver(),
                    android.provider.Settings.System.CALL_AUTO_RETRY,
                    mButtonAutoRetry.isChecked() ? 1 : 0);
            return true;
        } else if (preference == mButtonHAC) {
            int hac = mButtonHAC.isChecked() ? 1 : 0;
            Settings.System.putInt(mPhone.getContext().getContentResolver(),
                    Settings.System.HEARING_AID, hac);
            mAudioManager.setParameter(HAC_KEY, hac != 0 ? HAC_VAL_ON : HAC_VAL_OFF);
            return true;
        } else if (preference == mVoicemailSettings && preference.getIntent() != null) {
            if (DBG) log("Invoking cfg intent " + preference.getIntent().getPackage());
            this.startActivityForResult(preference.getIntent(), VOICEMAIL_PROVIDER_CFG_ID);
            return true;
        }
        return false;
    }
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mButtonDTMF) {
            int index = mButtonDTMF.findIndexOfValue((String) objValue);
            Settings.System.putInt(mPhone.getContext().getContentResolver(),
                    Settings.System.DTMF_TONE_TYPE_WHEN_DIALING, index);
        } else if (preference == mButtonTTY) {
            handleTTYChange(preference, objValue);
        } else if (preference == mVoicemailProviders) {
            final String currentProviderKey = getCurrentVoicemailProviderKey();
            final String newProviderKey = (String)objValue;
            if (DBG) log("VM provider changes to " + newProviderKey + " from " +
                    mPreviousVMProviderKey);
            if (mPreviousVMProviderKey.equals(newProviderKey)) {
                if (DBG) log("No change ");
                return true;
            }
            updateVMPreferenceWidgets(newProviderKey);
            mPreviousVMProviderKey = currentProviderKey;
            final VoiceMailProviderSettings newProviderSettings =
                    loadSettingsForVoiceMailProvider(newProviderKey);
            if (newProviderSettings == null) {
                if (DBG) log("Saved preferences not found - invoking config");
                mVMProviderSettingsForced = true;
                simulatePreferenceClick(mVoicemailSettings);
            } else {
                if (DBG) log("Saved preferences found - switching to them");
                mChangingVMorFwdDueToProviderChange = true;
                saveVoiceMailAndForwardingNumber(newProviderKey, newProviderSettings);
            }
        }
        return true;
    }
    public void onDialogClosed(EditPhoneNumberPreference preference, int buttonClicked) {
        if (DBG) log("onPreferenceClick: request preference click on dialog close: " +
                buttonClicked);
        if (buttonClicked == DialogInterface.BUTTON_NEGATIVE) {
            return;
        }
        if (preference instanceof EditPhoneNumberPreference) {
            EditPhoneNumberPreference epn = preference;
            if (epn == mSubMenuVoicemailSettings) {
                handleVMBtnClickRequest();
            }
        }
    }
    public String onGetDefaultNumber(EditPhoneNumberPreference preference) {
        if (preference == mSubMenuVoicemailSettings) {
            if (DBG) log("updating default for voicemail dialog");
            updateVoiceNumberField();
            return null;
        }
        String vmDisplay = mPhone.getVoiceMailNumber();
        if (TextUtils.isEmpty(vmDisplay)) {
            return null;
        }
        if (DBG) log("updating default for call forwarding dialogs");
        return getString(R.string.voicemail_abbreviated) + " " + vmDisplay;
    }
    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode == -1) {
            super.startActivityForResult(intent, requestCode);
            return;
        }
        if (DBG) log("startSubActivity: starting requested subactivity");
        super.startActivityForResult(intent, requestCode);
    }
    private void switchToPreviousVoicemailProvider() {
        if (DBG) log("switchToPreviousVoicemailProvider " + mPreviousVMProviderKey);
        if (mPreviousVMProviderKey != null) {
            if (mVMChangeCompletedSuccesfully || mFwdChangesRequireRollback) {
                showDialog(VOICEMAIL_REVERTING_DIALOG);
                VoiceMailProviderSettings prevSettings =
                    loadSettingsForVoiceMailProvider(mPreviousVMProviderKey);
                if (mVMChangeCompletedSuccesfully) {
                    mNewVMNumber = prevSettings.voicemailNumber;
                    if (DBG) log("have to revert VM to " + mNewVMNumber);
                    mPhone.setVoiceMailNumber(
                            mPhone.getVoiceMailAlphaTag().toString(),
                            mNewVMNumber,
                            Message.obtain(mRevertOptionComplete, EVENT_VOICEMAIL_CHANGED));
                }
                if (mFwdChangesRequireRollback) {
                    if (DBG) log("have to revert fwd");
                    final CallForwardInfo[] prevFwdSettings =
                        prevSettings.forwardingSettings;
                    if (prevFwdSettings != null) {
                        Map<Integer, AsyncResult> results =
                            mForwardingChangeResults;
                        resetForwardingChangeState();
                        for (int i = 0; i < prevFwdSettings.length; i++) {
                            CallForwardInfo fi = prevFwdSettings[i];
                            if (DBG) log("Reverting fwd #: " + i + ": " + fi.toString());
                            AsyncResult result = results.get(fi.reason);
                            if (result != null && result.exception == null) {
                                mExpectedChangeResultReasons.add(fi.reason);
                                mPhone.setCallForwardingOption(
                                        (fi.status == 1 ?
                                                CommandsInterface.CF_ACTION_REGISTRATION :
                                                CommandsInterface.CF_ACTION_DISABLE),
                                        fi.reason,
                                        fi.number,
                                        fi.timeSeconds,
                                        mRevertOptionComplete.obtainMessage(
                                                EVENT_FORWARDING_CHANGED, i, 0));
                            }
                        }
                    }
                }
            } else {
                if (DBG) log("No need to revert");
                onRevertDone();
            }
        }
    }
    void onRevertDone() {
        if (DBG) log("Flipping provider key back to " + mPreviousVMProviderKey);
        mVoicemailProviders.setValue(mPreviousVMProviderKey);
        updateVMPreferenceWidgets(mPreviousVMProviderKey);
        updateVoiceNumberField();
        if (mVMOrFwdSetError != 0) {
            showVMDialog(mVMOrFwdSetError);
            mVMOrFwdSetError = 0;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  VOICEMAIL_PROVIDER_CFG_ID) {
            boolean failure = false;
            if (DBG) log("mVMProviderSettingsForced: " + mVMProviderSettingsForced);
            final boolean isVMProviderSettingsForced = mVMProviderSettingsForced;
            mVMProviderSettingsForced = false;
            String vmNum = null;
            if (resultCode != RESULT_OK) {
                if (DBG) log("onActivityResult: vm provider cfg result not OK.");
                failure = true;
            } else {
                if (data == null) {
                    if (DBG) log("onActivityResult: vm provider cfg result has no data");
                    failure = true;
                } else {
                    if (data.getBooleanExtra(SIGNOUT_EXTRA, false)) {
                        if (DBG) log("Provider requested signout");
                        if (isVMProviderSettingsForced) {
                            if (DBG) log("Going back to previous provider on signout");
                            switchToPreviousVoicemailProvider();
                        } else {
                            final String victim = getCurrentVoicemailProviderKey();
                            if (DBG) log("Relaunching activity and ignoring " + victim);
                            Intent i = new Intent(ACTION_ADD_VOICEMAIL);
                            i.putExtra(IGNORE_PROVIDER_EXTRA, victim);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            this.startActivity(i);
                        }
                        return;
                    }
                    vmNum = data.getStringExtra(VM_NUMBER_EXTRA);
                    if (vmNum == null || vmNum.length() == 0) {
                        if (DBG) log("onActivityResult: vm provider cfg result has no vmnum");
                        failure = true;
                    }
                }
            }
            if (failure) {
                if (DBG) log("Failure in return from voicemail provider");
                if (isVMProviderSettingsForced) {
                    switchToPreviousVoicemailProvider();
                } else {
                    if (DBG) log("Not switching back the provider since this is not forced config");
                }
                return;
            }
            mChangingVMorFwdDueToProviderChange = isVMProviderSettingsForced;
            final String fwdNum = data.getStringExtra(FWD_NUMBER_EXTRA);
            final int fwdNumTime = data.getIntExtra(FWD_NUMBER_TIME_EXTRA, 20);
            if (DBG) log("onActivityResult: vm provider cfg result " +
                    (fwdNum != null ? "has" : " does not have") + " forwarding number");
            saveVoiceMailAndForwardingNumber(getCurrentVoicemailProviderKey(),
                    new VoiceMailProviderSettings(vmNum, fwdNum, fwdNumTime));
            return;
        }
        if (resultCode != RESULT_OK) {
            if (DBG) log("onActivityResult: contact picker result not OK.");
            return;
        }
        Cursor cursor = getContentResolver().query(data.getData(),
                NUM_PROJECTION, null, null, null);
        if ((cursor == null) || (!cursor.moveToFirst())) {
            if (DBG) log("onActivityResult: bad contact data, no results found.");
            return;
        }
        switch (requestCode) {
            case VOICEMAIL_PREF_ID:
                mSubMenuVoicemailSettings.onPickActivityResult(cursor.getString(0));
                break;
            default:
        }
    }
    private void handleVMBtnClickRequest() {
        saveVoiceMailAndForwardingNumber(
                getCurrentVoicemailProviderKey(),
                new VoiceMailProviderSettings(mSubMenuVoicemailSettings.getPhoneNumber(),
                        FWD_SETTINGS_DONT_TOUCH));
    }
    private void showDialogIfForeground(int id) {
        if (mForeground) {
            showDialog(id);
        }
    }
    private void dismissDialogSafely(int id) {
        try {
            dismissDialog(id);
        } catch (IllegalArgumentException e) {
        }
    }
    private void saveVoiceMailAndForwardingNumber(String key,
            VoiceMailProviderSettings newSettings) {
        if (DBG) log("saveVoiceMailAndForwardingNumber: " + newSettings.toString());
        mNewVMNumber = newSettings.voicemailNumber;
        if (mNewVMNumber == null) {
            mNewVMNumber = "";
        }
        mNewFwdSettings = newSettings.forwardingSettings;
        if (DBG) log("newFwdNumber " +
                String.valueOf((mNewFwdSettings != null ? mNewFwdSettings.length : 0))
                + " settings");
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            if (DBG) log("ignoring forwarding setting since this is CDMA phone");
            mNewFwdSettings = FWD_SETTINGS_DONT_TOUCH;
        }
        if (mNewVMNumber.equals(mOldVmNumber) && mNewFwdSettings == FWD_SETTINGS_DONT_TOUCH) {
            showVMDialog(MSG_VM_NOCHANGE);
            return;
        }
        maybeSaveSettingsForVoicemailProvider(key, newSettings);
        mVMChangeCompletedSuccesfully = false;
        mFwdChangesRequireRollback = false;
        mVMOrFwdSetError = 0;
        if (!key.equals(mPreviousVMProviderKey)) {
            mReadingSettingsForDefaultProvider =
                mPreviousVMProviderKey.equals(DEFAULT_VM_PROVIDER_KEY);
            if (DBG) log("Reading current forwarding settings");
            mForwardingReadResults = new CallForwardInfo[FORWARDING_SETTINGS_REASONS.length];
            for (int i = 0; i < FORWARDING_SETTINGS_REASONS.length; i++) {
                mForwardingReadResults[i] = null;
                mPhone.getCallForwardingOption(FORWARDING_SETTINGS_REASONS[i],
                        mGetOptionComplete.obtainMessage(EVENT_FORWARDING_GET_COMPLETED, i, 0));
            }
            showDialogIfForeground(VOICEMAIL_FWD_READING_DIALOG);
        } else {
            saveVoiceMailAndForwardingNumberStage2();
        }
    }
    private final Handler mGetOptionComplete = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult result = (AsyncResult) msg.obj;
            switch (msg.what) {
                case EVENT_FORWARDING_GET_COMPLETED:
                    handleForwardingSettingsReadResult(result, msg.arg1);
                    break;
            }
        }
    };
    void handleForwardingSettingsReadResult(AsyncResult ar, int idx) {
        if (DBG) Log.d(LOG_TAG, "handleForwardingSettingsReadResult: " + idx);
        Throwable error = null;
        if (ar.exception != null) {
            if (DBG) Log.d(LOG_TAG, "FwdRead: ar.exception=" +
                    ar.exception.getMessage());
            error = ar.exception;
        }
        if (ar.userObj instanceof Throwable) {
            if (DBG) Log.d(LOG_TAG, "FwdRead: userObj=" +
                    ((Throwable)ar.userObj).getMessage());
            error = (Throwable)ar.userObj;
        }
        if (mForwardingReadResults == null) {
            if (DBG) Log.d(LOG_TAG, "ignoring fwd reading result: " + idx);
            return;
        }
        if (error != null) {
            if (DBG) Log.d(LOG_TAG, "Error discovered for fwd read : " + idx);
            mForwardingReadResults = null;
            dismissDialogSafely(VOICEMAIL_FWD_READING_DIALOG);
            showVMDialog(MSG_FW_GET_EXCEPTION);
            return;
        }
        final CallForwardInfo cfInfoArray[] = (CallForwardInfo[]) ar.result;
        CallForwardInfo fi = null;
        for (int i = 0 ; i < cfInfoArray.length; i++) {
            if ((cfInfoArray[i].serviceClass & CommandsInterface.SERVICE_CLASS_VOICE) != 0) {
                fi = cfInfoArray[i];
                break;
            }
        }
        if (fi == null) {
            if (DBG) Log.d(LOG_TAG, "Creating default info for " + idx);
            fi = new CallForwardInfo();
            fi.status = 0;
            fi.reason = FORWARDING_SETTINGS_REASONS[idx];
            fi.serviceClass = CommandsInterface.SERVICE_CLASS_VOICE;
        } else {
            if (fi.number == null || fi.number.length() == 0) {
                fi.status = 0;
            }
            if (DBG) Log.d(LOG_TAG, "Got  " + fi.toString() + " for " + idx);
        }
        mForwardingReadResults[idx] = fi;
        boolean done = true;
        for (int i = 0; i < mForwardingReadResults.length; i++) {
            if (mForwardingReadResults[i] == null) {
                done = false;
                break;
            }
        }
        if (done) {
            if (DBG) Log.d(LOG_TAG, "Done receiving fwd info");
            dismissDialogSafely(VOICEMAIL_FWD_READING_DIALOG);
            if (mReadingSettingsForDefaultProvider) {
                maybeSaveSettingsForVoicemailProvider(DEFAULT_VM_PROVIDER_KEY,
                        new VoiceMailProviderSettings(this.mOldVmNumber,
                                mForwardingReadResults));
                mReadingSettingsForDefaultProvider = false;
            }
            saveVoiceMailAndForwardingNumberStage2();
        } else {
            if (DBG) Log.d(LOG_TAG, "Not done receiving fwd info");
        }
    }
    private CallForwardInfo infoForReason(CallForwardInfo[] infos, int reason) {
        CallForwardInfo result = null;
        if (null != infos) {
            for (CallForwardInfo info : infos) {
                if (info.reason == reason) {
                    result = info;
                    break;
                }
            }
        }
        return result;
    }
    private boolean isUpdateRequired(CallForwardInfo oldInfo,
            CallForwardInfo newInfo) {
        boolean result = true;
        if (0 == newInfo.status) {
            if (oldInfo != null && oldInfo.status == 0) {
                result = false;
            }
        }
        return result;
    }
    private void resetForwardingChangeState() {
        mForwardingChangeResults = new HashMap<Integer, AsyncResult>();
        mExpectedChangeResultReasons = new HashSet<Integer>();
    }
    private void saveVoiceMailAndForwardingNumberStage2() {
        mForwardingChangeResults = null;
        mVoicemailChangeResult = null;
        if (mNewFwdSettings != FWD_SETTINGS_DONT_TOUCH) {
            resetForwardingChangeState();
            for (int i = 0; i < mNewFwdSettings.length; i++) {
                CallForwardInfo fi = mNewFwdSettings[i];
                final boolean doUpdate = isUpdateRequired(infoForReason(
                            mForwardingReadResults, fi.reason), fi);
                if (doUpdate) {
                    if (DBG) log("Setting fwd #: " + i + ": " + fi.toString());
                    mExpectedChangeResultReasons.add(i);
                    mPhone.setCallForwardingOption(
                            fi.status == 1 ?
                                    CommandsInterface.CF_ACTION_REGISTRATION :
                                    CommandsInterface.CF_ACTION_DISABLE,
                            fi.reason,
                            fi.number,
                            fi.timeSeconds,
                            mSetOptionComplete.obtainMessage(
                                    EVENT_FORWARDING_CHANGED, fi.reason, 0));
                }
             }
             showDialogIfForeground(VOICEMAIL_FWD_SAVING_DIALOG);
        } else {
            if (DBG) log("Not touching fwd #");
            setVMNumberWithCarrier();
        }
    }
    void setVMNumberWithCarrier() {
        if (DBG) log("save voicemail #: " + mNewVMNumber);
        mPhone.setVoiceMailNumber(
                mPhone.getVoiceMailAlphaTag().toString(),
                mNewVMNumber,
                Message.obtain(mSetOptionComplete, EVENT_VOICEMAIL_CHANGED));
    }
    private final Handler mSetOptionComplete = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult result = (AsyncResult) msg.obj;
            boolean done = false;
            switch (msg.what) {
                case EVENT_VOICEMAIL_CHANGED:
                    mVoicemailChangeResult = result;
                    mVMChangeCompletedSuccesfully = checkVMChangeSuccess() == null;
                    if (DBG) log("VM change complete msg, VM change done = " +
                            String.valueOf(mVMChangeCompletedSuccesfully));
                    done = true;
                    break;
                case EVENT_FORWARDING_CHANGED:
                    mForwardingChangeResults.put(msg.arg1, result);
                    if (result.exception != null) {
                        if (DBG) log("Error in setting fwd# " + msg.arg1 + ": " +
                                result.exception.getMessage());
                    } else {
                        if (DBG) log("Success in setting fwd# " + msg.arg1);
                    }
                    final boolean completed = checkForwardingCompleted();
                    if (completed) {
                        if (checkFwdChangeSuccess() == null) {
                            if (DBG) log("Overall fwd changes completed ok, starting vm change");
                            setVMNumberWithCarrier();
                        } else {
                            if (DBG) log("Overall fwd changes completed, failure");
                            mFwdChangesRequireRollback = false;
                            Iterator<Map.Entry<Integer,AsyncResult>> it =
                                mForwardingChangeResults.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry<Integer,AsyncResult> entry = it.next();
                                if (entry.getValue().exception == null) {
                                    if (DBG) log("Rollback will be required");
                                    mFwdChangesRequireRollback =true;
                                    break;
                                }
                            }
                            done = true;
                        }
                    }
                    break;
                default:
            }
            if (done) {
                if (DBG) log("All VM provider related changes done");
                if (mForwardingChangeResults != null) {
                    dismissDialogSafely(VOICEMAIL_FWD_SAVING_DIALOG);
                }
                handleSetVMOrFwdMessage();
            }
        }
    };
    private final Handler mRevertOptionComplete = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult result = (AsyncResult) msg.obj;
            switch (msg.what) {
                case EVENT_VOICEMAIL_CHANGED:
                    mVoicemailChangeResult = result;
                    if (DBG) log("VM revert complete msg");
                    break;
                case EVENT_FORWARDING_CHANGED:
                    mForwardingChangeResults.put(msg.arg1, result);
                    if (result.exception != null) {
                        if (DBG) log("Error in reverting fwd# " + msg.arg1 + ": " +
                                result.exception.getMessage());
                    } else {
                        if (DBG) log("Success in reverting fwd# " + msg.arg1);
                    }
                    if (DBG) log("FWD revert complete msg ");
                    break;
                default:
            }
            final boolean done =
                (!mVMChangeCompletedSuccesfully || mVoicemailChangeResult != null) &&
                (!mFwdChangesRequireRollback || checkForwardingCompleted());
            if (done) {
                if (DBG) log("All VM reverts done");
                dismissDialogSafely(VOICEMAIL_REVERTING_DIALOG);
                onRevertDone();
            }
        }
    };
    private boolean checkForwardingCompleted() {
        boolean result;
        if (mForwardingChangeResults == null) {
            result = true;
        } else {
            result = true;
            for (Integer reason : mExpectedChangeResultReasons) {
                if (mForwardingChangeResults.get(reason) == null) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
    private String checkFwdChangeSuccess() {
        String result = null;
        Iterator<Map.Entry<Integer,AsyncResult>> it =
            mForwardingChangeResults.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer,AsyncResult> entry = it.next();
            Throwable exception = entry.getValue().exception;
            if (exception != null) {
                result = exception.getMessage();
                if (result == null) {
                    result = "";
                }
                break;
            }
        }
        return result;
    }
    private String checkVMChangeSuccess() {
        if (mVoicemailChangeResult.exception != null) {
            final String msg = mVoicemailChangeResult.exception.getMessage();
            if (msg == null) {
                return "";
            }
            return msg;
        }
        return null;
    }
    private void handleSetVMOrFwdMessage() {
        if (DBG) {
            log("handleSetVMMessage: set VM request complete");
        }
        boolean success = true;
        boolean fwdFailure = false;
        String exceptionMessage = "";
        if (mForwardingChangeResults != null) {
            exceptionMessage = checkFwdChangeSuccess();
            if (exceptionMessage != null) {
                success = false;
                fwdFailure = true;
            }
        }
        if (success) {
            exceptionMessage = checkVMChangeSuccess();
            if (exceptionMessage != null) {
                success = false;
            }
        }
        if (success) {
            if (DBG) log("change VM success!");
            handleVMAndFwdSetSuccess(MSG_VM_OK);
            updateVoiceNumberField();
        } else {
            if (fwdFailure) {
                log("change FW failed: " + exceptionMessage);
                handleVMOrFwdSetError(MSG_FW_SET_EXCEPTION);
            } else {
                log("change VM failed: " + exceptionMessage);
                handleVMOrFwdSetError(MSG_VM_EXCEPTION);
            }
        }
    }
    private void handleVMOrFwdSetError(int msgId) {
        if (mChangingVMorFwdDueToProviderChange) {
            mVMOrFwdSetError = msgId;
            mChangingVMorFwdDueToProviderChange = false;
            switchToPreviousVoicemailProvider();
            return;
        }
        mChangingVMorFwdDueToProviderChange = false;
        showVMDialog(msgId);
        updateVoiceNumberField();
    }
    private void handleVMAndFwdSetSuccess(int msgId) {
        mChangingVMorFwdDueToProviderChange = false;
        showVMDialog(msgId);
    }
    private void updateVoiceNumberField() {
        if (mSubMenuVoicemailSettings == null) {
            return;
        }
        mOldVmNumber = mPhone.getVoiceMailNumber();
        if (mOldVmNumber == null) {
            mOldVmNumber = "";
        }
        mSubMenuVoicemailSettings.setPhoneNumber(mOldVmNumber);
        final String summary = (mOldVmNumber.length() > 0) ? mOldVmNumber :
            getString(R.string.voicemail_number_not_set);
        mSubMenuVoicemailSettings.setSummary(summary);
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        mCurrentDialogId = id;
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        if ((id == VM_RESPONSE_ERROR) || (id == VM_NOCHANGE_ERROR) ||
            (id == FW_SET_RESPONSE_ERROR) || (id == FW_GET_RESPONSE_ERROR) ||
                (id == VOICEMAIL_DIALOG_CONFIRM)) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            int msgId;
            int titleId = R.string.error_updating_title;
            switch (id) {
                case VOICEMAIL_DIALOG_CONFIRM:
                    msgId = R.string.vm_changed;
                    titleId = R.string.voicemail;
                    b.setNegativeButton(R.string.close_dialog, this);
                    break;
                case VM_NOCHANGE_ERROR:
                    msgId = R.string.no_change;
                    titleId = R.string.voicemail;
                    b.setNegativeButton(R.string.close_dialog, this);
                    break;
                case VM_RESPONSE_ERROR:
                    msgId = R.string.vm_change_failed;
                    b.setPositiveButton(R.string.close_dialog, this);
                    break;
                case FW_SET_RESPONSE_ERROR:
                    msgId = R.string.fw_change_failed;
                    b.setPositiveButton(R.string.close_dialog, this);
                    break;
                case FW_GET_RESPONSE_ERROR:
                    msgId = R.string.fw_get_in_vm_failed;
                    b.setPositiveButton(R.string.alert_dialog_yes, this);
                    b.setNegativeButton(R.string.alert_dialog_no, this);
                    break;
                default:
                    msgId = R.string.exception_error;
                    b.setNeutralButton(R.string.close_dialog, this);
                    break;
            }
            b.setTitle(getText(titleId));
            String message = getText(msgId).toString();
            b.setMessage(message);
            b.setCancelable(false);
            AlertDialog dialog = b.create();
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            return dialog;
        } else if (id == VOICEMAIL_FWD_SAVING_DIALOG || id == VOICEMAIL_FWD_READING_DIALOG ||
                id == VOICEMAIL_REVERTING_DIALOG) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(getText(R.string.updating_title));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setMessage(getText(
                    id == VOICEMAIL_FWD_SAVING_DIALOG ? R.string.updating_settings :
                    (id == VOICEMAIL_REVERTING_DIALOG ? R.string.reverting_settings :
                    R.string.reading_settings)));
            return dialog;
        }
        return null;
    }
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which){
            case DialogInterface.BUTTON_NEUTRAL:
                if (DBG) log("Neutral button");
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if (DBG) log("Negative button");
                if (mCurrentDialogId == FW_GET_RESPONSE_ERROR) {
                    switchToPreviousVoicemailProvider();
                }
                break;
            case DialogInterface.BUTTON_POSITIVE:
                if (DBG) log("Positive button");
                if (mCurrentDialogId == FW_GET_RESPONSE_ERROR) {
                    saveVoiceMailAndForwardingNumberStage2();
                } else {
                    finish();
                }
                return;
            default:
        }
        if (getIntent().getAction().equals(ACTION_ADD_VOICEMAIL)) {
            finish();
        }
    }
    private void showVMDialog(int msgStatus) {
        switch (msgStatus) {
            case MSG_VM_EXCEPTION:
                showDialogIfForeground(VM_RESPONSE_ERROR);
                break;
            case MSG_FW_SET_EXCEPTION:
                showDialogIfForeground(FW_SET_RESPONSE_ERROR);
                break;
            case MSG_FW_GET_EXCEPTION:
                showDialogIfForeground(FW_GET_RESPONSE_ERROR);
                break;
            case MSG_VM_NOCHANGE:
                showDialogIfForeground(VM_NOCHANGE_ERROR);
                break;
            case MSG_VM_OK:
                showDialogIfForeground(VOICEMAIL_DIALOG_CONFIRM);
                break;
            case MSG_OK:
            default:
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (DBG) log("Creating activity");
        mPhone = PhoneFactory.getDefaultPhone();
        addPreferencesFromResource(R.xml.call_feature_setting);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        PreferenceScreen prefSet = getPreferenceScreen();
        mSubMenuVoicemailSettings = (EditPhoneNumberPreference)findPreference(BUTTON_VOICEMAIL_KEY);
        if (mSubMenuVoicemailSettings != null) {
            mSubMenuVoicemailSettings.setParentActivity(this, VOICEMAIL_PREF_ID, this);
            mSubMenuVoicemailSettings.setDialogOnClosedListener(this);
            mSubMenuVoicemailSettings.setDialogTitle(R.string.voicemail_settings_number_label);
        }
        mButtonDTMF = (ListPreference) findPreference(BUTTON_DTMF_KEY);
        mButtonAutoRetry = (CheckBoxPreference) findPreference(BUTTON_RETRY_KEY);
        mButtonHAC = (CheckBoxPreference) findPreference(BUTTON_HAC_KEY);
        mButtonTTY = (ListPreference) findPreference(BUTTON_TTY_KEY);
        mVoicemailProviders = (ListPreference) findPreference(BUTTON_VOICEMAIL_PROVIDER_KEY);
        if (mVoicemailProviders != null) {
            mVoicemailProviders.setOnPreferenceChangeListener(this);
            mVoicemailSettings = (PreferenceScreen)findPreference(BUTTON_VOICEMAIL_SETTING_KEY);
            initVoiceMailProviders();
        }
        if (mButtonDTMF != null) {
            if (getResources().getBoolean(R.bool.dtmf_type_enabled)) {
                mButtonDTMF.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonDTMF);
                mButtonDTMF = null;
            }
        }
        if (mButtonAutoRetry != null) {
            if (getResources().getBoolean(R.bool.auto_retry_enabled)) {
                mButtonAutoRetry.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonAutoRetry);
                mButtonAutoRetry = null;
            }
        }
        if (mButtonHAC != null) {
            if (getResources().getBoolean(R.bool.hac_enabled)) {
                mButtonHAC.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonHAC);
                mButtonHAC = null;
            }
        }
        if (mButtonTTY != null) {
            if (getResources().getBoolean(R.bool.tty_enabled)) {
                mButtonTTY.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mButtonTTY);
                mButtonTTY = null;
            }
        }
        if (!getResources().getBoolean(R.bool.world_phone)) {
            Preference options = prefSet.findPreference(BUTTON_CDMA_OPTIONS);
            if (options != null)
                prefSet.removePreference(options);
            options = prefSet.findPreference(BUTTON_GSM_UMTS_OPTIONS);
            if (options != null)
                prefSet.removePreference(options);
            int phoneType = mPhone.getPhoneType();
            if (phoneType == Phone.PHONE_TYPE_CDMA) {
                Preference fdnButton = prefSet.findPreference(BUTTON_FDN_KEY);
                if (fdnButton != null)
                    prefSet.removePreference(fdnButton);
                addPreferencesFromResource(R.xml.cdma_call_options);
            } else if (phoneType == Phone.PHONE_TYPE_GSM) {
                addPreferencesFromResource(R.xml.gsm_umts_call_options);
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
        }
        mContactListIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mContactListIntent.setType(android.provider.Contacts.Phones.CONTENT_ITEM_TYPE);
        if (icicle == null) {
            if (getIntent().getAction().equals(ACTION_ADD_VOICEMAIL) &&
                    mVoicemailProviders != null) {
                if (mVMProvidersData.size() > 1) {
                    simulatePreferenceClick(mVoicemailProviders);
                } else {
                    onPreferenceChange(mVoicemailProviders, DEFAULT_VM_PROVIDER_KEY);
                    mVoicemailProviders.setValue(DEFAULT_VM_PROVIDER_KEY);
                }
            }
        }
        updateVoiceNumberField();
        mVMProviderSettingsForced = false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mForeground = true;
        if (mButtonDTMF != null) {
            int dtmf = Settings.System.getInt(getContentResolver(),
                    Settings.System.DTMF_TONE_TYPE_WHEN_DIALING, DTMF_TONE_TYPE_NORMAL);
            mButtonDTMF.setValueIndex(dtmf);
        }
        if (mButtonAutoRetry != null) {
            int autoretry = Settings.System.getInt(getContentResolver(),
                    Settings.System.CALL_AUTO_RETRY, 0);
            mButtonAutoRetry.setChecked(autoretry != 0);
        }
        if (mButtonHAC != null) {
            int hac = Settings.System.getInt(getContentResolver(), Settings.System.HEARING_AID, 0);
            mButtonHAC.setChecked(hac != 0);
        }
        if (mButtonTTY != null) {
            int settingsTtyMode = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.PREFERRED_TTY_MODE,
                    Phone.TTY_MODE_OFF);
            mButtonTTY.setValue(Integer.toString(settingsTtyMode));
            updatePreferredTtyModeSummary(settingsTtyMode);
        }
    }
    private void handleTTYChange(Preference preference, Object objValue) {
        int buttonTtyMode;
        buttonTtyMode = Integer.valueOf((String) objValue).intValue();
        int settingsTtyMode = android.provider.Settings.Secure.getInt(
                getContentResolver(),
                android.provider.Settings.Secure.PREFERRED_TTY_MODE, preferredTtyMode);
        if (DBG) log("handleTTYChange: requesting set TTY mode enable (TTY) to" +
                Integer.toString(buttonTtyMode));
        if (buttonTtyMode != settingsTtyMode) {
            switch(buttonTtyMode) {
            case Phone.TTY_MODE_OFF:
            case Phone.TTY_MODE_FULL:
            case Phone.TTY_MODE_HCO:
            case Phone.TTY_MODE_VCO:
                android.provider.Settings.Secure.putInt(getContentResolver(),
                        android.provider.Settings.Secure.PREFERRED_TTY_MODE, buttonTtyMode);
                break;
            default:
                buttonTtyMode = Phone.TTY_MODE_OFF;
            }
            mButtonTTY.setValue(Integer.toString(buttonTtyMode));
            updatePreferredTtyModeSummary(buttonTtyMode);
            Intent ttyModeChanged = new Intent(TtyIntent.TTY_PREFERRED_MODE_CHANGE_ACTION);
            ttyModeChanged.putExtra(TtyIntent.TTY_PREFFERED_MODE, buttonTtyMode);
            sendBroadcast(ttyModeChanged);
        }
    }
    private void updatePreferredTtyModeSummary(int TtyMode) {
        String [] txts = getResources().getStringArray(R.array.tty_mode_entries);
        switch(TtyMode) {
            case Phone.TTY_MODE_OFF:
            case Phone.TTY_MODE_HCO:
            case Phone.TTY_MODE_VCO:
            case Phone.TTY_MODE_FULL:
                mButtonTTY.setSummary(txts[TtyMode]);
                break;
            default:
                mButtonTTY.setEnabled(false);
                mButtonTTY.setSummary(txts[Phone.TTY_MODE_OFF]);
        }
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    private void updateVMPreferenceWidgets(String currentProviderSetting) {
        final String key = currentProviderSetting;
        final VoiceMailProvider provider = mVMProvidersData.get(key);
        if (provider == null) {
            mVoicemailProviders.setSummary(getString(R.string.sum_voicemail_choose_provider));
            mVoicemailSettings.setSummary("");
            mVoicemailSettings.setEnabled(false);
            mVoicemailSettings.setIntent(null);
        } else {
            final String providerName = provider.name;
            mVoicemailProviders.setSummary(providerName);
            mVoicemailSettings.setSummary(getApplicationContext().getString(
                    R.string.voicemail_settings_for, providerName));
            mVoicemailSettings.setEnabled(true);
            mVoicemailSettings.setIntent(provider.intent);
        }
    }
    private void initVoiceMailProviders() {
        mPerProviderSavedVMNumbers =
            this.getApplicationContext().getSharedPreferences(
                VM_NUMBERS_SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String providerToIgnore = null;
        if (getIntent().getAction().equals(ACTION_ADD_VOICEMAIL)) {
            if (DBG) log("ACTION_ADD_VOICEMAIL");
            if (getIntent().hasExtra(IGNORE_PROVIDER_EXTRA)) {
                providerToIgnore = getIntent().getStringExtra(IGNORE_PROVIDER_EXTRA);
            }
            if (DBG) log("providerToIgnore=" + providerToIgnore);
            if (providerToIgnore != null) {
                deleteSettingsForVoicemailProvider(providerToIgnore);
            }
        }
        mVMProvidersData.clear();
        final String myCarrier = getString(R.string.voicemail_default);
        mVMProvidersData.put(DEFAULT_VM_PROVIDER_KEY, new VoiceMailProvider(myCarrier, null));
        PackageManager pm = getPackageManager();
        Intent intent = new Intent();
        intent.setAction(ACTION_CONFIGURE_VOICEMAIL);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        int len = resolveInfos.size() + 1; 
        for (int i = 0; i < resolveInfos.size(); i++) {
            final ResolveInfo ri= resolveInfos.get(i);
            final ActivityInfo currentActivityInfo = ri.activityInfo;
            final String key = makeKeyForActivity(currentActivityInfo);
            if (DBG) log("Loading " + key);
            if (key.equals(providerToIgnore)) {
                if (DBG) log("Ignoring " + key);
                len--;
                continue;
            }
            final String nameForDisplay = ri.loadLabel(pm).toString();
            Intent providerIntent = new Intent();
            providerIntent.setAction(ACTION_CONFIGURE_VOICEMAIL);
            providerIntent.setClassName(currentActivityInfo.packageName,
                    currentActivityInfo.name);
            mVMProvidersData.put(
                    key,
                    new VoiceMailProvider(nameForDisplay, providerIntent));
        }
        String [] entries = new String [len];
        String [] values = new String [len];
        entries[0] = myCarrier;
        values[0] = DEFAULT_VM_PROVIDER_KEY;
        int entryIdx = 1;
        for (int i = 0; i < resolveInfos.size(); i++) {
            final String key = makeKeyForActivity(resolveInfos.get(i).activityInfo);
            if (!mVMProvidersData.containsKey(key)) {
                continue;
            }
            entries[entryIdx] = mVMProvidersData.get(key).name;
            values[entryIdx] = key;
            entryIdx++;
        }
        mVoicemailProviders.setEntries(entries);
        mVoicemailProviders.setEntryValues(values);
        mPreviousVMProviderKey = getCurrentVoicemailProviderKey();
        updateVMPreferenceWidgets(mPreviousVMProviderKey);
    }
    private String makeKeyForActivity(ActivityInfo ai) {
        return ai.name;
    }
    private void simulatePreferenceClick(Preference preference) {
        final ListAdapter adapter = getPreferenceScreen().getRootAdapter();
        for (int idx = 0; idx < adapter.getCount(); idx++) {
            if (adapter.getItem(idx) == preference) {
                getPreferenceScreen().onItemClick(this.getListView(),
                        null, idx, adapter.getItemId(idx));
                break;
            }
        }
    }
    private void maybeSaveSettingsForVoicemailProvider(String key,
            VoiceMailProviderSettings newSettings) {
        if (mVoicemailProviders == null) {
            return;
        }
        final VoiceMailProviderSettings curSettings = loadSettingsForVoiceMailProvider(key);
        if (newSettings.equals(curSettings)) {
            if (DBG) log("Not saving setting for " + key + " since they have not changed");
            return;
        }
        if (DBG) log("Saving settings for " + key + ": " + newSettings.toString());
        Editor editor = mPerProviderSavedVMNumbers.edit();
        editor.putString(key + VM_NUMBER_TAG,newSettings.voicemailNumber);
        String fwdKey = key + FWD_SETTINGS_TAG;
        CallForwardInfo[] s = newSettings.forwardingSettings;
        if (s != FWD_SETTINGS_DONT_TOUCH) {
            editor.putInt(fwdKey + FWD_SETTINGS_LENGTH_TAG, s.length);
            for (int i = 0; i < s.length; i++) {
                final String settingKey = fwdKey + FWD_SETTING_TAG + String.valueOf(i);
                final CallForwardInfo fi = s[i];
                editor.putInt(settingKey + FWD_SETTING_STATUS, fi.status);
                editor.putInt(settingKey + FWD_SETTING_REASON, fi.reason);
                editor.putString(settingKey + FWD_SETTING_NUMBER, fi.number);
                editor.putInt(settingKey + FWD_SETTING_TIME, fi.timeSeconds);
            }
        } else {
            editor.putInt(fwdKey + FWD_SETTINGS_LENGTH_TAG, 0);
        }
        editor.commit();
    }
    private VoiceMailProviderSettings loadSettingsForVoiceMailProvider(String key) {
        final String vmNumberSetting = mPerProviderSavedVMNumbers.getString(key + VM_NUMBER_TAG,
                null);
        if (vmNumberSetting == null) {
            if (DBG) log("Settings for " + key + " not found");
            return null;
        }
        CallForwardInfo[] cfi = FWD_SETTINGS_DONT_TOUCH;
        String fwdKey = key + FWD_SETTINGS_TAG;
        final int fwdLen = mPerProviderSavedVMNumbers.getInt(fwdKey + FWD_SETTINGS_LENGTH_TAG, 0);
        if (fwdLen > 0) {
            cfi = new CallForwardInfo[fwdLen];
            for (int i = 0; i < cfi.length; i++) {
                final String settingKey = fwdKey + FWD_SETTING_TAG + String.valueOf(i);
                cfi[i] = new CallForwardInfo();
                cfi[i].status = mPerProviderSavedVMNumbers.getInt(
                        settingKey + FWD_SETTING_STATUS, 0);
                cfi[i].reason = mPerProviderSavedVMNumbers.getInt(
                        settingKey + FWD_SETTING_REASON,
                        CommandsInterface.CF_REASON_ALL_CONDITIONAL);
                cfi[i].serviceClass = CommandsInterface.SERVICE_CLASS_VOICE;
                cfi[i].toa = PhoneNumberUtils.TOA_International;
                cfi[i].number = mPerProviderSavedVMNumbers.getString(
                        settingKey + FWD_SETTING_NUMBER, "");
                cfi[i].timeSeconds = mPerProviderSavedVMNumbers.getInt(
                        settingKey + FWD_SETTING_TIME, 20);
            }
        }
        VoiceMailProviderSettings settings =  new VoiceMailProviderSettings(vmNumberSetting, cfi);
        if (DBG) log("Loaded settings for " + key + ": " + settings.toString());
        return settings;
    }
    private void deleteSettingsForVoicemailProvider(String key) {
        if (DBG) log("Deleting settings for" + key);
        if (mVoicemailProviders == null) {
            return;
        }
        mPerProviderSavedVMNumbers.edit()
            .putString(key + VM_NUMBER_TAG, null)
            .putInt(key + FWD_SETTINGS_TAG + FWD_SETTINGS_LENGTH_TAG, 0)
            .commit();
    }
    private String getCurrentVoicemailProviderKey() {
        final String key = mVoicemailProviders.getValue();
        return (key != null) ? key : DEFAULT_VM_PROVIDER_KEY;
    }
}
