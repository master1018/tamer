@TestTargetClass(Window.class)
public class WindowTest extends ActivityInstrumentationTestCase2<WindowStubActivity> {
    private Window mWindow;
    private Context mContext;
    private Instrumentation mInstrumentation;
    private WindowStubActivity mActivity;
    private static final int VIEWGROUP_LAYOUT_HEIGHT = 100;
    private static final int VIEWGROUP_LAYOUT_WIDTH = 200;
    public WindowTest() {
        super("com.android.cts.stub", WindowStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mContext = mInstrumentation.getContext();
        mActivity = getActivity();
        mWindow = mActivity.getWindow();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.setFlagFalse();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Window",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getContext",
            args = {}
        )
    })
    public void testConstructor() throws Exception {
        mWindow = new MockWindow(mContext);
        assertSame(mContext, mWindow.getContext());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addFlags",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearFlags",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFlags",
            args = {int.class, int.class}
        )
    })
   public void testOpFlags() throws Exception {
        mWindow = new MockWindow(mContext);
        final WindowManager.LayoutParams attrs = mWindow.getAttributes();
        assertEquals(0, attrs.flags);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        assertEquals(WindowManager.LayoutParams.FLAG_FULLSCREEN, attrs.flags);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        assertEquals(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_DITHER, attrs.flags);
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        assertEquals(WindowManager.LayoutParams.FLAG_DITHER, attrs.flags);
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_DITHER);
        assertEquals(0, attrs.flags);
        MockWindowCallback callback = new MockWindowCallback();
        mWindow.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        assertEquals(WindowManager.LayoutParams.FLAG_FULLSCREEN, attrs.flags);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mWindow.clearFlags(WindowManager.LayoutParams.FLAG_DITHER);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findViewById",
        args = {int.class}
    )
    public void testFindViewById() throws Exception {
        TextView v = (TextView) mWindow.findViewById(R.id.listview_window);
        assertNotNull(v);
        assertEquals(R.id.listview_window, v.getId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAttributes",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCallback",
            args = {android.view.Window.Callback.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCallback",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAttributes",
            args = {android.view.WindowManager.LayoutParams.class}
        )
    })
    public void testAccessAttributes() throws Exception {
        mWindow = new MockWindow(mContext);
        WindowManager.LayoutParams attr = mWindow.getAttributes();
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, attr.width);
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, attr.height);
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION, attr.type);
        assertEquals(PixelFormat.OPAQUE, attr.format);
        int width = 200;
        int height = 300;
        WindowManager.LayoutParams param = new WindowManager.LayoutParams(width, height,
                WindowManager.LayoutParams.TYPE_BASE_APPLICATION,
                WindowManager.LayoutParams.FLAG_DITHER, PixelFormat.RGBA_8888);
        MockWindowCallback callback = new MockWindowCallback();
        mWindow.setCallback(callback);
        assertSame(callback, mWindow.getCallback());
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        mWindow.setAttributes(param);
        attr = mWindow.getAttributes();
        assertEquals(width, attr.width);
        assertEquals(height, attr.height);
        assertEquals(WindowManager.LayoutParams.TYPE_BASE_APPLICATION, attr.type);
        assertEquals(PixelFormat.RGBA_8888, attr.format);
        assertEquals(WindowManager.LayoutParams.FLAG_DITHER, attr.flags);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getContainer",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContainer",
            args = {android.view.Window.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasChildren",
            args = {}
        )
    })
    public void testAccessContainer() throws Exception {
        mWindow = new MockWindow(mContext);
        assertNull(mWindow.getContainer());
        assertFalse(mWindow.hasChildren());
        MockWindow container = new MockWindow(mContext);
        mWindow.setContainer(container);
        assertSame(container, mWindow.getContainer());
        assertTrue(container.hasChildren());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addContentView",
            args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLayoutInflater",
            args = {}
        )
    })
    public void testAddContentView() throws Throwable {
        final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(VIEWGROUP_LAYOUT_WIDTH,
                VIEWGROUP_LAYOUT_HEIGHT);
        final LayoutInflater inflater = mActivity.getLayoutInflater();
        runTestOnUiThread(new Runnable() {
            public void run() {
                TextView addedView = (TextView) mWindow.findViewById(R.id.listview_addwindow);
                assertNull(addedView);
                mWindow.addContentView(inflater.inflate(R.layout.windowstub_addlayout, null), lp);
                TextView view = (TextView) mWindow.findViewById(R.id.listview_window);
                addedView = (TextView) mWindow.findViewById(R.id.listview_addwindow);
                assertNotNull(view);
                assertNotNull(addedView);
                assertEquals(R.id.listview_window, view.getId());
                assertEquals(R.id.listview_addwindow, addedView.getId());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "closeAllPanels",
        args = {}
    )
    public void testCloseAllPanels() throws Throwable {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "closePanel",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openPanel",
            args = {int.class, android.view.KeyEvent.class}
        )
    })
    public void testOpPanel() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWindow.openPanel(Window.FEATURE_OPTIONS_PANEL, null);
                assertTrue(mActivity.isOnCreateOptionsMenuCalled());
                mWindow.closePanel(Window.FEATURE_OPTIONS_PANEL);
                assertTrue(mActivity.isOnOptionsMenuClosedCalled());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "togglePanel",
        args = {int.class, android.view.KeyEvent.class}
    )
    public void testTogglePanelClose() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWindow.openPanel(Window.FEATURE_OPTIONS_PANEL, null);
                assertTrue(mActivity.isOnCreateOptionsMenuCalled());
                mWindow.togglePanel(Window.FEATURE_OPTIONS_PANEL, null);
                assertTrue(mActivity.isOnOptionsMenuClosedCalled());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "togglePanel",
        args = {int.class, android.view.KeyEvent.class}
    )
    public void testTogglePanelOpen() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWindow.togglePanel(Window.FEATURE_OPTIONS_PANEL, null);
                assertTrue(mActivity.isOnCreateOptionsMenuCalled());
                mWindow.closePanel(Window.FEATURE_OPTIONS_PANEL);
                assertTrue(mActivity.isOnOptionsMenuClosedCalled());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCurrentFocus",
        args = {}
    )
    public void testGetCurrentFocus() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                TextView v = (TextView) mWindow.findViewById(R.id.listview_window);
                v.clearFocus();
                assertNull(mWindow.getCurrentFocus());
                v.setFocusable(true);
                assertTrue(v.isFocusable());
                assertTrue(v.requestFocus());
                View focus = mWindow.getCurrentFocus();
                assertNotNull(focus);
                assertEquals(R.id.listview_window, focus.getId());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDecorView",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "peekDecorView",
            args = {}
        )
    })
    public void testDecorView() throws Exception {
        mInstrumentation.waitForIdleSync();
        View decor = mWindow.getDecorView();
        assertNotNull(decor);
        checkDecorView(decor);
        decor = mWindow.peekDecorView();
        if (decor != null) {
            checkDecorView(decor);
        }
    }
    private void checkDecorView(View decor) {
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        assertEquals(screenWidth, decor.getWidth());
        assertEquals(screenHeight, decor.getHeight());
        assertSame(mWindow.getContext(), decor.getContext());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVolumeControlStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVolumeControlStream",
            args = {int.class}
        )
    })
    public void testAccessVolumeControlStream() throws Exception {
        assertEquals(AudioManager.USE_DEFAULT_STREAM_TYPE, mWindow.getVolumeControlStream());
        mWindow.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        assertEquals(AudioManager.STREAM_MUSIC, mWindow.getVolumeControlStream());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "getWindowManager",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "setWindowManager",
            args = {android.view.WindowManager.class, android.os.IBinder.class,
                    java.lang.String.class}
        )
    })
    public void testAccessWindowManager() throws Exception {
        mWindow = new MockWindow(getActivity());
        WindowManager expected = (WindowManager) getActivity().getSystemService(
                Context.WINDOW_SERVICE);
        assertNull(mWindow.getWindowManager());
        mWindow.setWindowManager(expected, null, getName());
        assertNotNull(mWindow.getWindowManager());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "the windowStyle is obtained from com.android.internal.R.styleable.Window whose"
              + " details are invisible for user",
        method = "getWindowStyle",
        args = {}
    )
    public void testGetWindowStyle() throws Exception {
        mWindow = new MockWindow(mContext);
        final TypedArray windowStyle = mWindow.getWindowStyle();
        assertNotNull(windowStyle);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isActive",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "makeActive",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onActive",
            args = {}
        )
    })
    public void testIsActive() throws Exception {
        MockWindow window = new MockWindow(mContext);
        assertFalse(window.isActive());
        window.makeActive();
        assertTrue(window.isActive());
        assertTrue(window.mIsOnActiveCalled);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Now can only get the unfloating status",
        method = "isFloating",
        args = {}
    )
    public void testIsFloating() throws Exception {
        assertFalse(mWindow.isFloating());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "We set 'q' & 'a' key as shortcut key in WindowStubActivity.java,"
              + " other keys are not shortcut key",
        method = "isShortcutKey",
        args = {int.class, android.view.KeyEvent.class}
    )
    public void testIsShortcutKey() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.openOptionsMenu();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(mWindow.isShortcutKey(KeyEvent.KEYCODE_Q, new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_Q)));
        assertFalse(mWindow.isShortcutKey(KeyEvent.KEYCODE_F, new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_F)));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "performContextMenuIdentifierAction",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "performPanelIdentifierAction",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "performPanelShortcut",
            args = {int.class, int.class, android.view.KeyEvent.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc, hard to get known about its"
        + " functionality")
    public void testPerformMethods() throws Exception {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
             method = "restoreHierarchyState",
            args = {android.os.Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "saveHierarchyState",
            args = {}
        )
    })
    public void testKeepHierarchyState() throws Exception {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBackgroundDrawable",
            args = {android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBackgroundDrawableResource",
            args = {int.class}
        )
    })
    public void testSetBackgroundDrawable() throws Throwable {
        View decor = mWindow.getDecorView();
        assertEquals(PixelFormat.OPAQUE, decor.getBackground().getOpacity());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWindow.setBackgroundDrawableResource(R.drawable.faces);
            }
        });
        mInstrumentation.waitForIdleSync();
        runTestOnUiThread(new Runnable() {
            public void run() {
                ColorDrawable drawable = new ColorDrawable(0);
                mWindow.setBackgroundDrawable(drawable);
            }
        });
        mInstrumentation.waitForIdleSync();
        decor = mWindow.getDecorView();
        assertEquals(PixelFormat.TRANSPARENT, decor.getBackground().getOpacity());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWindow.setBackgroundDrawable(null);
            }
        });
        mInstrumentation.waitForIdleSync();
        decor = mWindow.getDecorView();
        assertNull(decor.getBackground());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setChildDrawable",
            args = {int.class, android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setChildInt",
            args = {int.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc, hard to get known about its"
        + " functionality")
    public void testSetChild() throws Exception {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setContentView",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "setContentView",
            args = {android.view.View.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "setContentView",
            args = {android.view.View.class, android.view.ViewGroup.LayoutParams.class}
        )
    })
    public void testSetContentView() throws Throwable {
        final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(VIEWGROUP_LAYOUT_WIDTH,
                VIEWGROUP_LAYOUT_HEIGHT);
        final LayoutInflater inflate = mActivity.getLayoutInflater();
        runTestOnUiThread(new Runnable() {
            public void run() {
                TextView view;
                View setView;
                mWindow.setContentView(R.layout.windowstub_layout);
                view = (TextView) mWindow.findViewById(R.id.listview_window);
                assertNotNull(view);
                assertEquals(R.id.listview_window, view.getId());
                setView = inflate.inflate(R.layout.windowstub_addlayout, null);
                mWindow.setContentView(setView);
                view = (TextView) mWindow.findViewById(R.id.listview_addwindow);
                assertNotNull(view);
                assertEquals(R.id.listview_addwindow, view.getId());
                setView = inflate.inflate(R.layout.windowstub_layout, null);
                mWindow.setContentView(setView, lp);
                assertEquals(VIEWGROUP_LAYOUT_WIDTH, setView.getLayoutParams().width);
                assertEquals(VIEWGROUP_LAYOUT_HEIGHT, setView.getLayoutParams().height);
                view = (TextView) mWindow.findViewById(R.id.listview_window);
                assertNotNull(view);
                assertEquals(R.id.listview_window, view.getId());
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setFeatureDrawable",
            args = {int.class, android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setFeatureDrawableAlpha",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setFeatureDrawableResource",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setFeatureDrawableUri",
            args = {int.class, android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setFeatureInt",
            args = {int.class, int.class}
        )
    })
    public void testSetFeature() throws Throwable {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setTitle",
            args = {java.lang.CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "setTitleColor",
            args = {int.class}
        )
    })
    public void testSetTitle() throws Throwable {
        final String title = "Android Window Test";
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWindow.setTitle(title);
                mWindow.setTitleColor(Color.BLUE);
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "superDispatchTouchEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "superDispatchTrackballEvent",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "superDispatchKeyEvent",
            args = {android.view.KeyEvent.class}
        )
    })
    @ToBeFixed(bug = "1719667", explanation = "In javadoc, it is not recommended for developer"
        + " to call these methods, they are used by custom windows.")
    public void testSuperDispatchEvent() throws Exception {
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "takeKeyEvents",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1728604", explanation = "Clear all focused view, use takeKeyEvents(false)"
        + " It's expected that the activity can't process key events, But when we send a key,"
        + " Acitivity#onKeyDown is still called by system.")
    public void testTakeKeyEvents() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                View v = mWindow.findViewById(R.id.listview_window);
                v.clearFocus();
                assertNull(mWindow.getCurrentFocus());
                mWindow.takeKeyEvents(false);
            }
        });
        mInstrumentation.waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "Test onConfigurationChanged, because the Window#onConfigurationChanged is"
              + " abstract and we can't let system call our own MockWindow#onConfigurationChanged"
              + " and can't check if the system call this callback method",
        method = "onConfigurationChanged",
        args = {android.content.res.Configuration.class}
    )
    public void testOnConfigurationChanged() throws Exception {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "Because getFeatures is final protected, we can't get the request Features and"
              + " no way to check the request result.",
        method = "requestFeature",
        args = {int.class}
    )
    public void testRequestFeature() throws Exception {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDefaultWindowFormat",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFormat",
            args = {int.class}
        )
    })
    public void testSetDefaultWindowFormat() throws Exception {
        MockWindowCallback callback;
        MockWindow window = new MockWindow(mContext);
        window.setFormat(PixelFormat.OPAQUE);
        callback = new MockWindowCallback();
        window.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        window.setDefaultWindowFormat(PixelFormat.JPEG);
        assertEquals(PixelFormat.OPAQUE, window.getAttributes().format);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        window.setFormat(PixelFormat.UNKNOWN);
        callback = new MockWindowCallback();
        window.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        window.setDefaultWindowFormat(PixelFormat.JPEG);
        assertEquals(PixelFormat.JPEG, window.getAttributes().format);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Lack of getter to obtain the gravity of window manager positioned in window",
        method = "setGravity",
        args = {int.class}
    )
    public void testSetGravity() throws Exception {
        mWindow = new MockWindow(mContext);
        WindowManager.LayoutParams attrs = mWindow.getAttributes();
        assertEquals(0, attrs.gravity);
        MockWindowCallback callback = new MockWindowCallback();
        mWindow.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        mWindow.setGravity(Gravity.TOP);
        attrs = mWindow.getAttributes();
        assertEquals(Gravity.TOP, attrs.gravity);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setLayout",
        args = {int.class, int.class}
    )
    public void testSetLayout() throws Exception {
        mWindow = new MockWindow(mContext);
        WindowManager.LayoutParams attrs = mWindow.getAttributes();
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, attrs.width);
        assertEquals(WindowManager.LayoutParams.MATCH_PARENT, attrs.height);
        MockWindowCallback callback = new MockWindowCallback();
        mWindow.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        mWindow.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        attrs = mWindow.getAttributes();
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT, attrs.width);
        assertEquals(WindowManager.LayoutParams.WRAP_CONTENT, attrs.height);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setType",
        args = {int.class}
    )
    public void testSetType() throws Exception {
        mWindow = new MockWindow(mContext);
        WindowManager.LayoutParams attrs = mWindow.getAttributes();
        assertEquals(WindowManager.LayoutParams.TYPE_APPLICATION, attrs.type);
        MockWindowCallback callback = new MockWindowCallback();
        mWindow.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        mWindow.setType(WindowManager.LayoutParams.TYPE_BASE_APPLICATION);
        attrs = mWindow.getAttributes();
        assertEquals(WindowManager.LayoutParams.TYPE_BASE_APPLICATION, mWindow.getAttributes().type);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setSoftInputMode",
        args = {int.class}
    )
    public void testSetSoftInputMode() throws Exception {
        mWindow = new MockWindow(mContext);
        assertEquals(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED,
                mWindow.getAttributes().softInputMode);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        assertEquals(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE,
                mWindow.getAttributes().softInputMode);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        assertEquals(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE,
                mWindow.getAttributes().softInputMode);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setWindowAnimations",
        args = {int.class}
    )
    public void testSetWindowAnimations() throws Exception {
        mWindow = new MockWindow(mContext);
        MockWindowCallback callback = new MockWindowCallback();
        mWindow.setCallback(callback);
        assertFalse(callback.isOnWindowAttributesChangedCalled());
        mWindow.setWindowAnimations(R.anim.alpha);
        WindowManager.LayoutParams attrs = mWindow.getAttributes();
        assertEquals(R.anim.alpha, attrs.windowAnimations);
        assertTrue(callback.isOnWindowAttributesChangedCalled());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test getFeatures, protected final method, can't call it",
            method = "getFeatures",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test getLocalFeatures, protected final method, can't call it",
            method = "getLocalFeatures",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test getForcedWindowFlags, protected final method, can't call it",
            method = "getForcedWindowFlags",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test hasSoftInputMode, protected final method, can't call it",
            method = "hasSoftInputMode",
            args = {}
        )
    })
    public void testFinalMethod() throws Exception {
    }
    public class MockWindow extends Window {
        public boolean mIsOnConfigurationChangedCalled = false;
        public boolean mIsOnActiveCalled = false;
        public MockWindow(Context context) {
            super(context);
        }
        public boolean isFloating() {
            return false;
        }
        public void setContentView(int layoutResID) {
        }
        public void setContentView(View view) {
        }
        public void setContentView(View view, ViewGroup.LayoutParams params) {
        }
        public void addContentView(View view, ViewGroup.LayoutParams params) {
        }
        public View getCurrentFocus() {
            return null;
        }
        public LayoutInflater getLayoutInflater() {
            return null;
        }
        public void setTitle(CharSequence title) {
        }
        public void setTitleColor(int textColor) {
        }
        public void openPanel(int featureId, KeyEvent event) {
        }
        public void closePanel(int featureId) {
        }
        public void togglePanel(int featureId, KeyEvent event) {
        }
        public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
            return true;
        }
        public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
            return true;
        }
        public void closeAllPanels() {
        }
        public boolean performContextMenuIdentifierAction(int id, int flags) {
            return true;
        }
        public void onConfigurationChanged(Configuration newConfig) {
            mIsOnConfigurationChangedCalled = true;
        }
        public void setBackgroundDrawable(Drawable drawable) {
        }
        public void setFeatureDrawableResource(int featureId, int resId) {
        }
        public void setFeatureDrawableUri(int featureId, Uri uri) {
        }
        public void setFeatureDrawable(int featureId, Drawable drawable) {
        }
        public void setFeatureDrawableAlpha(int featureId, int alpha) {
        }
        public void setFeatureInt(int featureId, int value) {
        }
        public void takeKeyEvents(boolean get) {
        }
        public boolean superDispatchKeyEvent(KeyEvent event) {
            return true;
        }
        public boolean superDispatchTouchEvent(MotionEvent event) {
            return true;
        }
        public boolean superDispatchTrackballEvent(MotionEvent event) {
            return true;
        }
        public View getDecorView() {
            return null;
        }
        public View peekDecorView() {
            return null;
        }
        public Bundle saveHierarchyState() {
            return null;
        }
        public void restoreHierarchyState(Bundle savedInstanceState) {
        }
        protected void onActive() {
            mIsOnActiveCalled = true;
        }
        public void setChildDrawable(int featureId, Drawable drawable) {
        }
        public void setChildInt(int featureId, int value) {
        }
        public boolean isShortcutKey(int keyCode, KeyEvent event) {
            return false;
        }
        public void setVolumeControlStream(int streamType) {
        }
        public int getVolumeControlStream() {
            return 0;
        }
        public void setDefaultWindowFormatFake(int format) {
            super.setDefaultWindowFormat(format);
        }
        @Override
        public void setDefaultWindowFormat(int format) {
            super.setDefaultWindowFormat(format);
        }
    }
    private class MockWindowCallback implements Window.Callback {
        private boolean mIsOnWindowAttributesChangedCalled;
        private boolean mIsOnPanelClosedCalled;
        public boolean dispatchKeyEvent(KeyEvent event) {
            return true;
        }
        public boolean dispatchTouchEvent(MotionEvent event) {
            return true;
        }
        public boolean dispatchTrackballEvent(MotionEvent event) {
            return true;
        }
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            return true;
        }
        public View onCreatePanelView(int featureId) {
            return null;
        }
        public boolean onCreatePanelMenu(int featureId, Menu menu) {
            return false;
        }
        public boolean onPreparePanel(int featureId, View view, Menu menu) {
            return false;
        }
        public boolean onMenuOpened(int featureId, Menu menu) {
            return false;
        }
        public boolean onMenuItemSelected(int featureId, MenuItem item) {
            return true;
        }
        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
            mIsOnWindowAttributesChangedCalled = true;
        }
        public boolean isOnWindowAttributesChangedCalled() {
            return mIsOnWindowAttributesChangedCalled;
        }
        public void onContentChanged() {
        }
        public void onWindowFocusChanged(boolean hasFocus) {
        }
        public void onDetachedFromWindow() {
        }
        public void onAttachedToWindow() {
        }
        public void onPanelClosed(int featureId, Menu menu) {
            mIsOnPanelClosedCalled = true;
        }
        public boolean isOnPanelClosedCalled() {
            return mIsOnPanelClosedCalled;
        }
        public boolean onSearchRequested() {
            return false;
        }
    }
}
