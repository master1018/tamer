public class MediaScannerActivity extends Activity
{
    public MediaScannerActivity() {
    }
    @Override
    public void onResume() {
        super.onResume();
        setContentView(R.layout.media_scanner_activity);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file:
                + Environment.getExternalStorageDirectory())));
        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setText("Sent ACTION_MEDIA_MOUNTED to trigger the Media Scanner.");
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                mTitle.setText("Media Scanner started scanning " + intent.getData().getPath());     
            }
            else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                mTitle.setText("Media Scanner finished scanning " + intent.getData().getPath());     
            }
        }
    };
    private TextView mTitle;
}
