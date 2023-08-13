public class RingerVolumePreference extends VolumePreference implements
        CheckBox.OnCheckedChangeListener {
    private static final String TAG = "RingerVolumePreference";
    private CheckBox mNotificationsUseRingVolumeCheckbox;
    private SeekBarVolumizer [] mSeekBarVolumizer;
    private static final int[] SEEKBAR_ID = new int[] {
        R.id.notification_volume_seekbar,
        R.id.media_volume_seekbar,
        R.id.alarm_volume_seekbar
    };
    private static final int[] SEEKBAR_TYPE = new int[] {
        AudioManager.STREAM_NOTIFICATION,
        AudioManager.STREAM_MUSIC,
        AudioManager.STREAM_ALARM
    };
    private TextView mNotificationVolumeTitle;
    public RingerVolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStreamType(AudioManager.STREAM_RING);
        setDialogLayoutResource(R.layout.preference_dialog_ringervolume);
        setDialogIcon(R.drawable.ic_settings_sound);
        mSeekBarVolumizer = new SeekBarVolumizer[SEEKBAR_ID.length];
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            mSeekBarVolumizer[i] = new SeekBarVolumizer(getContext(), seekBar,
                SEEKBAR_TYPE[i]);
        }
        mNotificationVolumeTitle = (TextView) view.findViewById(R.id.notification_volume_title);
        mNotificationsUseRingVolumeCheckbox =
                (CheckBox) view.findViewById(R.id.same_notification_volume);
        mNotificationsUseRingVolumeCheckbox.setOnCheckedChangeListener(this);
        mNotificationsUseRingVolumeCheckbox.setChecked(Settings.System.getInt(
                getContext().getContentResolver(),
                Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 1) == 1);
        setNotificationVolumeVisibility(!mNotificationsUseRingVolumeCheckbox.isChecked());
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult) {
            for (SeekBarVolumizer vol : mSeekBarVolumizer) {
                if (vol != null) vol.revertVolume();
            }
        }        
        cleanup();
    }
    @Override
    public void onActivityStop() {
        super.onActivityStop();
        cleanup();
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setNotificationVolumeVisibility(!isChecked);
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.NOTIFICATIONS_USE_RING_VOLUME, isChecked ? 1 : 0);
        if (isChecked) {
            AudioManager audioManager = (AudioManager) getContext()
                    .getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                    audioManager.getStreamVolume(AudioManager.STREAM_RING), 0);
        }
    }
    @Override
    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        super.onSampleStarting(volumizer);
        for (SeekBarVolumizer vol : mSeekBarVolumizer) {
            if (vol != null && vol != volumizer) vol.stopSample();
        }
    }
    private void setNotificationVolumeVisibility(boolean visible) {
        if (mSeekBarVolumizer[0] != null) {
            mSeekBarVolumizer[0].getSeekBar().setVisibility(
                    visible ? View.VISIBLE : View.GONE);
        }
        mNotificationVolumeTitle.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    private void cleanup() {
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            if (mSeekBarVolumizer[i] != null) {
                Dialog dialog = getDialog();
                if (dialog != null && dialog.isShowing()) {
                    mSeekBarVolumizer[i].revertVolume();
                }
                mSeekBarVolumizer[i].stop();
                mSeekBarVolumizer[i] = null;
            }
        }
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        VolumeStore[] volumeStore = myState.getVolumeStore(SEEKBAR_ID.length);
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBarVolumizer vol = mSeekBarVolumizer[i];
            if (vol != null) {
                vol.onSaveInstanceState(volumeStore[i]);
            }
        }
        return myState;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        VolumeStore[] volumeStore = myState.getVolumeStore(SEEKBAR_ID.length);
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBarVolumizer vol = mSeekBarVolumizer[i];
            if (vol != null) {
                vol.onRestoreInstanceState(volumeStore[i]);
            }
        }
    }
    private static class SavedState extends BaseSavedState {
        VolumeStore [] mVolumeStore;
        public SavedState(Parcel source) {
            super(source);
            mVolumeStore = new VolumeStore[SEEKBAR_ID.length];
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                mVolumeStore[i] = new VolumeStore();
                mVolumeStore[i].volume = source.readInt();
                mVolumeStore[i].originalVolume = source.readInt();
            }
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            for (int i = 0; i < SEEKBAR_ID.length; i++) {
                dest.writeInt(mVolumeStore[i].volume);
                dest.writeInt(mVolumeStore[i].originalVolume);
            }
        }
        VolumeStore[] getVolumeStore(int count) {
            if (mVolumeStore == null || mVolumeStore.length != count) {
                mVolumeStore = new VolumeStore[count];
                for (int i = 0; i < count; i++) {
                    mVolumeStore[i] = new VolumeStore();
                }
            }
            return mVolumeStore;
        }
        public SavedState(Parcelable superState) {
            super(superState);
        }
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
