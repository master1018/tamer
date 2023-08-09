public class AudioManagerStub extends Activity {
    private final int MP3_TO_PLAY = R.raw.testmp3;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private static CTSResult mCTSResult;
    public static void setCTSResult(CTSResult cr) {
        mCTSResult = cr;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = MediaPlayer.create(this, MP3_TO_PLAY);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            for (int i = 0; i < AudioSystem.getNumStreamTypes(); i++) {
                mAudioManager.setStreamMute(i, false);
                mAudioManager.setStreamSolo(i, false);
            }
        } catch (Exception e) {
            mCTSResult.setResult(CTSResult.RESULT_FAIL);
            finish();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            for (int i = 0; i < AudioSystem.getNumStreamTypes(); i++) {
                mAudioManager.setStreamMute(i, true);
                mAudioManager.setStreamSolo(i, true);
            }
        } catch (Exception e) {
            mCTSResult.setResult(CTSResult.RESULT_FAIL);
            finish();
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, AudioManagerStubHelper.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mCTSResult.setResult(CTSResult.RESULT_OK);
        finish();
    }
}
