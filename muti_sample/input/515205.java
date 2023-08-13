class AppNotRespondingDialog extends BaseErrorDialog {
    private static final String TAG = "AppNotRespondingDialog";
    static final int FORCE_CLOSE = 1;
    static final int WAIT = 2;
    static final int WAIT_AND_REPORT = 3;
    private final ActivityManagerService mService;
    private final ProcessRecord mProc;
    public AppNotRespondingDialog(ActivityManagerService service, Context context,
            ProcessRecord app, HistoryRecord activity) {
        super(context);
        mService = service;
        mProc = app;
        Resources res = context.getResources();
        setCancelable(false);
        int resid;
        CharSequence name1 = activity != null
                ? activity.info.loadLabel(context.getPackageManager())
                : null;
        CharSequence name2 = null;
        if ((app.pkgList.size() == 1) &&
                (name2=context.getPackageManager().getApplicationLabel(app.info)) != null) {
            if (name1 != null) {
                resid = com.android.internal.R.string.anr_activity_application;
            } else {
                name1 = name2;
                name2 = app.processName;
                resid = com.android.internal.R.string.anr_application_process;
            }
        } else {
            if (name1 != null) {
                name2 = app.processName;
                resid = com.android.internal.R.string.anr_activity_process;
            } else {
                name1 = app.processName;
                resid = com.android.internal.R.string.anr_process;
            }
        }
        setMessage(name2 != null
                ? res.getString(resid, name1.toString(), name2.toString())
                : res.getString(resid, name1.toString()));
        setButton(DialogInterface.BUTTON_POSITIVE,
                res.getText(com.android.internal.R.string.force_close),
                mHandler.obtainMessage(FORCE_CLOSE));
        setButton(DialogInterface.BUTTON_NEUTRAL,
                res.getText(com.android.internal.R.string.wait),
                mHandler.obtainMessage(WAIT));
        if (app.errorReportReceiver != null) {
            setButton(DialogInterface.BUTTON_NEGATIVE,
                    res.getText(com.android.internal.R.string.report),
                    mHandler.obtainMessage(WAIT_AND_REPORT));
        }
        setTitle(res.getText(com.android.internal.R.string.anr_title));
        getWindow().addFlags(FLAG_SYSTEM_ERROR);
        getWindow().setTitle("Application Not Responding: " + app.info.processName);
    }
    public void onStop() {
    }
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Intent appErrorIntent = null;
            switch (msg.what) {
                case FORCE_CLOSE:
                    mService.killAppAtUsersRequest(mProc, AppNotRespondingDialog.this);
                    break;
                case WAIT_AND_REPORT:
                case WAIT:
                    synchronized (mService) {
                        ProcessRecord app = mProc;
                        if (msg.what == WAIT_AND_REPORT) {
                            appErrorIntent = mService.createAppErrorIntentLocked(app,
                                    System.currentTimeMillis(), null);
                        }
                        app.notResponding = false;
                        app.notRespondingReport = null;
                        if (app.anrDialog == AppNotRespondingDialog.this) {
                            app.anrDialog = null;
                        }
                    }
                    break;
            }
            if (appErrorIntent != null) {
                try {
                    getContext().startActivity(appErrorIntent);
                } catch (ActivityNotFoundException e) {
                    Slog.w(TAG, "bug report receiver dissappeared", e);
                }
            }
        }
    };
}
