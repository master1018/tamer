class IntentBindRecord {
    final ServiceRecord service;
    final Intent.FilterComparison intent; 
    final HashMap<ProcessRecord, AppBindRecord> apps
            = new HashMap<ProcessRecord, AppBindRecord>();
    IBinder binder;
    boolean requested;
    boolean received;
    boolean hasBound;
    boolean doRebind;
    String stringName;      
    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("service="); pw.println(service);
        dumpInService(pw, prefix);
    }
    void dumpInService(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("intent={");
                pw.print(intent.getIntent().toShortString(true, false));
                pw.println('}');
        pw.print(prefix); pw.print("binder="); pw.println(binder);
        pw.print(prefix); pw.print("requested="); pw.print(requested);
                pw.print(" received="); pw.print(received);
                pw.print(" hasBound="); pw.print(hasBound);
                pw.print(" doRebind="); pw.println(doRebind);
        if (apps.size() > 0) {
            Iterator<AppBindRecord> it = apps.values().iterator();
            while (it.hasNext()) {
                AppBindRecord a = it.next();
                pw.print(prefix); pw.print("* Client AppBindRecord{");
                        pw.print(Integer.toHexString(System.identityHashCode(a)));
                        pw.print(' '); pw.print(a.client); pw.println('}');
                a.dumpInIntentBind(pw, prefix + "  ");
            }
        }
    }
    IntentBindRecord(ServiceRecord _service, Intent.FilterComparison _intent) {
        service = _service;
        intent = _intent;
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentBindRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(service.shortName);
        sb.append(':');
        if (intent != null) {
            intent.getIntent().toShortString(sb, false, false);
        }
        sb.append('}');
        return stringName = sb.toString();
    }
}
