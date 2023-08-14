public class UpdaterWindowImpl {
    private final Shell mParentShell;
    private final UpdaterData mUpdaterData;
    private ArrayList<Composite> mPages = new ArrayList<Composite>();
    private boolean mInternalPageChange;
    private ArrayList<Object[]> mExtraPages;
    private ProgressTaskFactory mTaskFactory;
    private Class<? extends Composite> mInitialPage;
    private boolean mRequestAutoUpdate;
    protected Shell mAndroidSdkUpdater;
    private SashForm mSashForm;
    private List mPageList;
    private Composite mPagesRootComposite;
    private LocalPackagesPage mLocalPackagePage;
    private RemotePackagesPage mRemotePackagesPage;
    private AvdManagerPage mAvdManagerPage;
    private StackLayout mStackLayout;
    public UpdaterWindowImpl(Shell parentShell, ISdkLog sdkLog, String osSdkRoot,
            boolean userCanChangeSdkRoot) {
        mParentShell = parentShell;
        mUpdaterData = new UpdaterData(osSdkRoot, sdkLog);
        mUpdaterData.setUserCanChangeSdkRoot(userCanChangeSdkRoot);
    }
    public void open() {
        if (mParentShell == null) {
            Display.setAppName("Android"); 
        }
        createContents();
        mAndroidSdkUpdater.open();
        mAndroidSdkUpdater.layout();
        if (postCreate()) {    
            Display display = Display.getDefault();
            while (!mAndroidSdkUpdater.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }
        dispose();  
    }
    protected void createContents() {
        mAndroidSdkUpdater = new Shell(mParentShell, SWT.SHELL_TRIM);
        mAndroidSdkUpdater.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                onAndroidSdkUpdaterDispose();    
            }
        });
        FillLayout fl;
        mAndroidSdkUpdater.setLayout(fl = new FillLayout(SWT.HORIZONTAL));
        fl.marginHeight = fl.marginWidth = 5;
        mAndroidSdkUpdater.setMinimumSize(new Point(200, 50));
        mAndroidSdkUpdater.setSize(745, 433);
        mAndroidSdkUpdater.setText("Android SDK and AVD Manager");
        mSashForm = new SashForm(mAndroidSdkUpdater, SWT.NONE);
        mPageList = new List(mSashForm, SWT.BORDER);
        mPageList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onPageListSelected();    
            }
        });
        mPagesRootComposite = new Composite(mSashForm, SWT.NONE);
        mStackLayout = new StackLayout();
        mPagesRootComposite.setLayout(mStackLayout);
        mAvdManagerPage = new AvdManagerPage(mPagesRootComposite, mUpdaterData);
        mLocalPackagePage = new LocalPackagesPage(mPagesRootComposite, mUpdaterData);
        mRemotePackagesPage = new RemotePackagesPage(mPagesRootComposite, mUpdaterData);
        mSashForm.setWeights(new int[] {150, 576});
    }
    public void registerExtraPage(String title, Class<? extends Composite> pageClass) {
        if (mExtraPages == null) {
            mExtraPages = new ArrayList<Object[]>();
        }
        mExtraPages.add(new Object[]{ title, pageClass });
    }
    public void setInitialPage(Class<? extends Composite> pageClass) {
        mInitialPage = pageClass;
    }
    public void setRequestAutoUpdate(boolean requestAutoUpdate) {
        mRequestAutoUpdate = requestAutoUpdate;
    }
    public void addListeners(ISdkListener listener) {
        mUpdaterData.addListeners(listener);
    }
    public void removeListener(ISdkListener listener) {
        mUpdaterData.removeListener(listener);
    }
    private Shell getShell() {
        return mAndroidSdkUpdater;
    }
    private void onAndroidSdkUpdaterDispose() {
        if (mUpdaterData != null) {
            ImageFactory imgFactory = mUpdaterData.getImageFactory();
            if (imgFactory != null) {
                imgFactory.dispose();
            }
        }
    }
    private void setWindowImage(Shell androidSdkUpdater) {
        String imageName = "android_icon_16.png"; 
        if (SdkConstants.currentPlatform() == SdkConstants.PLATFORM_DARWIN) {
            imageName = "android_icon_128.png"; 
        }
        if (mUpdaterData != null) {
            ImageFactory imgFactory = mUpdaterData.getImageFactory();
            if (imgFactory != null) {
                mAndroidSdkUpdater.setImage(imgFactory.getImageByName(imageName));
            }
        }
    }
    private boolean postCreate() {
        mUpdaterData.setWindowShell(getShell());
        mTaskFactory = new ProgressTaskFactory(getShell());
        mUpdaterData.setTaskFactory(mTaskFactory);
        mUpdaterData.setImageFactory(new ImageFactory(getShell().getDisplay()));
        setWindowImage(mAndroidSdkUpdater);
        addPage(mAvdManagerPage,     "Virtual Devices");
        addPage(mLocalPackagePage,   "Installed Packages");
        addPage(mRemotePackagesPage, "Available Packages");
        addExtraPages();
        int pageIndex = 0;
        int i = 0;
        for (Composite p : mPages) {
            if (p.getClass().equals(mInitialPage)) {
                pageIndex = i;
                break;
            }
            i++;
        }
        displayPage(pageIndex);
        mPageList.setSelection(pageIndex);
        setupSources();
        initializeSettings();
        if (mUpdaterData.checkIfInitFailed()) {
            return false;
        }
        mUpdaterData.notifyListeners(true );
        if (mRequestAutoUpdate) {
            mUpdaterData.updateOrInstallAll(null );
        }
        return true;
    }
    private void dispose() {
        mUpdaterData.getSources().saveUserSources(mUpdaterData.getSdkLog());
    }
    private void addPage(Composite page, String title) {
        page.setData(title);
        mPages.add(page);
        mPageList.add(title);
    }
    @SuppressWarnings("unchecked")
    private void addExtraPages() {
        if (mExtraPages == null) {
            return;
        }
        for (Object[] extraPage : mExtraPages) {
            String title = (String) extraPage[0];
            Class<? extends Composite> clazz = (Class<? extends Composite>) extraPage[1];
            Constructor<? extends Composite> cons;
            try {
                cons = clazz.getConstructor(new Class<?>[] { Composite.class });
                Composite instance = cons.newInstance(new Object[] { mPagesRootComposite });
                addPage(instance, title);
            } catch (NoSuchMethodException e) {
                mUpdaterData.getSdkLog().error(e,
                        "Failed to add extra page %1$s. Constructor args must be (Composite parent).",  
                        clazz.getSimpleName());
            } catch (Exception e) {
                mUpdaterData.getSdkLog().error(e,
                        "Failed to add extra page %1$s.",  
                        clazz.getSimpleName());
            }
        }
    }
    private void onPageListSelected() {
        if (mInternalPageChange == false) {
            int index = mPageList.getSelectionIndex();
            if (index >= 0) {
                displayPage(index);
            }
        }
    }
    private void displayPage(int index) {
        Composite page = mPages.get(index);
        if (page != null) {
            mStackLayout.topControl = page;
            mPagesRootComposite.layout(true);
            if (!mInternalPageChange) {
                mInternalPageChange = true;
                mPageList.setSelection(index);
                mInternalPageChange = false;
            }
        }
    }
    private void setupSources() {
        RepoSources sources = mUpdaterData.getSources();
        sources.add(new RepoSource(SdkRepository.URL_GOOGLE_SDK_REPO_SITE, false ));
        String str = System.getenv("SDK_UPDATER_URLS");
        if (str != null) {
            String[] urls = str.split(";");
            for (String url : urls) {
                if (url != null && url.length() > 0) {
                    RepoSource s = new RepoSource(url, false );
                    if (!sources.hasSource(s)) {
                        sources.add(s);
                    }
                }
            }
        }
        sources.loadUserSources(mUpdaterData.getSdkLog());
        str = System.getenv("SDK_UPDATER_USER_URLS");
        if (str != null) {
            String[] urls = str.split(";");
            for (String url : urls) {
                if (url != null && url.length() > 0) {
                    RepoSource s = new RepoSource(url, true );
                    if (!sources.hasSource(s)) {
                        sources.add(s);
                    }
                }
            }
        }
        mRemotePackagesPage.onSdkChange(false );
    }
    private void initializeSettings() {
        SettingsController c = mUpdaterData.getSettingsController();
        c.loadSettings();
        c.applySettings();
        for (Object page : mPages) {
            if (page instanceof ISettingsPage) {
                ISettingsPage settingsPage = (ISettingsPage) page;
                c.setSettingsPage(settingsPage);
                break;
            }
        }
    }
}
