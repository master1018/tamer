class ContentProviderRecord extends ContentProviderHolder {
    final HashSet<ProcessRecord> clients = new HashSet<ProcessRecord>();
    final int uid;
    final ApplicationInfo appInfo;
    int externals;     
    ProcessRecord app; 
    ProcessRecord launchingApp; 
    String stringName;
    public ContentProviderRecord(ProviderInfo _info, ApplicationInfo ai) {
        super(_info);
        uid = ai.uid;
        appInfo = ai;
        noReleaseNeeded = uid == 0 || uid == Process.SYSTEM_UID;
    }
    public ContentProviderRecord(ContentProviderRecord cpr) {
        super(cpr.info);
        uid = cpr.uid;
        appInfo = cpr.appInfo;
        noReleaseNeeded = cpr.noReleaseNeeded;
    }
    public boolean canRunHere(ProcessRecord app) {
        return (info.multiprocess || info.processName.equals(app.processName))
                && (uid == Process.SYSTEM_UID || uid == app.info.uid);
    }
    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("package=");
                pw.print(info.applicationInfo.packageName);
                pw.print("process="); pw.println(info.processName);
        pw.print(prefix); pw.print("app="); pw.println(app);
        if (launchingApp != null) {
            pw.print(prefix); pw.print("launchingApp="); pw.println(launchingApp);
        }
        pw.print(prefix); pw.print("uid="); pw.print(uid);
                pw.print(" provider="); pw.println(provider);
        pw.print(prefix); pw.print("name="); pw.println(info.authority);
        if (info.isSyncable || info.multiprocess || info.initOrder != 0) {
            pw.print(prefix); pw.print("isSyncable="); pw.print(info.isSyncable);
                    pw.print("multiprocess="); pw.print(info.multiprocess);
                    pw.print(" initOrder="); pw.println(info.initOrder);
        }
        if (clients.size() > 0) {
            pw.print(prefix); pw.print("clients="); pw.println(clients);
        }
        if (externals != 0) {
            pw.print(prefix); pw.print("externals="); pw.println(externals);
        }
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ContentProviderRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(info.name);
        sb.append('}');
        return stringName = sb.toString();
    }
}
