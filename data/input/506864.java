public class SharedPreferencesBackupHelper extends FileBackupHelperBase implements BackupHelper {
    private static final String TAG = "SharedPreferencesBackupHelper";
    private static final boolean DEBUG = false;
    private Context mContext;
    private String[] mPrefGroups;
    public SharedPreferencesBackupHelper(Context context, String... prefGroups) {
        super(context);
        mContext = context;
        mPrefGroups = prefGroups;
    }
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) {
        Context context = mContext;
        String[] prefGroups = mPrefGroups;
        final int N = prefGroups.length;
        String[] files = new String[N];
        for (int i=0; i<N; i++) {
            files[i] = context.getSharedPrefsFile(prefGroups[i]).getAbsolutePath();
        }
        performBackup_checked(oldState, data, newState, files, prefGroups);
    }
    public void restoreEntity(BackupDataInputStream data) {
        Context context = mContext;
        String key = data.getKey();
        if (DEBUG) Log.d(TAG, "got entity '" + key + "' size=" + data.size());
        if (isKeyInList(key, mPrefGroups)) {
            File f = context.getSharedPrefsFile(key).getAbsoluteFile();
            writeFile(f, data);
        }
    }
}
