class HardwarePropertyChooser extends GridDialog {
    private final Map<String, HardwareProperty> mProperties;
    private final Collection<String> mExceptProperties;
    private HardwareProperty mChosenProperty;
    private Label mTypeLabel;
    private Label mDescriptionLabel;
    HardwarePropertyChooser(Shell parentShell, Map<String, HardwareProperty> properties,
            Collection<String> exceptProperties) {
        super(parentShell, 2, false);
        mProperties = properties;
        mExceptProperties = exceptProperties;
    }
    public HardwareProperty getProperty() {
        return mChosenProperty;
    }
    @Override
    public void createDialogContent(Composite parent) {
        Label l = new Label(parent, SWT.NONE);
        l.setText("Property:");
        final Combo c = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        final ArrayList<String> indexToName = new ArrayList<String>();
        for (Entry<String, HardwareProperty> entry : mProperties.entrySet()) {
            if (mExceptProperties.contains(entry.getKey()) == false) {
                c.add(entry.getValue().getAbstract());
                indexToName.add(entry.getKey());
            }
        }
        boolean hasValues = true;
        if (indexToName.size() == 0) {
            hasValues = false;
            c.add("No properties");
            c.select(0);
            c.setEnabled(false);
        }
        c.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int index = c.getSelectionIndex();
                String name = indexToName.get(index);
                processSelection(name, true );
            }
        });
        l = new Label(parent, SWT.NONE);
        l.setText("Type:");
        mTypeLabel = new Label(parent, SWT.NONE);
        l = new Label(parent, SWT.NONE);
        l.setText("Description:");
        mDescriptionLabel = new Label(parent, SWT.NONE);
        if (hasValues) {
            c.select(0);
            processSelection(indexToName.get(0), false );
        }
    }
    private void processSelection(String name, boolean pack) {
        mChosenProperty = mProperties.get(name);
        mTypeLabel.setText(mChosenProperty.getType().getValue());
        String desc = mChosenProperty.getDescription();
        if (desc != null) {
            mDescriptionLabel.setText(mChosenProperty.getDescription());
        } else {
            mDescriptionLabel.setText("N/A");
        }
        if (pack) {
            getShell().pack();
        }
    }
}
