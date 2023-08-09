public class BluetoothNamePreference extends EditTextPreference implements TextWatcher {
    private static final String TAG = "BluetoothNamePreference";
    private static final int BLUETOOTH_NAME_MAX_LENGTH = 200;
    private LocalBluetoothManager mLocalManager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                setSummaryToName();
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED) &&
                    (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR) ==
                            BluetoothAdapter.STATE_ON)) {
                setSummaryToName();
            }
        }
    };
    public BluetoothNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLocalManager = LocalBluetoothManager.getInstance(context);
        setSummaryToName();
    }
    public void resume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        getContext().registerReceiver(mReceiver, filter);
        EditText et = getEditText();
        et.setFilters(new InputFilter[] {new LengthFilter(BLUETOOTH_NAME_MAX_LENGTH)});
        if (et != null) {
            et.addTextChangedListener(this);
            Dialog d = getDialog();
            if (d instanceof AlertDialog) {
                Button b = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
                b.setEnabled(et.getText().length() > 0);
            }
        }
    }
    public void pause() {
        EditText et = getEditText();
        if (et != null) {
            et.removeTextChangedListener(this);
        }
        getContext().unregisterReceiver(mReceiver);
    }
    private void setSummaryToName() {
        BluetoothAdapter adapter = mLocalManager.getBluetoothAdapter();
        if (adapter.isEnabled()) {
            setSummary(adapter.getName());
        }
    }
    @Override
    protected boolean persistString(String value) {
        BluetoothAdapter adapter = mLocalManager.getBluetoothAdapter();
        adapter.setName(value);
        return true;
    }
    @Override
    protected void onClick() {
        super.onClick();
        EditText et = getEditText();
        if (et != null) {
            et.setText(mLocalManager.getBluetoothAdapter().getName());
        }
    }
    public void afterTextChanged(Editable s) {
        Dialog d = getDialog();
        if (d instanceof AlertDialog) {
            ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() > 0);
        }
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
