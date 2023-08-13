public final class LayoutCreatorDialog extends GridDialog {
    private ConfigurationSelector mSelector;
    private Composite mStatusComposite;
    private Label mStatusLabel;
    private Label mStatusImage;
    private final FolderConfiguration mConfig = new FolderConfiguration();
    private final String mFileName;
    private final IAndroidTarget mTarget;
    public LayoutCreatorDialog(Shell parentShell, String fileName, IAndroidTarget target,
            FolderConfiguration config) {
        super(parentShell, 1, false);
        mFileName = fileName;
        mTarget = target;
        mConfig.set(config);
    }
    @Override
    public void createDialogContent(Composite parent) {
        new Label(parent, SWT.NONE).setText(
                String.format("Configuration for the alternate version of %1$s", mFileName));
        mSelector = new ConfigurationSelector(parent, false );
        mSelector.setConfiguration(mConfig);
        GridData gd = new GridData();
        gd.widthHint = ConfigurationSelector.WIDTH_HINT;
        gd.heightHint = ConfigurationSelector.HEIGHT_HINT;
        mSelector.setLayoutData(gd);
        mSelector.setOnChangeListener(new Runnable() {
            public void run() {
                ConfigurationState state = mSelector.getState();
                switch (state) {
                    case OK:
                        mSelector.getConfiguration(mConfig);
                        resetStatus();
                        mStatusImage.setImage(null);
                        getButton(IDialogConstants.OK_ID).setEnabled(true);
                        break;
                    case INVALID_CONFIG:
                        ResourceQualifier invalidQualifier = mSelector.getInvalidQualifier();
                        mStatusLabel.setText(String.format(
                                "Invalid Configuration: %1$s has no filter set.",
                                invalidQualifier.getName()));
                        mStatusImage.setImage(IconFactory.getInstance().getIcon("warning")); 
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                        break;
                    case REGION_WITHOUT_LANGUAGE:
                        mStatusLabel.setText(
                                "The Region qualifier requires the Language qualifier.");
                        mStatusImage.setImage(IconFactory.getInstance().getIcon("warning")); 
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                        break;
                }
                mStatusComposite.layout();
            }
        });
        mStatusComposite = new Composite(parent, SWT.NONE);
        mStatusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gl = new GridLayout(2, false);
        mStatusComposite.setLayout(gl);
        gl.marginHeight = gl.marginWidth = 0;
        mStatusImage = new Label(mStatusComposite, SWT.NONE);
        mStatusLabel = new Label(mStatusComposite, SWT.NONE);
        mStatusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        resetStatus();
    }
    public void getConfiguration(FolderConfiguration config) {
        config.set(mConfig);
    }
    private void resetStatus() {
        String displayString = Dialog.shortenText(String.format("New File: res/%1$s/%2$s",
                mConfig.getFolderName(ResourceFolderType.LAYOUT, mTarget), mFileName),
                mStatusLabel);
        mStatusLabel.setText(displayString);
    }
}
