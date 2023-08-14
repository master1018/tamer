public class BrowserActivity extends Activity
    implements View.OnCreateContextMenuListener, DownloadListener {
    private final static boolean DEBUG = com.android.browser.Browser.DEBUG;
    private final static boolean LOGV_ENABLED = com.android.browser.Browser.LOGV_ENABLED;
    private final static boolean LOGD_ENABLED = com.android.browser.Browser.LOGD_ENABLED;
    private static final int SHORTCUT_INVALID = 0;
    private static final int SHORTCUT_GOOGLE_SEARCH = 1;
    private static final int SHORTCUT_WIKIPEDIA_SEARCH = 2;
    private static final int SHORTCUT_DICTIONARY_SEARCH = 3;
    private static final int SHORTCUT_GOOGLE_MOBILE_LOCAL_SEARCH = 4;
    private static class ClearThumbnails extends AsyncTask<File, Void, Void> {
        @Override
        public Void doInBackground(File... files) {
            if (files != null) {
                for (File f : files) {
                    if (!f.delete()) {
                      Log.e(LOGTAG, f.getPath() + " was not deleted");
                    }
                }
            }
            return null;
        }
    }
    private FrameLayout mBrowserFrameLayout;
    @Override
    public void onCreate(Bundle icicle) {
        if (LOGV_ENABLED) {
            Log.v(LOGTAG, this + " onStart");
        }
        super.onCreate(icicle);
        if (false) {
            getWindow().setFormat(PixelFormat.RGBX_8888);
            BitmapFactory.setDefaultConfig(Bitmap.Config.ARGB_8888);
        }
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        mResolver = getContentResolver();
        if (handleWebSearchIntent(getIntent())) {
            finish();
            return;
        }
        mSecLockIcon = Resources.getSystem().getDrawable(
                android.R.drawable.ic_secure);
        mMixLockIcon = Resources.getSystem().getDrawable(
                android.R.drawable.ic_partial_secure);
        FrameLayout frameLayout = (FrameLayout) getWindow().getDecorView()
                .findViewById(com.android.internal.R.id.content);
        mBrowserFrameLayout = (FrameLayout) LayoutInflater.from(this)
                .inflate(R.layout.custom_screen, null);
        mContentView = (FrameLayout) mBrowserFrameLayout.findViewById(
                R.id.main_content);
        mErrorConsoleContainer = (LinearLayout) mBrowserFrameLayout
                .findViewById(R.id.error_console);
        mCustomViewContainer = (FrameLayout) mBrowserFrameLayout
                .findViewById(R.id.fullscreen_custom_content);
        frameLayout.addView(mBrowserFrameLayout, COVER_SCREEN_PARAMS);
        mTitleBar = new TitleBar(this);
        mTitleBar.setProgress(100);
        mFakeTitleBar = new TitleBar(this);
        mTabControl = new TabControl(this);
        retainIconsOnStartup();
        mSettings = BrowserSettings.getInstance();
        mSettings.setTabControl(mTabControl);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Browser");
        ConnectivityManager cm = (ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            mIsNetworkUp = info.isAvailable();
        }
        mNetworkStateChangedFilter = new IntentFilter();
        mNetworkStateChangedFilter.addAction(
                ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkStateIntentReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(
                            ConnectivityManager.CONNECTIVITY_ACTION)) {
                        NetworkInfo info = intent.getParcelableExtra(
                                ConnectivityManager.EXTRA_NETWORK_INFO);
                        String typeName = info.getTypeName();
                        String subtypeName = info.getSubtypeName();
                        sendNetworkType(typeName.toLowerCase(),
                                (subtypeName != null ? subtypeName.toLowerCase() : ""));
                        onNetworkToggle(info.isAvailable());
                    }
                }
            };
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mPackageInstallationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                final String packageName = intent.getData()
                        .getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(
                        Intent.EXTRA_REPLACING, false);
                if (Intent.ACTION_PACKAGE_REMOVED.equals(action) && replacing) {
                    return;
                }
                if (sGoogleApps.contains(packageName)) {
                    BrowserActivity.this.packageChanged(packageName,
                            Intent.ACTION_PACKAGE_ADDED.equals(action));
                }
                PackageManager pm = BrowserActivity.this.getPackageManager();
                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = pm.getPackageInfo(packageName,
                            PackageManager.GET_PERMISSIONS);
                } catch (PackageManager.NameNotFoundException e) {
                    return;
                }
                if (pkgInfo != null) {
                    String permissions[] = pkgInfo.requestedPermissions;
                    if (permissions == null) {
                        return;
                    }
                    boolean permissionOk = false;
                    for (String permit : permissions) {
                        if (PluginManager.PLUGIN_PERMISSION.equals(permit)) {
                            permissionOk = true;
                            break;
                        }
                    }
                    if (permissionOk) {
                        PluginManager.getInstance(BrowserActivity.this)
                                .refreshPlugins(
                                        Intent.ACTION_PACKAGE_ADDED
                                                .equals(action));
                    }
                }
            }
        };
        registerReceiver(mPackageInstallationReceiver, filter);
        if (!mTabControl.restoreState(icicle)) {
            new ClearThumbnails().execute(
                    mTabControl.getThumbnailDir().listFiles());
            CookieManager.getInstance().removeSessionCookie();
            final Intent intent = getIntent();
            final Bundle extra = intent.getExtras();
            UrlData urlData = getUrlDataFromIntent(intent);
            String action = intent.getAction();
            final Tab t = mTabControl.createNewTab(
                    (Intent.ACTION_VIEW.equals(action) &&
                    intent.getData() != null)
                    || RecognizerResultsIntent.ACTION_VOICE_SEARCH_RESULTS
                    .equals(action),
                    intent.getStringExtra(Browser.EXTRA_APPLICATION_ID), urlData.mUrl);
            mTabControl.setCurrentTab(t);
            attachTabToContentView(t);
            WebView webView = t.getWebView();
            if (extra != null) {
                int scale = extra.getInt(Browser.INITIAL_ZOOM_LEVEL, 0);
                if (scale > 0 && scale <= 1000) {
                    webView.setInitialScale(scale);
                }
            }
            if (urlData.isEmpty()) {
                loadUrl(webView, mSettings.getHomePage());
            } else {
                loadUrlDataIn(t, urlData);
            }
        } else {
            attachTabToContentView(mTabControl.getCurrentTab());
        }
        String jsFlags = mSettings.getJsFlags();
        if (jsFlags.trim().length() != 0) {
            mTabControl.getCurrentWebView().setJsFlags(jsFlags);
        }
        getInstalledPackages();
        mSystemAllowGeolocationOrigins
                = new SystemAllowGeolocationOrigins(getApplicationContext());
        mSystemAllowGeolocationOrigins.start();
    }
     void showVoiceSearchResults(String result) {
        ContentProviderClient client = mResolver.acquireContentProviderClient(
                Browser.BOOKMARKS_URI);
        ContentProvider prov = client.getLocalContentProvider();
        BrowserProvider bp = (BrowserProvider) prov;
        bp.setQueryResults(mTabControl.getCurrentTab().getVoiceSearchResults());
        client.release();
        Bundle bundle = createGoogleSearchSourceBundle(
                GOOGLE_SEARCH_SOURCE_SEARCHKEY);
        bundle.putBoolean(SearchManager.CONTEXT_IS_VOICE, true);
        startSearch(result, false, bundle, false);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            current = mTabControl.getTab(0);
            if (current == null) {
                return;
            }
            mTabControl.setCurrentTab(current);
            attachTabToContentView(current);
            resetTitleAndIcon(current.getWebView());
        }
        final String action = intent.getAction();
        final int flags = intent.getFlags();
        if (Intent.ACTION_MAIN.equals(action) ||
                (flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
        ((SearchManager) getSystemService(Context.SEARCH_SERVICE))
                .stopSearch();
        boolean activateVoiceSearch = RecognizerResultsIntent
                .ACTION_VOICE_SEARCH_RESULTS.equals(action);
        if (Intent.ACTION_VIEW.equals(action)
                || Intent.ACTION_SEARCH.equals(action)
                || MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)
                || Intent.ACTION_WEB_SEARCH.equals(action)
                || activateVoiceSearch) {
            if (current.isInVoiceSearchMode()) {
                String title = current.getVoiceDisplayTitle();
                if (title != null && title.equals(intent.getStringExtra(
                        SearchManager.QUERY))) {
                    return;
                }
                if (Intent.ACTION_SEARCH.equals(action)
                        && current.voiceSearchSourceIsGoogle()) {
                    Intent logIntent = new Intent(
                            LoggingEvents.ACTION_LOG_EVENT);
                    logIntent.putExtra(LoggingEvents.EXTRA_EVENT,
                            LoggingEvents.VoiceSearch.QUERY_UPDATED);
                    logIntent.putExtra(
                            LoggingEvents.VoiceSearch.EXTRA_QUERY_UPDATED_VALUE,
                            intent.getDataString());
                    sendBroadcast(logIntent);
                }
            }
            if (handleWebSearchIntent(intent)) {
                return;
            }
            UrlData urlData = getUrlDataFromIntent(intent);
            if (urlData.isEmpty()) {
                urlData = new UrlData(mSettings.getHomePage());
            }
            final String appId = intent
                    .getStringExtra(Browser.EXTRA_APPLICATION_ID);
            if (!TextUtils.isEmpty(urlData.mUrl) &&
                    urlData.mUrl.startsWith("javascript:")) {
                openTabAndShow(urlData, true, appId);
                return;
            }
            if ((Intent.ACTION_VIEW.equals(action)
                    || (activateVoiceSearch && appId != null))
                    && !getPackageName().equals(appId)
                    && (flags & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
                Tab appTab = mTabControl.getTabFromId(appId);
                if (appTab != null) {
                    Log.i(LOGTAG, "Reusing tab for " + appId);
                    dismissSubWindow(appTab);
                    removeTabFromContentView(appTab);
                    boolean needsLoad =
                            mTabControl.recreateWebView(appTab, urlData);
                    if (current != appTab) {
                        switchToTab(mTabControl.getTabIndex(appTab));
                        if (needsLoad) {
                            loadUrlDataIn(appTab, urlData);
                        }
                    } else {
                        attachTabToContentView(appTab);
                        if (needsLoad) {
                            loadUrlDataIn(appTab, urlData);
                        }
                    }
                    return;
                } else {
                    appTab = mTabControl.findUnusedTabWithUrl(urlData.mUrl);
                    if (appTab != null) {
                        if (current != appTab) {
                            switchToTab(mTabControl.getTabIndex(appTab));
                        }
                    } else {
                        openTabAndShow(urlData, true, appId);
                    }
                }
            } else {
                if (!urlData.isEmpty()
                        && urlData.mUrl.startsWith("about:debug")) {
                    if ("about:debug.dom".equals(urlData.mUrl)) {
                        current.getWebView().dumpDomTree(false);
                    } else if ("about:debug.dom.file".equals(urlData.mUrl)) {
                        current.getWebView().dumpDomTree(true);
                    } else if ("about:debug.render".equals(urlData.mUrl)) {
                        current.getWebView().dumpRenderTree(false);
                    } else if ("about:debug.render.file".equals(urlData.mUrl)) {
                        current.getWebView().dumpRenderTree(true);
                    } else if ("about:debug.display".equals(urlData.mUrl)) {
                        current.getWebView().dumpDisplayTree();
                    } else if (urlData.mUrl.startsWith("about:debug.drag")) {
                        int index = urlData.mUrl.codePointAt(16) - '0';
                        if (index <= 0 || index > 9) {
                            current.getWebView().setDragTracker(null);
                        } else {
                            current.getWebView().setDragTracker(new MeshTracker(index));
                        }
                    } else {
                        mSettings.toggleDebugSettings();
                    }
                    return;
                }
                dismissSubWindow(current);
                current.setAppId(null);
                loadUrlDataIn(current, urlData);
            }
        }
    }
    private int parseUrlShortcut(String url) {
        if (url == null) return SHORTCUT_INVALID;
        if (url.length() > 2 && url.charAt(1) == ' ') {
            switch (url.charAt(0)) {
            case 'g': return SHORTCUT_GOOGLE_SEARCH;
            case 'w': return SHORTCUT_WIKIPEDIA_SEARCH;
            case 'd': return SHORTCUT_DICTIONARY_SEARCH;
            case 'l': return SHORTCUT_GOOGLE_MOBILE_LOCAL_SEARCH;
            }
        }
        return SHORTCUT_INVALID;
    }
    private boolean handleWebSearchIntent(Intent intent) {
        if (intent == null) return false;
        String url = null;
        final String action = intent.getAction();
        if (RecognizerResultsIntent.ACTION_VOICE_SEARCH_RESULTS.equals(
                action)) {
            return false;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            if (data != null) url = data.toString();
        } else if (Intent.ACTION_SEARCH.equals(action)
                || MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)
                || Intent.ACTION_WEB_SEARCH.equals(action)) {
            url = intent.getStringExtra(SearchManager.QUERY);
        }
        return handleWebSearchRequest(url, intent.getBundleExtra(SearchManager.APP_DATA),
                intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
    }
    private boolean handleWebSearchRequest(String inUrl, Bundle appData, String extraData) {
        if (inUrl == null) return false;
        String url = fixUrl(inUrl).trim();
        if (Patterns.WEB_URL.matcher(url).matches()
                || ACCEPTED_URI_SCHEMA.matcher(url).matches()
                || parseUrlShortcut(url) != SHORTCUT_INVALID) {
            return false;
        }
        final ContentResolver cr = mResolver;
        final String newUrl = url;
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... unused) {
                Browser.updateVisitedHistory(cr, newUrl, false);
                Browser.addSearchUrl(cr, newUrl);
                return null;
            }
        }.execute();
        SearchEngine searchEngine = mSettings.getSearchEngine();
        if (searchEngine == null) return false;
        searchEngine.startSearch(this, url, appData, extraData);
        return true;
    }
    private UrlData getUrlDataFromIntent(Intent intent) {
        String url = "";
        Map<String, String> headers = null;
        if (intent != null) {
            final String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                url = smartUrlFilter(intent.getData());
                if (url != null && url.startsWith("content:")) {
                    String mimeType = intent.resolveType(getContentResolver());
                    if (mimeType != null) {
                        url += "?" + mimeType;
                    }
                }
                if (url != null && url.startsWith("http")) {
                    final Bundle pairs = intent
                            .getBundleExtra(Browser.EXTRA_HEADERS);
                    if (pairs != null && !pairs.isEmpty()) {
                        Iterator<String> iter = pairs.keySet().iterator();
                        headers = new HashMap<String, String>();
                        while (iter.hasNext()) {
                            String key = iter.next();
                            headers.put(key, pairs.getString(key));
                        }
                    }
                }
            } else if (Intent.ACTION_SEARCH.equals(action)
                    || MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)
                    || Intent.ACTION_WEB_SEARCH.equals(action)) {
                url = intent.getStringExtra(SearchManager.QUERY);
                if (url != null) {
                    mLastEnteredUrl = url;
                    url = fixUrl(url);
                    url = smartUrlFilter(url);
                    final ContentResolver cr = mResolver;
                    final String newUrl = url;
                    new AsyncTask<Void, Void, Void>() {
                        protected Void doInBackground(Void... unused) {
                            Browser.updateVisitedHistory(cr, newUrl, false);
                            return null;
                        }
                    }.execute();
                    String searchSource = "&source=android-" + GOOGLE_SEARCH_SOURCE_SUGGEST + "&";
                    if (url.contains(searchSource)) {
                        String source = null;
                        final Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
                        if (appData != null) {
                            source = appData.getString(Search.SOURCE);
                        }
                        if (TextUtils.isEmpty(source)) {
                            source = GOOGLE_SEARCH_SOURCE_UNKNOWN;
                        }
                        url = url.replace(searchSource, "&source=android-"+source+"&");
                    }
                }
            }
        }
        return new UrlData(url, headers, intent);
    }
     void showVoiceTitleBar(String title) {
        mTitleBar.setInVoiceMode(true);
        mFakeTitleBar.setInVoiceMode(true);
        mTitleBar.setDisplayTitle(title);
        mFakeTitleBar.setDisplayTitle(title);
    }
     void revertVoiceTitleBar() {
        mTitleBar.setInVoiceMode(false);
        mFakeTitleBar.setInVoiceMode(false);
        mTitleBar.setDisplayTitle(mUrl);
        mFakeTitleBar.setDisplayTitle(mUrl);
    }
     static String fixUrl(String inUrl) {
        int colon = inUrl.indexOf(':');
        boolean allLower = true;
        for (int index = 0; index < colon; index++) {
            char ch = inUrl.charAt(index);
            if (!Character.isLetter(ch)) {
                break;
            }
            allLower &= Character.isLowerCase(ch);
            if (index == colon - 1 && !allLower) {
                inUrl = inUrl.substring(0, colon).toLowerCase()
                        + inUrl.substring(colon);
            }
        }
        if (inUrl.startsWith("http:
            return inUrl;
        if (inUrl.startsWith("http:") ||
                inUrl.startsWith("https:")) {
            if (inUrl.startsWith("http:/") || inUrl.startsWith("https:/")) {
                inUrl = inUrl.replaceFirst("/", "
            } else inUrl = inUrl.replaceFirst(":", ":
        }
        return inUrl;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (LOGV_ENABLED) {
            Log.v(LOGTAG, "BrowserActivity.onResume: this=" + this);
        }
        if (!mActivityInPause) {
            Log.e(LOGTAG, "BrowserActivity is already resumed.");
            return;
        }
        mTabControl.resumeCurrentTab();
        mActivityInPause = false;
        resumeWebViewTimers();
        if (mWakeLock.isHeld()) {
            mHandler.removeMessages(RELEASE_WAKELOCK);
            mWakeLock.release();
        }
        registerReceiver(mNetworkStateIntentReceiver,
                         mNetworkStateChangedFilter);
        WebView.enablePlatformNotifications();
    }
    private TitleBar mFakeTitleBar;
    private boolean mOptionsMenuOpen;
    private boolean mConfigChanged;
    private boolean mIconView;
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (Window.FEATURE_OPTIONS_PANEL == featureId) {
            if (mOptionsMenuOpen) {
                if (mConfigChanged) {
                    mConfigChanged = false;
                } else {
                    if (mIconView) {
                        hideFakeTitleBar();
                        mIconView = false;
                    } else {
                        showFakeTitleBar();
                        mIconView = true;
                    }
                }
            } else {
                showFakeTitleBar();
                mOptionsMenuOpen = true;
                mConfigChanged = false;
                mIconView = true;
            }
        }
        return true;
    }
    private void showFakeTitleBar() {
        if (mFakeTitleBar.getParent() == null && mActiveTabsPage == null
                && !mActivityInPause) {
            WebView mainView = mTabControl.getCurrentWebView();
            if (mainView == null) {
                return;
            }
            WindowManager manager
                    = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params
                    = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP;
            boolean atTop = mainView.getScrollY() == 0;
            params.windowAnimations = atTop ? 0 : R.style.TitleBar;
            manager.addView(mFakeTitleBar, params);
        }
    }
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        mOptionsMenuOpen = false;
        if (!mInLoad) {
            hideFakeTitleBar();
        } else if (!mIconView) {
            showFakeTitleBar();
        }
    }
    private void hideFakeTitleBar() {
        if (mFakeTitleBar.getParent() == null) return;
        WindowManager.LayoutParams params = (WindowManager.LayoutParams)
                mFakeTitleBar.getLayoutParams();
        WebView mainView = mTabControl.getCurrentWebView();
        params.windowAnimations = mainView != null && mainView.getScrollY() == 0
                ? 0 : R.style.TitleBar;
        WindowManager manager
                    = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        manager.updateViewLayout(mFakeTitleBar, params);
        manager.removeView(mFakeTitleBar);
    }
     void showTitleBarContextMenu() {
        if (null == mTitleBar.getParent()) {
            return;
        }
        openContextMenu(mTitleBar);
    }
    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        if (mInLoad) {
            showFakeTitleBar();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (LOGV_ENABLED) {
            Log.v(LOGTAG, "BrowserActivity.onSaveInstanceState: this=" + this);
        }
        mTabControl.saveState(outState);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mActivityInPause) {
            Log.e(LOGTAG, "BrowserActivity is already paused.");
            return;
        }
        mTabControl.pauseCurrentTab();
        mActivityInPause = true;
        if (mTabControl.getCurrentIndex() >= 0 && !pauseWebViewTimers()) {
            mWakeLock.acquire();
            mHandler.sendMessageDelayed(mHandler
                    .obtainMessage(RELEASE_WAKELOCK), WAKELOCK_TIMEOUT);
        }
        if (mActiveTabsPage != null) {
            removeActiveTabPage(true);
        }
        cancelStopToast();
        unregisterReceiver(mNetworkStateIntentReceiver);
        WebView.disablePlatformNotifications();
    }
    @Override
    protected void onDestroy() {
        if (LOGV_ENABLED) {
            Log.v(LOGTAG, "BrowserActivity.onDestroy: this=" + this);
        }
        super.onDestroy();
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
            mUploadMessage = null;
        }
        if (mTabControl == null) return;
        hideFakeTitleBar();
        Tab t = mTabControl.getCurrentTab();
        if (t != null) {
            dismissSubWindow(t);
            removeTabFromContentView(t);
        }
        mTabControl.destroy();
        WebIconDatabase.getInstance().close();
        unregisterReceiver(mPackageInstallationReceiver);
        mSystemAllowGeolocationOrigins.stop();
        mSystemAllowGeolocationOrigins = null;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mConfigChanged = true;
        super.onConfigurationChanged(newConfig);
        if (mPageInfoDialog != null) {
            mPageInfoDialog.dismiss();
            showPageInfo(
                mPageInfoView,
                mPageInfoFromShowSSLCertificateOnError);
        }
        if (mSSLCertificateDialog != null) {
            mSSLCertificateDialog.dismiss();
            showSSLCertificate(
                mSSLCertificateView);
        }
        if (mSSLCertificateOnErrorDialog != null) {
            mSSLCertificateOnErrorDialog.dismiss();
            showSSLCertificateOnError(
                mSSLCertificateOnErrorView,
                mSSLCertificateOnErrorHandler,
                mSSLCertificateOnErrorError);
        }
        if (mHttpAuthenticationDialog != null) {
            String title = ((TextView) mHttpAuthenticationDialog
                    .findViewById(com.android.internal.R.id.alertTitle)).getText()
                    .toString();
            String name = ((TextView) mHttpAuthenticationDialog
                    .findViewById(R.id.username_edit)).getText().toString();
            String password = ((TextView) mHttpAuthenticationDialog
                    .findViewById(R.id.password_edit)).getText().toString();
            int focusId = mHttpAuthenticationDialog.getCurrentFocus()
                    .getId();
            mHttpAuthenticationDialog.dismiss();
            showHttpAuthentication(mHttpAuthHandler, null, null, title,
                    name, password, focusId);
        }
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mTabControl.freeMemory();
    }
    private void resumeWebViewTimers() {
        Tab tab = mTabControl.getCurrentTab();
        if (tab == null) return; 
        boolean inLoad = tab.inLoad();
        if ((!mActivityInPause && !inLoad) || (mActivityInPause && inLoad)) {
            CookieSyncManager.getInstance().startSync();
            WebView w = tab.getWebView();
            if (w != null) {
                w.resumeTimers();
            }
        }
    }
    private boolean pauseWebViewTimers() {
        Tab tab = mTabControl.getCurrentTab();
        boolean inLoad = tab.inLoad();
        if (mActivityInPause && !inLoad) {
            CookieSyncManager.getInstance().stopSync();
            WebView w = mTabControl.getCurrentWebView();
            if (w != null) {
                w.pauseTimers();
            }
            return true;
        } else {
            return false;
        }
    }
    private void retainIconsOnStartup() {
        final WebIconDatabase db = WebIconDatabase.getInstance();
        db.open(getDir("icons", 0).getPath());
        Cursor c = null;
        try {
            c = Browser.getAllBookmarks(mResolver);
            if (c.moveToFirst()) {
                int urlIndex = c.getColumnIndex(Browser.BookmarkColumns.URL);
                do {
                    String url = c.getString(urlIndex);
                    db.retainIconForPageUrl(url);
                } while (c.moveToNext());
            }
        } catch (IllegalStateException e) {
            Log.e(LOGTAG, "retainIconsOnStartup", e);
        } finally {
            if (c!= null) c.close();
        }
    }
    WebView getTopWindow() {
        return mTabControl.getCurrentTopWebView();
    }
    TabControl getTabControl() {
        return mTabControl;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser, menu);
        mMenu = menu;
        updateInLoadMenuItems();
        return true;
    }
    private void updateInLoadMenuItems() {
        if (mMenu == null) {
            return;
        }
        MenuItem src = mInLoad ?
                mMenu.findItem(R.id.stop_menu_id):
                    mMenu.findItem(R.id.reload_menu_id);
        MenuItem dest = mMenu.findItem(R.id.stop_reload_menu_id);
        dest.setIcon(src.getIcon());
        dest.setTitle(src.getTitle());
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mCanChord = true;
        int id = item.getItemId();
        boolean result = true;
        switch (id) {
            case R.id.title_bar_copy_page_url:
                Tab currentTab = mTabControl.getCurrentTab();
                if (null == currentTab) {
                    result = false;
                    break;
                }
                WebView mainView = currentTab.getWebView();
                if (null == mainView) {
                    result = false;
                    break;
                }
                copy(mainView.getUrl());
                break;
            case R.id.open_context_menu_id:
            case R.id.open_newtab_context_menu_id:
            case R.id.bookmark_context_menu_id:
            case R.id.save_link_context_menu_id:
            case R.id.share_link_context_menu_id:
            case R.id.copy_link_context_menu_id:
                final WebView webView = getTopWindow();
                if (null == webView) {
                    result = false;
                    break;
                }
                final HashMap hrefMap = new HashMap();
                hrefMap.put("webview", webView);
                final Message msg = mHandler.obtainMessage(
                        FOCUS_NODE_HREF, id, 0, hrefMap);
                webView.requestFocusNodeHref(msg);
                break;
            default:
                result = onOptionsItemSelected(item);
        }
        mCanChord = false;
        return result;
    }
    private Bundle createGoogleSearchSourceBundle(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(Search.SOURCE, source);
        return bundle;
    }
     void editUrl() {
        if (mOptionsMenuOpen) closeOptionsMenu();
        String url = (getTopWindow() == null) ? null : getTopWindow().getUrl();
        startSearch(mSettings.getHomePage().equals(url) ? null : url, true,
                null, false);
    }
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery,
            Bundle appSearchData, boolean globalSearch) {
        if (appSearchData == null) {
            appSearchData = createGoogleSearchSourceBundle(GOOGLE_SEARCH_SOURCE_TYPE);
        }
        SearchEngine searchEngine = mSettings.getSearchEngine();
        if (searchEngine != null && !searchEngine.supportsVoiceSearch()) {
            appSearchData.putBoolean(SearchManager.DISABLE_VOICE_SEARCH, true);
        }
        super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
    }
     boolean switchToTab(int index) {
        Tab tab = mTabControl.getTab(index);
        Tab currentTab = mTabControl.getCurrentTab();
        if (tab == null || tab == currentTab) {
            return false;
        }
        if (currentTab != null) {
            removeTabFromContentView(currentTab);
        }
        mTabControl.setCurrentTab(tab);
        attachTabToContentView(tab);
        resetTitleIconAndProgress();
        updateLockIconToLatest();
        return true;
    }
     Tab openTabToHomePage() {
        return openTabAndShow(mSettings.getHomePage(), false, null);
    }
     void closeCurrentWindow() {
        final Tab current = mTabControl.getCurrentTab();
        if (mTabControl.getTabCount() == 1) {
            openTabToHomePage();
            closeTab(current);
            return;
        }
        final Tab parent = current.getParentTab();
        int indexToShow = -1;
        if (parent != null) {
            indexToShow = mTabControl.getTabIndex(parent);
        } else {
            final int currentIndex = mTabControl.getCurrentIndex();
            indexToShow = currentIndex + 1;
            if (indexToShow > mTabControl.getTabCount() - 1) {
                indexToShow = currentIndex - 1;
            }
        }
        if (switchToTab(indexToShow)) {
            closeTab(current);
        }
    }
    private ActiveTabsPage mActiveTabsPage;
     void removeActiveTabPage(boolean needToAttach) {
        mContentView.removeView(mActiveTabsPage);
        mActiveTabsPage = null;
        mMenuState = R.id.MAIN_MENU;
        if (needToAttach) {
            attachTabToContentView(mTabControl.getCurrentTab());
        }
        getTopWindow().requestFocus();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mCanChord) {
            return false;
        }
        if (null == getTopWindow()) {
            return false;
        }
        if (mMenuIsDown) {
            mMenuIsDown = false;
        }
        switch (item.getItemId()) {
            case R.id.new_tab_menu_id:
                openTabToHomePage();
                break;
            case R.id.goto_menu_id:
                editUrl();
                break;
            case R.id.bookmarks_menu_id:
                bookmarksOrHistoryPicker(false);
                break;
            case R.id.active_tabs_menu_id:
                mActiveTabsPage = new ActiveTabsPage(this, mTabControl);
                removeTabFromContentView(mTabControl.getCurrentTab());
                hideFakeTitleBar();
                mContentView.addView(mActiveTabsPage, COVER_SCREEN_PARAMS);
                mActiveTabsPage.requestFocus();
                mMenuState = EMPTY_MENU;
                break;
            case R.id.add_bookmark_menu_id:
                Intent i = new Intent(BrowserActivity.this,
                        AddBookmarkPage.class);
                WebView w = getTopWindow();
                i.putExtra("url", w.getUrl());
                i.putExtra("title", w.getTitle());
                i.putExtra("touch_icon_url", w.getTouchIconUrl());
                i.putExtra("thumbnail", createScreenshot(w));
                startActivity(i);
                break;
            case R.id.stop_reload_menu_id:
                if (mInLoad) {
                    stopLoading();
                } else {
                    getTopWindow().reload();
                }
                break;
            case R.id.back_menu_id:
                getTopWindow().goBack();
                break;
            case R.id.forward_menu_id:
                getTopWindow().goForward();
                break;
            case R.id.close_menu_id:
                if (mTabControl.getCurrentSubWindow() != null) {
                    dismissSubWindow(mTabControl.getCurrentTab());
                    break;
                }
                closeCurrentWindow();
                break;
            case R.id.homepage_menu_id:
                Tab current = mTabControl.getCurrentTab();
                if (current != null) {
                    dismissSubWindow(current);
                    loadUrl(current.getWebView(), mSettings.getHomePage());
                }
                break;
            case R.id.preferences_menu_id:
                Intent intent = new Intent(this,
                        BrowserPreferencesPage.class);
                intent.putExtra(BrowserPreferencesPage.CURRENT_PAGE,
                        getTopWindow().getUrl());
                startActivityForResult(intent, PREFERENCES_PAGE);
                break;
            case R.id.find_menu_id:
                if (null == mFindDialog) {
                    mFindDialog = new FindDialog(this);
                }
                mFindDialog.setWebView(getTopWindow());
                mFindDialog.show();
                getTopWindow().setFindIsUp(true);
                mMenuState = EMPTY_MENU;
                break;
            case R.id.select_text_id:
                getTopWindow().emulateShiftHeld();
                break;
            case R.id.page_info_menu_id:
                showPageInfo(mTabControl.getCurrentTab(), false);
                break;
            case R.id.classic_history_menu_id:
                bookmarksOrHistoryPicker(true);
                break;
            case R.id.title_bar_share_page_url:
            case R.id.share_page_menu_id:
                Tab currentTab = mTabControl.getCurrentTab();
                if (null == currentTab) {
                    mCanChord = false;
                    return false;
                }
                currentTab.populatePickerData();
                sharePage(this, currentTab.getTitle(),
                        currentTab.getUrl(), currentTab.getFavicon(),
                        createScreenshot(currentTab.getWebView()));
                break;
            case R.id.dump_nav_menu_id:
                getTopWindow().debugDump();
                break;
            case R.id.dump_counters_menu_id:
                getTopWindow().dumpV8Counters();
                break;
            case R.id.zoom_in_menu_id:
                getTopWindow().zoomIn();
                break;
            case R.id.zoom_out_menu_id:
                getTopWindow().zoomOut();
                break;
            case R.id.view_downloads_menu_id:
                viewDownloads(null);
                break;
            case R.id.window_one_menu_id:
            case R.id.window_two_menu_id:
            case R.id.window_three_menu_id:
            case R.id.window_four_menu_id:
            case R.id.window_five_menu_id:
            case R.id.window_six_menu_id:
            case R.id.window_seven_menu_id:
            case R.id.window_eight_menu_id:
                {
                    int menuid = item.getItemId();
                    for (int id = 0; id < WINDOW_SHORTCUT_ID_ARRAY.length; id++) {
                        if (WINDOW_SHORTCUT_ID_ARRAY[id] == menuid) {
                            Tab desiredTab = mTabControl.getTab(id);
                            if (desiredTab != null &&
                                    desiredTab != mTabControl.getCurrentTab()) {
                                switchToTab(id);
                            }
                            break;
                        }
                    }
                }
                break;
            default:
                if (!super.onOptionsItemSelected(item)) {
                    return false;
                }
        }
        mCanChord = false;
        return true;
    }
    public void closeFind() {
        mMenuState = R.id.MAIN_MENU;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mCanChord = true;
        super.onPrepareOptionsMenu(menu);
        switch (mMenuState) {
            case EMPTY_MENU:
                if (mCurrentMenuState != mMenuState) {
                    menu.setGroupVisible(R.id.MAIN_MENU, false);
                    menu.setGroupEnabled(R.id.MAIN_MENU, false);
                    menu.setGroupEnabled(R.id.MAIN_SHORTCUT_MENU, false);
                }
                break;
            default:
                if (mCurrentMenuState != mMenuState) {
                    menu.setGroupVisible(R.id.MAIN_MENU, true);
                    menu.setGroupEnabled(R.id.MAIN_MENU, true);
                    menu.setGroupEnabled(R.id.MAIN_SHORTCUT_MENU, true);
                }
                final WebView w = getTopWindow();
                boolean canGoBack = false;
                boolean canGoForward = false;
                boolean isHome = false;
                if (w != null) {
                    canGoBack = w.canGoBack();
                    canGoForward = w.canGoForward();
                    isHome = mSettings.getHomePage().equals(w.getUrl());
                }
                final MenuItem back = menu.findItem(R.id.back_menu_id);
                back.setEnabled(canGoBack);
                final MenuItem home = menu.findItem(R.id.homepage_menu_id);
                home.setEnabled(!isHome);
                menu.findItem(R.id.forward_menu_id)
                        .setEnabled(canGoForward);
                menu.findItem(R.id.new_tab_menu_id).setEnabled(
                        mTabControl.canCreateNewTab());
                PackageManager pm = getPackageManager();
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                ResolveInfo ri = pm.resolveActivity(send, PackageManager.MATCH_DEFAULT_ONLY);
                menu.findItem(R.id.share_page_menu_id).setVisible(ri != null);
                boolean isNavDump = mSettings.isNavDump();
                final MenuItem nav = menu.findItem(R.id.dump_nav_menu_id);
                nav.setVisible(isNavDump);
                nav.setEnabled(isNavDump);
                boolean showDebugSettings = mSettings.showDebugSettings();
                final MenuItem counter = menu.findItem(R.id.dump_counters_menu_id);
                counter.setVisible(showDebugSettings);
                counter.setEnabled(showDebugSettings);
                break;
        }
        mCurrentMenuState = mMenuState;
        return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (v instanceof TitleBar) {
            return;
        }
        WebView webview = (WebView) v;
        WebView.HitTestResult result = webview.getHitTestResult();
        if (result == null) {
            return;
        }
        int type = result.getType();
        if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
            Log.w(LOGTAG,
                    "We should not show context menu when nothing is touched");
            return;
        }
        if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
            return;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browsercontext, menu);
        String extra = result.getExtra();
        menu.setGroupVisible(R.id.PHONE_MENU,
                type == WebView.HitTestResult.PHONE_TYPE);
        menu.setGroupVisible(R.id.EMAIL_MENU,
                type == WebView.HitTestResult.EMAIL_TYPE);
        menu.setGroupVisible(R.id.GEO_MENU,
                type == WebView.HitTestResult.GEO_TYPE);
        menu.setGroupVisible(R.id.IMAGE_MENU,
                type == WebView.HitTestResult.IMAGE_TYPE
                || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE);
        menu.setGroupVisible(R.id.ANCHOR_MENU,
                type == WebView.HitTestResult.SRC_ANCHOR_TYPE
                || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE);
        switch (type) {
            case WebView.HitTestResult.PHONE_TYPE:
                menu.setHeaderTitle(Uri.decode(extra));
                menu.findItem(R.id.dial_context_menu_id).setIntent(
                        new Intent(Intent.ACTION_VIEW, Uri
                                .parse(WebView.SCHEME_TEL + extra)));
                Intent addIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                addIntent.putExtra(Insert.PHONE, Uri.decode(extra));
                addIntent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                menu.findItem(R.id.add_contact_context_menu_id).setIntent(
                        addIntent);
                menu.findItem(R.id.copy_phone_context_menu_id).setOnMenuItemClickListener(
                        new Copy(extra));
                break;
            case WebView.HitTestResult.EMAIL_TYPE:
                menu.setHeaderTitle(extra);
                menu.findItem(R.id.email_context_menu_id).setIntent(
                        new Intent(Intent.ACTION_VIEW, Uri
                                .parse(WebView.SCHEME_MAILTO + extra)));
                menu.findItem(R.id.copy_mail_context_menu_id).setOnMenuItemClickListener(
                        new Copy(extra));
                break;
            case WebView.HitTestResult.GEO_TYPE:
                menu.setHeaderTitle(extra);
                menu.findItem(R.id.map_context_menu_id).setIntent(
                        new Intent(Intent.ACTION_VIEW, Uri
                                .parse(WebView.SCHEME_GEO
                                        + URLEncoder.encode(extra))));
                menu.findItem(R.id.copy_geo_context_menu_id).setOnMenuItemClickListener(
                        new Copy(extra));
                break;
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                TextView titleView = (TextView) LayoutInflater.from(this)
                        .inflate(android.R.layout.browser_link_context_header,
                        null);
                titleView.setText(extra);
                menu.setHeaderView(titleView);
                menu.findItem(R.id.open_newtab_context_menu_id).setVisible(
                        mTabControl.canCreateNewTab());
                menu.findItem(R.id.bookmark_context_menu_id).setVisible(
                        Bookmarks.urlHasAcceptableScheme(extra));
                PackageManager pm = getPackageManager();
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                ResolveInfo ri = pm.resolveActivity(send, PackageManager.MATCH_DEFAULT_ONLY);
                menu.findItem(R.id.share_link_context_menu_id).setVisible(ri != null);
                if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                    break;
                }
            case WebView.HitTestResult.IMAGE_TYPE:
                if (type == WebView.HitTestResult.IMAGE_TYPE) {
                    menu.setHeaderTitle(extra);
                }
                menu.findItem(R.id.view_image_context_menu_id).setIntent(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(extra)));
                menu.findItem(R.id.download_context_menu_id).
                        setOnMenuItemClickListener(new Download(extra));
                menu.findItem(R.id.set_wallpaper_context_menu_id).
                        setOnMenuItemClickListener(new SetAsWallpaper(extra));
                break;
            default:
                Log.w(LOGTAG, "We should not get here.");
                break;
        }
        hideFakeTitleBar();
    }
    private void attachTabToContentView(Tab t) {
        t.attachTabToContentView(mContentView);
        if (mShouldShowErrorConsole) {
            ErrorConsoleView errorConsole = t.getErrorConsole(true);
            if (errorConsole.numberOfErrors() == 0) {
                errorConsole.showConsole(ErrorConsoleView.SHOW_NONE);
            } else {
                errorConsole.showConsole(ErrorConsoleView.SHOW_MINIMIZED);
            }
            mErrorConsoleContainer.addView(errorConsole,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                  ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        WebView view = t.getWebView();
        view.setEmbeddedTitleBar(mTitleBar);
        if (t.isInVoiceSearchMode()) {
            showVoiceTitleBar(t.getVoiceDisplayTitle());
        } else {
            revertVoiceTitleBar();
        }
        t.getTopWindow().requestFocus();
    }
    void attachSubWindow(Tab t) {
        t.attachSubWindow(mContentView);
        getTopWindow().requestFocus();
    }
    private void removeTabFromContentView(Tab t) {
        t.removeTabFromContentView(mContentView);
        ErrorConsoleView errorConsole = t.getErrorConsole(false);
        if (errorConsole != null) {
            mErrorConsoleContainer.removeView(errorConsole);
        }
        WebView view = t.getWebView();
        if (view != null) {
            view.setEmbeddedTitleBar(null);
        }
    }
     void dismissSubWindow(Tab t) {
        t.removeSubWindow(mContentView);
        t.dismissSubWindow();
        getTopWindow().requestFocus();
    }
    private Tab openTabAndShow(String url, boolean closeOnExit, String appId) {
        return openTabAndShow(new UrlData(url), closeOnExit, appId);
    }
    Tab openTabAndShow(UrlData urlData, boolean closeOnExit,
            String appId) {
        Tab currentTab = mTabControl.getCurrentTab();
        if (!mTabControl.canCreateNewTab()) {
            Tab closeTab = mTabControl.getTab(0);
            closeTab(closeTab);
            if (closeTab == currentTab) {
                currentTab = null;
            }
        }
        final Tab tab = mTabControl.createNewTab(closeOnExit, appId,
                urlData.mUrl);
        WebView webview = tab.getWebView();
        if (currentTab != null) {
            removeTabFromContentView(currentTab);
        }
        mTabControl.setCurrentTab(tab);
        attachTabToContentView(tab);
        if (!urlData.isEmpty()) {
            loadUrlDataIn(tab, urlData);
        }
        return tab;
    }
    private Tab openTab(String url) {
        if (mSettings.openInBackground()) {
            Tab t = mTabControl.createNewTab();
            if (t != null) {
                WebView view = t.getWebView();
                loadUrl(view, url);
            }
            return t;
        } else {
            return openTabAndShow(url, false, null);
        }
    }
    private class Copy implements OnMenuItemClickListener {
        private CharSequence mText;
        public boolean onMenuItemClick(MenuItem item) {
            copy(mText);
            return true;
        }
        public Copy(CharSequence toCopy) {
            mText = toCopy;
        }
    }
    private class Download implements OnMenuItemClickListener {
        private String mText;
        public boolean onMenuItemClick(MenuItem item) {
            onDownloadStartNoStream(mText, null, null, null, -1);
            return true;
        }
        public Download(String toDownload) {
            mText = toDownload;
        }
    }
    private class SetAsWallpaper extends Thread implements
            OnMenuItemClickListener, DialogInterface.OnCancelListener {
        private URL mUrl;
        private ProgressDialog mWallpaperProgress;
        private boolean mCanceled = false;
        public SetAsWallpaper(String url) {
            try {
                mUrl = new URL(url);
            } catch (MalformedURLException e) {
                mUrl = null;
            }
        }
        public void onCancel(DialogInterface dialog) {
            mCanceled = true;
        }
        public boolean onMenuItemClick(MenuItem item) {
            if (mUrl != null) {
                mWallpaperProgress = new ProgressDialog(BrowserActivity.this);
                mWallpaperProgress.setIndeterminate(true);
                mWallpaperProgress.setMessage(getText(R.string.progress_dialog_setting_wallpaper));
                mWallpaperProgress.setCancelable(true);
                mWallpaperProgress.setOnCancelListener(this);
                mWallpaperProgress.show();
                start();
            }
            return true;
        }
        public void run() {
            Drawable oldWallpaper = BrowserActivity.this.getWallpaper();
            try {
                InputStream inputstream = mUrl.openStream();
                if (inputstream != null) {
                    setWallpaper(inputstream);
                }
            } catch (IOException e) {
                Log.e(LOGTAG, "Unable to set new wallpaper");
                mCanceled = true;
            }
            if (mCanceled) {
                int width = oldWallpaper.getIntrinsicWidth();
                int height = oldWallpaper.getIntrinsicHeight();
                Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bm);
                oldWallpaper.setBounds(0, 0, width, height);
                oldWallpaper.draw(canvas);
                try {
                    setWallpaper(bm);
                } catch (IOException e) {
                    Log.e(LOGTAG, "Unable to restore old wallpaper.");
                }
                mCanceled = false;
            }
            if (mWallpaperProgress.isShowing()) {
                mWallpaperProgress.dismiss();
            }
        }
    }
    private void copy(CharSequence text) {
        try {
            IClipboard clip = IClipboard.Stub.asInterface(ServiceManager.getService("clipboard"));
            if (clip != null) {
                clip.setClipboardText(text);
            }
        } catch (android.os.RemoteException e) {
            Log.e(LOGTAG, "Copy failed", e);
        }
    }
     void resetTitleAndRevertLockIcon() {
        mTabControl.getCurrentTab().revertLockIcon();
        updateLockIconToLatest();
        resetTitleIconAndProgress();
    }
    private void resetTitleIconAndProgress() {
        WebView current = mTabControl.getCurrentWebView();
        if (current == null) {
            return;
        }
        resetTitleAndIcon(current);
        int progress = current.getProgress();
        current.getWebChromeClient().onProgressChanged(current, progress);
    }
    private void resetTitleAndIcon(WebView view) {
        WebHistoryItem item = view.copyBackForwardList().getCurrentItem();
        if (item != null) {
            setUrlTitle(item.getUrl(), item.getTitle());
            setFavicon(item.getFavicon());
        } else {
            setUrlTitle(null, null);
            setFavicon(null);
        }
    }
    void setUrlTitle(String url, String title) {
        mUrl = url;
        mTitle = title;
        if (mTabControl.getCurrentTab().isInVoiceSearchMode()) return;
        mTitleBar.setDisplayTitle(url);
        mFakeTitleBar.setDisplayTitle(url);
    }
     static String buildTitleUrl(String url) {
        String titleUrl = null;
        if (url != null) {
            try {
                URL urlObj = new URL(url);
                if (urlObj != null) {
                    titleUrl = "";
                    String protocol = urlObj.getProtocol();
                    String host = urlObj.getHost();
                    if (host != null && 0 < host.length()) {
                        titleUrl = host;
                        if (protocol != null) {
                            if (protocol.equalsIgnoreCase("https")) {
                                titleUrl = protocol + ":
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {}
        }
        return titleUrl;
    }
    void setFavicon(Bitmap icon) {
        mTitleBar.setFavicon(icon);
        mFakeTitleBar.setFavicon(icon);
    }
     void closeTab(Tab t) {
        int currentIndex = mTabControl.getCurrentIndex();
        int removeIndex = mTabControl.getTabIndex(t);
        mTabControl.removeTab(t);
        if (currentIndex >= removeIndex && currentIndex != 0) {
            currentIndex--;
        }
        mTabControl.setCurrentTab(mTabControl.getTab(currentIndex));
        resetTitleIconAndProgress();
    }
     void goBackOnePageOrQuit() {
        Tab current = mTabControl.getCurrentTab();
        if (current == null) {
            moveTaskToBack(true);
            return;
        }
        WebView w = current.getWebView();
        if (w.canGoBack()) {
            w.goBack();
        } else {
            Tab parent = current.getParentTab();
            if (parent != null) {
                switchToTab(mTabControl.getTabIndex(parent));
                closeTab(current);
            } else {
                if (current.closeOnExit()) {
                    mTabControl.getCurrentTab().clearInLoad();
                    if (mTabControl.getTabCount() == 1) {
                        finish();
                        return;
                    }
                    boolean savedState = mActivityInPause;
                    if (savedState) {
                        Log.e(LOGTAG, "BrowserActivity is already paused "
                                + "while handing goBackOnePageOrQuit.");
                    }
                    mActivityInPause = true;
                    pauseWebViewTimers();
                    mActivityInPause = savedState;
                    removeTabFromContentView(current);
                    mTabControl.removeTab(current);
                }
                moveTaskToBack(true);
            }
        }
    }
    boolean isMenuDown() {
        return mMenuIsDown;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_MENU == keyCode) {
            mMenuIsDown = true;
            return super.onKeyDown(keyCode, event);
        }
        if (mMenuIsDown) return true;
        switch(keyCode) {
            case KeyEvent.KEYCODE_SPACE:
                if (event.isShiftPressed()) {
                    getTopWindow().pageUp(false);
                } else {
                    getTopWindow().pageDown(false);
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0) {
                    event.startTracking();
                    return true;
                } else if (mCustomView == null && mActiveTabsPage == null
                        && event.isLongPress()) {
                    bookmarksOrHistoryPicker(true);
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_MENU:
                mMenuIsDown = false;
                break;
            case KeyEvent.KEYCODE_BACK:
                if (event.isTracking() && !event.isCanceled()) {
                    if (mCustomView != null) {
                        mTabControl.getCurrentWebView().getWebChromeClient()
                                .onHideCustomView();
                    } else if (mActiveTabsPage != null) {
                        removeActiveTabPage(true);
                    } else {
                        WebView subwindow = mTabControl.getCurrentSubWindow();
                        if (subwindow != null) {
                            if (subwindow.canGoBack()) {
                                subwindow.goBack();
                            } else {
                                dismissSubWindow(mTabControl.getCurrentTab());
                            }
                        } else {
                            goBackOnePageOrQuit();
                        }
                    }
                    return true;
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
     void stopLoading() {
        mDidStopLoad = true;
        resetTitleAndRevertLockIcon();
        WebView w = getTopWindow();
        w.stopLoading();
        mTabControl.getCurrentWebView().getWebViewClient().onPageFinished(w,
                w.getUrl());
        cancelStopToast();
        mStopToast = Toast
                .makeText(this, R.string.stopping, Toast.LENGTH_SHORT);
        mStopToast.show();
    }
    boolean didUserStopLoading() {
        return mDidStopLoad;
    }
    private void cancelStopToast() {
        if (mStopToast != null) {
            mStopToast.cancel();
            mStopToast = null;
        }
    }
    public void postMessage(int what, int arg1, int arg2, Object obj,
            long delayMillis) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(what, arg1, arg2,
                obj), delayMillis);
    }
    void removeMessages(int what, Object object) {
        mHandler.removeMessages(what, object);
    }
    public final static int LOAD_URL                = 1001;
    public final static int STOP_LOAD               = 1002;
    private static final int FOCUS_NODE_HREF         = 102;
    private static final int RELEASE_WAKELOCK        = 107;
    static final int UPDATE_BOOKMARK_THUMBNAIL       = 108;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FOCUS_NODE_HREF:
                {
                    String url = (String) msg.getData().get("url");
                    String title = (String) msg.getData().get("title");
                    if (url == null || url.length() == 0) {
                        break;
                    }
                    HashMap focusNodeMap = (HashMap) msg.obj;
                    WebView view = (WebView) focusNodeMap.get("webview");
                    if (getTopWindow() != view) {
                        break;
                    }
                    switch (msg.arg1) {
                        case R.id.open_context_menu_id:
                        case R.id.view_image_context_menu_id:
                            loadUrlFromContext(getTopWindow(), url);
                            break;
                        case R.id.open_newtab_context_menu_id:
                            final Tab parent = mTabControl.getCurrentTab();
                            final Tab newTab = openTab(url);
                            if (newTab != parent) {
                                parent.addChildTab(newTab);
                            }
                            break;
                        case R.id.bookmark_context_menu_id:
                            Intent intent = new Intent(BrowserActivity.this,
                                    AddBookmarkPage.class);
                            intent.putExtra("url", url);
                            intent.putExtra("title", title);
                            startActivity(intent);
                            break;
                        case R.id.share_link_context_menu_id:
                            StringBuilder sb = new StringBuilder(
                                    Browser.BookmarkColumns.URL + " = ");
                            DatabaseUtils.appendEscapedSQLString(sb, url);
                            Cursor c = mResolver.query(Browser.BOOKMARKS_URI,
                                    Browser.HISTORY_PROJECTION,
                                    sb.toString(),
                                    null,
                                    null);
                            if (c.moveToFirst()) {
                                Bitmap favicon = null;
                                Bitmap thumbnail = null;
                                String linkTitle = c.getString(Browser.
                                        HISTORY_PROJECTION_TITLE_INDEX);
                                byte[] data = c.getBlob(Browser.
                                        HISTORY_PROJECTION_FAVICON_INDEX);
                                if (data != null) {
                                    favicon = BitmapFactory.decodeByteArray(
                                            data, 0, data.length);
                                }
                                data = c.getBlob(Browser.
                                        HISTORY_PROJECTION_THUMBNAIL_INDEX);
                                if (data != null) {
                                    thumbnail = BitmapFactory.decodeByteArray(
                                            data, 0, data.length);
                                }
                                sharePage(BrowserActivity.this,
                                        linkTitle, url, favicon, thumbnail);
                            } else {
                                Browser.sendString(BrowserActivity.this, url,
                                        getString(
                                        R.string.choosertitle_sharevia));
                            }
                            break;
                        case R.id.copy_link_context_menu_id:
                            copy(url);
                            break;
                        case R.id.save_link_context_menu_id:
                        case R.id.download_context_menu_id:
                            onDownloadStartNoStream(url, null, null, null, -1);
                            break;
                    }
                    break;
                }
                case LOAD_URL:
                    loadUrlFromContext(getTopWindow(), (String) msg.obj);
                    break;
                case STOP_LOAD:
                    stopLoading();
                    break;
                case RELEASE_WAKELOCK:
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                        mTabControl.stopAllLoading();
                    }
                    break;
                case UPDATE_BOOKMARK_THUMBNAIL:
                    WebView view = (WebView) msg.obj;
                    if (view != null) {
                        updateScreenshot(view);
                    }
                    break;
            }
        }
    };
    public static final void sharePage(Context c, String title, String url,
            Bitmap favicon, Bitmap screenshot) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, url);
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(Browser.EXTRA_SHARE_FAVICON, favicon);
        send.putExtra(Browser.EXTRA_SHARE_SCREENSHOT, screenshot);
        try {
            c.startActivity(Intent.createChooser(send, c.getString(
                    R.string.choosertitle_sharevia)));
        } catch(android.content.ActivityNotFoundException ex) {
        }
    }
    private void updateScreenshot(WebView view) {
        final Bitmap bm = createScreenshot(view);
        if (bm == null) {
            return;
        }
        final ContentResolver cr = getContentResolver();
        final String url = view.getUrl();
        final String originalUrl = view.getOriginalUrl();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                Cursor c = null;
                try {
                    c = BrowserBookmarksAdapter.queryBookmarksForUrl(
                            cr, originalUrl, url, true);
                    if (c != null) {
                        if (c.moveToFirst()) {
                            ContentValues values = new ContentValues();
                            final ByteArrayOutputStream os
                                    = new ByteArrayOutputStream();
                            bm.compress(Bitmap.CompressFormat.PNG, 100, os);
                            values.put(Browser.BookmarkColumns.THUMBNAIL,
                                    os.toByteArray());
                            do {
                                cr.update(ContentUris.withAppendedId(
                                        Browser.BOOKMARKS_URI, c.getInt(0)),
                                        values, null, null);
                            } while (c.moveToNext());
                        }
                    }
                } catch (IllegalStateException e) {
                } finally {
                    if (c != null) c.close();
                }
                return null;
            }
        }.execute();
    }
    private static int THUMBNAIL_WIDTH = 0;
    private static int THUMBNAIL_HEIGHT = 0;
     static int getDesiredThumbnailWidth(Context context) {
        if (THUMBNAIL_WIDTH == 0) {
            float density = context.getResources().getDisplayMetrics().density;
            THUMBNAIL_WIDTH = (int) (90 * density);
            THUMBNAIL_HEIGHT = (int) (80 * density);
        }
        return THUMBNAIL_WIDTH;
    }
     static int getDesiredThumbnailHeight(Context context) {
        getDesiredThumbnailWidth(context);
        return THUMBNAIL_HEIGHT;
    }
    private Bitmap createScreenshot(WebView view) {
        Picture thumbnail = view.capturePicture();
        if (thumbnail == null) {
            return null;
        }
        Bitmap bm = Bitmap.createBitmap(getDesiredThumbnailWidth(this),
                getDesiredThumbnailHeight(this), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bm);
        int thumbnailWidth = thumbnail.getWidth();
        int thumbnailHeight = thumbnail.getHeight();
        float scaleFactorX = 1.0f;
        float scaleFactorY = 1.0f;
        if (thumbnailWidth > 0) {
            scaleFactorX = (float) getDesiredThumbnailWidth(this) /
                    (float)thumbnailWidth;
        } else {
            return null;
        }
        if (view.getWidth() > view.getHeight() &&
                thumbnailHeight < view.getHeight() && thumbnailHeight > 0) {
            scaleFactorY = (float) getDesiredThumbnailHeight(this) /
                    (float)thumbnailHeight;
        } else {
            scaleFactorY = scaleFactorX;
        }
        canvas.scale(scaleFactorX, scaleFactorY);
        thumbnail.draw(canvas);
        return bm;
    }
     final static String SCHEME_WTAI = "wtai:
     final static String SCHEME_WTAI_MC = "wtai:
     final static String SCHEME_WTAI_SD = "wtai:
     final static String SCHEME_WTAI_AP = "wtai:
    private final static int INITIAL_PROGRESS = 10;
    void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (mActivityInPause) resumeWebViewTimers();
        resetLockIcon(url);
        setUrlTitle(url, null);
        setFavicon(favicon);
        onProgressChanged(view, INITIAL_PROGRESS);
        mDidStopLoad = false;
        if (!mIsNetworkUp) createAndShowNetworkDialog();
        if (mSettings.isTracing()) {
            String host;
            try {
                WebAddress uri = new WebAddress(url);
                host = uri.mHost;
            } catch (android.net.ParseException ex) {
                host = "browser";
            }
            host = host.replace('.', '_');
            host += ".trace";
            mInTrace = true;
            Debug.startMethodTracing(host, 20 * 1024 * 1024);
        }
        if (false) {
            mStart = SystemClock.uptimeMillis();
            mProcessStart = Process.getElapsedCpuTime();
            long[] sysCpu = new long[7];
            if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null,
                    sysCpu, null)) {
                mUserStart = sysCpu[0] + sysCpu[1];
                mSystemStart = sysCpu[2];
                mIdleStart = sysCpu[3];
                mIrqStart = sysCpu[4] + sysCpu[5] + sysCpu[6];
            }
            mUiStart = SystemClock.currentThreadTimeMillis();
        }
    }
    void onPageFinished(WebView view, String url) {
        resetTitleAndIcon(view);
        updateLockIconToLatest();
        if (mActivityInPause && pauseWebViewTimers()) {
            if (mWakeLock.isHeld()) {
                mHandler.removeMessages(RELEASE_WAKELOCK);
                mWakeLock.release();
            }
        }
        if (false) {
            long[] sysCpu = new long[7];
            if (Process.readProcFile("/proc/stat", SYSTEM_CPU_FORMAT, null,
                    sysCpu, null)) {
                String uiInfo = "UI thread used "
                        + (SystemClock.currentThreadTimeMillis() - mUiStart)
                        + " ms";
                if (LOGD_ENABLED) {
                    Log.d(LOGTAG, uiInfo);
                }
                String performanceString = "It took total "
                        + (SystemClock.uptimeMillis() - mStart)
                        + " ms clock time to load the page."
                        + "\nbrowser process used "
                        + (Process.getElapsedCpuTime() - mProcessStart)
                        + " ms, user processes used "
                        + (sysCpu[0] + sysCpu[1] - mUserStart) * 10
                        + " ms, kernel used "
                        + (sysCpu[2] - mSystemStart) * 10
                        + " ms, idle took " + (sysCpu[3] - mIdleStart) * 10
                        + " ms and irq took "
                        + (sysCpu[4] + sysCpu[5] + sysCpu[6] - mIrqStart)
                        * 10 + " ms, " + uiInfo;
                if (LOGD_ENABLED) {
                    Log.d(LOGTAG, performanceString + "\nWebpage: " + url);
                }
                if (url != null) {
                    String newUrl = new String(url);
                    if (newUrl.startsWith("http:
                        newUrl = newUrl.substring(11);
                    } else if (newUrl.startsWith("http:
                        newUrl = newUrl.substring(7);
                    } else if (newUrl.startsWith("https:
                        newUrl = newUrl.substring(12);
                    } else if (newUrl.startsWith("https:
                        newUrl = newUrl.substring(8);
                    }
                    if (LOGD_ENABLED) {
                        Log.d(LOGTAG, newUrl + " loaded");
                    }
                }
            }
         }
        if (mInTrace) {
            mInTrace = false;
            Debug.stopMethodTracing();
        }
    }
    boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith(SCHEME_WTAI)) {
            if (url.startsWith(SCHEME_WTAI_MC)) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(WebView.SCHEME_TEL +
                        url.substring(SCHEME_WTAI_MC.length())));
                startActivity(intent);
                return true;
            }
            if (url.startsWith(SCHEME_WTAI_SD)) {
                return false;
            }
            if (url.startsWith(SCHEME_WTAI_AP)) {
                return false;
            }
        }
        if (url.startsWith("about:") || url.startsWith("javascript:")) {
            return false;
        }
        Intent intent;
        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException ex) {
            Log.w("Browser", "Bad URI " + url + ": " + ex.getMessage());
            return false;
        }
        if (getPackageManager().resolveActivity(intent, 0) == null) {
            String packagename = intent.getPackage();
            if (packagename != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse("market:
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        }
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);
        try {
            if (startActivityIfNeeded(intent, -1)) {
                return true;
            }
        } catch (ActivityNotFoundException ex) {
        }
        if (mMenuIsDown) {
            openTab(url);
            closeOptionsMenu();
            return true;
        }
        return false;
    }
    void onProgressChanged(WebView view, int newProgress) {
        mFakeTitleBar.setProgress(newProgress);
        if (newProgress == 100) {
            if (mInLoad) {
                mInLoad = false;
                updateInLoadMenuItems();
                if (!mOptionsMenuOpen || !mIconView) {
                    hideFakeTitleBar();
                }
            }
        } else {
            if (!mInLoad) {
                mInLoad = true;
                updateInLoadMenuItems();
            }
            if (!mOptionsMenuOpen || mIconView) {
                showFakeTitleBar();
            }
        }
    }
    void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
        mCustomViewContainer.addView(view, COVER_SCREEN_GRAVITY_CENTER);
        mCustomView = view;
        mCustomViewCallback = callback;
        mOldMenuState = mMenuState;
        mMenuState = EMPTY_MENU;
        mContentView.setVisibility(View.GONE);
        setStatusBarVisibility(false);
        mCustomViewContainer.setVisibility(View.VISIBLE);
        mCustomViewContainer.bringToFront();
    }
    void onHideCustomView() {
        if (mCustomView == null)
            return;
        mCustomView.setVisibility(View.GONE);
        mCustomViewContainer.removeView(mCustomView);
        mCustomView = null;
        mMenuState = mOldMenuState;
        mOldMenuState = EMPTY_MENU;
        mCustomViewContainer.setVisibility(View.GONE);
        mCustomViewCallback.onCustomViewHidden();
        setStatusBarVisibility(true);
        mContentView.setVisibility(View.VISIBLE);
    }
    Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = BitmapFactory.decodeResource(
                    getResources(), R.drawable.default_video_poster);
        }
        return mDefaultVideoPoster;
    }
    View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(BrowserActivity.this);
            mVideoProgressView = inflater.inflate(
                    R.layout.video_loading_progress, null);
        }
        return mVideoProgressView;
    }
    private ValueCallback<Uri> mUploadMessage;
    void openFileChooser(ValueCallback<Uri> uploadMsg) {
        if (mUploadMessage != null) return;
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*
    public void onDownloadStart(String url, String userAgent,
            String contentDisposition, String mimetype, long contentLength) {
        if (contentDisposition == null
                || !contentDisposition.regionMatches(
                        true, 0, "attachment", 0, 10)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), mimetype);
            ResolveInfo info = getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                ComponentName myName = getComponentName();
                if (!myName.getPackageName().equals(
                        info.activityInfo.packageName)
                        || !myName.getClassName().equals(
                                info.activityInfo.name)) {
                    try {
                        startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException ex) {
                        if (LOGD_ENABLED) {
                            Log.d(LOGTAG, "activity not found for " + mimetype
                                    + " over " + Uri.parse(url).getScheme(),
                                    ex);
                        }
                    }
                }
            }
        }
        onDownloadStartNoStream(url, userAgent, contentDisposition, mimetype, contentLength);
    }
    private static String encodePath(String path) {
        char[] chars = path.toCharArray();
        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']') {
                needed = true;
                break;
            }
        }
        if (needed == false) {
            return path;
        }
        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
     void onDownloadStartNoStream(String url, String userAgent,
            String contentDisposition, String mimetype, long contentLength) {
        String filename = URLUtil.guessFileName(url,
                contentDisposition, mimetype);
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            int title;
            String msg;
            if (status.equals(Environment.MEDIA_SHARED)) {
                msg = getString(R.string.download_sdcard_busy_dlg_msg);
                title = R.string.download_sdcard_busy_dlg_title;
            } else {
                msg = getString(R.string.download_no_sdcard_dlg_msg, filename);
                title = R.string.download_no_sdcard_dlg_title;
            }
            new AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, null)
                .show();
            return;
        }
        WebAddress webAddress;
        try {
            webAddress = new WebAddress(url);
            webAddress.mPath = encodePath(webAddress.mPath);
        } catch (Exception e) {
            Log.e(LOGTAG, "Exception trying to parse url:" + url);
            return;
        }
        String cookies = CookieManager.getInstance().getCookie(url);
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_URI, webAddress.toString());
        values.put(Downloads.Impl.COLUMN_COOKIE_DATA, cookies);
        values.put(Downloads.Impl.COLUMN_USER_AGENT, userAgent);
        values.put(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE,
                getPackageName());
        values.put(Downloads.Impl.COLUMN_NOTIFICATION_CLASS,
                OpenDownloadReceiver.class.getCanonicalName());
        values.put(Downloads.Impl.COLUMN_VISIBILITY,
                Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        values.put(Downloads.Impl.COLUMN_MIME_TYPE, mimetype);
        values.put(Downloads.Impl.COLUMN_FILE_NAME_HINT, filename);
        values.put(Downloads.Impl.COLUMN_DESCRIPTION, webAddress.mHost);
        if (contentLength > 0) {
            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, contentLength);
        }
        if (mimetype == null) {
            new FetchUrlMimeType(this).execute(values);
        } else {
            final Uri contentUri =
                    getContentResolver().insert(Downloads.Impl.CONTENT_URI, values);
        }
        Toast.makeText(this, R.string.download_pending, Toast.LENGTH_SHORT)
                .show();
    }
    private void resetLockIcon(String url) {
        mTabControl.getCurrentTab().resetLockIcon(url);
        updateLockIconImage(LOCK_ICON_UNSECURE);
    }
    private void updateLockIconToLatest() {
        updateLockIconImage(mTabControl.getCurrentTab().getLockIconType());
    }
    private void updateLockIconImage(int lockIconType) {
        Drawable d = null;
        if (lockIconType == LOCK_ICON_SECURE) {
            d = mSecLockIcon;
        } else if (lockIconType == LOCK_ICON_MIXED) {
            d = mMixLockIcon;
        }
        mTitleBar.setLock(d);
        mFakeTitleBar.setLock(d);
    }
    private void showPageInfo(final Tab tab,
                              final boolean fromShowSSLCertificateOnError) {
        final LayoutInflater factory = LayoutInflater
                .from(this);
        final View pageInfoView = factory.inflate(R.layout.page_info, null);
        final WebView view = tab.getWebView();
        String url = null;
        String title = null;
        if (view == null) {
            url = tab.getUrl();
            title = tab.getTitle();
        } else if (view == mTabControl.getCurrentWebView()) {
            url = mUrl;
            title = mTitle;
        } else {
            url = view.getUrl();
            title = view.getTitle();
        }
        if (url == null) {
            url = "";
        }
        if (title == null) {
            title = "";
        }
        ((TextView) pageInfoView.findViewById(R.id.address)).setText(url);
        ((TextView) pageInfoView.findViewById(R.id.title)).setText(title);
        mPageInfoView = tab;
        mPageInfoFromShowSSLCertificateOnError = fromShowSSLCertificateOnError;
        AlertDialog.Builder alertDialogBuilder =
            new AlertDialog.Builder(this)
            .setTitle(R.string.page_info).setIcon(android.R.drawable.ic_dialog_info)
            .setView(pageInfoView)
            .setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        mPageInfoDialog = null;
                        mPageInfoView = null;
                        if (fromShowSSLCertificateOnError) {
                            showSSLCertificateOnError(
                                mSSLCertificateOnErrorView,
                                mSSLCertificateOnErrorHandler,
                                mSSLCertificateOnErrorError);
                        }
                    }
                })
            .setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mPageInfoDialog = null;
                        mPageInfoView = null;
                        if (fromShowSSLCertificateOnError) {
                            showSSLCertificateOnError(
                                mSSLCertificateOnErrorView,
                                mSSLCertificateOnErrorHandler,
                                mSSLCertificateOnErrorError);
                        }
                    }
                });
        if (fromShowSSLCertificateOnError ||
                (view != null && view.getCertificate() != null)) {
            alertDialogBuilder.setNeutralButton(
                R.string.view_certificate,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        mPageInfoDialog = null;
                        mPageInfoView = null;
                        if (fromShowSSLCertificateOnError) {
                            showSSLCertificateOnError(
                                mSSLCertificateOnErrorView,
                                mSSLCertificateOnErrorHandler,
                                mSSLCertificateOnErrorError);
                        } else {
                            if (view.getCertificate() != null) {
                                showSSLCertificate(tab);
                            }
                        }
                    }
                });
        }
        mPageInfoDialog = alertDialogBuilder.show();
    }
    private void showSSLCertificate(final Tab tab) {
        final View certificateView =
                inflateCertificateView(tab.getWebView().getCertificate());
        if (certificateView == null) {
            return;
        }
        LayoutInflater factory = LayoutInflater.from(this);
        final LinearLayout placeholder =
                (LinearLayout)certificateView.findViewById(R.id.placeholder);
        LinearLayout ll = (LinearLayout) factory.inflate(
            R.layout.ssl_success, placeholder);
        ((TextView)ll.findViewById(R.id.success))
            .setText(R.string.ssl_certificate_is_valid);
        mSSLCertificateView = tab;
        mSSLCertificateDialog =
            new AlertDialog.Builder(this)
                .setTitle(R.string.ssl_certificate).setIcon(
                    R.drawable.ic_dialog_browser_certificate_secure)
                .setView(certificateView)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                mSSLCertificateDialog = null;
                                mSSLCertificateView = null;
                                showPageInfo(tab, false);
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mSSLCertificateDialog = null;
                                mSSLCertificateView = null;
                                showPageInfo(tab, false);
                            }
                        })
                .show();
    }
    void showSSLCertificateOnError(
        final WebView view, final SslErrorHandler handler, final SslError error) {
        final View certificateView =
            inflateCertificateView(error.getCertificate());
        if (certificateView == null) {
            return;
        }
        LayoutInflater factory = LayoutInflater.from(this);
        final LinearLayout placeholder =
                (LinearLayout)certificateView.findViewById(R.id.placeholder);
        if (error.hasError(SslError.SSL_UNTRUSTED)) {
            LinearLayout ll = (LinearLayout)factory
                .inflate(R.layout.ssl_warning, placeholder);
            ((TextView)ll.findViewById(R.id.warning))
                .setText(R.string.ssl_untrusted);
        }
        if (error.hasError(SslError.SSL_IDMISMATCH)) {
            LinearLayout ll = (LinearLayout)factory
                .inflate(R.layout.ssl_warning, placeholder);
            ((TextView)ll.findViewById(R.id.warning))
                .setText(R.string.ssl_mismatch);
        }
        if (error.hasError(SslError.SSL_EXPIRED)) {
            LinearLayout ll = (LinearLayout)factory
                .inflate(R.layout.ssl_warning, placeholder);
            ((TextView)ll.findViewById(R.id.warning))
                .setText(R.string.ssl_expired);
        }
        if (error.hasError(SslError.SSL_NOTYETVALID)) {
            LinearLayout ll = (LinearLayout)factory
                .inflate(R.layout.ssl_warning, placeholder);
            ((TextView)ll.findViewById(R.id.warning))
                .setText(R.string.ssl_not_yet_valid);
        }
        mSSLCertificateOnErrorHandler = handler;
        mSSLCertificateOnErrorView = view;
        mSSLCertificateOnErrorError = error;
        mSSLCertificateOnErrorDialog =
            new AlertDialog.Builder(this)
                .setTitle(R.string.ssl_certificate).setIcon(
                    R.drawable.ic_dialog_browser_certificate_partially_secure)
                .setView(certificateView)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                mSSLCertificateOnErrorDialog = null;
                                mSSLCertificateOnErrorView = null;
                                mSSLCertificateOnErrorHandler = null;
                                mSSLCertificateOnErrorError = null;
                                view.getWebViewClient().onReceivedSslError(
                                                view, handler, error);
                            }
                        })
                 .setNeutralButton(R.string.page_info_view,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                mSSLCertificateOnErrorDialog = null;
                                showPageInfo(mTabControl.getTabFromView(view),
                                        true);
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mSSLCertificateOnErrorDialog = null;
                                mSSLCertificateOnErrorView = null;
                                mSSLCertificateOnErrorHandler = null;
                                mSSLCertificateOnErrorError = null;
                                view.getWebViewClient().onReceivedSslError(
                                                view, handler, error);
                            }
                        })
                .show();
    }
    private View inflateCertificateView(SslCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        LayoutInflater factory = LayoutInflater.from(this);
        View certificateView = factory.inflate(
            R.layout.ssl_certificate, null);
        SslCertificate.DName issuedTo = certificate.getIssuedTo();
        if (issuedTo != null) {
            ((TextView) certificateView.findViewById(R.id.to_common))
                .setText(issuedTo.getCName());
            ((TextView) certificateView.findViewById(R.id.to_org))
                .setText(issuedTo.getOName());
            ((TextView) certificateView.findViewById(R.id.to_org_unit))
                .setText(issuedTo.getUName());
        }
        SslCertificate.DName issuedBy = certificate.getIssuedBy();
        if (issuedBy != null) {
            ((TextView) certificateView.findViewById(R.id.by_common))
                .setText(issuedBy.getCName());
            ((TextView) certificateView.findViewById(R.id.by_org))
                .setText(issuedBy.getOName());
            ((TextView) certificateView.findViewById(R.id.by_org_unit))
                .setText(issuedBy.getUName());
        }
        String issuedOn = formatCertificateDate(
            certificate.getValidNotBeforeDate());
        ((TextView) certificateView.findViewById(R.id.issued_on))
            .setText(issuedOn);
        String expiresOn = formatCertificateDate(
            certificate.getValidNotAfterDate());
        ((TextView) certificateView.findViewById(R.id.expires_on))
            .setText(expiresOn);
        return certificateView;
    }
    private String formatCertificateDate(Date certificateDate) {
      if (certificateDate == null) {
          return "";
      }
      String formattedDate = DateFormat.getDateFormat(this).format(certificateDate);
      if (formattedDate == null) {
          return "";
      }
      return formattedDate;
    }
    void showHttpAuthentication(final HttpAuthHandler handler,
            final String host, final String realm, final String title,
            final String name, final String password, int focusId) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View v = factory
                .inflate(R.layout.http_authentication, null);
        if (name != null) {
            ((EditText) v.findViewById(R.id.username_edit)).setText(name);
        }
        if (password != null) {
            ((EditText) v.findViewById(R.id.password_edit)).setText(password);
        }
        String titleText = title;
        if (titleText == null) {
            titleText = getText(R.string.sign_in_to).toString().replace(
                    "%s1", host).replace("%s2", realm);
        }
        mHttpAuthHandler = handler;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(titleText)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(v)
                .setPositiveButton(R.string.action,
                        new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog,
                                     int whichButton) {
                                String nm = ((EditText) v
                                        .findViewById(R.id.username_edit))
                                        .getText().toString();
                                String pw = ((EditText) v
                                        .findViewById(R.id.password_edit))
                                        .getText().toString();
                                BrowserActivity.this.setHttpAuthUsernamePassword
                                        (host, realm, nm, pw);
                                handler.proceed(nm, pw);
                                mHttpAuthenticationDialog = null;
                                mHttpAuthHandler = null;
                            }})
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                handler.cancel();
                                BrowserActivity.this.resetTitleAndRevertLockIcon();
                                mHttpAuthenticationDialog = null;
                                mHttpAuthHandler = null;
                            }})
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            handler.cancel();
                            BrowserActivity.this.resetTitleAndRevertLockIcon();
                            mHttpAuthenticationDialog = null;
                            mHttpAuthHandler = null;
                        }})
                .create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        if (focusId != 0) {
            dialog.findViewById(focusId).requestFocus();
        } else {
            v.findViewById(R.id.username_edit).requestFocus();
        }
        mHttpAuthenticationDialog = dialog;
    }
    public int getProgress() {
        WebView w = mTabControl.getCurrentWebView();
        if (w != null) {
            return w.getProgress();
        } else {
            return 100;
        }
    }
    public void setHttpAuthUsernamePassword(String host, String realm,
                                            String username,
                                            String password) {
        WebView w = getTopWindow();
        if (w != null) {
            w.setHttpAuthUsernamePassword(host, realm, username, password);
        }
    }
    public void onNetworkToggle(boolean up) {
        if (up == mIsNetworkUp) {
            return;
        } else if (up) {
            mIsNetworkUp = true;
            if (mAlertDialog != null) {
                mAlertDialog.cancel();
                mAlertDialog = null;
            }
        } else {
            mIsNetworkUp = false;
            if (mInLoad) {
                createAndShowNetworkDialog();
           }
        }
        WebView w = mTabControl.getCurrentWebView();
        if (w != null) {
            w.setNetworkAvailable(up);
        }
    }
    boolean isNetworkUp() {
        return mIsNetworkUp;
    }
    private void createAndShowNetworkDialog() {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.loadSuspendedTitle)
                    .setMessage(R.string.loadSuspended)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (getTopWindow() == null) return;
        switch (requestCode) {
            case COMBO_PAGE:
                if (resultCode == RESULT_OK && intent != null) {
                    String data = intent.getAction();
                    Bundle extras = intent.getExtras();
                    if (extras != null && extras.getBoolean("new_window", false)) {
                        openTab(data);
                    } else {
                        final Tab currentTab =
                                mTabControl.getCurrentTab();
                        dismissSubWindow(currentTab);
                        if (data != null && data.length() != 0) {
                            loadUrl(getTopWindow(), data);
                        }
                    }
                }
            case PREFERENCES_PAGE:
                if (resultCode == RESULT_OK && intent != null) {
                    String action = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (BrowserSettings.PREF_CLEAR_HISTORY.equals(action)) {
                        mTabControl.removeParentChildRelationShips();
                    }
                }
                break;
            case FILE_SELECTED:
                if (null == mUploadMessage) break;
                Uri result = intent == null || resultCode != RESULT_OK ? null
                        : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
                break;
            default:
                break;
        }
        getTopWindow().requestFocus();
    }
    private void viewDownloads(Uri downloadRecord) {
        Intent intent = new Intent(this,
                BrowserDownloadPage.class);
        intent.setData(downloadRecord);
        startActivityForResult(intent, BrowserActivity.DOWNLOAD_PAGE);
    }
     void bookmarksOrHistoryPicker(boolean startWithHistory) {
        WebView current = mTabControl.getCurrentWebView();
        if (current == null) {
            return;
        }
        Intent intent = new Intent(this,
                CombinedBookmarkHistoryActivity.class);
        String title = current.getTitle();
        String url = current.getUrl();
        Bitmap thumbnail = createScreenshot(current);
        if (null == url) {
            url = mLastEnteredUrl;
            if (null == url) {
                url = mSettings.getHomePage();
            }
        }
        if (title == null) {
            title = url;
        }
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("thumbnail", thumbnail);
        intent.putExtra("disable_new_window", !mTabControl.canCreateNewTab());
        intent.putExtra("touch_icon_url", current.getTouchIconUrl());
        if (startWithHistory) {
            intent.putExtra(CombinedBookmarkHistoryActivity.STARTING_TAB,
                    CombinedBookmarkHistoryActivity.HISTORY_TAB);
        }
        startActivityForResult(intent, COMBO_PAGE);
    }
    private void loadUrlFromContext(WebView view, String url) {
        if (url != null && url.length() != 0 && view != null) {
            url = smartUrlFilter(url);
            if (!view.getWebViewClient().shouldOverrideUrlLoading(view, url)) {
                loadUrl(view, url);
            }
        }
    }
    private void loadUrl(WebView view, String url) {
        updateTitleBarForNewLoad(view, url);
        view.loadUrl(url);
    }
    private void loadUrlDataIn(Tab t, UrlData data) {
        updateTitleBarForNewLoad(t.getWebView(), data.mUrl);
        data.loadIn(t);
    }
    private void updateTitleBarForNewLoad(WebView view, String url) {
        if (view == getTopWindow()) {
            setUrlTitle(url, null);
            setFavicon(null);
            onProgressChanged(view, INITIAL_PROGRESS);
        }
    }
    private String smartUrlFilter(Uri inUri) {
        if (inUri != null) {
            return smartUrlFilter(inUri.toString());
        }
        return null;
    }
    protected static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" + 
            "(" +    
            "(?:http|https|file):\\/\\/" +
            "|(?:inline|data|about|content|javascript):" +
            ")" +
            "(.*)" );
    String smartUrlFilter(String url) {
        String inUrl = url.trim();
        boolean hasSpace = inUrl.indexOf(' ') != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl);
        if (matcher.matches()) {
            String scheme = matcher.group(1);
            String lcScheme = scheme.toLowerCase();
            if (!lcScheme.equals(scheme)) {
                inUrl = lcScheme + matcher.group(2);
            }
            if (hasSpace) {
                inUrl = inUrl.replace(" ", "%20");
            }
            return inUrl;
        }
        if (hasSpace) {
            int shortcut = parseUrlShortcut(inUrl);
            if (shortcut != SHORTCUT_INVALID) {
                Browser.addSearchUrl(mResolver, inUrl);
                String query = inUrl.substring(2);
                switch (shortcut) {
                case SHORTCUT_GOOGLE_SEARCH:
                    return URLUtil.composeSearchUrl(query, QuickSearch_G, QUERY_PLACE_HOLDER);
                case SHORTCUT_WIKIPEDIA_SEARCH:
                    return URLUtil.composeSearchUrl(query, QuickSearch_W, QUERY_PLACE_HOLDER);
                case SHORTCUT_DICTIONARY_SEARCH:
                    return URLUtil.composeSearchUrl(query, QuickSearch_D, QUERY_PLACE_HOLDER);
                case SHORTCUT_GOOGLE_MOBILE_LOCAL_SEARCH:
                    return URLUtil.composeSearchUrl(query, QuickSearch_L, QUERY_PLACE_HOLDER);
                }
            }
        } else {
            if (Patterns.WEB_URL.matcher(inUrl).matches()) {
                return URLUtil.guessUrl(inUrl);
            }
        }
        Browser.addSearchUrl(mResolver, inUrl);
        return URLUtil.composeSearchUrl(inUrl, QuickSearch_G, QUERY_PLACE_HOLDER);
    }
     void setShouldShowErrorConsole(boolean flag) {
        if (flag == mShouldShowErrorConsole) {
            return;
        }
        mShouldShowErrorConsole = flag;
        ErrorConsoleView errorConsole = mTabControl.getCurrentTab()
                .getErrorConsole(true);
        if (flag) {
            if (errorConsole.numberOfErrors() > 0) {
                errorConsole.showConsole(ErrorConsoleView.SHOW_MINIMIZED);
            } else {
                errorConsole.showConsole(ErrorConsoleView.SHOW_NONE);
            }
            mErrorConsoleContainer.addView(errorConsole,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                  ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            mErrorConsoleContainer.removeView(errorConsole);
        }
    }
    boolean shouldShowErrorConsole() {
        return mShouldShowErrorConsole;
    }
    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
    private void sendNetworkType(String type, String subtype) {
        WebView w = mTabControl.getCurrentWebView();
        if (w != null) {
            w.setNetworkType(type, subtype);
        }
    }
    private void packageChanged(String packageName, boolean wasAdded) {
        WebView w = mTabControl.getCurrentWebView();
        if (w == null) {
            return;
        }
        if (wasAdded) {
            w.addPackageName(packageName);
        } else {
            w.removePackageName(packageName);
        }
    }
    private void addPackageNames(Set<String> packageNames) {
        WebView w = mTabControl.getCurrentWebView();
        if (w == null) {
            return;
        }
        w.addPackageNames(packageNames);
    }
    private void getInstalledPackages() {
        AsyncTask<Void, Void, Set<String> > task =
            new AsyncTask<Void, Void, Set<String> >() {
            protected Set<String> doInBackground(Void... unused) {
                Set<String> installedPackages = new HashSet<String>();
                PackageManager pm = BrowserActivity.this.getPackageManager();
                if (pm != null) {
                    List<PackageInfo> packages = pm.getInstalledPackages(0);
                    for (PackageInfo p : packages) {
                        if (BrowserActivity.this.sGoogleApps.contains(p.packageName)) {
                            installedPackages.add(p.packageName);
                        }
                    }
                }
                return installedPackages;
            }
            protected void onPostExecute(Set<String> installedPackages) {
                addPackageNames(installedPackages);
            }
        };
        task.execute();
    }
    final static int LOCK_ICON_UNSECURE = 0;
    final static int LOCK_ICON_SECURE   = 1;
    final static int LOCK_ICON_MIXED    = 2;
    private BrowserSettings mSettings;
    private TabControl      mTabControl;
    private ContentResolver mResolver;
    private FrameLayout     mContentView;
    private View            mCustomView;
    private FrameLayout     mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mCurrentMenuState = 0;
    private int mMenuState = R.id.MAIN_MENU;
    private int mOldMenuState = EMPTY_MENU;
    private static final int EMPTY_MENU = -1;
    private Menu mMenu;
    private FindDialog mFindDialog;
    boolean mCanChord;
    private boolean mInLoad;
    private boolean mIsNetworkUp;
    private boolean mDidStopLoad;
     boolean mActivityInPause = true;
    private boolean mMenuIsDown;
    private static boolean mInTrace;
    private static final int[] SYSTEM_CPU_FORMAT = new int[] {
            Process.PROC_SPACE_TERM | Process.PROC_COMBINE,
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG, 
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG, 
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG, 
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG, 
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG, 
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG, 
            Process.PROC_SPACE_TERM | Process.PROC_OUT_LONG  
    };
    private long mStart;
    private long mProcessStart;
    private long mUserStart;
    private long mSystemStart;
    private long mIdleStart;
    private long mIrqStart;
    private long mUiStart;
    private Drawable    mMixLockIcon;
    private Drawable    mSecLockIcon;
    private AlertDialog mAlertDialog;
    private String mUrl;
    private String mTitle;
    private AlertDialog mPageInfoDialog;
    private Tab mPageInfoView;
    private boolean mPageInfoFromShowSSLCertificateOnError;
    private AlertDialog mSSLCertificateOnErrorDialog;
    private WebView mSSLCertificateOnErrorView;
    private SslErrorHandler mSSLCertificateOnErrorHandler;
    private SslError mSSLCertificateOnErrorError;
    private AlertDialog mSSLCertificateDialog;
    private Tab mSSLCertificateView;
    private AlertDialog mHttpAuthenticationDialog;
    private HttpAuthHandler mHttpAuthHandler;
     static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
                                            new FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT);
     static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
                                            new FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            Gravity.CENTER);
    final static String QuickSearch_G = "http:
    final static String QuickSearch_W = "http:
    final static String QuickSearch_D = "http:
    final static String QuickSearch_L = "http:
    final static String QUERY_PLACE_HOLDER = "%s";
    final static String GOOGLE_SEARCH_SOURCE_SEARCHKEY = "browser-key";
    final static String GOOGLE_SEARCH_SOURCE_GOTO = "browser-goto";
    final static String GOOGLE_SEARCH_SOURCE_TYPE = "browser-type";
    final static String GOOGLE_SEARCH_SOURCE_SUGGEST = "browser-suggest";
    final static String GOOGLE_SEARCH_SOURCE_UNKNOWN = "unknown";
    private final static String LOGTAG = "browser";
    private String mLastEnteredUrl;
    private PowerManager.WakeLock mWakeLock;
    private final static int WAKELOCK_TIMEOUT = 5 * 60 * 1000; 
    private Toast mStopToast;
    private TitleBar mTitleBar;
    private LinearLayout mErrorConsoleContainer = null;
    private boolean mShouldShowErrorConsole = false;
    final static private int[] WINDOW_SHORTCUT_ID_ARRAY =
    { R.id.window_one_menu_id, R.id.window_two_menu_id, R.id.window_three_menu_id,
      R.id.window_four_menu_id, R.id.window_five_menu_id, R.id.window_six_menu_id,
      R.id.window_seven_menu_id, R.id.window_eight_menu_id };
    private IntentFilter mNetworkStateChangedFilter;
    private BroadcastReceiver mNetworkStateIntentReceiver;
    private BroadcastReceiver mPackageInstallationReceiver;
    private SystemAllowGeolocationOrigins mSystemAllowGeolocationOrigins;
    final static int COMBO_PAGE                 = 1;
    final static int DOWNLOAD_PAGE              = 2;
    final static int PREFERENCES_PAGE           = 3;
    final static int FILE_SELECTED              = 4;
    private Bitmap mDefaultVideoPoster;
    private View mVideoProgressView;
    private static Set<String> sGoogleApps;
    static {
        sGoogleApps = new HashSet<String>();
        sGoogleApps.add("com.google.android.youtube");
    }
     static class UrlData {
        final String mUrl;
        final Map<String, String> mHeaders;
        final Intent mVoiceIntent;
        UrlData(String url) {
            this.mUrl = url;
            this.mHeaders = null;
            this.mVoiceIntent = null;
        }
        UrlData(String url, Map<String, String> headers, Intent intent) {
            this.mUrl = url;
            this.mHeaders = headers;
            if (RecognizerResultsIntent.ACTION_VOICE_SEARCH_RESULTS
                    .equals(intent.getAction())) {
                this.mVoiceIntent = intent;
            } else {
                this.mVoiceIntent = null;
            }
        }
        boolean isEmpty() {
            return mVoiceIntent == null && (mUrl == null || mUrl.length() == 0);
        }
        public void loadIn(Tab t) {
            if (mVoiceIntent != null) {
                t.activateVoiceSearchMode(mVoiceIntent);
            } else {
                t.getWebView().loadUrl(mUrl, mHeaders);
            }
        }
    };
     static final UrlData EMPTY_URL_DATA = new UrlData(null);
}
