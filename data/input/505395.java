public class LocalTransport extends IBackupTransport.Stub {
    private static final String TAG = "LocalTransport";
    private static final boolean DEBUG = true;
    private static final String TRANSPORT_DIR_NAME
            = "com.android.internal.backup.LocalTransport";
    private static final long RESTORE_TOKEN = 1;
    private Context mContext;
    private PackageManager mPackageManager;
    private File mDataDir = new File(Environment.getDownloadCacheDirectory(), "backup");
    private PackageInfo[] mRestorePackages = null;
    private int mRestorePackage = -1;  
    public LocalTransport(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
    }
    public String transportDirName() {
        return TRANSPORT_DIR_NAME;
    }
    public long requestBackupTime() {
        return 0;
    }
    public int initializeDevice() {
        if (DEBUG) Log.v(TAG, "wiping all data");
        deleteContents(mDataDir);
        return BackupConstants.TRANSPORT_OK;
    }
    public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor data) {
        if (DEBUG) Log.v(TAG, "performBackup() pkg=" + packageInfo.packageName);
        File packageDir = new File(mDataDir, packageInfo.packageName);
        packageDir.mkdirs();
        BackupDataInput changeSet = new BackupDataInput(data.getFileDescriptor());
        try {
            int bufSize = 512;
            byte[] buf = new byte[bufSize];
            while (changeSet.readNextHeader()) {
                String key = changeSet.getKey();
                String base64Key = new String(Base64.encode(key.getBytes()));
                File entityFile = new File(packageDir, base64Key);
                int dataSize = changeSet.getDataSize();
                if (DEBUG) Log.v(TAG, "Got change set key=" + key + " size=" + dataSize
                        + " key64=" + base64Key);
                if (dataSize >= 0) {
                    if (entityFile.exists()) {
                        entityFile.delete();
                    }
                    FileOutputStream entity = new FileOutputStream(entityFile);
                    if (dataSize > bufSize) {
                        bufSize = dataSize;
                        buf = new byte[bufSize];
                    }
                    changeSet.readEntityData(buf, 0, dataSize);
                    if (DEBUG) Log.v(TAG, "  data size " + dataSize);
                    try {
                        entity.write(buf, 0, dataSize);
                    } catch (IOException e) {
                        Log.e(TAG, "Unable to update key file " + entityFile.getAbsolutePath());
                        return BackupConstants.TRANSPORT_ERROR;
                    } finally {
                        entity.close();
                    }
                } else {
                    entityFile.delete();
                }
            }
            return BackupConstants.TRANSPORT_OK;
        } catch (IOException e) {
            Log.v(TAG, "Exception reading backup input:", e);
            return BackupConstants.TRANSPORT_ERROR;
        }
    }
    private void deleteContents(File dirname) {
        File[] contents = dirname.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (f.isDirectory()) {
                    deleteContents(f);
                }
                f.delete();
            }
        }
    }
    public int clearBackupData(PackageInfo packageInfo) {
        if (DEBUG) Log.v(TAG, "clearBackupData() pkg=" + packageInfo.packageName);
        File packageDir = new File(mDataDir, packageInfo.packageName);
        for (File f : packageDir.listFiles()) {
            f.delete();
        }
        packageDir.delete();
        return BackupConstants.TRANSPORT_OK;
    }
    public int finishBackup() {
        if (DEBUG) Log.v(TAG, "finishBackup()");
        return BackupConstants.TRANSPORT_OK;
    }
    public RestoreSet[] getAvailableRestoreSets() throws android.os.RemoteException {
        RestoreSet set = new RestoreSet("Local disk image", "flash", RESTORE_TOKEN);
        RestoreSet[] array = { set };
        return array;
    }
    public long getCurrentRestoreSet() {
        return RESTORE_TOKEN;
    }
    public int startRestore(long token, PackageInfo[] packages) {
        if (DEBUG) Log.v(TAG, "start restore " + token);
        mRestorePackages = packages;
        mRestorePackage = -1;
        return BackupConstants.TRANSPORT_OK;
    }
    public String nextRestorePackage() {
        if (mRestorePackages == null) throw new IllegalStateException("startRestore not called");
        while (++mRestorePackage < mRestorePackages.length) {
            String name = mRestorePackages[mRestorePackage].packageName;
            if (new File(mDataDir, name).isDirectory()) {
                if (DEBUG) Log.v(TAG, "  nextRestorePackage() = " + name);
                return name;
            }
        }
        if (DEBUG) Log.v(TAG, "  no more packages to restore");
        return "";
    }
    public int getRestoreData(ParcelFileDescriptor outFd) {
        if (mRestorePackages == null) throw new IllegalStateException("startRestore not called");
        if (mRestorePackage < 0) throw new IllegalStateException("nextRestorePackage not called");
        File packageDir = new File(mDataDir, mRestorePackages[mRestorePackage].packageName);
        File[] blobs = packageDir.listFiles();
        if (blobs == null) {  
            Log.e(TAG, "Error listing directory: " + packageDir);
            return BackupConstants.TRANSPORT_ERROR;
        }
        if (DEBUG) Log.v(TAG, "  getRestoreData() found " + blobs.length + " key files");
        BackupDataOutput out = new BackupDataOutput(outFd.getFileDescriptor());
        try {
            for (File f : blobs) {
                FileInputStream in = new FileInputStream(f);
                try {
                    int size = (int) f.length();
                    byte[] buf = new byte[size];
                    in.read(buf);
                    String key = new String(Base64.decode(f.getName()));
                    if (DEBUG) Log.v(TAG, "    ... key=" + key + " size=" + size);
                    out.writeEntityHeader(key, size);
                    out.writeEntityData(buf, size);
                } finally {
                    in.close();
                }
            }
            return BackupConstants.TRANSPORT_OK;
        } catch (IOException e) {
            Log.e(TAG, "Unable to read backup records", e);
            return BackupConstants.TRANSPORT_ERROR;
        }
    }
    public void finishRestore() {
        if (DEBUG) Log.v(TAG, "finishRestore()");
    }
}
