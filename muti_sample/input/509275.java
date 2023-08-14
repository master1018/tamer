class GlobalActions implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener  {
    private static final String TAG = "GlobalActions";
    private StatusBarManager mStatusBar;
    private final Context mContext;
    private final AudioManager mAudioManager;
    private ArrayList<Action> mItems;
    private AlertDialog mDialog;
    private ToggleAction mSilentModeToggle;
    private ToggleAction mAirplaneModeOn;
    private MyAdapter mAdapter;
    private boolean mKeyguardShowing = false;
    private boolean mDeviceProvisioned = false;
    private ToggleAction.State mAirplaneState = ToggleAction.State.Off;
    private boolean mIsWaitingForEcmExit = false;
    public GlobalActions(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        context.registerReceiver(mBroadcastReceiver, filter);
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }
    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        mKeyguardShowing = keyguardShowing;
        mDeviceProvisioned = isDeviceProvisioned;
        if (mDialog == null) {
            mStatusBar = (StatusBarManager)mContext.getSystemService(Context.STATUS_BAR_SERVICE);
            mDialog = createDialog();
        }
        prepareDialog();
        mStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
        mDialog.show();
    }
    private AlertDialog createDialog() {
        mSilentModeToggle = new ToggleAction(
                R.drawable.ic_lock_silent_mode,
                R.drawable.ic_lock_silent_mode_off,
                R.string.global_action_toggle_silent_mode,
                R.string.global_action_silent_mode_on_status,
                R.string.global_action_silent_mode_off_status) {
            void willCreate() {
                mEnabledIconResId = (Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.VIBRATE_IN_SILENT, 1) == 1)
                    ? R.drawable.ic_lock_silent_mode_vibrate
                    : R.drawable.ic_lock_silent_mode;
            }
            void onToggle(boolean on) {
                if (on) {
                    mAudioManager.setRingerMode((Settings.System.getInt(mContext.getContentResolver(),
                        Settings.System.VIBRATE_IN_SILENT, 1) == 1)
                        ? AudioManager.RINGER_MODE_VIBRATE
                        : AudioManager.RINGER_MODE_SILENT);
                } else {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
            }
            public boolean showDuringKeyguard() {
                return true;
            }
            public boolean showBeforeProvisioning() {
                return false;
            }
        };
        mAirplaneModeOn = new ToggleAction(
                R.drawable.ic_lock_airplane_mode,
                R.drawable.ic_lock_airplane_mode_off,
                R.string.global_actions_toggle_airplane_mode,
                R.string.global_actions_airplane_mode_on_status,
                R.string.global_actions_airplane_mode_off_status) {
            void onToggle(boolean on) {
                if (Boolean.parseBoolean(
                        SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
                    mIsWaitingForEcmExit = true;
                    Intent ecmDialogIntent =
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null);
                    ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(ecmDialogIntent);
                } else {
                    changeAirplaneModeSystemSetting(on);
                }
            }
            @Override
            protected void changeStateFromPress(boolean buttonOn) {
                if (!(Boolean.parseBoolean(
                        SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE)))) {
                    mState = buttonOn ? State.TurningOn : State.TurningOff;
                    mAirplaneState = mState;
                }
            }
            public boolean showDuringKeyguard() {
                return true;
            }
            public boolean showBeforeProvisioning() {
                return false;
            }
        };
        mItems = Lists.newArrayList(
                mSilentModeToggle,
                mAirplaneModeOn,
                new SinglePressAction(
                        com.android.internal.R.drawable.ic_lock_power_off,
                        R.string.global_action_power_off) {
                    public void onPress() {
                        ShutdownThread.shutdown(mContext, true);
                    }
                    public boolean showDuringKeyguard() {
                        return true;
                    }
                    public boolean showBeforeProvisioning() {
                        return true;
                    }
                });
        mAdapter = new MyAdapter();
        final AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
        ab.setAdapter(mAdapter, this)
                .setInverseBackgroundForced(true)
                .setTitle(R.string.global_actions);
        final AlertDialog dialog = ab.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        dialog.setOnDismissListener(this);
        return dialog;
    }
    private void prepareDialog() {
        final boolean silentModeOn =
                mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
        mSilentModeToggle.updateState(
                silentModeOn ? ToggleAction.State.On : ToggleAction.State.Off);
        mAirplaneModeOn.updateState(mAirplaneState);
        mAdapter.notifyDataSetChanged();
        if (mKeyguardShowing) {
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        } else {
            mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        }
    }
    public void onDismiss(DialogInterface dialog) {
        mStatusBar.disable(StatusBarManager.DISABLE_NONE);
    }
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        mAdapter.getItem(which).onPress();
    }
    private class MyAdapter extends BaseAdapter {
        public int getCount() {
            int count = 0;
            for (int i = 0; i < mItems.size(); i++) {
                final Action action = mItems.get(i);
                if (mKeyguardShowing && !action.showDuringKeyguard()) {
                    continue;
                }
                if (!mDeviceProvisioned && !action.showBeforeProvisioning()) {
                    continue;
                }
                count++;
            }
            return count;
        }
        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
        public Action getItem(int position) {
            int filteredPos = 0;
            for (int i = 0; i < mItems.size(); i++) {
                final Action action = mItems.get(i);
                if (mKeyguardShowing && !action.showDuringKeyguard()) {
                    continue;
                }
                if (!mDeviceProvisioned && !action.showBeforeProvisioning()) {
                    continue;
                }
                if (filteredPos == position) {
                    return action;
                }
                filteredPos++;
            }
            throw new IllegalArgumentException("position " + position + " out of "
                    + "range of showable actions, filtered count = "
                    + "= " + getCount() + ", keyguardshowing=" + mKeyguardShowing
                    + ", provisioned=" + mDeviceProvisioned);
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            Action action = getItem(position);
            return action.create(mContext, convertView, parent, LayoutInflater.from(mContext));
        }
    }
    private interface Action {
        View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater);
        void onPress();
        boolean showDuringKeyguard();
        boolean showBeforeProvisioning();
        boolean isEnabled();
    }
    private static abstract class SinglePressAction implements Action {
        private final int mIconResId;
        private final int mMessageResId;
        protected SinglePressAction(int iconResId, int messageResId) {
            mIconResId = iconResId;
            mMessageResId = messageResId;
        }
        public boolean isEnabled() {
            return true;
        }
        abstract public void onPress();
        public View create(
                Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = (convertView != null) ?
                    convertView :
                    inflater.inflate(R.layout.global_actions_item, parent, false);
            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            TextView messageView = (TextView) v.findViewById(R.id.message);
            v.findViewById(R.id.status).setVisibility(View.GONE);
            icon.setImageDrawable(context.getResources().getDrawable(mIconResId));
            messageView.setText(mMessageResId);
            return v;
        }
    }
    private static abstract class ToggleAction implements Action {
        enum State {
            Off(false),
            TurningOn(true),
            TurningOff(true),
            On(false);
            private final boolean inTransition;
            State(boolean intermediate) {
                inTransition = intermediate;
            }
            public boolean inTransition() {
                return inTransition;
            }
        }
        protected State mState = State.Off;
        protected int mEnabledIconResId;
        protected int mDisabledIconResid;
        protected int mMessageResId;
        protected int mEnabledStatusMessageResId;
        protected int mDisabledStatusMessageResId;
        public ToggleAction(int enabledIconResId,
                int disabledIconResid,
                int essage,
                int enabledStatusMessageResId,
                int disabledStatusMessageResId) {
            mEnabledIconResId = enabledIconResId;
            mDisabledIconResid = disabledIconResid;
            mMessageResId = essage;
            mEnabledStatusMessageResId = enabledStatusMessageResId;
            mDisabledStatusMessageResId = disabledStatusMessageResId;
        }
        void willCreate() {
        }
        public View create(Context context, View convertView, ViewGroup parent,
                LayoutInflater inflater) {
            willCreate();
            View v = (convertView != null) ?
                    convertView :
                    inflater.inflate(R
                            .layout.global_actions_item, parent, false);
            ImageView icon = (ImageView) v.findViewById(R.id.icon);
            TextView messageView = (TextView) v.findViewById(R.id.message);
            TextView statusView = (TextView) v.findViewById(R.id.status);
            messageView.setText(mMessageResId);
            boolean on = ((mState == State.On) || (mState == State.TurningOn));
            icon.setImageDrawable(context.getResources().getDrawable(
                    (on ? mEnabledIconResId : mDisabledIconResid)));
            statusView.setText(on ? mEnabledStatusMessageResId : mDisabledStatusMessageResId);
            statusView.setVisibility(View.VISIBLE);
            final boolean enabled = isEnabled();
            messageView.setEnabled(enabled);
            statusView.setEnabled(enabled);
            icon.setEnabled(enabled);
            v.setEnabled(enabled);
            return v;
        }
        public final void onPress() {
            if (mState.inTransition()) {
                Log.w(TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            final boolean nowOn = !(mState == State.On);
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }
        public boolean isEnabled() {
            return !mState.inTransition();
        }
        protected void changeStateFromPress(boolean buttonOn) {
            mState = buttonOn ? State.On : State.Off;
        }
        abstract void onToggle(boolean on);
        public void updateState(State state) {
            mState = state;
        }
    }
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                    || Intent.ACTION_SCREEN_OFF.equals(action)) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(reason)) {
                    mHandler.sendEmptyMessage(MESSAGE_DISMISS);
                }
            } else if (TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED.equals(action)) {
                if (!(intent.getBooleanExtra("PHONE_IN_ECM_STATE", false)) &&
                        mIsWaitingForEcmExit) {
                    mIsWaitingForEcmExit = false;
                    changeAirplaneModeSystemSetting(true);
                }
            }
        }
    };
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            final boolean inAirplaneMode = serviceState.getState() == ServiceState.STATE_POWER_OFF;
            mAirplaneState = inAirplaneMode ? ToggleAction.State.On : ToggleAction.State.Off;
            mAirplaneModeOn.updateState(mAirplaneState);
            mAdapter.notifyDataSetChanged();
        }
    };
    private static final int MESSAGE_DISMISS = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DISMISS) {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        }
    };
    private void changeAirplaneModeSystemSetting(boolean on) {
        Settings.System.putInt(
                mContext.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON,
                on ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("state", on);
        mContext.sendBroadcast(intent);
    }
}
