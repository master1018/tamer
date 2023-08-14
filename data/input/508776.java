public class TestPlaylist extends ActivityInstrumentationTestCase <PlaylistBrowserActivity>{
    private static String TAG = "musicplayertests";
    public TestPlaylist() {
        super("com.android.music",PlaylistBrowserActivity.class);
    }
    @Override 
    protected void setUp() throws Exception {   
        super.setUp(); 
    }
    @Override 
    protected void tearDown() throws Exception {   
        super.tearDown();           
    }
    private void clearSearchString(int length){
        Instrumentation inst = getInstrumentation();
        for (int j=0; j< length; j++)
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
    }
    public void deletePlaylist(String playlistname) throws Exception{
        Instrumentation inst = getInstrumentation();
        inst.sendStringSync(playlistname);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);       
        inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.CHILD_MENU_BASE + 1, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        clearSearchString(playlistname.length());
    }
    public void addNewPlaylist(String playListName) throws Exception{
        Instrumentation inst = getInstrumentation();
        Activity trackBrowserActivity;
        ActivityMonitor trackBrowserMon = inst.addMonitor("com.android.music.TrackBrowserActivity", 
                null, false);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setClassName("com.android.music", "com.android.music.TrackBrowserActivity");
        getActivity().startActivity(intent);     
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        trackBrowserActivity = trackBrowserMon.waitForActivityWithTimeout(2000);
        inst.invokeContextMenuAction(trackBrowserActivity, MusicUtils.Defs.NEW_PLAYLIST, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        clearSearchString(MusicPlayerNames.DEFAULT_PLAYLIST_LENGTH);
        inst.sendStringSync(playListName);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        trackBrowserActivity.finish();
        clearSearchString(playListName.length());
    }
    public void renamePlaylist(String oldPlaylistName, String newPlaylistName) throws Exception{
        Instrumentation inst = getInstrumentation();
        inst.sendStringSync(oldPlaylistName);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);       
        inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.CHILD_MENU_BASE + 3, 0);
        Thread.sleep(MusicPlayerNames.WAIT_SHORT_TIME);
        clearSearchString(oldPlaylistName.length());
        inst.sendStringSync(newPlaylistName);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        clearSearchString(oldPlaylistName.length());
    }
    public boolean verifyPlaylist(String playlistname) throws Exception{
        Cursor mCursor;
        boolean isEmptyPlaylist = true;
        String[] cols = new String[] {
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
            assertNull(TAG, resolver);
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " = '" + playlistname +"'";
            mCursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            isEmptyPlaylist = mCursor.moveToFirst();
        }
        return isEmptyPlaylist;
    }
    @LargeTest
    public void testDeletePlaylist() throws Exception{
        boolean isEmptyPlaylist = true;
        addNewPlaylist(MusicPlayerNames.DELETE_PLAYLIST_NAME);
        deletePlaylist(MusicPlayerNames.DELETE_PLAYLIST_NAME);  
        isEmptyPlaylist = verifyPlaylist(MusicPlayerNames.DELETE_PLAYLIST_NAME);
        assertFalse("testDeletePlaylist", isEmptyPlaylist);
    }
    @LargeTest
    public void testRenamePlaylist() throws Exception{
        boolean isEmptyPlaylist = true;
        addNewPlaylist(MusicPlayerNames.ORIGINAL_PLAYLIST_NAME);
        renamePlaylist(MusicPlayerNames.ORIGINAL_PLAYLIST_NAME, MusicPlayerNames.RENAMED_PLAYLIST_NAME);  
        isEmptyPlaylist = verifyPlaylist(MusicPlayerNames.RENAMED_PLAYLIST_NAME);
        deletePlaylist(MusicPlayerNames.RENAMED_PLAYLIST_NAME);
        assertTrue("testDeletePlaylist", isEmptyPlaylist);
    }
}    
