public class AbsoluteFileBackupHelper extends FileBackupHelperBase implements BackupHelper {
    private static final String TAG = "AbsoluteFileBackupHelper";
    private static final boolean DEBUG = false;
    Context mContext;
    String[] mFiles;
    public AbsoluteFileBackupHelper(Context context, String... files) {
        super(context);
        mContext = context;
        mFiles = files;
    }
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) {
        performBackup_checked(oldState, data, newState, mFiles, mFiles);
    }
    public void restoreEntity(BackupDataInputStream data) {
        if (DEBUG) Log.d(TAG, "got entity '" + data.getKey() + "' size=" + data.size());
        String key = data.getKey();
        if (isKeyInList(key, mFiles)) {
            File f = new File(key);
            writeFile(f, data);
        }
    }
}
