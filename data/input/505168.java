public class MidWindow extends Window implements MenuBuilder.Callback {
    private final static String TAG = "MidWindow";
    private final static boolean SWEEP_OPEN_MENU = false;
    ContextMenuCallback mContextMenuCallback = new ContextMenuCallback(FEATURE_CONTEXT_MENU);
    private DecorView mDecor;
    private ViewGroup mContentParent;
    private boolean mIsFloating;
    private LayoutInflater mLayoutInflater;
    private TextView mTitleView;
    private DrawableFeatureState[] mDrawables;
    private PanelFeatureState[] mPanels;
    private PanelFeatureState mPreparedPanel;
    private int mPanelChordingKey;
    private ImageView mLeftIconView;
    private ImageView mRightIconView;
    private ProgressBar mCircularProgressBar;
    private ProgressBar mHorizontalProgressBar;
    private int mBackgroundResource = 0;
    private Drawable mBackgroundDrawable;
    private int mFrameResource = 0;
    private int mTextColor = 0;
    private CharSequence mTitle = null;
    private int mTitleColor = 0;
    private ContextMenuBuilder mContextMenu;
    private MenuDialogHelper mContextMenuHelper;
    private int mVolumeControlStreamType = AudioManager.USE_DEFAULT_STREAM_TYPE;
    private long mVolumeKeyUpTime;
    private KeyguardManager mKeyguardManager = null;
    private TelephonyManager mTelephonyManager = null;
    private boolean mSearchKeyDownReceived;
    private final Handler mKeycodeCallTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mKeycodeCallTimeoutActive) return;
            mKeycodeCallTimeoutActive = false;
            Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                startCallActivity();
            }
        }
    };
    private boolean mKeycodeCallTimeoutActive = false;
    private final Handler mKeycodeCameraTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!mKeycodeCameraTimeoutActive) return;
            mKeycodeCameraTimeoutActive = false;
            Intent intent = new Intent(Intent.ACTION_CAMERA_BUTTON, null);
            intent.putExtra(Intent.EXTRA_KEY_EVENT, (KeyEvent) msg.obj);
            getContext().sendOrderedBroadcast(intent, null);
        }
    };
    private boolean mKeycodeCameraTimeoutActive = false;
    public MidWindow(Context context) {
        super(context);
        mLayoutInflater = LayoutInflater.from(context);
    }
    @Override
    public final void setContainer(Window container) {
        super.setContainer(container);
    }
    @Override
    public boolean requestFeature(int featureId) {
        if (mContentParent != null) {
            throw new AndroidRuntimeException("requestFeature() must be called before adding content");
        }
        final int features = getFeatures();
        if ((features != DEFAULT_FEATURES) && (featureId == FEATURE_CUSTOM_TITLE)) {
            throw new AndroidRuntimeException("You cannot combine custom titles with other title features");
        }
        if (((features & (1 << FEATURE_CUSTOM_TITLE)) != 0) && (featureId != FEATURE_CUSTOM_TITLE)) {
            throw new AndroidRuntimeException("You cannot combine custom titles with other title features");
        }
        return super.requestFeature(featureId);
    }
    @Override
    public void setContentView(int layoutResID) {
        if (mContentParent == null) {
            installDecor();
        } else {
            mContentParent.removeAllViews();
        }
        mLayoutInflater.inflate(layoutResID, mContentParent);
        final Callback cb = getCallback();
        if (cb != null) {
            cb.onContentChanged();
        }
    }
    @Override
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (mContentParent == null) {
            installDecor();
        } else {
            mContentParent.removeAllViews();
        }
        mContentParent.addView(view, params);
        final Callback cb = getCallback();
        if (cb != null) {
            cb.onContentChanged();
        }
    }
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        if (mContentParent == null) {
            installDecor();
        }
        mContentParent.addView(view, params);
        final Callback cb = getCallback();
        if (cb != null) {
            cb.onContentChanged();
        }
    }
    @Override
    public View getCurrentFocus() {
        return mDecor != null ? mDecor.findFocus() : null;
    }
    @Override
    public boolean isFloating() {
        return mIsFloating;
    }
    @Override
    public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }
    @Override
    public void setTitle(CharSequence title) {
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
        mTitle = title;
    }
    @Override
    public void setTitleColor(int textColor) {
        if (mTitleView != null) {
            mTitleView.setTextColor(textColor);
        }
        mTitleColor = textColor;
    }
    public final boolean preparePanel(PanelFeatureState st, KeyEvent event) {
        if (st.isPrepared)
            return true;
        if ((mPreparedPanel != null) && (mPreparedPanel != st)) {
            closePanel(mPreparedPanel, false);
        }
        final Callback cb = getCallback();
        if (cb != null) {
            st.createdPanelView = cb.onCreatePanelView(st.featureId);
        }
        if (st.createdPanelView == null) {
            if (st.menu == null) {
                if (!initializePanelMenu(st) || (st.menu == null)) {
                    return false;
                }
                if ((cb == null) || !cb.onCreatePanelMenu(st.featureId, st.menu)) {
                    st.menu = null;
                    return false;
                }
            }
            if (!cb.onPreparePanel(st.featureId, st.createdPanelView, st.menu)) {
                return false;
            }
            KeyCharacterMap kmap = KeyCharacterMap.load(event != null ? event.getDeviceId() : 0);
            st.qwertyMode = kmap.getKeyboardType() != KeyCharacterMap.NUMERIC;
            st.menu.setQwertyMode(st.qwertyMode);
        }
        st.isPrepared = true;
        st.isHandled = false;
        mPreparedPanel = st;
        return true;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        PanelFeatureState st = getPanelState(FEATURE_OPTIONS_PANEL, false);
        if ((st != null) && (st.menu != null)) {
            final MenuBuilder menuBuilder = (MenuBuilder) st.menu;
            if (st.isOpen) {
                final Bundle state = new Bundle();
                menuBuilder.saveHierarchyState(state);
                clearMenuViews(st);
                reopenMenu(false);
                menuBuilder.restoreHierarchyState(state);
            } else {
                clearMenuViews(st);
            }
        }
    }
    private static void clearMenuViews(PanelFeatureState st) {
        st.createdPanelView = null;
        st.refreshDecorView = true;
        ((MenuBuilder) st.menu).clearMenuViews();
    }
    @Override
    public final void openPanel(int featureId, KeyEvent event) {
        openPanel(getPanelState(featureId, true), event);
    }
    private void openPanel(PanelFeatureState st, KeyEvent event) {
        if (st.isOpen) {
            return;
        }
        Callback cb = getCallback();
        if ((cb != null) && (!cb.onMenuOpened(st.featureId, st.menu))) {
            closePanel(st, true);
            return;
        }
        final WindowManager wm = getWindowManager();
        if (wm == null) {
            return;
        }
        if (!preparePanel(st, event)) {
            return;
        }
        if (st.decorView == null || st.refreshDecorView) {
            if (st.decorView == null) {
                if (!initializePanelDecor(st) || (st.decorView == null))
                    return;
            } else if (st.refreshDecorView && (st.decorView.getChildCount() > 0)) {
                st.decorView.removeAllViews();
            }
            if (!initializePanelContent(st) || (st.shownPanelView == null)) {
                return;
            }
            ViewGroup.LayoutParams lp = st.shownPanelView.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            }
            int backgroundResId; 
            if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                backgroundResId = st.fullBackground;
            } else {
                backgroundResId = st.background;
            }
            st.decorView.setWindowBackground(getContext().getResources().getDrawable(
                    backgroundResId));
            st.decorView.addView(st.shownPanelView, lp);
            if (!st.shownPanelView.hasFocus()) {
                st.shownPanelView.requestFocus();
            }
        }
        st.isOpen = true;
        st.isHandled = false;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT,
                st.x, st.y, WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_DITHER,
                st.decorView.mDefaultOpacity);
        lp.gravity = st.gravity;
        lp.windowAnimations = st.windowAnimations;
        wm.addView(st.decorView, lp);
    }
    @Override
    public final void closePanel(int featureId) {
        closePanel(getPanelState(featureId, true), true);
    }
    public final void closePanel(PanelFeatureState st, boolean doCallback) {
        final ViewManager wm = getWindowManager();
        if ((wm != null) && st.isOpen) {
            if (st.decorView != null) {
                wm.removeView(st.decorView);
            }
            if (doCallback) {
                callOnPanelClosed(st.featureId, st, null);
            }
        }
        st.isPrepared = false;
        st.isHandled = false;
        st.isOpen = false;
        st.shownPanelView = null;
        if (st.isInExpandedMode) {
            st.refreshDecorView = true;
            st.isInExpandedMode = false;
        }
        if (mPreparedPanel == st) {
            mPreparedPanel = null;
            mPanelChordingKey = 0;
        }
    }
    @Override
    public final void togglePanel(int featureId, KeyEvent event) {
        PanelFeatureState st = getPanelState(featureId, true);
        if (st.isOpen) {
            closePanel(st, true);
        } else {
            openPanel(st, event);
        }
    }
    public final boolean onKeyDownPanel(int featureId, KeyEvent event) {
        mPanelChordingKey = event.getKeyCode();
        PanelFeatureState st = getPanelState(featureId, true);
        if (!st.isOpen) {
            return preparePanel(st, event);
        }
        return false;
    }
    public final void onKeyUpPanel(int featureId, KeyEvent event) {
        mPanelChordingKey = 0;
        boolean playSoundEffect = false;
        PanelFeatureState st = getPanelState(featureId, true);
        if (st.isOpen || st.isHandled) {
            playSoundEffect = st.isOpen;
            closePanel(st, true);
        } else if (st.isPrepared) {
            EventLog.writeEvent(50001, 0);
            openPanel(st, event);
            playSoundEffect = true;
        }
        if (playSoundEffect) {
            AudioManager audioManager = (AudioManager) getContext().getSystemService(
                    Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
            } else {
                Log.w(TAG, "Couldn't get audio manager");
            }
        }
    }
    @Override
    public final void closeAllPanels() {
        final ViewManager wm = getWindowManager();
        if (wm == null) {
            return;
        }
        final PanelFeatureState[] panels = mPanels;
        final int N = panels != null ? panels.length : 0;
        for (int i = 0; i < N; i++) {
            final PanelFeatureState panel = panels[i];
            if (panel != null) {
                closePanel(panel, true);
            }
        }
        closeContextMenu();
    }
    private synchronized void closeContextMenu() {
        mContextMenu = null;
        if (mContextMenuHelper != null) {
            mContextMenuHelper.dismiss();
            mContextMenuHelper = null;
        }
    }
    @Override
    public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
        return performPanelShortcut(getPanelState(featureId, true), keyCode, event, flags);
    }
    private boolean performPanelShortcut(PanelFeatureState st, int keyCode, KeyEvent event,
            int flags) {
        if (event.isSystem() || (st == null)) {
            return false;
        }
        boolean handled = false;
        if ((st.isPrepared || preparePanel(st, event)) && st.menu != null) {
            handled = st.menu.performShortcut(keyCode, event, flags);
        }
        if (handled) {
            st.isHandled = true;
            if ((flags & Menu.FLAG_PERFORM_NO_CLOSE) == 0) {
                closePanel(st, true);
            }
        }
        return handled;
    }
    @Override
    public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
        PanelFeatureState st = getPanelState(featureId, true);
        if (!preparePanel(st, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU))) {
            return false;
        }
        if (st.menu == null) {
            return false;
        }
        boolean res = st.menu.performIdentifierAction(id, flags);
        closePanel(st, true);
        return res;
    }
    public PanelFeatureState findMenuPanel(Menu menu) {
        final PanelFeatureState[] panels = mPanels;
        final int N = panels != null ? panels.length : 0;
        for (int i = 0; i < N; i++) {
            final PanelFeatureState panel = panels[i];
            if (panel != null && panel.menu == menu) {
                return panel;
            }
        }
        return null;
    }
    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        final Callback cb = getCallback();
        if (cb != null) {
            final PanelFeatureState panel = findMenuPanel(menu.getRootMenu());
            if (panel != null) {
                return cb.onMenuItemSelected(panel.featureId, item);
            }
        }
        return false;
    }
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        final PanelFeatureState panel = findMenuPanel(menu);
        if (panel != null) {
            closePanel(panel, allMenusAreClosing);
        }
    }
    public void onCloseSubMenu(SubMenuBuilder subMenu) {
        final Menu parentMenu = subMenu.getRootMenu();
        final PanelFeatureState panel = findMenuPanel(parentMenu);
        if (panel != null) {
            callOnPanelClosed(panel.featureId, panel, parentMenu);
            closePanel(panel, true);
        }
    }
    public boolean onSubMenuSelected(final SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return true;
        }
        new MenuDialogHelper(subMenu).show(null);
        return true;
    }
    public void onMenuModeChange(MenuBuilder menu) {
        reopenMenu(true);
    }
    private void reopenMenu(boolean toggleMenuMode) {
        PanelFeatureState st = getPanelState(FEATURE_OPTIONS_PANEL, true);
        boolean newExpandedMode = toggleMenuMode ? !st.isInExpandedMode : st.isInExpandedMode;
        st.refreshDecorView = true;
        closePanel(st, false);
        st.isInExpandedMode = newExpandedMode;
        openPanel(st, null);
    }
    protected boolean initializePanelMenu(final PanelFeatureState st) {
        final MenuBuilder menu = new MenuBuilder(getContext());
        menu.setCallback(this);
        st.setMenu(menu);
        return true;
    }
    protected boolean initializePanelDecor(PanelFeatureState st) {
        st.decorView = new DecorView(getContext(), st.featureId);
        st.gravity = Gravity.CENTER | Gravity.BOTTOM;
        st.setStyle(getContext());
        return true;
    }
    protected boolean initializePanelContent(PanelFeatureState st) {
        if (st.createdPanelView != null) {
            st.shownPanelView = st.createdPanelView;
            return true;
        }
        final MenuBuilder menu = (MenuBuilder)st.menu;
        if (menu == null) {
            return false;
        }
        st.shownPanelView = menu.getMenuView((st.isInExpandedMode) ? MenuBuilder.TYPE_EXPANDED
                : MenuBuilder.TYPE_ICON, st.decorView);
        if (st.shownPanelView != null) {
            final int defaultAnimations = ((MenuView) st.shownPanelView).getWindowAnimations();
            if (defaultAnimations != 0) {
                st.windowAnimations = defaultAnimations;
            }
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean performContextMenuIdentifierAction(int id, int flags) {
        return (mContextMenu != null) ? mContextMenu.performIdentifierAction(id, flags) : false;
    }
    @Override
    public final void setBackgroundDrawable(Drawable drawable) {
        if (drawable != mBackgroundDrawable) {
            mBackgroundResource = 0;
            mBackgroundDrawable = drawable;
            if (mDecor != null) {
                mDecor.setWindowBackground(drawable);
            }
        }
    }
    @Override
    public final void setFeatureDrawableResource(int featureId, int resId) {
        if (resId != 0) {
            DrawableFeatureState st = getDrawableState(featureId, true);
            if (st.resid != resId) {
                st.resid = resId;
                st.uri = null;
                st.local = getContext().getResources().getDrawable(resId);
                updateDrawable(featureId, st, false);
            }
        } else {
            setFeatureDrawable(featureId, null);
        }
    }
    @Override
    public final void setFeatureDrawableUri(int featureId, Uri uri) {
        if (uri != null) {
            DrawableFeatureState st = getDrawableState(featureId, true);
            if (st.uri == null || !st.uri.equals(uri)) {
                st.resid = 0;
                st.uri = uri;
                st.local = loadImageURI(uri);
                updateDrawable(featureId, st, false);
            }
        } else {
            setFeatureDrawable(featureId, null);
        }
    }
    @Override
    public final void setFeatureDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        st.resid = 0;
        st.uri = null;
        if (st.local != drawable) {
            st.local = drawable;
            updateDrawable(featureId, st, false);
        }
    }
    @Override
    public void setFeatureDrawableAlpha(int featureId, int alpha) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        if (st.alpha != alpha) {
            st.alpha = alpha;
            updateDrawable(featureId, st, false);
        }
    }
    protected final void setFeatureDefaultDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        if (st.def != drawable) {
            st.def = drawable;
            updateDrawable(featureId, st, false);
        }
    }
    @Override
    public final void setFeatureInt(int featureId, int value) {
        updateInt(featureId, value, false);
    }
    protected final void updateDrawable(int featureId, boolean fromActive) {
        final DrawableFeatureState st = getDrawableState(featureId, false);
        if (st != null) {
            updateDrawable(featureId, st, fromActive);
        }
    }
    protected void onDrawableChanged(int featureId, Drawable drawable, int alpha) {
        ImageView view;
        if (featureId == FEATURE_LEFT_ICON) {
            view = getLeftIconView();
        } else if (featureId == FEATURE_RIGHT_ICON) {
            view = getRightIconView();
        } else {
            return;
        }
        if (drawable != null) {
            drawable.setAlpha(alpha);
            view.setImageDrawable(drawable);
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
    protected void onIntChanged(int featureId, int value) {
        if (featureId == FEATURE_PROGRESS || featureId == FEATURE_INDETERMINATE_PROGRESS) {
            updateProgressBars(value);
        } else if (featureId == FEATURE_CUSTOM_TITLE) {
            FrameLayout titleContainer = (FrameLayout) findViewById(com.android.internal.R.id.title_container);
            if (titleContainer != null) {
                mLayoutInflater.inflate(value, titleContainer);
            }
        }
    }
    private void updateProgressBars(int value) {
        ProgressBar circularProgressBar = getCircularProgressBar(true);
        ProgressBar horizontalProgressBar = getHorizontalProgressBar(true);
        final int features = getLocalFeatures();
        if (value == PROGRESS_VISIBILITY_ON) {
            if ((features & (1 << FEATURE_PROGRESS)) != 0) {
                int level = horizontalProgressBar.getProgress();
                int visibility = (horizontalProgressBar.isIndeterminate() || level < 10000) ?
                        View.VISIBLE : View.INVISIBLE;
                horizontalProgressBar.setVisibility(visibility);
            }
            if ((features & (1 << FEATURE_INDETERMINATE_PROGRESS)) != 0) {
                circularProgressBar.setVisibility(View.VISIBLE);
            }
        } else if (value == PROGRESS_VISIBILITY_OFF) {
            if ((features & (1 << FEATURE_PROGRESS)) != 0) {
                horizontalProgressBar.setVisibility(View.GONE);
            }
            if ((features & (1 << FEATURE_INDETERMINATE_PROGRESS)) != 0) {
                circularProgressBar.setVisibility(View.GONE);
            }
        } else if (value == PROGRESS_INDETERMINATE_ON) {
            horizontalProgressBar.setIndeterminate(true);
        } else if (value == PROGRESS_INDETERMINATE_OFF) {
            horizontalProgressBar.setIndeterminate(false);
        } else if (PROGRESS_START <= value && value <= PROGRESS_END) {
            horizontalProgressBar.setProgress(value - PROGRESS_START);
            if (value < PROGRESS_END) {
                showProgressBars(horizontalProgressBar, circularProgressBar);
            } else {
                hideProgressBars(horizontalProgressBar, circularProgressBar);
            }
        } else if (PROGRESS_SECONDARY_START <= value && value <= PROGRESS_SECONDARY_END) {
            horizontalProgressBar.setSecondaryProgress(value - PROGRESS_SECONDARY_START);
            showProgressBars(horizontalProgressBar, circularProgressBar);
        }
    }
    private void showProgressBars(ProgressBar horizontalProgressBar, ProgressBar spinnyProgressBar) {
        final int features = getLocalFeatures();
        if ((features & (1 << FEATURE_INDETERMINATE_PROGRESS)) != 0 &&
                spinnyProgressBar.getVisibility() == View.INVISIBLE) {
            spinnyProgressBar.setVisibility(View.VISIBLE);
        }
        if ((features & (1 << FEATURE_PROGRESS)) != 0 &&
                horizontalProgressBar.getProgress() < 10000) {
            horizontalProgressBar.setVisibility(View.VISIBLE);
        }
    }
    private void hideProgressBars(ProgressBar horizontalProgressBar, ProgressBar spinnyProgressBar) {
        final int features = getLocalFeatures();
        Animation anim = AnimationUtils.loadAnimation(getContext(), com.android.internal.R.anim.fade_out);
        anim.setDuration(1000);
        if ((features & (1 << FEATURE_INDETERMINATE_PROGRESS)) != 0 &&
                spinnyProgressBar.getVisibility() == View.VISIBLE) {
            spinnyProgressBar.startAnimation(anim);
            spinnyProgressBar.setVisibility(View.INVISIBLE);
        }
        if ((features & (1 << FEATURE_PROGRESS)) != 0 &&
                horizontalProgressBar.getVisibility() == View.VISIBLE) {
            horizontalProgressBar.startAnimation(anim);
            horizontalProgressBar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void takeKeyEvents(boolean get) {
        mDecor.setFocusable(get);
    }
    @Override
    public boolean superDispatchKeyEvent(KeyEvent event) {
        return mDecor.superDispatchKeyEvent(event);
    }
    @Override
    public boolean superDispatchTouchEvent(MotionEvent event) {
        return mDecor.superDispatchTouchEvent(event);
    }
    @Override
    public boolean superDispatchTrackballEvent(MotionEvent event) {
        return mDecor.superDispatchTrackballEvent(event);
    }
    protected boolean onKeyDown(int featureId, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                AudioManager audioManager = (AudioManager) getContext().getSystemService(
                        Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.adjustSuggestedStreamVolume(
                            keyCode == KeyEvent.KEYCODE_VOLUME_UP
                                    ? AudioManager.ADJUST_RAISE
                                    : AudioManager.ADJUST_LOWER,
                            mVolumeControlStreamType,
                            AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_VIBRATE);
                }
                return true;
            }
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mTelephonyManager == null) {
                    mTelephonyManager = (TelephonyManager) getContext().getSystemService(
                            Context.TELEPHONY_SERVICE);
                }
                if (mTelephonyManager != null &&
                        mTelephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                    return true;  
                }
            case KeyEvent.KEYCODE_MUTE: 
            case KeyEvent.KEYCODE_HEADSETHOOK: 
            case KeyEvent.KEYCODE_MEDIA_STOP: 
            case KeyEvent.KEYCODE_MEDIA_NEXT: 
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: 
            case KeyEvent.KEYCODE_MEDIA_REWIND: 
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                getContext().sendOrderedBroadcast(intent, null);
                return true;
            }
            case KeyEvent.KEYCODE_CAMERA: {
                if (getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    break;
                }
                if (event.getRepeatCount() > 0) break;
                mKeycodeCameraTimeoutActive = true;
                mKeycodeCameraTimeoutHandler.removeMessages(0);
                Message message = mKeycodeCameraTimeoutHandler.obtainMessage(0);
                message.obj = event;
                mKeycodeCameraTimeoutHandler.sendMessageDelayed(message,
                        ViewConfiguration.getLongPressTimeout());
                return true;
            }
            case KeyEvent.KEYCODE_MENU: {
                if (event.getRepeatCount() > 0) break;
                onKeyDownPanel((featureId < 0) ? FEATURE_OPTIONS_PANEL : featureId, event);
                return true;
            }
            case KeyEvent.KEYCODE_BACK: {
                if (event.getRepeatCount() > 0) break;
                if (featureId < 0) break;
                if (featureId == FEATURE_OPTIONS_PANEL) {
                    PanelFeatureState st = getPanelState(featureId, false);
                    if (st != null && st.isInExpandedMode) {
                        reopenMenu(true);
                        return true;
                    }
                }
                closePanel(featureId);
                return true;
            }
            case KeyEvent.KEYCODE_CALL: {
                if (getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    break;
                }
                if (event.getRepeatCount() > 0) break;
                mKeycodeCallTimeoutActive = true;
                mKeycodeCallTimeoutHandler.removeMessages(0);
                mKeycodeCallTimeoutHandler.sendMessageDelayed(
                        mKeycodeCallTimeoutHandler.obtainMessage(0),
                        ViewConfiguration.getLongPressTimeout());
                return true;
            }
            case KeyEvent.KEYCODE_SEARCH: {
                if (event.getRepeatCount() == 0) {
                    mSearchKeyDownReceived = true;
                }
                break;
            }
        }
        return false;
    }
    private KeyguardManager getKeyguardManager() {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        }
        return mKeyguardManager;
    }
    protected boolean onKeyUp(int featureId, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                AudioManager audioManager = (AudioManager) getContext().getSystemService(
                        Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.adjustSuggestedStreamVolume(
                            AudioManager.ADJUST_SAME,
                            mVolumeControlStreamType,
                            AudioManager.FLAG_PLAY_SOUND);
                    mVolumeKeyUpTime = SystemClock.uptimeMillis();
                }
                return true;
            }
            case KeyEvent.KEYCODE_MENU: {
                onKeyUpPanel(featureId < 0 ? FEATURE_OPTIONS_PANEL : featureId,
                        event);
                return true;
            }
            case KeyEvent.KEYCODE_HEADSETHOOK: 
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: 
            case KeyEvent.KEYCODE_MEDIA_STOP: 
            case KeyEvent.KEYCODE_MEDIA_NEXT: 
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS: 
            case KeyEvent.KEYCODE_MEDIA_REWIND: 
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                intent.putExtra(Intent.EXTRA_KEY_EVENT, event);
                getContext().sendOrderedBroadcast(intent, null);
                return true;
            }
            case KeyEvent.KEYCODE_CAMERA: {
                if (getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    break;
                }
                if (event.getRepeatCount() > 0) break; 
                mKeycodeCameraTimeoutHandler.removeMessages(0);
                if (!mKeycodeCameraTimeoutActive) break;
                mKeycodeCameraTimeoutActive = false;
                return true;
            }
            case KeyEvent.KEYCODE_CALL: {
                if (getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    break;
                }
                if (event.getRepeatCount() > 0) break;
                mKeycodeCallTimeoutHandler.removeMessages(0);
                if (!mKeycodeCallTimeoutActive) break;
                mKeycodeCallTimeoutActive = false;
                startCallActivity();
                return true;
            }
            case KeyEvent.KEYCODE_SEARCH: {
                if (getKeyguardManager().inKeyguardRestrictedInputMode() ||
                        !mSearchKeyDownReceived) {
                    break;
                }
                mSearchKeyDownReceived = false;
                launchDefaultSearch();
                return true;
            }
        }
        return false;
    }
    private void startCallActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("call_key", true);
        getContext().startActivity(intent);
    }
    @Override
    protected void onActive() {
    }
    @Override
    public final View getDecorView() {
        if (mDecor == null) {
            installDecor();
        }
        return mDecor;
    }
    @Override
    public final View peekDecorView() {
        return mDecor;
    }
    static private final String FOCUSED_ID_TAG = "android:focusedViewId";
    static private final String VIEWS_TAG = "android:views";
    static private final String PANELS_TAG = "android:Panels";
    @Override
    public Bundle saveHierarchyState() {
        Bundle outState = new Bundle();
        if (mContentParent == null) {
            return outState;
        }
        SparseArray<Parcelable> states = new SparseArray<Parcelable>();
        mContentParent.saveHierarchyState(states);
        outState.putSparseParcelableArray(VIEWS_TAG, states);
        View focusedView = mContentParent.findFocus();
        if (focusedView != null) {
            if (focusedView.getId() != View.NO_ID) {
                outState.putInt(FOCUSED_ID_TAG, focusedView.getId());
            } else {
                if (Config.LOGD) {
                    Log.d(TAG, "couldn't save which view has focus because the focused view "
                            + focusedView + " has no id.");
                }
            }
        }
        SparseArray<Parcelable> panelStates = new SparseArray<Parcelable>();
        savePanelState(panelStates);
        if (panelStates.size() > 0) {
            outState.putSparseParcelableArray(PANELS_TAG, panelStates);
        }
        return outState;
    }
    @Override
    public void restoreHierarchyState(Bundle savedInstanceState) {
        if (mContentParent == null) {
            return;
        }
        SparseArray<Parcelable> savedStates
                = savedInstanceState.getSparseParcelableArray(VIEWS_TAG);
        if (savedStates != null) {
            mContentParent.restoreHierarchyState(savedStates);
        }
        int focusedViewId = savedInstanceState.getInt(FOCUSED_ID_TAG, View.NO_ID);
        if (focusedViewId != View.NO_ID) {
            View needsFocus = mContentParent.findViewById(focusedViewId);
            if (needsFocus != null) {
                needsFocus.requestFocus();
            } else {
                Log.w(TAG,
                        "Previously focused view reported id " + focusedViewId
                                + " during save, but can't be found during restore.");
            }
        }
        SparseArray<Parcelable> panelStates = savedInstanceState.getSparseParcelableArray(PANELS_TAG);
        if (panelStates != null) {
            restorePanelState(panelStates);
        }
    }
    private void savePanelState(SparseArray<Parcelable> icicles) {
        PanelFeatureState[] panels = mPanels;
        if (panels == null) {
            return;
        }
        for (int curFeatureId = panels.length - 1; curFeatureId >= 0; curFeatureId--) {
            if (panels[curFeatureId] != null) {
                icicles.put(curFeatureId, panels[curFeatureId].onSaveInstanceState());
            }
        }
    }
    private void restorePanelState(SparseArray<Parcelable> icicles) {
        PanelFeatureState st;
        for (int curFeatureId = icicles.size() - 1; curFeatureId >= 0; curFeatureId--) {
            st = getPanelState(curFeatureId, false );
            if (st == null) {
                continue;
            }
            st.onRestoreInstanceState(icicles.get(curFeatureId));
        }
    }
    private void openPanelsAfterRestore() {
        PanelFeatureState[] panels = mPanels;
        if (panels == null) {
            return;
        }
        PanelFeatureState st;
        for (int i = panels.length - 1; i >= 0; i--) {
            st = panels[i];
            if ((st != null) && st.isOpen) {
                st.isOpen = false;
                openPanel(st, null);
            }
        }
    }
    private final class DecorView extends FrameLayout {
        int mDefaultOpacity = PixelFormat.OPAQUE;
        private final int mFeatureId;
        private final Rect mDrawingBounds = new Rect();
        private final Rect mBackgroundPadding = new Rect();
        private final Rect mFramePadding = new Rect();
        private final Rect mFrameOffsets = new Rect();
        private final Paint mBlackPaint = new Paint();
        private boolean mChanging;
        private Drawable mMenuBackground;
        private boolean mWatchingForMenu;
        private int mDownY;
        public DecorView(Context context, int featureId) {
            super(context);
            mFeatureId = featureId;
            mBlackPaint.setColor(0xFF000000);
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            final int keyCode = event.getKeyCode();
            final boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;
            if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                    && mVolumeKeyUpTime + VolumePanel.PLAY_SOUND_DELAY
                            > SystemClock.uptimeMillis()) {
                AudioManager audioManager = (AudioManager) getContext().getSystemService(
                        Context.AUDIO_SERVICE);
                if (audioManager != null) {
                    audioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_SAME,
                            mVolumeControlStreamType, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                }
            }
            if (isDown && (event.getRepeatCount() == 0)) {
                if ((mPanelChordingKey > 0) && (mPanelChordingKey != keyCode)) {
                    boolean handled = performPanelShortcut(mPreparedPanel, keyCode, event,
                            Menu.FLAG_PERFORM_NO_CLOSE);
                    if (!handled) {
                        handled = dispatchKeyShortcut(event);
                        if (handled && mPreparedPanel != null) {
                            mPreparedPanel.isHandled = true;
                        }
                    }
                    if (handled) {
                        return true;
                    }
                }
                if ((mPreparedPanel != null) && mPreparedPanel.isOpen) {
                    if (performPanelShortcut(mPreparedPanel, keyCode, event, 0)) {
                        return true;
                    }
                }
            }
            final Callback cb = getCallback();
            final boolean handled = cb != null && mFeatureId < 0 ? cb.dispatchKeyEvent(event)
                    : super.dispatchKeyEvent(event);
            if (handled) {
                return true;
            }
            return isDown ? MidWindow.this.onKeyDown(mFeatureId, event.getKeyCode(), event)
                    : MidWindow.this.onKeyUp(mFeatureId, event.getKeyCode(), event);
        }
        private boolean dispatchKeyShortcut(KeyEvent event) {
            View focusedView = findFocus();
            return focusedView == null ? false : focusedView.dispatchKeyShortcutEvent(event);
        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            final Callback cb = getCallback();
            return cb != null && mFeatureId < 0 ? cb.dispatchTouchEvent(ev) : super
                    .dispatchTouchEvent(ev);
        }
        @Override
        public boolean dispatchTrackballEvent(MotionEvent ev) {
            final Callback cb = getCallback();
            return cb != null && mFeatureId < 0 ? cb.dispatchTrackballEvent(ev) : super
                    .dispatchTrackballEvent(ev);
        }
        public boolean superDispatchKeyEvent(KeyEvent event) {
            return super.dispatchKeyEvent(event);
        }
        public boolean superDispatchTouchEvent(MotionEvent event) {
            return super.dispatchTouchEvent(event);
        }
        public boolean superDispatchTrackballEvent(MotionEvent event) {
            return super.dispatchTrackballEvent(event);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return onInterceptTouchEvent(event);
        }
        private boolean isOutOfBounds(int x, int y) {
            return x < -5 || y < -5 || x > (getWidth() + 5)
                    || y > (getHeight() + 5);
        }
        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (mFeatureId >= 0) {
                if (action == MotionEvent.ACTION_DOWN) {
                    int x = (int)event.getX();
                    int y = (int)event.getY();
                    if (isOutOfBounds(x, y)) {
                        closePanel(mFeatureId);
                        return true;
                    }
                }
            }
            if (!SWEEP_OPEN_MENU) {
                return false;
            }
            if (mFeatureId >= 0) {
                if (action == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "Watchiing!");
                    mWatchingForMenu = true;
                    mDownY = (int) event.getY();
                    return false;
                }
                if (!mWatchingForMenu) {
                    return false;
                }
                int y = (int)event.getY();
                if (action == MotionEvent.ACTION_MOVE) {
                    if (y > (mDownY+30)) {
                        Log.i(TAG, "Closing!");
                        closePanel(mFeatureId);
                        mWatchingForMenu = false;
                        return true;
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    mWatchingForMenu = false;
                }
                return false;
            }
            if (action == MotionEvent.ACTION_DOWN) {
                int y = (int)event.getY();
                if (y >= (getHeight()-5) && !hasChildren()) {
                    Log.i(TAG, "Watchiing!");
                    mWatchingForMenu = true;
                }
                return false;
            }
            if (!mWatchingForMenu) {
                return false;
            }
            int y = (int)event.getY();
            if (action == MotionEvent.ACTION_MOVE) {
                if (y < (getHeight()-30)) {
                    Log.i(TAG, "Opening!");
                    openPanel(FEATURE_OPTIONS_PANEL, new KeyEvent(
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
                    mWatchingForMenu = false;
                    return true;
                }
            } else if (action == MotionEvent.ACTION_UP) {
                mWatchingForMenu = false;
            }
            return false;
        }
        @Override
        protected boolean setFrame(int l, int t, int r, int b) {
            boolean changed = super.setFrame(l, t, r, b);
            if (changed) {
                final Rect drawingBounds = mDrawingBounds;
                getDrawingRect(drawingBounds);
                Drawable fg = getForeground();
                if (fg != null) {
                    final Rect frameOffsets = mFrameOffsets;
                    drawingBounds.left += frameOffsets.left;
                    drawingBounds.top += frameOffsets.top;
                    drawingBounds.right -= frameOffsets.right;
                    drawingBounds.bottom -= frameOffsets.bottom;
                    fg.setBounds(drawingBounds);
                    final Rect framePadding = mFramePadding;
                    drawingBounds.left += framePadding.left - frameOffsets.left;
                    drawingBounds.top += framePadding.top - frameOffsets.top;
                    drawingBounds.right -= framePadding.right - frameOffsets.right;
                    drawingBounds.bottom -= framePadding.bottom - frameOffsets.bottom;
                }
                Drawable bg = getBackground();
                if (bg != null) {
                    bg.setBounds(drawingBounds);
                }
                if (SWEEP_OPEN_MENU) {
                    if (mMenuBackground == null && mFeatureId < 0
                            && getAttributes().height
                            == WindowManager.LayoutParams.MATCH_PARENT) {
                        mMenuBackground = getContext().getResources().getDrawable(
                                com.android.internal.R.drawable.menu_background);
                    }
                    if (mMenuBackground != null) {
                        mMenuBackground.setBounds(drawingBounds.left,
                                drawingBounds.bottom-6, drawingBounds.right,
                                drawingBounds.bottom+20);
                    }
                }
            }
            return changed;
        }
        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (mMenuBackground != null) {
                mMenuBackground.draw(canvas);
            }
        }
        @Override
        public boolean showContextMenuForChild(View originalView) {
            if (mContextMenu == null) {
                mContextMenu = new ContextMenuBuilder(getContext());
                mContextMenu.setCallback(mContextMenuCallback);
            } else {
                mContextMenu.clearAll();
            }
            mContextMenuHelper = mContextMenu.show(originalView, originalView.getWindowToken());
            return mContextMenuHelper != null;
        }
        public void startChanging() {
            mChanging = true;
        }
        public void finishChanging() {
            mChanging = false;
            drawableChanged();
        }
        public void setWindowBackground(Drawable drawable) {
            if (getBackground() != drawable) {
                setBackgroundDrawable(drawable);
                if (drawable != null) {
                    drawable.getPadding(mBackgroundPadding);
                } else {
                    mBackgroundPadding.setEmpty();
                }
                drawableChanged();
            }
        }
        public void setWindowFrame(Drawable drawable) {
            if (getForeground() != drawable) {
                setForeground(drawable);
                if (drawable != null) {
                    drawable.getPadding(mFramePadding);
                } else {
                    mFramePadding.setEmpty();
                }
                drawableChanged();
            }
        }
        @Override
        protected boolean fitSystemWindows(Rect insets) {
            mFrameOffsets.set(insets);
            if (getForeground() != null) {
                drawableChanged();
            }
            return super.fitSystemWindows(insets);
        }
        private void drawableChanged() {
            if (mChanging) {
                return;
            }
            setPadding(mFramePadding.left + mBackgroundPadding.left, mFramePadding.top
                    + mBackgroundPadding.top, mFramePadding.right + mBackgroundPadding.right,
                    mFramePadding.bottom + mBackgroundPadding.bottom);
            requestLayout();
            invalidate();
            int opacity = PixelFormat.OPAQUE;
            Drawable bg = getBackground();
            Drawable fg = getForeground();
            if (bg != null) {
                if (fg == null) {
                    opacity = bg.getOpacity();
                } else if (mFramePadding.left <= 0 && mFramePadding.top <= 0
                        && mFramePadding.right <= 0 && mFramePadding.bottom <= 0) {
                    int fop = fg.getOpacity();
                    int bop = bg.getOpacity();
                    if (Config.LOGV)
                        Log.v(TAG, "Background opacity: " + bop + ", Frame opacity: " + fop);
                    if (fop == PixelFormat.OPAQUE || bop == PixelFormat.OPAQUE) {
                        opacity = PixelFormat.OPAQUE;
                    } else if (fop == PixelFormat.UNKNOWN) {
                        opacity = bop;
                    } else if (bop == PixelFormat.UNKNOWN) {
                        opacity = fop;
                    } else {
                        opacity = Drawable.resolveOpacity(fop, bop);
                    }
                } else {
                    if (Config.LOGV)
                        Log.v(TAG, "Padding: " + mFramePadding);
                    opacity = PixelFormat.TRANSLUCENT;
                }
            }
            if (Config.LOGV)
                Log.v(TAG, "Background: " + bg + ", Frame: " + fg);
            if (Config.LOGV)
                Log.v(TAG, "Selected default opacity: " + opacity);
            mDefaultOpacity = opacity;
            if (mFeatureId < 0) {
                setDefaultWindowFormat(opacity);
            }
        }
        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            mKeycodeCallTimeoutHandler.removeMessages(0);
            mKeycodeCallTimeoutActive = false;
            mKeycodeCameraTimeoutHandler.removeMessages(0);
            mKeycodeCameraTimeoutActive = false;
            if (!hasWindowFocus && mPanelChordingKey > 0) {
                closePanel(FEATURE_OPTIONS_PANEL);
            }
            final Callback cb = getCallback();
            if (cb != null && mFeatureId < 0) {
                cb.onWindowFocusChanged(hasWindowFocus);
            }
        }
        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            final Callback cb = getCallback();
            if (cb != null && mFeatureId < 0) {
                cb.onAttachedToWindow();
            }
            if (mFeatureId == -1) {
                openPanelsAfterRestore();
            }
        }
        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            final Callback cb = getCallback();
            if (cb != null && mFeatureId < 0) {
                cb.onDetachedFromWindow();
            }
        }
    }
    protected DecorView generateDecor() {
        return new DecorView(getContext(), -1);
    }
    protected void setFeatureFromAttrs(int featureId, TypedArray attrs,
            int drawableAttr, int alphaAttr) {
        Drawable d = attrs.getDrawable(drawableAttr);
        if (d != null) {
            requestFeature(featureId);
            setFeatureDefaultDrawable(featureId, d);
        }
        if ((getFeatures() & (1 << featureId)) != 0) {
            int alpha = attrs.getInt(alphaAttr, -1);
            if (alpha >= 0) {
                setFeatureDrawableAlpha(featureId, alpha);
            }
        }
    }
    protected ViewGroup generateLayout(DecorView decor) {
        final Context c = getContext();
        TypedArray a = getWindowStyle();
        if (false) {
            System.out.println("From style:");
            String s = "Attrs:";
            for (int i = 0; i < com.android.internal.R.styleable.Window.length; i++) {
                s = s + " " + Integer.toHexString(com.android.internal.R.styleable.Window[i]) + "="
                        + a.getString(i);
            }
            System.out.println(s);
        }
        mIsFloating = a.getBoolean(com.android.internal.R.styleable.Window_windowIsFloating, false);
        int flagsToUpdate = (FLAG_LAYOUT_IN_SCREEN|FLAG_LAYOUT_INSET_DECOR)
                & (~getForcedWindowFlags());
        if (mIsFloating) {
            setLayout(WRAP_CONTENT, WRAP_CONTENT);
            setFlags(0, flagsToUpdate);
            WindowManager.LayoutParams params = getAttributes();
            TypedArray attrs = c.obtainStyledAttributes(
                    com.android.internal.R.styleable.Theme);
            params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            params.dimAmount = attrs.getFloat(
                    android.R.styleable.Theme_backgroundDimAmount, 0.5f);
            attrs.recycle();
        } else {
            setFlags(FLAG_LAYOUT_IN_SCREEN|FLAG_LAYOUT_INSET_DECOR, flagsToUpdate);
        }
        if (a.getBoolean(com.android.internal.R.styleable.Window_windowNoTitle, false)) {
            requestFeature(FEATURE_NO_TITLE);
        }
        if (a.getBoolean(com.android.internal.R.styleable.Window_windowFullscreen, false)) {
            setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN&(~getForcedWindowFlags()));
        }
        if (getContainer() == null) {
            if (mBackgroundDrawable == null) {
                if (mBackgroundResource == 0) {
                    mBackgroundResource = a.getResourceId(
                            com.android.internal.R.styleable.Window_windowBackground, 0);
                }
                if (mFrameResource == 0) {
                    mFrameResource = a.getResourceId(com.android.internal.R.styleable.Window_windowFrame, 0);
                }
                if (false) {
                    System.out.println("Background: "
                            + Integer.toHexString(mBackgroundResource) + " Frame: "
                            + Integer.toHexString(mFrameResource));
                }
            }
            mTextColor = a.getColor(com.android.internal.R.styleable.Window_textColor, 0xFF000000);
        }
        int layoutResource;
        int features = getLocalFeatures();
        if ((features & ((1 << FEATURE_LEFT_ICON) | (1 << FEATURE_RIGHT_ICON))) != 0) {
            if (mIsFloating) {
                layoutResource = com.android.internal.R.layout.dialog_title_icons;
            } else {
                layoutResource = com.android.internal.R.layout.screen_title_icons;
            }
        } else if ((features & ((1 << FEATURE_PROGRESS) | (1 << FEATURE_INDETERMINATE_PROGRESS))) != 0) {
            layoutResource = com.android.internal.R.layout.screen_progress;
        } else if ((features & (1 << FEATURE_CUSTOM_TITLE)) != 0) {
            if (mIsFloating) {
                layoutResource = com.android.internal.R.layout.dialog_custom_title;
            } else {
                layoutResource = com.android.internal.R.layout.screen_custom_title;
            }
        } else if ((features & (1 << FEATURE_NO_TITLE)) == 0) {
            if (mIsFloating) {
                layoutResource = com.android.internal.R.layout.dialog_title;
            } else {
                layoutResource = com.android.internal.R.layout.screen_title;
            }
        } else {
            layoutResource = com.android.internal.R.layout.screen_simple;
        }
        mDecor.startChanging();
        View in = mLayoutInflater.inflate(layoutResource, null);
        decor.addView(in, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        ViewGroup contentParent = (ViewGroup)findViewById(ID_ANDROID_CONTENT);
        if (contentParent == null) {
            throw new RuntimeException("Window couldn't find content container view");
        }
        if ((features & (1 << FEATURE_INDETERMINATE_PROGRESS)) != 0) {
            ProgressBar progress = getCircularProgressBar(false);
            if (progress != null) {
                progress.setIndeterminate(true);
            }
        }
        if (getContainer() == null) {
            Drawable drawable = mBackgroundDrawable;
            if (mBackgroundResource != 0) {
                drawable = getContext().getResources().getDrawable(mBackgroundResource);
            }
            mDecor.setWindowBackground(drawable);
            drawable = null;
            if (mFrameResource != 0) {
                drawable = getContext().getResources().getDrawable(mFrameResource);
            }
            mDecor.setWindowFrame(drawable);
            if (mTitleColor == 0) {
                mTitleColor = mTextColor;
            }
            if (mTitle != null) {
                setTitle(mTitle);
            }
            setTitleColor(mTitleColor);
        }
        mDecor.finishChanging();
        return contentParent;
    }
    private void installDecor() {
        if (mDecor == null) {
            mDecor = generateDecor();
            mDecor.setIsRootNamespace(true);
        }
        if (mContentParent == null) {
            mContentParent = generateLayout(mDecor);
            mTitleView = (TextView)findViewById(com.android.internal.R.id.title);
            if (mTitleView != null) {
                if ((getLocalFeatures() & (1 << FEATURE_NO_TITLE)) != 0) {
                    View titleContainer = findViewById(com.android.internal.R.id.title_container);
                    if (titleContainer != null) {
                        titleContainer.setVisibility(View.GONE);
                    } else {
                        mTitleView.setVisibility(View.GONE);
                    }
                    if (mContentParent instanceof FrameLayout) {
                        ((FrameLayout)mContentParent).setForeground(null);
                    }
                } else {
                    mTitleView.setText(mTitle);
                }
            }
        }
    }
    private Drawable loadImageURI(Uri uri) {
        try {
            return Drawable.createFromStream(
                    getContext().getContentResolver().openInputStream(uri), null);
        } catch (Exception e) {
            Log.w(TAG, "Unable to open content: " + uri);
        }
        return null;
    }
    private DrawableFeatureState getDrawableState(int featureId, boolean required) {
        if ((getFeatures() & (1 << featureId)) == 0) {
            if (!required) {
                return null;
            }
            throw new RuntimeException("The feature has not been requested");
        }
        DrawableFeatureState[] ar;
        if ((ar = mDrawables) == null || ar.length <= featureId) {
            DrawableFeatureState[] nar = new DrawableFeatureState[featureId + 1];
            if (ar != null) {
                System.arraycopy(ar, 0, nar, 0, ar.length);
            }
            mDrawables = ar = nar;
        }
        DrawableFeatureState st = ar[featureId];
        if (st == null) {
            ar[featureId] = st = new DrawableFeatureState(featureId);
        }
        return st;
    }
    private PanelFeatureState getPanelState(int featureId, boolean required) {
        return getPanelState(featureId, required, null);
    }
    private PanelFeatureState getPanelState(int featureId, boolean required,
            PanelFeatureState convertPanelState) {
        if ((getFeatures() & (1 << featureId)) == 0) {
            if (!required) {
                return null;
            }
            throw new RuntimeException("The feature has not been requested");
        }
        PanelFeatureState[] ar;
        if ((ar = mPanels) == null || ar.length <= featureId) {
            PanelFeatureState[] nar = new PanelFeatureState[featureId + 1];
            if (ar != null) {
                System.arraycopy(ar, 0, nar, 0, ar.length);
            }
            mPanels = ar = nar;
        }
        PanelFeatureState st = ar[featureId];
        if (st == null) {
            ar[featureId] = st = (convertPanelState != null)
                    ? convertPanelState
                    : new PanelFeatureState(featureId);
        }
        return st;
    }
    @Override
    public final void setChildDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        st.child = drawable;
        updateDrawable(featureId, st, false);
    }
    @Override
    public final void setChildInt(int featureId, int value) {
        updateInt(featureId, value, false);
    }
    @Override
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        PanelFeatureState st = getPanelState(FEATURE_OPTIONS_PANEL, true);
        return st.menu != null && st.menu.isShortcutKey(keyCode, event);
    }
    private void updateDrawable(int featureId, DrawableFeatureState st, boolean fromResume) {
        if (mContentParent == null) {
            return;
        }
        final int featureMask = 1 << featureId;
        if ((getFeatures() & featureMask) == 0 && !fromResume) {
            return;
        }
        Drawable drawable = null;
        if (st != null) {
            drawable = st.child;
            if (drawable == null)
                drawable = st.local;
            if (drawable == null)
                drawable = st.def;
        }
        if ((getLocalFeatures() & featureMask) == 0) {
            if (getContainer() != null) {
                if (isActive() || fromResume) {
                    getContainer().setChildDrawable(featureId, drawable);
                }
            }
        } else if (st != null && (st.cur != drawable || st.curAlpha != st.alpha)) {
            st.cur = drawable;
            st.curAlpha = st.alpha;
            onDrawableChanged(featureId, drawable, st.alpha);
        }
    }
    private void updateInt(int featureId, int value, boolean fromResume) {
        if (mContentParent == null) {
            return;
        }
        final int featureMask = 1 << featureId;
        if ((getFeatures() & featureMask) == 0 && !fromResume) {
            return;
        }
        if ((getLocalFeatures() & featureMask) == 0) {
            if (getContainer() != null) {
                getContainer().setChildInt(featureId, value);
            }
        } else {
            onIntChanged(featureId, value);
        }
    }
    private ImageView getLeftIconView() {
        if (mLeftIconView != null) {
            return mLeftIconView;
        }
        if (mContentParent == null) {
            installDecor();
        }
        return (mLeftIconView = (ImageView)findViewById(com.android.internal.R.id.left_icon));
    }
    private ProgressBar getCircularProgressBar(boolean shouldInstallDecor) {
        if (mCircularProgressBar != null) {
            return mCircularProgressBar;
        }
        if (mContentParent == null && shouldInstallDecor) {
            installDecor();
        }
        mCircularProgressBar = (ProgressBar)findViewById(com.android.internal.R.id.progress_circular);
        mCircularProgressBar.setVisibility(View.INVISIBLE);
        return mCircularProgressBar;
    }
    private ProgressBar getHorizontalProgressBar(boolean shouldInstallDecor) {
        if (mHorizontalProgressBar != null) {
            return mHorizontalProgressBar;
        }
        if (mContentParent == null && shouldInstallDecor) {
            installDecor();
        }
        mHorizontalProgressBar = (ProgressBar)findViewById(com.android.internal.R.id.progress_horizontal);
        mHorizontalProgressBar.setVisibility(View.INVISIBLE);
        return mHorizontalProgressBar;
    }
    private ImageView getRightIconView() {
        if (mRightIconView != null) {
            return mRightIconView;
        }
        if (mContentParent == null) {
            installDecor();
        }
        return (mRightIconView = (ImageView)findViewById(com.android.internal.R.id.right_icon));
    }
    private void callOnPanelClosed(int featureId, PanelFeatureState panel, Menu menu) {
        final Callback cb = getCallback();
        if (cb == null)
            return;
        if (menu == null) {
            if (panel == null) {
                if ((featureId >= 0) && (featureId < mPanels.length)) {
                    panel = mPanels[featureId];
                }
            }
            if (panel != null) {
                menu = panel.menu;
            }
        }
        if ((panel != null) && (!panel.isOpen))
            return;
        cb.onPanelClosed(featureId, menu);
    }
    private boolean launchDefaultSearch() {
        final Callback cb = getCallback();
        if (cb == null) {
            return false;
        } else {
            return cb.onSearchRequested();
        }
    }
    @Override
    public void setVolumeControlStream(int streamType) {
        mVolumeControlStreamType = streamType;
    }
    @Override
    public int getVolumeControlStream() {
        return mVolumeControlStreamType;
    }
    private static final class DrawableFeatureState {
        DrawableFeatureState(int _featureId) {
            featureId = _featureId;
        }
        final int featureId;
        int resid;
        Uri uri;
        Drawable local;
        Drawable child;
        Drawable def;
        Drawable cur;
        int alpha = 255;
        int curAlpha = 255;
    }
    private static final class PanelFeatureState {
        int featureId;
        int background;
        int fullBackground;
        int gravity;
        int x;
        int y;
        int windowAnimations;
        DecorView decorView;
        View createdPanelView;
        View shownPanelView;
        Menu menu;
        boolean isPrepared;
        boolean isHandled;
        boolean isOpen;
        boolean isInExpandedMode;
        public boolean qwertyMode;
        boolean refreshDecorView;
        Bundle frozenMenuState;
        PanelFeatureState(int featureId) {
            this.featureId = featureId;
            refreshDecorView = false;
        }
        void setStyle(Context context) {
            TypedArray a = context.obtainStyledAttributes(com.android.internal.R.styleable.Theme);
            background = a.getResourceId(
                    com.android.internal.R.styleable.Theme_panelBackground, 0);
            fullBackground = a.getResourceId(
                    com.android.internal.R.styleable.Theme_panelFullBackground, 0);
            windowAnimations = a.getResourceId(
                    com.android.internal.R.styleable.Theme_windowAnimationStyle, 0);
            a.recycle();
        }
        void setMenu(Menu menu) {
            this.menu = menu;
            if (frozenMenuState != null) {
                ((MenuBuilder) menu).restoreHierarchyState(frozenMenuState);
                frozenMenuState = null;
            }
        }
        Parcelable onSaveInstanceState() {
            SavedState savedState = new SavedState();
            savedState.featureId = featureId;
            savedState.isOpen = isOpen;
            savedState.isInExpandedMode = isInExpandedMode;
            if (menu != null) {
                savedState.menuState = new Bundle();
                ((MenuBuilder) menu).saveHierarchyState(savedState.menuState);
            }
            return savedState;
        }
        void onRestoreInstanceState(Parcelable state) {
            SavedState savedState = (SavedState) state;
            featureId = savedState.featureId;
            isOpen = savedState.isOpen;
            isInExpandedMode = savedState.isInExpandedMode;
            frozenMenuState = savedState.menuState;
            menu = null;
            createdPanelView = null;
            shownPanelView = null;
            decorView = null;
        }
        private static class SavedState implements Parcelable {
            int featureId;
            boolean isOpen;
            boolean isInExpandedMode;
            Bundle menuState;
            public int describeContents() {
                return 0;
            }
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(featureId);
                dest.writeInt(isOpen ? 1 : 0);
                dest.writeInt(isInExpandedMode ? 1 : 0);
                if (isOpen) {
                    dest.writeBundle(menuState);
                }
            }
            private static SavedState readFromParcel(Parcel source) {
                SavedState savedState = new SavedState();
                savedState.featureId = source.readInt();
                savedState.isOpen = source.readInt() == 1;
                savedState.isInExpandedMode = source.readInt() == 1;
                if (savedState.isOpen) {
                    savedState.menuState = source.readBundle();
                }
                return savedState;
            }
            public static final Parcelable.Creator<SavedState> CREATOR
                    = new Parcelable.Creator<SavedState>() {
                public SavedState createFromParcel(Parcel in) {
                    return readFromParcel(in);
                }
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
        }
    }
    private final class ContextMenuCallback implements MenuBuilder.Callback {
        private int mFeatureId;
        public ContextMenuCallback(int featureId) {
            mFeatureId = featureId;
        }
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (allMenusAreClosing) {
                Callback callback = getCallback();
                if (callback != null) callback.onPanelClosed(mFeatureId, menu);
                if (menu == mContextMenu) {
                    closeContextMenu();
                }
            }
        }
        public void onCloseSubMenu(SubMenuBuilder menu) {
            Callback callback = getCallback();
            if (callback != null) callback.onPanelClosed(mFeatureId, menu.getRootMenu());
        }
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            Callback callback = getCallback();
            return (callback != null) && callback.onMenuItemSelected(mFeatureId, item);
        }
        public void onMenuModeChange(MenuBuilder menu) {
        }
        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            subMenu.setCallback(this);
            new MenuDialogHelper(subMenu).show(null);
            return true;
        }
    }
}
