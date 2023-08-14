public class StreamStarter extends Activity
{
    private ServiceToken mToken;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.streamstarter);
        TextView tv = (TextView) findViewById(R.id.streamloading);
        Uri uri = getIntent().getData();
        String msg = getString(R.string.streamloadingtext, uri.getHost());
        tv.setText(msg);
    }
    @Override
    public void onResume() {
        super.onResume();
        mToken = MusicUtils.bindToService(this, new ServiceConnection() {
            public void onServiceConnected(ComponentName classname, IBinder obj) {
                try {
                    IntentFilter f = new IntentFilter();
                    f.addAction(MediaPlaybackService.ASYNC_OPEN_COMPLETE);
                    f.addAction(MediaPlaybackService.PLAYBACK_COMPLETE);
                    registerReceiver(mStatusListener, new IntentFilter(f));
                    MusicUtils.sService.openFileAsync(getIntent().getData().toString());
                } catch (RemoteException ex) {
                }
            }
            public void onServiceDisconnected(ComponentName classname) {
            }
        });
    }
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MediaPlaybackService.PLAYBACK_COMPLETE)) {
                String msg = getString(R.string.fail_to_start_stream);
                Toast mt = Toast.makeText(StreamStarter.this, msg, Toast.LENGTH_SHORT);
                mt.show();
                finish();
                return;
            }
            try {
                MusicUtils.sService.play();
                intent = new Intent("com.android.music.PLAYBACK_VIEWER");
                intent.putExtra("oneshot", true);
                startActivity(intent);
            } catch (RemoteException ex) {
            }
            finish();
        }
    };
    @Override
    public void onPause() {
        if (MusicUtils.sService != null) {
            try {
                if (! MusicUtils.sService.isPlaying()) {
                    MusicUtils.sService.stop();
                }
            } catch (RemoteException ex) {
            }
        }
        unregisterReceiver(mStatusListener);
        MusicUtils.unbindFromService(mToken);
        super.onPause();
    }
}
