public class ConfigurationSelector extends Composite {
    public static final int WIDTH_HINT = 600;
    public static final int HEIGHT_HINT = 250;
    private Runnable mOnChangeListener;
    private TableViewer mFullTableViewer;
    private TableViewer mSelectionTableViewer;
    private Button mAddButton;
    private Button mRemoveButton;
    private StackLayout mStackLayout;
    private boolean mOnRefresh = false;
    private final FolderConfiguration mBaseConfiguration = new FolderConfiguration();
    private final FolderConfiguration mSelectedConfiguration = new FolderConfiguration();
    private final HashMap<Class<? extends ResourceQualifier>, QualifierEditBase> mUiMap =
        new HashMap<Class<? extends ResourceQualifier>, QualifierEditBase>();
    private final boolean mDeviceMode;
    private Composite mQualifierEditParent;
    private IQualifierFilter mQualifierFilter;
    private static class DigitVerifier implements VerifyListener {
        public void verifyText(VerifyEvent e) {
            for (int i = 0 ; i < e.text.length(); i++) {
                char letter = e.text.charAt(i);
                if (letter < '0' || letter > '9') {
                    e.doit = false;
                    return;
                }
            }
        }
    }
    public static class MobileCodeVerifier extends DigitVerifier {
        @Override
        public void verifyText(VerifyEvent e) {
            super.verifyText(e);
            if (e.doit) {
                if (e.text.length() - e.end + e.start +
                        ((Text)e.getSource()).getText().length() > 3) {
                    e.doit = false;
                }
            }
        }
    }
    public static class LanguageRegionVerifier implements VerifyListener {
        public void verifyText(VerifyEvent e) {
            if (e.text.length() - e.end + e.start + ((Combo)e.getSource()).getText().length() > 2) {
                e.doit = false;
                return;
            }
            for (int i = 0 ; i < e.text.length(); i++) {
                char letter = e.text.charAt(i);
                if ((letter < 'a' || letter > 'z') && (letter < 'A' || letter > 'Z')) {
                    e.doit = false;
                    return;
                }
            }
        }
    }
    public static class DensityVerifier extends DigitVerifier { }
    public static class DimensionVerifier extends DigitVerifier { }
    public enum ConfigurationState {
        OK, INVALID_CONFIG, REGION_WITHOUT_LANGUAGE;
    }
    public interface IQualifierFilter {
        boolean accept(ResourceQualifier qualifier);
    }
    public ConfigurationSelector(Composite parent, boolean deviceMode) {
        super(parent, SWT.NONE);
        mDeviceMode  = deviceMode;
        mBaseConfiguration.createDefault();
        GridLayout gl = new GridLayout(4, false);
        gl.marginWidth = gl.marginHeight = 0;
        setLayout(gl);
        final Table fullTable = new Table(this, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        fullTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        fullTable.setHeaderVisible(true);
        fullTable.setLinesVisible(true);
        final TableColumn fullTableColumn = new TableColumn(fullTable, SWT.LEFT);
        fullTableColumn.setText("Available Qualifiers");
        fullTable.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = fullTable.getClientArea();
                fullTableColumn.setWidth(r.width);
            }
        });
        mFullTableViewer = new TableViewer(fullTable);
        mFullTableViewer.setContentProvider(new QualifierContentProvider());
        mFullTableViewer.setLabelProvider(new QualifierLabelProvider(
                false ));
        mFullTableViewer.setInput(mBaseConfiguration);
        mFullTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection structSelection = (IStructuredSelection)selection;
                    Object first = structSelection.getFirstElement();
                    if (first instanceof ResourceQualifier) {
                        mAddButton.setEnabled(true);
                        return;
                    }
                }
                mAddButton.setEnabled(false);
            }
        });
        Composite buttonComposite = new Composite(this, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginWidth = gl.marginHeight = 0;
        buttonComposite.setLayout(gl);
        buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        new Composite(buttonComposite, SWT.NONE);
        mAddButton = new Button(buttonComposite, SWT.BORDER | SWT.PUSH);
        mAddButton.setText("->");
        mAddButton.setEnabled(false);
        mAddButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection =
                    (IStructuredSelection)mFullTableViewer.getSelection();
                Object first = selection.getFirstElement();
                if (first instanceof ResourceQualifier) {
                    ResourceQualifier qualifier = (ResourceQualifier)first;
                    mBaseConfiguration.removeQualifier(qualifier);
                    mSelectedConfiguration.addQualifier(qualifier);
                    mFullTableViewer.refresh();
                    mSelectionTableViewer.refresh();
                    mSelectionTableViewer.setSelection(new StructuredSelection(qualifier), true);
                    onChange(false );
                }
            }
        });
        mRemoveButton = new Button(buttonComposite, SWT.BORDER | SWT.PUSH);
        mRemoveButton.setText("<-");
        mRemoveButton.setEnabled(false);
        mRemoveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection =
                    (IStructuredSelection)mSelectionTableViewer.getSelection();
                Object first = selection.getFirstElement();
                if (first instanceof ResourceQualifier) {
                    ResourceQualifier qualifier = (ResourceQualifier)first;
                    mSelectedConfiguration.removeQualifier(qualifier);
                    mBaseConfiguration.addQualifier(qualifier);
                    mFullTableViewer.refresh();
                    mSelectionTableViewer.refresh();
                    onChange(false );
                }
            }
        });
        final Table selectionTable = new Table(this, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        selectionTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        selectionTable.setHeaderVisible(true);
        selectionTable.setLinesVisible(true);
        final TableColumn selectionTableColumn = new TableColumn(selectionTable, SWT.LEFT);
        selectionTableColumn.setText("Chosen Qualifiers");
        selectionTable.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = selectionTable.getClientArea();
                selectionTableColumn.setWidth(r.width);
            }
        });
        mSelectionTableViewer = new TableViewer(selectionTable);
        mSelectionTableViewer.setContentProvider(new QualifierContentProvider());
        mSelectionTableViewer.setLabelProvider(new QualifierLabelProvider(
                true ));
        mSelectionTableViewer.setInput(mSelectedConfiguration);
        mSelectionTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                if (mOnRefresh) {
                    return;
                }
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection structSelection = (IStructuredSelection)selection;
                    if (structSelection.isEmpty() == false) {
                        Object first = structSelection.getFirstElement();
                        if (first instanceof ResourceQualifier) {
                            mRemoveButton.setEnabled(true);
                            QualifierEditBase composite = mUiMap.get(first.getClass());
                            if (composite != null) {
                                composite.setQualifier((ResourceQualifier)first);
                            }
                            mStackLayout.topControl = composite;
                            mQualifierEditParent.layout();
                            return;
                        }
                    } else {
                        mStackLayout.topControl = null;
                        mQualifierEditParent.layout();
                    }
                }
                mRemoveButton.setEnabled(false);
            }
        });
        mQualifierEditParent = new Composite(this, SWT.NONE);
        mQualifierEditParent.setLayout(mStackLayout = new StackLayout());
        mQualifierEditParent.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        mUiMap.put(CountryCodeQualifier.class, new MCCEdit(mQualifierEditParent));
        mUiMap.put(NetworkCodeQualifier.class, new MNCEdit(mQualifierEditParent));
        mUiMap.put(LanguageQualifier.class, new LanguageEdit(mQualifierEditParent));
        mUiMap.put(RegionQualifier.class, new RegionEdit(mQualifierEditParent));
        mUiMap.put(ScreenSizeQualifier.class, new ScreenSizeEdit(mQualifierEditParent));
        mUiMap.put(ScreenRatioQualifier.class, new ScreenRatioEdit(mQualifierEditParent));
        mUiMap.put(ScreenOrientationQualifier.class, new OrientationEdit(mQualifierEditParent));
        mUiMap.put(PixelDensityQualifier.class, new PixelDensityEdit(mQualifierEditParent));
        mUiMap.put(TouchScreenQualifier.class, new TouchEdit(mQualifierEditParent));
        mUiMap.put(KeyboardStateQualifier.class, new KeyboardEdit(mQualifierEditParent));
        mUiMap.put(TextInputMethodQualifier.class, new TextInputEdit(mQualifierEditParent));
        mUiMap.put(NavigationMethodQualifier.class, new NavigationEdit(mQualifierEditParent));
        mUiMap.put(ScreenDimensionQualifier.class, new ScreenDimensionEdit(mQualifierEditParent));
        mUiMap.put(VersionQualifier.class, new VersionEdit(mQualifierEditParent));
    }
    public void setQualifierFilter(IQualifierFilter filter) {
        mQualifierFilter = filter;
    }
    public void setOnChangeListener(Runnable listener) {
        mOnChangeListener = listener;
    }
    public void setConfiguration(FolderConfiguration config) {
        mSelectedConfiguration.set(config);
        mSelectionTableViewer.refresh();
        mBaseConfiguration.substract(mSelectedConfiguration);
        mFullTableViewer.refresh();
    }
    public boolean setConfiguration(String[] folderSegments) {
        FolderConfiguration config = ResourceManager.getInstance().getConfig(folderSegments);
        if (config == null) {
            return false;
        }
        setConfiguration(config);
        return true;
    }
    public boolean setConfiguration(String folderName) {
        String[] folderSegments = folderName.split(FolderConfiguration.QUALIFIER_SEP);
        return setConfiguration(folderSegments);
    }
    public void getConfiguration(FolderConfiguration config) {
        config.set(mSelectedConfiguration);
    }
    public ConfigurationState getState() {
        if (mSelectedConfiguration.getInvalidQualifier() != null) {
            return ConfigurationState.INVALID_CONFIG;
        }
        if (mSelectedConfiguration.checkRegion() == false) {
            return ConfigurationState.REGION_WITHOUT_LANGUAGE;
        }
        return ConfigurationState.OK;
    }
    public ResourceQualifier getInvalidQualifier() {
        return mSelectedConfiguration.getInvalidQualifier();
    }
    private void onChange(boolean keepSelection) {
        ISelection selection = null;
        if (keepSelection) {
            mOnRefresh = true;
            selection = mSelectionTableViewer.getSelection();
        }
        mSelectionTableViewer.refresh(true);
        if (keepSelection) {
            mSelectionTableViewer.setSelection(selection);
            mOnRefresh = false;
        }
        if (mOnChangeListener != null) {
            mOnChangeListener.run();
        }
    }
    private class QualifierContentProvider implements IStructuredContentProvider {
        private FolderConfiguration mInput;
        public QualifierContentProvider() {
        }
        public void dispose() {
        }
        public Object[] getElements(Object inputElement) {
            if (mQualifierFilter == null) {
                return mInput.getQualifiers();
            }
            ArrayList<ResourceQualifier> list = new ArrayList<ResourceQualifier>();
            for (ResourceQualifier qual : mInput.getQualifiers()) {
                if (mQualifierFilter.accept(qual)) {
                    list.add(qual);
                }
            }
            return list.toArray();
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            mInput = null;
            if (newInput instanceof FolderConfiguration) {
                mInput = (FolderConfiguration)newInput;
            }
        }
    }
    private static class QualifierLabelProvider implements ITableLabelProvider {
        private final boolean mShowQualifierValue;
        public QualifierLabelProvider(boolean showQualifierValue) {
            mShowQualifierValue = showQualifierValue;
        }
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ResourceQualifier) {
                if (mShowQualifierValue) {
                    String value = ((ResourceQualifier)element).getStringValue();
                    if (value.length() == 0) {
                        return String.format("%1$s (?)",
                                ((ResourceQualifier)element).getShortName());
                    } else {
                        return value;
                    }
                } else {
                    return ((ResourceQualifier)element).getShortName();
                }
            }
            return null;
        }
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ResourceQualifier) {
                return ((ResourceQualifier)element).getIcon();
            }
            return null;
        }
        public void addListener(ILabelProviderListener listener) {
        }
        public void dispose() {
        }
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void removeListener(ILabelProviderListener listener) {
        }
    }
    private abstract static class QualifierEditBase extends Composite {
        public QualifierEditBase(Composite parent, String title) {
            super(parent, SWT.NONE);
            setLayout(new GridLayout(1, false));
            new Label(this, SWT.NONE).setText(title);
        }
        public abstract void setQualifier(ResourceQualifier qualifier);
    }
    private class MCCEdit extends QualifierEditBase {
        private Text mText;
        public MCCEdit(Composite parent) {
            super(parent, CountryCodeQualifier.NAME);
            mText = new Text(this, SWT.BORDER);
            mText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mText.addVerifyListener(new MobileCodeVerifier());
            mText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onTextChange();
                }
            });
            mText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    onTextChange();
                }
            });
            new Label(this, SWT.NONE).setText("(3 digit code)");
        }
        private void onTextChange() {
            String value = mText.getText();
            if (value.length() == 0) {
                mSelectedConfiguration.setCountryCodeQualifier(new CountryCodeQualifier());
            } else {
                try {
                    CountryCodeQualifier qualifier = CountryCodeQualifier.getQualifier(
                            CountryCodeQualifier.getFolderSegment(Integer.parseInt(value)));
                    if (qualifier != null) {
                        mSelectedConfiguration.setCountryCodeQualifier(qualifier);
                    } else {
                        mSelectedConfiguration.setCountryCodeQualifier(new CountryCodeQualifier());
                    }
                } catch (NumberFormatException nfe) {
                    mSelectedConfiguration.setCountryCodeQualifier(new CountryCodeQualifier());
                }
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            CountryCodeQualifier q = (CountryCodeQualifier)qualifier;
            mText.setText(Integer.toString(q.getCode()));
        }
    }
    private class MNCEdit extends QualifierEditBase {
        private Text mText;
        public MNCEdit(Composite parent) {
            super(parent, NetworkCodeQualifier.NAME);
            mText = new Text(this, SWT.BORDER);
            mText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mText.addVerifyListener(new MobileCodeVerifier());
            mText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onTextChange();
                }
            });
            mText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    onTextChange();
                }
            });
            new Label(this, SWT.NONE).setText("(1-3 digit code)");
        }
        private void onTextChange() {
            String value = mText.getText();
            if (value.length() == 0) {
                mSelectedConfiguration.setNetworkCodeQualifier(new NetworkCodeQualifier());
            } else {
                try {
                    NetworkCodeQualifier qualifier = NetworkCodeQualifier.getQualifier(
                            NetworkCodeQualifier.getFolderSegment(Integer.parseInt(value)));
                    if (qualifier != null) {
                        mSelectedConfiguration.setNetworkCodeQualifier(qualifier);
                    } else {
                        mSelectedConfiguration.setNetworkCodeQualifier(new NetworkCodeQualifier());
                    }
                } catch (NumberFormatException nfe) {
                    mSelectedConfiguration.setNetworkCodeQualifier(new NetworkCodeQualifier());
                }
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            NetworkCodeQualifier q = (NetworkCodeQualifier)qualifier;
            mText.setText(Integer.toString(q.getCode()));
        }
    }
    private class LanguageEdit extends QualifierEditBase {
        private Combo mLanguage;
        public LanguageEdit(Composite parent) {
            super(parent, LanguageQualifier.NAME);
            mLanguage = new Combo(this, SWT.DROP_DOWN);
            mLanguage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mLanguage.addVerifyListener(new LanguageRegionVerifier());
            mLanguage.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onLanguageChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onLanguageChange();
                }
            });
            mLanguage.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onLanguageChange();
                }
            });
            new Label(this, SWT.NONE).setText("(2 letter code)");
        }
        private void onLanguageChange() {
            String value = mLanguage.getText();
            if (value.length() == 0) {
                mSelectedConfiguration.setLanguageQualifier(new LanguageQualifier());
            } else {
                LanguageQualifier qualifier = null;
                String segment = LanguageQualifier.getFolderSegment(value);
                if (segment != null) {
                    qualifier = LanguageQualifier.getQualifier(segment);
                }
                if (qualifier != null) {
                    mSelectedConfiguration.setLanguageQualifier(qualifier);
                } else {
                    mSelectedConfiguration.setLanguageQualifier(new LanguageQualifier());
                }
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            LanguageQualifier q = (LanguageQualifier)qualifier;
            String value = q.getValue();
            if (value != null) {
                mLanguage.setText(value);
            }
        }
    }
    private class RegionEdit extends QualifierEditBase {
        private Combo mRegion;
        public RegionEdit(Composite parent) {
            super(parent, RegionQualifier.NAME);
            mRegion = new Combo(this, SWT.DROP_DOWN);
            mRegion.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mRegion.addVerifyListener(new LanguageRegionVerifier());
            mRegion.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onRegionChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onRegionChange();
                }
            });
            mRegion.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onRegionChange();
                }
            });
            new Label(this, SWT.NONE).setText("(2 letter code)");
        }
        private void onRegionChange() {
            String value = mRegion.getText();
            if (value.length() == 0) {
                mSelectedConfiguration.setRegionQualifier(new RegionQualifier());
            } else {
                RegionQualifier qualifier = null;
                String segment = RegionQualifier.getFolderSegment(value);
                if (segment != null) {
                    qualifier = RegionQualifier.getQualifier(segment);
                }
                if (qualifier != null) {
                    mSelectedConfiguration.setRegionQualifier(qualifier);
                } else {
                    mSelectedConfiguration.setRegionQualifier(new RegionQualifier());
                }
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            RegionQualifier q = (RegionQualifier)qualifier;
            String value = q.getValue();
            if (value != null) {
                mRegion.setText(q.getValue());
            }
        }
    }
    private class ScreenSizeEdit extends QualifierEditBase {
        private Combo mSize;
        public ScreenSizeEdit(Composite parent) {
            super(parent, ScreenSizeQualifier.NAME);
            mSize = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            ScreenSize[] ssValues = ScreenSize.values();
            for (ScreenSize value : ssValues) {
                mSize.add(value.getDisplayValue());
            }
            mSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mSize.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onScreenSizeChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onScreenSizeChange();
                }
            });
        }
        protected void onScreenSizeChange() {
            int index = mSize.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setScreenSizeQualifier(new ScreenSizeQualifier(
                    ScreenSize.getByIndex(index)));
            } else {
                mSelectedConfiguration.setScreenSizeQualifier(
                        new ScreenSizeQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            ScreenSizeQualifier q = (ScreenSizeQualifier)qualifier;
            ScreenSize value = q.getValue();
            if (value == null) {
                mSize.clearSelection();
            } else {
                mSize.select(ScreenSize.getIndex(value));
            }
        }
    }
    private class ScreenRatioEdit extends QualifierEditBase {
        private Combo mSize;
        public ScreenRatioEdit(Composite parent) {
            super(parent, ScreenRatioQualifier.NAME);
            mSize = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            ScreenRatio[] srValues = ScreenRatio.values();
            for (ScreenRatio value : srValues) {
                mSize.add(value.getDisplayValue());
            }
            mSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mSize.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onScreenRatioChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onScreenRatioChange();
                }
            });
        }
        protected void onScreenRatioChange() {
            int index = mSize.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setScreenRatioQualifier(new ScreenRatioQualifier(
                        ScreenRatio.getByIndex(index)));
            } else {
                mSelectedConfiguration.setScreenRatioQualifier(
                        new ScreenRatioQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            ScreenRatioQualifier q = (ScreenRatioQualifier)qualifier;
            ScreenRatio value = q.getValue();
            if (value == null) {
                mSize.clearSelection();
            } else {
                mSize.select(ScreenRatio.getIndex(value));
            }
        }
    }
    private class OrientationEdit extends QualifierEditBase {
        private Combo mOrientation;
        public OrientationEdit(Composite parent) {
            super(parent, ScreenOrientationQualifier.NAME);
            mOrientation = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            ScreenOrientation[] soValues = ScreenOrientation.values();
            for (ScreenOrientation value : soValues) {
                mOrientation.add(value.getDisplayValue());
            }
            mOrientation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mOrientation.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onOrientationChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onOrientationChange();
                }
            });
        }
        protected void onOrientationChange() {
            int index = mOrientation.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setScreenOrientationQualifier(new ScreenOrientationQualifier(
                    ScreenOrientation.getByIndex(index)));
            } else {
                mSelectedConfiguration.setScreenOrientationQualifier(
                        new ScreenOrientationQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            ScreenOrientationQualifier q = (ScreenOrientationQualifier)qualifier;
            ScreenOrientation value = q.getValue();
            if (value == null) {
                mOrientation.clearSelection();
            } else {
                mOrientation.select(ScreenOrientation.getIndex(value));
            }
        }
    }
    private class PixelDensityEdit extends QualifierEditBase {
        private Combo mDensity;
        public PixelDensityEdit(Composite parent) {
            super(parent, PixelDensityQualifier.NAME);
            mDensity = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            Density[] soValues = Density.values();
            for (Density value : soValues) {
                if (mDeviceMode == false || value != Density.NODPI) {
                    mDensity.add(value.getDisplayValue());
                }
            }
            mDensity.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mDensity.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onDensityChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onDensityChange();
                }
            });
        }
        private void onDensityChange() {
            int index = mDensity.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setPixelDensityQualifier(new PixelDensityQualifier(
                    Density.getByIndex(index)));
            } else {
                mSelectedConfiguration.setPixelDensityQualifier(
                        new PixelDensityQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            PixelDensityQualifier q = (PixelDensityQualifier)qualifier;
            Density value = q.getValue();
            if (value == null) {
                mDensity.clearSelection();
            } else {
                mDensity.select(Density.getIndex(value));
            }
        }
    }
    private class TouchEdit extends QualifierEditBase {
        private Combo mTouchScreen;
        public TouchEdit(Composite parent) {
            super(parent, TouchScreenQualifier.NAME);
            mTouchScreen = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            TouchScreenType[] tstValues = TouchScreenType.values();
            for (TouchScreenType value : tstValues) {
                mTouchScreen.add(value.getDisplayValue());
            }
            mTouchScreen.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mTouchScreen.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onTouchChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onTouchChange();
                }
            });
        }
        protected void onTouchChange() {
            int index = mTouchScreen.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setTouchTypeQualifier(new TouchScreenQualifier(
                        TouchScreenType.getByIndex(index)));
            } else {
                mSelectedConfiguration.setTouchTypeQualifier(new TouchScreenQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            TouchScreenQualifier q = (TouchScreenQualifier)qualifier;
            TouchScreenType value = q.getValue();
            if (value == null) {
                mTouchScreen.clearSelection();
            } else {
                mTouchScreen.select(TouchScreenType.getIndex(value));
            }
        }
    }
    private class KeyboardEdit extends QualifierEditBase {
        private Combo mKeyboard;
        public KeyboardEdit(Composite parent) {
            super(parent, KeyboardStateQualifier.NAME);
            mKeyboard = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            KeyboardState[] ksValues = KeyboardState.values();
            for (KeyboardState value : ksValues) {
                mKeyboard.add(value.getDisplayValue());
            }
            mKeyboard.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mKeyboard.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onKeyboardChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onKeyboardChange();
                }
            });
        }
        protected void onKeyboardChange() {
            int index = mKeyboard.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setKeyboardStateQualifier(new KeyboardStateQualifier(
                        KeyboardState.getByIndex(index)));
            } else {
                mSelectedConfiguration.setKeyboardStateQualifier(
                        new KeyboardStateQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            KeyboardStateQualifier q = (KeyboardStateQualifier)qualifier;
            KeyboardState value = q.getValue();
            if (value == null) {
                mKeyboard.clearSelection();
            } else {
                mKeyboard.select(KeyboardState.getIndex(value));
            }
        }
    }
    private class TextInputEdit extends QualifierEditBase {
        private Combo mTextInput;
        public TextInputEdit(Composite parent) {
            super(parent, TextInputMethodQualifier.NAME);
            mTextInput = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            TextInputMethod[] timValues = TextInputMethod.values();
            for (TextInputMethod value : timValues) {
                mTextInput.add(value.getDisplayValue());
            }
            mTextInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mTextInput.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onTextInputChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onTextInputChange();
                }
            });
        }
        protected void onTextInputChange() {
            int index = mTextInput.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setTextInputMethodQualifier(new TextInputMethodQualifier(
                        TextInputMethod.getByIndex(index)));
            } else {
                mSelectedConfiguration.setTextInputMethodQualifier(
                        new TextInputMethodQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            TextInputMethodQualifier q = (TextInputMethodQualifier)qualifier;
            TextInputMethod value = q.getValue();
            if (value == null) {
                mTextInput.clearSelection();
            } else {
                mTextInput.select(TextInputMethod.getIndex(value));
            }
        }
    }
    private class NavigationEdit extends QualifierEditBase {
        private Combo mNavigation;
        public NavigationEdit(Composite parent) {
            super(parent, NavigationMethodQualifier.NAME);
            mNavigation = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
            NavigationMethod[] nmValues = NavigationMethod.values();
            for (NavigationMethod value : nmValues) {
                mNavigation.add(value.getDisplayValue());
            }
            mNavigation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mNavigation.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    onNavigationChange();
                }
                public void widgetSelected(SelectionEvent e) {
                    onNavigationChange();
                }
            });
        }
        protected void onNavigationChange() {
            int index = mNavigation.getSelectionIndex();
            if (index != -1) {
                mSelectedConfiguration.setNavigationMethodQualifier(new NavigationMethodQualifier(
                        NavigationMethod.getByIndex(index)));
            } else {
                mSelectedConfiguration.setNavigationMethodQualifier(
                        new NavigationMethodQualifier());
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            NavigationMethodQualifier q = (NavigationMethodQualifier)qualifier;
            NavigationMethod value = q.getValue();
            if (value == null) {
                mNavigation.clearSelection();
            } else {
                mNavigation.select(NavigationMethod.getIndex(value));
            }
        }
    }
    private class ScreenDimensionEdit extends QualifierEditBase {
        private Text mSize1;
        private Text mSize2;
        public ScreenDimensionEdit(Composite parent) {
            super(parent, ScreenDimensionQualifier.NAME);
            ModifyListener modifyListener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onSizeChange();
                }
            };
            FocusAdapter focusListener = new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    onSizeChange();
                }
            };
            mSize1 = new Text(this, SWT.BORDER);
            mSize1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mSize1.addVerifyListener(new DimensionVerifier());
            mSize1.addModifyListener(modifyListener);
            mSize1.addFocusListener(focusListener);
            mSize2 = new Text(this, SWT.BORDER);
            mSize2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mSize2.addVerifyListener(new DimensionVerifier());
            mSize2.addModifyListener(modifyListener);
            mSize2.addFocusListener(focusListener);
        }
        private void onSizeChange() {
            String size1 = mSize1.getText();
            String size2 = mSize2.getText();
            if (size1.length() == 0 || size2.length() == 0) {
                mSelectedConfiguration.setScreenDimensionQualifier(new ScreenDimensionQualifier());
            } else {
                ScreenDimensionQualifier qualifier = ScreenDimensionQualifier.getQualifier(size1,
                        size2);
                if (qualifier != null) {
                    mSelectedConfiguration.setScreenDimensionQualifier(qualifier);
                } else {
                    mSelectedConfiguration.setScreenDimensionQualifier(
                            new ScreenDimensionQualifier());
                }
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            ScreenDimensionQualifier q = (ScreenDimensionQualifier)qualifier;
            mSize1.setText(Integer.toString(q.getValue1()));
            mSize2.setText(Integer.toString(q.getValue2()));
        }
    }
    private class VersionEdit extends QualifierEditBase {
        private Text mText;
        public VersionEdit(Composite parent) {
            super(parent, VersionQualifier.NAME);
            mText = new Text(this, SWT.BORDER);
            mText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            mText.addVerifyListener(new MobileCodeVerifier());
            mText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    onVersionChange();
                }
            });
            mText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    onVersionChange();
                }
            });
            new Label(this, SWT.NONE).setText("(Platform API level)");
        }
        private void onVersionChange() {
            String value = mText.getText();
            if (value.length() == 0) {
                mSelectedConfiguration.setVersionQualifier(new VersionQualifier());
            } else {
                try {
                    VersionQualifier qualifier = VersionQualifier.getQualifier(
                            VersionQualifier.getFolderSegment(Integer.parseInt(value)));
                    if (qualifier != null) {
                        mSelectedConfiguration.setVersionQualifier(qualifier);
                    } else {
                        mSelectedConfiguration.setVersionQualifier(new VersionQualifier());
                    }
                } catch (NumberFormatException nfe) {
                    mSelectedConfiguration.setVersionQualifier(new VersionQualifier());
                }
            }
            onChange(true );
        }
        @Override
        public void setQualifier(ResourceQualifier qualifier) {
            VersionQualifier q = (VersionQualifier)qualifier;
            mText.setText(Integer.toString(q.getVersion()));
        }
    }
}
