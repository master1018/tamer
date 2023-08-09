public class RenamingDelegatingContext extends ContextWrapper {
    private Context mFileContext;
    private String mFilePrefix = null;
    private File mCacheDir;
    private final Object mSync = new Object();
    private Set<String> mDatabaseNames = Sets.newHashSet();
    private Set<String> mFileNames = Sets.newHashSet();
    public static <T extends ContentProvider> T providerWithRenamedContext(
            Class<T> contentProvider, Context c, String filePrefix)
            throws IllegalAccessException, InstantiationException {
        return providerWithRenamedContext(contentProvider, c, filePrefix, false);
    }
    public static <T extends ContentProvider> T providerWithRenamedContext(
            Class<T> contentProvider, Context c, String filePrefix,
            boolean allowAccessToExistingFilesAndDbs)
            throws IllegalAccessException, InstantiationException {
        Class<T> mProviderClass = contentProvider;
        T mProvider = mProviderClass.newInstance();
        RenamingDelegatingContext mContext = new RenamingDelegatingContext(c, filePrefix);
        if (allowAccessToExistingFilesAndDbs) {
            mContext.makeExistingFilesAndDbsAccessible();
        }
        mProvider.attachInfo(mContext, null);
        return mProvider;
    }
    public void makeExistingFilesAndDbsAccessible() {
        String[] databaseList = mFileContext.databaseList();
        for (String diskName : databaseList) {
            if (shouldDiskNameBeVisible(diskName)) {
                mDatabaseNames.add(publicNameFromDiskName(diskName));
            }
        }
        String[] fileList = mFileContext.fileList();
        for (String diskName : fileList) {
            if (shouldDiskNameBeVisible(diskName)) {
                mFileNames.add(publicNameFromDiskName(diskName));
            }
        }
    }
    boolean shouldDiskNameBeVisible(String diskName) {
        return diskName.startsWith(mFilePrefix);
    }
    String publicNameFromDiskName(String diskName) {
        if (!shouldDiskNameBeVisible(diskName)) {
            throw new IllegalArgumentException("disk file should not be visible: " + diskName);
        }
        return diskName.substring(mFilePrefix.length(), diskName.length());
    }
    public RenamingDelegatingContext(Context context, String filePrefix) {
        super(context);
        mFileContext = context;
        mFilePrefix = filePrefix;
    }
    public RenamingDelegatingContext(Context context, Context fileContext, String filePrefix) {
        super(context);
        mFileContext = fileContext;
        mFilePrefix = filePrefix;
    }
    public String getDatabasePrefix() {
        return mFilePrefix;
    }
    private String renamedFileName(String name) {
        return mFilePrefix + name;
    }
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name,
            int mode, SQLiteDatabase.CursorFactory factory) {
        final String internalName = renamedFileName(name);
        if (!mDatabaseNames.contains(name)) {
            mDatabaseNames.add(name);
            mFileContext.deleteDatabase(internalName);
        }
        return mFileContext.openOrCreateDatabase(internalName, mode, factory);
    }
    @Override
    public boolean deleteDatabase(String name) {
        if (mDatabaseNames.contains(name)) {
            mDatabaseNames.remove(name);
            return mFileContext.deleteDatabase(renamedFileName(name));
        } else {
            return false;
        }
    }
    @Override
    public File getDatabasePath(String name) {
        return mFileContext.getDatabasePath(renamedFileName(name));
    }
    @Override
    public String[] databaseList() {
        return mDatabaseNames.toArray(new String[]{});
    }
    @Override
    public FileInputStream openFileInput(String name)
            throws FileNotFoundException {
        final String internalName = renamedFileName(name);
        if (mFileNames.contains(name)) {
            return mFileContext.openFileInput(internalName);
        } else {
            throw new FileNotFoundException(internalName);
        }
    }
    @Override
    public FileOutputStream openFileOutput(String name, int mode)
            throws FileNotFoundException {
        mFileNames.add(name);
        return mFileContext.openFileOutput(renamedFileName(name), mode);
    }
    @Override
    public File getFileStreamPath(String name) {
        return mFileContext.getFileStreamPath(renamedFileName(name));
    }
    @Override
    public boolean deleteFile(String name) {
        if (mFileNames.contains(name)) {
            mFileNames.remove(name);
            return mFileContext.deleteFile(renamedFileName(name));
        } else {
            return false;
        }
    }
    @Override
    public String[] fileList() {
        return mFileNames.toArray(new String[]{});
    }
    @Override
    public File getCacheDir() {
        synchronized (mSync) {
            if (mCacheDir == null) {
                mCacheDir = new File(mFileContext.getCacheDir(), renamedFileName("cache"));
            }
            if (!mCacheDir.exists()) {
                if(!mCacheDir.mkdirs()) {
                    Log.w("RenamingDelegatingContext", "Unable to create cache directory");
                    return null;
                }
                FileUtils.setPermissions(
                        mCacheDir.getPath(),
                        FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IXOTH,
                        -1, -1);
            }
        }
        return mCacheDir;
    }
}
