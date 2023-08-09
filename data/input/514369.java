public class SettingsPage extends Composite implements ISettingsPage {
    private SettingsChangedCallback mSettingsChangedCallback;
    private Group mProxySettingsGroup;
    private Group mMiscGroup;
    private Label mProxyServerLabel;
    private Label mProxyPortLabel;
    private Text mProxyServerText;
    private Text mProxyPortText;
    private Button mForceHttpCheck;
    private Button mAskAdbRestartCheck;
    private SelectionAdapter mApplyOnSelected = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            applyNewSettings(); 
        }
    };
    private ModifyListener mApplyOnModified = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            applyNewSettings(); 
        }
    };
    public SettingsPage(Composite parent) {
        super(parent, SWT.BORDER);
        createContents(this);
        mProxySettingsGroup = new Group(this, SWT.NONE);
        mProxySettingsGroup.setText("Proxy Settings");
        mProxySettingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        mProxySettingsGroup.setLayout(new GridLayout(2, false));
        mProxyServerLabel = new Label(mProxySettingsGroup, SWT.NONE);
        mProxyServerLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        mProxyServerLabel.setText("HTTP Proxy Server");
        String tooltip = "The DNS name or IP of the HTTP proxy server to use. " +
                         "When empty, no HTTP proxy is used.";
        mProxyServerLabel.setToolTipText(tooltip);
        mProxyServerText = new Text(mProxySettingsGroup, SWT.BORDER);
        mProxyServerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mProxyServerText.addModifyListener(mApplyOnModified);
        mProxyServerText.setToolTipText(tooltip);
        mProxyPortLabel = new Label(mProxySettingsGroup, SWT.NONE);
        mProxyPortLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        mProxyPortLabel.setText("HTTP Proxy Port");
        tooltip = "The port of the HTTP proxy server to use. " +
                  "When empty, the default for HTTP or HTTPS is used.";
        mProxyPortLabel.setToolTipText(tooltip);
        mProxyPortText = new Text(mProxySettingsGroup, SWT.BORDER);
        mProxyPortText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        mProxyPortText.addModifyListener(mApplyOnModified);
        mProxyPortText.setToolTipText(tooltip);
        mMiscGroup = new Group(this, SWT.NONE);
        mMiscGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        mMiscGroup.setText("Misc");
        mMiscGroup.setLayout(new GridLayout(2, false));
        mForceHttpCheck = new Button(mMiscGroup, SWT.CHECK);
        mForceHttpCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        mForceHttpCheck.setText("Force https:
        mForceHttpCheck.setToolTipText("If you are not able to connect to the official Android repository " +
                "using HTTPS, enable this setting to force accessing it via HTTP.");
        mForceHttpCheck.addSelectionListener(mApplyOnSelected);
        mAskAdbRestartCheck = new Button(mMiscGroup, SWT.CHECK);
        mAskAdbRestartCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        mAskAdbRestartCheck.setText("Ask before restarting ADB");
        mAskAdbRestartCheck.setToolTipText("When checked, the user will be asked for permission " +
                "to restart ADB after updating an addon-on package or a tool package.");
        mAskAdbRestartCheck.addSelectionListener(mApplyOnSelected);
        postCreate();  
    }
    private void createContents(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
    }
    @Override
    protected void checkSubclass() {
    }
    private void postCreate() {
    }
    public void loadSettings(Properties in_settings) {
        mProxyServerText.setText(in_settings.getProperty(KEY_HTTP_PROXY_HOST, ""));  
        mProxyPortText.setText(  in_settings.getProperty(KEY_HTTP_PROXY_PORT, ""));  
        mForceHttpCheck.setSelection(Boolean.parseBoolean(in_settings.getProperty(KEY_FORCE_HTTP)));
        mAskAdbRestartCheck.setSelection(Boolean.parseBoolean(in_settings.getProperty(KEY_ASK_ADB_RESTART)));
    }
    public void retrieveSettings(Properties out_settings) {
        out_settings.setProperty(KEY_HTTP_PROXY_HOST, mProxyServerText.getText());
        out_settings.setProperty(KEY_HTTP_PROXY_PORT, mProxyPortText.getText());
        out_settings.setProperty(KEY_FORCE_HTTP,
                Boolean.toString(mForceHttpCheck.getSelection()));
        out_settings.setProperty(KEY_ASK_ADB_RESTART,
                Boolean.toString(mAskAdbRestartCheck.getSelection()));
    }
    public void setOnSettingsChanged(SettingsChangedCallback settingsChangedCallback) {
        mSettingsChangedCallback = settingsChangedCallback;
    }
    private void applyNewSettings() {
        if (mSettingsChangedCallback != null) {
            mSettingsChangedCallback.onSettingsChanged(this);
        }
    }
}
