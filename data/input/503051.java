public class PackageManagerBackupAgent extends BackupAgent {
    private static final String TAG = "PMBA";
    private static final boolean DEBUG = false;
    private static final String GLOBAL_METADATA_KEY = "@meta@";
    private List<PackageInfo> mAllPackages;
    private PackageManager mPackageManager;
    private HashMap<String, Metadata> mRestoredSignatures;
    private HashMap<String, Metadata> mStateVersions = new HashMap<String, Metadata>();
    private final HashSet<String> mExisting = new HashSet<String>();
    private int mStoredSdkVersion;
    private String mStoredIncrementalVersion;
    private boolean mHasMetadata;
    public class Metadata {
        public int versionCode;
        public Signature[] signatures;
        Metadata(int version, Signature[] sigs) {
            versionCode = version;
            signatures = sigs;
        }
    }
    PackageManagerBackupAgent(PackageManager packageMgr, List<PackageInfo> packages) {
        mPackageManager = packageMgr;
        mAllPackages = packages;
        mRestoredSignatures = null;
        mHasMetadata = false;
    }
    public boolean hasMetadata() {
        return mHasMetadata;
    }
    public Metadata getRestoredMetadata(String packageName) {
        if (mRestoredSignatures == null) {
            Slog.w(TAG, "getRestoredMetadata() before metadata read!");
            return null;
        }
        return mRestoredSignatures.get(packageName);
    }
    public Set<String> getRestoredPackages() {
        if (mRestoredSignatures == null) {
            Slog.w(TAG, "getRestoredPackages() before metadata read!");
            return null;
        }
        return mRestoredSignatures.keySet();
    }
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) {
        if (DEBUG) Slog.v(TAG, "onBackup()");
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();  
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        parseStateFile(oldState);
        if (mStoredIncrementalVersion == null
                || !mStoredIncrementalVersion.equals(Build.VERSION.INCREMENTAL)) {
            Slog.i(TAG, "Previous metadata " + mStoredIncrementalVersion + " mismatch vs "
                    + Build.VERSION.INCREMENTAL + " - rewriting");
            mExisting.clear();
        }
        try {
            if (!mExisting.contains(GLOBAL_METADATA_KEY)) {
                if (DEBUG) Slog.v(TAG, "Storing global metadata key");
                outputBufferStream.writeInt(Build.VERSION.SDK_INT);
                outputBufferStream.writeUTF(Build.VERSION.INCREMENTAL);
                writeEntity(data, GLOBAL_METADATA_KEY, outputBuffer.toByteArray());
            } else {
                if (DEBUG) Slog.v(TAG, "Global metadata key already stored");
                mExisting.remove(GLOBAL_METADATA_KEY);
            }
            for (PackageInfo pkg : mAllPackages) {
                String packName = pkg.packageName;
                if (packName.equals(GLOBAL_METADATA_KEY)) {
                    continue;
                } else {
                    PackageInfo info = null;
                    try {
                        info = mPackageManager.getPackageInfo(packName,
                                PackageManager.GET_SIGNATURES);
                    } catch (NameNotFoundException e) {
                        mExisting.add(packName);
                        continue;
                    }
                    if (mExisting.contains(packName)) {
                        mExisting.remove(packName);
                        if (info.versionCode == mStateVersions.get(packName).versionCode) {
                            continue;
                        }
                    }
                    if (info.signatures == null || info.signatures.length == 0)
                    {
                        Slog.w(TAG, "Not backing up package " + packName
                                + " since it appears to have no signatures.");
                        continue;
                    }
                    outputBuffer.reset();
                    outputBufferStream.writeInt(info.versionCode);
                    writeSignatureArray(outputBufferStream, info.signatures);
                    if (DEBUG) {
                        Slog.v(TAG, "+ writing metadata for " + packName
                                + " version=" + info.versionCode
                                + " entityLen=" + outputBuffer.size());
                    }
                    writeEntity(data, packName, outputBuffer.toByteArray());
                }
            }
            for (String app : mExisting) {
                if (DEBUG) Slog.v(TAG, "- removing metadata for deleted pkg " + app);
                try {
                    data.writeEntityHeader(app, -1);
                } catch (IOException e) {
                    Slog.e(TAG, "Unable to write package deletions!");
                    return;
                }
            }
        } catch (IOException e) {
            Slog.e(TAG, "Unable to write package backup data file!");
            return;
        }
        writeStateFile(mAllPackages, newState);
    }
    private static void writeEntity(BackupDataOutput data, String key, byte[] bytes)
            throws IOException {
        data.writeEntityHeader(key, bytes.length);
        data.writeEntityData(bytes, bytes.length);
    }
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
            throws IOException {
        List<ApplicationInfo> restoredApps = new ArrayList<ApplicationInfo>();
        HashMap<String, Metadata> sigMap = new HashMap<String, Metadata>();
        if (DEBUG) Slog.v(TAG, "onRestore()");
        int storedSystemVersion = -1;
        while (data.readNextHeader()) {
            String key = data.getKey();
            int dataSize = data.getDataSize();
            if (DEBUG) Slog.v(TAG, "   got key=" + key + " dataSize=" + dataSize);
            byte[] inputBytes = new byte[dataSize];
            data.readEntityData(inputBytes, 0, dataSize);
            ByteArrayInputStream inputBuffer = new ByteArrayInputStream(inputBytes);
            DataInputStream inputBufferStream = new DataInputStream(inputBuffer);
            if (key.equals(GLOBAL_METADATA_KEY)) {
                int storedSdkVersion = inputBufferStream.readInt();
                if (DEBUG) Slog.v(TAG, "   storedSystemVersion = " + storedSystemVersion);
                if (storedSystemVersion > Build.VERSION.SDK_INT) {
                    Slog.w(TAG, "Restore set was from a later version of Android; not restoring");
                    return;
                }
                mStoredSdkVersion = storedSdkVersion;
                mStoredIncrementalVersion = inputBufferStream.readUTF();
                mHasMetadata = true;
                if (DEBUG) {
                    Slog.i(TAG, "Restore set version " + storedSystemVersion
                            + " is compatible with OS version " + Build.VERSION.SDK_INT
                            + " (" + mStoredIncrementalVersion + " vs "
                            + Build.VERSION.INCREMENTAL + ")");
                }
            } else {
                int versionCode = inputBufferStream.readInt();
                Signature[] sigs = readSignatureArray(inputBufferStream);
                if (DEBUG) {
                    Slog.i(TAG, "   read metadata for " + key
                            + " dataSize=" + dataSize
                            + " versionCode=" + versionCode + " sigs=" + sigs);
                }
                if (sigs == null || sigs.length == 0) {
                    Slog.w(TAG, "Not restoring package " + key
                            + " since it appears to have no signatures.");
                    continue;
                }
                ApplicationInfo app = new ApplicationInfo();
                app.packageName = key;
                restoredApps.add(app);
                sigMap.put(key, new Metadata(versionCode, sigs));
            }
        }
        mRestoredSignatures = sigMap;
    }
    private static void writeSignatureArray(DataOutputStream out, Signature[] sigs)
            throws IOException {
        out.writeInt(sigs.length);
        for (Signature sig : sigs) {
            byte[] flat = sig.toByteArray();
            out.writeInt(flat.length);
            out.write(flat);
        }
    }
    private static Signature[] readSignatureArray(DataInputStream in) {
        try {
            int num;
            try {
                num = in.readInt();
            } catch (EOFException e) {
                Slog.w(TAG, "Read empty signature block");
                return null;
            }
            if (DEBUG) Slog.v(TAG, " ... unflatten read " + num);
            if (num > 20) {
                Slog.e(TAG, "Suspiciously large sig count in restore data; aborting");
                throw new IllegalStateException("Bad restore state");
            }
            Signature[] sigs = new Signature[num];
            for (int i = 0; i < num; i++) {
                int len = in.readInt();
                byte[] flatSig = new byte[len];
                in.read(flatSig);
                sigs[i] = new Signature(flatSig);
            }
            return sigs;
        } catch (IOException e) {
            Slog.e(TAG, "Unable to read signatures");
            return null;
        }
    }
    private void parseStateFile(ParcelFileDescriptor stateFile) {
        mExisting.clear();
        mStateVersions.clear();
        mStoredSdkVersion = 0;
        mStoredIncrementalVersion = null;
        FileInputStream instream = new FileInputStream(stateFile.getFileDescriptor());
        DataInputStream in = new DataInputStream(instream);
        int bufSize = 256;
        byte[] buf = new byte[bufSize];
        try {
            String pkg = in.readUTF();
            if (pkg.equals(GLOBAL_METADATA_KEY)) {
                mStoredSdkVersion = in.readInt();
                mStoredIncrementalVersion = in.readUTF();
                mExisting.add(GLOBAL_METADATA_KEY);
            } else {
                Slog.e(TAG, "No global metadata in state file!");
                return;
            }
            while (true) {
                pkg = in.readUTF();
                int versionCode = in.readInt();
                mExisting.add(pkg);
                mStateVersions.put(pkg, new Metadata(versionCode, null));
            }
        } catch (EOFException eof) {
        } catch (IOException e) {
            Slog.e(TAG, "Unable to read Package Manager state file: " + e);
        }
    }
    private void writeStateFile(List<PackageInfo> pkgs, ParcelFileDescriptor stateFile) {
        FileOutputStream outstream = new FileOutputStream(stateFile.getFileDescriptor());
        DataOutputStream out = new DataOutputStream(outstream);
        try {
            out.writeUTF(GLOBAL_METADATA_KEY);
            out.writeInt(Build.VERSION.SDK_INT);
            out.writeUTF(Build.VERSION.INCREMENTAL);
            for (PackageInfo pkg : pkgs) {
                out.writeUTF(pkg.packageName);
                out.writeInt(pkg.versionCode);
            }
        } catch (IOException e) {
            Slog.e(TAG, "Unable to write package manager state file!");
            return;
        }
    }
}
