public class BackupHelperDispatcher {
    private static final String TAG = "BackupHelperDispatcher";
    private static class Header {
        int chunkSize; 
        String keyPrefix;
    }
    TreeMap<String,BackupHelper> mHelpers = new TreeMap<String,BackupHelper>();
    public BackupHelperDispatcher() {
    }
    public void addHelper(String keyPrefix, BackupHelper helper) {
        mHelpers.put(keyPrefix, helper);
    }
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
             ParcelFileDescriptor newState) throws IOException {
        int err;
        Header header = new Header();
        TreeMap<String,BackupHelper> helpers = (TreeMap<String,BackupHelper>)mHelpers.clone();
        FileDescriptor oldStateFD = null;
        FileDescriptor newStateFD = newState.getFileDescriptor();
        if (oldState != null) {
            oldStateFD = oldState.getFileDescriptor();
            while ((err = readHeader_native(header, oldStateFD)) >= 0) {
                if (err == 0) {
                    BackupHelper helper = helpers.get(header.keyPrefix);
                    Log.d(TAG, "handling existing helper '" + header.keyPrefix + "' " + helper);
                    if (helper != null) {
                        doOneBackup(oldState, data, newState, header, helper);
                        helpers.remove(header.keyPrefix);
                    } else {
                        skipChunk_native(oldStateFD, header.chunkSize);
                    }
                }
            }
        }
        for (Map.Entry<String,BackupHelper> entry: helpers.entrySet()) {
            header.keyPrefix = entry.getKey();
            Log.d(TAG, "handling new helper '" + header.keyPrefix + "'");
            BackupHelper helper = entry.getValue();
            doOneBackup(oldState, data, newState, header, helper);
        }
    }
    private void doOneBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState, Header header, BackupHelper helper) 
            throws IOException {
        int err;
        FileDescriptor newStateFD = newState.getFileDescriptor();
        int pos = allocateHeader_native(header, newStateFD);
        if (pos < 0) {
            throw new IOException("allocateHeader_native failed (error " + pos + ")");
        }
        data.setKeyPrefix(header.keyPrefix);
        helper.performBackup(oldState, data, newState);
        err = writeHeader_native(header, newStateFD, pos);
        if (err != 0) {
            throw new IOException("writeHeader_native failed (error " + err + ")");
        }
    }
    public void performRestore(BackupDataInput input, int appVersionCode,
            ParcelFileDescriptor newState)
            throws IOException {
        boolean alreadyComplained = false;
        BackupDataInputStream stream = new BackupDataInputStream(input);
        while (input.readNextHeader()) {
            String rawKey = input.getKey();
            int pos = rawKey.indexOf(':');
            if (pos > 0) {
                String prefix = rawKey.substring(0, pos);
                BackupHelper helper = mHelpers.get(prefix);
                if (helper != null) {
                    stream.dataSize = input.getDataSize();
                    stream.key = rawKey.substring(pos+1);
                    helper.restoreEntity(stream);
                } else {
                    if (!alreadyComplained) {
                        Log.w(TAG, "Couldn't find helper for: '" + rawKey + "'");
                        alreadyComplained = true;
                    }
                }
            } else {
                if (!alreadyComplained) {
                    Log.w(TAG, "Entity with no prefix: '" + rawKey + "'");
                    alreadyComplained = true;
                }
            }
            input.skipEntityData(); 
        }
        for (BackupHelper helper: mHelpers.values()) {
            helper.writeNewStateDescription(newState);
        }
    }
    private static native int readHeader_native(Header h, FileDescriptor fd);
    private static native int skipChunk_native(FileDescriptor fd, int bytesToSkip);
    private static native int allocateHeader_native(Header h, FileDescriptor fd);
    private static native int writeHeader_native(Header h, FileDescriptor fd, int pos);
}
