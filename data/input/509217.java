class TabControl {
    private static final String LOGTAG = "TabControl";
    private static final int MAX_TABS = 8;
    private ArrayList<Tab> mTabs = new ArrayList<Tab>(MAX_TABS);
    private ArrayList<Tab> mTabQueue = new ArrayList<Tab>(MAX_TABS);
    private int mCurrentTab = -1;
    private final BrowserActivity mActivity;
    private final File mThumbnailDir;
    TabControl(BrowserActivity activity) {
        mActivity = activity;
        mThumbnailDir = activity.getDir("thumbnails", 0);
    }
    File getThumbnailDir() {
        return mThumbnailDir;
    }
    BrowserActivity getBrowserActivity() {
        return mActivity;
    }
    WebView getCurrentWebView() {
        Tab t = getTab(mCurrentTab);
        if (t == null) {
            return null;
        }
        return t.getWebView();
    }
    WebView getCurrentTopWebView() {
        Tab t = getTab(mCurrentTab);
        if (t == null) {
            return null;
        }
        return t.getTopWindow();
    }
    WebView getCurrentSubWindow() {
        Tab t = getTab(mCurrentTab);
        if (t == null) {
            return null;
        }
        return t.getSubWebView();
    }
    Tab getTab(int index) {
        if (index >= 0 && index < mTabs.size()) {
            return mTabs.get(index);
        }
        return null;
    }
    Tab getCurrentTab() {
        return getTab(mCurrentTab);
    }
    int getCurrentIndex() {
        return mCurrentTab;
    }
    int getTabIndex(Tab tab) {
        if (tab == null) {
            return -1;
        }
        return mTabs.indexOf(tab);
    }
    boolean canCreateNewTab() {
        return MAX_TABS != mTabs.size();
    }
    Tab createNewTab(boolean closeOnExit, String appId, String url) {
        int size = mTabs.size();
        if (MAX_TABS == size) {
            return null;
        }
        final WebView w = createNewWebView();
        Tab t = new Tab(mActivity, w, closeOnExit, appId, url);
        mTabs.add(t);
        t.putInBackground();
        return t;
    }
    Tab createNewTab() {
        return createNewTab(false, null, null);
    }
    void removeParentChildRelationShips() {
        for (Tab tab : mTabs) {
            tab.removeFromTree();
        }
    }
    boolean removeTab(Tab t) {
        if (t == null) {
            return false;
        }
        Tab current = getCurrentTab();
        mTabs.remove(t);
        if (current == t) {
            t.putInBackground();
            mCurrentTab = -1;
        } else {
            mCurrentTab = getTabIndex(current);
        }
        t.destroy();
        t.removeFromTree();
        for (Tab tab : mTabs) {
            Vector<Tab> children = tab.getChildTabs();
            if (children != null) {
                for (Tab child : children) {
                    child.setParentTab(tab);
                }
            }
        }
        Bundle savedState = t.getSavedState();
        if (savedState != null) {
            if (savedState.containsKey(Tab.CURRPICTURE)) {
                new File(savedState.getString(Tab.CURRPICTURE)).delete();
            }
        }
        mTabQueue.remove(t);
        return true;
    }
    void destroy() {
        for (Tab t : mTabs) {
            t.destroy();
        }
        mTabs.clear();
        mTabQueue.clear();
    }
    int getTabCount() {
        return mTabs.size();
    }
    void saveState(Bundle outState) {
        final int numTabs = getTabCount();
        outState.putInt(Tab.NUMTABS, numTabs);
        final int index = getCurrentIndex();
        outState.putInt(Tab.CURRTAB, (index >= 0 && index < numTabs) ? index : 0);
        for (int i = 0; i < numTabs; i++) {
            final Tab t = getTab(i);
            if (t.saveState()) {
                outState.putBundle(Tab.WEBVIEW + i, t.getSavedState());
            }
        }
    }
    boolean restoreState(Bundle inState) {
        final int numTabs = (inState == null)
                ? -1 : inState.getInt(Tab.NUMTABS, -1);
        if (numTabs == -1) {
            return false;
        } else {
            final int currentTab = inState.getInt(Tab.CURRTAB, -1);
            for (int i = 0; i < numTabs; i++) {
                if (i == currentTab) {
                    Tab t = createNewTab();
                    setCurrentTab(t);
                    if (!t.restoreState(inState.getBundle(Tab.WEBVIEW + i))) {
                        Log.w(LOGTAG, "Fail in restoreState, load home page.");
                        t.getWebView().loadUrl(BrowserSettings.getInstance()
                                .getHomePage());
                    }
                } else {
                    Tab t = new Tab(mActivity, null, false, null, null);
                    Bundle state = inState.getBundle(Tab.WEBVIEW + i);
                    if (state != null) {
                        t.setSavedState(state);
                        t.populatePickerDataFromSavedState();
                        t.setAppId(state.getString(Tab.APPID));
                        t.setOriginalUrl(state.getString(Tab.ORIGINALURL));
                    }
                    mTabs.add(t);
                    mTabQueue.add(0, t);
                }
            }
            for (int i = 0; i < numTabs; i++) {
                final Bundle b = inState.getBundle(Tab.WEBVIEW + i);
                final Tab t = getTab(i);
                if (b != null && t != null) {
                    final int parentIndex = b.getInt(Tab.PARENTTAB, -1);
                    if (parentIndex != -1) {
                        final Tab parent = getTab(parentIndex);
                        if (parent != null) {
                            parent.addChildTab(t);
                        }
                    }
                }
            }
        }
        return true;
    }
    void freeMemory() {
        if (getTabCount() == 0) return;
        Vector<Tab> tabs = getHalfLeastUsedTabs(getCurrentTab());
        if (tabs.size() > 0) {
            Log.w(LOGTAG, "Free " + tabs.size() + " tabs in the browser");
            for (Tab t : tabs) {
                t.saveState();
                t.destroy();
            }
            return;
        }
        Log.w(LOGTAG, "Free WebView's unused memory and cache");
        WebView view = getCurrentWebView();
        if (view != null) {
            view.freeMemory();
        }
    }
    private Vector<Tab> getHalfLeastUsedTabs(Tab current) {
        Vector<Tab> tabsToGo = new Vector<Tab>();
        if (getTabCount() == 1 || current == null) {
            return tabsToGo;
        }
        if (mTabQueue.size() == 0) {
            return tabsToGo;
        }
        int openTabCount = 0;
        for (Tab t : mTabQueue) {
            if (t != null && t.getWebView() != null) {
                openTabCount++;
                if (t != current && t != current.getParentTab()) {
                    tabsToGo.add(t);
                }
            }
        }
        openTabCount /= 2;
        if (tabsToGo.size() > openTabCount) {
            tabsToGo.setSize(openTabCount);
        }
        return tabsToGo;
    }
    Tab getTabFromView(WebView view) {
        final int size = getTabCount();
        for (int i = 0; i < size; i++) {
            final Tab t = getTab(i);
            if (t.getSubWebView() == view || t.getWebView() == view) {
                return t;
            }
        }
        return null;
    }
    Tab getTabFromId(String id) {
        if (id == null) {
            return null;
        }
        final int size = getTabCount();
        for (int i = 0; i < size; i++) {
            final Tab t = getTab(i);
            if (id.equals(t.getAppId())) {
                return t;
            }
        }
        return null;
    }
    void stopAllLoading() {
        final int size = getTabCount();
        for (int i = 0; i < size; i++) {
            final Tab t = getTab(i);
            final WebView webview = t.getWebView();
            if (webview != null) {
                webview.stopLoading();
            }
            final WebView subview = t.getSubWebView();
            if (subview != null) {
                webview.stopLoading();
            }
        }
    }
    private boolean tabMatchesUrl(Tab t, String url) {
        if (t.getAppId() != null) {
            return false;
        }
        WebView webview = t.getWebView();
        if (webview == null) {
            return false;
        } else if (url.equals(webview.getUrl())
                || url.equals(webview.getOriginalUrl())) {
            return true;
        }
        return false;
    }
    Tab findUnusedTabWithUrl(String url) {
        if (url == null) {
            return null;
        }
        Tab t = getCurrentTab();
        if (t != null && tabMatchesUrl(t, url)) {
            return t;
        }
        final int size = getTabCount();
        for (int i = 0; i < size; i++) {
            t = getTab(i);
            if (tabMatchesUrl(t, url)) {
                return t;
            }
        }
        return null;
    }
    boolean recreateWebView(Tab t, BrowserActivity.UrlData urlData) {
        final String url = urlData.mUrl;
        final WebView w = t.getWebView();
        if (w != null) {
            if (url != null && url.equals(t.getOriginalUrl())
                    && urlData.mVoiceIntent == null) {
                final WebBackForwardList list = w.copyBackForwardList();
                if (list != null) {
                    w.goBackOrForward(-list.getCurrentIndex());
                    w.clearHistory(); 
                    return false;
                }
            }
            t.destroy();
        }
        t.setWebView(createNewWebView());
        if (getCurrentTab() == t) {
            setCurrentTab(t, true);
        }
        t.setSavedState(null);
        t.clearPickerData();
        t.setOriginalUrl(url);
        return true;
    }
    private WebView createNewWebView() {
        WebView w = new WebView(mActivity);
        w.setScrollbarFadingEnabled(true);
        w.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        w.setMapTrackballToArrowKeys(false); 
        w.getSettings().setBuiltInZoomControls(true);
        final BrowserSettings s = BrowserSettings.getInstance();
        s.addObserver(w.getSettings()).update(s, null);
        if (false) {
            MeshTracker mt = new MeshTracker(2);
            Paint paint = new Paint();
            Bitmap bm = BitmapFactory.decodeResource(mActivity.getResources(),
                                         R.drawable.pattern_carbon_fiber_dark);
            paint.setShader(new BitmapShader(bm, Shader.TileMode.REPEAT,
                                             Shader.TileMode.REPEAT));
            mt.setBGPaint(paint);
            w.setDragTracker(mt);
        }
        return w;
    }
    boolean setCurrentTab(Tab newTab) {
        return setCurrentTab(newTab, false);
    }
    void pauseCurrentTab() {
        Tab t = getCurrentTab();
        if (t != null) {
            t.pause();
        }
    }
    void resumeCurrentTab() {
        Tab t = getCurrentTab();
        if (t != null) {
            t.resume();
        }
    }
    private boolean setCurrentTab(Tab newTab, boolean force) {
        Tab current = getTab(mCurrentTab);
        if (current == newTab && !force) {
            return true;
        }
        if (current != null) {
            current.putInBackground();
            mCurrentTab = -1;
        }
        if (newTab == null) {
            return false;
        }
        int index = mTabQueue.indexOf(newTab);
        if (index != -1) {
            mTabQueue.remove(index);
        }
        mTabQueue.add(newTab);
        mCurrentTab = mTabs.indexOf(newTab);
        WebView mainView = newTab.getWebView();
        boolean needRestore = (mainView == null);
        if (needRestore) {
            mainView = createNewWebView();
            newTab.setWebView(mainView);
        }
        newTab.putInForeground();
        if (needRestore) {
            if (!newTab.restoreState(newTab.getSavedState())) {
                mainView.loadUrl(BrowserSettings.getInstance().getHomePage());
            }
        }
        return true;
    }
}
