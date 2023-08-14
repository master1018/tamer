public class PackageInstallerActivity extends Activity implements OnCancelListener, OnClickListener {
    private static final String TAG = "PackageInstaller";
    private Uri mPackageURI;    
    private boolean localLOGV = false;
    PackageManager mPm;
    private PackageParser.Package mPkgInfo;
    private ApplicationInfo mAppInfo = null;
    View mInstallConfirm;
    private Button mOk;
    private Button mCancel;
    private static final int DLG_BASE = 0;
    private static final int DLG_REPLACE_APP = DLG_BASE + 1;
    private static final int DLG_UNKNOWN_APPS = DLG_BASE + 2;
    private static final int DLG_PACKAGE_ERROR = DLG_BASE + 3;
    private static final int DLG_OUT_OF_SPACE = DLG_BASE + 4;
    private static final int DLG_INSTALL_ERROR = DLG_BASE + 5;
    private void startInstallConfirm() {
        LinearLayout permsSection = (LinearLayout) mInstallConfirm.findViewById(R.id.permissions_section);
        LinearLayout securityList = (LinearLayout) permsSection.findViewById(
                R.id.security_settings_list);
        boolean permVisible = false;
        if(mPkgInfo != null) {
            AppSecurityPermissions asp = new AppSecurityPermissions(this, mPkgInfo);
            if(asp.getPermissionCount() > 0) {
                permVisible = true;
                securityList.addView(asp.getPermissionsView());
            }
        }
        if(!permVisible){
            permsSection.setVisibility(View.INVISIBLE);
        }
        mInstallConfirm.setVisibility(View.VISIBLE);
        mOk = (Button)findViewById(R.id.ok_button);
        mCancel = (Button)findViewById(R.id.cancel_button);
        mOk.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }
    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }
    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case DLG_REPLACE_APP:
            int msgId = R.string.dlg_app_replacement_statement;
            if ((mAppInfo != null) && (mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                msgId = R.string.dlg_sys_app_replacement_statement;
            }
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.dlg_app_replacement_title)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startInstallConfirm();
                        }})
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Canceling installation");
                            finish();
                        }})
                    .setMessage(msgId)
                    .setOnCancelListener(this)
                    .create();
        case DLG_UNKNOWN_APPS:
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.unknown_apps_dlg_title)
                    .setMessage(R.string.unknown_apps_dlg_text)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Finishing off activity so that user can navigate to settings manually");
                            finish();
                        }})
                    .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Launching settings");
                            launchSettingsAppAndFinish();
                        }
                    })
                    .setOnCancelListener(this)
                    .create(); 
        case DLG_PACKAGE_ERROR :
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.Parse_error_dlg_title)
                    .setMessage(R.string.Parse_error_dlg_text)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setOnCancelListener(this)
                    .create();
        case DLG_OUT_OF_SPACE:
            CharSequence appTitle = mPm.getApplicationLabel(mPkgInfo.applicationInfo);
            String dlgText = getString(R.string.out_of_space_dlg_text, 
                    appTitle.toString());
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.out_of_space_dlg_title)
                    .setMessage(dlgText)
                    .setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
                            startActivity(intent);   
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Canceling installation");
                            finish();
                        }
                  })
                  .setOnCancelListener(this)
                  .create();
        case DLG_INSTALL_ERROR :
            CharSequence appTitle1 = mPm.getApplicationLabel(mPkgInfo.applicationInfo);
            String dlgText1 = getString(R.string.install_failed_msg,
                    appTitle1.toString());
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.install_failed)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setMessage(dlgText1)
                    .setOnCancelListener(this)
                    .create();
       }
       return null;
   }
    private void launchSettingsAppAndFinish() {
        Intent launchSettingsIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
        startActivity(launchSettingsIntent);
        finish();
    }
    private boolean isInstallingUnknownAppsAllowed() {
        return Settings.Secure.getInt(getContentResolver(), 
            Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0;
    }
    private void initiateInstall() {
        String pkgName = mPkgInfo.packageName;
        String[] oldName = mPm.canonicalToCurrentPackageNames(new String[] { pkgName });
        if (oldName != null && oldName.length > 0 && oldName[0] != null) {
            pkgName = oldName[0];
            mPkgInfo.setPackageName(pkgName);
        }
        try {
            mAppInfo = mPm.getApplicationInfo(pkgName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            mAppInfo = null;
        }
        if (mAppInfo == null) {
            startInstallConfirm();
        } else {
            if(localLOGV) Log.i(TAG, "Replacing existing package:"+
                    mPkgInfo.applicationInfo.packageName);
            showDialogInner(DLG_REPLACE_APP);
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Intent intent = getIntent();
        mPackageURI = intent.getData();
        mPm = getPackageManager();
        mPkgInfo = PackageUtil.getPackageInfo(mPackageURI);
        if(mPkgInfo == null) {
            Log.w(TAG, "Parse error when parsing manifest. Discontinuing installation");
            showDialogInner(DLG_PACKAGE_ERROR);
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.install_start);
        mInstallConfirm = findViewById(R.id.install_confirm_panel);
        mInstallConfirm.setVisibility(View.INVISIBLE);
        PackageUtil.AppSnippet as = PackageUtil.getAppSnippet(this,
                mPkgInfo.applicationInfo, mPackageURI);
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
        if(!isInstallingUnknownAppsAllowed()) {
            showDialogInner(DLG_UNKNOWN_APPS);
            return;
        }
        initiateInstall();
    }
    public void onCancel(DialogInterface dialog) {
        finish();
    }
    public void onClick(View v) {
        if(v == mOk) {
            Intent newIntent = new Intent();
            newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO,
                    mPkgInfo.applicationInfo);
            newIntent.setData(mPackageURI);
            newIntent.setClass(this, InstallAppProgress.class);
            String installerPackageName = getIntent().getStringExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME);
            if (installerPackageName != null) {
                newIntent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, installerPackageName);
            }
            if(localLOGV) Log.i(TAG, "downloaded app uri="+mPackageURI);
            startActivity(newIntent);
            finish();
        } else if(v == mCancel) {
            finish();
        }
    }
}
