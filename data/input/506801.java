public class PowerDialog extends Dialog implements OnClickListener,
        OnKeyListener {
    private static final String TAG = "PowerDialog";
    static private StatusBarManager sStatusBar;
    private Button mKeyguard;
    private Button mPower;
    private Button mRadioPower;
    private Button mSilent;
    private LocalPowerManager mPowerManager;
    public PowerDialog(Context context, LocalPowerManager powerManager) {
        super(context);
        mPowerManager = powerManager;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (sStatusBar == null) {
            sStatusBar = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        setContentView(com.android.internal.R.layout.power_dialog);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        if (!getContext().getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        setTitle(context.getText(R.string.power_dialog));
        mKeyguard = (Button) findViewById(R.id.keyguard);
        mPower = (Button) findViewById(R.id.off);
        mRadioPower = (Button) findViewById(R.id.radio_power);
        mSilent = (Button) findViewById(R.id.silent);
        if (mKeyguard != null) {
            mKeyguard.setOnKeyListener(this);
            mKeyguard.setOnClickListener(this);
        }
        if (mPower != null) {
            mPower.setOnClickListener(this);
        }
        if (mRadioPower != null) {
            mRadioPower.setOnClickListener(this);
        }
        if (mSilent != null) {
            mSilent.setOnClickListener(this);
            mSilent.setVisibility(View.GONE);
        }
        CharSequence text;
        text = context.getText(R.string.screen_lock);
        mKeyguard.setText(text);
        mKeyguard.requestFocus();
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                text = phone.isRadioOn() ? context
                        .getText(R.string.turn_off_radio) : context
                        .getText(R.string.turn_on_radio);
            }
        } catch (RemoteException ex) {
        }
        mRadioPower.setText(text);
    }
    public void onClick(View v) {
        this.dismiss();
        if (v == mPower) {
            ShutdownThread.shutdown(getContext(), true);
        } else if (v == mRadioPower) {
            try {
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (phone != null) {
                    phone.toggleRadioOnOff();
                }
            } catch (RemoteException ex) {
            }
        } else if (v == mSilent) {
        } else if (v == mKeyguard) {
            if (v.isInTouchMode()) {
                this.dismiss();
                mPowerManager.goToSleep(SystemClock.uptimeMillis() + 1);
            }
        }
    }
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
                || event.getAction() != KeyEvent.ACTION_UP) {
            return false;
        }
        this.dismiss();
        mPowerManager.goToSleep(event.getEventTime() + 1);
        return true;
    }
    public void show() {
        super.show();
        Log.d(TAG, "show... disabling expand");
        sStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
    }
    public void dismiss() {
        super.dismiss();
        Log.d(TAG, "dismiss... reenabling expand");
        sStatusBar.disable(StatusBarManager.DISABLE_NONE);
    }
}
