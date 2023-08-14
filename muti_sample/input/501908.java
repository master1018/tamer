public class AlbumsPlaybackStress extends ActivityInstrumentationTestCase <AlbumBrowserActivity>{
  private Activity browseActivity;
  private String[] testing;
  private String TAG = "AlbumsPlaybackStress";
  public AlbumsPlaybackStress() {
      super("com.android.music",AlbumBrowserActivity.class);
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
    public void testAlbumPlay() { 
      Instrumentation inst = getInstrumentation();
      try{
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);     
        for(int i=0; i< MusicPlayerNames.NO_ALBUMS_TOBE_PLAYED; i++){
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
          Thread.sleep(MusicPlayerNames.WAIT_LONG_TIME);
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
          inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);       
        } 
      }catch (Exception e){
          Log.v(TAG, e.toString());
      }
      inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
      ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
      ((ActivityManager)getActivity().getSystemService("activity")).getMemoryInfo(mi);
      assertFalse(TAG, mi.lowMemory); 
  }
}
