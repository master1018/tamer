public class TonePlayer {
    private static final HashMap<Tone, Integer> mToneMap = new HashMap<Tone, Integer>();
    static {
        mToneMap.put(Tone.DIAL, ToneGenerator.TONE_SUP_DIAL);
        mToneMap.put(Tone.BUSY, ToneGenerator.TONE_SUP_BUSY);
        mToneMap.put(Tone.CONGESTION, ToneGenerator.TONE_SUP_CONGESTION);
        mToneMap.put(Tone.RADIO_PATH_ACK, ToneGenerator.TONE_SUP_RADIO_ACK);
        mToneMap.put(Tone.RADIO_PATH_NOT_AVAILABLE, ToneGenerator.TONE_SUP_RADIO_NOTAVAIL);
        mToneMap.put(Tone.ERROR_SPECIAL_INFO, ToneGenerator.TONE_SUP_ERROR);
        mToneMap.put(Tone.CALL_WAITING, ToneGenerator.TONE_SUP_CALL_WAITING);
        mToneMap.put(Tone.RINGING, ToneGenerator.TONE_SUP_RINGTONE);
        mToneMap.put(Tone.GENERAL_BEEP, ToneGenerator.TONE_PROP_BEEP);
        mToneMap.put(Tone.POSITIVE_ACK, ToneGenerator.TONE_PROP_ACK);
        mToneMap.put(Tone.NEGATIVE_ACK, ToneGenerator.TONE_PROP_NACK);
    }
    private ToneGenerator mToneGenerator = null;
    TonePlayer() {
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
    }
    public void play(Tone tone) {
        int toneId = getToneId(tone);
        if (toneId > 0 && mToneGenerator != null) {
            mToneGenerator.startTone(toneId);
        }
    }
    public void stop() {
        if (mToneGenerator != null) {
            mToneGenerator.stopTone();
        }
    }
    public void release() {
        mToneGenerator.release();
    }
    private int getToneId(Tone tone) {
        int toneId = ToneGenerator.TONE_PROP_BEEP;
        if (tone != null && mToneMap.containsKey(tone)) {
            toneId = mToneMap.get(tone);
        }
        return toneId;
    }
}
