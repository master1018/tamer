public class StaticPortEditDialog extends Dialog {
    private static final int DLG_WIDTH = 400;
    private static final int DLG_HEIGHT = 200;
    private Shell mParent;
    private Shell mShell;
    private boolean mOk = false;
    private String mAppName;
    private String mPortNumber;
    private Button mOkButton;
    private Label mWarning;
    private ArrayList<Integer> mPorts;
    private int mEditPort = -1;
    private String mDeviceSn;
    public StaticPortEditDialog(Shell parent, ArrayList<Integer> ports) {
        super(parent, SWT.DIALOG_TRIM | SWT.BORDER | SWT.APPLICATION_MODAL);
        mPorts = ports;
        mDeviceSn = IDevice.FIRST_EMULATOR_SN;
    }
    public StaticPortEditDialog(Shell shell, ArrayList<Integer> ports,
            String oldDeviceSN, String oldAppName, String oldPortNumber) {
        this(shell, ports);
        mDeviceSn = oldDeviceSN;
        mAppName = oldAppName;
        mPortNumber = oldPortNumber;
        mEditPort = Integer.valueOf(mPortNumber);
    }
    public boolean open() {
        createUI();
        if (mParent == null || mShell == null) {
            return false;
        }
        mShell.setMinimumSize(DLG_WIDTH, DLG_HEIGHT);
        Rectangle r = mParent.getBounds();
        int cx = r.x + r.width/2;
        int x = cx - DLG_WIDTH / 2;
        int cy = r.y + r.height/2;
        int y = cy - DLG_HEIGHT / 2;
        mShell.setBounds(x, y, DLG_WIDTH, DLG_HEIGHT);
        mShell.open();
        Display display = mParent.getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return mOk;
    }
    public String getDeviceSN() {
        return mDeviceSn;
    }
    public String getAppName() {
        return mAppName;
    }
    public int getPortNumber() {
        return Integer.valueOf(mPortNumber);
    }
    private void createUI() {
        mParent = getParent();
        mShell = new Shell(mParent, getStyle());
        mShell.setText("Static Port");
        mShell.setLayout(new GridLayout(1, false));
        mShell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
            }
        });
        Composite main = new Composite(mShell, SWT.NONE);
        main.setLayoutData(new GridData(GridData.FILL_BOTH));
        main.setLayout(new GridLayout(2, false));
        Label l0 = new Label(main, SWT.NONE);
        l0.setText("Device Name:");
        final Text deviceSNText = new Text(main, SWT.SINGLE | SWT.BORDER);
        deviceSNText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (mDeviceSn != null) {
            deviceSNText.setText(mDeviceSn);
        }
        deviceSNText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mDeviceSn = deviceSNText.getText().trim();
                validate();
            }
        });
        Label l = new Label(main, SWT.NONE);
        l.setText("Application Name:");
        final Text appNameText = new Text(main, SWT.SINGLE | SWT.BORDER);
        if (mAppName != null) {
            appNameText.setText(mAppName);
        }
        appNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        appNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mAppName = appNameText.getText().trim();
                validate();
            }
        });
        Label l2 = new Label(main, SWT.NONE);
        l2.setText("Debug Port:");
        final Text debugPortText = new Text(main, SWT.SINGLE | SWT.BORDER);
        if (mPortNumber != null) {
            debugPortText.setText(mPortNumber);
        }
        debugPortText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        debugPortText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mPortNumber = debugPortText.getText().trim();
                validate();
            }
        });
        Composite warningComp = new Composite(mShell, SWT.NONE);
        warningComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        warningComp.setLayout(new GridLayout(1, true));
        mWarning = new Label(warningComp, SWT.NONE);
        mWarning.setText("");
        mWarning.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite bottomComp = new Composite(mShell, SWT.NONE);
        bottomComp
                .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        bottomComp.setLayout(new GridLayout(2, true));
        mOkButton = new Button(bottomComp, SWT.NONE);
        mOkButton.setText("OK");
        mOkButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mOk = true;
                mShell.close();
            }
        });
        mOkButton.setEnabled(false);
        mShell.setDefaultButton(mOkButton);
        Button cancelButton = new Button(bottomComp, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mShell.close();
            }
        });
        validate();
    }
    private void validate() {
        mWarning.setText(""); 
        if (mDeviceSn == null || mDeviceSn.length() == 0) {
            mWarning.setText("Device name missing.");
            mOkButton.setEnabled(false);
            return;
        }
        if (mAppName == null || mAppName.length() == 0) {
            mWarning.setText("Application name missing.");
            mOkButton.setEnabled(false);
            return;
        }
        String packageError = "Application name must be a valid Java package name.";
        String[] packageSegments = mAppName.split("\\."); 
        for (String p : packageSegments) {
            if (p.matches("^[a-zA-Z][a-zA-Z0-9]*") == false) { 
                mWarning.setText(packageError);
                mOkButton.setEnabled(false);
                return;
            }
            if (p.matches("^[a-z][a-z0-9]*") == false) { 
                mWarning.setText("Lower case is recommended for Java packages.");
            }
        }
        if (mAppName.charAt(mAppName.length()-1) == '.') {
            mWarning.setText(packageError);
            mOkButton.setEnabled(false);
            return;
        }
        if (mPortNumber == null || mPortNumber.length() == 0) {
            mWarning.setText("Port Number missing.");
            mOkButton.setEnabled(false);
            return;
        }
        if (mPortNumber.matches("[0-9]*") == false) { 
            mWarning.setText("Port Number invalid.");
            mOkButton.setEnabled(false);
            return;
        }
        long port = Long.valueOf(mPortNumber);
        if (port >= 32767) {
            mOkButton.setEnabled(false);
            return;
        }
        if (port != mEditPort) {
            for (Integer i : mPorts) {
                if (port == i.intValue()) {
                    mWarning.setText("Port already in use.");
                    mOkButton.setEnabled(false);
                    return;
                }
            }
        }
        mOkButton.setEnabled(true);
    }
}
