class Tab {
    private static final String LOGTAG = "Tab";
    private static final String CONSOLE_LOGTAG = "browser";
    private GeolocationPermissionsPrompt mGeolocationPermissionsPrompt;
    private View mContainer;
    private WebView mMainView;
    private View mSubViewContainer;
    private WebView mSubView;
    private Bundle mSavedState;
    private PickerData mPickerData;
    private Tab mParentTab;
    private Vector<Tab> mChildTabs;
    private boolean mCloseOnExit;
    private boolean mInForeground;
    private boolean mInLoad;
    private long mLoadStartTime;
    private String mAppId;
    private String mOriginalUrl;
    private ErrorConsoleView mErrorConsole;
    private int mLockIconType;
    private int mPrevLockIconType;
    private final LayoutInflater mInflateService;
    private final BrowserActivity mActivity;
    private final DownloadListener mDownloadListener;
    private final WebBackForwardListClient mWebBackForwardListClient;
    DownloadTouchIcon mTouchIconLoader;
    private static class PickerData {
        String  mUrl;
        String  mTitle;
        Bitmap  mFavicon;
    }
    static final String WEBVIEW = "webview";
    static final String NUMTABS = "numTabs";
    static final String CURRTAB = "currentTab";
    static final String CURRURL = "currentUrl";
    static final String CURRTITLE = "currentTitle";
    static final String CURRPICTURE = "currentPicture";
    static final String CLOSEONEXIT = "closeonexit";
    static final String PARENTTAB = "parentTab";
    static final String APPID = "appid";
    static final String ORIGINALURL = "originalUrl";
    private VoiceSearchData mVoiceSearchData;
    public void revertVoiceSearchMode() {
        if (mVoiceSearchData != null) {
            mVoiceSearchData = null;
            if (mInForeground) {
                mActivity.revertVoiceTitleBar();
            }
        }
    }
    public boolean isInVoiceSearchMode() {
        return mVoiceSearchData != null;
    }
    public boolean voiceSearchSourceIsGoogle() {
        return mVoiceSearchData != null && mVoiceSearchData.mSourceIsGoogle;
    }
    public String getVoiceDisplayTitle() {
        if (mVoiceSearchData == null) return null;
        return mVoiceSearchData.mLastVoiceSearchTitle;
    }
    public ArrayList<String> getVoiceSearchResults() {
        if (mVoiceSearchData == null) return null;
        return mVoiceSearchData.mVoiceSearchResults;
    }
     void activateVoiceSearchMode(Intent intent) {
        int index = 0;
        ArrayList<String> results = intent.getStringArrayListExtra(
                    RecognizerResultsIntent.EXTRA_VOICE_SEARCH_RESULT_STRINGS);
        if (results != null) {
            ArrayList<String> urls = intent.getStringArrayListExtra(
                        RecognizerResultsIntent.EXTRA_VOICE_SEARCH_RESULT_URLS);
            ArrayList<String> htmls = intent.getStringArrayListExtra(
                        RecognizerResultsIntent.EXTRA_VOICE_SEARCH_RESULT_HTML);
            ArrayList<String> baseUrls = intent.getStringArrayListExtra(
                        RecognizerResultsIntent
                        .EXTRA_VOICE_SEARCH_RESULT_HTML_BASE_URLS);
            int size = results.size();
            if (urls == null || size != urls.size()) {
                throw new AssertionError("improper extras passed in Intent");
            }
            if (htmls == null || htmls.size() != size || baseUrls == null ||
                    (baseUrls.size() != size && baseUrls.size() != 1)) {
                htmls = null;
                baseUrls = null;
            }
            mVoiceSearchData = new VoiceSearchData(results, urls, htmls,
                    baseUrls);
            mVoiceSearchData.mHeaders = intent.getParcelableArrayListExtra(
                    RecognizerResultsIntent
                    .EXTRA_VOICE_SEARCH_RESULT_HTTP_HEADERS);
            mVoiceSearchData.mSourceIsGoogle = intent.getBooleanExtra(
                    VoiceSearchData.SOURCE_IS_GOOGLE, false);
            mVoiceSearchData.mVoiceSearchIntent = new Intent(intent);
        }
        String extraData = intent.getStringExtra(
                SearchManager.EXTRA_DATA_KEY);
        if (extraData != null) {
            index = Integer.parseInt(extraData);
            if (index >= mVoiceSearchData.mVoiceSearchResults.size()) {
                throw new AssertionError("index must be less than "
                        + "size of mVoiceSearchResults");
            }
            if (mVoiceSearchData.mSourceIsGoogle) {
                Intent logIntent = new Intent(
                        LoggingEvents.ACTION_LOG_EVENT);
                logIntent.putExtra(LoggingEvents.EXTRA_EVENT,
                        LoggingEvents.VoiceSearch.N_BEST_CHOOSE);
                logIntent.putExtra(
                        LoggingEvents.VoiceSearch.EXTRA_N_BEST_CHOOSE_INDEX,
                        index);
                mActivity.sendBroadcast(logIntent);
            }
            if (mVoiceSearchData.mVoiceSearchIntent != null) {
                Intent latest = new Intent(mVoiceSearchData.mVoiceSearchIntent);
                latest.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
                mVoiceSearchData.mVoiceSearchIntent = latest;
            }
        }
        mVoiceSearchData.mLastVoiceSearchTitle
                = mVoiceSearchData.mVoiceSearchResults.get(index);
        if (mInForeground) {
            mActivity.showVoiceTitleBar(mVoiceSearchData.mLastVoiceSearchTitle);
        }
        if (mVoiceSearchData.mVoiceSearchHtmls != null) {
            String uriString = mVoiceSearchData.mVoiceSearchHtmls.get(index);
            if (uriString != null) {
                Uri dataUri = Uri.parse(uriString);
                if (RecognizerResultsIntent.URI_SCHEME_INLINE.equals(
                        dataUri.getScheme())) {
                    String baseUrl = mVoiceSearchData.mVoiceSearchBaseUrls.get(
                            mVoiceSearchData.mVoiceSearchBaseUrls.size() > 1 ?
                            index : 0);
                    mVoiceSearchData.mLastVoiceSearchUrl = baseUrl;
                    mMainView.loadDataWithBaseURL(baseUrl,
                            uriString.substring(RecognizerResultsIntent
                            .URI_SCHEME_INLINE.length() + 1), "text/html",
                            "utf-8", baseUrl);
                    return;
                }
            }
        }
        mVoiceSearchData.mLastVoiceSearchUrl
                = mVoiceSearchData.mVoiceSearchUrls.get(index);
        if (null == mVoiceSearchData.mLastVoiceSearchUrl) {
            mVoiceSearchData.mLastVoiceSearchUrl = mActivity.smartUrlFilter(
                    mVoiceSearchData.mLastVoiceSearchTitle);
        }
        Map<String, String> headers = null;
        if (mVoiceSearchData.mHeaders != null) {
            int bundleIndex = mVoiceSearchData.mHeaders.size() == 1 ? 0
                    : index;
            Bundle bundle = mVoiceSearchData.mHeaders.get(bundleIndex);
            if (bundle != null && !bundle.isEmpty()) {
                Iterator<String> iter = bundle.keySet().iterator();
                headers = new HashMap<String, String>();
                while (iter.hasNext()) {
                    String key = iter.next();
                    headers.put(key, bundle.getString(key));
                }
            }
        }
        mMainView.loadUrl(mVoiceSearchData.mLastVoiceSearchUrl, headers);
    }
     static class VoiceSearchData {
        public VoiceSearchData(ArrayList<String> results,
                ArrayList<String> urls, ArrayList<String> htmls,
                ArrayList<String> baseUrls) {
            mVoiceSearchResults = results;
            mVoiceSearchUrls = urls;
            mVoiceSearchHtmls = htmls;
            mVoiceSearchBaseUrls = baseUrls;
        }
        public ArrayList<String> mVoiceSearchResults;
        public ArrayList<String> mVoiceSearchUrls;
        public ArrayList<String> mVoiceSearchHtmls;
        public ArrayList<String> mVoiceSearchBaseUrls;
        public String mLastVoiceSearchUrl;
        public String mLastVoiceSearchTitle;
        public boolean mSourceIsGoogle;
        public ArrayList<Bundle> mHeaders;
        public Intent mVoiceSearchIntent;
        public static String SOURCE_IS_GOOGLE
                = "android.speech.extras.SOURCE_IS_GOOGLE";
    }
    private class ErrorDialog {
        public final int mTitle;
        public final String mDescription;
        public final int mError;
        ErrorDialog(int title, String desc, int error) {
            mTitle = title;
            mDescription = desc;
            mError = error;
        }
    };
    private void processNextError() {
        if (mQueuedErrors == null) {
            return;
        }
        mQueuedErrors.removeFirst();
        if (mQueuedErrors.size() == 0) {
            mQueuedErrors = null;
            return;
        }
        showError(mQueuedErrors.getFirst());
    }
    private DialogInterface.OnDismissListener mDialogListener =
            new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface d) {
                    processNextError();
                }
            };
    private LinkedList<ErrorDialog> mQueuedErrors;
    private void queueError(int err, String desc) {
        if (mQueuedErrors == null) {
            mQueuedErrors = new LinkedList<ErrorDialog>();
        }
        for (ErrorDialog d : mQueuedErrors) {
            if (d.mError == err) {
                return;
            }
        }
        ErrorDialog errDialog = new ErrorDialog(
                err == WebViewClient.ERROR_FILE_NOT_FOUND ?
                R.string.browserFrameFileErrorLabel :
                R.string.browserFrameNetworkErrorLabel,
                desc, err);
        mQueuedErrors.addLast(errDialog);
        if (mQueuedErrors.size() == 1 && mInForeground) {
            showError(errDialog);
        }
    }
    private void showError(ErrorDialog errDialog) {
        if (mInForeground) {
            AlertDialog d = new AlertDialog.Builder(mActivity)
                    .setTitle(errDialog.mTitle)
                    .setMessage(errDialog.mDescription)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            d.setOnDismissListener(mDialogListener);
            d.show();
        }
    }
    private final WebViewClient mWebViewClient = new WebViewClient() {
        private Message mDontResend;
        private Message mResend;
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mInLoad = true;
            mLoadStartTime = SystemClock.uptimeMillis();
            if (mVoiceSearchData != null
                    && !url.equals(mVoiceSearchData.mLastVoiceSearchUrl)) {
                if (mVoiceSearchData.mSourceIsGoogle) {
                    Intent i = new Intent(LoggingEvents.ACTION_LOG_EVENT);
                    i.putExtra(LoggingEvents.EXTRA_FLUSH, true);
                    mActivity.sendBroadcast(i);
                }
                revertVoiceSearchMode();
            }
            mActivity.removeMessages(BrowserActivity.UPDATE_BOOKMARK_THUMBNAIL,
                    view);
            if (mTouchIconLoader != null) {
                mTouchIconLoader.mTab = null;
                mTouchIconLoader = null;
            }
            if (mErrorConsole != null) {
                mErrorConsole.clearErrorMessages();
                if (mActivity.shouldShowErrorConsole()) {
                    mErrorConsole.showConsole(ErrorConsoleView.SHOW_NONE);
                }
            }
            if (favicon != null) {
                BrowserBookmarksAdapter.updateBookmarkFavicon(mActivity
                        .getContentResolver(), null, url, favicon);
            }
            CookieSyncManager.getInstance().resetSync();
            if (!mActivity.isNetworkUp()) {
                view.setNetworkAvailable(false);
            }
            if (mInForeground) {
                mActivity.onPageStarted(view, url, favicon);
            }
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            LogTag.logPageFinishedLoading(
                    url, SystemClock.uptimeMillis() - mLoadStartTime);
            mInLoad = false;
            if (mInForeground && !mActivity.didUserStopLoading()
                    || !mInForeground) {
                mActivity.postMessage(
                        BrowserActivity.UPDATE_BOOKMARK_THUMBNAIL, 0, 0, view,
                        500);
            }
            if (mInForeground) {
                mActivity.onPageFinished(view, url);
            }
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mInForeground) {
                return mActivity.shouldOverrideUrlLoading(view, url);
            } else {
                return false;
            }
        }
        @Override
        public void onLoadResource(WebView view, String url) {
            if (url != null && url.length() > 0) {
                if (mLockIconType == BrowserActivity.LOCK_ICON_SECURE) {
                    if (!(URLUtil.isHttpsUrl(url) || URLUtil.isDataUrl(url)
                            || URLUtil.isAboutUrl(url))) {
                        mLockIconType = BrowserActivity.LOCK_ICON_MIXED;
                    }
                }
            }
        }
        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            if (errorCode != WebViewClient.ERROR_HOST_LOOKUP &&
                    errorCode != WebViewClient.ERROR_CONNECT &&
                    errorCode != WebViewClient.ERROR_BAD_URL &&
                    errorCode != WebViewClient.ERROR_UNSUPPORTED_SCHEME &&
                    errorCode != WebViewClient.ERROR_FILE) {
                queueError(errorCode, description);
            }
            Log.e(LOGTAG, "onReceivedError " + errorCode + " " + failingUrl
                    + " " + description);
            if (mInForeground) {
                mActivity.resetTitleAndRevertLockIcon();
            }
        }
        @Override
        public void onFormResubmission(WebView view, final Message dontResend,
                                       final Message resend) {
            if (!mInForeground) {
                dontResend.sendToTarget();
                return;
            }
            if (mDontResend != null) {
                Log.w(LOGTAG, "onFormResubmission should not be called again "
                        + "while dialog is still up");
                dontResend.sendToTarget();
                return;
            }
            mDontResend = dontResend;
            mResend = resend;
            new AlertDialog.Builder(mActivity).setTitle(
                    R.string.browserFrameFormResubmitLabel).setMessage(
                    R.string.browserFrameFormResubmitMessage)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    if (mResend != null) {
                                        mResend.sendToTarget();
                                        mResend = null;
                                        mDontResend = null;
                                    }
                                }
                            }).setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    if (mDontResend != null) {
                                        mDontResend.sendToTarget();
                                        mResend = null;
                                        mDontResend = null;
                                    }
                                }
                            }).setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            if (mDontResend != null) {
                                mDontResend.sendToTarget();
                                mResend = null;
                                mDontResend = null;
                            }
                        }
                    }).show();
        }
        @Override
        public void doUpdateVisitedHistory(WebView view, String url,
                boolean isReload) {
            if (url.regionMatches(true, 0, "about:", 0, 6)) {
                return;
            }
            int index = url.indexOf("client=ms-");
            if (index > 0 && url.contains(".google.")) {
                int end = url.indexOf('&', index);
                if (end > 0) {
                    url = url.substring(0, index)
                            .concat(url.substring(end + 1));
                } else {
                    url = url.substring(0, index-1);
                }
            }
            final ContentResolver cr = mActivity.getContentResolver();
            final String newUrl = url;
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... unused) {
                    Browser.updateVisitedHistory(cr, newUrl, true);
                    return null;
                }
            }.execute();
            WebIconDatabase.getInstance().retainIconForPageUrl(url);
        }
        @Override
        public void onReceivedSslError(final WebView view,
                final SslErrorHandler handler, final SslError error) {
            if (!mInForeground) {
                handler.cancel();
                return;
            }
            if (BrowserSettings.getInstance().showSecurityWarnings()) {
                final LayoutInflater factory =
                    LayoutInflater.from(mActivity);
                final View warningsView =
                    factory.inflate(R.layout.ssl_warnings, null);
                final LinearLayout placeholder =
                    (LinearLayout)warningsView.findViewById(R.id.placeholder);
                if (error.hasError(SslError.SSL_UNTRUSTED)) {
                    LinearLayout ll = (LinearLayout)factory
                        .inflate(R.layout.ssl_warning, null);
                    ((TextView)ll.findViewById(R.id.warning))
                        .setText(R.string.ssl_untrusted);
                    placeholder.addView(ll);
                }
                if (error.hasError(SslError.SSL_IDMISMATCH)) {
                    LinearLayout ll = (LinearLayout)factory
                        .inflate(R.layout.ssl_warning, null);
                    ((TextView)ll.findViewById(R.id.warning))
                        .setText(R.string.ssl_mismatch);
                    placeholder.addView(ll);
                }
                if (error.hasError(SslError.SSL_EXPIRED)) {
                    LinearLayout ll = (LinearLayout)factory
                        .inflate(R.layout.ssl_warning, null);
                    ((TextView)ll.findViewById(R.id.warning))
                        .setText(R.string.ssl_expired);
                    placeholder.addView(ll);
                }
                if (error.hasError(SslError.SSL_NOTYETVALID)) {
                    LinearLayout ll = (LinearLayout)factory
                        .inflate(R.layout.ssl_warning, null);
                    ((TextView)ll.findViewById(R.id.warning))
                        .setText(R.string.ssl_not_yet_valid);
                    placeholder.addView(ll);
                }
                new AlertDialog.Builder(mActivity).setTitle(
                        R.string.security_warning).setIcon(
                        android.R.drawable.ic_dialog_alert).setView(
                        warningsView).setPositiveButton(R.string.ssl_continue,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                handler.proceed();
                            }
                        }).setNeutralButton(R.string.view_certificate,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                mActivity.showSSLCertificateOnError(view,
                                        handler, error);
                            }
                        }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                handler.cancel();
                                mActivity.resetTitleAndRevertLockIcon();
                            }
                        }).setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                handler.cancel();
                                mActivity.resetTitleAndRevertLockIcon();
                            }
                        }).show();
            } else {
                handler.proceed();
            }
        }
        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                final HttpAuthHandler handler, final String host,
                final String realm) {
            String username = null;
            String password = null;
            boolean reuseHttpAuthUsernamePassword = handler
                    .useHttpAuthUsernamePassword();
            if (reuseHttpAuthUsernamePassword && view != null) {
                String[] credentials = view.getHttpAuthUsernamePassword(
                        host, realm);
                if (credentials != null && credentials.length == 2) {
                    username = credentials[0];
                    password = credentials[1];
                }
            }
            if (username != null && password != null) {
                handler.proceed(username, password);
            } else {
                if (mInForeground) {
                    mActivity.showHttpAuthentication(handler, host, realm,
                            null, null, null, 0);
                } else {
                    handler.cancel();
                }
            }
        }
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            if (!mInForeground) {
                return false;
            }
            if (mActivity.isMenuDown()) {
                return mActivity.getWindow().isShortcutKey(event.getKeyCode(),
                        event);
            } else {
                return false;
            }
        }
        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            if (!mInForeground || mActivity.mActivityInPause) {
                return;
            }
            if (event.isDown()) {
                mActivity.onKeyDown(event.getKeyCode(), event);
            } else {
                mActivity.onKeyUp(event.getKeyCode(), event);
            }
        }
    };
    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        private void createWindow(final boolean dialog, final Message msg) {
            WebView.WebViewTransport transport =
                    (WebView.WebViewTransport) msg.obj;
            if (dialog) {
                createSubWindow();
                mActivity.attachSubWindow(Tab.this);
                transport.setWebView(mSubView);
            } else {
                final Tab newTab = mActivity.openTabAndShow(
                        BrowserActivity.EMPTY_URL_DATA, false, null);
                if (newTab != Tab.this) {
                    Tab.this.addChildTab(newTab);
                }
                transport.setWebView(newTab.getWebView());
            }
            msg.sendToTarget();
        }
        @Override
        public boolean onCreateWindow(WebView view, final boolean dialog,
                final boolean userGesture, final Message resultMsg) {
            if (!mInForeground) {
                return false;
            }
            if (dialog && mSubView != null) {
                new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.too_many_subwindows_dialog_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.too_many_subwindows_dialog_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return false;
            } else if (!mActivity.getTabControl().canCreateNewTab()) {
                new AlertDialog.Builder(mActivity)
                        .setTitle(R.string.too_many_windows_dialog_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.too_many_windows_dialog_message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return false;
            }
            if (userGesture) {
                createWindow(dialog, resultMsg);
                return true;
            }
            final AlertDialog.OnClickListener allowListener =
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface d,
                                int which) {
                            createWindow(dialog, resultMsg);
                        }
                    };
            final AlertDialog.OnClickListener blockListener =
                    new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface d, int which) {
                            resultMsg.sendToTarget();
                        }
                    };
            final AlertDialog d =
                    new AlertDialog.Builder(mActivity)
                    .setTitle(R.string.attention)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.popup_window_attempt)
                    .setPositiveButton(R.string.allow, allowListener)
                    .setNegativeButton(R.string.block, blockListener)
                    .setCancelable(false)
                    .create();
            d.show();
            return true;
        }
        @Override
        public void onRequestFocus(WebView view) {
            if (!mInForeground) {
                mActivity.switchToTab(mActivity.getTabControl().getTabIndex(
                        Tab.this));
            }
        }
        @Override
        public void onCloseWindow(WebView window) {
            if (mParentTab != null) {
                if (mInForeground) {
                    mActivity.switchToTab(mActivity.getTabControl()
                            .getTabIndex(mParentTab));
                }
                mActivity.closeTab(Tab.this);
            }
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                CookieSyncManager.getInstance().sync();
            }
            if (mInForeground) {
                mActivity.onProgressChanged(view, newProgress);
            }
        }
        @Override
        public void onReceivedTitle(WebView view, final String title) {
            final String pageUrl = view.getUrl();
            if (mInForeground) {
                mActivity.setUrlTitle(pageUrl, title);
            }
            if (pageUrl == null || pageUrl.length()
                    >= SQLiteDatabase.SQLITE_MAX_LIKE_PATTERN_LENGTH) {
                return;
            }
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... unused) {
                    String url = pageUrl;
                    if (url.startsWith("http:
                        url = url.substring(11);
                    } else if (url.startsWith("http:
                        url = url.substring(4);
                    }
                    Cursor c = null;
                    try {
                        final ContentResolver cr
                                = mActivity.getContentResolver();
                        url = "%" + url;
                        String [] selArgs = new String[] { url };
                        String where = Browser.BookmarkColumns.URL
                                + " LIKE ? AND "
                                + Browser.BookmarkColumns.BOOKMARK + " = 0";
                        c = cr.query(Browser.BOOKMARKS_URI, new String[]
                                { Browser.BookmarkColumns._ID }, where, selArgs,
                                null);
                        if (c.moveToFirst()) {
                            ContentValues map = new ContentValues();
                            map.put(Browser.BookmarkColumns.TITLE, title);
                            String[] projection = new String[]
                                    { Integer.valueOf(c.getInt(0)).toString() };
                            cr.update(Browser.BOOKMARKS_URI, map, "_id = ?",
                                    projection);
                        }
                    } catch (IllegalStateException e) {
                        Log.e(LOGTAG, "Tab onReceived title", e);
                    } catch (SQLiteException ex) {
                        Log.e(LOGTAG,
                                "onReceivedTitle() caught SQLiteException: ",
                                ex);
                    } finally {
                        if (c != null) c.close();
                    }
                    return null;
                }
            }.execute();
        }
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (icon != null) {
                BrowserBookmarksAdapter.updateBookmarkFavicon(mActivity
                        .getContentResolver(), view.getOriginalUrl(), view
                        .getUrl(), icon);
            }
            if (mInForeground) {
                mActivity.setFavicon(icon);
            }
        }
        @Override
        public void onReceivedTouchIconUrl(WebView view, String url,
                boolean precomposed) {
            final ContentResolver cr = mActivity.getContentResolver();
            if (precomposed && mTouchIconLoader != null) {
                mTouchIconLoader.cancel(false);
                mTouchIconLoader = null;
            }
            if (mTouchIconLoader == null) {
                mTouchIconLoader = new DownloadTouchIcon(Tab.this, cr, view);
                mTouchIconLoader.execute(url);
            }
        }
        @Override
        public void onShowCustomView(View view,
                WebChromeClient.CustomViewCallback callback) {
            if (mInForeground) mActivity.onShowCustomView(view, callback);
        }
        @Override
        public void onHideCustomView() {
            if (mInForeground) mActivity.onHideCustomView();
        }
        @Override
        public void onExceededDatabaseQuota(String url,
            String databaseIdentifier, long currentQuota, long estimatedSize,
            long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            BrowserSettings.getInstance().getWebStorageSizeManager()
                    .onExceededDatabaseQuota(url, databaseIdentifier,
                            currentQuota, estimatedSize, totalUsedQuota,
                            quotaUpdater);
        }
        @Override
        public void onReachedMaxAppCacheSize(long spaceNeeded,
                long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
            BrowserSettings.getInstance().getWebStorageSizeManager()
                    .onReachedMaxAppCacheSize(spaceNeeded, totalUsedQuota,
                            quotaUpdater);
        }
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                GeolocationPermissions.Callback callback) {
            if (mInForeground) {
                getGeolocationPermissionsPrompt().show(origin, callback);
            }
        }
        @Override
        public void onGeolocationPermissionsHidePrompt() {
            if (mInForeground && mGeolocationPermissionsPrompt != null) {
                mGeolocationPermissionsPrompt.hide();
            }
        }
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (mInForeground) {
                ErrorConsoleView errorConsole = getErrorConsole(true);
                errorConsole.addErrorMessage(consoleMessage);
                if (mActivity.shouldShowErrorConsole()
                        && errorConsole.getShowState() != ErrorConsoleView.SHOW_MAXIMIZED) {
                    errorConsole.showConsole(ErrorConsoleView.SHOW_MINIMIZED);
                }
            }
            String message = "Console: " + consoleMessage.message() + " "
                    + consoleMessage.sourceId() +  ":"
                    + consoleMessage.lineNumber();
            switch (consoleMessage.messageLevel()) {
                case TIP:
                    Log.v(CONSOLE_LOGTAG, message);
                    break;
                case LOG:
                    Log.i(CONSOLE_LOGTAG, message);
                    break;
                case WARNING:
                    Log.w(CONSOLE_LOGTAG, message);
                    break;
                case ERROR:
                    Log.e(CONSOLE_LOGTAG, message);
                    break;
                case DEBUG:
                    Log.d(CONSOLE_LOGTAG, message);
                    break;
            }
            return true;
        }
        @Override
        public Bitmap getDefaultVideoPoster() {
            if (mInForeground) {
                return mActivity.getDefaultVideoPoster();
            }
            return null;
        }
        @Override
        public View getVideoLoadingProgressView() {
            if (mInForeground) {
                return mActivity.getVideoLoadingProgressView();
            }
            return null;
        }
        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            if (mInForeground) {
                mActivity.openFileChooser(uploadMsg);
            } else {
                uploadMsg.onReceiveValue(null);
            }
        }
        @Override
        public void getVisitedHistory(final ValueCallback<String[]> callback) {
            AsyncTask<Void, Void, String[]> task = new AsyncTask<Void, Void, String[]>() {
                public String[] doInBackground(Void... unused) {
                    return Browser.getVisitedHistory(mActivity
                            .getContentResolver());
                }
                public void onPostExecute(String[] result) {
                    callback.onReceiveValue(result);
                };
            };
            task.execute();
        };
    };
    private static class SubWindowClient extends WebViewClient {
        private final WebViewClient mClient;
        SubWindowClient(WebViewClient client) {
            mClient = client;
        }
        @Override
        public void doUpdateVisitedHistory(WebView view, String url,
                boolean isReload) {
            mClient.doUpdateVisitedHistory(view, url, isReload);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return mClient.shouldOverrideUrlLoading(view, url);
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                SslError error) {
            mClient.onReceivedSslError(view, handler, error);
        }
        @Override
        public void onReceivedHttpAuthRequest(WebView view,
                HttpAuthHandler handler, String host, String realm) {
            mClient.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
        @Override
        public void onFormResubmission(WebView view, Message dontResend,
                Message resend) {
            mClient.onFormResubmission(view, dontResend, resend);
        }
        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            mClient.onReceivedError(view, errorCode, description, failingUrl);
        }
        @Override
        public boolean shouldOverrideKeyEvent(WebView view,
                android.view.KeyEvent event) {
            return mClient.shouldOverrideKeyEvent(view, event);
        }
        @Override
        public void onUnhandledKeyEvent(WebView view,
                android.view.KeyEvent event) {
            mClient.onUnhandledKeyEvent(view, event);
        }
    }
    private class SubWindowChromeClient extends WebChromeClient {
        private final WebChromeClient mClient;
        SubWindowChromeClient(WebChromeClient client) {
            mClient = client;
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mClient.onProgressChanged(view, newProgress);
        }
        @Override
        public boolean onCreateWindow(WebView view, boolean dialog,
                boolean userGesture, android.os.Message resultMsg) {
            return mClient.onCreateWindow(view, dialog, userGesture, resultMsg);
        }
        @Override
        public void onCloseWindow(WebView window) {
            if (window != mSubView) {
                Log.e(LOGTAG, "Can't close the window");
            }
            mActivity.dismissSubWindow(Tab.this);
        }
    }
    Tab(BrowserActivity activity, WebView w, boolean closeOnExit, String appId,
            String url) {
        mActivity = activity;
        mCloseOnExit = closeOnExit;
        mAppId = appId;
        mOriginalUrl = url;
        mLockIconType = BrowserActivity.LOCK_ICON_UNSECURE;
        mPrevLockIconType = BrowserActivity.LOCK_ICON_UNSECURE;
        mInLoad = false;
        mInForeground = false;
        mInflateService = LayoutInflater.from(activity);
        mContainer = mInflateService.inflate(R.layout.tab, null);
        mDownloadListener = new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                    String contentDisposition, String mimetype,
                    long contentLength) {
                mActivity.onDownloadStart(url, userAgent, contentDisposition,
                        mimetype, contentLength);
                if (mMainView.copyBackForwardList().getSize() == 0) {
                    if (mActivity.getTabControl().getCurrentWebView()
                            == mMainView) {
                        mActivity.goBackOnePageOrQuit();
                    } else {
                        mActivity.closeTab(Tab.this);
                    }
                }
            }
        };
        mWebBackForwardListClient = new WebBackForwardListClient() {
            @Override
            public void onNewHistoryItem(WebHistoryItem item) {
                if (isInVoiceSearchMode()) {
                    item.setCustomData(mVoiceSearchData.mVoiceSearchIntent);
                }
            }
            @Override
            public void onIndexChanged(WebHistoryItem item, int index) {
                Object data = item.getCustomData();
                if (data != null && data instanceof Intent) {
                    activateVoiceSearchMode((Intent) data);
                }
            }
        };
        setWebView(w);
    }
    void setWebView(WebView w) {
        if (mMainView == w) {
            return;
        }
        if (mGeolocationPermissionsPrompt != null) {
            mGeolocationPermissionsPrompt.hide();
        }
        FrameLayout wrapper =
                (FrameLayout) mContainer.findViewById(R.id.webview_wrapper);
        wrapper.removeView(mMainView);
        mMainView = w;
        if (mMainView != null) {
            mMainView.setWebViewClient(mWebViewClient);
            mMainView.setWebChromeClient(mWebChromeClient);
            mMainView.setDownloadListener(mDownloadListener);
            mMainView.setWebBackForwardListClient(mWebBackForwardListClient);
        }
    }
    void destroy() {
        if (mMainView != null) {
            dismissSubWindow();
            BrowserSettings.getInstance().deleteObserver(mMainView.getSettings());
            WebView webView = mMainView;
            setWebView(null);
            webView.destroy();
        }
    }
    void removeFromTree() {
        if (mChildTabs != null) {
            for(Tab t : mChildTabs) {
                t.setParentTab(null);
            }
        }
        if (mParentTab != null) {
            mParentTab.mChildTabs.remove(this);
        }
    }
    boolean createSubWindow() {
        if (mSubView == null) {
            mSubViewContainer = mInflateService.inflate(
                    R.layout.browser_subwindow, null);
            mSubView = (WebView) mSubViewContainer.findViewById(R.id.webview);
            mSubView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            mSubView.setMapTrackballToArrowKeys(false);
            mSubView.getSettings().setBuiltInZoomControls(true);
            mSubView.setWebViewClient(new SubWindowClient(mWebViewClient));
            mSubView.setWebChromeClient(new SubWindowChromeClient(
                    mWebChromeClient));
            mSubView.setDownloadListener(new DownloadListener() {
                public void onDownloadStart(String url, String userAgent,
                        String contentDisposition, String mimetype,
                        long contentLength) {
                    mActivity.onDownloadStart(url, userAgent,
                            contentDisposition, mimetype, contentLength);
                    if (mSubView.copyBackForwardList().getSize() == 0) {
                        dismissSubWindow();
                    }
                }
            });
            mSubView.setOnCreateContextMenuListener(mActivity);
            final BrowserSettings s = BrowserSettings.getInstance();
            s.addObserver(mSubView.getSettings()).update(s, null);
            final ImageButton cancel = (ImageButton) mSubViewContainer
                    .findViewById(R.id.subwindow_close);
            cancel.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mSubView.getWebChromeClient().onCloseWindow(mSubView);
                }
            });
            return true;
        }
        return false;
    }
    void dismissSubWindow() {
        if (mSubView != null) {
            BrowserSettings.getInstance().deleteObserver(
                    mSubView.getSettings());
            mSubView.destroy();
            mSubView = null;
            mSubViewContainer = null;
        }
    }
    void attachSubWindow(ViewGroup content) {
        if (mSubView != null) {
            content.addView(mSubViewContainer,
                    BrowserActivity.COVER_SCREEN_PARAMS);
        }
    }
    void removeSubWindow(ViewGroup content) {
        if (mSubView != null) {
            content.removeView(mSubViewContainer);
        }
    }
    void attachTabToContentView(ViewGroup content) {
        if (mMainView == null) {
            return;
        }
        FrameLayout wrapper =
                (FrameLayout) mContainer.findViewById(R.id.webview_wrapper);
        ViewGroup parent = (ViewGroup) mMainView.getParent();
        if (parent != wrapper) {
            if (parent != null) {
                Log.w(LOGTAG, "mMainView already has a parent in"
                        + " attachTabToContentView!");
                parent.removeView(mMainView);
            }
            wrapper.addView(mMainView);
        } else {
            Log.w(LOGTAG, "mMainView is already attached to wrapper in"
                    + " attachTabToContentView!");
        }
        parent = (ViewGroup) mContainer.getParent();
        if (parent != content) {
            if (parent != null) {
                Log.w(LOGTAG, "mContainer already has a parent in"
                        + " attachTabToContentView!");
                parent.removeView(mContainer);
            }
            content.addView(mContainer, BrowserActivity.COVER_SCREEN_PARAMS);
        } else {
            Log.w(LOGTAG, "mContainer is already attached to content in"
                    + " attachTabToContentView!");
        }
        attachSubWindow(content);
    }
    void removeTabFromContentView(ViewGroup content) {
        if (mMainView == null) {
            return;
        }
        FrameLayout wrapper =
                (FrameLayout) mContainer.findViewById(R.id.webview_wrapper);
        wrapper.removeView(mMainView);
        content.removeView(mContainer);
        removeSubWindow(content);
    }
    void setParentTab(Tab parent) {
        mParentTab = parent;
        if (mSavedState != null) {
            if (parent == null) {
                mSavedState.remove(PARENTTAB);
            } else {
                mSavedState.putInt(PARENTTAB, mActivity.getTabControl()
                        .getTabIndex(parent));
            }
        }
    }
    void addChildTab(Tab child) {
        if (mChildTabs == null) {
            mChildTabs = new Vector<Tab>();
        }
        mChildTabs.add(child);
        child.setParentTab(this);
    }
    Vector<Tab> getChildTabs() {
        return mChildTabs;
    }
    void resume() {
        if (mMainView != null) {
            mMainView.onResume();
            if (mSubView != null) {
                mSubView.onResume();
            }
        }
    }
    void pause() {
        if (mMainView != null) {
            mMainView.onPause();
            if (mSubView != null) {
                mSubView.onPause();
            }
        }
    }
    void putInForeground() {
        mInForeground = true;
        resume();
        mMainView.setOnCreateContextMenuListener(mActivity);
        if (mSubView != null) {
            mSubView.setOnCreateContextMenuListener(mActivity);
        }
        if (mQueuedErrors != null && mQueuedErrors.size() >  0) {
            showError(mQueuedErrors.getFirst());
        }
    }
    void putInBackground() {
        mInForeground = false;
        pause();
        mMainView.setOnCreateContextMenuListener(null);
        if (mSubView != null) {
            mSubView.setOnCreateContextMenuListener(null);
        }
    }
    WebView getTopWindow() {
        if (mSubView != null) {
            return mSubView;
        }
        return mMainView;
    }
    WebView getWebView() {
        return mMainView;
    }
    WebView getSubWebView() {
        return mSubView;
    }
    GeolocationPermissionsPrompt getGeolocationPermissionsPrompt() {
        if (mGeolocationPermissionsPrompt == null) {
            ViewStub stub = (ViewStub) mContainer
                    .findViewById(R.id.geolocation_permissions_prompt);
            mGeolocationPermissionsPrompt = (GeolocationPermissionsPrompt) stub
                    .inflate();
            mGeolocationPermissionsPrompt.init();
        }
        return mGeolocationPermissionsPrompt;
    }
    String getAppId() {
        return mAppId;
    }
    void setAppId(String id) {
        mAppId = id;
    }
    String getOriginalUrl() {
        return mOriginalUrl;
    }
    void setOriginalUrl(String url) {
        mOriginalUrl = url;
    }
    String getUrl() {
        if (mPickerData != null) {
            return mPickerData.mUrl;
        }
        return null;
    }
    String getTitle() {
        if (mPickerData != null) {
            return mPickerData.mTitle;
        }
        return null;
    }
    Bitmap getFavicon() {
        if (mPickerData != null) {
            return mPickerData.mFavicon;
        }
        return null;
    }
    ErrorConsoleView getErrorConsole(boolean createIfNecessary) {
        if (createIfNecessary && mErrorConsole == null) {
            mErrorConsole = new ErrorConsoleView(mActivity);
            mErrorConsole.setWebView(mMainView);
        }
        return mErrorConsole;
    }
    public Tab getParentTab() {
        return mParentTab;
    }
    boolean closeOnExit() {
        return mCloseOnExit;
    }
    void resetLockIcon(String url) {
        mPrevLockIconType = mLockIconType;
        mLockIconType = BrowserActivity.LOCK_ICON_UNSECURE;
        if (URLUtil.isHttpsUrl(url)) {
            mLockIconType = BrowserActivity.LOCK_ICON_SECURE;
        }
    }
    void revertLockIcon() {
        mLockIconType = mPrevLockIconType;
    }
    int getLockIconType() {
        return mLockIconType;
    }
    boolean inLoad() {
        return mInLoad;
    }
    void clearInLoad() {
        mInLoad = false;
    }
    void populatePickerData() {
        if (mMainView == null) {
            populatePickerDataFromSavedState();
            return;
        }
        final WebBackForwardList list = mMainView.copyBackForwardList();
        final WebHistoryItem item = list != null ? list.getCurrentItem() : null;
        populatePickerData(item);
    }
    private void populatePickerData(WebHistoryItem item) {
        mPickerData = new PickerData();
        if (item != null) {
            mPickerData.mUrl = item.getUrl();
            mPickerData.mTitle = item.getTitle();
            mPickerData.mFavicon = item.getFavicon();
            if (mPickerData.mTitle == null) {
                mPickerData.mTitle = mPickerData.mUrl;
            }
        }
    }
    void populatePickerDataFromSavedState() {
        if (mSavedState == null) {
            return;
        }
        mPickerData = new PickerData();
        mPickerData.mUrl = mSavedState.getString(CURRURL);
        mPickerData.mTitle = mSavedState.getString(CURRTITLE);
    }
    void clearPickerData() {
        mPickerData = null;
    }
    Bundle getSavedState() {
        return mSavedState;
    }
    void setSavedState(Bundle state) {
        mSavedState = state;
    }
    boolean saveState() {
        if (mMainView == null) {
            return mSavedState != null;
        }
        mSavedState = new Bundle();
        final WebBackForwardList list = mMainView.saveState(mSavedState);
        if (list != null) {
            final File f = new File(mActivity.getTabControl().getThumbnailDir(),
                    mMainView.hashCode() + "_pic.save");
            if (mMainView.savePicture(mSavedState, f)) {
                mSavedState.putString(CURRPICTURE, f.getPath());
            } else {
                f.delete();
            }
        }
        final WebHistoryItem item = list != null ? list.getCurrentItem() : null;
        populatePickerData(item);
        if (mPickerData.mUrl != null) {
            mSavedState.putString(CURRURL, mPickerData.mUrl);
        }
        if (mPickerData.mTitle != null) {
            mSavedState.putString(CURRTITLE, mPickerData.mTitle);
        }
        mSavedState.putBoolean(CLOSEONEXIT, mCloseOnExit);
        if (mAppId != null) {
            mSavedState.putString(APPID, mAppId);
        }
        if (mOriginalUrl != null) {
            mSavedState.putString(ORIGINALURL, mOriginalUrl);
        }
        if (mParentTab != null) {
            mSavedState.putInt(PARENTTAB, mActivity.getTabControl().getTabIndex(
                    mParentTab));
        }
        return true;
    }
    boolean restoreState(Bundle b) {
        if (b == null) {
            return false;
        }
        mSavedState = null;
        mPickerData = null;
        mCloseOnExit = b.getBoolean(CLOSEONEXIT);
        mAppId = b.getString(APPID);
        mOriginalUrl = b.getString(ORIGINALURL);
        final WebBackForwardList list = mMainView.restoreState(b);
        if (list == null) {
            return false;
        }
        if (b.containsKey(CURRPICTURE)) {
            final File f = new File(b.getString(CURRPICTURE));
            mMainView.restorePicture(b, f);
            f.delete();
        }
        return true;
    }
}
