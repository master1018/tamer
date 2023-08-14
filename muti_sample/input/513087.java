public class TestSongs extends ActivityInstrumentationTestCase <TrackBrowserActivity>{
    private static String TAG = "musicplayertests";
    public TestSongs() {
        super("com.android.music",TrackBrowserActivity.class);
    }
    @Override 
    protected void setUp() throws Exception {   
        super.setUp(); 
    }
    @Override 
    protected void tearDown() throws Exception {   
        super.tearDown();           
    }
    public void addNewPlaylist() throws Exception{
      Instrumentation inst = getInstrumentation();      
      for (int i=0; i< MusicPlayerNames.NO_OF_PLAYLIST; i++){
        inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.NEW_PLAYLIST, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        for (int j=0; j< MusicPlayerNames.DEFAULT_PLAYLIST_LENGTH; j++)
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
        inst.sendStringSync(MusicPlayerNames.unsortedPlaylistTitle[i]);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
      }
    }
    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        Log.v(TAG, "Copy file");
      }
      private void rescanSdcard() throws Exception{     
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file:
             + Environment.getExternalStorageDirectory()));    
        Log.v(TAG,"start the intent");
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addDataScheme("file");     
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file:
            + Environment.getExternalStorageDirectory())));    
          Thread.sleep(MusicPlayerNames.WAIT_VERY_LONG_TIME);
      }
    @LargeTest
    public void testAddPlaylist() throws Exception{
      Cursor mCursor;
      addNewPlaylist();
      String[] cols = new String[] {
          MediaStore.Audio.Playlists.NAME
      };
      ContentResolver resolver = getActivity().getContentResolver();
      if (resolver == null) {
        System.out.println("resolver = null");
      } else {
        String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
        mCursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
          cols, whereclause, null,
          MediaStore.Audio.Playlists.NAME);
        mCursor.moveToFirst();
        for (int j=0;j<10;j++){
          assertEquals("New sorted Playlist title:", MusicPlayerNames.expectedPlaylistTitle[j], mCursor.getString(0)); 
          mCursor.moveToNext();
        }
      }
    }   
    @LargeTest
    public void testSetRingtone() throws Exception{
      Cursor mCursor;
      Instrumentation inst = getInstrumentation();      
      inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.USE_AS_RINGTONE, 0);
      ContentResolver resolver = getActivity().getContentResolver();
      if (resolver == null) {
        System.out.println("resolver = null");
      } else {
        String whereclause = MediaStore.Audio.Media.IS_RINGTONE + " = 1";
        mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
           null, whereclause, null, null);
        mCursor.moveToFirst();
        int isRingtoneSet = mCursor.getCount();
        assertEquals(TAG, MusicPlayerNames.EXPECTED_NO_RINGTONE, isRingtoneSet);
      }
    }
    @LargeTest
    public void testDeleteSong() throws Exception{
      Instrumentation inst = getInstrumentation();      
      Cursor mCursor;
      Log.v(TAG, "Copy a temp file to the sdcard");
      File goldenfile = new File(MusicPlayerNames.GOLDENSONG);
      File toBeDeleteSong = new File(MusicPlayerNames.DELETESONG);
      copy(goldenfile, toBeDeleteSong);
      rescanSdcard();
      Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
      inst.sendStringSync(MusicPlayerNames.TOBEDELETESONGNAME);
      Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
      inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.DELETE_ITEM, 0);
      inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
      inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
      Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
      for (int j=0; j< MusicPlayerNames.TOBEDELETESONGNAME.length(); j++)
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
      File checkDeletedFile = new File(MusicPlayerNames.DELETESONG);
      assertFalse(TAG, checkDeletedFile.exists());
      ContentResolver resolver = getActivity().getContentResolver();
      if (resolver == null) {
        System.out.println("resolver = null");
      } else {
        String whereclause = MediaStore.Audio.Media.DISPLAY_NAME + " = '" + 
        MusicPlayerNames.TOBEDELETESONGNAME + "'";
        mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
           null, whereclause, null, null);
        boolean isEmptyCursor = mCursor.moveToFirst();
        assertFalse(TAG,isEmptyCursor);
      }     
    } 
}
