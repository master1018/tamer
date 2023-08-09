public class VolumePreference extends SeekBarPreference implements 
        PreferenceManager.OnActivityStopListener, View.OnKeyListener {
    private static final String TAG = "VolumePreference";
    private int mStreamType;
    private SeekBarVolumizer mSeekBarVolumizer;
    public VolumePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.VolumePreference, 0, 0);
        mStreamType = a.getInt(android.R.styleable.VolumePreference_streamType, 0);
        a.recycle();        
    }
    public void setStreamType(int streamType) {
        mStreamType = streamType;
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        final SeekBar seekBar = (SeekBar) view.findViewById(com.android.internal.R.id.seekbar);
        mSeekBarVolumizer = new SeekBarVolumizer(getContext(), seekBar, mStreamType);
        getPreferenceManager().registerOnActivityStopListener(this);
        view.setOnKeyListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (mSeekBarVolumizer == null) return true;
        boolean isdown = (event.getAction() == KeyEvent.ACTION_DOWN);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isdown) {
                    mSeekBarVolumizer.changeVolumeBy(-1);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isdown) {
                    mSeekBarVolumizer.changeVolumeBy(1);
                }
                return true;
            default:
                return false;
        }
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult && mSeekBarVolumizer != null) {
            mSeekBarVolumizer.revertVolume();
        }
        cleanup();
    }
    public void onActivityStop() {
        cleanup();
    }
    private void cleanup() {
       getPreferenceManager().unregisterOnActivityStopListener(this);
       if (mSeekBarVolumizer != null) {
           Dialog dialog = getDialog();
           if (dialog != null && dialog.isShowing()) {
               View view = dialog.getWindow().getDecorView()
                       .findViewById(com.android.internal.R.id.seekbar);
               if (view != null) view.setOnKeyListener(null);
               mSeekBarVolumizer.revertVolume();
           }
           mSeekBarVolumizer.stop();
           mSeekBarVolumizer = null;
       }
    }
    protected void onSampleStarting(SeekBarVolumizer volumizer) {
        if (mSeekBarVolumizer != null && volumizer != mSeekBarVolumizer) {
            mSeekBarVolumizer.stopSample();
        }
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.onSaveInstanceState(myState.getVolumeStore());
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
        if (mSeekBarVolumizer != null) {
            mSeekBarVolumizer.onRestoreInstanceState(myState.getVolumeStore());
        }
    }
    public static class VolumeStore {
        public int volume = -1;
        public int originalVolume = -1;
    }
    private static class SavedState extends BaseSavedState {
        VolumeStore mVolumeStore = new VolumeStore();
        public SavedState(Parcel source) {
            super(source);
            mVolumeStore.volume = source.readInt();
            mVolumeStore.originalVolume = source.readInt();
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mVolumeStore.volume);
            dest.writeInt(mVolumeStore.originalVolume);
        }
        VolumeStore getVolumeStore() {
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
    public class SeekBarVolumizer implements OnSeekBarChangeListener, Runnable {
        private Context mContext;
        private Handler mHandler = new Handler();
        private AudioManager mAudioManager;
        private int mStreamType;
        private int mOriginalStreamVolume; 
        private Ringtone mRingtone;
        private int mLastProgress = -1;
        private SeekBar mSeekBar;
        private ContentObserver mVolumeObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (mSeekBar != null) {
                    int volume = System.getInt(mContext.getContentResolver(),
                            System.VOLUME_SETTINGS[mStreamType], -1);
                    if (volume >= 0) {
                        mSeekBar.setProgress(volume);
                    }
                }
            }
        };
        public SeekBarVolumizer(Context context, SeekBar seekBar, int streamType) {
            mContext = context;
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            mStreamType = streamType;
            mSeekBar = seekBar;
            initSeekBar(seekBar);
        }
        private void initSeekBar(SeekBar seekBar) {
            seekBar.setMax(mAudioManager.getStreamMaxVolume(mStreamType));
            mOriginalStreamVolume = mAudioManager.getStreamVolume(mStreamType);
            seekBar.setProgress(mOriginalStreamVolume);
            seekBar.setOnSeekBarChangeListener(this);
            mContext.getContentResolver().registerContentObserver(
                    System.getUriFor(System.VOLUME_SETTINGS[mStreamType]),
                    false, mVolumeObserver);
            Uri defaultUri = null;
            if (mStreamType == AudioManager.STREAM_RING) {
                defaultUri = Settings.System.DEFAULT_RINGTONE_URI;
            } else if (mStreamType == AudioManager.STREAM_NOTIFICATION) {
                defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            } else {
                defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }
            mRingtone = RingtoneManager.getRingtone(mContext, defaultUri);
            if (mRingtone != null) {
                mRingtone.setStreamType(mStreamType);
            }
        }
        public void stop() {
            stopSample();
            mContext.getContentResolver().unregisterContentObserver(mVolumeObserver);
            mSeekBar.setOnSeekBarChangeListener(null);
        }
        public void revertVolume() {
            mAudioManager.setStreamVolume(mStreamType, mOriginalStreamVolume, 0);
        }
        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromTouch) {
            if (!fromTouch) {
                return;
            }
            postSetVolume(progress);
        }
        void postSetVolume(int progress) {
            mLastProgress = progress;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (mRingtone != null && !mRingtone.isPlaying()) {
                sample();
            }
        }
        public void run() {
            mAudioManager.setStreamVolume(mStreamType, mLastProgress, 0);
        }
        private void sample() {
            onSampleStarting(this);
            mRingtone.play();
        }
        public void stopSample() {
            if (mRingtone != null) {
                mRingtone.stop();
            }
        }
        public SeekBar getSeekBar() {
            return mSeekBar;
        }
        public void changeVolumeBy(int amount) {
            mSeekBar.incrementProgressBy(amount);
            if (mRingtone != null && !mRingtone.isPlaying()) {
                sample();
            }
            postSetVolume(mSeekBar.getProgress());
        }
        public void onSaveInstanceState(VolumeStore volumeStore) {
            if (mLastProgress >= 0) {
                volumeStore.volume = mLastProgress;
                volumeStore.originalVolume = mOriginalStreamVolume;
            }
        }
        public void onRestoreInstanceState(VolumeStore volumeStore) {
            if (volumeStore.volume != -1) {
                mOriginalStreamVolume = volumeStore.originalVolume;
                mLastProgress = volumeStore.volume;
                postSetVolume(mLastProgress);
            }
        }
    }
}
