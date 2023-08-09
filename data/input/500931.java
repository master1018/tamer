public class Environment {
    private static final File ROOT_DIRECTORY
            = getDirectory("ANDROID_ROOT", "/system");
    private static IMountService mMntSvc = null;
    public static File getRootDirectory() {
        return ROOT_DIRECTORY;
    }
    private static final File DATA_DIRECTORY
            = getDirectory("ANDROID_DATA", "/data");
    private static final File EXTERNAL_STORAGE_DIRECTORY
            = getDirectory("EXTERNAL_STORAGE", "/sdcard");
    private static final File EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY
            = new File (new File(getDirectory("EXTERNAL_STORAGE", "/sdcard"),
                    "Android"), "data");
    private static final File EXTERNAL_STORAGE_ANDROID_MEDIA_DIRECTORY
            = new File (new File(getDirectory("EXTERNAL_STORAGE", "/sdcard"),
                    "Android"), "media");
    private static final File DOWNLOAD_CACHE_DIRECTORY
            = getDirectory("DOWNLOAD_CACHE", "/cache");
    public static File getDataDirectory() {
        return DATA_DIRECTORY;
    }
    public static File getExternalStorageDirectory() {
        return EXTERNAL_STORAGE_DIRECTORY;
    }
    public static String DIRECTORY_MUSIC = "Music";
    public static String DIRECTORY_PODCASTS = "Podcasts";
    public static String DIRECTORY_RINGTONES = "Ringtones";
    public static String DIRECTORY_ALARMS = "Alarms";
    public static String DIRECTORY_NOTIFICATIONS = "Notifications";
    public static String DIRECTORY_PICTURES = "Pictures";
    public static String DIRECTORY_MOVIES = "Movies";
    public static String DIRECTORY_DOWNLOADS = "Download";
    public static String DIRECTORY_DCIM = "DCIM";
    public static File getExternalStoragePublicDirectory(String type) {
        return new File(getExternalStorageDirectory(), type);
    }
    public static File getExternalStorageAndroidDataDir() {
        return EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY;
    }
    public static File getExternalStorageAppDataDirectory(String packageName) {
        return new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY, packageName);
    }
    public static File getExternalStorageAppMediaDirectory(String packageName) {
        return new File(EXTERNAL_STORAGE_ANDROID_MEDIA_DIRECTORY, packageName);
    }
    public static File getExternalStorageAppFilesDirectory(String packageName) {
        return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
                packageName), "files");
    }
    public static File getExternalStorageAppCacheDirectory(String packageName) {
        return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
                packageName), "cache");
    }
    public static File getDownloadCacheDirectory() {
        return DOWNLOAD_CACHE_DIRECTORY;
    }
    public static final String MEDIA_REMOVED = "removed";
    public static final String MEDIA_UNMOUNTED = "unmounted";
    public static final String MEDIA_CHECKING = "checking";
    public static final String MEDIA_NOFS = "nofs";
    public static final String MEDIA_MOUNTED = "mounted";
    public static final String MEDIA_MOUNTED_READ_ONLY = "mounted_ro";
    public static final String MEDIA_SHARED = "shared";
    public static final String MEDIA_BAD_REMOVAL = "bad_removal";
    public static final String MEDIA_UNMOUNTABLE = "unmountable";
    public static String getExternalStorageState() {
        try {
            if (mMntSvc == null) {
                mMntSvc = IMountService.Stub.asInterface(ServiceManager
                                                         .getService("mount"));
            }
            return mMntSvc.getVolumeState(getExternalStorageDirectory().toString());
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }
    }
    static File getDirectory(String variableName, String defaultPath) {
        String path = System.getenv(variableName);
        return path == null ? new File(defaultPath) : new File(path);
    }
}
