class ProcessRecord implements Watchdog.PssRequestor {
    final BatteryStatsImpl.Uid.Proc batteryStats; 
    final ApplicationInfo info; 
    final String processName;   
    final HashSet<String> pkgList = new HashSet();
    IApplicationThread thread;  
    int pid;                    
    boolean starting;           
    long lastActivityTime;      
    long lruWeight;             
    int maxAdj;                 
    int hiddenAdj;              
    int curRawAdj;              
    int setRawAdj;              
    int curAdj;                 
    int setAdj;                 
    int curSchedGroup;          
    int setSchedGroup;          
    boolean setIsForeground;    
    boolean foregroundServices; 
    boolean bad;                
    boolean killedBackground;   
    IBinder forcingToForeground;
    int adjSeq;                 
    int lruSeq;                 
    ComponentName instrumentationClass;
    ApplicationInfo instrumentationInfo; 
    String instrumentationProfileFile; 
    IInstrumentationWatcher instrumentationWatcher; 
    Bundle instrumentationArguments;
    ComponentName instrumentationResultClass;
    BroadcastRecord curReceiver;
    long lastRequestedGc;       
    long lastLowMemory;         
    boolean reportLowMemory;    
    boolean empty;              
    boolean hidden;             
    int lastPss;                
    String adjType;             
    int adjTypeCode;            
    Object adjSource;           
    Object adjTarget;           
    final ArrayList activities = new ArrayList();
    final HashSet services = new HashSet();
    final HashSet<ServiceRecord> executingServices
             = new HashSet<ServiceRecord>();
    final HashSet<ConnectionRecord> connections
            = new HashSet<ConnectionRecord>();  
    final HashSet<ReceiverList> receivers = new HashSet<ReceiverList>();
    final HashMap pubProviders = new HashMap(); 
    final HashMap<ContentProviderRecord, Integer> conProviders
            = new HashMap<ContentProviderRecord, Integer>(); 
    boolean persistent;         
    boolean crashing;           
    Dialog crashDialog;         
    boolean notResponding;      
    Dialog anrDialog;           
    boolean removed;            
    boolean debugging;          
    int persistentActivities;   
    boolean waitedForDebugger;  
    Dialog waitDialog;          
    String shortStringName;     
    String stringName;          
    ActivityManager.ProcessErrorStateInfo crashingReport;
    ActivityManager.ProcessErrorStateInfo notRespondingReport;
    ComponentName errorReportReceiver;
    void dump(PrintWriter pw, String prefix) {
        long now = SystemClock.uptimeMillis();
        if (info.className != null) {
            pw.print(prefix); pw.print("class="); pw.println(info.className);
        }
        if (info.manageSpaceActivityName != null) {
            pw.print(prefix); pw.print("manageSpaceActivityName=");
            pw.println(info.manageSpaceActivityName);
        }
        pw.print(prefix); pw.print("dir="); pw.print(info.sourceDir);
                pw.print(" publicDir="); pw.print(info.publicSourceDir);
                pw.print(" data="); pw.println(info.dataDir);
        pw.print(prefix); pw.print("packageList="); pw.println(pkgList);
        if (instrumentationClass != null || instrumentationProfileFile != null
                || instrumentationArguments != null) {
            pw.print(prefix); pw.print("instrumentationClass=");
                    pw.print(instrumentationClass);
                    pw.print(" instrumentationProfileFile=");
                    pw.println(instrumentationProfileFile);
            pw.print(prefix); pw.print("instrumentationArguments=");
                    pw.println(instrumentationArguments);
            pw.print(prefix); pw.print("instrumentationInfo=");
                    pw.println(instrumentationInfo);
            if (instrumentationInfo != null) {
                instrumentationInfo.dump(new PrintWriterPrinter(pw), prefix + "  ");
            }
        }
        pw.print(prefix); pw.print("thread="); pw.print(thread);
                pw.print(" curReceiver="); pw.println(curReceiver);
        pw.print(prefix); pw.print("pid="); pw.print(pid); pw.print(" starting=");
                pw.print(starting); pw.print(" lastPss="); pw.println(lastPss);
        pw.print(prefix); pw.print("lastActivityTime="); pw.print(lastActivityTime);
                pw.print(" lruWeight="); pw.println(lruWeight);
                pw.print(" hidden="); pw.print(hidden);
                pw.print(" empty="); pw.println(empty);
        pw.print(prefix); pw.print("oom: max="); pw.print(maxAdj);
                pw.print(" hidden="); pw.print(hiddenAdj);
                pw.print(" curRaw="); pw.print(curRawAdj);
                pw.print(" setRaw="); pw.print(setRawAdj);
                pw.print(" cur="); pw.print(curAdj);
                pw.print(" set="); pw.println(setAdj);
        pw.print(prefix); pw.print("curSchedGroup="); pw.print(curSchedGroup);
                pw.print(" setSchedGroup="); pw.println(setSchedGroup);
        pw.print(prefix); pw.print("setIsForeground="); pw.print(setIsForeground);
                pw.print(" foregroundServices="); pw.print(foregroundServices);
                pw.print(" forcingToForeground="); pw.println(forcingToForeground);
        pw.print(prefix); pw.print("persistent="); pw.print(persistent);
                pw.print(" removed="); pw.print(removed);
                pw.print(" persistentActivities="); pw.println(persistentActivities);
        pw.print(prefix); pw.print("adjSeq="); pw.print(adjSeq);
                pw.print(" lruSeq="); pw.println(lruSeq);
        if (killedBackground) {
            pw.print(prefix); pw.print("killedBackground="); pw.println(killedBackground);
        }
        if (debugging || crashing || crashDialog != null || notResponding
                || anrDialog != null || bad) {
            pw.print(prefix); pw.print("debugging="); pw.print(debugging);
                    pw.print(" crashing="); pw.print(crashing);
                    pw.print(" "); pw.print(crashDialog);
                    pw.print(" notResponding="); pw.print(notResponding);
                    pw.print(" " ); pw.print(anrDialog);
                    pw.print(" bad="); pw.print(bad);
                    if (errorReportReceiver != null) {
                        pw.print(" errorReportReceiver=");
                        pw.print(errorReportReceiver.flattenToShortString());
                    }
                    pw.println();
        }
        if (activities.size() > 0) {
            pw.print(prefix); pw.print("activities="); pw.println(activities);
        }
        if (services.size() > 0) {
            pw.print(prefix); pw.print("services="); pw.println(services);
        }
        if (executingServices.size() > 0) {
            pw.print(prefix); pw.print("executingServices="); pw.println(executingServices);
        }
        if (connections.size() > 0) {
            pw.print(prefix); pw.print("connections="); pw.println(connections);
        }
        if (pubProviders.size() > 0) {
            pw.print(prefix); pw.print("pubProviders="); pw.println(pubProviders);
        }
        if (conProviders.size() > 0) {
            pw.print(prefix); pw.print("conProviders="); pw.println(conProviders);
        }
        if (receivers.size() > 0) {
            pw.print(prefix); pw.print("receivers="); pw.println(receivers);
        }
    }
    ProcessRecord(BatteryStatsImpl.Uid.Proc _batteryStats, IApplicationThread _thread,
            ApplicationInfo _info, String _processName) {
        batteryStats = _batteryStats;
        info = _info;
        processName = _processName;
        pkgList.add(_info.packageName);
        thread = _thread;
        maxAdj = ActivityManagerService.EMPTY_APP_ADJ;
        hiddenAdj = ActivityManagerService.HIDDEN_APP_MIN_ADJ;
        curRawAdj = setRawAdj = -100;
        curAdj = setAdj = -100;
        persistent = false;
        removed = false;
        persistentActivities = 0;
    }
    public void setPid(int _pid) {
        pid = _pid;
        shortStringName = null;
        stringName = null;
    }
    public boolean isInterestingToUserLocked() {
        final int size = activities.size();
        for (int i = 0 ; i < size ; i++) {
            HistoryRecord r = (HistoryRecord) activities.get(i);
            if (r.isInterestingToUserLocked()) {
                return true;
            }
        }
        return false;
    }
    public void stopFreezingAllLocked() {
        int i = activities.size();
        while (i > 0) {
            i--;
            ((HistoryRecord)activities.get(i)).stopFreezingScreenLocked(true);
        }
    }
    public void requestPss() {
        IApplicationThread localThread = thread;
        if (localThread != null) {
            try {
                localThread.requestPss();
            } catch (RemoteException e) {
            }
        }
    }
    public String toShortString() {
        if (shortStringName != null) {
            return shortStringName;
        }
        StringBuilder sb = new StringBuilder(128);
        toShortString(sb);
        return shortStringName = sb.toString();
    }
    void toShortString(StringBuilder sb) {
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(pid);
        sb.append(':');
        sb.append(processName);
        sb.append('/');
        sb.append(info.uid);
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ProcessRecord{");
        toShortString(sb);
        sb.append('}');
        return stringName = sb.toString();
    }
    public boolean addPackage(String pkg) {
        if (!pkgList.contains(pkg)) {
            pkgList.add(pkg);
            return true;
        }
        return false;
    }
    public void resetPackageList() {
        pkgList.clear();
        pkgList.add(info.packageName);
    }
    public String[] getPackageList() {
        int size = pkgList.size();
        if (size == 0) {
            return null;
        }
        String list[] = new String[size];
        pkgList.toArray(list);
        return list;
    }
}
