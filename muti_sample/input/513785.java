public class ExternalMediaFormatActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private static final int POSITIVE_BUTTON = AlertDialog.BUTTON1;
    private BroadcastReceiver mStorageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("ExternalMediaFormatActivity", "got action " + action);
            if (action == Intent.ACTION_MEDIA_REMOVED ||
                action == Intent.ACTION_MEDIA_CHECKING ||
                action == Intent.ACTION_MEDIA_MOUNTED ||
                action == Intent.ACTION_MEDIA_SHARED) {
                finish();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ExternalMediaFormatActivity", "onCreate!");
        final AlertController.AlertParams p = mAlertParams;
        p.mIconId = com.android.internal.R.drawable.stat_sys_warning;
        p.mTitle = getString(com.android.internal.R.string.extmedia_format_title);
        p.mMessage = getString(com.android.internal.R.string.extmedia_format_message);
        p.mPositiveButtonText = getString(com.android.internal.R.string.extmedia_format_button_format);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(com.android.internal.R.string.cancel);
        p.mNegativeButtonListener = this;
        setupAlert();
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        registerReceiver(mStorageReceiver, filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mStorageReceiver);
    }
    public void onClick(DialogInterface dialog, int which) {
        if (which == POSITIVE_BUTTON) {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                .getService("mount"));
            if (mountService != null) {
                try {
                    mountService.formatVolume(Environment.getExternalStorageDirectory().toString());
                } catch (RemoteException e) {
                }
            }
        }
        finish();
    }
}
