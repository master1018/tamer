class AppBindRecord {
    final ServiceRecord service;    
    final IntentBindRecord intent;  
    final ProcessRecord client; 
    final HashSet<ConnectionRecord> connections = new HashSet<ConnectionRecord>();
    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "service=" + service);
        pw.println(prefix + "client=" + client);
        dumpInIntentBind(pw, prefix);
    }
    void dumpInIntentBind(PrintWriter pw, String prefix) {
        if (connections.size() > 0) {
            pw.println(prefix + "Per-process Connections:");
            Iterator<ConnectionRecord> it = connections.iterator();
            while (it.hasNext()) {
                ConnectionRecord c = it.next();
                pw.println(prefix + "  " + c);
            }
        }
    }
    AppBindRecord(ServiceRecord _service, IntentBindRecord _intent,
            ProcessRecord _client) {
        service = _service;
        intent = _intent;
        client = _client;
    }
    public String toString() {
        return "AppBindRecord{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + service.shortName + ":" + client.processName + "}";
    }
}
