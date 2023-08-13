class ConnectionRecord {
    final AppBindRecord binding;    
    final HistoryRecord activity;   
    final IServiceConnection conn;  
    final int flags;                
    final int clientLabel;          
    final PendingIntent clientIntent; 
    String stringName;              
    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "binding=" + binding);
        if (activity != null) {
            pw.println(prefix + "activity=" + activity);
        }
        pw.println(prefix + "conn=" + conn.asBinder()
                + " flags=0x" + Integer.toHexString(flags));
    }
    ConnectionRecord(AppBindRecord _binding, HistoryRecord _activity,
               IServiceConnection _conn, int _flags,
               int _clientLabel, PendingIntent _clientIntent) {
        binding = _binding;
        activity = _activity;
        conn = _conn;
        flags = _flags;
        clientLabel = _clientLabel;
        clientIntent = _clientIntent;
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ConnectionRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(binding.service.shortName);
        sb.append(":@");
        sb.append(Integer.toHexString(System.identityHashCode(conn.asBinder())));
        sb.append('}');
        return stringName = sb.toString();
    }
}
