public class MediaPlayerInvokeTest extends ActivityInstrumentationTestCase2<MediaFrameworkTest> {
   private static final String TAG = "MediaPlayerInvokeTest";
   private MediaPlayer mPlayer;
   private Random rnd;
   public MediaPlayerInvokeTest() {
       super("com.android.mediaframeworktest", MediaFrameworkTest.class);
       rnd = new Random(Calendar.getInstance().getTimeInMillis());
    }
    @Override
    protected void setUp() throws Exception {
      super.setUp();
      mPlayer = new MediaPlayer();
    }
    @Override
    protected void tearDown() throws Exception {
        mPlayer.release();
        super.tearDown();
    }
    @Suppress
    @MediumTest
    public void testPing() throws Exception {
        mPlayer.setDataSource("test:invoke_mock_media_player.so?url=ping");
        Parcel request = mPlayer.newRequest();
        Parcel reply = Parcel.obtain();
        int val = rnd.nextInt();
        request.writeInt(val);
        assertEquals(0, mPlayer.invoke(request, reply));
        assertEquals(val, reply.readInt());
   }
}
