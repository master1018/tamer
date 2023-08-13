public class AudioService extends IAudioService.Stub {
    private static final String TAG = "AudioService";
    private static final int PERSIST_DELAY = 3000;
    private Context mContext;
    private ContentResolver mContentResolver;
    private VolumePanel mVolumePanel;
    private static final int SHARED_MSG = -1;
    private static final int SENDMSG_REPLACE = 0;
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int MSG_SET_SYSTEM_VOLUME = 0;
    private static final int MSG_PERSIST_VOLUME = 1;
    private static final int MSG_PERSIST_RINGER_MODE = 3;
    private static final int MSG_PERSIST_VIBRATE_SETTING = 4;
    private static final int MSG_MEDIA_SERVER_DIED = 5;
    private static final int MSG_MEDIA_SERVER_STARTED = 6;
    private static final int MSG_PLAY_SOUND_EFFECT = 7;
    private static final int MSG_BTA2DP_DOCK_TIMEOUT = 8;
    private static final int BTA2DP_DOCK_TIMEOUT_MILLIS = 8000;
    private AudioSystemThread mAudioSystemThread;
    private AudioHandler mAudioHandler;
    private VolumeStreamState[] mStreamStates;
    private SettingsObserver mSettingsObserver;
    private int mMode;
    private Object mSettingsLock = new Object();
    private boolean mMediaServerOk;
    private SoundPool mSoundPool;
    private Object mSoundEffectsLock = new Object();
    private static final int NUM_SOUNDPOOL_CHANNELS = 4;
    private static final int SOUND_EFFECT_VOLUME = 1000;
    private static final String SOUND_EFFECTS_PATH = "/media/audio/ui/";
    private static final String[] SOUND_EFFECT_FILES = new String[] {
        "Effect_Tick.ogg",
        "KeypressStandard.ogg",
        "KeypressSpacebar.ogg",
        "KeypressDelete.ogg",
        "KeypressReturn.ogg"
    };
    private int[][] SOUND_EFFECT_FILES_MAP = new int[][] {
        {0, -1},  
        {0, -1},  
        {0, -1},  
        {0, -1},  
        {0, -1},  
        {1, -1},  
        {2, -1},  
        {3, -1},  
        {4, -1}   
    };
    private int[] MAX_STREAM_VOLUME = new int[] {
        5,  
        7,  
        7,  
        15, 
        7,  
        7,  
        15, 
        7,  
        15, 
        15  
    };
    private int[] STREAM_VOLUME_ALIAS = new int[] {
        AudioSystem.STREAM_VOICE_CALL,  
        AudioSystem.STREAM_SYSTEM,  
        AudioSystem.STREAM_RING,  
        AudioSystem.STREAM_MUSIC, 
        AudioSystem.STREAM_ALARM,  
        AudioSystem.STREAM_NOTIFICATION,  
        AudioSystem.STREAM_BLUETOOTH_SCO, 
        AudioSystem.STREAM_SYSTEM,  
        AudioSystem.STREAM_VOICE_CALL, 
        AudioSystem.STREAM_MUSIC  
    };
    private AudioSystem.ErrorCallback mAudioSystemCallback = new AudioSystem.ErrorCallback() {
        public void onError(int error) {
            switch (error) {
            case AudioSystem.AUDIO_STATUS_SERVER_DIED:
                if (mMediaServerOk) {
                    sendMsg(mAudioHandler, MSG_MEDIA_SERVER_DIED, SHARED_MSG, SENDMSG_NOOP, 0, 0,
                            null, 1500);
                    mMediaServerOk = false;
                }
                break;
            case AudioSystem.AUDIO_STATUS_OK:
                if (!mMediaServerOk) {
                    sendMsg(mAudioHandler, MSG_MEDIA_SERVER_STARTED, SHARED_MSG, SENDMSG_NOOP, 0, 0,
                            null, 0);
                    mMediaServerOk = true;
                }
                break;
            default:
                break;
            }
       }
    };
    private int mRingerMode;
    private int mRingerModeAffectedStreams;
    private int mRingerModeMutedStreams;
    private int mMuteAffectedStreams;
    private int mVibrateSetting;
    private int mNotificationsUseRingVolume;
    private final BroadcastReceiver mReceiver = new AudioServiceBroadcastReceiver();
    private final BroadcastReceiver mMediaButtonReceiver = new MediaButtonBroadcastReceiver();
    private HashMap <Integer, String> mConnectedDevices = new HashMap <Integer, String>();
    private int mForcedUseForComm;
    private ArrayList <SetModeDeathHandler> mSetModeDeathHandlers = new ArrayList <SetModeDeathHandler>();
    private ArrayList <ScoClient> mScoClients = new ArrayList <ScoClient>();
    private BluetoothHeadset mBluetoothHeadset;
    private boolean mBluetoothHeadsetConnected;
    public AudioService(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        MAX_STREAM_VOLUME[AudioSystem.STREAM_VOICE_CALL] = SystemProperties.getInt(
            "ro.config.vc_call_vol_steps",
           MAX_STREAM_VOLUME[AudioSystem.STREAM_VOICE_CALL]);
        mVolumePanel = new VolumePanel(context, this);
        mSettingsObserver = new SettingsObserver();
        mForcedUseForComm = AudioSystem.FORCE_NONE;
        createAudioSystemThread();
        readPersistedSettings();
        createStreamStates();
        mMode = AudioSystem.MODE_INVALID;
        setMode(AudioSystem.MODE_NORMAL, null);
        mMediaServerOk = true;
        mRingerModeMutedStreams = 0;
        setRingerModeInt(getRingerMode(), false);
        AudioSystem.setErrorCallback(mAudioSystemCallback);
        loadSoundEffects();
        mBluetoothHeadsetConnected = false;
        mBluetoothHeadset = new BluetoothHeadset(context,
                                                 mBluetoothHeadsetServiceListener);
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(BluetoothA2dp.ACTION_SINK_STATE_CHANGED);
        intentFilter.addAction(BluetoothHeadset.ACTION_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
        intentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        context.registerReceiver(mReceiver, intentFilter);
        intentFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        context.registerReceiver(mMediaButtonReceiver, intentFilter);
        TelephonyManager tmgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        tmgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    private void createAudioSystemThread() {
        mAudioSystemThread = new AudioSystemThread();
        mAudioSystemThread.start();
        waitForAudioHandlerCreation();
    }
    private void waitForAudioHandlerCreation() {
        synchronized(this) {
            while (mAudioHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while waiting on volume handler.");
                }
            }
        }
    }
    private void createStreamStates() {
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        VolumeStreamState[] streams = mStreamStates = new VolumeStreamState[numStreamTypes];
        for (int i = 0; i < numStreamTypes; i++) {
            streams[i] = new VolumeStreamState(System.VOLUME_SETTINGS[STREAM_VOLUME_ALIAS[i]], i);
        }
        for (int i = 0; i < numStreamTypes; i++) {
            if (STREAM_VOLUME_ALIAS[i] != i) {
                int index = rescaleIndex(streams[i].mIndex, STREAM_VOLUME_ALIAS[i], i);
                streams[i].mIndex = streams[i].getValidIndex(index);
                setStreamVolumeIndex(i, index);
                index = rescaleIndex(streams[i].mLastAudibleIndex, STREAM_VOLUME_ALIAS[i], i);
                streams[i].mLastAudibleIndex = streams[i].getValidIndex(index);
            }
        }
    }
    private void readPersistedSettings() {
        final ContentResolver cr = mContentResolver;
        mRingerMode = System.getInt(cr, System.MODE_RINGER, AudioManager.RINGER_MODE_NORMAL);
        mVibrateSetting = System.getInt(cr, System.VIBRATE_ON, 0);
        mRingerModeAffectedStreams = Settings.System.getInt(cr,
                Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                ((1 << AudioSystem.STREAM_RING)|(1 << AudioSystem.STREAM_NOTIFICATION)|
                 (1 << AudioSystem.STREAM_SYSTEM)|(1 << AudioSystem.STREAM_SYSTEM_ENFORCED)));
        mMuteAffectedStreams = System.getInt(cr,
                System.MUTE_STREAMS_AFFECTED,
                ((1 << AudioSystem.STREAM_MUSIC)|(1 << AudioSystem.STREAM_RING)|(1 << AudioSystem.STREAM_SYSTEM)));
        mNotificationsUseRingVolume = System.getInt(cr,
                Settings.System.NOTIFICATIONS_USE_RING_VOLUME, 1);
        if (mNotificationsUseRingVolume == 1) {
            STREAM_VOLUME_ALIAS[AudioSystem.STREAM_NOTIFICATION] = AudioSystem.STREAM_RING;
        }
        broadcastRingerMode();
        broadcastVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        broadcastVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION);
    }
    private void setStreamVolumeIndex(int stream, int index) {
        AudioSystem.setStreamVolumeIndex(stream, (index + 5)/10);
    }
    private int rescaleIndex(int index, int srcStream, int dstStream) {
        return (index * mStreamStates[dstStream].getMaxIndex() + mStreamStates[srcStream].getMaxIndex() / 2) / mStreamStates[srcStream].getMaxIndex();
    }
    public void adjustVolume(int direction, int flags) {
        adjustSuggestedStreamVolume(direction, AudioManager.USE_DEFAULT_STREAM_TYPE, flags);
    }
    public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags) {
        int streamType = getActiveStreamType(suggestedStreamType);
        if (streamType != AudioSystem.STREAM_RING && (flags & AudioManager.FLAG_PLAY_SOUND) != 0) {
            flags &= ~AudioManager.FLAG_PLAY_SOUND;
        }
        adjustStreamVolume(streamType, direction, flags);
    }
    public void adjustStreamVolume(int streamType, int direction, int flags) {
        ensureValidDirection(direction);
        ensureValidStreamType(streamType);
        VolumeStreamState streamState = mStreamStates[STREAM_VOLUME_ALIAS[streamType]];
        final int oldIndex = (streamState.muteCount() != 0) ? streamState.mLastAudibleIndex : streamState.mIndex;
        boolean adjustVolume = true;
        if ((flags & AudioManager.FLAG_ALLOW_RINGER_MODES) != 0
                || streamType == AudioSystem.STREAM_RING) {
            adjustVolume = checkForRingerModeChange(oldIndex, direction);
        }
        int index;
        if (streamState.muteCount() != 0) {
            if (adjustVolume) {
                streamState.adjustLastAudibleIndex(direction);
                sendMsg(mAudioHandler, MSG_PERSIST_VOLUME, streamType,
                        SENDMSG_REPLACE, 0, 1, streamState, PERSIST_DELAY);
            }
            index = streamState.mLastAudibleIndex;
        } else {
            if (adjustVolume && streamState.adjustIndex(direction)) {
                sendMsg(mAudioHandler, MSG_SET_SYSTEM_VOLUME, STREAM_VOLUME_ALIAS[streamType], SENDMSG_NOOP, 0, 0,
                        streamState, 0);
            }
            index = streamState.mIndex;
        }
        mVolumePanel.postVolumeChanged(streamType, flags);
        sendVolumeUpdate(streamType, oldIndex, index);
    }
    public void setStreamVolume(int streamType, int index, int flags) {
        ensureValidStreamType(streamType);
        VolumeStreamState streamState = mStreamStates[STREAM_VOLUME_ALIAS[streamType]];
        final int oldIndex = (streamState.muteCount() != 0) ? streamState.mLastAudibleIndex : streamState.mIndex;
        index = rescaleIndex(index * 10, streamType, STREAM_VOLUME_ALIAS[streamType]);
        setStreamVolumeInt(STREAM_VOLUME_ALIAS[streamType], index, false, true);
        index = (streamState.muteCount() != 0) ? streamState.mLastAudibleIndex : streamState.mIndex;
        mVolumePanel.postVolumeChanged(streamType, flags);
        sendVolumeUpdate(streamType, oldIndex, index);
    }
    private void sendVolumeUpdate(int streamType, int oldIndex, int index) {
        oldIndex = (oldIndex + 5) / 10;
        index = (index + 5) / 10;
        Intent intent = new Intent(AudioManager.VOLUME_CHANGED_ACTION);
        intent.putExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, streamType);
        intent.putExtra(AudioManager.EXTRA_VOLUME_STREAM_VALUE, index);
        intent.putExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_VALUE, oldIndex);
        mContext.sendBroadcast(intent);
    }
    private void setStreamVolumeInt(int streamType, int index, boolean force, boolean lastAudible) {
        VolumeStreamState streamState = mStreamStates[streamType];
        if (streamState.muteCount() != 0) {
            if (index != 0) {
                streamState.setLastAudibleIndex(index);
                sendMsg(mAudioHandler, MSG_PERSIST_VOLUME, streamType,
                        SENDMSG_REPLACE, 0, 1, streamState, PERSIST_DELAY);
            }
        } else {
            if (streamState.setIndex(index, lastAudible) || force) {
                sendMsg(mAudioHandler, MSG_SET_SYSTEM_VOLUME, streamType, SENDMSG_NOOP, 0, 0,
                        streamState, 0);
            }
        }
    }
    public void setStreamSolo(int streamType, boolean state, IBinder cb) {
        for (int stream = 0; stream < mStreamStates.length; stream++) {
            if (!isStreamAffectedByMute(stream) || stream == streamType) continue;
            mStreamStates[stream].mute(cb, state);
         }
    }
    public void setStreamMute(int streamType, boolean state, IBinder cb) {
        if (isStreamAffectedByMute(streamType)) {
            mStreamStates[streamType].mute(cb, state);
        }
    }
    public int getStreamVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (mStreamStates[streamType].mIndex + 5) / 10;
    }
    public int getStreamMaxVolume(int streamType) {
        ensureValidStreamType(streamType);
        return (mStreamStates[streamType].getMaxIndex() + 5) / 10;
    }
    public int getRingerMode() {
        return mRingerMode;
    }
    public void setRingerMode(int ringerMode) {
        synchronized (mSettingsLock) {
            if (ringerMode != mRingerMode) {
                setRingerModeInt(ringerMode, true);
                broadcastRingerMode();
            }
        }
    }
    private void setRingerModeInt(int ringerMode, boolean persist) {
        mRingerMode = ringerMode;
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = numStreamTypes - 1; streamType >= 0; streamType--) {
            if (isStreamMutedByRingerMode(streamType)) {
                if (!isStreamAffectedByRingerMode(streamType) ||
                    mRingerMode == AudioManager.RINGER_MODE_NORMAL) {
                    mStreamStates[streamType].mute(null, false);
                    mRingerModeMutedStreams &= ~(1 << streamType);
                }
            } else {
                if (isStreamAffectedByRingerMode(streamType) &&
                    mRingerMode != AudioManager.RINGER_MODE_NORMAL) {
                   mStreamStates[streamType].mute(null, true);
                   mRingerModeMutedStreams |= (1 << streamType);
               }
            }
        }
        if (persist) {
            sendMsg(mAudioHandler, MSG_PERSIST_RINGER_MODE, SHARED_MSG,
                    SENDMSG_REPLACE, 0, 0, null, PERSIST_DELAY);
        }
    }
    public boolean shouldVibrate(int vibrateType) {
        switch (getVibrateSetting(vibrateType)) {
            case AudioManager.VIBRATE_SETTING_ON:
                return mRingerMode != AudioManager.RINGER_MODE_SILENT;
            case AudioManager.VIBRATE_SETTING_ONLY_SILENT:
                return mRingerMode == AudioManager.RINGER_MODE_VIBRATE;
            case AudioManager.VIBRATE_SETTING_OFF:
                return false;
            default:
                return false;
        }
    }
    public int getVibrateSetting(int vibrateType) {
        return (mVibrateSetting >> (vibrateType * 2)) & 3;
    }
    public void setVibrateSetting(int vibrateType, int vibrateSetting) {
        mVibrateSetting = getValueForVibrateSetting(mVibrateSetting, vibrateType, vibrateSetting);
        broadcastVibrateSetting(vibrateType);
        sendMsg(mAudioHandler, MSG_PERSIST_VIBRATE_SETTING, SHARED_MSG, SENDMSG_NOOP, 0, 0,
                null, 0);
    }
    public static int getValueForVibrateSetting(int existingValue, int vibrateType,
            int vibrateSetting) {
        existingValue &= ~(3 << (vibrateType * 2));
        existingValue |= (vibrateSetting & 3) << (vibrateType * 2);
        return existingValue;
    }
    private class SetModeDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb; 
        private int mMode = AudioSystem.MODE_NORMAL; 
        SetModeDeathHandler(IBinder cb) {
            mCb = cb;
        }
        public void binderDied() {
            synchronized(mSetModeDeathHandlers) {
                Log.w(TAG, "setMode() client died");
                int index = mSetModeDeathHandlers.indexOf(this);
                if (index < 0) {
                    Log.w(TAG, "unregistered setMode() client died");
                } else {
                    mSetModeDeathHandlers.remove(this);
                    if (index == 0) {
                        SetModeDeathHandler hdlr = mSetModeDeathHandlers.get(0);
                        int mode = hdlr.getMode();
                        if (AudioService.this.mMode != mode) {
                            if (AudioSystem.setPhoneState(mode) == AudioSystem.AUDIO_STATUS_OK) {
                                AudioService.this.mMode = mode;
                            }
                        }
                    }
                }
            }
        }
        public void setMode(int mode) {
            mMode = mode;
        }
        public int getMode() {
            return mMode;
        }
        public IBinder getBinder() {
            return mCb;
        }
    }
    public void setMode(int mode, IBinder cb) {
        if (!checkAudioSettingsPermission("setMode()")) {
            return;
        }
        if (mode < AudioSystem.MODE_CURRENT || mode > AudioSystem.MODE_IN_CALL) {
            return;
        }
        synchronized (mSettingsLock) {
            if (mode == AudioSystem.MODE_CURRENT) {
                mode = mMode;
            }
            if (mode != mMode) {
                if (AudioSystem.setPhoneState(mode) == AudioSystem.AUDIO_STATUS_OK) {
                    mMode = mode;
                    synchronized(mSetModeDeathHandlers) {
                        SetModeDeathHandler hdlr = null;
                        Iterator iter = mSetModeDeathHandlers.iterator();
                        while (iter.hasNext()) {
                            SetModeDeathHandler h = (SetModeDeathHandler)iter.next();
                            if (h.getBinder() == cb) {
                                hdlr = h;
                                iter.remove();
                                break;
                            }
                        }
                        if (hdlr == null) {
                            hdlr = new SetModeDeathHandler(cb);
                            if (cb != null) {
                                try {
                                    cb.linkToDeath(hdlr, 0);
                                } catch (RemoteException e) {
                                    Log.w(TAG, "setMode() could not link to "+cb+" binder death");
                                }
                            }
                        }
                        mSetModeDeathHandlers.add(0, hdlr);
                        hdlr.setMode(mode);
                    }
                    if (mode != AudioSystem.MODE_NORMAL) {
                        clearAllScoClients();
                    }
                }
            }
            int streamType = getActiveStreamType(AudioManager.USE_DEFAULT_STREAM_TYPE);
            int index = mStreamStates[STREAM_VOLUME_ALIAS[streamType]].mIndex;
            setStreamVolumeInt(STREAM_VOLUME_ALIAS[streamType], index, true, false);
        }
    }
    public int getMode() {
        return mMode;
    }
    public void playSoundEffect(int effectType) {
        sendMsg(mAudioHandler, MSG_PLAY_SOUND_EFFECT, SHARED_MSG, SENDMSG_NOOP,
                effectType, -1, null, 0);
    }
    public void playSoundEffectVolume(int effectType, float volume) {
        loadSoundEffects();
        sendMsg(mAudioHandler, MSG_PLAY_SOUND_EFFECT, SHARED_MSG, SENDMSG_NOOP,
                effectType, (int) (volume * 1000), null, 0);
    }
    public boolean loadSoundEffects() {
        synchronized (mSoundEffectsLock) {
            if (mSoundPool != null) {
                return true;
            }
            mSoundPool = new SoundPool(NUM_SOUNDPOOL_CHANNELS, AudioSystem.STREAM_SYSTEM, 0);
            if (mSoundPool == null) {
                return false;
            }
            int[] poolId = new int[SOUND_EFFECT_FILES.length];
            for (int fileIdx = 0; fileIdx < SOUND_EFFECT_FILES.length; fileIdx++) {
                poolId[fileIdx] = -1;
            }
            for (int effect = 0; effect < AudioManager.NUM_SOUND_EFFECTS; effect++) {
                if (SOUND_EFFECT_FILES_MAP[effect][1] == 0) {
                    continue;
                }
                if (poolId[SOUND_EFFECT_FILES_MAP[effect][0]] == -1) {
                    String filePath = Environment.getRootDirectory() + SOUND_EFFECTS_PATH + SOUND_EFFECT_FILES[SOUND_EFFECT_FILES_MAP[effect][0]];
                    int sampleId = mSoundPool.load(filePath, 0);
                    SOUND_EFFECT_FILES_MAP[effect][1] = sampleId;
                    poolId[SOUND_EFFECT_FILES_MAP[effect][0]] = sampleId;
                    if (sampleId <= 0) {
                        Log.w(TAG, "Soundpool could not load file: "+filePath);
                    }
                } else {
                    SOUND_EFFECT_FILES_MAP[effect][1] = poolId[SOUND_EFFECT_FILES_MAP[effect][0]];
                }
            }
        }
        return true;
    }
    public void unloadSoundEffects() {
        synchronized (mSoundEffectsLock) {
            if (mSoundPool == null) {
                return;
            }
            int[] poolId = new int[SOUND_EFFECT_FILES.length];
            for (int fileIdx = 0; fileIdx < SOUND_EFFECT_FILES.length; fileIdx++) {
                poolId[fileIdx] = 0;
            }
            for (int effect = 0; effect < AudioManager.NUM_SOUND_EFFECTS; effect++) {
                if (SOUND_EFFECT_FILES_MAP[effect][1] <= 0) {
                    continue;
                }
                if (poolId[SOUND_EFFECT_FILES_MAP[effect][0]] == 0) {
                    mSoundPool.unload(SOUND_EFFECT_FILES_MAP[effect][1]);
                    SOUND_EFFECT_FILES_MAP[effect][1] = -1;
                    poolId[SOUND_EFFECT_FILES_MAP[effect][0]] = -1;
                }
            }
            mSoundPool = null;
        }
    }
    public void reloadAudioSettings() {
        readPersistedSettings();
        int numStreamTypes = AudioSystem.getNumStreamTypes();
        for (int streamType = 0; streamType < numStreamTypes; streamType++) {
            VolumeStreamState streamState = mStreamStates[streamType];
            String settingName = System.VOLUME_SETTINGS[STREAM_VOLUME_ALIAS[streamType]];
            String lastAudibleSettingName = settingName + System.APPEND_FOR_LAST_AUDIBLE;
            int index = Settings.System.getInt(mContentResolver,
                                           settingName,
                                           AudioManager.DEFAULT_STREAM_VOLUME[streamType]);
            if (STREAM_VOLUME_ALIAS[streamType] != streamType) {
                index = rescaleIndex(index * 10, STREAM_VOLUME_ALIAS[streamType], streamType);
            } else {
                index *= 10;
            }
            streamState.mIndex = streamState.getValidIndex(index);
            index = (index + 5) / 10;
            index = Settings.System.getInt(mContentResolver,
                                            lastAudibleSettingName,
                                            (index > 0) ? index : AudioManager.DEFAULT_STREAM_VOLUME[streamType]);
            if (STREAM_VOLUME_ALIAS[streamType] != streamType) {
                index = rescaleIndex(index * 10, STREAM_VOLUME_ALIAS[streamType], streamType);
            } else {
                index *= 10;
            }
            streamState.mLastAudibleIndex = streamState.getValidIndex(index);
            if (streamState.muteCount() != 0 && !isStreamAffectedByMute(streamType)) {
                int size = streamState.mDeathHandlers.size();
                for (int i = 0; i < size; i++) {
                    streamState.mDeathHandlers.get(i).mMuteCount = 1;
                    streamState.mDeathHandlers.get(i).mute(false);
                }
            }
            if (streamState.muteCount() == 0) {
                setStreamVolumeIndex(streamType, streamState.mIndex);
            }
        }
        setRingerModeInt(getRingerMode(), false);
    }
    public void setSpeakerphoneOn(boolean on){
        if (!checkAudioSettingsPermission("setSpeakerphoneOn()")) {
            return;
        }
        if (on) {
            AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION, AudioSystem.FORCE_SPEAKER);
            mForcedUseForComm = AudioSystem.FORCE_SPEAKER;
        } else {
            AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION, AudioSystem.FORCE_NONE);
            mForcedUseForComm = AudioSystem.FORCE_NONE;
        }
    }
    public boolean isSpeakerphoneOn() {
        if (mForcedUseForComm == AudioSystem.FORCE_SPEAKER) {
            return true;
        } else {
            return false;
        }
    }
    public void setBluetoothScoOn(boolean on){
        if (!checkAudioSettingsPermission("setBluetoothScoOn()")) {
            return;
        }
        if (on) {
            AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION, AudioSystem.FORCE_BT_SCO);
            AudioSystem.setForceUse(AudioSystem.FOR_RECORD, AudioSystem.FORCE_BT_SCO);
            mForcedUseForComm = AudioSystem.FORCE_BT_SCO;
        } else {
            AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION, AudioSystem.FORCE_NONE);
            AudioSystem.setForceUse(AudioSystem.FOR_RECORD, AudioSystem.FORCE_NONE);
            mForcedUseForComm = AudioSystem.FORCE_NONE;
        }
    }
    public boolean isBluetoothScoOn() {
        if (mForcedUseForComm == AudioSystem.FORCE_BT_SCO) {
            return true;
        } else {
            return false;
        }
    }
    public void startBluetoothSco(IBinder cb){
        if (!checkAudioSettingsPermission("startBluetoothSco()")) {
            return;
        }
        ScoClient client = getScoClient(cb);
        client.incCount();
    }
    public void stopBluetoothSco(IBinder cb){
        if (!checkAudioSettingsPermission("stopBluetoothSco()")) {
            return;
        }
        ScoClient client = getScoClient(cb);
        client.decCount();
    }
    private class ScoClient implements IBinder.DeathRecipient {
        private IBinder mCb; 
        private int mStartcount; 
        ScoClient(IBinder cb) {
            mCb = cb;
            mStartcount = 0;
        }
        public void binderDied() {
            synchronized(mScoClients) {
                Log.w(TAG, "SCO client died");
                int index = mScoClients.indexOf(this);
                if (index < 0) {
                    Log.w(TAG, "unregistered SCO client died");
                } else {
                    clearCount(true);
                    mScoClients.remove(this);
                }
            }
        }
        public void incCount() {
            synchronized(mScoClients) {
                requestScoState(BluetoothHeadset.AUDIO_STATE_CONNECTED);
                if (mStartcount == 0) {
                    try {
                        mCb.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Log.w(TAG, "ScoClient  incCount() could not link to "+mCb+" binder death");
                    }
                }
                mStartcount++;
            }
        }
        public void decCount() {
            synchronized(mScoClients) {
                if (mStartcount == 0) {
                    Log.w(TAG, "ScoClient.decCount() already 0");
                } else {
                    mStartcount--;
                    if (mStartcount == 0) {
                        try {
                            mCb.unlinkToDeath(this, 0);
                        } catch (NoSuchElementException e) {
                            Log.w(TAG, "decCount() going to 0 but not registered to binder");
                        }
                    }
                    requestScoState(BluetoothHeadset.AUDIO_STATE_DISCONNECTED);
                }
            }
        }
        public void clearCount(boolean stopSco) {
            synchronized(mScoClients) {
                if (mStartcount != 0) {
                    try {
                        mCb.unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e) {
                        Log.w(TAG, "clearCount() mStartcount: "+mStartcount+" != 0 but not registered to binder");
                    }
                }
                mStartcount = 0;
                if (stopSco) {
                    requestScoState(BluetoothHeadset.AUDIO_STATE_DISCONNECTED);
                }
            }
        }
        public int getCount() {
            return mStartcount;
        }
        public IBinder getBinder() {
            return mCb;
        }
        public int totalCount() {
            synchronized(mScoClients) {
                int count = 0;
                int size = mScoClients.size();
                for (int i = 0; i < size; i++) {
                    count += mScoClients.get(i).getCount();
                }
                return count;
            }
        }
        private void requestScoState(int state) {
            if (totalCount() == 0 &&
                mBluetoothHeadsetConnected &&
                AudioService.this.mMode == AudioSystem.MODE_NORMAL) {
                if (state == BluetoothHeadset.AUDIO_STATE_CONNECTED) {
                    mBluetoothHeadset.startVoiceRecognition();
                } else {
                    mBluetoothHeadset.stopVoiceRecognition();
                }
            }
        }
    }
    public ScoClient getScoClient(IBinder cb) {
        synchronized(mScoClients) {
            ScoClient client;
            int size = mScoClients.size();
            for (int i = 0; i < size; i++) {
                client = mScoClients.get(i);
                if (client.getBinder() == cb)
                    return client;
            }
            client = new ScoClient(cb);
            mScoClients.add(client);
            return client;
        }
    }
    public void clearAllScoClients() {
        synchronized(mScoClients) {
            int size = mScoClients.size();
            for (int i = 0; i < size; i++) {
                mScoClients.get(i).clearCount(false);
            }
        }
    }
    private BluetoothHeadset.ServiceListener mBluetoothHeadsetServiceListener =
        new BluetoothHeadset.ServiceListener() {
        public void onServiceConnected() {
            if (mBluetoothHeadset != null &&
                mBluetoothHeadset.getState() == BluetoothHeadset.STATE_CONNECTED) {
                mBluetoothHeadsetConnected = true;
            }
        }
        public void onServiceDisconnected() {
            if (mBluetoothHeadset != null &&
                mBluetoothHeadset.getState() == BluetoothHeadset.STATE_DISCONNECTED) {
                mBluetoothHeadsetConnected = false;
                clearAllScoClients();
            }
        }
    };
    private boolean checkForRingerModeChange(int oldIndex, int direction) {
        boolean adjustVolumeIndex = true;
        int newRingerMode = mRingerMode;
        if (mRingerMode == AudioManager.RINGER_MODE_NORMAL) {
            if (direction == AudioManager.ADJUST_LOWER
                    && (oldIndex + 5) / 10 == 1) {
                newRingerMode = System.getInt(mContentResolver, System.VIBRATE_IN_SILENT, 1) == 1
                    ? AudioManager.RINGER_MODE_VIBRATE
                    : AudioManager.RINGER_MODE_SILENT;
            }
        } else {
            if (direction == AudioManager.ADJUST_RAISE) {
                newRingerMode = AudioManager.RINGER_MODE_NORMAL;
            } else {
                adjustVolumeIndex = false;
            }
        }
        if (newRingerMode != mRingerMode) {
            setRingerMode(newRingerMode);
            adjustVolumeIndex = false;
        }
        return adjustVolumeIndex;
    }
    public boolean isStreamAffectedByRingerMode(int streamType) {
        return (mRingerModeAffectedStreams & (1 << streamType)) != 0;
    }
    private boolean isStreamMutedByRingerMode(int streamType) {
        return (mRingerModeMutedStreams & (1 << streamType)) != 0;
    }
    public boolean isStreamAffectedByMute(int streamType) {
        return (mMuteAffectedStreams & (1 << streamType)) != 0;
    }
    private void ensureValidDirection(int direction) {
        if (direction < AudioManager.ADJUST_LOWER || direction > AudioManager.ADJUST_RAISE) {
            throw new IllegalArgumentException("Bad direction " + direction);
        }
    }
    private void ensureValidStreamType(int streamType) {
        if (streamType < 0 || streamType >= mStreamStates.length) {
            throw new IllegalArgumentException("Bad stream type " + streamType);
        }
    }
    private int getActiveStreamType(int suggestedStreamType) {
        boolean isOffhook = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) isOffhook = phone.isOffhook();
        } catch (RemoteException e) {
            Log.w(TAG, "Couldn't connect to phone service", e);
        }
        if (AudioSystem.getForceUse(AudioSystem.FOR_COMMUNICATION) == AudioSystem.FORCE_BT_SCO) {
            return AudioSystem.STREAM_BLUETOOTH_SCO;
        } else if (isOffhook || AudioSystem.isStreamActive(AudioSystem.STREAM_VOICE_CALL)) {
            return AudioSystem.STREAM_VOICE_CALL;
        } else if (AudioSystem.isStreamActive(AudioSystem.STREAM_MUSIC)) {
            return AudioSystem.STREAM_MUSIC;
        } else if (suggestedStreamType == AudioManager.USE_DEFAULT_STREAM_TYPE) {
            return AudioSystem.STREAM_RING;
        } else {
            return suggestedStreamType;
        }
    }
    private void broadcastRingerMode() {
        Intent broadcast = new Intent(AudioManager.RINGER_MODE_CHANGED_ACTION);
        broadcast.putExtra(AudioManager.EXTRA_RINGER_MODE, mRingerMode);
        broadcast.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        long origCallerIdentityToken = Binder.clearCallingIdentity();
        mContext.sendStickyBroadcast(broadcast);
        Binder.restoreCallingIdentity(origCallerIdentityToken);
    }
    private void broadcastVibrateSetting(int vibrateType) {
        if (ActivityManagerNative.isSystemReady()) {
            Intent broadcast = new Intent(AudioManager.VIBRATE_SETTING_CHANGED_ACTION);
            broadcast.putExtra(AudioManager.EXTRA_VIBRATE_TYPE, vibrateType);
            broadcast.putExtra(AudioManager.EXTRA_VIBRATE_SETTING, getVibrateSetting(vibrateType));
            mContext.sendBroadcast(broadcast);
        }
    }
    private static int getMsg(int baseMsg, int streamType) {
        return (baseMsg & 0xffff) | streamType << 16;
    }
    private static int getMsgBase(int msg) {
        return msg & 0xffff;
    }
    private static void sendMsg(Handler handler, int baseMsg, int streamType,
            int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        int msg = (streamType == SHARED_MSG) ? baseMsg : getMsg(baseMsg, streamType);
        if (existingMsgPolicy == SENDMSG_REPLACE) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == SENDMSG_NOOP && handler.hasMessages(msg)) {
            return;
        }
        handler
                .sendMessageDelayed(handler.obtainMessage(msg, arg1, arg2, obj), delay);
    }
    boolean checkAudioSettingsPermission(String method) {
        if (mContext.checkCallingOrSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS")
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        String msg = "Audio Settings Permission Denial: " + method + " from pid="
                + Binder.getCallingPid()
                + ", uid=" + Binder.getCallingUid();
        Log.w(TAG, msg);
        return false;
    }
    public class VolumeStreamState {
        private final int mStreamType;
        private String mVolumeIndexSettingName;
        private String mLastAudibleVolumeIndexSettingName;
        private int mIndexMax;
        private int mIndex;
        private int mLastAudibleIndex;
        private ArrayList<VolumeDeathHandler> mDeathHandlers; 
        private VolumeStreamState(String settingName, int streamType) {
            setVolumeIndexSettingName(settingName);
            mStreamType = streamType;
            final ContentResolver cr = mContentResolver;
            mIndexMax = MAX_STREAM_VOLUME[streamType];
            mIndex = Settings.System.getInt(cr,
                                            mVolumeIndexSettingName,
                                            AudioManager.DEFAULT_STREAM_VOLUME[streamType]);
            mLastAudibleIndex = Settings.System.getInt(cr,
                                                       mLastAudibleVolumeIndexSettingName,
                                                       (mIndex > 0) ? mIndex : AudioManager.DEFAULT_STREAM_VOLUME[streamType]);
            AudioSystem.initStreamVolume(streamType, 0, mIndexMax);
            mIndexMax *= 10;
            mIndex = getValidIndex(10 * mIndex);
            mLastAudibleIndex = getValidIndex(10 * mLastAudibleIndex);
            setStreamVolumeIndex(streamType, mIndex);
            mDeathHandlers = new ArrayList<VolumeDeathHandler>();
        }
        public void setVolumeIndexSettingName(String settingName) {
            mVolumeIndexSettingName = settingName;
            mLastAudibleVolumeIndexSettingName = settingName + System.APPEND_FOR_LAST_AUDIBLE;
        }
        public boolean adjustIndex(int deltaIndex) {
            return setIndex(mIndex + deltaIndex * 10, true);
        }
        public boolean setIndex(int index, boolean lastAudible) {
            int oldIndex = mIndex;
            mIndex = getValidIndex(index);
            if (oldIndex != mIndex) {
                if (lastAudible) {
                    mLastAudibleIndex = mIndex;
                }
                int numStreamTypes = AudioSystem.getNumStreamTypes();
                for (int streamType = numStreamTypes - 1; streamType >= 0; streamType--) {
                    if (streamType != mStreamType && STREAM_VOLUME_ALIAS[streamType] == mStreamType) {
                        mStreamStates[streamType].setIndex(rescaleIndex(mIndex, mStreamType, streamType), lastAudible);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        public void setLastAudibleIndex(int index) {
            mLastAudibleIndex = getValidIndex(index);
        }
        public void adjustLastAudibleIndex(int deltaIndex) {
            setLastAudibleIndex(mLastAudibleIndex + deltaIndex * 10);
        }
        public int getMaxIndex() {
            return mIndexMax;
        }
        public void mute(IBinder cb, boolean state) {
            VolumeDeathHandler handler = getDeathHandler(cb, state);
            if (handler == null) {
                Log.e(TAG, "Could not get client death handler for stream: "+mStreamType);
                return;
            }
            handler.mute(state);
        }
        private int getValidIndex(int index) {
            if (index < 0) {
                return 0;
            } else if (index > mIndexMax) {
                return mIndexMax;
            }
            return index;
        }
        private class VolumeDeathHandler implements IBinder.DeathRecipient {
            private IBinder mICallback; 
            private int mMuteCount; 
            VolumeDeathHandler(IBinder cb) {
                mICallback = cb;
            }
            public void mute(boolean state) {
                synchronized(mDeathHandlers) {
                    if (state) {
                        if (mMuteCount == 0) {
                            try {
                                if (mICallback != null) {
                                    mICallback.linkToDeath(this, 0);
                                }
                                mDeathHandlers.add(this);
                                if (muteCount() == 0) {
                                    setIndex(0, false);
                                    sendMsg(mAudioHandler, MSG_SET_SYSTEM_VOLUME, mStreamType, SENDMSG_NOOP, 0, 0,
                                            VolumeStreamState.this, 0);
                                }
                            } catch (RemoteException e) {
                                binderDied();
                                mDeathHandlers.notify();
                                return;
                            }
                        } else {
                            Log.w(TAG, "stream: "+mStreamType+" was already muted by this client");
                        }
                        mMuteCount++;
                    } else {
                        if (mMuteCount == 0) {
                            Log.e(TAG, "unexpected unmute for stream: "+mStreamType);
                        } else {
                            mMuteCount--;
                            if (mMuteCount == 0) {
                                mDeathHandlers.remove(this);
                                if (mICallback != null) {
                                    mICallback.unlinkToDeath(this, 0);
                                }
                                if (muteCount() == 0) {
                                    if (!isStreamAffectedByRingerMode(mStreamType) || mRingerMode == AudioManager.RINGER_MODE_NORMAL) {
                                        setIndex(mLastAudibleIndex, false);
                                        sendMsg(mAudioHandler, MSG_SET_SYSTEM_VOLUME, mStreamType, SENDMSG_NOOP, 0, 0,
                                                VolumeStreamState.this, 0);
                                    }
                                }
                            }
                        }
                    }
                    mDeathHandlers.notify();
                }
            }
            public void binderDied() {
                Log.w(TAG, "Volume service client died for stream: "+mStreamType);
                if (mMuteCount != 0) {
                    mMuteCount = 1;
                    mute(false);
                }
            }
        }
        private int muteCount() {
            int count = 0;
            int size = mDeathHandlers.size();
            for (int i = 0; i < size; i++) {
                count += mDeathHandlers.get(i).mMuteCount;
            }
            return count;
        }
        private VolumeDeathHandler getDeathHandler(IBinder cb, boolean state) {
            synchronized(mDeathHandlers) {
                VolumeDeathHandler handler;
                int size = mDeathHandlers.size();
                for (int i = 0; i < size; i++) {
                    handler = mDeathHandlers.get(i);
                    if (cb == handler.mICallback) {
                        return handler;
                    }
                }
                if (state) {
                    handler = new VolumeDeathHandler(cb);
                } else {
                    Log.w(TAG, "stream was not muted by this client");
                    handler = null;
                }
                return handler;
            }
        }
    }
    private class AudioSystemThread extends Thread {
        AudioSystemThread() {
            super("AudioService");
        }
        @Override
        public void run() {
            Looper.prepare();
            synchronized(AudioService.this) {
                mAudioHandler = new AudioHandler();
                AudioService.this.notify();
            }
            Looper.loop();
        }
    }
    private class AudioHandler extends Handler {
        private void setSystemVolume(VolumeStreamState streamState) {
            setStreamVolumeIndex(streamState.mStreamType, streamState.mIndex);
            int numStreamTypes = AudioSystem.getNumStreamTypes();
            for (int streamType = numStreamTypes - 1; streamType >= 0; streamType--) {
                if (streamType != streamState.mStreamType &&
                    STREAM_VOLUME_ALIAS[streamType] == streamState.mStreamType) {
                    setStreamVolumeIndex(streamType, mStreamStates[streamType].mIndex);
                }
            }
            sendMsg(mAudioHandler, MSG_PERSIST_VOLUME, streamState.mStreamType,
                    SENDMSG_REPLACE, 1, 1, streamState, PERSIST_DELAY);
        }
        private void persistVolume(VolumeStreamState streamState, boolean current, boolean lastAudible) {
            if (current) {
                System.putInt(mContentResolver, streamState.mVolumeIndexSettingName,
                              (streamState.mIndex + 5)/ 10);
            }
            if (lastAudible) {
                System.putInt(mContentResolver, streamState.mLastAudibleVolumeIndexSettingName,
                    (streamState.mLastAudibleIndex + 5) / 10);
            }
        }
        private void persistRingerMode() {
            System.putInt(mContentResolver, System.MODE_RINGER, mRingerMode);
        }
        private void persistVibrateSetting() {
            System.putInt(mContentResolver, System.VIBRATE_ON, mVibrateSetting);
        }
        private void playSoundEffect(int effectType, int volume) {
            synchronized (mSoundEffectsLock) {
                if (mSoundPool == null) {
                    return;
                }
                float volFloat;
                if (volume < 0) {
                    float dBPerStep = (float)((0.5 * 100) / MAX_STREAM_VOLUME[AudioSystem.STREAM_MUSIC]);
                    int musicVolIndex = (mStreamStates[AudioSystem.STREAM_MUSIC].mIndex + 5) / 10;
                    float musicVoldB = dBPerStep * (musicVolIndex - MAX_STREAM_VOLUME[AudioSystem.STREAM_MUSIC]);
                    volFloat = (float)Math.pow(10, (musicVoldB - 3)/20);
                } else {
                    volFloat = (float) volume / 1000.0f;
                }
                if (SOUND_EFFECT_FILES_MAP[effectType][1] > 0) {
                    mSoundPool.play(SOUND_EFFECT_FILES_MAP[effectType][1], volFloat, volFloat, 0, 0, 1.0f);
                } else {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    if (mediaPlayer != null) {
                        try {
                            String filePath = Environment.getRootDirectory() + SOUND_EFFECTS_PATH + SOUND_EFFECT_FILES[SOUND_EFFECT_FILES_MAP[effectType][0]];
                            mediaPlayer.setDataSource(filePath);
                            mediaPlayer.setAudioStreamType(AudioSystem.STREAM_SYSTEM);
                            mediaPlayer.prepare();
                            mediaPlayer.setVolume(volFloat, volFloat);
                            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    cleanupPlayer(mp);
                                }
                            });
                            mediaPlayer.setOnErrorListener(new OnErrorListener() {
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    cleanupPlayer(mp);
                                    return true;
                                }
                            });
                            mediaPlayer.start();
                        } catch (IOException ex) {
                            Log.w(TAG, "MediaPlayer IOException: "+ex);
                        } catch (IllegalArgumentException ex) {
                            Log.w(TAG, "MediaPlayer IllegalArgumentException: "+ex);
                        } catch (IllegalStateException ex) {
                            Log.w(TAG, "MediaPlayer IllegalStateException: "+ex);
                        }
                    }
                }
            }
        }
        private void cleanupPlayer(MediaPlayer mp) {
            if (mp != null) {
                try {
                    mp.stop();
                    mp.release();
                } catch (IllegalStateException ex) {
                    Log.w(TAG, "MediaPlayer IllegalStateException: "+ex);
                }
            }
        }
        @Override
        public void handleMessage(Message msg) {
            int baseMsgWhat = getMsgBase(msg.what);
            switch (baseMsgWhat) {
                case MSG_SET_SYSTEM_VOLUME:
                    setSystemVolume((VolumeStreamState) msg.obj);
                    break;
                case MSG_PERSIST_VOLUME:
                    persistVolume((VolumeStreamState) msg.obj, (msg.arg1 != 0), (msg.arg2 != 0));
                    break;
                case MSG_PERSIST_RINGER_MODE:
                    persistRingerMode();
                    break;
                case MSG_PERSIST_VIBRATE_SETTING:
                    persistVibrateSetting();
                    break;
                case MSG_MEDIA_SERVER_DIED:
                    if (!mMediaServerOk) {
                        Log.e(TAG, "Media server died.");
                        AudioSystem.isStreamActive(AudioSystem.STREAM_MUSIC);
                        sendMsg(mAudioHandler, MSG_MEDIA_SERVER_DIED, SHARED_MSG, SENDMSG_NOOP, 0, 0,
                                null, 500);
                    }
                    break;
                case MSG_MEDIA_SERVER_STARTED:
                    Log.e(TAG, "Media server started.");
                    Set set = mConnectedDevices.entrySet();
                    Iterator i = set.iterator();
                    while(i.hasNext()){
                        Map.Entry device = (Map.Entry)i.next();
                        AudioSystem.setDeviceConnectionState(((Integer)device.getKey()).intValue(),
                                                             AudioSystem.DEVICE_STATE_AVAILABLE,
                                                             (String)device.getValue());
                    }
                    AudioSystem.setPhoneState(mMode);
                    AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION, mForcedUseForComm);
                    AudioSystem.setForceUse(AudioSystem.FOR_RECORD, mForcedUseForComm);
                    int numStreamTypes = AudioSystem.getNumStreamTypes();
                    for (int streamType = numStreamTypes - 1; streamType >= 0; streamType--) {
                        int index;
                        VolumeStreamState streamState = mStreamStates[streamType];
                        AudioSystem.initStreamVolume(streamType, 0, (streamState.mIndexMax + 5) / 10);
                        if (streamState.muteCount() == 0) {
                            index = streamState.mIndex;
                        } else {
                            index = 0;
                        }
                        setStreamVolumeIndex(streamType, index);
                    }
                    setRingerModeInt(getRingerMode(), false);
                    break;
                case MSG_PLAY_SOUND_EFFECT:
                    playSoundEffect(msg.arg1, msg.arg2);
                    break;
                case MSG_BTA2DP_DOCK_TIMEOUT:
                    makeA2dpDeviceUnavailableNow( (String) msg.obj );
                    break;
            }
        }
    }
    private class SettingsObserver extends ContentObserver {
        SettingsObserver() {
            super(new Handler());
            mContentResolver.registerContentObserver(Settings.System.getUriFor(
                Settings.System.MODE_RINGER_STREAMS_AFFECTED), false, this);
            mContentResolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.NOTIFICATIONS_USE_RING_VOLUME), false, this);
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            synchronized (mSettingsLock) {
                int ringerModeAffectedStreams = Settings.System.getInt(mContentResolver,
                        Settings.System.MODE_RINGER_STREAMS_AFFECTED,
                        0);
                if (ringerModeAffectedStreams != mRingerModeAffectedStreams) {
                    mRingerModeAffectedStreams = ringerModeAffectedStreams;
                    setRingerModeInt(getRingerMode(), false);
                }
                int notificationsUseRingVolume = Settings.System.getInt(mContentResolver,
                        Settings.System.NOTIFICATIONS_USE_RING_VOLUME,
                        1);
                if (notificationsUseRingVolume != mNotificationsUseRingVolume) {
                    mNotificationsUseRingVolume = notificationsUseRingVolume;
                    if (mNotificationsUseRingVolume == 1) {
                        STREAM_VOLUME_ALIAS[AudioSystem.STREAM_NOTIFICATION] = AudioSystem.STREAM_RING;
                        mStreamStates[AudioSystem.STREAM_NOTIFICATION].setVolumeIndexSettingName(
                                System.VOLUME_SETTINGS[AudioSystem.STREAM_RING]);
                    } else {
                        STREAM_VOLUME_ALIAS[AudioSystem.STREAM_NOTIFICATION] = AudioSystem.STREAM_NOTIFICATION;
                        mStreamStates[AudioSystem.STREAM_NOTIFICATION].setVolumeIndexSettingName(
                                System.VOLUME_SETTINGS[AudioSystem.STREAM_NOTIFICATION]);
                        sendMsg(mAudioHandler, MSG_PERSIST_VOLUME, AudioSystem.STREAM_NOTIFICATION,
                                SENDMSG_REPLACE, 1, 1, mStreamStates[AudioSystem.STREAM_NOTIFICATION], 0);
                    }
                }
            }
        }
    }
    private void makeA2dpDeviceAvailable(String address) {
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP,
                AudioSystem.DEVICE_STATE_AVAILABLE,
                address);
        AudioSystem.setParameters("A2dpSuspended=false");
        mConnectedDevices.put( new Integer(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP),
                address);
    }
    private void makeA2dpDeviceUnavailableNow(String address) {
        Intent noisyIntent = new Intent(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        mContext.sendBroadcast(noisyIntent);
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP,
                AudioSystem.DEVICE_STATE_UNAVAILABLE,
                address);
        mConnectedDevices.remove(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP);
    }
    private void makeA2dpDeviceUnavailableLater(String address) {
        AudioSystem.setParameters("A2dpSuspended=true");
        mConnectedDevices.remove(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP);
        Message msg = mAudioHandler.obtainMessage(MSG_BTA2DP_DOCK_TIMEOUT, address);
        mAudioHandler.sendMessageDelayed(msg, BTA2DP_DOCK_TIMEOUT_MILLIS);
    }
    private void cancelA2dpDeviceTimeout() {
        mAudioHandler.removeMessages(MSG_BTA2DP_DOCK_TIMEOUT);
    }
    private boolean hasScheduledA2dpDockTimeout() {
        return mAudioHandler.hasMessages(MSG_BTA2DP_DOCK_TIMEOUT);
    }
    private String mDockAddress;
    private class AudioServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_DOCK_EVENT)) {
                int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,
                        Intent.EXTRA_DOCK_STATE_UNDOCKED);
                int config;
                switch (dockState) {
                    case Intent.EXTRA_DOCK_STATE_DESK:
                        config = AudioSystem.FORCE_BT_DESK_DOCK;
                        break;
                    case Intent.EXTRA_DOCK_STATE_CAR:
                        config = AudioSystem.FORCE_BT_CAR_DOCK;
                        break;
                    case Intent.EXTRA_DOCK_STATE_UNDOCKED:
                    default:
                        config = AudioSystem.FORCE_NONE;
                }
                AudioSystem.setForceUse(AudioSystem.FOR_DOCK, config);
            } else if (action.equals(BluetoothA2dp.ACTION_SINK_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_SINK_STATE,
                                               BluetoothA2dp.STATE_DISCONNECTED);
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = btDevice.getAddress();
                boolean isConnected = (mConnectedDevices.containsKey(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP) &&
                                       ((String)mConnectedDevices.get(AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP)).equals(address));
                if (isConnected &&
                    state != BluetoothA2dp.STATE_CONNECTED && state != BluetoothA2dp.STATE_PLAYING) {
                    if (btDevice.isBluetoothDock()) {
                        if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                            makeA2dpDeviceUnavailableLater(address);
                        }
                    } else {
                        makeA2dpDeviceUnavailableNow(address);
                    }
                } else if (!isConnected &&
                             (state == BluetoothA2dp.STATE_CONNECTED ||
                              state == BluetoothA2dp.STATE_PLAYING)) {
                    if (btDevice.isBluetoothDock()) {
                        cancelA2dpDeviceTimeout();
                        mDockAddress = address;
                    } else {
                        if(hasScheduledA2dpDockTimeout()) {
                            cancelA2dpDeviceTimeout();
                            makeA2dpDeviceUnavailableNow(mDockAddress);
                        }
                    }
                    makeA2dpDeviceAvailable(address);
                }
            } else if (action.equals(BluetoothHeadset.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE,
                                               BluetoothHeadset.STATE_ERROR);
                int device = AudioSystem.DEVICE_OUT_BLUETOOTH_SCO;
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String address = null;
                if (btDevice != null) {
                    address = btDevice.getAddress();
                    BluetoothClass btClass = btDevice.getBluetoothClass();
                    if (btClass != null) {
                        switch (btClass.getDeviceClass()) {
                        case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                        case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                            device = AudioSystem.DEVICE_OUT_BLUETOOTH_SCO_HEADSET;
                            break;
                        case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                            device = AudioSystem.DEVICE_OUT_BLUETOOTH_SCO_CARKIT;
                            break;
                        }
                    }
                }
                boolean isConnected = (mConnectedDevices.containsKey(device) &&
                                       ((String)mConnectedDevices.get(device)).equals(address));
                if (isConnected && state != BluetoothHeadset.STATE_CONNECTED) {
                    AudioSystem.setDeviceConnectionState(device,
                                                         AudioSystem.DEVICE_STATE_UNAVAILABLE,
                                                         address);
                    mConnectedDevices.remove(device);
                    mBluetoothHeadsetConnected = false;
                    clearAllScoClients();
                } else if (!isConnected && state == BluetoothHeadset.STATE_CONNECTED) {
                    AudioSystem.setDeviceConnectionState(device,
                                                         AudioSystem.DEVICE_STATE_AVAILABLE,
                                                         address);
                    mConnectedDevices.put(new Integer(device), address);
                    mBluetoothHeadsetConnected = true;
                }
            } else if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                int microphone = intent.getIntExtra("microphone", 0);
                if (microphone != 0) {
                    boolean isConnected = mConnectedDevices.containsKey(AudioSystem.DEVICE_OUT_WIRED_HEADSET);
                    if (state == 0 && isConnected) {
                        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_WIRED_HEADSET,
                                AudioSystem.DEVICE_STATE_UNAVAILABLE,
                                "");
                        mConnectedDevices.remove(AudioSystem.DEVICE_OUT_WIRED_HEADSET);
                    } else if (state == 1 && !isConnected)  {
                        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_WIRED_HEADSET,
                                AudioSystem.DEVICE_STATE_AVAILABLE,
                                "");
                        mConnectedDevices.put( new Integer(AudioSystem.DEVICE_OUT_WIRED_HEADSET), "");
                    }
                } else {
                    boolean isConnected = mConnectedDevices.containsKey(AudioSystem.DEVICE_OUT_WIRED_HEADPHONE);
                    if (state == 0 && isConnected) {
                        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_WIRED_HEADPHONE,
                                AudioSystem.DEVICE_STATE_UNAVAILABLE,
                                "");
                        mConnectedDevices.remove(AudioSystem.DEVICE_OUT_WIRED_HEADPHONE);
                    } else if (state == 1 && !isConnected)  {
                        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_WIRED_HEADPHONE,
                                AudioSystem.DEVICE_STATE_AVAILABLE,
                                "");
                        mConnectedDevices.put( new Integer(AudioSystem.DEVICE_OUT_WIRED_HEADPHONE), "");
                    }
                }
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_AUDIO_STATE,
                                               BluetoothHeadset.STATE_ERROR);
                synchronized (mScoClients) {
                    if (!mScoClients.isEmpty()) {
                        switch (state) {
                        case BluetoothHeadset.AUDIO_STATE_CONNECTED:
                            state = AudioManager.SCO_AUDIO_STATE_CONNECTED;
                            break;
                        case BluetoothHeadset.AUDIO_STATE_DISCONNECTED:
                            state = AudioManager.SCO_AUDIO_STATE_DISCONNECTED;
                            break;
                        default:
                            state = AudioManager.SCO_AUDIO_STATE_ERROR;
                            break;
                        }
                        if (state != AudioManager.SCO_AUDIO_STATE_ERROR) {
                            Intent newIntent = new Intent(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
                            newIntent.putExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, state);
                            mContext.sendStickyBroadcast(newIntent);
                        }
                    }
                }
            }
        }
    }
    private final static String IN_VOICE_COMM_FOCUS_ID = "AudioFocus_For_Phone_Ring_And_Calls";
    private final static Object mAudioFocusLock = new Object();
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                int ringVolume = AudioService.this.getStreamVolume(AudioManager.STREAM_RING);
                if (ringVolume > 0) {
                    requestAudioFocus(AudioManager.STREAM_RING,
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
                                null, null ,
                                IN_VOICE_COMM_FOCUS_ID );
                }
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                requestAudioFocus(AudioManager.STREAM_RING,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
                        null, null ,
                        IN_VOICE_COMM_FOCUS_ID );
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                abandonAudioFocus(null, IN_VOICE_COMM_FOCUS_ID);
            }
        }
    };
    private void notifyTopOfAudioFocusStack() {
        if (!mFocusStack.empty() && (mFocusStack.peek().mFocusDispatcher != null)) {
            if (canReassignAudioFocus()) {
                try {
                    mFocusStack.peek().mFocusDispatcher.dispatchAudioFocusChange(
                            AudioManager.AUDIOFOCUS_GAIN, mFocusStack.peek().mClientId);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failure to signal gain of audio control focus due to "+ e);
                    e.printStackTrace();
                }
            }
        }
    }
    private static class FocusStackEntry {
        public int mStreamType = -1;
        public boolean mIsTransportControlReceiver = false;
        public IAudioFocusDispatcher mFocusDispatcher = null;
        public IBinder mSourceRef = null;
        public String mClientId;
        public int mFocusChangeType;
        public FocusStackEntry() {
        }
        public FocusStackEntry(int streamType, int duration, boolean isTransportControlReceiver,
                IAudioFocusDispatcher afl, IBinder source, String id) {
            mStreamType = streamType;
            mIsTransportControlReceiver = isTransportControlReceiver;
            mFocusDispatcher = afl;
            mSourceRef = source;
            mClientId = id;
            mFocusChangeType = duration;
        }
    }
    private Stack<FocusStackEntry> mFocusStack = new Stack<FocusStackEntry>();
    private void dumpFocusStack(PrintWriter pw) {
        pw.println("\nAudio Focus stack entries:");
        synchronized(mAudioFocusLock) {
            Iterator<FocusStackEntry> stackIterator = mFocusStack.iterator();
            while(stackIterator.hasNext()) {
                FocusStackEntry fse = stackIterator.next();
                pw.println("     source:" + fse.mSourceRef + " -- client: " + fse.mClientId
                        + " -- duration: " +fse.mFocusChangeType);
            }
        }
    }
    private void removeFocusStackEntry(String clientToRemove, boolean signal) {
        if (!mFocusStack.empty() && mFocusStack.peek().mClientId.equals(clientToRemove))
        {
            mFocusStack.pop();
            if (signal) {
                notifyTopOfAudioFocusStack();
            }
        } else {
            Iterator<FocusStackEntry> stackIterator = mFocusStack.iterator();
            while(stackIterator.hasNext()) {
                FocusStackEntry fse = (FocusStackEntry)stackIterator.next();
                if(fse.mClientId.equals(clientToRemove)) {
                    Log.i(TAG, " AudioFocus  abandonAudioFocus(): removing entry for "
                            + fse.mClientId);
                    mFocusStack.remove(fse);
                }
            }
        }
    }
    private void removeFocusStackEntryForClient(IBinder cb) {
        boolean isTopOfStackForClientToRemove = !mFocusStack.isEmpty() &&
                mFocusStack.peek().mSourceRef.equals(cb);
        Iterator<FocusStackEntry> stackIterator = mFocusStack.iterator();
        while(stackIterator.hasNext()) {
            FocusStackEntry fse = (FocusStackEntry)stackIterator.next();
            if(fse.mSourceRef.equals(cb)) {
                Log.i(TAG, " AudioFocus  abandonAudioFocus(): removing entry for "
                        + fse.mClientId);
                mFocusStack.remove(fse);
            }
        }
        if (isTopOfStackForClientToRemove) {
            notifyTopOfAudioFocusStack();
        }
    }
    private boolean canReassignAudioFocus() {
        if (!mFocusStack.isEmpty() && IN_VOICE_COMM_FOCUS_ID.equals(mFocusStack.peek().mClientId)) {
            return false;
        }
        return true;
    }
    private class AudioFocusDeathHandler implements IBinder.DeathRecipient {
        private IBinder mCb; 
        AudioFocusDeathHandler(IBinder cb) {
            mCb = cb;
        }
        public void binderDied() {
            synchronized(mAudioFocusLock) {
                Log.w(TAG, "  AudioFocus   audio focus client died");
                removeFocusStackEntryForClient(mCb);
            }
        }
        public IBinder getBinder() {
            return mCb;
        }
    }
    public int requestAudioFocus(int mainStreamType, int focusChangeHint, IBinder cb,
            IAudioFocusDispatcher fd, String clientId) {
        Log.i(TAG, " AudioFocus  requestAudioFocus() from " + clientId);
        if (!IN_VOICE_COMM_FOCUS_ID.equals(clientId) && ((cb == null) || !cb.pingBinder())) {
            Log.i(TAG, " AudioFocus  DOA client for requestAudioFocus(), exiting");
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
        synchronized(mAudioFocusLock) {
            if (!canReassignAudioFocus()) {
                return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
            }
            if (!mFocusStack.empty() && mFocusStack.peek().mClientId.equals(clientId)) {
                if (mFocusStack.peek().mFocusChangeType == focusChangeHint) {
                    return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
                }
                mFocusStack.pop();
            }
            if (!mFocusStack.empty() && (mFocusStack.peek().mFocusDispatcher != null)) {
                try {
                    mFocusStack.peek().mFocusDispatcher.dispatchAudioFocusChange(
                            -1 * focusChangeHint, 
                            mFocusStack.peek().mClientId);
                } catch (RemoteException e) {
                    Log.e(TAG, " Failure to signal loss of focus due to "+ e);
                    e.printStackTrace();
                }
            }
            removeFocusStackEntry(clientId, false);
            mFocusStack.push(new FocusStackEntry(mainStreamType, focusChangeHint, false, fd, cb,
                    clientId));
        }
        if (!IN_VOICE_COMM_FOCUS_ID.equals(clientId)) {
            AudioFocusDeathHandler afdh = new AudioFocusDeathHandler(cb);
            try {
                cb.linkToDeath(afdh, 0);
            } catch (RemoteException e) {
                Log.w(TAG, "AudioFocus  requestAudioFocus() could not link to "+cb+" binder death");
            }
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
    public int abandonAudioFocus(IAudioFocusDispatcher fl, String clientId) {
        Log.i(TAG, " AudioFocus  abandonAudioFocus() from " + clientId);
        try {
            synchronized(mAudioFocusLock) {
                removeFocusStackEntry(clientId, true);
            }
        } catch (java.util.ConcurrentModificationException cme) {
            Log.e(TAG, "FATAL EXCEPTION AudioFocus  abandonAudioFocus() caused " + cme);
            cme.printStackTrace();
        }
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }
    public void unregisterAudioFocusClient(String clientId) {
        synchronized(mAudioFocusLock) {
            removeFocusStackEntry(clientId, false);
        }
    }
    private class MediaButtonBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!Intent.ACTION_MEDIA_BUTTON.equals(action)) {
                return;
            }
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event != null) {
                if ((getMode() == AudioSystem.MODE_IN_CALL) ||
                        (getMode() == AudioSystem.MODE_RINGTONE)) {
                    return;
                }
                synchronized(mRCStack) {
                    if (!mRCStack.empty()) {
                        Intent targetedIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                        targetedIntent.putExtras(intent.getExtras());
                        targetedIntent.setComponent(mRCStack.peek().mReceiverComponent);
                        abortBroadcast();
                        context.sendBroadcast(targetedIntent, null);
                    }
                }
            }
        }
    }
    private static class RemoteControlStackEntry {
        public ComponentName mReceiverComponent;
        public RemoteControlStackEntry() {
        }
        public RemoteControlStackEntry(ComponentName r) {
            mReceiverComponent = r;
        }
    }
    private Stack<RemoteControlStackEntry> mRCStack = new Stack<RemoteControlStackEntry>();
    private void dumpRCStack(PrintWriter pw) {
        pw.println("\nRemote Control stack entries:");
        synchronized(mRCStack) {
            Iterator<RemoteControlStackEntry> stackIterator = mRCStack.iterator();
            while(stackIterator.hasNext()) {
                RemoteControlStackEntry fse = stackIterator.next();
                pw.println("     receiver:" + fse.mReceiverComponent);
            }
        }
    }
    private void pushMediaButtonReceiver(ComponentName newReceiver) {
        if (!mRCStack.empty() && mRCStack.peek().mReceiverComponent.equals(newReceiver)) {
            return;
        }
        Iterator<RemoteControlStackEntry> stackIterator = mRCStack.iterator();
        while(stackIterator.hasNext()) {
            RemoteControlStackEntry rcse = (RemoteControlStackEntry)stackIterator.next();
            if(rcse.mReceiverComponent.equals(newReceiver)) {
                mRCStack.remove(rcse);
                break;
            }
        }
        mRCStack.push(new RemoteControlStackEntry(newReceiver));
    }
    private void removeMediaButtonReceiver(ComponentName newReceiver) {
        Iterator<RemoteControlStackEntry> stackIterator = mRCStack.iterator();
        while(stackIterator.hasNext()) {
            RemoteControlStackEntry rcse = (RemoteControlStackEntry)stackIterator.next();
            if(rcse.mReceiverComponent.equals(newReceiver)) {
                mRCStack.remove(rcse);
                break;
            }
        }
    }
    public void registerMediaButtonEventReceiver(ComponentName eventReceiver) {
        Log.i(TAG, "  Remote Control   registerMediaButtonEventReceiver() for " + eventReceiver);
        synchronized(mRCStack) {
            pushMediaButtonReceiver(eventReceiver);
        }
    }
    public void unregisterMediaButtonEventReceiver(ComponentName eventReceiver) {
        Log.i(TAG, "  Remote Control   unregisterMediaButtonEventReceiver() for " + eventReceiver);
        synchronized(mRCStack) {
            removeMediaButtonReceiver(eventReceiver);
        }
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        dumpFocusStack(pw);
        dumpRCStack(pw);
    }
}
