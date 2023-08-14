public class EmergencyCallbackModeExitDialog extends Activity implements OnDismissListener {
    static final String ACTION_SHOW_ECM_EXIT_DIALOG =
            "com.android.phone.action.ACTION_SHOW_ECM_EXIT_DIALOG";
    public static final String EXTRA_EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int EXIT_ECM_BLOCK_OTHERS = 1;
    public static final int EXIT_ECM_DIALOG = 2;
    public static final int EXIT_ECM_PROGRESS_DIALOG = 3;
    public static final int EXIT_ECM_IN_EMERGENCY_CALL_DIALOG = 4;
    AlertDialog mAlertDialog = null;
    ProgressDialog mProgressDialog = null;
    CountDownTimer mTimer = null;
    EmergencyCallbackModeService mService = null;
    Handler mHandler = null;
    int mDialogType = 0;
    long mEcmTimeout = 0;
    private boolean mInEmergencyCall = false;
    private static final int ECM_TIMER_RESET = 1;
    private Phone mPhone = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Boolean.parseBoolean(
                    SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            finish();
        }
        mHandler = new Handler();
        Thread waitForConnectionCompleteThread = new Thread(null, mTask,
                "EcmExitDialogWaitThread");
        waitForConnectionCompleteThread.start();
        mPhone = PhoneFactory.getDefaultPhone();
        mPhone.registerForEcmTimerReset(mTimerResetHandler, ECM_TIMER_RESET, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        registerReceiver(mEcmExitReceiver, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mEcmExitReceiver);
        mPhone.unregisterForEcmTimerReset(mHandler);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDialogType = savedInstanceState.getInt("DIALOG_TYPE");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("DIALOG_TYPE", mDialogType);
    }
    private Runnable mTask = new Runnable() {
        public void run() {
            Looper.prepare();
            bindService(new Intent(EmergencyCallbackModeExitDialog.this,
                    EmergencyCallbackModeService.class), mConnection, Context.BIND_AUTO_CREATE);
            synchronized (EmergencyCallbackModeExitDialog.this) {
                try {
                    if (mService == null) {
                        EmergencyCallbackModeExitDialog.this.wait();
                    }
                } catch (InterruptedException e) {
                    Log.d("ECM", "EmergencyCallbackModeExitDialog InterruptedException: "
                            + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (mService != null) {
                mEcmTimeout = mService.getEmergencyCallbackModeTimeout();
                mInEmergencyCall = mService.getEmergencyCallbackModeCallState();
            }
            unbindService(mConnection);
            mHandler.post(new Runnable() {
                public void run() {
                    showEmergencyCallbackModeExitDialog();
                }
            });
        }
    };
    private void showEmergencyCallbackModeExitDialog() {
        if(mInEmergencyCall) {
            mDialogType = EXIT_ECM_IN_EMERGENCY_CALL_DIALOG;
            showDialog(EXIT_ECM_IN_EMERGENCY_CALL_DIALOG);
        } else {
            if (getIntent().getAction().equals(
                    TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS)) {
                mDialogType = EXIT_ECM_BLOCK_OTHERS;
                showDialog(EXIT_ECM_BLOCK_OTHERS);
            } else if (getIntent().getAction().equals(ACTION_SHOW_ECM_EXIT_DIALOG)) {
                mDialogType = EXIT_ECM_DIALOG;
                showDialog(EXIT_ECM_DIALOG);
            }
            mTimer = new CountDownTimer(mEcmTimeout, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    CharSequence text = getDialogText(millisUntilFinished);
                    mAlertDialog.setMessage(text);
                }
                @Override
                public void onFinish() {
                }
            }.start();
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case EXIT_ECM_BLOCK_OTHERS:
        case EXIT_ECM_DIALOG:
            CharSequence text = getDialogText(mEcmTimeout);
            mAlertDialog = new AlertDialog.Builder(EmergencyCallbackModeExitDialog.this)
                    .setIcon(R.drawable.picture_emergency32x32)
                    .setTitle(R.string.phone_in_ecm_notification_title)
                    .setMessage(text)
                    .setPositiveButton(R.string.alert_dialog_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int whichButton) {
                                    mPhone.exitEmergencyCallbackMode();
                                    showDialog(EXIT_ECM_PROGRESS_DIALOG);
                                    mTimer.cancel();
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    setResult(RESULT_OK, (new Intent()).putExtra(
                                            EXTRA_EXIT_ECM_RESULT, false));
                                    finish();
                                }
                            }).create();
            mAlertDialog.setOnDismissListener(this);
            return mAlertDialog;
        case EXIT_ECM_IN_EMERGENCY_CALL_DIALOG:
            mAlertDialog = new AlertDialog.Builder(EmergencyCallbackModeExitDialog.this)
                    .setIcon(R.drawable.picture_emergency32x32)
                    .setTitle(R.string.phone_in_ecm_notification_title)
                    .setMessage(R.string.alert_dialog_in_ecm_call)
                    .setNeutralButton(R.string.alert_dialog_dismiss,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    setResult(RESULT_OK, (new Intent()).putExtra(
                                            EXTRA_EXIT_ECM_RESULT, false));
                                    finish();
                                }
                            }).create();
            mAlertDialog.setOnDismissListener(this);
            return mAlertDialog;
        case EXIT_ECM_PROGRESS_DIALOG:
            mProgressDialog = new ProgressDialog(EmergencyCallbackModeExitDialog.this);
            mProgressDialog.setMessage(getText(R.string.progress_dialog_exiting_ecm));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            return mProgressDialog;
        default:
            return null;
        }
    }
    private CharSequence getDialogText(long millisUntilFinished) {
        int minutes = (int)(millisUntilFinished / 60000);
        String time = String.format("%d:%02d", minutes,
                (millisUntilFinished % 60000) / 1000);
        switch (mDialogType) {
        case EXIT_ECM_BLOCK_OTHERS:
            return String.format(getResources().getQuantityText(
                    R.plurals.alert_dialog_not_avaialble_in_ecm, minutes).toString(), time);
        case EXIT_ECM_DIALOG:
            return String.format(getResources().getQuantityText(R.plurals.alert_dialog_exit_ecm,
                    minutes).toString(), time);
        }
        return null;
    }
    public void onDismiss(DialogInterface dialog) {
        EmergencyCallbackModeExitDialog.this.setResult(RESULT_OK, (new Intent())
                .putExtra(EXTRA_EXIT_ECM_RESULT, false));
        finish();
    }
    private BroadcastReceiver mEcmExitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED)) {
                if (intent.getBooleanExtra("phoneinECMState", false) == false) {
                    if (mAlertDialog != null)
                        mAlertDialog.dismiss();
                    if (mProgressDialog != null)
                        mProgressDialog.dismiss();
                    EmergencyCallbackModeExitDialog.this.setResult(RESULT_OK, (new Intent())
                            .putExtra(EXTRA_EXIT_ECM_RESULT, true));
                    finish();
                }
            }
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = ((EmergencyCallbackModeService.LocalBinder)service).getService();
            synchronized (EmergencyCallbackModeExitDialog.this) {
                EmergencyCallbackModeExitDialog.this.notify();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };
    private Handler mTimerResetHandler = new Handler () {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ECM_TIMER_RESET:
                    if(!((Boolean)((AsyncResult) msg.obj).result).booleanValue()) {
                        EmergencyCallbackModeExitDialog.this.setResult(RESULT_OK, (new Intent())
                                .putExtra(EXTRA_EXIT_ECM_RESULT, false));
                        finish();
                    }
                    break;
            }
        }
    };
}
