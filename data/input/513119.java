public class MediaMimeTest extends ActivityInstrumentationTestCase2<MediaFrameworkTest> {    
    private final String TAG = "MediaMimeTest";
    private Context mContext;
    private final String MP3_FILE = "/sdcard/media_api/music/SHORTMP3.mp3";
    public MediaMimeTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
    }
    @Override
    protected void setUp() throws Exception {
      super.setUp();
      mContext = getActivity();
      assertTrue(new File(MP3_FILE).exists());
    }
    @Override 
    protected void tearDown() throws Exception {     
        super.tearDown();              
    }
    @MediumTest
    public void testCheckMediaPlaybackHandlesAudioMp3() throws Exception {
        assertMediaPlaybackActivityHandles("audio/mp3");
    }
    @Suppress
    public void testCheckMediaPlaybackHandlesAudio() throws Exception {
        assertMediaPlaybackActivityHandles("audio/*");
    }
    public void testCheckMediaPlaybackHandlesApplicationItunes() throws Exception {
        assertMediaPlaybackActivityHandles("application/itunes");
    }
    @MediumTest
    public void testCheckActivityResolverMimeHandlingIsCaseSensitive() throws Exception {
        assertNoActivityHandles("AUDIO/MP3");   
    }
    @MediumTest
    public void testCheckWhiteSpacesInMimeTypeHandling() throws Exception {
        assertNoActivityHandles(" audio/mp3");
        assertNoActivityHandles(" audio/mp3 ");
        assertMediaPlaybackActivityHandles("audio/mp3 ");
    }
    private ResolveInfo resolveMime(String mime) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromParts("file", MP3_FILE, null);
        viewIntent.setDataAndType(uri, mime);
        return mContext.getPackageManager().resolveActivity(
                viewIntent, PackageManager.MATCH_DEFAULT_ONLY);
    }
    private void assertMediaPlaybackActivityHandles(String mime) throws Exception {
        ResolveInfo ri = resolveMime(mime);
        assertNotNull(ri);
    }
    private void assertNoActivityHandles(String mime) throws Exception {
        assertNull(resolveMime(mime));
    }
}
