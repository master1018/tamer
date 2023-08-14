class TaskRecord {
    final int taskId;       
    final String affinity;  
    final boolean clearOnBackground; 
    Intent intent;          
    Intent affinityIntent;  
    ComponentName origActivity; 
    ComponentName realActivity; 
    int numActivities;      
    long lastActiveTime;    
    boolean rootWasReset;   
    String stringName;      
    TaskRecord(int _taskId, ActivityInfo info, Intent _intent,
            boolean _clearOnBackground) {
        taskId = _taskId;
        affinity = info.taskAffinity;
        clearOnBackground = _clearOnBackground;
        setIntent(_intent, info);
    }
    void touchActiveTime() {
        lastActiveTime = android.os.SystemClock.elapsedRealtime();
    }
    long getInactiveDuration() {
        return android.os.SystemClock.elapsedRealtime() - lastActiveTime;
    }
    void setIntent(Intent _intent, ActivityInfo info) {
        stringName = null;
        if (info.targetActivity == null) {
            intent = _intent;
            realActivity = _intent != null ? _intent.getComponent() : null;
            origActivity = null;
        } else {
            ComponentName targetComponent = new ComponentName(
                    info.packageName, info.targetActivity);
            if (_intent != null) {
                Intent targetIntent = new Intent(_intent);
                targetIntent.setComponent(targetComponent);
                intent = targetIntent;
                realActivity = targetComponent;
                origActivity = _intent.getComponent();
            } else {
                intent = null;
                realActivity = targetComponent;
                origActivity = new ComponentName(info.packageName, info.name);
            }
        }
        if (intent != null &&
                (intent.getFlags()&Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED) != 0) {
            rootWasReset = true;
        }
    }
    void dump(PrintWriter pw, String prefix) {
        if (clearOnBackground || numActivities != 0 || rootWasReset) {
            pw.print(prefix); pw.print("clearOnBackground="); pw.print(clearOnBackground);
                    pw.print(" numActivities="); pw.print(numActivities);
                    pw.print(" rootWasReset="); pw.println(rootWasReset);
        }
        if (affinity != null) {
            pw.print(prefix); pw.print("affinity="); pw.println(affinity);
        }
        if (intent != null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(prefix); sb.append("intent={");
            intent.toShortString(sb, true, false);
            sb.append('}');
            pw.println(sb.toString());
        }
        if (affinityIntent != null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(prefix); sb.append("affinityIntent={");
            affinityIntent.toShortString(sb, true, false);
            sb.append('}');
            pw.println(sb.toString());
        }
        if (origActivity != null) {
            pw.print(prefix); pw.print("origActivity=");
            pw.println(origActivity.flattenToShortString());
        }
        if (realActivity != null) {
            pw.print(prefix); pw.print("realActivity=");
            pw.println(realActivity.flattenToShortString());
        }
        pw.print(prefix); pw.print("lastActiveTime="); pw.print(lastActiveTime);
                pw.print(" (inactive for ");
                pw.print((getInactiveDuration()/1000)); pw.println("s)");
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("TaskRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" #");
        sb.append(taskId);
        if (affinity != null) {
            sb.append(" A ");
            sb.append(affinity);
        } else if (intent != null) {
            sb.append(" I ");
            sb.append(intent.getComponent().flattenToShortString());
        } else if (affinityIntent != null) {
            sb.append(" aI ");
            sb.append(affinityIntent.getComponent().flattenToShortString());
        } else {
            sb.append(" ??");
        }
        sb.append('}');
        return stringName = sb.toString();
    }
}
