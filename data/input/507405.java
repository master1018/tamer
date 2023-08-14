public class AvdManagerPage extends Composite implements ISdkListener {
    private AvdSelector mAvdSelector;
    private final UpdaterData mUpdaterData;
    public AvdManagerPage(Composite parent, UpdaterData updaterData) {
        super(parent, SWT.BORDER);
        mUpdaterData = updaterData;
        mUpdaterData.addListeners(this);
        createContents(this);
        postCreate();  
    }
    private void createContents(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData());
        try {
            label.setText(String.format(
                    "List of existing Android Virtual Devices located at %s",
                    AvdManager.getBaseAvdFolder()));
        } catch (AndroidLocationException e) {
            label.setText(e.getMessage());
        }
        mAvdSelector = new AvdSelector(parent,
                mUpdaterData.getOsSdkRoot(),
                mUpdaterData.getAvdManager(),
                DisplayMode.MANAGER,
                mUpdaterData.getSdkLog());
        mAvdSelector.setSettingsController(mUpdaterData.getSettingsController());
    }
    @Override
    public void dispose() {
        mUpdaterData.removeListener(this);
        super.dispose();
    }
    @Override
    protected void checkSubclass() {
    }
    private void postCreate() {
    }
    public void onSdkChange(boolean init) {
        mAvdSelector.refresh(false );
    }
}
