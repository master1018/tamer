public class UninstallerActivity extends Activity implements OnClickListener,
        DialogInterface.OnCancelListener {
    private static final String TAG = "UninstallerActivity";
    private boolean localLOGV = false;
    PackageManager mPm;
    private ApplicationInfo mAppInfo;
    private Button mOk;
    private Button mCancel;
    private static final int DLG_BASE = 0;
    private static final int DLG_APP_NOT_FOUND = DLG_BASE + 1;
    private static final int DLG_UNINSTALL_FAILED = DLG_BASE + 2;
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case DLG_APP_NOT_FOUND :
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.app_not_found_dlg_title)
                    .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.app_not_found_dlg_text)
                    .setNeutralButton(getString(R.string.dlg_ok), 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }})
                    .create();
        case DLG_UNINSTALL_FAILED :
           CharSequence appTitle = mPm.getApplicationLabel(mAppInfo);
           String dlgText = getString(R.string.uninstall_failed_msg,
                    appTitle.toString());
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.uninstall_failed)
                    .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                    .setMessage(dlgText)
                    .setNeutralButton(getString(R.string.dlg_ok), 
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }})
                    .create();
        }
        return null;
    }
    private void startUninstallProgress() {
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO, 
                                                  mAppInfo);
        newIntent.setClass(this, UninstallAppProgress.class);
        startActivity(newIntent);
        finish();
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Intent intent = getIntent();
        Uri packageURI = intent.getData();
        String packageName = packageURI.getEncodedSchemeSpecificPart();
        if(packageName == null) {
            Log.e(TAG, "Invalid package name:"+packageName);
            showDialog(DLG_APP_NOT_FOUND);
            return;
        }
        mPm = getPackageManager();
        boolean errFlag = false;
        try {
            mAppInfo = mPm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            errFlag = true;
        }
        if(mAppInfo == null || errFlag) {
            Log.e(TAG, "Invalid application:"+packageName);
            showDialog(DLG_APP_NOT_FOUND);
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.uninstall_confirm);
            TextView question = (TextView) findViewById(R.id.uninstall_question);
            TextView confirm = (TextView) findViewById(R.id.uninstall_confirm_text);
            if ((mAppInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                question.setText(R.string.uninstall_update_question);
                confirm.setText(R.string.uninstall_update_text);
            } else {
                question.setText(R.string.uninstall_application_question);
                confirm.setText(R.string.uninstall_application_text);
            }
            PackageUtil.initSnippetForInstalledApp(this, mAppInfo, R.id.app_snippet);
            mOk = (Button)findViewById(R.id.ok_button);
            mCancel = (Button)findViewById(R.id.cancel_button);
            mOk.setOnClickListener(this);
            mCancel.setOnClickListener(this);
        }
    }
    public void onClick(View v) {
        if(v == mOk) {
            startUninstallProgress();
        } else if(v == mCancel) {
            finish();
        }
    }
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
