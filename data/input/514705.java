class HistoryRecord extends IApplicationToken.Stub {
    final ActivityManagerService service; 
    final ActivityInfo info; 
    final int launchedFromUid; 
    final Intent intent;    
    final ComponentName realActivity;  
    final String shortComponentName; 
    final String resolvedType; 
    final String packageName; 
    final String processName; 
    final String taskAffinity; 
    final boolean stateNotNeeded; 
    final boolean fullscreen;     
    final boolean componentSpecified;  
    final boolean isHomeActivity; 
    final String baseDir;   
    final String resDir;   
    final String dataDir;   
    CharSequence nonLocalizedLabel;  
    int labelRes;           
    int icon;               
    int theme;              
    TaskRecord task;        
    long startTime;         
    long cpuTimeAtResume;   
    Configuration configuration; 
    HistoryRecord resultTo; 
    final String resultWho; 
    final int requestCode;  
    ArrayList results;      
    HashSet<WeakReference<PendingIntentRecord>> pendingResults; 
    ArrayList newIntents;   
    HashSet<ConnectionRecord> connections; 
    HashSet<UriPermission> readUriPermissions; 
    HashSet<UriPermission> writeUriPermissions; 
    ProcessRecord app;  
    Bitmap thumbnail;       
    CharSequence description; 
    ActivityManagerService.ActivityState state;    
    Bundle  icicle;         
    boolean frontOfTask;    
    boolean launchFailed;   
    boolean haveState;      
    boolean stopped;        
    boolean delayedResume;  
    boolean finishing;      
    boolean configDestroy;  
    int configChangeFlags;  
    boolean keysPaused;     
    boolean inHistory;      
    boolean persistent;     
    int launchMode;         
    boolean visible;        
    boolean waitingVisible; 
    boolean nowVisible;     
    boolean thumbnailNeeded;
    boolean idle;           
    boolean hasBeenLaunched;
    boolean frozenBeforeDestroy;
    String stringName;      
    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("packageName="); pw.print(packageName);
                pw.print(" processName="); pw.println(processName);
        pw.print(prefix); pw.print("launchedFromUid="); pw.print(launchedFromUid);
                pw.print(" app="); pw.println(app);
        pw.print(prefix); pw.println(intent);
        pw.print(prefix); pw.print("frontOfTask="); pw.print(frontOfTask);
                pw.print(" task="); pw.println(task);
        pw.print(prefix); pw.print("taskAffinity="); pw.println(taskAffinity);
        pw.print(prefix); pw.print("realActivity=");
                pw.println(realActivity.flattenToShortString());
        pw.print(prefix); pw.print("base="); pw.print(baseDir);
                if (!resDir.equals(baseDir)) pw.print(" res="); pw.print(resDir);
                pw.print(" data="); pw.println(dataDir);
        pw.print(prefix); pw.print("labelRes=0x");
                pw.print(Integer.toHexString(labelRes));
                pw.print(" icon=0x"); pw.print(Integer.toHexString(icon));
                pw.print(" theme=0x"); pw.println(Integer.toHexString(theme));
        pw.print(prefix); pw.print("stateNotNeeded="); pw.print(stateNotNeeded);
                pw.print(" componentSpecified="); pw.print(componentSpecified);
                pw.print(" isHomeActivity="); pw.println(isHomeActivity);
        pw.print(prefix); pw.print("configuration="); pw.println(configuration);
        if (resultTo != null || resultWho != null) {
            pw.print(prefix); pw.print("resultTo="); pw.print(resultTo);
                    pw.print(" resultWho="); pw.print(resultWho);
                    pw.print(" resultCode="); pw.println(requestCode);
        }
        if (results != null) {
            pw.print(prefix); pw.print("results="); pw.println(results);
        }
        if (pendingResults != null) {
            pw.print(prefix); pw.print("pendingResults="); pw.println(pendingResults);
        }
        if (readUriPermissions != null) {
            pw.print(prefix); pw.print("readUriPermissions="); pw.println(readUriPermissions);
        }
        if (writeUriPermissions != null) {
            pw.print(prefix); pw.print("writeUriPermissions="); pw.println(writeUriPermissions);
        }
        pw.print(prefix); pw.print("launchFailed="); pw.print(launchFailed);
                pw.print(" haveState="); pw.print(haveState);
                pw.print(" icicle="); pw.println(icicle);
        pw.print(prefix); pw.print("state="); pw.print(state);
                pw.print(" stopped="); pw.print(stopped);
                pw.print(" delayedResume="); pw.print(delayedResume);
                pw.print(" finishing="); pw.println(finishing);
        pw.print(prefix); pw.print("keysPaused="); pw.print(keysPaused);
                pw.print(" inHistory="); pw.print(inHistory);
                pw.print(" persistent="); pw.print(persistent);
                pw.print(" launchMode="); pw.println(launchMode);
        pw.print(prefix); pw.print("fullscreen="); pw.print(fullscreen);
                pw.print(" visible="); pw.print(visible);
                pw.print(" frozenBeforeDestroy="); pw.print(frozenBeforeDestroy);
                pw.print(" thumbnailNeeded="); pw.print(thumbnailNeeded);
                pw.print(" idle="); pw.println(idle);
        if (waitingVisible || nowVisible) {
            pw.print(prefix); pw.print("waitingVisible="); pw.print(waitingVisible);
                    pw.print(" nowVisible="); pw.println(nowVisible);
        }
        if (configDestroy || configChangeFlags != 0) {
            pw.print(prefix); pw.print("configDestroy="); pw.print(configDestroy);
                    pw.print(" configChangeFlags=");
                    pw.println(Integer.toHexString(configChangeFlags));
        }
        if (connections != null) {
            pw.print(prefix); pw.print("connections="); pw.println(connections);
        }
    }
    HistoryRecord(ActivityManagerService _service, ProcessRecord _caller,
            int _launchedFromUid, Intent _intent, String _resolvedType,
            ActivityInfo aInfo, Configuration _configuration,
            HistoryRecord _resultTo, String _resultWho, int _reqCode,
            boolean _componentSpecified) {
        service = _service;
        info = aInfo;
        launchedFromUid = _launchedFromUid;
        intent = _intent;
        shortComponentName = _intent.getComponent().flattenToShortString();
        resolvedType = _resolvedType;
        componentSpecified = _componentSpecified;
        configuration = _configuration;
        resultTo = _resultTo;
        resultWho = _resultWho;
        requestCode = _reqCode;
        state = ActivityManagerService.ActivityState.INITIALIZING;
        frontOfTask = false;
        launchFailed = false;
        haveState = false;
        stopped = false;
        delayedResume = false;
        finishing = false;
        configDestroy = false;
        keysPaused = false;
        inHistory = false;
        persistent = false;
        visible = true;
        waitingVisible = false;
        nowVisible = false;
        thumbnailNeeded = false;
        idle = false;
        hasBeenLaunched = false;
        if (aInfo != null) {
            if (aInfo.targetActivity == null
                    || aInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE
                    || aInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {
                realActivity = _intent.getComponent();
            } else {
                realActivity = new ComponentName(aInfo.packageName,
                        aInfo.targetActivity);
            }
            taskAffinity = aInfo.taskAffinity;
            stateNotNeeded = (aInfo.flags&
                    ActivityInfo.FLAG_STATE_NOT_NEEDED) != 0;
            baseDir = aInfo.applicationInfo.sourceDir;
            resDir = aInfo.applicationInfo.publicSourceDir;
            dataDir = aInfo.applicationInfo.dataDir;
            nonLocalizedLabel = aInfo.nonLocalizedLabel;
            labelRes = aInfo.labelRes;
            if (nonLocalizedLabel == null && labelRes == 0) {
                ApplicationInfo app = aInfo.applicationInfo;
                nonLocalizedLabel = app.nonLocalizedLabel;
                labelRes = app.labelRes;
            }
            icon = aInfo.getIconResource();
            theme = aInfo.getThemeResource();
            if ((aInfo.flags&ActivityInfo.FLAG_MULTIPROCESS) != 0
                    && _caller != null
                    && (aInfo.applicationInfo.uid == Process.SYSTEM_UID
                            || aInfo.applicationInfo.uid == _caller.info.uid)) {
                processName = _caller.processName;
            } else {
                processName = aInfo.processName;
            }
            if (intent != null && (aInfo.flags & ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS) != 0) {
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            }
            packageName = aInfo.applicationInfo.packageName;
            launchMode = aInfo.launchMode;
            AttributeCache.Entry ent = AttributeCache.instance().get(packageName,
                    theme != 0 ? theme : android.R.style.Theme,
                    com.android.internal.R.styleable.Window);
            fullscreen = ent != null && !ent.array.getBoolean(
                    com.android.internal.R.styleable.Window_windowIsFloating, false)
                    && !ent.array.getBoolean(
                    com.android.internal.R.styleable.Window_windowIsTranslucent, false);
            if (!_componentSpecified || _launchedFromUid == Process.myUid()
                    || _launchedFromUid == 0) {
                if (Intent.ACTION_MAIN.equals(_intent.getAction()) &&
                        _intent.hasCategory(Intent.CATEGORY_HOME) &&
                        _intent.getCategories().size() == 1 &&
                        _intent.getData() == null &&
                        _intent.getType() == null &&
                        (intent.getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) != 0 &&
                        !"android".equals(realActivity.getClassName())) {
                    isHomeActivity = true;
                } else {
                    isHomeActivity = false;
                }
            } else {
                isHomeActivity = false;
            }
        } else {
            realActivity = null;
            taskAffinity = null;
            stateNotNeeded = false;
            baseDir = null;
            resDir = null;
            dataDir = null;
            processName = null;
            packageName = null;
            fullscreen = true;
            isHomeActivity = false;
        }
    }
    void addResultLocked(HistoryRecord from, String resultWho,
            int requestCode, int resultCode,
            Intent resultData) {
        ActivityResult r = new ActivityResult(from, resultWho,
        		requestCode, resultCode, resultData);
        if (results == null) {
            results = new ArrayList();
        }
        results.add(r);
    }
    void removeResultsLocked(HistoryRecord from, String resultWho,
            int requestCode) {
        if (results != null) {
            for (int i=results.size()-1; i>=0; i--) {
                ActivityResult r = (ActivityResult)results.get(i);
                if (r.mFrom != from) continue;
                if (r.mResultWho == null) {
                    if (resultWho != null) continue;
                } else {
                    if (!r.mResultWho.equals(resultWho)) continue;
                }
                if (r.mRequestCode != requestCode) continue;
                results.remove(i);
            }
        }
    }
    void addNewIntentLocked(Intent intent) {
        if (newIntents == null) {
            newIntents = new ArrayList();
        }
        newIntents.add(intent);
    }
    void pauseKeyDispatchingLocked() {
        if (!keysPaused) {
            keysPaused = true;
            service.mWindowManager.pauseKeyDispatching(this);
        }
    }
    void resumeKeyDispatchingLocked() {
        if (keysPaused) {
            keysPaused = false;
            service.mWindowManager.resumeKeyDispatching(this);
        }
    }
    public boolean mayFreezeScreenLocked(ProcessRecord app) {
        return app == null || (!app.crashing && !app.notResponding);
    }
    public void startFreezingScreenLocked(ProcessRecord app, int configChanges) {
        if (mayFreezeScreenLocked(app)) {
            service.mWindowManager.startAppFreezingScreen(this, configChanges);
        }
    }
    public void stopFreezingScreenLocked(boolean force) {
        if (force || frozenBeforeDestroy) {
            frozenBeforeDestroy = false;
            service.mWindowManager.stopAppFreezingScreen(this, force);
        }
    }
    public void windowsVisible() {
        synchronized(service) {
            if (startTime != 0) {
                final long curTime = SystemClock.uptimeMillis();
                final long thisTime = curTime - startTime;
                final long totalTime = service.mInitialStartTime != 0
                        ? (curTime - service.mInitialStartTime) : thisTime;
                if (ActivityManagerService.SHOW_ACTIVITY_START_TIME) {
                    EventLog.writeEvent(EventLogTags.ACTIVITY_LAUNCH_TIME,
                            System.identityHashCode(this), shortComponentName,
                            thisTime, totalTime);
                    StringBuilder sb = service.mStringBuilder;
                    sb.setLength(0);
                    sb.append("Displayed activity ");
                    sb.append(shortComponentName);
                    sb.append(": ");
                    sb.append(thisTime);
                    sb.append(" ms (total ");
                    sb.append(totalTime);
                    sb.append(" ms)");
                    Log.i(ActivityManagerService.TAG, sb.toString());
                }
                service.reportActivityLaunchedLocked(false, this, thisTime, totalTime);
                if (totalTime > 0) {
                    service.mUsageStatsService.noteLaunchTime(realActivity, (int)totalTime);
                }
                startTime = 0;
                service.mInitialStartTime = 0;
            }
            service.reportActivityVisibleLocked(this);
            if (ActivityManagerService.DEBUG_SWITCH) Log.v(
                    ActivityManagerService.TAG, "windowsVisible(): " + this);
            if (!nowVisible) {
                nowVisible = true;
                if (!idle) {
                    service.processStoppingActivitiesLocked(false);
                } else {
                    final int N = service.mWaitingVisibleActivities.size();
                    if (N > 0) {
                        for (int i=0; i<N; i++) {
                            HistoryRecord r = (HistoryRecord)
                                service.mWaitingVisibleActivities.get(i);
                            r.waitingVisible = false;
                            if (ActivityManagerService.DEBUG_SWITCH) Log.v(
                                    ActivityManagerService.TAG,
                                    "Was waiting for visible: " + r);
                        }
                        service.mWaitingVisibleActivities.clear();
                        Message msg = Message.obtain();
                        msg.what = ActivityManagerService.IDLE_NOW_MSG;
                        service.mHandler.sendMessage(msg);
                    }
                }
                service.scheduleAppGcsLocked();
            }
        }
    }
    public void windowsGone() {
        if (ActivityManagerService.DEBUG_SWITCH) Log.v(
                ActivityManagerService.TAG, "windowsGone(): " + this);
        nowVisible = false;
    }
    private HistoryRecord getWaitingHistoryRecordLocked() {
        HistoryRecord r = this;
        if (r.waitingVisible) {
            r = service.mResumedActivity;
            if (r == null) {
                r = service.mPausingActivity;
            }
            if (r == null) {
                r = this;
            }
        }
        return r;
    }
    public boolean keyDispatchingTimedOut() {
        HistoryRecord r;
        ProcessRecord anrApp = null;
        synchronized(service) {
            r = getWaitingHistoryRecordLocked();
            if (r != null && r.app != null) {
                if (r.app.debugging) {
                    return false;
                }
                if (service.mDidDexOpt) {
                    service.mDidDexOpt = false;
                    return false;
                }
                if (r.app.instrumentationClass == null) { 
                    anrApp = r.app;
                } else {
                    Bundle info = new Bundle();
                    info.putString("shortMsg", "keyDispatchingTimedOut");
                    info.putString("longMsg", "Timed out while dispatching key event");
                    service.finishInstrumentationLocked(
                            r.app, Activity.RESULT_CANCELED, info);
                }
            }
        }
        if (anrApp != null) {
            service.appNotResponding(anrApp, r, this,
                    "keyDispatchingTimedOut");
        }
        return true;
    }
    public long getKeyDispatchingTimeout() {
        synchronized(service) {
            HistoryRecord r = getWaitingHistoryRecordLocked();
            if (r == null || r.app == null
                    || r.app.instrumentationClass == null) {
                return ActivityManagerService.KEY_DISPATCHING_TIMEOUT;
            }
            return ActivityManagerService.INSTRUMENTATION_KEY_DISPATCHING_TIMEOUT;
        }
    }
    public boolean isInterestingToUserLocked() {
        return visible || nowVisible || state == ActivityState.PAUSING || 
                state == ActivityState.RESUMED;
     }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("HistoryRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(intent.getComponent().flattenToShortString());
        sb.append('}');
        return stringName = sb.toString();
    }
}
