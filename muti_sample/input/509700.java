public class MediaFrameworkTest extends Activity {
    public static SurfaceView mSurfaceView;
    private MediaController mMediaController;
    private String urlpath;
    private MediaPlayer mpmidi;
    private MediaPlayer mpmp3;
    private String testfilepath = "/sdcard/awb.awb";
    public static AssetFileDescriptor midiafd;
    public static AssetFileDescriptor mp3afd;
    public MediaFrameworkTest() {
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.surface_view);
        mSurfaceView = (SurfaceView)findViewById(R.id.surface_view);
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        midiafd = this.getResources().openRawResourceFd(R.raw.testmidi);
        mp3afd = this.getResources().openRawResourceFd(R.raw.testmp3);
    }
    public void startPlayback(String filename){
      String mimetype = "audio/mpeg";
      Uri path = Uri.parse(filename);
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(path, mimetype);
      startActivity(intent);
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
      switch (keyCode) {
          case KeyEvent.KEYCODE_0:
            MediaPlayer mp = new MediaPlayer();
            try{
              mp.setDataSource(MediaNames.VIDEO_RTSP3GP);
              Log.v("emily","awb  " + testfilepath);
              mp.setDisplay(mSurfaceView.getHolder());
              mp.prepare();
              mp.start();
            }catch (Exception e){}
              break;
          case KeyEvent.KEYCODE_1:
            startPlayback(MediaNames.STREAM_MP3_1);
            break;
          case KeyEvent.KEYCODE_2:
            startPlayback(MediaNames.STREAM_MP3_2);
            break;
          case KeyEvent.KEYCODE_3:
            startPlayback(MediaNames.STREAM_MP3_3);
            break;
          case KeyEvent.KEYCODE_4:
            startPlayback(MediaNames.STREAM_MP3_4);
            break;
          case KeyEvent.KEYCODE_5:
            startPlayback(MediaNames.STREAM_MP3_5);
            break;
          case KeyEvent.KEYCODE_6:
            startPlayback(MediaNames.STREAM_MP3_6);
            break;
          case KeyEvent.KEYCODE_7:
            startPlayback(MediaNames.STREAM_MP3_7);
            break;
          case KeyEvent.KEYCODE_8:
            startPlayback(MediaNames.STREAM_MP3_8);
            break;
          case KeyEvent.KEYCODE_9:
            startPlayback(MediaNames.STREAM_MP3_9);
            break;
      }
      return super.onKeyDown(keyCode, event);
  }  
  public static boolean checkStreamingServer() throws Exception {
      InetAddress address = InetAddress.getByAddress(MediaNames.STREAM_SERVER);
      return address.isReachable(10000);
  }
}
