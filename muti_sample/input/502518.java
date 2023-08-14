public class ProxySelector extends Activity
{
    private final static String LOGTAG = "Settings";
    EditText    mHostnameField;
    EditText    mPortField;
    Button      mOKButton;
    private static final String HOSTNAME_REGEXP = "^$|^[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*(\\.[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*)*$";
    private static final Pattern HOSTNAME_PATTERN;
    static {
        HOSTNAME_PATTERN = Pattern.compile(HOSTNAME_REGEXP);
    }
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (android.util.Config.LOGV) Log.v(LOGTAG, "[ProxySelector] onStart");
        setContentView(R.layout.proxy);
        initView();
        populateFields(false);
    }
    protected void showError(int error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.proxy_error)
                .setMessage(error)
                .setPositiveButton(R.string.proxy_error_dismiss, null)
                .show();
    }
    void initView() {
        mHostnameField = (EditText)findViewById(R.id.hostname);
        mHostnameField.setOnFocusChangeListener(mOnFocusChangeHandler);
        mPortField = (EditText)findViewById(R.id.port);
        mPortField.setOnClickListener(mOKHandler);
        mPortField.setOnFocusChangeListener(mOnFocusChangeHandler);
        mOKButton = (Button)findViewById(R.id.action);
        mOKButton.setOnClickListener(mOKHandler);
        Button b = (Button)findViewById(R.id.clear);
        b.setOnClickListener(mClearHandler);
        b = (Button)findViewById(R.id.defaultView);
        b.setOnClickListener(mDefaultHandler);
    }
    void populateFields(boolean useDefault) {
        String hostname = null;
        int port = -1;
        if (useDefault) {
            hostname = Proxy.getDefaultHost();
            port = Proxy.getDefaultPort();
        } else {
            hostname = Proxy.getHost(this);
            port = Proxy.getPort(this);
        }
        if (hostname == null) {
            hostname = "";
        }
        mHostnameField.setText(hostname);
        String portStr = port == -1 ? "" : Integer.toString(port);
        mPortField.setText(portStr);
        Intent intent = getIntent();
        String buttonLabel = intent.getStringExtra("button-label");
        if (!TextUtils.isEmpty(buttonLabel)) {
            mOKButton.setText(buttonLabel);
        }
        String title = intent.getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
    }
    int validate(String hostname, String port) {
        Matcher match = HOSTNAME_PATTERN.matcher(hostname);
        if (!match.matches()) return R.string.proxy_error_invalid_host;
        if (hostname.length() > 0 && port.length() == 0) {
            return R.string.proxy_error_empty_port;
        }
        if (port.length() > 0) {
            if (hostname.length() == 0) {
                return R.string.proxy_error_empty_host_set_port;
            }
            int portVal = -1;
            try {
                portVal = Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                return R.string.proxy_error_invalid_port;
            }
            if (portVal <= 0 || portVal > 0xFFFF) {
                return R.string.proxy_error_invalid_port;
            }
        }
        return 0;
    }
    boolean saveToDb() {
        String hostname = mHostnameField.getText().toString().trim();
        String portStr = mPortField.getText().toString().trim();
        int port = -1;
        int result = validate(hostname, portStr);
        if (result > 0) {
            showError(result);
            return false;
        }
        if (portStr.length() > 0) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        ContentResolver res = getContentResolver();
        if (hostname.equals(Proxy.getDefaultHost())
                && port == Proxy.getDefaultPort()) {
            hostname = null;
        }
        if (!TextUtils.isEmpty(hostname)) {
            hostname += ':' + portStr;
        }
        Settings.Secure.putString(res, Settings.Secure.HTTP_PROXY, hostname);
        sendBroadcast(new Intent(Proxy.PROXY_CHANGE_ACTION));
        return true;
    }
    OnClickListener mOKHandler = new OnClickListener() {
            public void onClick(View v) {
                if (saveToDb()) {
                    finish();
                }
            }
        };
    OnClickListener mClearHandler = new OnClickListener() {
            public void onClick(View v) {
                mHostnameField.setText("");
                mPortField.setText("");
            }
        };
    OnClickListener mDefaultHandler = new OnClickListener() {
            public void onClick(View v) {
                populateFields(true);
            }
        };
    OnFocusChangeListener mOnFocusChangeHandler = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    TextView textView = (TextView) v;
                    Selection.selectAll((Spannable) textView.getText());
                }
            }
        };
}
