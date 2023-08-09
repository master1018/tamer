public class VolumePanel extends Handler
{
    private static final String TAG = "VolumePanel";
    private static boolean LOGD = false;
    public static final int PLAY_SOUND_DELAY = 300;
    public static final int VIBRATE_DELAY = 300;
    private static final int VIBRATE_DURATION = 300;
    private static final int BEEP_DURATION = 150;
    private static final int MAX_VOLUME = 100;
    private static final int FREE_DELAY = 10000;
    private static final int MSG_VOLUME_CHANGED = 0;
    private static final int MSG_FREE_RESOURCES = 1;
    private static final int MSG_PLAY_SOUND = 2;
    private static final int MSG_STOP_SOUNDS = 3;
    private static final int MSG_VIBRATE = 4;
    private static final int RINGTONE_VOLUME_TEXT = com.android.internal.R.string.volume_ringtone;
    private static final int MUSIC_VOLUME_TEXT = com.android.internal.R.string.volume_music;
    private static final int INCALL_VOLUME_TEXT = com.android.internal.R.string.volume_call;
    private static final int ALARM_VOLUME_TEXT = com.android.internal.R.string.volume_alarm;
    private static final int UNKNOWN_VOLUME_TEXT = com.android.internal.R.string.volume_unknown;
    private static final int NOTIFICATION_VOLUME_TEXT =
            com.android.internal.R.string.volume_notification;
    private static final int BLUETOOTH_INCALL_VOLUME_TEXT =
            com.android.internal.R.string.volume_bluetooth_call;
    protected Context mContext;
    private AudioManager mAudioManager;
    protected AudioService mAudioService;
    private boolean mRingIsSilent;
    private final Toast mToast;
    private final View mView;
    private final TextView mMessage;
    private final TextView mAdditionalMessage;
    private final ImageView mSmallStreamIcon;
    private final ImageView mLargeStreamIcon;
    private final ProgressBar mLevel;
    private ToneGenerator mToneGenerators[];
    private Vibrator mVibrator;
    public VolumePanel(Context context, AudioService volumeService) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAudioService = volumeService;
        mToast = new Toast(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mView = inflater.inflate(com.android.internal.R.layout.volume_adjust, null);
        mMessage = (TextView) view.findViewById(com.android.internal.R.id.message);
        mAdditionalMessage =
                (TextView) view.findViewById(com.android.internal.R.id.additional_message);
        mSmallStreamIcon = (ImageView) view.findViewById(com.android.internal.R.id.other_stream_icon);
        mLargeStreamIcon = (ImageView) view.findViewById(com.android.internal.R.id.ringer_stream_icon);
        mLevel = (ProgressBar) view.findViewById(com.android.internal.R.id.level);
        mToneGenerators = new ToneGenerator[AudioSystem.getNumStreamTypes()];
        mVibrator = new Vibrator();
    }
    public void postVolumeChanged(int streamType, int flags) {
        if (hasMessages(MSG_VOLUME_CHANGED)) return;
        removeMessages(MSG_FREE_RESOURCES);
        obtainMessage(MSG_VOLUME_CHANGED, streamType, flags).sendToTarget();
    }
    protected void onVolumeChanged(int streamType, int flags) {
        if (LOGD) Log.d(TAG, "onVolumeChanged(streamType: " + streamType + ", flags: " + flags + ")");
        if ((flags & AudioManager.FLAG_SHOW_UI) != 0) {
            onShowVolumeChanged(streamType, flags);
        }
        if ((flags & AudioManager.FLAG_PLAY_SOUND) != 0 && ! mRingIsSilent) {
            removeMessages(MSG_PLAY_SOUND);
            sendMessageDelayed(obtainMessage(MSG_PLAY_SOUND, streamType, flags), PLAY_SOUND_DELAY);
        }
        if ((flags & AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE) != 0) {
            removeMessages(MSG_PLAY_SOUND);
            removeMessages(MSG_VIBRATE);
            onStopSounds();
        }
        removeMessages(MSG_FREE_RESOURCES);
        sendMessageDelayed(obtainMessage(MSG_FREE_RESOURCES), FREE_DELAY);
    }
    protected void onShowVolumeChanged(int streamType, int flags) {
        int index = mAudioService.getStreamVolume(streamType);
        int message = UNKNOWN_VOLUME_TEXT;
        int additionalMessage = 0;
        mRingIsSilent = false;
        if (LOGD) {
            Log.d(TAG, "onShowVolumeChanged(streamType: " + streamType
                    + ", flags: " + flags + "), index: " + index);
        }
        int max = mAudioService.getStreamMaxVolume(streamType);
        switch (streamType) {
            case AudioManager.STREAM_RING: {
                setRingerIcon();
                message = RINGTONE_VOLUME_TEXT;
                Uri ringuri = RingtoneManager.getActualDefaultRingtoneUri(
                        mContext, RingtoneManager.TYPE_RINGTONE);
                if (ringuri == null) {
                    additionalMessage =
                        com.android.internal.R.string.volume_music_hint_silent_ringtone_selected;
                    mRingIsSilent = true;
                }
                break;
            }
            case AudioManager.STREAM_MUSIC: {
                message = MUSIC_VOLUME_TEXT;
                if (mAudioManager.isBluetoothA2dpOn()) {
                    additionalMessage =
                        com.android.internal.R.string.volume_music_hint_playing_through_bluetooth;
                    setLargeIcon(com.android.internal.R.drawable.ic_volume_bluetooth_ad2p);
                } else {
                    setSmallIcon(index);
                }
                break;
            }
            case AudioManager.STREAM_VOICE_CALL: {
                index++;
                max++;
                message = INCALL_VOLUME_TEXT;
                setSmallIcon(index);
                break;
            }
            case AudioManager.STREAM_ALARM: {
                message = ALARM_VOLUME_TEXT;
                setSmallIcon(index);
                break;
            }
            case AudioManager.STREAM_NOTIFICATION: {
                message = NOTIFICATION_VOLUME_TEXT;
                setSmallIcon(index);
                Uri ringuri = RingtoneManager.getActualDefaultRingtoneUri(
                        mContext, RingtoneManager.TYPE_NOTIFICATION);
                if (ringuri == null) {
                    additionalMessage =
                        com.android.internal.R.string.volume_music_hint_silent_ringtone_selected;
                    mRingIsSilent = true;
                }
                break;
            }
            case AudioManager.STREAM_BLUETOOTH_SCO: {
                index++;
                max++;
                message = BLUETOOTH_INCALL_VOLUME_TEXT;
                setLargeIcon(com.android.internal.R.drawable.ic_volume_bluetooth_in_call);
                break;
            }
        }
        String messageString = Resources.getSystem().getString(message);
        if (!mMessage.getText().equals(messageString)) {
            mMessage.setText(messageString);
        }
        if (additionalMessage == 0) {
            mAdditionalMessage.setVisibility(View.GONE);
        } else {
            mAdditionalMessage.setVisibility(View.VISIBLE);
            mAdditionalMessage.setText(Resources.getSystem().getString(additionalMessage));
        }
        if (max != mLevel.getMax()) {
            mLevel.setMax(max);
        }
        mLevel.setProgress(index);
        mToast.setView(mView);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.TOP, 0, 0);
        mToast.show();
        if ((flags & AudioManager.FLAG_VIBRATE) != 0 &&
                mAudioService.isStreamAffectedByRingerMode(streamType) &&
                mAudioService.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE &&
                mAudioService.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER)) {
            sendMessageDelayed(obtainMessage(MSG_VIBRATE), VIBRATE_DELAY);
        }
    }
    protected void onPlaySound(int streamType, int flags) {
        if (hasMessages(MSG_STOP_SOUNDS)) {
            removeMessages(MSG_STOP_SOUNDS);
            onStopSounds();
        }
        synchronized (this) {
            ToneGenerator toneGen = getOrCreateToneGenerator(streamType);
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP);
        }
        sendMessageDelayed(obtainMessage(MSG_STOP_SOUNDS), BEEP_DURATION);
    }
    protected void onStopSounds() {
        synchronized (this) {
            int numStreamTypes = AudioSystem.getNumStreamTypes();
            for (int i = numStreamTypes - 1; i >= 0; i--) {
                ToneGenerator toneGen = mToneGenerators[i];
                if (toneGen != null) {
                    toneGen.stopTone();
                }
            }
        }
    }
    protected void onVibrate() {
        if (mAudioService.getRingerMode() != AudioManager.RINGER_MODE_VIBRATE) {
            return;
        }
        mVibrator.vibrate(VIBRATE_DURATION);
    }
    private ToneGenerator getOrCreateToneGenerator(int streamType) {
        synchronized (this) {
            if (mToneGenerators[streamType] == null) {
                return mToneGenerators[streamType] = new ToneGenerator(streamType, MAX_VOLUME);
            } else {
                return mToneGenerators[streamType];
            }
        }
    }
    private void setSmallIcon(int index) {
        mLargeStreamIcon.setVisibility(View.GONE);
        mSmallStreamIcon.setVisibility(View.VISIBLE);
        mSmallStreamIcon.setImageResource(index == 0
                ? com.android.internal.R.drawable.ic_volume_off_small
                : com.android.internal.R.drawable.ic_volume_small);
    }
    private void setLargeIcon(int resId) {
        mSmallStreamIcon.setVisibility(View.GONE);
        mLargeStreamIcon.setVisibility(View.VISIBLE);
        mLargeStreamIcon.setImageResource(resId);
    }
    private void setRingerIcon() {
        mSmallStreamIcon.setVisibility(View.GONE);
        mLargeStreamIcon.setVisibility(View.VISIBLE);
        int ringerMode = mAudioService.getRingerMode();
        int icon;
        if (LOGD) Log.d(TAG, "setRingerIcon(), ringerMode: " + ringerMode);
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            icon = com.android.internal.R.drawable.ic_volume_off;
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            icon = com.android.internal.R.drawable.ic_vibrate;
        } else {
            icon = com.android.internal.R.drawable.ic_volume;
        }
        mLargeStreamIcon.setImageResource(icon);
    }
    protected void onFreeResources() {
        mSmallStreamIcon.setImageDrawable(null);
        mLargeStreamIcon.setImageDrawable(null);
        synchronized (this) {
            for (int i = mToneGenerators.length - 1; i >= 0; i--) {
                if (mToneGenerators[i] != null) {
                    mToneGenerators[i].release();
                }
                mToneGenerators[i] = null;
            }
        }
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_VOLUME_CHANGED: {
                onVolumeChanged(msg.arg1, msg.arg2);
                break;
            }
            case MSG_FREE_RESOURCES: {
                onFreeResources();
                break;
            }
            case MSG_STOP_SOUNDS: {
                onStopSounds();
                break;
            }
            case MSG_PLAY_SOUND: {
                onPlaySound(msg.arg1, msg.arg2);
                break;
            }
            case MSG_VIBRATE: {
                onVibrate();
                break;
            }
        }
    }
}
