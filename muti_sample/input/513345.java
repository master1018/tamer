public class StkDialogActivity extends Activity implements View.OnClickListener {
    TextMessage mTextMsg;
    Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_ID_TIMEOUT:
                sendResponse(StkAppService.RES_ID_TIMEOUT);
                finish();
                break;
            }
        }
    };
    private static final String TEXT = "text";
    private static final int MSG_ID_TIMEOUT = 1;
    public static final int OK_BUTTON = R.id.button_ok;
    public static final int CANCEL_BUTTON = R.id.button_cancel;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initFromIntent(getIntent());
        if (mTextMsg == null) {
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        Window window = getWindow();
        setContentView(R.layout.stk_msg_dialog);
        TextView mMessageView = (TextView) window
                .findViewById(R.id.dialog_message);
        Button okButton = (Button) findViewById(R.id.button_ok);
        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        setTitle(mTextMsg.title);
        if (!(mTextMsg.iconSelfExplanatory && mTextMsg.icon != null)) {
            mMessageView.setText(mTextMsg.text);
        }
        if (mTextMsg.icon == null) {
            window.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                    com.android.internal.R.drawable.stat_notify_sim_toolkit);
        } else {
            window.setFeatureDrawable(Window.FEATURE_LEFT_ICON,
                    new BitmapDrawable(mTextMsg.icon));
        }
    }
    public void onClick(View v) {
        String input = null;
        switch (v.getId()) {
        case OK_BUTTON:
            sendResponse(StkAppService.RES_ID_CONFIRM, true);
            finish();
            break;
        case CANCEL_BUTTON:
            sendResponse(StkAppService.RES_ID_CONFIRM, false);
            finish();
            break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            sendResponse(StkAppService.RES_ID_BACKWARD);
            finish();
            break;
        }
        return false;
    }
    @Override
    public void onResume() {
        super.onResume();
        startTimeOut();
    }
    @Override
    public void onPause() {
        super.onPause();
        cancelTimeOut();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TEXT, mTextMsg);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTextMsg = savedInstanceState.getParcelable(TEXT);
    }
    private void sendResponse(int resId, boolean confirmed) {
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_RESPONSE);
        args.putInt(StkAppService.RES_ID, resId);
        args.putBoolean(StkAppService.CONFIRMATION, confirmed);
        startService(new Intent(this, StkAppService.class).putExtras(args));
    }
    private void sendResponse(int resId) {
        sendResponse(resId, true);
    }
    private void initFromIntent(Intent intent) {
        if (intent != null) {
            mTextMsg = intent.getParcelableExtra("TEXT");
        } else {
            finish();
        }
    }
    private void cancelTimeOut() {
        mTimeoutHandler.removeMessages(MSG_ID_TIMEOUT);
    }
    private void startTimeOut() {
        cancelTimeOut();
        int dialogDuration = StkApp.calculateDurationInMilis(mTextMsg.duration);
        if (dialogDuration == 0) {
            dialogDuration = StkApp.UI_TIMEOUT;
        }
        mTimeoutHandler.sendMessageDelayed(mTimeoutHandler
                .obtainMessage(MSG_ID_TIMEOUT), dialogDuration);
    }
}
