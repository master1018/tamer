final class WindowLeaked extends AndroidRuntimeException {
    public WindowLeaked(String msg) {
        super(msg);
    }
}
public class WindowManagerImpl implements WindowManager {
    public static final int RELAYOUT_IN_TOUCH_MODE = 0x1;
    public static final int RELAYOUT_FIRST_TIME = 0x2;
    public static final int ADD_FLAG_APP_VISIBLE = 0x2;
    public static final int ADD_FLAG_IN_TOUCH_MODE = RELAYOUT_IN_TOUCH_MODE;
    public static final int ADD_OKAY = 0;
    public static final int ADD_BAD_APP_TOKEN = -1;
    public static final int ADD_BAD_SUBWINDOW_TOKEN = -2;
    public static final int ADD_NOT_APP_TOKEN = -3;
    public static final int ADD_APP_EXITING = -4;
    public static final int ADD_DUPLICATE_ADD = -5;
    public static final int ADD_STARTING_NOT_NEEDED = -6;
    public static final int ADD_MULTIPLE_SINGLETON = -7;
    public static final int ADD_PERMISSION_DENIED = -8;
    public static WindowManagerImpl getDefault()
    {
        return mWindowManager;
    }
    public void addView(View view)
    {
        addView(view, new WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION, 0, PixelFormat.OPAQUE));
    }
    public void addView(View view, ViewGroup.LayoutParams params)
    {
        addView(view, params, false);
    }
    public void addViewNesting(View view, ViewGroup.LayoutParams params)
    {
        addView(view, params, false);
    }
    private void addView(View view, ViewGroup.LayoutParams params, boolean nest)
    {
        if (Config.LOGV) Log.v("WindowManager", "addView view=" + view);
        if (!(params instanceof WindowManager.LayoutParams)) {
            throw new IllegalArgumentException(
                    "Params must be WindowManager.LayoutParams");
        }
        final WindowManager.LayoutParams wparams
                = (WindowManager.LayoutParams)params;
        ViewRoot root;
        View panelParentView = null;
        synchronized (this) {
            int index = findViewLocked(view, false);
            if (index >= 0) {
                if (!nest) {
                    throw new IllegalStateException("View " + view
                            + " has already been added to the window manager.");
                }
                root = mRoots[index];
                root.mAddNesting++;
                view.setLayoutParams(wparams);
                root.setLayoutParams(wparams, true);
                return;
            }
            if (wparams.type >= WindowManager.LayoutParams.FIRST_SUB_WINDOW &&
                    wparams.type <= WindowManager.LayoutParams.LAST_SUB_WINDOW) {
                final int count = mViews != null ? mViews.length : 0;
                for (int i=0; i<count; i++) {
                    if (mRoots[i].mWindow.asBinder() == wparams.token) {
                        panelParentView = mViews[i];
                    }
                }
            }
            root = new ViewRoot(view.getContext());
            root.mAddNesting = 1;
            view.setLayoutParams(wparams);
            if (mViews == null) {
                index = 1;
                mViews = new View[1];
                mRoots = new ViewRoot[1];
                mParams = new WindowManager.LayoutParams[1];
            } else {
                index = mViews.length + 1;
                Object[] old = mViews;
                mViews = new View[index];
                System.arraycopy(old, 0, mViews, 0, index-1);
                old = mRoots;
                mRoots = new ViewRoot[index];
                System.arraycopy(old, 0, mRoots, 0, index-1);
                old = mParams;
                mParams = new WindowManager.LayoutParams[index];
                System.arraycopy(old, 0, mParams, 0, index-1);
            }
            index--;
            mViews[index] = view;
            mRoots[index] = root;
            mParams[index] = wparams;
        }
        root.setView(view, wparams, panelParentView);
    }
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        if (!(params instanceof WindowManager.LayoutParams)) {
            throw new IllegalArgumentException("Params must be WindowManager.LayoutParams");
        }
        final WindowManager.LayoutParams wparams
                = (WindowManager.LayoutParams)params;
        view.setLayoutParams(wparams);
        synchronized (this) {
            int index = findViewLocked(view, true);
            ViewRoot root = mRoots[index];
            mParams[index] = wparams;
            root.setLayoutParams(wparams, false);
        }
    }
    public void removeView(View view) {
        synchronized (this) {
            int index = findViewLocked(view, true);
            View curView = removeViewLocked(index);
            if (curView == view) {
                return;
            }
            throw new IllegalStateException("Calling with view " + view
                    + " but the ViewRoot is attached to " + curView);
        }
    }
    public void removeViewImmediate(View view) {
        synchronized (this) {
            int index = findViewLocked(view, true);
            ViewRoot root = mRoots[index];
            View curView = root.getView();
            root.mAddNesting = 0;
            root.die(true);
            finishRemoveViewLocked(curView, index);
            if (curView == view) {
                return;
            }
            throw new IllegalStateException("Calling with view " + view
                    + " but the ViewRoot is attached to " + curView);
        }
    }
    View removeViewLocked(int index) {
        ViewRoot root = mRoots[index];
        View view = root.getView();
        root.mAddNesting--;
        if (root.mAddNesting > 0) {
            return view;
        }
        InputMethodManager imm = InputMethodManager.getInstance(view.getContext());
        if (imm != null) {
            imm.windowDismissed(mViews[index].getWindowToken());
        }
        root.die(false);
        finishRemoveViewLocked(view, index);
        return view;
    }
    void finishRemoveViewLocked(View view, int index) {
        final int count = mViews.length;
        View[] tmpViews = new View[count-1];
        removeItem(tmpViews, mViews, index);
        mViews = tmpViews;
        ViewRoot[] tmpRoots = new ViewRoot[count-1];
        removeItem(tmpRoots, mRoots, index);
        mRoots = tmpRoots;
        WindowManager.LayoutParams[] tmpParams
                = new WindowManager.LayoutParams[count-1];
        removeItem(tmpParams, mParams, index);
        mParams = tmpParams;
        view.assignParent(null);
    }
    public void closeAll(IBinder token, String who, String what) {
        synchronized (this) {
            if (mViews == null)
                return;
            int count = mViews.length;
            for (int i=0; i<count; i++) {
                if (token == null || mParams[i].token == token) {
                    ViewRoot root = mRoots[i];
                    root.mAddNesting = 1;
                    if (who != null) {
                        WindowLeaked leak = new WindowLeaked(
                                what + " " + who + " has leaked window "
                                + root.getView() + " that was originally added here");
                        leak.setStackTrace(root.getLocation().getStackTrace());
                        Log.e("WindowManager", leak.getMessage(), leak);
                    }
                    removeViewLocked(i);
                    i--;
                    count--;
                }
            }
        }
    }
    public WindowManager.LayoutParams getRootViewLayoutParameter(View view) {
        ViewParent vp = view.getParent();
        while (vp != null && !(vp instanceof ViewRoot)) {
            vp = vp.getParent();
        }
        if (vp == null) return null;
        ViewRoot vr = (ViewRoot)vp;
        int N = mRoots.length;
        for (int i = 0; i < N; ++i) {
            if (mRoots[i] == vr) {
                return mParams[i];
            }
        }
        return null;
    }
    public void closeAll() {
        closeAll(null, null, null);
    }
    public Display getDefaultDisplay() {
        return new Display(Display.DEFAULT_DISPLAY);
    }
    private View[] mViews;
    private ViewRoot[] mRoots;
    private WindowManager.LayoutParams[] mParams;
    private static void removeItem(Object[] dst, Object[] src, int index)
    {
        if (dst.length > 0) {
            if (index > 0) {
                System.arraycopy(src, 0, dst, 0, index);
            }
            if (index < dst.length) {
                System.arraycopy(src, index+1, dst, index, src.length-index-1);
            }
        }
    }
    private int findViewLocked(View view, boolean required)
    {
        synchronized (this) {
            final int count = mViews != null ? mViews.length : 0;
            for (int i=0; i<count; i++) {
                if (mViews[i] == view) {
                    return i;
                }
            }
            if (required) {
                throw new IllegalArgumentException(
                        "View not attached to window manager");
            }
            return -1;
        }
    }
    private static WindowManagerImpl mWindowManager = new WindowManagerImpl();
}
