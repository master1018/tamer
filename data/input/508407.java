class AppWaitingForDebuggerDialog extends BaseErrorDialog {
    final ActivityManagerService mService;
    final ProcessRecord mProc;
    private CharSequence mAppName;
    public AppWaitingForDebuggerDialog(ActivityManagerService service,
            Context context, ProcessRecord app) {
        super(context);
        mService = service;
        mProc = app;
        mAppName = context.getPackageManager().getApplicationLabel(app.info);
        setCancelable(false);
        StringBuilder text = new StringBuilder();
        if (mAppName != null && mAppName.length() > 0) {
            text.append("Application ");
            text.append(mAppName);
            text.append(" (process ");
            text.append(app.processName);
            text.append(")");
        } else {
            text.append("Process ");
            text.append(app.processName);
        }
        text.append(" is waiting for the debugger to attach.");
        setMessage(text.toString());
        setButton("Force Close", mHandler.obtainMessage(1, app));
        setTitle("Waiting For Debugger");
        getWindow().setTitle("Waiting For Debugger: " + app.info.processName);
    }
    public void onStop() {
    }
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mService.killAppAtUsersRequest(mProc, AppWaitingForDebuggerDialog.this);
                    break;
            }
        }
    };
}
