public class LocalActivityManager {
    private static final String TAG = "LocalActivityManager";
    private static final boolean localLOGV = false || Config.LOGV;
    private static class LocalActivityRecord extends Binder {
        LocalActivityRecord(String _id, Intent _intent) {
            id = _id;
            intent = _intent;
        }
        final String id;                
        Intent intent;                  
        ActivityInfo activityInfo;      
        Activity activity;              
        Window window;                  
        Bundle instanceState;           
        int curState = RESTORED;        
    }
    static final int RESTORED = 0;      
    static final int INITIALIZING = 1;  
    static final int CREATED = 2;       
    static final int STARTED = 3;       
    static final int RESUMED = 4;       
    static final int DESTROYED = 5;     
    private final ActivityThread mActivityThread;
    private final Activity mParent;
    private LocalActivityRecord mResumed;
    private final Map<String, LocalActivityRecord> mActivities
            = new HashMap<String, LocalActivityRecord>();
    private final ArrayList<LocalActivityRecord> mActivityArray
            = new ArrayList<LocalActivityRecord>();
    private boolean mSingleMode;
    private boolean mFinishing;
    private int mCurState = INITIALIZING;
    public LocalActivityManager(Activity parent, boolean singleMode) {
        mActivityThread = ActivityThread.currentActivityThread();
        mParent = parent;
        mSingleMode = singleMode;
    }
    private void moveToState(LocalActivityRecord r, int desiredState) {
        if (r.curState == RESTORED || r.curState == DESTROYED) {
            return;
        }
        if (r.curState == INITIALIZING) {
            HashMap<String,Object> lastNonConfigurationInstances =
                mParent.getLastNonConfigurationChildInstances();
            Object instance = null;
            if (lastNonConfigurationInstances != null) {
                instance = lastNonConfigurationInstances.get(r.id);
            }
            if (localLOGV) Log.v(TAG, r.id + ": starting " + r.intent);
            if (r.activityInfo == null) {
                r.activityInfo = mActivityThread.resolveActivityInfo(r.intent);
            }
            r.activity = mActivityThread.startActivityNow(
                    mParent, r.id, r.intent, r.activityInfo, r, r.instanceState, instance);
            if (r.activity == null) {
                return;
            }
            r.window = r.activity.getWindow();
            r.instanceState = null;
            r.curState = STARTED;
            if (desiredState == RESUMED) {
                if (localLOGV) Log.v(TAG, r.id + ": resuming");
                mActivityThread.performResumeActivity(r, true);
                r.curState = RESUMED;
            }
            return;
        }
        switch (r.curState) {
            case CREATED:
                if (desiredState == STARTED) {
                    if (localLOGV) Log.v(TAG, r.id + ": restarting");
                    mActivityThread.performRestartActivity(r);
                    r.curState = STARTED;
                }
                if (desiredState == RESUMED) {
                    if (localLOGV) Log.v(TAG, r.id + ": restarting and resuming");
                    mActivityThread.performRestartActivity(r);
                    mActivityThread.performResumeActivity(r, true);
                    r.curState = RESUMED;
                }
                return;
            case STARTED:
                if (desiredState == RESUMED) {
                    if (localLOGV) Log.v(TAG, r.id + ": resuming");
                    mActivityThread.performResumeActivity(r, true);
                    r.instanceState = null;
                    r.curState = RESUMED;
                }
                if (desiredState == CREATED) {
                    if (localLOGV) Log.v(TAG, r.id + ": stopping");
                    mActivityThread.performStopActivity(r);
                    r.curState = CREATED;
                }
                return;
            case RESUMED:
                if (desiredState == STARTED) {
                    if (localLOGV) Log.v(TAG, r.id + ": pausing");
                    performPause(r, mFinishing);
                    r.curState = STARTED;
                }
                if (desiredState == CREATED) {
                    if (localLOGV) Log.v(TAG, r.id + ": pausing");
                    performPause(r, mFinishing);
                    if (localLOGV) Log.v(TAG, r.id + ": stopping");
                    mActivityThread.performStopActivity(r);
                    r.curState = CREATED;
                }
                return;
        }
    }
    private void performPause(LocalActivityRecord r, boolean finishing) {
        boolean needState = r.instanceState == null;
        Bundle instanceState = mActivityThread.performPauseActivity(r,
                finishing, needState);
        if (needState) {
            r.instanceState = instanceState;
        }
    }
    public Window startActivity(String id, Intent intent) {
        if (mCurState == INITIALIZING) {
            throw new IllegalStateException(
                    "Activities can't be added until the containing group has been created.");
        }
        boolean adding = false;
        boolean sameIntent = false;
        ActivityInfo aInfo = null;
        LocalActivityRecord r = mActivities.get(id);
        if (r == null) {
            r = new LocalActivityRecord(id, intent);
            adding = true;
        } else if (r.intent != null) {
            sameIntent = r.intent.filterEquals(intent); 
            if (sameIntent) {
                aInfo = r.activityInfo;
            }
        }
        if (aInfo == null) {
            aInfo = mActivityThread.resolveActivityInfo(intent);
        }
        if (mSingleMode) {
            LocalActivityRecord old = mResumed;
            if (old != null && old != r && mCurState == RESUMED) {
                moveToState(old, STARTED);
            }
        }
        if (adding) {
            mActivities.put(id, r);
            mActivityArray.add(r);
        } else if (r.activityInfo != null) {
            if (aInfo == r.activityInfo ||
                    (aInfo.name.equals(r.activityInfo.name) &&
                            aInfo.packageName.equals(r.activityInfo.packageName))) {
                if (aInfo.launchMode != ActivityInfo.LAUNCH_MULTIPLE ||
                        (intent.getFlags()&Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0) {
                    ArrayList<Intent> intents = new ArrayList<Intent>(1);
                    intents.add(intent);
                    if (localLOGV) Log.v(TAG, r.id + ": new intent");
                    mActivityThread.performNewIntents(r, intents);
                    r.intent = intent;
                    moveToState(r, mCurState);
                    if (mSingleMode) {
                        mResumed = r;
                    }
                    return r.window;
                }
                if (sameIntent &&
                        (intent.getFlags()&Intent.FLAG_ACTIVITY_CLEAR_TOP) == 0) {
                    r.intent = intent;
                    moveToState(r, mCurState);
                    if (mSingleMode) {
                        mResumed = r;
                    }
                    return r.window;
                }
            }
            performDestroy(r, true);
        }
        r.intent = intent;
        r.curState = INITIALIZING;
        r.activityInfo = aInfo;
        moveToState(r, mCurState);
        if (mSingleMode) {
            mResumed = r;
        }
        return r.window;
    }
    private Window performDestroy(LocalActivityRecord r, boolean finish) {
        Window win = null;
        win = r.window;
        if (r.curState == RESUMED && !finish) {
            performPause(r, finish);
        }
        if (localLOGV) Log.v(TAG, r.id + ": destroying");
        mActivityThread.performDestroyActivity(r, finish);
        r.activity = null;
        r.window = null;
        if (finish) {
            r.instanceState = null;
        }
        r.curState = DESTROYED;
        return win;
    }
    public Window destroyActivity(String id, boolean finish) {
        LocalActivityRecord r = mActivities.get(id);
        Window win = null;
        if (r != null) {
            win = performDestroy(r, finish);
            if (finish) {
                mActivities.remove(r);
            }
        }
        return win;
    }
    public Activity getCurrentActivity() {
        return mResumed != null ? mResumed.activity : null;
    }
    public String getCurrentId() {
        return mResumed != null ? mResumed.id : null;
    }
    public Activity getActivity(String id) {
        LocalActivityRecord r = mActivities.get(id);
        return r != null ? r.activity : null;
    }
    public void dispatchCreate(Bundle state) {
        if (state != null) {
            final Iterator<String> i = state.keySet().iterator();
            while (i.hasNext()) {
                try {
                    final String id = i.next();
                    final Bundle astate = state.getBundle(id);
                    LocalActivityRecord r = mActivities.get(id);
                    if (r != null) {
                        r.instanceState = astate;
                    } else {
                        r = new LocalActivityRecord(id, null);
                        r.instanceState = astate;
                        mActivities.put(id, r);
                        mActivityArray.add(r);
                    }
                } catch (Exception e) {
                    Log.e(TAG,
                          "Exception thrown when restoring LocalActivityManager state",
                          e);
                }
            }
        }
        mCurState = CREATED;
    }
    public Bundle saveInstanceState() {
        Bundle state = null;
        final int N = mActivityArray.size();
        for (int i=0; i<N; i++) {
            final LocalActivityRecord r = mActivityArray.get(i);
            if (state == null) {
                state = new Bundle();
            }
            if ((r.instanceState != null || r.curState == RESUMED)
                    && r.activity != null) {
                final Bundle childState = new Bundle();
                r.activity.onSaveInstanceState(childState);
                r.instanceState = childState;
            }
            if (r.instanceState != null) {
                state.putBundle(r.id, r.instanceState);
            }
        }
        return state;
    }
    public void dispatchResume() {
        mCurState = RESUMED;
        if (mSingleMode) {
            if (mResumed != null) {
                moveToState(mResumed, RESUMED);
            }
        } else {
            final int N = mActivityArray.size();
            for (int i=0; i<N; i++) {
                moveToState(mActivityArray.get(i), RESUMED);
            }
        }
    }
    public void dispatchPause(boolean finishing) {
        if (finishing) {
            mFinishing = true;
        }
        mCurState = STARTED;
        if (mSingleMode) {
            if (mResumed != null) {
                moveToState(mResumed, STARTED);
            }
        } else {
            final int N = mActivityArray.size();
            for (int i=0; i<N; i++) {
                LocalActivityRecord r = mActivityArray.get(i);
                if (r.curState == RESUMED) {
                    moveToState(r, STARTED);
                }
            }
        }
    }
    public void dispatchStop() {
        mCurState = CREATED;
        final int N = mActivityArray.size();
        for (int i=0; i<N; i++) {
            LocalActivityRecord r = mActivityArray.get(i);
            moveToState(r, CREATED);
        }
    }
    public HashMap<String,Object> dispatchRetainNonConfigurationInstance() {
        HashMap<String,Object> instanceMap = null;
        final int N = mActivityArray.size();
        for (int i=0; i<N; i++) {
            LocalActivityRecord r = mActivityArray.get(i);
            if ((r != null) && (r.activity != null)) {
                Object instance = r.activity.onRetainNonConfigurationInstance();
                if (instance != null) {
                    if (instanceMap == null) {
                        instanceMap = new HashMap<String,Object>();
                    }
                    instanceMap.put(r.id, instance);
                }
            }
        }
        return instanceMap;
    }
    public void removeAllActivities() {
        dispatchDestroy(true);
    }
    public void dispatchDestroy(boolean finishing) {
        final int N = mActivityArray.size();
        for (int i=0; i<N; i++) {
            LocalActivityRecord r = mActivityArray.get(i);
            if (localLOGV) Log.v(TAG, r.id + ": destroying");
            mActivityThread.performDestroyActivity(r, finishing);
        }
        mActivities.clear();
        mActivityArray.clear();
    }
}
