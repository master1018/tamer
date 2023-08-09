class UriPermission {
    final int uid;
    final Uri uri;
    int modeFlags = 0;
    int globalModeFlags = 0;
    final HashSet<HistoryRecord> readActivities = new HashSet<HistoryRecord>();
    final HashSet<HistoryRecord> writeActivities = new HashSet<HistoryRecord>();
    String stringName;
    UriPermission(int _uid, Uri _uri) {
        uid = _uid;
        uri = _uri;
    }
    void clearModes(int modeFlags) {
        if ((modeFlags&Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
            globalModeFlags &= ~Intent.FLAG_GRANT_READ_URI_PERMISSION;
            modeFlags &= ~Intent.FLAG_GRANT_READ_URI_PERMISSION;
            if (readActivities.size() > 0) {
                for (HistoryRecord r : readActivities) {
                    r.readUriPermissions.remove(this);
                    if (r.readUriPermissions.size() == 0) {
                        r.readUriPermissions = null;
                    }
                }
                readActivities.clear();
            }
        }
        if ((modeFlags&Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0) {
            globalModeFlags &= ~Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            modeFlags &= ~Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            if (readActivities.size() > 0) {
                for (HistoryRecord r : readActivities) {
                    r.writeUriPermissions.remove(this);
                    if (r.writeUriPermissions.size() == 0) {
                        r.writeUriPermissions = null;
                    }
                }
                readActivities.clear();
            }
        }
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("UriPermission{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(uri);
        sb.append('}');
        return stringName = sb.toString();
    }
    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix); pw.print("modeFlags=0x");
                pw.print(Integer.toHexString(modeFlags));
                pw.print(" uid="); pw.print(uid); 
                pw.print(" globalModeFlags=0x");
                pw.println(Integer.toHexString(globalModeFlags));
        if (readActivities.size() != 0) {
            pw.print(prefix); pw.print("readActivities="); pw.println(readActivities);
        }
        if (writeActivities.size() != 0) {
            pw.print(prefix); pw.print("writeActivities="); pw.println(writeActivities);
        }
    }
}
