public abstract class KeyguardViewBase extends FrameLayout {
    private KeyguardViewCallback mCallback;
    private AudioManager mAudioManager;
    private TelephonyManager mTelephonyManager = null;
    public KeyguardViewBase(Context context) {
        super(context);
        mForegroundInPadding = false;
        setForegroundGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP);
        setForeground(
                context.getResources().getDrawable(
                        com.android.internal.R.drawable.title_bar_shadow));
    }
    void setCallback(KeyguardViewCallback callback) {
        mCallback = callback;
    }
    public KeyguardViewCallback getCallback() {
        return mCallback;
    }
    abstract public void reset();
    abstract public void onScreenTurnedOff();
    abstract public void onScreenTurnedOn();
    abstract public void wakeWhenReadyTq(int keyCode);
    abstract public void verifyUnlock();
    abstract public void cleanUp();
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (shouldEventKeepScreenOnWhileKeyguardShowing(event)) {
            mCallback.pokeWakelock();
        }
        if (interceptMediaKey(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private boolean shouldEventKeepScreenOnWhileKeyguardShowing(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
                return false;
            default:
                return true;
        }
    }
    private boolean interceptMediaKey(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (mTelephonyManager == null) {
                        mTelephonyManager = (TelephonyManager) getContext().getSystemService(
                                Context.TELEPHONY_SERVICE);
                    }
                    if (mTelephonyManager != null &&
                            mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                        return true;  
                    }
                case KeyEvent.KEYCODE_HEADSETHOOK: 
                case KeyEvent.KEYCODE_MEDIA_STOP: 
                case KeyEvent.KEYCODE_MEDIA_NEXT: 
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS: 
                case KeyEvent.KEYCODE_MEDIA_REWIND: 
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                    getContext().sendOrderedBroadcast(intent, null);
                    return true;
                }
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN: {
                    synchronized (this) {
                        if (mAudioManager == null) {
                            mAudioManager = (AudioManager) getContext().getSystemService(
                                    Context.AUDIO_SERVICE);
                        }
                    }
                    if (mAudioManager.isMusicActive()) {
                        mAudioManager.adjustStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    keyCode == KeyEvent.KEYCODE_VOLUME_UP
                                            ? AudioManager.ADJUST_RAISE
                                            : AudioManager.ADJUST_LOWER,
                                    0);
                    }
                    return true;
                }
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MUTE:
                case KeyEvent.KEYCODE_HEADSETHOOK: 
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: 
                case KeyEvent.KEYCODE_MEDIA_STOP: 
                case KeyEvent.KEYCODE_MEDIA_NEXT: 
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS: 
                case KeyEvent.KEYCODE_MEDIA_REWIND: 
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                    getContext().sendOrderedBroadcast(intent, null);
                    return true;
                }
            }
        }
        return false;
    }
}
