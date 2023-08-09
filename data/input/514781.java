class FactoryErrorDialog extends BaseErrorDialog {
    public FactoryErrorDialog(Context context, CharSequence msg) {
        super(context);
        setCancelable(false);
        setTitle(context.getText(com.android.internal.R.string.factorytest_failed));
        setMessage(msg);
        setButton(context.getText(com.android.internal.R.string.factorytest_reboot),
                mHandler.obtainMessage(0));
        getWindow().setTitle("Factory Error");
    }
    public void onStop() {
    }
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            throw new RuntimeException("Rebooting from failed factory test");
        }
    };
}
