public class Instrumentation {
    public static final String REPORT_KEY_IDENTIFIER = "id";
    public static final String REPORT_KEY_STREAMRESULT = "stream";
    private static final String TAG = "Instrumentation";
    private final Object mSync = new Object();
    private ActivityThread mThread = null;
    private MessageQueue mMessageQueue = null;
    private Context mInstrContext;
    private Context mAppContext;
    private ComponentName mComponent;
    private Thread mRunner;
    private List<ActivityWaiter> mWaitingActivities;
    private List<ActivityMonitor> mActivityMonitors;
    private IInstrumentationWatcher mWatcher;
    private boolean mAutomaticPerformanceSnapshots = false;
    private PerformanceCollector mPerformanceCollector;
    private Bundle mPerfMetrics = new Bundle();
    public Instrumentation() {
    }
    public void onCreate(Bundle arguments) {
    }
    public void start() {
        if (mRunner != null) {
            throw new RuntimeException("Instrumentation already started");
        }
        mRunner = new InstrumentationThread("Instr: " + getClass().getName());
        mRunner.start();
    }
    public void onStart() {
    }
    public boolean onException(Object obj, Throwable e) {
        return false;
    }
    public void sendStatus(int resultCode, Bundle results) {
        if (mWatcher != null) {
            try {
                mWatcher.instrumentationStatus(mComponent, resultCode, results);
            }
            catch (RemoteException e) {
                mWatcher = null;
            }
        }
    }
    public void finish(int resultCode, Bundle results) {
        if (mAutomaticPerformanceSnapshots) {
            endPerformanceSnapshot();
        }
        if (mPerfMetrics != null) {
            results.putAll(mPerfMetrics);
        }
        mThread.finishInstrumentation(resultCode, results);
    }
    public void setAutomaticPerformanceSnapshots() {
        mAutomaticPerformanceSnapshots = true;
        mPerformanceCollector = new PerformanceCollector();
    }
    public void startPerformanceSnapshot() {
        if (!isProfiling()) {
            mPerformanceCollector.beginSnapshot(null);
        }
    }
    public void endPerformanceSnapshot() {
        if (!isProfiling()) {
            mPerfMetrics = mPerformanceCollector.endSnapshot();
        }
    }
    public void onDestroy() {
    }
    public Context getContext() {
        return mInstrContext;
    }
    public ComponentName getComponentName() {
        return mComponent;
    }
    public Context getTargetContext() {
        return mAppContext;
    }
    public boolean isProfiling() {
        return mThread.isProfiling();
    }
    public void startProfiling() {
        if (mThread.isProfiling()) {
            File file = new File(mThread.getProfileFilePath());
            file.getParentFile().mkdirs();
            Debug.startMethodTracing(file.toString(), 8 * 1024 * 1024);
        }
    }
    public void stopProfiling() {
        if (mThread.isProfiling()) {
            Debug.stopMethodTracing();
        }
    }
    public void setInTouchMode(boolean inTouch) {
        try {
            IWindowManager.Stub.asInterface(
                    ServiceManager.getService("window")).setInTouchMode(inTouch);
        } catch (RemoteException e) {
        }
    }
    public void waitForIdle(Runnable recipient) {
        mMessageQueue.addIdleHandler(new Idler(recipient));
        mThread.getHandler().post(new EmptyRunnable());
    }
    public void waitForIdleSync() {
        validateNotAppThread();
        Idler idler = new Idler(null);
        mMessageQueue.addIdleHandler(idler);
        mThread.getHandler().post(new EmptyRunnable());
        idler.waitForIdle();
    }
    public void runOnMainSync(Runnable runner) {
        validateNotAppThread();
        SyncRunnable sr = new SyncRunnable(runner);
        mThread.getHandler().post(sr);
        sr.waitForComplete();
    }
    public Activity startActivitySync(Intent intent) {
        validateNotAppThread();
        synchronized (mSync) {
            intent = new Intent(intent);
            ActivityInfo ai = intent.resolveActivityInfo(
                getTargetContext().getPackageManager(), 0);
            if (ai == null) {
                throw new RuntimeException("Unable to resolve activity for: " + intent);
            }
            String myProc = mThread.getProcessName();
            if (!ai.processName.equals(myProc)) {
                throw new RuntimeException("Intent in process "
                        + myProc + " resolved to different process "
                        + ai.processName + ": " + intent);
            }
            intent.setComponent(new ComponentName(
                    ai.applicationInfo.packageName, ai.name));
            final ActivityWaiter aw = new ActivityWaiter(intent);
            if (mWaitingActivities == null) {
                mWaitingActivities = new ArrayList();
            }
            mWaitingActivities.add(aw);
            getTargetContext().startActivity(intent);
            do {
                try {
                    mSync.wait();
                } catch (InterruptedException e) {
                }
            } while (mWaitingActivities.contains(aw));
            return aw.activity;
        }
    }
    public static class ActivityMonitor {
        private final IntentFilter mWhich;
        private final String mClass;
        private final ActivityResult mResult;
        private final boolean mBlock;
         int mHits = 0;
         Activity mLastActivity = null;
        public ActivityMonitor(
            IntentFilter which, ActivityResult result, boolean block) {
            mWhich = which;
            mClass = null;
            mResult = result;
            mBlock = block;
        }
        public ActivityMonitor(
            String cls, ActivityResult result, boolean block) {
            mWhich = null;
            mClass = cls;
            mResult = result;
            mBlock = block;
        }
        public final IntentFilter getFilter() {
            return mWhich;
        }
        public final ActivityResult getResult() {
            return mResult;
        }
        public final boolean isBlocking() {
            return mBlock;
        }
        public final int getHits() {
            return mHits;
        }
        public final Activity getLastActivity() {
            return mLastActivity;
        }
        public final Activity waitForActivity() {
            synchronized (this) {
                while (mLastActivity == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
                Activity res = mLastActivity;
                mLastActivity = null;
                return res;
            }
        }
        public final Activity waitForActivityWithTimeout(long timeOut) {
            synchronized (this) {
                try {
                    wait(timeOut);
                } catch (InterruptedException e) {
                }
                if (mLastActivity == null) {
                    return null;
                } else {
                    Activity res = mLastActivity;
                    mLastActivity = null;
                    return res;
                }
            }
        }
        final boolean match(Context who,
                            Activity activity,
                            Intent intent) {
            synchronized (this) {
                if (mWhich != null
                    && mWhich.match(who.getContentResolver(), intent,
                                    true, "Instrumentation") < 0) {
                    return false;
                }
                if (mClass != null) {
                    String cls = null;
                    if (activity != null) {
                        cls = activity.getClass().getName();
                    } else if (intent.getComponent() != null) {
                        cls = intent.getComponent().getClassName();
                    }
                    if (cls == null || !mClass.equals(cls)) {
                        return false;
                    }
                }
                if (activity != null) {
                    mLastActivity = activity;
                    notifyAll();
                }
                return true;
            }
        }
    }
    public void addMonitor(ActivityMonitor monitor) {
        synchronized (mSync) {
            if (mActivityMonitors == null) {
                mActivityMonitors = new ArrayList();
            }
            mActivityMonitors.add(monitor);
        }
    }
    public ActivityMonitor addMonitor(
        IntentFilter filter, ActivityResult result, boolean block) {
        ActivityMonitor am = new ActivityMonitor(filter, result, block);
        addMonitor(am);
        return am;
    }
    public ActivityMonitor addMonitor(
        String cls, ActivityResult result, boolean block) {
        ActivityMonitor am = new ActivityMonitor(cls, result, block);
        addMonitor(am);
        return am;
    }
    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        waitForIdleSync();
        synchronized (mSync) {
            if (monitor.getHits() < minHits) {
                return false;
            }
            mActivityMonitors.remove(monitor);
        }
        return true;
    }
    public Activity waitForMonitor(ActivityMonitor monitor) {
        Activity activity = monitor.waitForActivity();
        synchronized (mSync) {
            mActivityMonitors.remove(monitor);
        }
        return activity;
    }
    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        Activity activity = monitor.waitForActivityWithTimeout(timeOut);
        synchronized (mSync) {
            mActivityMonitors.remove(monitor);
        }
        return activity;
    }
    public void removeMonitor(ActivityMonitor monitor) {
        synchronized (mSync) {
            mActivityMonitors.remove(monitor);
        }
    }
    public boolean invokeMenuActionSync(Activity targetActivity, 
                                    int id, int flag) {
        class MenuRunnable implements Runnable {
            private final Activity activity;
            private final int identifier;
            private final int flags;
            boolean returnValue;
            public MenuRunnable(Activity _activity, int _identifier,
                                    int _flags) {
                activity = _activity;
                identifier = _identifier;
                flags = _flags;
            }
            public void run() {
                Window win = activity.getWindow();
                returnValue = win.performPanelIdentifierAction(
                            Window.FEATURE_OPTIONS_PANEL,
                            identifier, 
                            flags);                
            }
        }        
        MenuRunnable mr = new MenuRunnable(targetActivity, id, flag);
        runOnMainSync(mr);
        return mr.returnValue;
    }
    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        validateNotAppThread();
        final KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER); 
        sendKeySync(downEvent);
        waitForIdleSync();
        try {
            Thread.sleep(ViewConfiguration.getLongPressTimeout());
        } catch (InterruptedException e) {
            Log.e(TAG, "Could not sleep for long press timeout", e);
            return false;
        }
        final KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER); 
        sendKeySync(upEvent);
        waitForIdleSync();
        class ContextMenuRunnable implements Runnable {
            private final Activity activity;
            private final int identifier;
            private final int flags;
            boolean returnValue;
            public ContextMenuRunnable(Activity _activity, int _identifier,
                                    int _flags) {
                activity = _activity;
                identifier = _identifier;
                flags = _flags;
            }
            public void run() {
                Window win = activity.getWindow();
                returnValue = win.performContextMenuIdentifierAction(
                            identifier, 
                            flags);                
            }
        }        
        ContextMenuRunnable cmr = new ContextMenuRunnable(targetActivity, id, flag);
        runOnMainSync(cmr);
        return cmr.returnValue;
    }
    public void sendStringSync(String text) {
        if (text == null) {
            return;
        }
        KeyCharacterMap keyCharacterMap = 
            KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        KeyEvent[] events = keyCharacterMap.getEvents(text.toCharArray());
        if (events != null) {
            for (int i = 0; i < events.length; i++) {
                sendKeySync(events[i]);
            }
        }        
    }
    public void sendKeySync(KeyEvent event) {
        validateNotAppThread();
        try {
            (IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
                .injectKeyEvent(event, true);
        } catch (RemoteException e) {
        }
    }
    public void sendKeyDownUpSync(int key) {        
        sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, key));
        sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, key));
    }
    public void sendCharacterSync(int keyCode) {
        sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }
    public void sendPointerSync(MotionEvent event) {
        validateNotAppThread();
        try {
            (IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
                .injectPointerEvent(event, true);
        } catch (RemoteException e) {
        }
    }
    public void sendTrackballEventSync(MotionEvent event) {
        validateNotAppThread();
        try {
            (IWindowManager.Stub.asInterface(ServiceManager.getService("window")))
                .injectTrackballEvent(event, true);
        } catch (RemoteException e) {
        }
    }
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, 
            ClassNotFoundException {
        return newApplication(cl.loadClass(className), context);
    }
    static public Application newApplication(Class<?> clazz, Context context)
            throws InstantiationException, IllegalAccessException, 
            ClassNotFoundException {
        Application app = (Application)clazz.newInstance();
        app.attach(context);
        return app;
    }
    public void callApplicationOnCreate(Application app) {
        app.onCreate();
    }
    public Activity newActivity(Class<?> clazz, Context context, 
            IBinder token, Application application, Intent intent, ActivityInfo info, 
            CharSequence title, Activity parent, String id,
            Object lastNonConfigurationInstance) throws InstantiationException, 
            IllegalAccessException {
        Activity activity = (Activity)clazz.newInstance();
        ActivityThread aThread = null;
        activity.attach(context, aThread, this, token, application, intent, info, title,
                parent, id, lastNonConfigurationInstance, new Configuration());
        return activity;
    }
    public Activity newActivity(ClassLoader cl, String className,
            Intent intent)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        return (Activity)cl.loadClass(className).newInstance();
    }
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if (mWaitingActivities != null) {
            synchronized (mSync) {
                final int N = mWaitingActivities.size();
                for (int i=0; i<N; i++) {
                    final ActivityWaiter aw = mWaitingActivities.get(i);
                    final Intent intent = aw.intent;
                    if (intent.filterEquals(activity.getIntent())) {
                        aw.activity = activity;
                        mMessageQueue.addIdleHandler(new ActivityGoing(aw));
                    }
                }
            }
        }
        activity.onCreate(icicle);
        if (mActivityMonitors != null) {
            synchronized (mSync) {
                final int N = mActivityMonitors.size();
                for (int i=0; i<N; i++) {
                    final ActivityMonitor am = mActivityMonitors.get(i);
                    am.match(activity, activity, activity.getIntent());
                }
            }
        }
    }
    public void callActivityOnDestroy(Activity activity) {
      if (mWaitingActivities != null) {
          synchronized (mSync) {
              final int N = mWaitingActivities.size();
              for (int i=0; i<N; i++) {
                  final ActivityWaiter aw = mWaitingActivities.get(i);
                  final Intent intent = aw.intent;
                  if (intent.filterEquals(activity.getIntent())) {
                      aw.activity = activity;
                      mMessageQueue.addIdleHandler(new ActivityGoing(aw));
                  }
              }
          }
      }
      activity.onDestroy();
      if (mActivityMonitors != null) {
          synchronized (mSync) {
              final int N = mActivityMonitors.size();
              for (int i=0; i<N; i++) {
                  final ActivityMonitor am = mActivityMonitors.get(i);
                  am.match(activity, activity, activity.getIntent());
              }
          }
      }
  }
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        activity.performRestoreInstanceState(savedInstanceState);
    }
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        activity.onPostCreate(icicle);
    }
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        activity.onNewIntent(intent);
    }
    public void callActivityOnStart(Activity activity) {
        activity.onStart();
    }
    public void callActivityOnRestart(Activity activity) {
        activity.onRestart();
    }
    public void callActivityOnResume(Activity activity) {
        activity.onResume();
        if (mActivityMonitors != null) {
            synchronized (mSync) {
                final int N = mActivityMonitors.size();
                for (int i=0; i<N; i++) {
                    final ActivityMonitor am = mActivityMonitors.get(i);
                    am.match(activity, activity, activity.getIntent());
                }
            }
        }
    }
    public void callActivityOnStop(Activity activity) {
        activity.onStop();
    }
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        activity.performSaveInstanceState(outState);
    }
    public void callActivityOnPause(Activity activity) {
        activity.performPause();
    }
    public void callActivityOnUserLeaving(Activity activity) {
        activity.performUserLeaving();
    }
    public void startAllocCounting() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        Debug.resetAllCounts();
        Debug.startAllocCounting();
    }
    public void stopAllocCounting() {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        Debug.stopAllocCounting();
    }
    private void addValue(String key, int value, Bundle results) {
        if (results.containsKey(key)) {
            List<Integer> list = results.getIntegerArrayList(key);
            if (list != null) {
                list.add(value);
            }
        } else {
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(value);
            results.putIntegerArrayList(key, list);
        }
    }
    public Bundle getAllocCounts() {
        Bundle results = new Bundle();
        results.putLong("global_alloc_count", Debug.getGlobalAllocCount());
        results.putLong("global_alloc_size", Debug.getGlobalAllocSize());
        results.putLong("global_freed_count", Debug.getGlobalFreedCount());
        results.putLong("global_freed_size", Debug.getGlobalFreedSize());
        results.putLong("gc_invocation_count", Debug.getGlobalGcInvocationCount());    
        return results;
    }
    public Bundle getBinderCounts() {
        Bundle results = new Bundle();
        results.putLong("sent_transactions", Debug.getBinderSentTransactions());
        results.putLong("received_transactions", Debug.getBinderReceivedTransactions());
        return results;
    }
    public static final class ActivityResult {
        public ActivityResult(int resultCode, Intent resultData) {
            mResultCode = resultCode;
            mResultData = resultData;
        }
        public int getResultCode() {
            return mResultCode;
        }
        public Intent getResultData() {
            return mResultData;
        }
        private final int mResultCode;
        private final Intent mResultData;
    }
    public ActivityResult execStartActivity(
        Context who, IBinder contextThread, IBinder token, Activity target,
        Intent intent, int requestCode) {
        IApplicationThread whoThread = (IApplicationThread) contextThread;
        if (mActivityMonitors != null) {
            synchronized (mSync) {
                final int N = mActivityMonitors.size();
                for (int i=0; i<N; i++) {
                    final ActivityMonitor am = mActivityMonitors.get(i);
                    if (am.match(who, null, intent)) {
                        am.mHits++;
                        if (am.isBlocking()) {
                            return requestCode >= 0 ? am.getResult() : null;
                        }
                        break;
                    }
                }
            }
        }
        try {
            int result = ActivityManagerNative.getDefault()
                .startActivity(whoThread, intent,
                        intent.resolveTypeIfNeeded(who.getContentResolver()),
                        null, 0, token, target != null ? target.mEmbeddedID : null,
                        requestCode, false, false);
            checkStartActivityResult(result, intent);
        } catch (RemoteException e) {
        }
        return null;
    }
     final void init(ActivityThread thread,
            Context instrContext, Context appContext, ComponentName component, 
            IInstrumentationWatcher watcher) {
        mThread = thread;
        mMessageQueue = mThread.getLooper().myQueue();
        mInstrContext = instrContext;
        mAppContext = appContext;
        mComponent = component;
        mWatcher = watcher;
    }
     static void checkStartActivityResult(int res, Object intent) {
        if (res >= IActivityManager.START_SUCCESS) {
            return;
        }
        switch (res) {
            case IActivityManager.START_INTENT_NOT_RESOLVED:
            case IActivityManager.START_CLASS_NOT_FOUND:
                if (intent instanceof Intent && ((Intent)intent).getComponent() != null)
                    throw new ActivityNotFoundException(
                            "Unable to find explicit activity class "
                            + ((Intent)intent).getComponent().toShortString()
                            + "; have you declared this activity in your AndroidManifest.xml?");
                throw new ActivityNotFoundException(
                        "No Activity found to handle " + intent);
            case IActivityManager.START_PERMISSION_DENIED:
                throw new SecurityException("Not allowed to start activity "
                        + intent);
            case IActivityManager.START_FORWARD_AND_REQUEST_CONFLICT:
                throw new AndroidRuntimeException(
                        "FORWARD_RESULT_FLAG used while also requesting a result");
            case IActivityManager.START_NOT_ACTIVITY:
                throw new IllegalArgumentException(
                        "PendingIntent is not an activity");
            default:
                throw new AndroidRuntimeException("Unknown error code "
                        + res + " when starting " + intent);
        }
    }
    private final void validateNotAppThread() {
        if (ActivityThread.currentActivityThread() != null) {
            throw new RuntimeException(
                "This method can not be called from the main application thread");
        }
    }
    private final class InstrumentationThread extends Thread {
        public InstrumentationThread(String name) {
            super(name);
        }
        public void run() {
            IActivityManager am = ActivityManagerNative.getDefault();
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
            } catch (RuntimeException e) {
                Log.w(TAG, "Exception setting priority of instrumentation thread "                                            
                        + Process.myTid(), e);                                                                             
            }
            if (mAutomaticPerformanceSnapshots) {
                startPerformanceSnapshot();
            }
            onStart();
        }
    }
    private static final class EmptyRunnable implements Runnable {
        public void run() {
        }
    }
    private static final class SyncRunnable implements Runnable {
        private final Runnable mTarget;
        private boolean mComplete;
        public SyncRunnable(Runnable target) {
            mTarget = target;
        }
        public void run() {
            mTarget.run();
            synchronized (this) {
                mComplete = true;
                notifyAll();
            }
        }
        public void waitForComplete() {
            synchronized (this) {
                while (!mComplete) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
    private static final class ActivityWaiter {
        public final Intent intent;
        public Activity activity;
        public ActivityWaiter(Intent _intent) {
            intent = _intent;
        }
    }
    private final class ActivityGoing implements MessageQueue.IdleHandler {
        private final ActivityWaiter mWaiter;
        public ActivityGoing(ActivityWaiter waiter) {
            mWaiter = waiter;
        }
        public final boolean queueIdle() {
            synchronized (mSync) {
                mWaitingActivities.remove(mWaiter);
                mSync.notifyAll();
            }
            return false;
        }
    }
    private static final class Idler implements MessageQueue.IdleHandler {
        private final Runnable mCallback;
        private boolean mIdle;
        public Idler(Runnable callback) {
            mCallback = callback;
            mIdle = false;
        }
        public final boolean queueIdle() {
            if (mCallback != null) {
                mCallback.run();
            }
            synchronized (this) {
                mIdle = true;
                notifyAll();
            }
            return false;
        }
        public void waitForIdle() {
            synchronized (this) {
                while (!mIdle) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
