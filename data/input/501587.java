class BrowserSettings extends Observable {
    private boolean loadsImagesAutomatically;
    private boolean javaScriptEnabled;
    private WebSettings.PluginState pluginState;
    private boolean javaScriptCanOpenWindowsAutomatically;
    private boolean showSecurityWarnings;
    private boolean rememberPasswords;
    private boolean saveFormData;
    private boolean openInBackground;
    private String defaultTextEncodingName;
    private String homeUrl = "";
    private SearchEngine searchEngine;
    private boolean autoFitPage;
    private boolean landscapeOnly;
    private boolean loadsPageInOverviewMode;
    private boolean showDebugSettings;
    private boolean appCacheEnabled;
    private boolean databaseEnabled;
    private boolean domStorageEnabled;
    private boolean geolocationEnabled;
    private boolean workersEnabled;  
    private long appCacheMaxSize = Long.MAX_VALUE;
    private String appCachePath;  
    private String databasePath; 
    private String geolocationDatabasePath; 
    private WebStorageSizeManager webStorageSizeManager;
    private String jsFlags = "";
    private final static String TAG = "BrowserSettings";
    public WebSettings.LayoutAlgorithm layoutAlgorithm =
        WebSettings.LayoutAlgorithm.NARROW_COLUMNS;
    private boolean useWideViewPort = true;
    private int userAgent = 0;
    private boolean tracing = false;
    private boolean lightTouch = false;
    private boolean navDump = false;
    private boolean showConsole = true;
    private static int minimumFontSize = 8;
    private static int minimumLogicalFontSize = 8;
    private static int defaultFontSize = 16;
    private static int defaultFixedFontSize = 13;
    private static WebSettings.TextSize textSize =
        WebSettings.TextSize.NORMAL;
    private static WebSettings.ZoomDensity zoomDensity =
        WebSettings.ZoomDensity.MEDIUM;
    private static int pageCacheCapacity;
    public final static String PREF_CLEAR_CACHE = "privacy_clear_cache";
    public final static String PREF_CLEAR_COOKIES = "privacy_clear_cookies";
    public final static String PREF_CLEAR_HISTORY = "privacy_clear_history";
    public final static String PREF_HOMEPAGE = "homepage";
    public final static String PREF_SEARCH_ENGINE = "search_engine";
    public final static String PREF_CLEAR_FORM_DATA =
            "privacy_clear_form_data";
    public final static String PREF_CLEAR_PASSWORDS =
            "privacy_clear_passwords";
    public final static String PREF_EXTRAS_RESET_DEFAULTS =
            "reset_default_preferences";
    public final static String PREF_DEBUG_SETTINGS = "debug_menu";
    public final static String PREF_WEBSITE_SETTINGS = "website_settings";
    public final static String PREF_TEXT_SIZE = "text_size";
    public final static String PREF_DEFAULT_ZOOM = "default_zoom";
    public final static String PREF_DEFAULT_TEXT_ENCODING =
            "default_text_encoding";
    public final static String PREF_CLEAR_GEOLOCATION_ACCESS =
            "privacy_clear_geolocation_access";
    private static final String DESKTOP_USERAGENT = "Mozilla/5.0 (Macintosh; " +
            "U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/530.17 (KHTML, " +
            "like Gecko) Version/4.0 Safari/530.17";
    private static final String IPHONE_USERAGENT = "Mozilla/5.0 (iPhone; U; " +
            "CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 " +
            "(KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";
    public final static int MAX_TEXTVIEW_LEN = 80;
    private TabControl mTabControl;
    private static BrowserSettings sSingleton;
    private HashMap<WebSettings,Observer> mWebSettingsToObservers =
        new HashMap<WebSettings,Observer>();
    static class Observer implements java.util.Observer {
        private WebSettings mSettings;
        Observer(WebSettings w) {
            mSettings = w;
        }
        public void update(Observable o, Object arg) {
            BrowserSettings b = (BrowserSettings)o;
            WebSettings s = mSettings;
            s.setLayoutAlgorithm(b.layoutAlgorithm);
            if (b.userAgent == 0) {
                s.setUserAgentString(null);
            } else if (b.userAgent == 1) {
                s.setUserAgentString(DESKTOP_USERAGENT);
            } else if (b.userAgent == 2) {
                s.setUserAgentString(IPHONE_USERAGENT);
            }
            s.setUseWideViewPort(b.useWideViewPort);
            s.setLoadsImagesAutomatically(b.loadsImagesAutomatically);
            s.setJavaScriptEnabled(b.javaScriptEnabled);
            s.setPluginState(b.pluginState);
            s.setJavaScriptCanOpenWindowsAutomatically(
                    b.javaScriptCanOpenWindowsAutomatically);
            s.setDefaultTextEncodingName(b.defaultTextEncodingName);
            s.setMinimumFontSize(b.minimumFontSize);
            s.setMinimumLogicalFontSize(b.minimumLogicalFontSize);
            s.setDefaultFontSize(b.defaultFontSize);
            s.setDefaultFixedFontSize(b.defaultFixedFontSize);
            s.setNavDump(b.navDump);
            s.setTextSize(b.textSize);
            s.setDefaultZoom(b.zoomDensity);
            s.setLightTouchEnabled(b.lightTouch);
            s.setSaveFormData(b.saveFormData);
            s.setSavePassword(b.rememberPasswords);
            s.setLoadWithOverviewMode(b.loadsPageInOverviewMode);
            s.setPageCacheCapacity(pageCacheCapacity);
            s.setNeedInitialFocus(false);
            s.setSupportMultipleWindows(true);
            s.setAppCacheEnabled(b.appCacheEnabled);
            s.setDatabaseEnabled(b.databaseEnabled);
            s.setDomStorageEnabled(b.domStorageEnabled);
            s.setWorkersEnabled(b.workersEnabled);  
            s.setGeolocationEnabled(b.geolocationEnabled);
            s.setAppCacheMaxSize(b.appCacheMaxSize);
            s.setAppCachePath(b.appCachePath);
            s.setDatabasePath(b.databasePath);
            s.setGeolocationDatabasePath(b.geolocationDatabasePath);
            b.updateTabControlSettings();
        }
    }
    public void loadFromDb(final Context ctx) {
        SharedPreferences p =
                PreferenceManager.getDefaultSharedPreferences(ctx);
        appCachePath = ctx.getDir("appcache", 0).getPath();
        webStorageSizeManager = new WebStorageSizeManager(
                ctx,
                new WebStorageSizeManager.StatFsDiskInfo(appCachePath),
                new WebStorageSizeManager.WebKitAppCacheInfo(appCachePath));
        appCacheMaxSize = webStorageSizeManager.getAppCacheMaxSize();
        databasePath = ctx.getDir("databases", 0).getPath();
        geolocationDatabasePath = ctx.getDir("geolocation", 0).getPath();
        if (p.getString(PREF_HOMEPAGE, "") == "") {
            setHomePage(ctx, getFactoryResetHomeUrl(ctx));
        }
        ActivityManager am = (ActivityManager) ctx
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (am.getMemoryClass() > 16) {
            pageCacheCapacity = 5;
        } else {
            pageCacheCapacity = 1;
        }
        syncSharedPreferences(ctx, p);
    }
     void syncSharedPreferences(Context ctx, SharedPreferences p) {
        homeUrl =
            p.getString(PREF_HOMEPAGE, homeUrl);
        String searchEngineName = p.getString(PREF_SEARCH_ENGINE,
                SearchEngine.GOOGLE);
        if (searchEngine == null || !searchEngine.getName().equals(searchEngineName)) {
            if (searchEngine != null) {
                if (searchEngine.supportsVoiceSearch()) {
                    for (int i = 0; i < mTabControl.getTabCount(); i++) {
                        mTabControl.getTab(i).revertVoiceSearchMode();
                    }
                }
                searchEngine.close();
            }
            searchEngine = SearchEngines.get(ctx, searchEngineName);
        }
        Log.i(TAG, "Selected search engine: " + searchEngine);
        loadsImagesAutomatically = p.getBoolean("load_images",
                loadsImagesAutomatically);
        javaScriptEnabled = p.getBoolean("enable_javascript",
                javaScriptEnabled);
        pluginState = WebSettings.PluginState.valueOf(
                p.getString("plugin_state", pluginState.name()));
        javaScriptCanOpenWindowsAutomatically = !p.getBoolean(
            "block_popup_windows",
            !javaScriptCanOpenWindowsAutomatically);
        showSecurityWarnings = p.getBoolean("show_security_warnings",
                showSecurityWarnings);
        rememberPasswords = p.getBoolean("remember_passwords",
                rememberPasswords);
        saveFormData = p.getBoolean("save_formdata",
                saveFormData);
        boolean accept_cookies = p.getBoolean("accept_cookies",
                CookieManager.getInstance().acceptCookie());
        CookieManager.getInstance().setAcceptCookie(accept_cookies);
        openInBackground = p.getBoolean("open_in_background", openInBackground);
        textSize = WebSettings.TextSize.valueOf(
                p.getString(PREF_TEXT_SIZE, textSize.name()));
        zoomDensity = WebSettings.ZoomDensity.valueOf(
                p.getString(PREF_DEFAULT_ZOOM, zoomDensity.name()));
        autoFitPage = p.getBoolean("autofit_pages", autoFitPage);
        loadsPageInOverviewMode = p.getBoolean("load_page",
                loadsPageInOverviewMode);
        boolean landscapeOnlyTemp =
                p.getBoolean("landscape_only", landscapeOnly);
        if (landscapeOnlyTemp != landscapeOnly) {
            landscapeOnly = landscapeOnlyTemp;
        }
        useWideViewPort = true; 
        if (autoFitPage) {
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS;
        } else {
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL;
        }
        defaultTextEncodingName =
                p.getString(PREF_DEFAULT_TEXT_ENCODING,
                        defaultTextEncodingName);
        showDebugSettings =
                p.getBoolean(PREF_DEBUG_SETTINGS, showDebugSettings);
        if (showDebugSettings) {
            boolean small_screen = p.getBoolean("small_screen",
                    layoutAlgorithm ==
                    WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            if (small_screen) {
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
            } else {
                boolean normal_layout = p.getBoolean("normal_layout",
                        layoutAlgorithm == WebSettings.LayoutAlgorithm.NORMAL);
                if (normal_layout) {
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL;
                } else {
                    layoutAlgorithm =
                            WebSettings.LayoutAlgorithm.NARROW_COLUMNS;
                }
            }
            useWideViewPort = p.getBoolean("wide_viewport", useWideViewPort);
            tracing = p.getBoolean("enable_tracing", tracing);
            lightTouch = p.getBoolean("enable_light_touch", lightTouch);
            navDump = p.getBoolean("enable_nav_dump", navDump);
            userAgent = Integer.parseInt(p.getString("user_agent", "0"));
        }
        jsFlags = p.getString("js_engine_flags", "");
        showConsole = p.getBoolean("javascript_console", showConsole);
        appCacheEnabled = p.getBoolean("enable_appcache", appCacheEnabled);
        databaseEnabled = p.getBoolean("enable_database", databaseEnabled);
        domStorageEnabled = p.getBoolean("enable_domstorage", domStorageEnabled);
        geolocationEnabled = p.getBoolean("enable_geolocation", geolocationEnabled);
        workersEnabled = p.getBoolean("enable_workers", workersEnabled);
        update();
    }
    public String getHomePage() {
        return homeUrl;
    }
    public SearchEngine getSearchEngine() {
        return searchEngine;
    }
    public String getJsFlags() {
        return jsFlags;
    }
    public WebStorageSizeManager getWebStorageSizeManager() {
        return webStorageSizeManager;
    }
    public void setHomePage(Context context, String url) {
        Editor ed = PreferenceManager.
                getDefaultSharedPreferences(context).edit();
        ed.putString(PREF_HOMEPAGE, url);
        ed.commit();
        homeUrl = url;
    }
    public WebSettings.TextSize getTextSize() {
        return textSize;
    }
    public WebSettings.ZoomDensity getDefaultZoom() {
        return zoomDensity;
    }
    public boolean openInBackground() {
        return openInBackground;
    }
    public boolean showSecurityWarnings() {
        return showSecurityWarnings;
    }
    public boolean isTracing() {
        return tracing;
    }
    public boolean isLightTouch() {
        return lightTouch;
    }
    public boolean isNavDump() {
        return navDump;
    }
    public boolean showDebugSettings() {
        return showDebugSettings;
    }
    public void toggleDebugSettings() {
        showDebugSettings = !showDebugSettings;
        navDump = showDebugSettings;
        update();
    }
    public Observer addObserver(WebSettings s) {
        Observer old = mWebSettingsToObservers.get(s);
        if (old != null) {
            super.deleteObserver(old);
        }
        Observer o = new Observer(s);
        mWebSettingsToObservers.put(s, o);
        super.addObserver(o);
        return o;
    }
    public void deleteObserver(WebSettings s) {
        Observer o = mWebSettingsToObservers.get(s);
        if (o != null) {
            mWebSettingsToObservers.remove(s);
            super.deleteObserver(o);
        }
    }
     static BrowserSettings getInstance() {
        if (sSingleton == null ) {
            sSingleton = new BrowserSettings();
        }
        return sSingleton;
    }
    void setTabControl(TabControl tabControl) {
        mTabControl = tabControl;
        updateTabControlSettings();
    }
     void update() {
        setChanged();
        notifyObservers();
    }
     void clearCache(Context context) {
        WebIconDatabase.getInstance().removeAllIcons();
        if (mTabControl != null) {
            WebView current = mTabControl.getCurrentWebView();
            if (current != null) {
                current.clearCache(true);
            }
        }
    }
     void clearCookies(Context context) {
        CookieManager.getInstance().removeAllCookie();
    }
    void clearHistory(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Browser.clearHistory(resolver);
        Browser.clearSearches(resolver);
    }
     void clearFormData(Context context) {
        WebViewDatabase.getInstance(context).clearFormData();
        if (mTabControl != null) {
            mTabControl.getCurrentTopWebView().clearFormData();
        }
    }
     void clearPasswords(Context context) {
        WebViewDatabase db = WebViewDatabase.getInstance(context);
        db.clearUsernamePassword();
        db.clearHttpAuthUsernamePassword();
    }
    private void updateTabControlSettings() {
        mTabControl.getBrowserActivity().setShouldShowErrorConsole(
            showDebugSettings && showConsole);
        mTabControl.getBrowserActivity().setRequestedOrientation(
            landscapeOnly ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    private void maybeDisableWebsiteSettings(Context context) {
        PreferenceActivity activity = (PreferenceActivity) context;
        final PreferenceScreen screen = (PreferenceScreen)
            activity.findPreference(BrowserSettings.PREF_WEBSITE_SETTINGS);
        screen.setEnabled(false);
        WebStorage.getInstance().getOrigins(new ValueCallback<Map>() {
            public void onReceiveValue(Map webStorageOrigins) {
                if ((webStorageOrigins != null) && !webStorageOrigins.isEmpty()) {
                    screen.setEnabled(true);
                }
            }
        });
        GeolocationPermissions.getInstance().getOrigins(new ValueCallback<Set<String> >() {
            public void onReceiveValue(Set<String> geolocationOrigins) {
                if ((geolocationOrigins != null) && !geolocationOrigins.isEmpty()) {
                    screen.setEnabled(true);
                }
            }
        });
    }
     void clearDatabases(Context context) {
        WebStorage.getInstance().deleteAllData();
        maybeDisableWebsiteSettings(context);
    }
     void clearLocationAccess(Context context) {
        GeolocationPermissions.getInstance().clearAll();
        maybeDisableWebsiteSettings(context);
    }
     void resetDefaultPreferences(Context ctx) {
        reset();
        SharedPreferences p =
            PreferenceManager.getDefaultSharedPreferences(ctx);
        p.edit().clear().commit();
        PreferenceManager.setDefaultValues(ctx, R.xml.browser_preferences,
                true);
        setHomePage(ctx, getFactoryResetHomeUrl(ctx));
        appCacheMaxSize = webStorageSizeManager.getAppCacheMaxSize();
    }
    private String getFactoryResetHomeUrl(Context context) {
        String url = context.getResources().getString(R.string.homepage_base);
        if (url.indexOf("{CID}") != -1) {
            url = url.replace("{CID}",
                    BrowserProvider.getClientId(context.getContentResolver()));
        }
        return url;
    }
    private BrowserSettings() {
        reset();
    }
    private void reset() {
        loadsImagesAutomatically = true;
        javaScriptEnabled = true;
        pluginState = WebSettings.PluginState.ON;
        javaScriptCanOpenWindowsAutomatically = false;
        showSecurityWarnings = true;
        rememberPasswords = true;
        saveFormData = true;
        openInBackground = false;
        autoFitPage = true;
        landscapeOnly = false;
        loadsPageInOverviewMode = true;
        showDebugSettings = false;
        appCacheEnabled = true;
        databaseEnabled = true;
        domStorageEnabled = true;
        geolocationEnabled = true;
        workersEnabled = true;  
    }
}
