public class WarnOfStorageLimitsActivity extends Activity implements DialogInterface,
                    DialogInterface.OnClickListener {
    protected AlertController mAlert;
    protected AlertController.AlertParams mAlertParams;
    private static final String LOG_TAG = "WarnOfStorageLimitsActivity";
    private static final int POSITIVE_BUTTON = AlertDialog.BUTTON1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(com.android.internal.R.style.Theme_Dialog_Alert);
        super.onCreate(savedInstanceState);
        mAlert = new AlertController(this, this, getWindow());
        mAlertParams = new AlertController.AlertParams(this);
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.storage_limits_title);
        p.mMessage = getString(R.string.storage_limits_message);
        p.mPositiveButtonText = getString(R.string.storage_limits_setting);
        p.mNegativeButtonText = getString(R.string.storage_limits_setting_dismiss);
        p.mPositiveButtonListener = this;
        setupAlert();
    }
    public void onClick(DialogInterface dialog, int which) {
        if (which == POSITIVE_BUTTON) {
            Intent intent = new Intent(this,
                    MessagingPreferenceActivity.class);
            startActivity(intent);
        }
        finish();
    }
    public void cancel() {
        finish();
    }
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }
    protected void setupAlert() {
        mAlertParams.apply(mAlert);
        mAlert.installContent();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAlert.onKeyDown(keyCode, event)) return true;
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mAlert.onKeyUp(keyCode, event)) return true;
        return super.onKeyUp(keyCode, event);
    }
}
