public class Activity extends ContextThemeWrapper
        implements LayoutInflater.Factory,
        Window.Callback, KeyEvent.Callback,
        OnCreateContextMenuListener, ComponentCallbacks {
    private static final String TAG = "Activity";
    public static final int RESULT_CANCELED    = 0;
    public static final int RESULT_OK           = -1;
    public static final int RESULT_FIRST_USER   = 1;
    private static long sInstanceCount = 0;
    private static final String WINDOW_HIERARCHY_TAG = "android:viewHierarchyState";
    private static final String SAVED_DIALOG_IDS_KEY = "android:savedDialogIds";
    private static final String SAVED_DIALOGS_TAG = "android:savedDialogs";
    private static final String SAVED_DIALOG_KEY_PREFIX = "android:dialog_";
    private static final String SAVED_DIALOG_ARGS_KEY_PREFIX = "android:dialog_args_";
    private static class ManagedDialog {
        Dialog mDialog;
        Bundle mArgs;
    }
    private SparseArray<ManagedDialog> mManagedDialogs;
    private Instrumentation mInstrumentation;
    private IBinder mToken;
    private int mIdent;
     String mEmbeddedID;
    private Application mApplication;
     Intent mIntent;
    private ComponentName mComponent;
     ActivityInfo mActivityInfo;
     ActivityThread mMainThread;
     Object mLastNonConfigurationInstance;
     HashMap<String,Object> mLastNonConfigurationChildInstances;
    Activity mParent;
    boolean mCalled;
    private boolean mResumed;
    private boolean mStopped;
    boolean mFinished;
    boolean mStartedActivity;
     int mConfigChangeFlags;
     Configuration mCurrentConfig;
    private SearchManager mSearchManager;
    private Window mWindow;
    private WindowManager mWindowManager;
     View mDecor = null;
     boolean mWindowAdded = false;
     boolean mVisibleFromServer = false;
     boolean mVisibleFromClient = true;
    private CharSequence mTitle;
    private int mTitleColor = 0;
    private static final class ManagedCursor {
        ManagedCursor(Cursor cursor) {
            mCursor = cursor;
            mReleased = false;
            mUpdated = false;
        }
        private final Cursor mCursor;
        private boolean mReleased;
        private boolean mUpdated;
    }
    private final ArrayList<ManagedCursor> mManagedCursors =
        new ArrayList<ManagedCursor>();
    int mResultCode = RESULT_CANCELED;
    Intent mResultData = null;
    private boolean mTitleReady = false;
    private int mDefaultKeyMode = DEFAULT_KEYS_DISABLE;
    private SpannableStringBuilder mDefaultKeySsb = null;
    protected static final int[] FOCUSED_STATE_SET = {com.android.internal.R.attr.state_focused};
    private Thread mUiThread;
    private final Handler mHandler = new Handler();
    public static long getInstanceCount() {
        return sInstanceCount;
    }
    public Intent getIntent() {
        return mIntent;
    }
    public void setIntent(Intent newIntent) {
        mIntent = newIntent;
    }
    public final Application getApplication() {
        return mApplication;
    }
    public final boolean isChild() {
        return mParent != null;
    }
    public final Activity getParent() {
        return mParent;
    }
    public WindowManager getWindowManager() {
        return mWindowManager;
    }
    public Window getWindow() {
        return mWindow;
    }
    public View getCurrentFocus() {
        return mWindow != null ? mWindow.getCurrentFocus() : null;
    }
    @Override
    public int getWallpaperDesiredMinimumWidth() {
        int width = super.getWallpaperDesiredMinimumWidth();
        return width <= 0 ? getWindowManager().getDefaultDisplay().getWidth() : width;
    }
    @Override
    public int getWallpaperDesiredMinimumHeight() {
        int height = super.getWallpaperDesiredMinimumHeight();
        return height <= 0 ? getWindowManager().getDefaultDisplay().getHeight() : height;
    }
    protected void onCreate(Bundle savedInstanceState) {
        mVisibleFromClient = !mWindow.getWindowStyle().getBoolean(
                com.android.internal.R.styleable.Window_windowNoDisplay, false);
        mCalled = true;
    }
    final void performRestoreInstanceState(Bundle savedInstanceState) {
        onRestoreInstanceState(savedInstanceState);
        restoreManagedDialogs(savedInstanceState);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mWindow != null) {
            Bundle windowState = savedInstanceState.getBundle(WINDOW_HIERARCHY_TAG);
            if (windowState != null) {
                mWindow.restoreHierarchyState(windowState);
            }
        }
    }
    private void restoreManagedDialogs(Bundle savedInstanceState) {
        final Bundle b = savedInstanceState.getBundle(SAVED_DIALOGS_TAG);
        if (b == null) {
            return;
        }
        final int[] ids = b.getIntArray(SAVED_DIALOG_IDS_KEY);
        final int numDialogs = ids.length;
        mManagedDialogs = new SparseArray<ManagedDialog>(numDialogs);
        for (int i = 0; i < numDialogs; i++) {
            final Integer dialogId = ids[i];
            Bundle dialogState = b.getBundle(savedDialogKeyFor(dialogId));
            if (dialogState != null) {
                final ManagedDialog md = new ManagedDialog();
                md.mArgs = b.getBundle(savedDialogArgsKeyFor(dialogId));
                md.mDialog = createDialog(dialogId, dialogState, md.mArgs);
                if (md.mDialog != null) {
                    mManagedDialogs.put(dialogId, md);
                    onPrepareDialog(dialogId, md.mDialog, md.mArgs);
                    md.mDialog.onRestoreInstanceState(dialogState);
                }
            }
        }
    }
    private Dialog createDialog(Integer dialogId, Bundle state, Bundle args) {
        final Dialog dialog = onCreateDialog(dialogId, args);
        if (dialog == null) {
            return null;
        }
        dialog.dispatchOnCreate(state);
        return dialog;
    }
    private static String savedDialogKeyFor(int key) {
        return SAVED_DIALOG_KEY_PREFIX + key;
    }
    private static String savedDialogArgsKeyFor(int key) {
        return SAVED_DIALOG_ARGS_KEY_PREFIX + key;
    }
    protected void onPostCreate(Bundle savedInstanceState) {
        if (!isChild()) {
            mTitleReady = true;
            onTitleChanged(getTitle(), getTitleColor());
        }
        mCalled = true;
    }
    protected void onStart() {
        mCalled = true;
    }
    protected void onRestart() {
        mCalled = true;
    }
    protected void onResume() {
        mCalled = true;
    }
    protected void onPostResume() {
        final Window win = getWindow();
        if (win != null) win.makeActive();
        mCalled = true;
    }
    protected void onNewIntent(Intent intent) {
    }
    final void performSaveInstanceState(Bundle outState) {
        onSaveInstanceState(outState);
        saveManagedDialogs(outState);
    }
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBundle(WINDOW_HIERARCHY_TAG, mWindow.saveHierarchyState());
    }
    private void saveManagedDialogs(Bundle outState) {
        if (mManagedDialogs == null) {
            return;
        }
        final int numDialogs = mManagedDialogs.size();
        if (numDialogs == 0) {
            return;
        }
        Bundle dialogState = new Bundle();
        int[] ids = new int[mManagedDialogs.size()];
        for (int i = 0; i < numDialogs; i++) {
            final int key = mManagedDialogs.keyAt(i);
            ids[i] = key;
            final ManagedDialog md = mManagedDialogs.valueAt(i);
            dialogState.putBundle(savedDialogKeyFor(key), md.mDialog.onSaveInstanceState());
            if (md.mArgs != null) {
                dialogState.putBundle(savedDialogArgsKeyFor(key), md.mArgs);
            }
        }
        dialogState.putIntArray(SAVED_DIALOG_IDS_KEY, ids);
        outState.putBundle(SAVED_DIALOGS_TAG, dialogState);
    }
    protected void onPause() {
        mCalled = true;
    }
    protected void onUserLeaveHint() {
    }
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        final View view = mDecor;
        if (view == null) {
            return false;
        }
        final int vw = view.getWidth();
        final int vh = view.getHeight();
        final int dw = outBitmap.getWidth();
        final int dh = outBitmap.getHeight();
        canvas.save();
        canvas.scale(((float)dw)/vw, ((float)dh)/vh);
        view.draw(canvas);
        canvas.restore();
        return true;
    }
    public CharSequence onCreateDescription() {
        return null;
    }
    protected void onStop() {
        mCalled = true;
    }
    protected void onDestroy() {
        mCalled = true;
        if (mManagedDialogs != null) {
            final int numDialogs = mManagedDialogs.size();
            for (int i = 0; i < numDialogs; i++) {
                final ManagedDialog md = mManagedDialogs.valueAt(i);
                if (md.mDialog.isShowing()) {
                    md.mDialog.dismiss();
                }
            }
            mManagedDialogs = null;
        }
        synchronized (mManagedCursors) {
            int numCursors = mManagedCursors.size();
            for (int i = 0; i < numCursors; i++) {
                ManagedCursor c = mManagedCursors.get(i);
                if (c != null) {
                    c.mCursor.close();
                }
            }
            mManagedCursors.clear();
        }
        if (mSearchManager != null) {
            mSearchManager.stopSearch();
        }
    }
    public void onConfigurationChanged(Configuration newConfig) {
        mCalled = true;
        if (mWindow != null) {
            mWindow.onConfigurationChanged(newConfig);
        }
    }
    public int getChangingConfigurations() {
        return mConfigChangeFlags;
    }
    public Object getLastNonConfigurationInstance() {
        return mLastNonConfigurationInstance;
    }
    public Object onRetainNonConfigurationInstance() {
        return null;
    }
    HashMap<String,Object> getLastNonConfigurationChildInstances() {
        return mLastNonConfigurationChildInstances;
    }
    HashMap<String,Object> onRetainNonConfigurationChildInstances() {
        return null;
    }
    public void onLowMemory() {
        mCalled = true;
    }
    public final Cursor managedQuery(Uri uri,
                                     String[] projection,
                                     String selection,
                                     String sortOrder)
    {
        Cursor c = getContentResolver().query(uri, projection, selection, null, sortOrder);
        if (c != null) {
            startManagingCursor(c);
        }
        return c;
    }
    public final Cursor managedQuery(Uri uri,
                                     String[] projection,
                                     String selection,
                                     String[] selectionArgs,
                                     String sortOrder)
    {
        Cursor c = getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (c != null) {
            startManagingCursor(c);
        }
        return c;
    }
    @Deprecated
    public void managedCommitUpdates(Cursor c) {
        synchronized (mManagedCursors) {
            final int N = mManagedCursors.size();
            for (int i=0; i<N; i++) {
                ManagedCursor mc = mManagedCursors.get(i);
                if (mc.mCursor == c) {
                    c.commitUpdates();
                    mc.mUpdated = true;
                    return;
                }
            }
            throw new RuntimeException(
                "Cursor " + c + " is not currently managed");
        }
    }
    public void startManagingCursor(Cursor c) {
        synchronized (mManagedCursors) {
            mManagedCursors.add(new ManagedCursor(c));
        }
    }
    public void stopManagingCursor(Cursor c) {
        synchronized (mManagedCursors) {
            final int N = mManagedCursors.size();
            for (int i=0; i<N; i++) {
                ManagedCursor mc = mManagedCursors.get(i);
                if (mc.mCursor == c) {
                    mManagedCursors.remove(i);
                    break;
                }
            }
        }
    }
    public void setPersistent(boolean isPersistent) {
        if (mParent == null) {
            try {
                ActivityManagerNative.getDefault()
                    .setPersistent(mToken, isPersistent);
            } catch (RemoteException e) {
            }
        } else {
            throw new RuntimeException("setPersistent() not yet supported for embedded activities");
        }
    }
    public View findViewById(int id) {
        return getWindow().findViewById(id);
    }
    public void setContentView(int layoutResID) {
        getWindow().setContentView(layoutResID);
    }
    public void setContentView(View view) {
        getWindow().setContentView(view);
    }
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().setContentView(view, params);
    }
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().addContentView(view, params);
    }
    static public final int DEFAULT_KEYS_DISABLE = 0;
    static public final int DEFAULT_KEYS_DIALER = 1;
    static public final int DEFAULT_KEYS_SHORTCUT = 2;
    static public final int DEFAULT_KEYS_SEARCH_LOCAL = 3;
    static public final int DEFAULT_KEYS_SEARCH_GLOBAL = 4;
    public final void setDefaultKeyMode(int mode) {
        mDefaultKeyMode = mode;
        switch (mode) {
        case DEFAULT_KEYS_DISABLE:
        case DEFAULT_KEYS_SHORTCUT:
            mDefaultKeySsb = null;      
            break;
        case DEFAULT_KEYS_DIALER:
        case DEFAULT_KEYS_SEARCH_LOCAL:
        case DEFAULT_KEYS_SEARCH_GLOBAL:
            mDefaultKeySsb = new SpannableStringBuilder();
            Selection.setSelection(mDefaultKeySsb,0);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getApplicationInfo().targetSdkVersion
                    >= Build.VERSION_CODES.ECLAIR) {
                event.startTracking();
            } else {
                onBackPressed();
            }
            return true;
        }
        if (mDefaultKeyMode == DEFAULT_KEYS_DISABLE) {
            return false;
        } else if (mDefaultKeyMode == DEFAULT_KEYS_SHORTCUT) {
            if (getWindow().performPanelShortcut(Window.FEATURE_OPTIONS_PANEL, 
                    keyCode, event, Menu.FLAG_ALWAYS_PERFORM_CLOSE)) {
                return true;
            }
            return false;
        } else {
            boolean clearSpannable = false;
            boolean handled;
            if ((event.getRepeatCount() != 0) || event.isSystem()) {
                clearSpannable = true;
                handled = false;
            } else {
                handled = TextKeyListener.getInstance().onKeyDown(
                        null, mDefaultKeySsb, keyCode, event);
                if (handled && mDefaultKeySsb.length() > 0) {
                    final String str = mDefaultKeySsb.toString();
                    clearSpannable = true;
                    switch (mDefaultKeyMode) {
                    case DEFAULT_KEYS_DIALER:
                        Intent intent = new Intent(Intent.ACTION_DIAL,  Uri.parse("tel:" + str));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);    
                        break;
                    case DEFAULT_KEYS_SEARCH_LOCAL:
                        startSearch(str, false, null, false);
                        break;
                    case DEFAULT_KEYS_SEARCH_GLOBAL:
                        startSearch(str, false, null, true);
                        break;
                    }
                }
            }
            if (clearSpannable) {
                mDefaultKeySsb.clear();
                mDefaultKeySsb.clearSpans();
                Selection.setSelection(mDefaultKeySsb,0);
            }
            return handled;
        }
    }
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getApplicationInfo().targetSdkVersion
                >= Build.VERSION_CODES.ECLAIR) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                    && !event.isCanceled()) {
                onBackPressed();
                return true;
            }
        }
        return false;
    }
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return false;
    }
    public void onBackPressed() {
        finish();
    }
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }
    public void onUserInteraction() {
    }
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        if (mParent == null) {
            View decor = mDecor;
            if (decor != null && decor.getParent() != null) {
                getWindowManager().updateViewLayout(decor, params);
            }
        }
    }
    public void onContentChanged() {
    }
    public void onWindowFocusChanged(boolean hasFocus) {
    }
    public void onAttachedToWindow() {
    }
    public void onDetachedFromWindow() {
    }
    public boolean hasWindowFocus() {
        Window w = getWindow();
        if (w != null) {
            View d = w.getDecorView();
            if (d != null) {
                return d.hasWindowFocus();
            }
        }
        return false;
    }
    public boolean dispatchKeyEvent(KeyEvent event) {
        onUserInteraction();
        Window win = getWindow();
        if (win.superDispatchKeyEvent(event)) {
            return true;
        }
        View decor = mDecor;
        if (decor == null) decor = win.getDecorView();
        return event.dispatch(this, decor != null
                ? decor.getKeyDispatcherState() : null, this);
    }
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        onUserInteraction();
        if (getWindow().superDispatchTrackballEvent(ev)) {
            return true;
        }
        return onTrackballEvent(ev);
    }
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getPackageName());
        LayoutParams params = getWindow().getAttributes();
        boolean isFullScreen = (params.width == LayoutParams.MATCH_PARENT) &&
            (params.height == LayoutParams.MATCH_PARENT);
        event.setFullScreen(isFullScreen);
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
           event.getText().add(title);
        }
        return true;
    }
    public View onCreatePanelView(int featureId) {
        return null;
    }
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL) {
            return onCreateOptionsMenu(menu);
        }
        return false;
    }
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL && menu != null) {
            boolean goforit = onPrepareOptionsMenu(menu);
            return goforit && menu.hasVisibleItems();
        }
        return true;
    }
    public boolean onMenuOpened(int featureId, Menu menu) {
        return true;
    }
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (featureId) {
            case Window.FEATURE_OPTIONS_PANEL:
                EventLog.writeEvent(50000, 0, item.getTitleCondensed());
                return onOptionsItemSelected(item);
            case Window.FEATURE_CONTEXT_MENU:
                EventLog.writeEvent(50000, 1, item.getTitleCondensed());
                return onContextItemSelected(item);
            default:
                return false;
        }
    }
    public void onPanelClosed(int featureId, Menu menu) {
        switch (featureId) {
            case Window.FEATURE_OPTIONS_PANEL:
                onOptionsMenuClosed(menu);
                break;
            case Window.FEATURE_CONTEXT_MENU:
                onContextMenuClosed(menu);
                break;
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mParent != null) {
            return mParent.onCreateOptionsMenu(menu);
        }
        return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mParent != null) {
            return mParent.onPrepareOptionsMenu(menu);
        }
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mParent != null) {
            return mParent.onOptionsItemSelected(item);
        }
        return false;
    }
    public void onOptionsMenuClosed(Menu menu) {
        if (mParent != null) {
            mParent.onOptionsMenuClosed(menu);
        }
    }
    public void openOptionsMenu() {
        mWindow.openPanel(Window.FEATURE_OPTIONS_PANEL, null);
    }
    public void closeOptionsMenu() {
        mWindow.closePanel(Window.FEATURE_OPTIONS_PANEL);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }
    public void registerForContextMenu(View view) {
        view.setOnCreateContextMenuListener(this);
    }
    public void unregisterForContextMenu(View view) {
        view.setOnCreateContextMenuListener(null);
    }
    public void openContextMenu(View view) {
        view.showContextMenu();
    }
    public void closeContextMenu() {
        mWindow.closePanel(Window.FEATURE_CONTEXT_MENU);
    }
    public boolean onContextItemSelected(MenuItem item) {
        if (mParent != null) {
            return mParent.onContextItemSelected(item);
        }
        return false;
    }
    public void onContextMenuClosed(Menu menu) {
        if (mParent != null) {
            mParent.onContextMenuClosed(menu);
        }
    }
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        return null;
    }
    protected Dialog onCreateDialog(int id, Bundle args) {
        return onCreateDialog(id);
    }
    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog) {
        dialog.setOwnerActivity(this);
    }
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        onPrepareDialog(id, dialog);
    }
    public final void showDialog(int id) {
        showDialog(id, null);
    }
    public final boolean showDialog(int id, Bundle args) {
        if (mManagedDialogs == null) {
            mManagedDialogs = new SparseArray<ManagedDialog>();
        }
        ManagedDialog md = mManagedDialogs.get(id);
        if (md == null) {
            md = new ManagedDialog();
            md.mDialog = createDialog(id, null, args);
            if (md.mDialog == null) {
                return false;
            }
            mManagedDialogs.put(id, md);
        }
        md.mArgs = args;
        onPrepareDialog(id, md.mDialog, args);
        md.mDialog.show();
        return true;
    }
    public final void dismissDialog(int id) {
        if (mManagedDialogs == null) {
            throw missingDialog(id);
        }
        final ManagedDialog md = mManagedDialogs.get(id);
        if (md == null) {
            throw missingDialog(id);
        }
        md.mDialog.dismiss();
    }
    private IllegalArgumentException missingDialog(int id) {
        return new IllegalArgumentException("no dialog with id " + id + " was ever "
                + "shown via Activity#showDialog");
    }
    public final void removeDialog(int id) {
        if (mManagedDialogs == null) {
            return;
        }
        final ManagedDialog md = mManagedDialogs.get(id);
        if (md == null) {
            return;
        }
        md.mDialog.dismiss();
        mManagedDialogs.remove(id);
    }
    public boolean onSearchRequested() {
        startSearch(null, false, null, false); 
        return true;
    }
    public void startSearch(String initialQuery, boolean selectInitialQuery, 
            Bundle appSearchData, boolean globalSearch) {
        ensureSearchManager();
        mSearchManager.startSearch(initialQuery, selectInitialQuery, getComponentName(),
                        appSearchData, globalSearch); 
    }
    public void triggerSearch(String query, Bundle appSearchData) {
        ensureSearchManager();
        mSearchManager.triggerSearch(query, getComponentName(), appSearchData);
    }
    public void takeKeyEvents(boolean get) {
        getWindow().takeKeyEvents(get);
    }
    public final boolean requestWindowFeature(int featureId) {
        return getWindow().requestFeature(featureId);
    }
    public final void setFeatureDrawableResource(int featureId, int resId) {
        getWindow().setFeatureDrawableResource(featureId, resId);
    }
    public final void setFeatureDrawableUri(int featureId, Uri uri) {
        getWindow().setFeatureDrawableUri(featureId, uri);
    }
    public final void setFeatureDrawable(int featureId, Drawable drawable) {
        getWindow().setFeatureDrawable(featureId, drawable);
    }
    public final void setFeatureDrawableAlpha(int featureId, int alpha) {
        getWindow().setFeatureDrawableAlpha(featureId, alpha);
    }
    public LayoutInflater getLayoutInflater() {
        return getWindow().getLayoutInflater();
    }
    public MenuInflater getMenuInflater() {
        return new MenuInflater(this);
    }
    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid,
            boolean first) {
        if (mParent == null) {
            super.onApplyThemeResource(theme, resid, first);
        } else {
            try {
                theme.setTo(mParent.getTheme());
            } catch (Exception e) {
            }
            theme.applyStyle(resid, false);
        }
    }
    public void startActivityForResult(Intent intent, int requestCode) {
        if (mParent == null) {
            Instrumentation.ActivityResult ar =
                mInstrumentation.execStartActivity(
                    this, mMainThread.getApplicationThread(), mToken, this,
                    intent, requestCode);
            if (ar != null) {
                mMainThread.sendActivityResult(
                    mToken, mEmbeddedID, requestCode, ar.getResultCode(),
                    ar.getResultData());
            }
            if (requestCode >= 0) {
                mStartedActivity = true;
            }
        } else {
            mParent.startActivityFromChild(this, intent, requestCode);
        }
    }
    public void startIntentSenderForResult(IntentSender intent, int requestCode,
            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws IntentSender.SendIntentException {
        if (mParent == null) {
            startIntentSenderForResultInner(intent, requestCode, fillInIntent,
                    flagsMask, flagsValues, this);
        } else {
            mParent.startIntentSenderFromChild(this, intent, requestCode,
                    fillInIntent, flagsMask, flagsValues, extraFlags);
        }
    }
    private void startIntentSenderForResultInner(IntentSender intent, int requestCode,
            Intent fillInIntent, int flagsMask, int flagsValues, Activity activity)
            throws IntentSender.SendIntentException {
        try {
            String resolvedType = null;
            if (fillInIntent != null) {
                resolvedType = fillInIntent.resolveTypeIfNeeded(getContentResolver());
            }
            int result = ActivityManagerNative.getDefault()
                .startActivityIntentSender(mMainThread.getApplicationThread(), intent,
                        fillInIntent, resolvedType, mToken, activity.mEmbeddedID,
                        requestCode, flagsMask, flagsValues);
            if (result == IActivityManager.START_CANCELED) {
                throw new IntentSender.SendIntentException();
            }
            Instrumentation.checkStartActivityResult(result, null);
        } catch (RemoteException e) {
        }
        if (requestCode >= 0) {
            mStartedActivity = true;
        }
    }
    @Override
    public void startActivity(Intent intent) {
        startActivityForResult(intent, -1);
    }
    public void startIntentSender(IntentSender intent,
            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws IntentSender.SendIntentException {
        startIntentSenderForResult(intent, -1, fillInIntent, flagsMask,
                flagsValues, extraFlags);
    }
    public boolean startActivityIfNeeded(Intent intent, int requestCode) {
        if (mParent == null) {
            int result = IActivityManager.START_RETURN_INTENT_TO_CALLER;
            try {
                result = ActivityManagerNative.getDefault()
                    .startActivity(mMainThread.getApplicationThread(),
                            intent, intent.resolveTypeIfNeeded(
                                    getContentResolver()),
                            null, 0,
                            mToken, mEmbeddedID, requestCode, true, false);
            } catch (RemoteException e) {
            }
            Instrumentation.checkStartActivityResult(result, intent);
            if (requestCode >= 0) {
                mStartedActivity = true;
            }
            return result != IActivityManager.START_RETURN_INTENT_TO_CALLER;
        }
        throw new UnsupportedOperationException(
            "startActivityIfNeeded can only be called from a top-level activity");
    }
    public boolean startNextMatchingActivity(Intent intent) {
        if (mParent == null) {
            try {
                return ActivityManagerNative.getDefault()
                    .startNextMatchingActivity(mToken, intent);
            } catch (RemoteException e) {
            }
            return false;
        }
        throw new UnsupportedOperationException(
            "startNextMatchingActivity can only be called from a top-level activity");
    }
    public void startActivityFromChild(Activity child, Intent intent, 
            int requestCode) {
        Instrumentation.ActivityResult ar =
            mInstrumentation.execStartActivity(
                this, mMainThread.getApplicationThread(), mToken, child,
                intent, requestCode);
        if (ar != null) {
            mMainThread.sendActivityResult(
                mToken, child.mEmbeddedID, requestCode,
                ar.getResultCode(), ar.getResultData());
        }
    }
    public void startIntentSenderFromChild(Activity child, IntentSender intent,
            int requestCode, Intent fillInIntent, int flagsMask, int flagsValues,
            int extraFlags)
            throws IntentSender.SendIntentException {
        startIntentSenderForResultInner(intent, requestCode, fillInIntent,
                flagsMask, flagsValues, child);
    }
    public void overridePendingTransition(int enterAnim, int exitAnim) {
        try {
            ActivityManagerNative.getDefault().overridePendingTransition(
                    mToken, getPackageName(), enterAnim, exitAnim);
        } catch (RemoteException e) {
        }
    }
    public final void setResult(int resultCode) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = null;
        }
    }
    public final void setResult(int resultCode, Intent data) {
        synchronized (this) {
            mResultCode = resultCode;
            mResultData = data;
        }
    }
    public String getCallingPackage() {
        try {
            return ActivityManagerNative.getDefault().getCallingPackage(mToken);
        } catch (RemoteException e) {
            return null;
        }
    }
    public ComponentName getCallingActivity() {
        try {
            return ActivityManagerNative.getDefault().getCallingActivity(mToken);
        } catch (RemoteException e) {
            return null;
        }
    }
    public void setVisible(boolean visible) {
        if (mVisibleFromClient != visible) {
            mVisibleFromClient = visible;
            if (mVisibleFromServer) {
                if (visible) makeVisible();
                else mDecor.setVisibility(View.INVISIBLE);
            }
        }
    }
    void makeVisible() {
        if (!mWindowAdded) {
            ViewManager wm = getWindowManager();
            wm.addView(mDecor, getWindow().getAttributes());
            mWindowAdded = true;
        }
        mDecor.setVisibility(View.VISIBLE);
    }
    public boolean isFinishing() {
        return mFinished;
    }
    public void finish() {
        if (mParent == null) {
            int resultCode;
            Intent resultData;
            synchronized (this) {
                resultCode = mResultCode;
                resultData = mResultData;
            }
            if (Config.LOGV) Log.v(TAG, "Finishing self: token=" + mToken);
            try {
                if (ActivityManagerNative.getDefault()
                    .finishActivity(mToken, resultCode, resultData)) {
                    mFinished = true;
                }
            } catch (RemoteException e) {
            }
        } else {
            mParent.finishFromChild(this);
        }
    }
    public void finishFromChild(Activity child) {
        finish();
    }
    public void finishActivity(int requestCode) {
        if (mParent == null) {
            try {
                ActivityManagerNative.getDefault()
                    .finishSubActivity(mToken, mEmbeddedID, requestCode);
            } catch (RemoteException e) {
            }
        } else {
            mParent.finishActivityFromChild(this, requestCode);
        }
    }
    public void finishActivityFromChild(Activity child, int requestCode) {
        try {
            ActivityManagerNative.getDefault()
                .finishSubActivity(mToken, child.mEmbeddedID, requestCode);
        } catch (RemoteException e) {
        }
    }
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    }
    public PendingIntent createPendingResult(int requestCode, Intent data,
            int flags) {
        String packageName = getPackageName();
        try {
            IIntentSender target =
                ActivityManagerNative.getDefault().getIntentSender(
                        IActivityManager.INTENT_SENDER_ACTIVITY_RESULT, packageName,
                        mParent == null ? mToken : mParent.mToken,
                        mEmbeddedID, requestCode, data, null, flags);
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
        }
        return null;
    }
    public void setRequestedOrientation(int requestedOrientation) {
        if (mParent == null) {
            try {
                ActivityManagerNative.getDefault().setRequestedOrientation(
                        mToken, requestedOrientation);
            } catch (RemoteException e) {
            }
        } else {
            mParent.setRequestedOrientation(requestedOrientation);
        }
    }
    public int getRequestedOrientation() {
        if (mParent == null) {
            try {
                return ActivityManagerNative.getDefault()
                        .getRequestedOrientation(mToken);
            } catch (RemoteException e) {
            }
        } else {
            return mParent.getRequestedOrientation();
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
    public int getTaskId() {
        try {
            return ActivityManagerNative.getDefault()
                .getTaskForActivity(mToken, false);
        } catch (RemoteException e) {
            return -1;
        }
    }
    public boolean isTaskRoot() {
        try {
            return ActivityManagerNative.getDefault()
                .getTaskForActivity(mToken, true) >= 0;
        } catch (RemoteException e) {
            return false;
        }
    }
    public boolean moveTaskToBack(boolean nonRoot) {
        try {
            return ActivityManagerNative.getDefault().moveActivityTaskToBack(
                    mToken, nonRoot);
        } catch (RemoteException e) {
        }
        return false;
    }
    public String getLocalClassName() {
        final String pkg = getPackageName();
        final String cls = mComponent.getClassName();
        int packageLen = pkg.length();
        if (!cls.startsWith(pkg) || cls.length() <= packageLen
                || cls.charAt(packageLen) != '.') {
            return cls;
        }
        return cls.substring(packageLen+1);
    }
    public ComponentName getComponentName()
    {
        return mComponent;
    }
    public SharedPreferences getPreferences(int mode) {
        return getSharedPreferences(getLocalClassName(), mode);
    }
    private void ensureSearchManager() {
        if (mSearchManager != null) {
            return;
        }
        mSearchManager = new SearchManager(this, null);
    }
    @Override
    public Object getSystemService(String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException(
                    "System services not available to Activities before onCreate()");
        }
        if (WINDOW_SERVICE.equals(name)) {
            return mWindowManager;
        } else if (SEARCH_SERVICE.equals(name)) {
            ensureSearchManager();
            return mSearchManager;
        }
        return super.getSystemService(name);
    }
    public void setTitle(CharSequence title) {
        mTitle = title;
        onTitleChanged(title, mTitleColor);
        if (mParent != null) {
            mParent.onChildTitleChanged(this, title);
        }
    }
    public void setTitle(int titleId) {
        setTitle(getText(titleId));
    }
    public void setTitleColor(int textColor) {
        mTitleColor = textColor;
        onTitleChanged(mTitle, textColor);
    }
    public final CharSequence getTitle() {
        return mTitle;
    }
    public final int getTitleColor() {
        return mTitleColor;
    }
    protected void onTitleChanged(CharSequence title, int color) {
        if (mTitleReady) {
            final Window win = getWindow();
            if (win != null) {
                win.setTitle(title);
                if (color != 0) {
                    win.setTitleColor(color);
                }
            }
        }
    }
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
    }
    public final void setProgressBarVisibility(boolean visible) {
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, visible ? Window.PROGRESS_VISIBILITY_ON :
            Window.PROGRESS_VISIBILITY_OFF);
    }
    public final void setProgressBarIndeterminateVisibility(boolean visible) {
        getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                visible ? Window.PROGRESS_VISIBILITY_ON : Window.PROGRESS_VISIBILITY_OFF);
    }
    public final void setProgressBarIndeterminate(boolean indeterminate) {
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
                indeterminate ? Window.PROGRESS_INDETERMINATE_ON : Window.PROGRESS_INDETERMINATE_OFF);
    }
    public final void setProgress(int progress) {
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, progress + Window.PROGRESS_START);
    }
    public final void setSecondaryProgress(int secondaryProgress) {
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
                secondaryProgress + Window.PROGRESS_SECONDARY_START);
    }
    public final void setVolumeControlStream(int streamType) {
        getWindow().setVolumeControlStream(streamType);
    }
    public final int getVolumeControlStream() {
        return getWindow().getVolumeControlStream();
    }
    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }
    final void setParent(Activity parent) {
        mParent = parent;
    }
    final void attach(Context context, ActivityThread aThread, Instrumentation instr, IBinder token,
            Application application, Intent intent, ActivityInfo info, CharSequence title, 
            Activity parent, String id, Object lastNonConfigurationInstance,
            Configuration config) {
        attach(context, aThread, instr, token, 0, application, intent, info, title, parent, id,
            lastNonConfigurationInstance, null, config);
    }
    final void attach(Context context, ActivityThread aThread,
            Instrumentation instr, IBinder token, int ident,
            Application application, Intent intent, ActivityInfo info,
            CharSequence title, Activity parent, String id,
            Object lastNonConfigurationInstance,
            HashMap<String,Object> lastNonConfigurationChildInstances,
            Configuration config) {
        attachBaseContext(context);
        mWindow = PolicyManager.makeNewWindow(this);
        mWindow.setCallback(this);
        if (info.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            mWindow.setSoftInputMode(info.softInputMode);
        }
        mUiThread = Thread.currentThread();
        mMainThread = aThread;
        mInstrumentation = instr;
        mToken = token;
        mIdent = ident;
        mApplication = application;
        mIntent = intent;
        mComponent = intent.getComponent();
        mActivityInfo = info;
        mTitle = title;
        mParent = parent;
        mEmbeddedID = id;
        mLastNonConfigurationInstance = lastNonConfigurationInstance;
        mLastNonConfigurationChildInstances = lastNonConfigurationChildInstances;
        mWindow.setWindowManager(null, mToken, mComponent.flattenToString());
        if (mParent != null) {
            mWindow.setContainer(mParent.getWindow());
        }
        mWindowManager = mWindow.getWindowManager();
        mCurrentConfig = config;
    }
    final IBinder getActivityToken() {
        return mParent != null ? mParent.getActivityToken() : mToken;
    }
    final void performStart() {
        mCalled = false;
        mInstrumentation.callActivityOnStart(this);
        if (!mCalled) {
            throw new SuperNotCalledException(
                "Activity " + mComponent.toShortString() +
                " did not call through to super.onStart()");
        }
    }
    final void performRestart() {
        synchronized (mManagedCursors) {
            final int N = mManagedCursors.size();
            for (int i=0; i<N; i++) {
                ManagedCursor mc = mManagedCursors.get(i);
                if (mc.mReleased || mc.mUpdated) {
                    mc.mCursor.requery();
                    mc.mReleased = false;
                    mc.mUpdated = false;
                }
            }
        }
        if (mStopped) {
            mStopped = false;
            mCalled = false;
            mInstrumentation.callActivityOnRestart(this);
            if (!mCalled) {
                throw new SuperNotCalledException(
                    "Activity " + mComponent.toShortString() +
                    " did not call through to super.onRestart()");
            }
            performStart();
        }
    }
    final void performResume() {
        performRestart();
        mLastNonConfigurationInstance = null;
        mCalled = false;
        mInstrumentation.callActivityOnResume(this);
        if (!mCalled) {
            throw new SuperNotCalledException(
                "Activity " + mComponent.toShortString() +
                " did not call through to super.onResume()");
        }
        mResumed = true;
        mCalled = false;
        onPostResume();
        if (!mCalled) {
            throw new SuperNotCalledException(
                "Activity " + mComponent.toShortString() +
                " did not call through to super.onPostResume()");
        }
    }
    final void performPause() {
        onPause();
    }
    final void performUserLeaving() {
        onUserInteraction();
        onUserLeaveHint();
    }
    final void performStop() {
        if (!mStopped) {
            if (mWindow != null) {
                mWindow.closeAllPanels();
            }
            mCalled = false;
            mInstrumentation.callActivityOnStop(this);
            if (!mCalled) {
                throw new SuperNotCalledException(
                    "Activity " + mComponent.toShortString() +
                    " did not call through to super.onStop()");
            }
            synchronized (mManagedCursors) {
                final int N = mManagedCursors.size();
                for (int i=0; i<N; i++) {
                    ManagedCursor mc = mManagedCursors.get(i);
                    if (!mc.mReleased) {
                        mc.mCursor.deactivate();
                        mc.mReleased = true;
                    }
                }
            }
            mStopped = true;
        }
        mResumed = false;
    }
    final boolean isResumed() {
        return mResumed;
    }
    void dispatchActivityResult(String who, int requestCode, 
        int resultCode, Intent data) {
        if (Config.LOGV) Log.v(
            TAG, "Dispatching result: who=" + who + ", reqCode=" + requestCode
            + ", resCode=" + resultCode + ", data=" + data);
        if (who == null) {
            onActivityResult(requestCode, resultCode, data);
        }
    }
}
