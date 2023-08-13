public class MusicPlaybackStress extends ActivityInstrumentationTestCase <TrackBrowserActivity>{
    private static String TAG = "mediaplayertests";
    public MusicPlaybackStress() {
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
    @LargeTest
    public void testPlayAllSongs() {
      Activity mediaPlaybackActivity;
      try{
        Instrumentation inst = getInstrumentation();
        ActivityMonitor mediaPlaybackMon = inst.addMonitor("com.android.music.MediaPlaybackActivity", 
          null, false);
        inst.invokeMenuActionSync(getActivity(), MusicUtils.Defs.CHILD_MENU_BASE + 3, 0);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        mediaPlaybackActivity = mediaPlaybackMon.waitForActivityWithTimeout(2000);
        for (int i=0;i< MusicPlayerNames.NO_SKIPPING_SONGS;i++){               
          Thread.sleep(MusicPlayerNames.SKIP_WAIT_TIME);
          if (i==0){
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
          }
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);      
        }   
        mediaPlaybackActivity.finish();
      }catch (Exception e){
        Log.e(TAG, e.toString());
      }
      ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
      ((ActivityManager)getActivity().getSystemService("activity")).getMemoryInfo(mi);
      assertFalse(TAG, mi.lowMemory);      
    }
}
