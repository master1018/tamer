class ServiceRecord extends Binder {
    final ActivityManagerService ams;
    final BatteryStatsImpl.Uid.Pkg.Serv stats;
    final ComponentName name; 
    final String shortName; 
    final Intent.FilterComparison intent;
    final ServiceInfo serviceInfo;
    final ApplicationInfo appInfo;
    final String packageName; 
    final String processName; 
    final String permission;
    final String baseDir;   
    final String resDir;   
    final String dataDir;   
    final boolean exported; 
    final Runnable restarter; 
    final long createTime;  
    final HashMap<Intent.FilterComparison, IntentBindRecord> bindings
            = new HashMap<Intent.FilterComparison, IntentBindRecord>();
    final HashMap<IBinder, ConnectionRecord> connections
            = new HashMap<IBinder, ConnectionRecord>();
    static final int MAX_DELIVERY_COUNT = 3;
    static final int MAX_DONE_EXECUTING_COUNT = 6;
    static class StartItem {
        final int id;
        final Intent intent;
        long deliveredTime;
        int deliveryCount;
        int doneExecutingCount;
        StartItem(int _id, Intent _intent) {
            id = _id;
            intent = _intent;
        }
    }
    final ArrayList<StartItem> deliveredStarts = new ArrayList<StartItem>();
    final ArrayList<StartItem> pendingStarts = new ArrayList<StartItem>();
    ProcessRecord app;      
    boolean isForeground;   
    int foregroundId;       
    Notification foregroundNoti; 
    long lastActivity;      
    boolean startRequested; 
    boolean stopIfKilled;   
    boolean callStart;      
    int lastStartId;        
    int executeNesting;     
    long executingStart;    
    int crashCount;         
    int totalRestartCount;  
    int restartCount;       
    long restartDelay;      
    long restartTime;       
    long nextRestartTime;   
    String stringName;      
    void dumpStartList(PrintWriter pw, String prefix, List<StartItem> list, long now) {
        final int N = list.size();
        for (int i=0; i<N; i++) {
            StartItem si = list.get(i);
            pw.print(prefix); pw.print("#"); pw.print(i);
                    pw.print(" id="); pw.print(si.id);
                    if (now != 0) pw.print(" dur="); pw.print(now-si.deliveredTime);
                    if (si.deliveryCount != 0) {
                        pw.print(" dc="); pw.print(si.deliveryCount);
                    }
                    if (si.doneExecutingCount != 0) {
                        pw.print(" dxc="); pw.print(si.doneExecutingCount);
                    }
                    pw.print(" ");
                    if (si.intent != null) pw.println(si.intent.toString());
                    else pw.println("null");
        }
    }
    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("intent={");
                pw.print(intent.getIntent().toShortString(true, false));
                pw.println('}');
        pw.print(prefix); pw.print("packageName="); pw.println(packageName);
        pw.print(prefix); pw.print("processName="); pw.println(processName);
        if (permission != null) {
            pw.print(prefix); pw.print("permission="); pw.println(permission);
        }
        long now = SystemClock.uptimeMillis();
        pw.print(prefix); pw.print("baseDir="); pw.print(baseDir);
                if (!resDir.equals(baseDir)) pw.print(" resDir="); pw.print(resDir);
                pw.print(" dataDir="); pw.println(dataDir);
        pw.print(prefix); pw.print("app="); pw.println(app);
        if (isForeground || foregroundId != 0) {
            pw.print(prefix); pw.print("isForeground="); pw.print(isForeground);
                    pw.print(" foregroundId="); pw.print(foregroundId);
                    pw.print(" foregroundNoti="); pw.println(foregroundNoti);
        }
        pw.print(prefix); pw.print("lastActivity="); pw.print(lastActivity-now);
                pw.print(" executingStart="); pw.print(executingStart-now);
                pw.print(" restartTime="); pw.println(restartTime);
        if (startRequested || lastStartId != 0) {
            pw.print(prefix); pw.print("startRequested="); pw.print(startRequested);
                    pw.print(" stopIfKilled="); pw.print(stopIfKilled);
                    pw.print(" callStart="); pw.print(callStart);
                    pw.print(" lastStartId="); pw.println(lastStartId);
        }
        if (executeNesting != 0 || crashCount != 0 || restartCount != 0
                || restartDelay != 0 || nextRestartTime != 0) {
            pw.print(prefix); pw.print("executeNesting="); pw.print(executeNesting);
                    pw.print(" restartCount="); pw.print(restartCount);
                    pw.print(" restartDelay="); pw.print(restartDelay-now);
                    pw.print(" nextRestartTime="); pw.print(nextRestartTime-now);
                    pw.print(" crashCount="); pw.println(crashCount);
        }
        if (deliveredStarts.size() > 0) {
            pw.print(prefix); pw.println("Delivered Starts:");
            dumpStartList(pw, prefix, deliveredStarts, SystemClock.uptimeMillis());
        }
        if (pendingStarts.size() > 0) {
            pw.print(prefix); pw.println("Pending Starts:");
            dumpStartList(pw, prefix, pendingStarts, 0);
        }
        if (bindings.size() > 0) {
            Iterator<IntentBindRecord> it = bindings.values().iterator();
            pw.print(prefix); pw.println("Bindings:");
            while (it.hasNext()) {
                IntentBindRecord b = it.next();
                pw.print(prefix); pw.print("* IntentBindRecord{");
                        pw.print(Integer.toHexString(System.identityHashCode(b)));
                        pw.println("}:");
                b.dumpInService(pw, prefix + "  ");
            }
        }
        if (connections.size() > 0) {
            pw.print(prefix); pw.println("All Connections:");
            Iterator<ConnectionRecord> it = connections.values().iterator();
            while (it.hasNext()) {
                ConnectionRecord c = it.next();
                pw.print(prefix); pw.print("  "); pw.println(c);
            }
        }
    }
    ServiceRecord(ActivityManagerService ams,
            BatteryStatsImpl.Uid.Pkg.Serv servStats, ComponentName name,
            Intent.FilterComparison intent, ServiceInfo sInfo, Runnable restarter) {
        this.ams = ams;
        this.stats = servStats;
        this.name = name;
        shortName = name.flattenToShortString();
        this.intent = intent;
        serviceInfo = sInfo;
        appInfo = sInfo.applicationInfo;
        packageName = sInfo.applicationInfo.packageName;
        processName = sInfo.processName;
        permission = sInfo.permission;
        baseDir = sInfo.applicationInfo.sourceDir;
        resDir = sInfo.applicationInfo.publicSourceDir;
        dataDir = sInfo.applicationInfo.dataDir;
        exported = sInfo.exported;
        this.restarter = restarter;
        createTime = lastActivity = SystemClock.uptimeMillis();
    }
    public AppBindRecord retrieveAppBindingLocked(Intent intent,
            ProcessRecord app) {
        Intent.FilterComparison filter = new Intent.FilterComparison(intent);
        IntentBindRecord i = bindings.get(filter);
        if (i == null) {
            i = new IntentBindRecord(this, filter);
            bindings.put(filter, i);
        }
        AppBindRecord a = i.apps.get(app);
        if (a != null) {
            return a;
        }
        a = new AppBindRecord(this, i, app);
        i.apps.put(app, a);
        return a;
    }
    public void resetRestartCounter() {
        restartCount = 0;
        restartDelay = 0;
        restartTime = 0;
    }
    public StartItem findDeliveredStart(int id, boolean remove) {
        final int N = deliveredStarts.size();
        for (int i=0; i<N; i++) {
            StartItem si = deliveredStarts.get(i);
            if (si.id == id) {
                if (remove) deliveredStarts.remove(i);
                return si;
            }
        }
        return null;
    }
    public void postNotification() {
        if (foregroundId != 0 && foregroundNoti != null) {
            final String localPackageName = packageName;
            final int localForegroundId = foregroundId;
            final Notification localForegroundNoti = foregroundNoti;
            ams.mHandler.post(new Runnable() {
                public void run() {
                    INotificationManager inm = NotificationManager.getService();
                    if (inm == null) {
                        return;
                    }
                    try {
                        int[] outId = new int[1];
                        inm.enqueueNotification(localPackageName, localForegroundId,
                                localForegroundNoti, outId);
                    } catch (RuntimeException e) {
                        Slog.w(ActivityManagerService.TAG,
                                "Error showing notification for service", e);
                        ams.setServiceForeground(name, ServiceRecord.this,
                                localForegroundId, null, true);
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }
    public void cancelNotification() {
        if (foregroundId != 0) {
            final String localPackageName = packageName;
            final int localForegroundId = foregroundId;
            ams.mHandler.post(new Runnable() {
                public void run() {
                    INotificationManager inm = NotificationManager.getService();
                    if (inm == null) {
                        return;
                    }
                    try {
                        inm.cancelNotification(localPackageName, localForegroundId);
                    } catch (RuntimeException e) {
                        Slog.w(ActivityManagerService.TAG,
                                "Error canceling notification for service", e);
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ServiceRecord{")
            .append(Integer.toHexString(System.identityHashCode(this)))
            .append(' ').append(shortName).append('}');
        return stringName = sb.toString();
    }
}
