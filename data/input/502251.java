public class DeviceCommandDialog extends Dialog {
    public static final int DEVICE_STATE = 0;
    public static final int APP_STATE = 1;
    public static final int RADIO_STATE = 2;
    public static final int LOGCAT = 3;
    private String mCommand;
    private String mFileName;
    private Label mStatusLabel;
    private Button mCancelDone;
    private Button mSave;
    private Text mText;
    private Font mFont = null;
    private boolean mCancel;
    private boolean mFinished;
    public DeviceCommandDialog(String command, String fileName, Shell parent) {
        this(command, fileName, parent,
            SWT.DIALOG_TRIM | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }
    public DeviceCommandDialog(String command, String fileName, Shell parent,
        int style)
    {
        super(parent, style);
        mCommand = command;
        mFileName = fileName;
    }
    public void open(IDevice currentDevice) {
        Shell parent = getParent();
        Shell shell = new Shell(parent, getStyle());
        shell.setText("Remote Command");
        mFinished = false;
        mFont = findFont(shell.getDisplay());
        createContents(shell);
        shell.setMinimumSize(500, 200);
        shell.setSize(800, 600);
        shell.open();
        executeCommand(shell, currentDevice);
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        if (mFont != null)
            mFont.dispose();
    }
    private void createContents(final Shell shell) {
        GridData data;
        shell.setLayout(new GridLayout(2, true));
        shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                if (!mFinished) {
                    Log.d("ddms", "NOT closing - cancelling command");
                    event.doit = false;
                    mCancel = true;
                }
            }
        });
        mStatusLabel = new Label(shell, SWT.NONE);
        mStatusLabel.setText("Executing '" + shortCommandString() + "'");
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = 2;
        mStatusLabel.setLayoutData(data);
        mText = new Text(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        mText.setEditable(false);
        mText.setFont(mFont);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        mText.setLayoutData(data);
        mSave = new Button(shell, SWT.PUSH);
        mSave.setText("Save");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        mSave.setLayoutData(data);
        mSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveText(shell);
            }
        });
        mSave.setEnabled(false);
        mCancelDone = new Button(shell, SWT.PUSH);
        mCancelDone.setText("Cancel");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        mCancelDone.setLayoutData(data);
        mCancelDone.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!mFinished)
                    mCancel = true;
                else
                    shell.close();
            }
        });
    }
    private Font findFont(Display display) {
        String fontStr = PrefsDialog.getStore().getString("textOutputFont");
        if (fontStr != null) {
            FontData fdat = new FontData(fontStr);
            if (fdat != null)
                return new Font(display, fdat);
        }
        return null;
    }
    class Gatherer extends Thread implements IShellOutputReceiver {
        public static final int RESULT_UNKNOWN = 0;
        public static final int RESULT_SUCCESS = 1;
        public static final int RESULT_FAILURE = 2;
        public static final int RESULT_CANCELLED = 3;
        private Shell mShell;
        private String mCommand;
        private Text mText;
        private int mResult;
        private IDevice mDevice;
        public Gatherer(Shell shell, IDevice device, String command, Text text) {
            mShell = shell;
            mDevice = device;
            mCommand = command;
            mText = text;
            mResult = RESULT_UNKNOWN;
            mCancel = false;
        }
        @Override
        public void run() {
            if (mDevice == null) {
                Log.w("ddms", "Cannot execute command: no device selected.");
                mResult = RESULT_FAILURE;
            } else {
                try {
                    mDevice.executeShellCommand(mCommand, this);
                    if (mCancel)
                        mResult = RESULT_CANCELLED;
                    else
                        mResult = RESULT_SUCCESS;
                }
                catch (IOException ioe) {
                    Log.w("ddms", "Remote exec failed: " + ioe.getMessage());
                    mResult = RESULT_FAILURE;
                }
            }
            mShell.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateForResult(mResult);
                }
            });
        }
        public void addOutput(byte[] data, int offset, int length) {
            Log.v("ddms", "received " + length + " bytes");
            try {
                final String text;
                text = new String(data, offset, length, "ISO-8859-1");
                mText.getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        mText.append(text);
                    }
                });
            }
            catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();      
            }
        }
        public void flush() {
        }
        public boolean isCancelled() {
            return mCancel;
        }
    };
    private void executeCommand(Shell shell, IDevice device) {
        Gatherer gath = new Gatherer(shell, device, commandString(), mText);
        gath.start();
    }
    private void updateForResult(int result) {
        if (result == Gatherer.RESULT_SUCCESS) {
            mStatusLabel.setText("Successfully executed '"
                + shortCommandString() + "'");
            mSave.setEnabled(true);
        } else if (result == Gatherer.RESULT_CANCELLED) {
            mStatusLabel.setText("Execution cancelled; partial results below");
            mSave.setEnabled(true);     
        } else if (result == Gatherer.RESULT_FAILURE) {
            mStatusLabel.setText("Failed");
        }
        mStatusLabel.pack();
        mCancelDone.setText("Done");
        mFinished = true;
    }
    private void saveText(Shell shell) {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        String fileName;
        dlg.setText("Save output...");
        dlg.setFileName(defaultFileName());
        dlg.setFilterPath(PrefsDialog.getStore().getString("lastTextSaveDir"));
        dlg.setFilterNames(new String[] {
            "Text Files (*.txt)"
        });
        dlg.setFilterExtensions(new String[] {
            "*.txt"
        });
        fileName = dlg.open();
        if (fileName != null) {
            PrefsDialog.getStore().setValue("lastTextSaveDir",
                                            dlg.getFilterPath());
            Log.d("ddms", "Saving output to " + fileName);
            String text = mText.getText();
            byte[] ascii;
            try {
                ascii = text.getBytes("ISO-8859-1");
            }
            catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
                ascii = new byte[0];
            }
            try {
                int length = ascii.length;
                FileOutputStream outFile = new FileOutputStream(fileName);
                BufferedOutputStream out = new BufferedOutputStream(outFile);
                for (int i = 0; i < length; i++) {
                    if (i < length-1 &&
                        ascii[i] == 0x0d && ascii[i+1] == 0x0a)
                    {
                        continue;
                    }
                    out.write(ascii[i]);
                }
                out.close();        
            }
            catch (IOException ioe) {
                Log.w("ddms", "Unable to save " + fileName + ": " + ioe);
            }
        }
    }
    private String commandString() {
        return mCommand;
    }
    private String defaultFileName() {
        return mFileName;
    }
    private String shortCommandString() {
        String str = commandString();
        if (str.length() > 50)
            return str.substring(0, 50) + "...";
        else
            return str;
    }
}
