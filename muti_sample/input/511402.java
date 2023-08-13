class ResolutionChooserDialog extends GridDialog {
    public final static float[] MONITOR_SIZES = new float[] {
            13.3f, 14, 15.4f, 15.6f, 17, 19, 20, 21, 24, 30,
    };
    private Button mButton;
    private Combo mScreenSizeCombo;
    private Combo mMonitorCombo;
    private Monitor[] mMonitors;
    private int mScreenSizeIndex = -1;
    private int mMonitorIndex = 0;
    ResolutionChooserDialog(Shell parentShell) {
        super(parentShell, 2, false);
    }
    int getDensity() {
        float size = MONITOR_SIZES[mScreenSizeIndex];
        Rectangle rect = mMonitors[mMonitorIndex].getBounds();
        double d = Math.sqrt(rect.width * rect.width + rect.height * rect.height) / size;
        return (int)Math.round(d);
    }
    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText("Monitor Density");
        super.configureShell(newShell);
    }
    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        mButton = getButton(IDialogConstants.OK_ID);
        mButton.setEnabled(false);
        return control;
    }
    @Override
    public void createDialogContent(Composite parent) {
        Label l = new Label(parent, SWT.NONE);
        l.setText("Screen Size:");
        mScreenSizeCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (float size : MONITOR_SIZES) {
            if (Math.round(size) == size) {
                mScreenSizeCombo.add(String.format("%.0f\"", size));
            } else {
                mScreenSizeCombo.add(String.format("%.1f\"", size));
            }
        }
        mScreenSizeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                mScreenSizeIndex = mScreenSizeCombo.getSelectionIndex();
                mButton.setEnabled(mScreenSizeIndex != -1);
            }
        });
        l = new Label(parent, SWT.NONE);
        l.setText("Resolution:");
        mMonitorCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        mMonitors = parent.getDisplay().getMonitors();
        for (Monitor m : mMonitors) {
            Rectangle r = m.getBounds();
            mMonitorCombo.add(String.format("%d x %d", r.width, r.height));
        }
        mMonitorCombo.select(mMonitorIndex);
        mMonitorCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                mMonitorIndex = mMonitorCombo.getSelectionIndex();
            }
        });
    }
}
