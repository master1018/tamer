class WifiApDialog extends AlertDialog implements View.OnClickListener,
        TextWatcher, AdapterView.OnItemSelectedListener {
    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    private final DialogInterface.OnClickListener mListener;
    private static final int OPEN_INDEX = 0;
    private static final int WPA_INDEX = 1;
    private View mView;
    private TextView mSsid;
    private int mSecurityType = AccessPoint.SECURITY_NONE;
    private EditText mPassword;
    WifiConfiguration mWifiConfig;
    public WifiApDialog(Context context, DialogInterface.OnClickListener listener,
            WifiConfiguration wifiConfig) {
        super(context);
        mListener = listener;
        mWifiConfig = wifiConfig;
        if (wifiConfig != null)
          mSecurityType = AccessPoint.getSecurity(wifiConfig);
    }
    public WifiConfiguration getConfig() {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = mSsid.getText().toString();
        switch (mSecurityType) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;
            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                if (mPassword.length() != 0) {
                    String password = mPassword.getText().toString();
                    config.preSharedKey = password;
                }
                return config;
        }
        return null;
    }
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.wifi_ap_dialog, null);
        Spinner mSecurity = ((Spinner) mView.findViewById(R.id.security));
        setView(mView);
        setInverseBackgroundForced(true);
        Context context = getContext();
        setTitle(R.string.wifi_tether_configure_ap_text);
        mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
        mSsid = (TextView) mView.findViewById(R.id.ssid);
        mPassword = (EditText) mView.findViewById(R.id.password);
        setButton(BUTTON_SUBMIT, context.getString(R.string.wifi_save), mListener);
        setButton(DialogInterface.BUTTON_NEGATIVE,
        context.getString(R.string.wifi_cancel), mListener);
        if (mWifiConfig != null) {
            mSsid.setText(mWifiConfig.SSID);
            switch (mSecurityType) {
              case AccessPoint.SECURITY_NONE:
                  mSecurity.setSelection(OPEN_INDEX);
                  break;
              case AccessPoint.SECURITY_PSK:
                  String str = mWifiConfig.preSharedKey;
                  mPassword.setText(str);
                  mSecurity.setSelection(WPA_INDEX);
                  break;
            }
        }
        mSsid.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        ((CheckBox) mView.findViewById(R.id.show_password)).setOnClickListener(this);
        mSecurity.setOnItemSelectedListener(this);
        super.onCreate(savedInstanceState);
        showSecurityFields();
        validate();
    }
    private void validate() {
        if ((mSsid != null && mSsid.length() == 0) ||
                   (mSecurityType == AccessPoint.SECURITY_PSK && mPassword.length() < 8)) {
            getButton(BUTTON_SUBMIT).setEnabled(false);
        } else {
            getButton(BUTTON_SUBMIT).setEnabled(true);
        }
    }
    public void onClick(View view) {
        mPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | (((CheckBox) view).isChecked() ?
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void afterTextChanged(Editable editable) {
        validate();
    }
    public void onItemSelected(AdapterView parent, View view, int position, long id) {
        if(position == OPEN_INDEX)
            mSecurityType = AccessPoint.SECURITY_NONE;
        else
            mSecurityType = AccessPoint.SECURITY_PSK;
        showSecurityFields();
        validate();
    }
    public void onNothingSelected(AdapterView parent) {
    }
    private void showSecurityFields() {
        if (mSecurityType == AccessPoint.SECURITY_NONE) {
            mView.findViewById(R.id.fields).setVisibility(View.GONE);
            return;
        }
        mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
    }
}
