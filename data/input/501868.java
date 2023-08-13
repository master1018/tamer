class BaseErrorDialog extends AlertDialog {
    public BaseErrorDialog(Context context) {
        super(context, com.android.internal.R.style.Theme_Dialog_AppError);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        getWindow().setTitle("Error Dialog");
        setIcon(R.drawable.ic_dialog_alert);
    }
    public void onStart() {
        super.onStart();
        setEnabled(false);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0), 1000);
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mConsuming) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private void setEnabled(boolean enabled) {
        Button b = (Button)findViewById(R.id.button1);
        if (b != null) {
            b.setEnabled(enabled);
        }
        b = (Button)findViewById(R.id.button2);
        if (b != null) {
            b.setEnabled(enabled);
        }
        b = (Button)findViewById(R.id.button3);
        if (b != null) {
            b.setEnabled(enabled);
        }
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mConsuming = false;
                setEnabled(true);
            }
        }
    };
    private boolean mConsuming = true;
}
