public class MediaPlayerStressTest extends ActivityInstrumentationTestCase2<MediaFrameworkTest> {    
    private String TAG = "MediaPlayerStressTest";
    private MediaRecorder mRecorder;
    private Camera mCamera;
    private static final int NUMBER_OF_RANDOM_REPOSITION_AND_PLAY = 10;
    private static final int NUMBER_OF_RANDOM_REPOSITION_AND_PLAY_SHORT = 5;
    private static final int NUMBER_OF_STRESS_LOOPS = 500;
    private static final int PLAYBACK_END_TOLERANCE = 30000;
    private static final int WAIT_UNTIL_PLAYBACK_FINISH = 515000 ;
    public MediaPlayerStressTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
    }
    protected void setUp() throws Exception {
        getActivity();
        super.setUp();
    }
    @LargeTest
    public void testStressHWDecoderRelease() throws Exception {
        SurfaceHolder mSurfaceHolder;
        long randomseed = System.currentTimeMillis(); 
        Random generator = new Random(randomseed);
        Log.v(TAG, "Random seed: " + randomseed);
        int video_duration = MediaNames.STREAM_H264_480_360_1411k_DURATION;
        int random_play_time;
        mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
        try {
            assertTrue(MediaFrameworkTest.checkStreamingServer());
            for (int i = 0; i < NUMBER_OF_STRESS_LOOPS; i++) {
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(MediaNames.STREAM_H264_480_360_1411k);
                mp.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
                mp.prepare();
                mp.start();
                for (int j = 0; j < generator.nextInt(10); j++) {
                    random_play_time =
                        generator.nextInt(MediaNames.STREAM_H264_480_360_1411k_DURATION / 2);
                    Log.v(TAG, "Play time = " + random_play_time);
                    Thread.sleep(random_play_time);
                    int seek_time = MediaNames.STREAM_H264_480_360_1411k_DURATION / 2;
                    Log.v(TAG, "Seek time = " + seek_time);
                    mp.seekTo(seek_time);
                }
                mp.release();
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            assertTrue("testStressHWDecoderRelease", false);
        }
    }
    @LargeTest
    public void testStressGetCurrentPosition() throws Exception {
        SurfaceHolder mSurfaceHolder;
        long randomseed = System.currentTimeMillis(); 
        Random generator = new Random(randomseed);
        Log.v(TAG, "Random seed: " + randomseed);
        int video_duration = MediaNames.VIDEO_H263_AMR_DURATION;
        int random_play_time = 0;
        int random_seek_time = 0;
        int random_no_of_seek = 0;
        mSurfaceHolder = MediaFrameworkTest.mSurfaceView.getHolder();
        try {
            for (int i = 0; i < NUMBER_OF_STRESS_LOOPS; i++) {
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(MediaNames.VIDEO_H263_AMR);
                mp.setDisplay(MediaFrameworkTest.mSurfaceView.getHolder());
                mp.prepare();
                mp.start();
                random_no_of_seek = generator.nextInt(10);
                if (random_no_of_seek == 0) {
                    random_no_of_seek = 1;
                }
                Log.v(TAG, "random_seek = " + random_no_of_seek);
                for (int j = 0; j < random_no_of_seek; j++) {
                    random_play_time =
                        generator.nextInt(video_duration / 100);
                    Log.v(TAG, "Play time = " + random_play_time);
                    Thread.sleep(random_play_time);
                    random_seek_time =
                        generator.nextInt(video_duration / 2);
                    Log.v(TAG, "Seek time = " + random_seek_time);
                    mp.seekTo(random_seek_time);
                }
                mp.seekTo(video_duration - 10000);
                Thread.sleep(PLAYBACK_END_TOLERANCE);
                Log.v(TAG, "CurrentPosition = " + mp.getCurrentPosition());
                if ( mp.isPlaying() || mp.getCurrentPosition()
                        > (video_duration)){
                    assertTrue("Current PlayTime greater than duration", false);
                }
                mp.release();
            }
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            assertTrue("testStressGetCurrentPosition", false);
        }
    }
}
