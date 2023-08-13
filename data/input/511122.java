public final class InputMethodManager {
    static final boolean DEBUG = false;
    static final String TAG = "InputMethodManager";
    static final Object mInstanceSync = new Object();
    static InputMethodManager mInstance;
    final IInputMethodManager mService;
    final Looper mMainLooper;
    final H mH;
    final IInputContext mIInputContext;
    boolean mActive = false;
    boolean mHasBeenInactive = true;
    boolean mFullscreenMode;
    View mCurRootView;
    View mServedView;
    View mNextServedView;
    boolean mNextServedNeedsStart;
    boolean mServedConnecting;
    EditorInfo mCurrentTextBoxAttribute;
    InputConnection mServedInputConnection;
    CompletionInfo[] mCompletions;
    Rect mTmpCursorRect = new Rect();
    Rect mCursorRect = new Rect();
    int mCursorSelStart;
    int mCursorSelEnd;
    int mCursorCandStart;
    int mCursorCandEnd;
    int mBindSequence = -1;
    String mCurId;
    IInputMethodSession mCurMethod;
    static final int MSG_DUMP = 1;
    static final int MSG_BIND = 2;
    static final int MSG_UNBIND = 3;
    static final int MSG_SET_ACTIVE = 4;
    class H extends Handler {
        H(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DUMP: {
                    HandlerCaller.SomeArgs args = (HandlerCaller.SomeArgs)msg.obj;
                    try {
                        doDump((FileDescriptor)args.arg1,
                                (PrintWriter)args.arg2, (String[])args.arg3);
                    } catch (RuntimeException e) {
                        ((PrintWriter)args.arg2).println("Exception: " + e);
                    }
                    synchronized (args.arg4) {
                        ((CountDownLatch)args.arg4).countDown();
                    }
                    return;
                }
                case MSG_BIND: {
                    final InputBindResult res = (InputBindResult)msg.obj;
                    synchronized (mH) {
                        if (mBindSequence < 0 || mBindSequence != res.sequence) {
                            Log.w(TAG, "Ignoring onBind: cur seq=" + mBindSequence
                                    + ", given seq=" + res.sequence);
                            return;
                        }
                        mCurMethod = res.method;
                        mCurId = res.id;
                        mBindSequence = res.sequence;
                    }
                    startInputInner();
                    return;
                }
                case MSG_UNBIND: {
                    final int sequence = msg.arg1;
                    synchronized (mH) {
                        if (mBindSequence == sequence) {
                            if (false) {
                                if (mCurMethod != null && mCurrentTextBoxAttribute != null) {
                                    try {
                                        mCurMethod.finishInput();
                                    } catch (RemoteException e) {
                                        Log.w(TAG, "IME died: " + mCurId, e);
                                    }
                                }
                            }
                            clearBindingLocked();
                            if (mServedView != null && mServedView.isFocused()) {
                                mServedConnecting = true;
                            }
                        }
                        startInputInner();
                    }
                    return;
                }
                case MSG_SET_ACTIVE: {
                    final boolean active = msg.arg1 != 0;
                    synchronized (mH) {
                        mActive = active;
                        mFullscreenMode = false;
                        if (!active) {
                            mHasBeenInactive = true;
                            try {
                                mIInputContext.finishComposingText();
                            } catch (RemoteException e) {
                            }
                        }
                    }
                    return;
                }
            }
        }
    }
    class ControlledInputConnectionWrapper extends IInputConnectionWrapper {
        public ControlledInputConnectionWrapper(Looper mainLooper, InputConnection conn) {
            super(mainLooper, conn);
        }
        public boolean isActive() {
            return mActive;
        }
    }
    final IInputMethodClient.Stub mClient = new IInputMethodClient.Stub() {
        @Override protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
            CountDownLatch latch = new CountDownLatch(1);
            HandlerCaller.SomeArgs sargs = new HandlerCaller.SomeArgs();
            sargs.arg1 = fd;
            sargs.arg2 = fout;
            sargs.arg3 = args;
            sargs.arg4 = latch;
            mH.sendMessage(mH.obtainMessage(MSG_DUMP, sargs));
            try {
                if (!latch.await(5, TimeUnit.SECONDS)) {
                    fout.println("Timeout waiting for dump");
                }
            } catch (InterruptedException e) {
                fout.println("Interrupted waiting for dump");
            }
        }
        public void setUsingInputMethod(boolean state) {
        }
        public void onBindMethod(InputBindResult res) {
            mH.sendMessage(mH.obtainMessage(MSG_BIND, res));
        }
        public void onUnbindMethod(int sequence) {
            mH.sendMessage(mH.obtainMessage(MSG_UNBIND, sequence, 0));
        }
        public void setActive(boolean active) {
            mH.sendMessage(mH.obtainMessage(MSG_SET_ACTIVE, active ? 1 : 0, 0));
        }
    };    
    final InputConnection mDummyInputConnection = new BaseInputConnection(this, false);
    InputMethodManager(IInputMethodManager service, Looper looper) {
        mService = service;
        mMainLooper = looper;
        mH = new H(looper);
        mIInputContext = new ControlledInputConnectionWrapper(looper,
                mDummyInputConnection);
        if (mInstance == null) {
            mInstance = this;
        }
    }
    static public InputMethodManager getInstance(Context context) {
        return getInstance(context.getMainLooper());
    }
    static public InputMethodManager getInstance(Looper mainLooper) {
        synchronized (mInstanceSync) {
            if (mInstance != null) {
                return mInstance;
            }
            IBinder b = ServiceManager.getService(Context.INPUT_METHOD_SERVICE);
            IInputMethodManager service = IInputMethodManager.Stub.asInterface(b);
            mInstance = new InputMethodManager(service, mainLooper);
        }
        return mInstance;
    }
    static public InputMethodManager peekInstance() {
        return mInstance;
    }
    public IInputMethodClient getClient() {
        return mClient;
    }
    public IInputContext getInputContext() {
        return mIInputContext;
    }
    public List<InputMethodInfo> getInputMethodList() {
        try {
            return mService.getInputMethodList();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public List<InputMethodInfo> getEnabledInputMethodList() {
        try {
            return mService.getEnabledInputMethodList();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void showStatusIcon(IBinder imeToken, String packageName, int iconId) {
        try {
            mService.updateStatusIcon(imeToken, packageName, iconId);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void hideStatusIcon(IBinder imeToken) {
        try {
            mService.updateStatusIcon(imeToken, null, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void setFullscreenMode(boolean fullScreen) {
        mFullscreenMode = fullScreen;
    }
    public boolean isFullscreenMode() {
        return mFullscreenMode;
    }
    public boolean isActive(View view) {
        checkFocus();
        synchronized (mH) {
            return (mServedView == view
                    || (mServedView != null
                            && mServedView.checkInputConnectionProxy(view)))
                    && mCurrentTextBoxAttribute != null;
        }
    }
    public boolean isActive() {
        checkFocus();
        synchronized (mH) {
            return mServedView != null && mCurrentTextBoxAttribute != null;
        }
    }
    public boolean isAcceptingText() {
        checkFocus();
        return mServedInputConnection != null;
    }
    void clearBindingLocked() {
        clearConnectionLocked();
        mBindSequence = -1;
        mCurId = null;
        mCurMethod = null;
    }
    void clearConnectionLocked() {
        mCurrentTextBoxAttribute = null;
        mServedInputConnection = null;
    }
    void finishInputLocked() {
        mNextServedView = null;
        if (mServedView != null) {
            if (DEBUG) Log.v(TAG, "FINISH INPUT: " + mServedView);
            if (mCurrentTextBoxAttribute != null) {
                try {
                    mService.finishInput(mClient);
                } catch (RemoteException e) {
                }
            }
            if (mServedInputConnection != null) {
                Handler vh = mServedView.getHandler();
                if (vh != null) {
                    vh.sendMessage(vh.obtainMessage(ViewRoot.FINISH_INPUT_CONNECTION,
                            mServedInputConnection));
                }
            }
            mServedView = null;
            mCompletions = null;
            mServedConnecting = false;
            clearConnectionLocked();
        }
    }
    public void reportFinishInputConnection(InputConnection ic) {
        if (mServedInputConnection != ic) {
            ic.finishComposingText();
        }
    }
    public void displayCompletions(View view, CompletionInfo[] completions) {
        checkFocus();
        synchronized (mH) {
            if (mServedView != view && (mServedView == null
                            || !mServedView.checkInputConnectionProxy(view))) {
                return;
            }
            mCompletions = completions;
            if (mCurMethod != null) {
                try {
                    mCurMethod.displayCompletions(mCompletions);
                } catch (RemoteException e) {
                }
            }
        }
    }
    public void updateExtractedText(View view, int token, ExtractedText text) {
        checkFocus();
        synchronized (mH) {
            if (mServedView != view && (mServedView == null
                    || !mServedView.checkInputConnectionProxy(view))) {
                return;
            }
            if (mCurMethod != null) {
                try {
                    mCurMethod.updateExtractedText(token, text);
                } catch (RemoteException e) {
                }
            }
        }
    }
    public static final int SHOW_IMPLICIT = 0x0001;
    public static final int SHOW_FORCED = 0x0002;
    public boolean showSoftInput(View view, int flags) {
        return showSoftInput(view, flags, null);
    }
    public static final int RESULT_UNCHANGED_SHOWN = 0;
    public static final int RESULT_UNCHANGED_HIDDEN = 1;
    public static final int RESULT_SHOWN = 2;
    public static final int RESULT_HIDDEN = 3;
    public boolean showSoftInput(View view, int flags,
            ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (mH) {
            if (mServedView != view && (mServedView == null
                    || !mServedView.checkInputConnectionProxy(view))) {
                return false;
            }
            try {
                return mService.showSoftInput(mClient, flags, resultReceiver);
            } catch (RemoteException e) {
            }
            return false;
        }
    }
    public void showSoftInputUnchecked(int flags, ResultReceiver resultReceiver) {
        try {
            mService.showSoftInput(mClient, flags, resultReceiver);
        } catch (RemoteException e) {
        }
    }
    public static final int HIDE_IMPLICIT_ONLY = 0x0001;
    public static final int HIDE_NOT_ALWAYS = 0x0002;
    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
        return hideSoftInputFromWindow(windowToken, flags, null);
    }
    public boolean hideSoftInputFromWindow(IBinder windowToken, int flags,
            ResultReceiver resultReceiver) {
        checkFocus();
        synchronized (mH) {
            if (mServedView == null || mServedView.getWindowToken() != windowToken) {
                return false;
            }
            try {
                return mService.hideSoftInput(mClient, flags, resultReceiver);
            } catch (RemoteException e) {
            }
            return false;
        }
    }
    public void toggleSoftInputFromWindow(IBinder windowToken, int showFlags, int hideFlags) {
        synchronized (mH) {
            if (mServedView == null || mServedView.getWindowToken() != windowToken) {
                return;
            }
            if (mCurMethod != null) {
                try {
                    mCurMethod.toggleSoftInput(showFlags, hideFlags);
                } catch (RemoteException e) {
                }
            }
        }
    }
    public void toggleSoftInput(int showFlags, int hideFlags) {
        if (mCurMethod != null) {
            try {
                mCurMethod.toggleSoftInput(showFlags, hideFlags);
            } catch (RemoteException e) {
            }
        }
    }
    public void restartInput(View view) {
        checkFocus();
        synchronized (mH) {
            if (mServedView != view && (mServedView == null
                    || !mServedView.checkInputConnectionProxy(view))) {
                return;
            }
            mServedConnecting = true;
        }
        startInputInner();
    }
    void startInputInner() {
        final View view;
        synchronized (mH) {
            view = mServedView;
            if (DEBUG) Log.v(TAG, "Starting input: view=" + view);
            if (view == null) {
                if (DEBUG) Log.v(TAG, "ABORT input: no served view!");
                return;
            }
        }
        Handler vh = view.getHandler();
        if (vh == null) {
            if (DEBUG) Log.v(TAG, "ABORT input: no handler for view!");
            return;
        }
        if (vh.getLooper() != Looper.myLooper()) {
            if (DEBUG) Log.v(TAG, "Starting input: reschedule to view thread");
            vh.post(new Runnable() {
                public void run() {
                    startInputInner();
                }
            });
            return;
        }
        EditorInfo tba = new EditorInfo();
        tba.packageName = view.getContext().getPackageName();
        tba.fieldId = view.getId();
        InputConnection ic = view.onCreateInputConnection(tba);
        if (DEBUG) Log.v(TAG, "Starting input: tba=" + tba + " ic=" + ic);
        synchronized (mH) {
            if (mServedView != view || !mServedConnecting) {
                if (DEBUG) Log.v(TAG, 
                        "Starting input: finished by someone else (view="
                        + mServedView + " conn=" + mServedConnecting + ")");
                return;
            }
            final boolean initial = mCurrentTextBoxAttribute == null;
            mCurrentTextBoxAttribute = tba;
            mServedConnecting = false;
            mServedInputConnection = ic;
            IInputContext servedContext;
            if (ic != null) {
                mCursorSelStart = tba.initialSelStart;
                mCursorSelEnd = tba.initialSelEnd;
                mCursorCandStart = -1;
                mCursorCandEnd = -1;
                mCursorRect.setEmpty();
                servedContext = new ControlledInputConnectionWrapper(vh.getLooper(), ic);
            } else {
                servedContext = null;
            }
            try {
                if (DEBUG) Log.v(TAG, "START INPUT: " + view + " ic="
                        + ic + " tba=" + tba + " initial=" + initial);
                InputBindResult res = mService.startInput(mClient,
                        servedContext, tba, initial, mCurMethod == null);
                if (DEBUG) Log.v(TAG, "Starting input: Bind result=" + res);
                if (res != null) {
                    if (res.id != null) {
                        mBindSequence = res.sequence;
                        mCurMethod = res.method;
                    } else {
                        if (DEBUG) Log.v(TAG, "ABORT input: no input method!");
                        return;
                    }
                }
                if (mCurMethod != null && mCompletions != null) {
                    try {
                        mCurMethod.displayCompletions(mCompletions);
                    } catch (RemoteException e) {
                    }
                }
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + mCurId, e);
            }
        }
    }
    public void windowDismissed(IBinder appWindowToken) {
        checkFocus();
        synchronized (mH) {
            if (mServedView != null &&
                    mServedView.getWindowToken() == appWindowToken) {
                finishInputLocked();
            }
        }
    }
    public void focusIn(View view) {
        synchronized (mH) {
            focusInLocked(view);
        }
    }
    void focusInLocked(View view) {
        if (DEBUG) Log.v(TAG, "focusIn: " + view);
        if (mCurRootView != view.getRootView()) {
            if (DEBUG) Log.v(TAG, "Not IME target window, ignoring");
            return;
        }
        mNextServedView = view;
        scheduleCheckFocusLocked(view);
    }
    public void focusOut(View view) {
        synchronized (mH) {
            if (DEBUG) Log.v(TAG, "focusOut: " + view
                    + " mServedView=" + mServedView
                    + " winFocus=" + view.hasWindowFocus());
            if (mServedView != view) {
                if (false && view.hasWindowFocus()) {
                    mNextServedView = null;
                    scheduleCheckFocusLocked(view);
                }
            }
        }
    }
    void scheduleCheckFocusLocked(View view) {
        Handler vh = view.getHandler();
        if (vh != null && !vh.hasMessages(ViewRoot.CHECK_FOCUS)) {
            vh.sendMessage(vh.obtainMessage(ViewRoot.CHECK_FOCUS));
        }
    }
    public void checkFocus() {
        if (mServedView == mNextServedView && !mNextServedNeedsStart) {
            return;
        }
        InputConnection ic = null;
        synchronized (mH) {
            if (mServedView == mNextServedView && !mNextServedNeedsStart) {
                return;
            }
            if (DEBUG) Log.v(TAG, "checkFocus: view=" + mServedView
                    + " next=" + mNextServedView
                    + " restart=" + mNextServedNeedsStart);
            mNextServedNeedsStart = false;
            if (mNextServedView == null) {
                finishInputLocked();
                closeCurrentInput();
                return;
            }
            ic = mServedInputConnection;
            mServedView = mNextServedView;
            mCurrentTextBoxAttribute = null;
            mCompletions = null;
            mServedConnecting = true;
        }
        if (ic != null) {
            ic.finishComposingText();
        }
        startInputInner();
    }
    void closeCurrentInput() {
        try {
            mService.hideSoftInput(mClient, HIDE_NOT_ALWAYS, null);
        } catch (RemoteException e) {
        }
    }
    public void onWindowFocus(View rootView, View focusedView, int softInputMode,
            boolean first, int windowFlags) {
        synchronized (mH) {
            if (DEBUG) Log.v(TAG, "onWindowFocus: " + focusedView
                    + " softInputMode=" + softInputMode
                    + " first=" + first + " flags=#"
                    + Integer.toHexString(windowFlags));
            if (mHasBeenInactive) {
                if (DEBUG) Log.v(TAG, "Has been inactive!  Starting fresh");
                mHasBeenInactive = false;
                mNextServedNeedsStart = true;
            }
            focusInLocked(focusedView != null ? focusedView : rootView);
        }
        checkFocus();
        synchronized (mH) {
            try {
                final boolean isTextEditor = focusedView != null &&
                        focusedView.onCheckIsTextEditor();
                mService.windowGainedFocus(mClient, rootView.getWindowToken(),
                        focusedView != null, isTextEditor, softInputMode, first,
                        windowFlags);
            } catch (RemoteException e) {
            }
        }
    }
    public void startGettingWindowFocus(View rootView) {
        synchronized (mH) {
            mCurRootView = rootView;
        }
    }
    public void updateSelection(View view, int selStart, int selEnd,
            int candidatesStart, int candidatesEnd) {
        checkFocus();
        synchronized (mH) {
            if ((mServedView != view && (mServedView == null
                        || !mServedView.checkInputConnectionProxy(view)))
                    || mCurrentTextBoxAttribute == null || mCurMethod == null) {
                return;
            }
            if (mCursorSelStart != selStart || mCursorSelEnd != selEnd
                    || mCursorCandStart != candidatesStart
                    || mCursorCandEnd != candidatesEnd) {
                if (DEBUG) Log.d(TAG, "updateSelection");
                try {
                    if (DEBUG) Log.v(TAG, "SELECTION CHANGE: " + mCurMethod);
                    mCurMethod.updateSelection(mCursorSelStart, mCursorSelEnd,
                            selStart, selEnd, candidatesStart, candidatesEnd);
                    mCursorSelStart = selStart;
                    mCursorSelEnd = selEnd;
                    mCursorCandStart = candidatesStart;
                    mCursorCandEnd = candidatesEnd;
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + mCurId, e);
                }
            }
        }
    }
    public boolean isWatchingCursor(View view) {
        return false;
    }
    public void updateCursor(View view, int left, int top, int right, int bottom) {
        checkFocus();
        synchronized (mH) {
            if ((mServedView != view && (mServedView == null
                        || !mServedView.checkInputConnectionProxy(view)))
                    || mCurrentTextBoxAttribute == null || mCurMethod == null) {
                return;
            }
            mTmpCursorRect.set(left, top, right, bottom);
            if (!mCursorRect.equals(mTmpCursorRect)) {
                if (DEBUG) Log.d(TAG, "updateCursor");
                try {
                    if (DEBUG) Log.v(TAG, "CURSOR CHANGE: " + mCurMethod);
                    mCurMethod.updateCursor(mTmpCursorRect);
                    mCursorRect.set(mTmpCursorRect);
                } catch (RemoteException e) {
                    Log.w(TAG, "IME died: " + mCurId, e);
                }
            }
        }
    }
    public void sendAppPrivateCommand(View view, String action, Bundle data) {
        checkFocus();
        synchronized (mH) {
            if ((mServedView != view && (mServedView == null
                        || !mServedView.checkInputConnectionProxy(view)))
                    || mCurrentTextBoxAttribute == null || mCurMethod == null) {
                return;
            }
            try {
                if (DEBUG) Log.v(TAG, "APP PRIVATE COMMAND " + action + ": " + data);
                mCurMethod.appPrivateCommand(action, data);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + mCurId, e);
            }
        }
    }
    public void setInputMethod(IBinder token, String id) {
        try {
            mService.setInputMethod(token, id);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void hideSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            mService.hideMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void showSoftInputFromInputMethod(IBinder token, int flags) {
        try {
            mService.showMySoftInput(token, flags);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void dispatchKeyEvent(Context context, int seq, KeyEvent key,
            IInputMethodCallback callback) {
        synchronized (mH) {
            if (DEBUG) Log.d(TAG, "dispatchKeyEvent");
            if (mCurMethod == null) {
                try {
                    callback.finishedEvent(seq, false);
                } catch (RemoteException e) {
                }
                return;
            }
            if (key.getAction() == KeyEvent.ACTION_DOWN
                    && key.getKeyCode() == KeyEvent.KEYCODE_SYM) {
                showInputMethodPicker();
                try {
                    callback.finishedEvent(seq, true);
                } catch (RemoteException e) {
                }
                return;
            }
            try {
                if (DEBUG) Log.v(TAG, "DISPATCH KEY: " + mCurMethod);
                mCurMethod.dispatchKeyEvent(seq, key, callback);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + mCurId + " dropping: " + key, e);
                try {
                    callback.finishedEvent(seq, false);
                } catch (RemoteException ex) {
                }
            }
        }
    }
    void dispatchTrackballEvent(Context context, int seq, MotionEvent motion,
            IInputMethodCallback callback) {
        synchronized (mH) {
            if (DEBUG) Log.d(TAG, "dispatchTrackballEvent");
            if (mCurMethod == null || mCurrentTextBoxAttribute == null) {
                try {
                    callback.finishedEvent(seq, false);
                } catch (RemoteException e) {
                }
                return;
            }
            try {
                if (DEBUG) Log.v(TAG, "DISPATCH TRACKBALL: " + mCurMethod);
                mCurMethod.dispatchTrackballEvent(seq, motion, callback);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + mCurId + " dropping trackball: " + motion, e);
                try {
                    callback.finishedEvent(seq, false);
                } catch (RemoteException ex) {
                }
            }
        }
    }
    public void showInputMethodPicker() {
        synchronized (mH) {
            try {
                mService.showInputMethodPickerFromClient(mClient);
            } catch (RemoteException e) {
                Log.w(TAG, "IME died: " + mCurId, e);
            }
        }
    }
    void doDump(FileDescriptor fd, PrintWriter fout, String[] args) {
        final Printer p = new PrintWriterPrinter(fout);
        p.println("Input method client state for " + this + ":");
        p.println("  mService=" + mService);
        p.println("  mMainLooper=" + mMainLooper);
        p.println("  mIInputContext=" + mIInputContext);
        p.println("  mActive=" + mActive
                + " mHasBeenInactive=" + mHasBeenInactive
                + " mBindSequence=" + mBindSequence
                + " mCurId=" + mCurId);
        p.println("  mCurMethod=" + mCurMethod);
        p.println("  mCurRootView=" + mCurRootView);
        p.println("  mServedView=" + mServedView);
        p.println("  mNextServedNeedsStart=" + mNextServedNeedsStart
                + " mNextServedView=" + mNextServedView);
        p.println("  mServedConnecting=" + mServedConnecting);
        if (mCurrentTextBoxAttribute != null) {
            p.println("  mCurrentTextBoxAttribute:");
            mCurrentTextBoxAttribute.dump(p, "    ");
        } else {
            p.println("  mCurrentTextBoxAttribute: null");
        }
        p.println("  mServedInputConnection=" + mServedInputConnection);
        p.println("  mCompletions=" + mCompletions);
        p.println("  mCursorRect=" + mCursorRect);
        p.println("  mCursorSelStart=" + mCursorSelStart
                + " mCursorSelEnd=" + mCursorSelEnd
                + " mCursorCandStart=" + mCursorCandStart
                + " mCursorCandEnd=" + mCursorCandEnd);
    }
}
