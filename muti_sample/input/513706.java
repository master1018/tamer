public class AudioPermissionTest extends AndroidTestCase {
    static String PATH_PREFIX = Environment.getExternalStorageDirectory().toString();
    static String AUDIO_CAPTURE_PATH = PATH_PREFIX + "this-should-not-exist.amr";
    static int BEAUTY_SLEEP_INTERVAL = 5 * 1000;
    MediaPlayer mMediaPlayer = null;
    MediaRecorder mMediaRecorder = null;
    boolean mRecorded = false;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMediaRecorder = new MediaRecorder();
    }
    @LargeTest
    void testMicrophoneRecording() {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(AUDIO_CAPTURE_PATH);
        try {
            mMediaRecorder.prepare();
        }
        catch (SecurityException e) {
            return;
        } catch (Exception e) {
            fail("Could not prepare MediaRecorder: " + e.toString());
        }
        try {
            mMediaRecorder.start();
        } catch (SecurityException e) {
            return;
        }
        try {
            Thread.sleep(BEAUTY_SLEEP_INTERVAL);
        } catch (InterruptedException e) {
        }
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mRecorded = true;
            fail("Recorded from MediaRecorder.AudioSource.MIC");
        } catch (SecurityException e) {
            mRecorded = false;
        }
    }
    void doRemoteMp3(Uri uri) {
        try {
            MediaPlayer plyr = new MediaPlayer();
            plyr.setDataSource(mContext, uri);
            plyr.setAudioStreamType(AudioManager.STREAM_MUSIC);
            plyr.prepare();
            plyr.seekTo(1000);    
            plyr.start();
            Thread.sleep(BEAUTY_SLEEP_INTERVAL / 10);
            plyr.stop();
            fail("We just downloaded a song off the Internet with no permissions, and uploaded arbitrary data in the query string");
            plyr.release();
        } catch (SecurityException e) {
        } catch (Exception e) {
            fail("Got further than we should have trying to load a remote media source");
        }
    }
    @LargeTest
    void testRemoteMp3() {
        doRemoteMp3(Uri.parse("http:
    }
}
