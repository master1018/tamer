public class ScanningProgress extends Activity
{
    private final static int CHECK = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == CHECK) {
                String status = Environment.getExternalStorageState();
                if (!status.equals(Environment.MEDIA_MOUNTED)) {
                    finish();
                    return;
                }
                Cursor c = MusicUtils.query(ScanningProgress.this,
                        MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        null, null, null, null);
                if (c != null) {
                    c.close();
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                Message next = obtainMessage(CHECK);
                sendMessageDelayed(next, 3000);
            }
        }
    };
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.scanning);
        getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);
        setResult(RESULT_CANCELED);
        Message msg = mHandler.obtainMessage(CHECK);
        mHandler.sendMessageDelayed(msg, 1000);
    }
    @Override
    public void onDestroy() {
        mHandler.removeMessages(CHECK);
        super.onDestroy();
    }
}
