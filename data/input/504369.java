public class ConfigurationComposite extends Composite {
    private final static String CONFIG_STATE = "state";  
    private final static String THEME_SEPARATOR = "----------"; 
    private final static String FAKE_LOCALE_VALUE = "__"; 
    private final static int LOCALE_LANG = 0;
    private final static int LOCALE_REGION = 1;
    private Button mClippingButton;
    private Label mCurrentLayoutLabel;
    private Combo mDeviceCombo;
    private Combo mDeviceConfigCombo;
    private Combo mLocaleCombo;
    private Combo mThemeCombo;
    private Button mCreateButton;
    private int mPlatformThemeCount = 0;
    private int mDisableUpdates = 0;
    private List<LayoutDevice> mDeviceList;
    private final ArrayList<ResourceQualifier[] > mLocaleList =
        new ArrayList<ResourceQualifier[]>();
    private boolean mClipping = true;
    private final ConfigState mState = new ConfigState();
    private boolean mSdkChanged = false;
    private boolean mFirstXmlModelChange = true;
    private final IConfigListener mListener;
    private final FolderConfiguration mCurrentConfig = new FolderConfiguration();
    private IFile mEditedFile;
    private ProjectResources mResources;
    private IAndroidTarget mTarget;
    private FolderConfiguration mEditedConfig;
    public interface IConfigListener {
        void onConfigurationChange();
        void onThemeChange();
        void onCreate();
        void onClippingChange();
        ProjectResources getProjectResources();
        ProjectResources getFrameworkResources();
        Map<String, Map<String, IResourceValue>> getConfiguredProjectResources();
        Map<String, Map<String, IResourceValue>> getConfiguredFrameworkResources();
    }
    private class ConfigState {
        private final static String SEP = ":"; 
        private final static String SEP_LOCALE = "-"; 
        LayoutDevice device;
        String configName;
        ResourceQualifier[] locale;
        String theme;
        String getData() {
            StringBuilder sb = new StringBuilder();
            if (device != null) {
                sb.append(device.getName());
                sb.append(SEP);
                sb.append(configName);
                sb.append(SEP);
                if (locale != null) {
                    sb.append(((LanguageQualifier) locale[0]).getValue());
                    sb.append(SEP_LOCALE);
                    sb.append(((RegionQualifier) locale[1]).getValue());
                }
                sb.append(SEP);
                sb.append(theme);
                sb.append(SEP);
            }
            return sb.toString();
        }
        boolean setData(String data) {
            String[] values = data.split(SEP);
            if (values.length == 4) {
                for (LayoutDevice d : mDeviceList) {
                    if (d.getName().equals(values[0])) {
                        device = d;
                        FolderConfiguration config = device.getConfigs().get(values[1]);
                        if (config != null) {
                            configName = values[1];
                            locale = new ResourceQualifier[2];
                            String locales[] = values[2].split(SEP_LOCALE);
                            if (locales.length >= 2) {
                                if (locales[0].length() > 0) {
                                    locale[0] = new LanguageQualifier(locales[0]);
                                }
                                if (locales[1].length() > 0) {
                                    locale[1] = new RegionQualifier(locales[1]);
                                }
                            }
                            theme = values[3];
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (device != null) {
                sb.append(device.getName());
            } else {
                sb.append("null");
            }
            sb.append(SEP);
            sb.append(configName);
            sb.append(SEP);
            if (locale != null) {
                sb.append(((LanguageQualifier) locale[0]).getValue());
                sb.append(SEP_LOCALE);
                sb.append(((RegionQualifier) locale[1]).getValue());
            }
            sb.append(SEP);
            sb.append(theme);
            sb.append(SEP);
            return sb.toString();
        }
    }
    public static abstract class CustomToggle {
        private final String mUiLabel;
        private final Image mImage;
        private final String mUiTooltip;
        public CustomToggle(
                String uiLabel,
                Image image,
                String uiTooltip) {
            mUiLabel = uiLabel;
            mImage = image;
            mUiTooltip = uiTooltip;
        }
        public abstract void onSelected(boolean newState);
        private void createToggle(Composite parent) {
            final Button b = new Button(parent, SWT.TOGGLE | SWT.FLAT);
            if (mUiTooltip != null) {
                b.setToolTipText(mUiTooltip);
            }
            if (mImage != null) {
                b.setImage(mImage);
            }
            if (mUiLabel != null) {
                b.setText(mUiLabel);
            }
            b.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    onSelected(b.getSelection());
                }
            });
        }
    }
    public ConfigurationComposite(IConfigListener listener,
            CustomToggle[] customToggles,
            Composite parent, int style) {
        super(parent, style);
        mListener = listener;
        if (customToggles == null) {
            customToggles = new CustomToggle[0];
        }
        GridLayout gl;
        GridData gd;
        int cols = 10;  
        Composite labelParent = new Composite(this, SWT.NONE);
        labelParent.setLayout(gl = new GridLayout(3 + customToggles.length, false));
        gl.marginWidth = gl.marginHeight = 0;
        labelParent.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = cols;
        new Label(labelParent, SWT.NONE).setText("Editing config:");
        mCurrentLayoutLabel = new Label(labelParent, SWT.NONE);
        mCurrentLayoutLabel.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.widthHint = 50;
        for (CustomToggle toggle : customToggles) {
            toggle.createToggle(labelParent);
        }
        mClippingButton = new Button(labelParent, SWT.TOGGLE | SWT.FLAT);
        mClippingButton.setSelection(mClipping);
        mClippingButton.setToolTipText("Toggles screen clipping on/off");
        mClippingButton.setImage(IconFactory.getInstance().getIcon("clipping")); 
        mClippingButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onClippingChange();
            }
        });
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        setLayout(gl = new GridLayout(cols, false));
        gl.marginHeight = 0;
        gl.horizontalSpacing = 0;
        new Label(this, SWT.NONE).setText("Devices");
        mDeviceCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        mDeviceCombo.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        mDeviceCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onDeviceChange(true );
            }
        });
        new Label(this, SWT.NONE).setText("Config");
        mDeviceConfigCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        mDeviceConfigCombo.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        mDeviceConfigCombo.addSelectionListener(new SelectionAdapter() {
            @Override
             public void widgetSelected(SelectionEvent e) {
                onDeviceConfigChange();
            }
        });
        new Label(this, SWT.NONE).setText("Locale");
        mLocaleCombo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        mLocaleCombo.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        mLocaleCombo.addVerifyListener(new LanguageRegionVerifier());
        mLocaleCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                onLocaleChange();
            }
            public void widgetSelected(SelectionEvent e) {
                onLocaleChange();
            }
        });
        Label separator = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setLayoutData(gd = new GridData(
                GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
        gd.heightHint = 0;
        mThemeCombo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        mThemeCombo.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        mThemeCombo.setEnabled(false);
        mThemeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onThemeChange();
            }
        });
        separator = new Label(this, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setLayoutData(gd = new GridData(
                GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL));
        gd.heightHint = 0;
        mCreateButton = new Button(this, SWT.PUSH | SWT.FLAT);
        mCreateButton.setText("Create...");
        mCreateButton.setEnabled(false);
        mCreateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mListener != null) {
                    mListener.onCreate();
                }
            }
        });
    }
    public void setFile(IFile file) {
        mEditedFile = file;
    }
    public void replaceFile(IFile file) {
        if (mState.device == null) {
            setFile(file); 
            return;
        }
        mEditedFile = file;
        IProject iProject = mEditedFile.getProject();
        mResources = ResourceManager.getInstance().getProjectResources(iProject);
        ResourceFolder resFolder = ResourceManager.getInstance().getResourceFolder(file);
        mEditedConfig = resFolder.getConfiguration();
        mDisableUpdates++; 
        LoadStatus sdkStatus = AdtPlugin.getDefault().getSdkLoadStatus();
        if (sdkStatus == LoadStatus.LOADED) {
            LoadStatus targetStatus = Sdk.getCurrent().checkAndLoadTargetData(mTarget, null);
            if (targetStatus == LoadStatus.LOADED) {
                adaptConfigSelection(true );
                computeCurrentConfig(true );
                updateConfigDisplay(mEditedConfig);
            }
        }
        mDisableUpdates--;
    }
    public void changeFileOnNewConfig(IFile file) {
        mEditedFile = file;
        IProject iProject = mEditedFile.getProject();
        mResources = ResourceManager.getInstance().getProjectResources(iProject);
        ResourceFolder resFolder = ResourceManager.getInstance().getResourceFolder(file);
        mEditedConfig = resFolder.getConfiguration();
        updateConfigDisplay(mEditedConfig);
    }
    public void onSdkLoaded(IAndroidTarget target) {
        mSdkChanged = true;
        mTarget = target;
        mDisableUpdates++; 
        initDevices();
        mDisableUpdates--;
    }
    public void onXmlModelLoaded() {
        LoadStatus sdkStatus = AdtPlugin.getDefault().getSdkLoadStatus();
        if (sdkStatus == LoadStatus.LOADED) {
            mDisableUpdates++; 
            if (mSdkChanged || mFirstXmlModelChange) {
                initDevices();
            }
            IProject iProject = mEditedFile.getProject();
            Sdk currentSdk = Sdk.getCurrent();
            if (currentSdk != null) {
                mTarget = currentSdk.getTarget(iProject);
            }
            LoadStatus targetStatus = LoadStatus.FAILED;
            if (mTarget != null) {
                targetStatus = Sdk.getCurrent().checkAndLoadTargetData(mTarget, null);
            }
            if (targetStatus == LoadStatus.LOADED) {
                if (mResources == null) {
                    mResources = ResourceManager.getInstance().getProjectResources(iProject);
                }
                if (mEditedConfig == null) {
                    ResourceFolder resFolder = mResources.getResourceFolder(
                            (IFolder) mEditedFile.getParent());
                    mEditedConfig = resFolder.getConfiguration();
                }
                AndroidTargetData targetData = Sdk.getCurrent().getTargetData(mTarget);
                if (targetData != null) {
                    LayoutBridge bridge = targetData.getLayoutBridge();
                    setClippingSupport(bridge.apiLevel >= 4);
                }
                boolean loadedConfigData = false;
                try {
                    QualifiedName qname = new QualifiedName(AdtPlugin.PLUGIN_ID, CONFIG_STATE);
                    String data = mEditedFile.getPersistentProperty(qname);
                    if (data != null) {
                        loadedConfigData = mState.setData(data);
                    }
                } catch (CoreException e) {
                }
                updateThemes();
                updateLocales();
                if (loadedConfigData) {
                    selectDevice(mState.device);
                    fillConfigCombo(mState.configName);
                    adaptConfigSelection(false );
                } else {
                    findAndSetCompatibleConfig(false );
                }
                updateConfigDisplay(mEditedConfig);
                computeCurrentConfig(true );
            }
            mDisableUpdates--;
            mFirstXmlModelChange  = false;
        }
    }
    private void findAndSetCompatibleConfig(boolean favorCurrentConfig) {
        LayoutDevice anyDeviceMatch = null; 
        String anyConfigMatchName = null;
        int anyLocaleIndex = -1;
        LayoutDevice bestDeviceMatch = null; 
        String bestConfigMatchName = null;
        int bestLocaleIndex = -1;
        FolderConfiguration testConfig = new FolderConfiguration();
        mainloop: for (LayoutDevice device : mDeviceList) {
            for (Entry<String, FolderConfiguration> entry :
                    device.getConfigs().entrySet()) {
                testConfig.set(entry.getValue());
                for (int i = 0 ; i < mLocaleList.size() ; i++) {
                    ResourceQualifier[] locale = mLocaleList.get(i);
                    testConfig.setLanguageQualifier((LanguageQualifier)locale[LOCALE_LANG]);
                    testConfig.setRegionQualifier((RegionQualifier)locale[LOCALE_REGION]);
                    if (mEditedConfig.isMatchFor(testConfig)) {
                        if (anyDeviceMatch == null) {
                            anyDeviceMatch = device;
                            anyConfigMatchName = entry.getKey();
                            anyLocaleIndex = i;
                        }
                        if (isCurrentFileBestMatchFor(testConfig)) {
                            bestDeviceMatch = device;
                            bestConfigMatchName = entry.getKey();
                            bestLocaleIndex = i;
                            break mainloop;
                        }
                    }
                }
            }
        }
        if (bestDeviceMatch == null) {
            if (favorCurrentConfig) {
                if (mEditedConfig.isMatchFor(mCurrentConfig) == false) {
                    AdtPlugin.log(IStatus.ERROR,
                            "favorCurrentConfig can only be true if the current config is compatible");
                }
                AdtPlugin.printErrorToConsole(mEditedFile.getProject(),
                        String.format(
                                "'%1$s' is not a best match for any device/locale combination.",
                                mEditedConfig.toDisplayString()),
                        String.format(
                                "Displaying it with '%1$s'",
                                mCurrentConfig.toDisplayString()));
            } else if (anyDeviceMatch != null) {
                selectDevice(mState.device = anyDeviceMatch);
                fillConfigCombo(anyConfigMatchName);
                mLocaleCombo.select(anyLocaleIndex);
                computeCurrentConfig(false );
                AdtPlugin.printErrorToConsole(mEditedFile.getProject(),
                        String.format(
                                "'%1$s' is not a best match for any device/locale combination.",
                                mEditedConfig.toDisplayString()),
                        String.format(
                                "Displaying it with '%1$s'",
                                mCurrentConfig.toDisplayString()));
            } else {
            }
        } else {
            selectDevice(mState.device = bestDeviceMatch);
            fillConfigCombo(bestConfigMatchName);
            mLocaleCombo.select(bestLocaleIndex);
        }
    }
    private void adaptConfigSelection(boolean needBestMatch) {
        boolean needConfigChange = true; 
        boolean currentConfigIsCompatible = false;
        int configIndex = mDeviceConfigCombo.getSelectionIndex();
        if (configIndex != -1) {
            String configName = mDeviceConfigCombo.getItem(configIndex);
            FolderConfiguration currentConfig = mState.device.getConfigs().get(configName);
            if (mEditedConfig.isMatchFor(currentConfig)) {
                currentConfigIsCompatible = true; 
                if (needBestMatch == false || isCurrentFileBestMatchFor(currentConfig)) {
                    needConfigChange = false;
                }
            }
        }
        if (needConfigChange) {
            FolderConfiguration testConfig = new FolderConfiguration();
            String matchName = null;
            int localeIndex = -1;
            Map<String, FolderConfiguration> configs = mState.device.getConfigs();
            mainloop: for (Entry<String, FolderConfiguration> entry : configs.entrySet()) {
                testConfig.set(entry.getValue());
                for (int i = 0 ; i < mLocaleList.size() ; i++) {
                    ResourceQualifier[] locale = mLocaleList.get(i);
                    testConfig.setLanguageQualifier((LanguageQualifier)locale[LOCALE_LANG]);
                    testConfig.setRegionQualifier((RegionQualifier)locale[LOCALE_REGION]);
                    if (mEditedConfig.isMatchFor(testConfig) &&
                            isCurrentFileBestMatchFor(testConfig)) {
                        matchName = entry.getKey();
                        localeIndex = i;
                        break mainloop;
                    }
                }
            }
            if (matchName != null) {
                selectConfig(matchName);
                mLocaleCombo.select(localeIndex);
            } else {
                findAndSetCompatibleConfig(currentConfigIsCompatible);
            }
        }
    }
    private void setLocaleCombo(ResourceQualifier language, ResourceQualifier region) {
        final int count = mLocaleList.size();
        for (int i = 0 ; i < count ; i++) {
            ResourceQualifier[] locale = mLocaleList.get(i);
            if (locale[LOCALE_LANG].equals(language)) {
                if (region == null) {
                    if (FAKE_LOCALE_VALUE.equals(
                            ((RegionQualifier)locale[LOCALE_REGION]).getValue())) {
                        mLocaleCombo.select(i);
                        break;
                    }
                } else if (region.equals(locale[LOCALE_REGION])) {
                    mLocaleCombo.select(i);
                    break;
                }
            }
        }
    }
    private void updateConfigDisplay(FolderConfiguration fileConfig) {
        String current = fileConfig.toDisplayString();
        mCurrentLayoutLabel.setText(current != null ? current : "(Default)");
    }
    private void saveState(boolean force) {
        if (mDisableUpdates == 0) {
            int index = mDeviceConfigCombo.getSelectionIndex();
            if (index != -1) {
                mState.configName = mDeviceConfigCombo.getItem(index);
            } else {
                mState.configName = null;
            }
            index = mLocaleCombo.getSelectionIndex();
            if (index != -1) {
                mState.locale = mLocaleList.get(index);
            } else {
                mState.locale = null;
            }
            index = mThemeCombo.getSelectionIndex();
            if (index != -1) {
                mState.theme = mThemeCombo.getItem(index);
            }
        }
    }
    public void storeState() {
        try {
            QualifiedName qname = new QualifiedName(AdtPlugin.PLUGIN_ID, CONFIG_STATE); 
            mEditedFile.setPersistentProperty(qname, mState.getData());
        } catch (CoreException e) {
        }
    }
    public void updateLocales() {
        if (mListener == null) {
            return; 
        }
        mDisableUpdates++;
        mLocaleCombo.removeAll();
        mLocaleList.clear();
        SortedSet<String> languages = null;
        boolean hasLocale = false;
        ProjectResources project = mListener.getProjectResources();
        if (project != null) {
            languages = project.getLanguages();
            for (String language : languages) {
                hasLocale = true;
                LanguageQualifier langQual = new LanguageQualifier(language);
                SortedSet<String> regions = project.getRegions(language);
                for (String region : regions) {
                    mLocaleCombo.add(String.format("%1$s / %2$s", language, region)); 
                    RegionQualifier regionQual = new RegionQualifier(region);
                    mLocaleList.add(new ResourceQualifier[] { langQual, regionQual });
                }
                if (regions.size() > 0) {
                    mLocaleCombo.add(String.format("%1$s / Other", language)); 
                } else {
                    mLocaleCombo.add(String.format("%1$s / Any", language)); 
                }
                mLocaleList.add(new ResourceQualifier[] {
                        langQual,
                        new RegionQualifier(FAKE_LOCALE_VALUE)
                });
            }
        }
        if (hasLocale) {
            mLocaleCombo.add("Other");
        } else {
            mLocaleCombo.add("Any");
        }
        mLocaleList.add(new ResourceQualifier[] {
                new LanguageQualifier(FAKE_LOCALE_VALUE),
                new RegionQualifier(FAKE_LOCALE_VALUE)
        });
        if (mState.locale != null) {
            setLocaleCombo(mState.locale[LOCALE_LANG],
                    mState.locale[LOCALE_REGION]);
        } else {
            mLocaleCombo.select(0);
        }
        mThemeCombo.getParent().layout();
        mDisableUpdates--;
    }
    private void updateThemes() {
        if (mListener == null) {
            return; 
        }
        ProjectResources frameworkProject = mListener.getFrameworkResources();
        mDisableUpdates++;
        mThemeCombo.removeAll();
        mPlatformThemeCount = 0;
        ArrayList<String> themes = new ArrayList<String>();
        if (frameworkProject != null) {
            Map<String, Map<String, IResourceValue>> frameworResources =
                mListener.getConfiguredFrameworkResources();
            if (frameworResources != null) {
                Map<String, IResourceValue> styles = frameworResources.get(
                        ResourceType.STYLE.getName());
                for (IResourceValue value : styles.values()) {
                    String name = value.getName();
                    if (name.startsWith("Theme.") || name.equals("Theme")) {
                        themes.add(value.getName());
                        mPlatformThemeCount++;
                    }
                }
                Collections.sort(themes);
                for (String theme : themes) {
                    mThemeCombo.add(theme);
                }
                mPlatformThemeCount = themes.size();
                themes.clear();
            }
        }
        ProjectResources project = mListener.getProjectResources();
        if (project != null) {
            Map<String, Map<String, IResourceValue>> configuredProjectRes =
                mListener.getConfiguredProjectResources();
            if (configuredProjectRes != null) {
                Map<String, IResourceValue> styleMap = configuredProjectRes.get(
                        ResourceType.STYLE.getName());
                if (styleMap != null) {
                    for (IResourceValue value : styleMap.values()) {
                        if (isTheme(value, styleMap)) {
                            themes.add(value.getName());
                        }
                    }
                    if (mPlatformThemeCount > 0 && themes.size() > 0) {
                        mThemeCombo.add(THEME_SEPARATOR);
                    }
                    Collections.sort(themes);
                    for (String theme : themes) {
                        mThemeCombo.add(theme);
                    }
                }
            }
        }
        if (mState.theme != null) {
            final int count = mThemeCombo.getItemCount();
            for (int i = 0 ; i < count ; i++) {
                if (mState.theme.equals(mThemeCombo.getItem(i))) {
                    mThemeCombo.select(i);
                    break;
                }
            }
            mThemeCombo.setEnabled(true);
        } else if (mThemeCombo.getItemCount() > 0) {
            mThemeCombo.select(0);
            mThemeCombo.setEnabled(true);
        } else {
            mThemeCombo.setEnabled(false);
        }
        mThemeCombo.getParent().layout();
        mDisableUpdates--;
    }
    public FolderConfiguration getEditedConfig() {
        return mEditedConfig;
    }
    public FolderConfiguration getCurrentConfig() {
        return mCurrentConfig;
    }
    public void getCurrentConfig(FolderConfiguration config) {
        config.set(mCurrentConfig);
    }
    public Density getDensity() {
        if (mCurrentConfig != null) {
            PixelDensityQualifier qual = mCurrentConfig.getPixelDensityQualifier();
            if (qual != null) {
                Density d = qual.getValue();
                if (d != Density.NODPI) {
                    return d;
                }
            }
        }
        return Density.MEDIUM;
    }
    public float getXDpi() {
        if (mState.device != null) {
            float dpi = mState.device.getXDpi();
            if (Float.isNaN(dpi) == false) {
                return dpi;
            }
        }
        return getDensity().getDpiValue();
    }
    public float getYDpi() {
        if (mState.device != null) {
            float dpi = mState.device.getYDpi();
            if (Float.isNaN(dpi) == false) {
                return dpi;
            }
        }
        return getDensity().getDpiValue();
    }
    public Rectangle getScreenBounds() {
        ScreenOrientationQualifier qual = mCurrentConfig.getScreenOrientationQualifier();
        ScreenOrientation orientation = ScreenOrientation.PORTRAIT;
        if (qual != null) {
            orientation = qual.getValue();
        }
        ScreenDimensionQualifier qual2 = mCurrentConfig.getScreenDimensionQualifier();
        int s1, s2;
        if (qual2 != null) {
            s1 = qual2.getValue1();
            s2 = qual2.getValue2();
        } else {
            s1 = 480;
            s2 = 320;
        }
        switch (orientation) {
            default:
            case PORTRAIT:
                return new Rectangle(0, 0, s2, s1);
            case LANDSCAPE:
                return new Rectangle(0, 0, s1, s2);
            case SQUARE:
                return new Rectangle(0, 0, s1, s1);
        }
    }
    public String getTheme() {
        int themeIndex = mThemeCombo.getSelectionIndex();
        if (themeIndex != -1) {
            return mThemeCombo.getItem(themeIndex);
        }
        return null;
    }
    public boolean isProjectTheme() {
        return mThemeCombo.getSelectionIndex() >= mPlatformThemeCount;
    }
    public boolean getClipping() {
        return mClipping;
    }
    private void setClippingSupport(boolean b) {
        mClippingButton.setEnabled(b);
        if (b) {
            mClippingButton.setToolTipText("Toggles screen clipping on/off");
        } else {
            mClipping = true;
            mClippingButton.setSelection(true);
            mClippingButton.setToolTipText("Non clipped rendering is not supported");
        }
    }
    private void initDevices() {
        mDeviceList = null;
        Sdk sdk = Sdk.getCurrent();
        if (sdk != null) {
            LayoutDeviceManager manager = sdk.getLayoutDeviceManager();
            mDeviceList = manager.getCombinedList();
        }
        mDeviceCombo.removeAll();
        mDeviceConfigCombo.removeAll();
        if (mDeviceList != null) {
            for (LayoutDevice device : mDeviceList) {
                mDeviceCombo.add(device.getName());
            }
            mDeviceCombo.select(0);
            if (mDeviceList.size() > 0) {
                Map<String, FolderConfiguration> configs = mDeviceList.get(0).getConfigs();
                Set<String> configNames = configs.keySet();
                for (String name : configNames) {
                    mDeviceConfigCombo.add(name);
                }
                mDeviceConfigCombo.select(0);
                if (configNames.size() == 1) {
                    mDeviceConfigCombo.setEnabled(false);
                }
            }
        }
        mDeviceCombo.add("Custom...");
    }
    private boolean selectDevice(LayoutDevice device) {
        final int count = mDeviceList.size();
        for (int i = 0 ; i < count ; i++) {
            if (device == mDeviceList.get(i)) {
                mDeviceCombo.select(i);
                return true;
            }
        }
        return false;
    }
    private void selectConfig(String name) {
        final int count = mDeviceConfigCombo.getItemCount();
        for (int i = 0 ; i < count ; i++) {
            String item = mDeviceConfigCombo.getItem(i);
            if (name.equals(item)) {
                mDeviceConfigCombo.select(i);
                return;
            }
        }
    }
    private void onDeviceChange(boolean recomputeLayout) {
        if (mDisableUpdates > 0) {
            return;
        }
        String newConfigName = null;
        int deviceIndex = mDeviceCombo.getSelectionIndex();
        if (deviceIndex != -1) {
            if (deviceIndex == mDeviceCombo.getItemCount() - 1) {
                onCustomDeviceConfig();
                return;
            }
            if (mState.device != null) {
                int index = mDeviceConfigCombo.getSelectionIndex();
                if (index != -1) {
                    FolderConfiguration oldConfig = mState.device.getConfigs().get(
                            mDeviceConfigCombo.getItem(index));
                    LayoutDevice newDevice = mDeviceList.get(deviceIndex);
                    newConfigName = getClosestMatch(oldConfig, newDevice.getConfigs());
                }
            }
            mState.device = mDeviceList.get(deviceIndex);
        } else {
            mState.device = null;
        }
        fillConfigCombo(newConfigName);
        computeCurrentConfig(false );
        if (recomputeLayout) {
            onDeviceConfigChange();
        }
    }
    private void onCustomDeviceConfig() {
        ConfigManagerDialog dialog = new ConfigManagerDialog(getShell());
        dialog.open();
        Sdk.getCurrent().getLayoutDeviceManager().save();
        mDisableUpdates++;
        LayoutDevice oldCurrent = mState.device;
        initDevices();
        if (selectDevice(oldCurrent)) {
            selectConfig(mState.configName);
            adaptConfigSelection(false );
        } else {
            findAndSetCompatibleConfig(false );
        }
        mDisableUpdates--;
        computeCurrentConfig(false );
        onDeviceChange(true );
    }
    private String getClosestMatch(FolderConfiguration oldConfig,
            Map<String, FolderConfiguration> configs) {
        ArrayList<Entry<String, FolderConfiguration>> list1 =
            new ArrayList<Entry<String,FolderConfiguration>>();
        ArrayList<Entry<String, FolderConfiguration>> list2 =
            new ArrayList<Entry<String,FolderConfiguration>>();
        list1.addAll(configs.entrySet());
        final int count = FolderConfiguration.getQualifierCount();
        for (int i = 0 ; i < count ; i++) {
            for (Entry<String, FolderConfiguration> entry : list1) {
                ResourceQualifier oldQualifier = oldConfig.getQualifier(i);
                FolderConfiguration config = entry.getValue();
                ResourceQualifier newQualifier = config.getQualifier(i);
                if (oldQualifier == null) {
                    if (newQualifier == null) {
                        list2.add(entry);
                    }
                } else if (oldQualifier.equals(newQualifier)) {
                    list2.add(entry);
                }
            }
            if (list2.size() == 1) {
                return list2.get(0).getKey();
            }
            if (list2.size() != 0) {
                list1.clear();
                list1.addAll(list2);
                list2.clear();
            }
        }
        if (list1.size() > 0) {
            return list1.get(0).getKey();
        }
        return null;
    }
    private void fillConfigCombo(String refName) {
        mDeviceConfigCombo.removeAll();
        if (mState.device != null) {
            Set<String> configNames = mState.device.getConfigs().keySet();
            int selectionIndex = 0;
            int i = 0;
            for (String name : configNames) {
                mDeviceConfigCombo.add(name);
                if (name.equals(refName)) {
                    selectionIndex = i;
                }
                i++;
            }
            mDeviceConfigCombo.select(selectionIndex);
            mDeviceConfigCombo.setEnabled(configNames.size() > 1);
        }
    }
    private void onDeviceConfigChange() {
        if (mDisableUpdates > 0) {
            return;
        }
        if (computeCurrentConfig(false ) && mListener != null) {
            mListener.onConfigurationChange();
        }
    }
    private void onLocaleChange() {
        if (mDisableUpdates > 0) {
            return;
        }
        if (computeCurrentConfig(false ) &&  mListener != null) {
            mListener.onConfigurationChange();
        }
    }
    private boolean computeCurrentConfig(boolean force) {
        saveState(force);
        if (mState.device != null) {
            int configIndex = mDeviceConfigCombo.getSelectionIndex();
            String name = mDeviceConfigCombo.getItem(configIndex);
            FolderConfiguration config = mState.device.getConfigs().get(name);
            mCurrentConfig.set(config);
            int localeIndex = mLocaleCombo.getSelectionIndex();
            if (localeIndex != -1) {
                ResourceQualifier[] localeQualifiers = mLocaleList.get(localeIndex);
                mCurrentConfig.setLanguageQualifier(
                        (LanguageQualifier)localeQualifiers[LOCALE_LANG]);
                mCurrentConfig.setRegionQualifier(
                        (RegionQualifier)localeQualifiers[LOCALE_REGION]);
            }
            checkCreateEnable();
            return true;
        }
        return false;
    }
    private void onThemeChange() {
        saveState(false );
        int themeIndex = mThemeCombo.getSelectionIndex();
        if (themeIndex != -1) {
            String theme = mThemeCombo.getItem(themeIndex);
            if (theme.equals(THEME_SEPARATOR)) {
                mThemeCombo.select(0);
            }
            if (mListener != null) {
                mListener.onThemeChange();
            }
        }
    }
    private void onClippingChange() {
        mClipping = mClippingButton.getSelection();
        if (mListener != null) {
            mListener.onClippingChange();
        }
    }
    private boolean isTheme(IResourceValue value, Map<String, IResourceValue> styleMap) {
        if (value instanceof IStyleResourceValue) {
            IStyleResourceValue style = (IStyleResourceValue)value;
            boolean frameworkStyle = false;
            String parentStyle = style.getParentStyle();
            if (parentStyle == null) {
                String name = style.getName();
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    parentStyle = name.substring(0, index);
                }
            } else {
                if (parentStyle.startsWith("@")) {
                    parentStyle = parentStyle.substring(1);
                }
                if (parentStyle.startsWith("android:")) {
                    frameworkStyle = true;
                    parentStyle = parentStyle.substring("android:".length());
                }
                if (parentStyle.startsWith("style/")) {
                    parentStyle = parentStyle.substring("style/".length());
                }
            }
            if (parentStyle != null) {
                if (frameworkStyle) {
                    return parentStyle.equals("Theme") || parentStyle.startsWith("Theme.");
                } else {
                    value = styleMap.get(parentStyle);
                    if (value != null) {
                        return isTheme(value, styleMap);
                    }
                }
            }
        }
        return false;
    }
    private void checkCreateEnable() {
        mCreateButton.setEnabled(mEditedConfig.equals(mCurrentConfig) == false);
    }
    private boolean isCurrentFileBestMatchFor(FolderConfiguration config) {
        ResourceFile match = mResources.getMatchingFile(mEditedFile.getName(),
                ResourceFolderType.LAYOUT, config);
        if (match != null) {
            return match.getFile().equals(mEditedFile);
        } else {
            AdtPlugin.log(IStatus.ERROR, "Current file is not a match for the given config.");
        }
        return false;
    }
}
