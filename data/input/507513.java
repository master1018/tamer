class AppErrorDialog extends BaseErrorDialog {
    private final static String TAG = "AppErrorDialog";
    private final AppErrorResult mResult;
    private final ProcessRecord mProc;
    static final int FORCE_QUIT = 0;
    static final int FORCE_QUIT_AND_REPORT = 1;
    static final long DISMISS_TIMEOUT = 1000 * 60 * 5;
    public AppErrorDialog(Context context, AppErrorResult result, ProcessRecord app) {
        super(context);
        Resources res = context.getResources();
        mProc = app;
        mResult = result;
        CharSequence name;
        if ((app.pkgList.size() == 1) &&
                (name=context.getPackageManager().getApplicationLabel(app.info)) != null) {
            setMessage(res.getString(
                    com.android.internal.R.string.aerr_application,
                    name.toString(), app.info.processName));
        } else {
            name = app.processName;
            setMessage(res.getString(
                    com.android.internal.R.string.aerr_process,
                    name.toString()));
        }
        setCancelable(false);
        setButton(DialogInterface.BUTTON_POSITIVE,
                res.getText(com.android.internal.R.string.force_close),
                mHandler.obtainMessage(FORCE_QUIT));
        if (app.errorReportReceiver != null) {
            setButton(DialogInterface.BUTTON_NEGATIVE,
                    res.getText(com.android.internal.R.string.report),
                    mHandler.obtainMessage(FORCE_QUIT_AND_REPORT));
        }
        setTitle(res.getText(com.android.internal.R.string.aerr_title));
        getWindow().addFlags(FLAG_SYSTEM_ERROR);
        getWindow().setTitle("Application Error: " + app.info.processName);
        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(FORCE_QUIT),
                DISMISS_TIMEOUT);
    }
    public void onStop() {
    }
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            synchronized (mProc) {
                if (mProc != null && mProc.crashDialog == AppErrorDialog.this) {
                    mProc.crashDialog = null;
                }
            }
            mResult.set(msg.what);
            dismiss();
        }
    };
}
