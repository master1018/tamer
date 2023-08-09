public class FileHelperExampleAgent extends BackupAgentHelper {
    static final String FILE_HELPER_KEY = "the_file";
    @Override
    public void onCreate() {
        FileBackupHelper helper = new FileBackupHelper(this, BackupRestoreActivity.DATA_FILE_NAME);
        addHelper(FILE_HELPER_KEY, helper);
    }
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
             ParcelFileDescriptor newState) throws IOException {
        synchronized (BackupRestoreActivity.sDataLock) {
            super.onBackup(oldState, data, newState);
        }
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
            ParcelFileDescriptor newState) throws IOException {
        synchronized (BackupRestoreActivity.sDataLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }
}
