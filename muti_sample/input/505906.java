class BackupRecord {
    public static final int BACKUP_NORMAL = 0;
    public static final int BACKUP_FULL = 1;
    public static final int RESTORE = 2;
    final BatteryStatsImpl.Uid.Pkg.Serv stats;
    String stringName;                     
    final ApplicationInfo appInfo;         
    final int backupMode;                  
    ProcessRecord app;                     
    BackupRecord(BatteryStatsImpl.Uid.Pkg.Serv _agentStats, ApplicationInfo _appInfo,
            int _backupMode) {
        stats = _agentStats;
        appInfo = _appInfo;
        backupMode = _backupMode;
    }
    public String toString() {
        if (stringName != null) {
            return stringName;
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("BackupRecord{")
            .append(Integer.toHexString(System.identityHashCode(this)))
            .append(' ').append(appInfo.packageName)
            .append(' ').append(appInfo.name)
            .append(' ').append(appInfo.backupAgentName).append('}');
        return stringName = sb.toString();
    }
}
