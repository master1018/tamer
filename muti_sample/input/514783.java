public abstract class Context {
    public static final int MODE_PRIVATE = 0x0000;
    public static final int MODE_WORLD_READABLE = 0x0001;
    public static final int MODE_WORLD_WRITEABLE = 0x0002;
    public static final int MODE_APPEND = 0x8000;
    public static final int BIND_AUTO_CREATE = 0x0001;
    public static final int BIND_DEBUG_UNBIND = 0x0002;
    public static final int BIND_NOT_FOREGROUND = 0x0004;
    public abstract AssetManager getAssets();
    public abstract Resources getResources();
    public abstract PackageManager getPackageManager();
    public abstract ContentResolver getContentResolver();
    public abstract Looper getMainLooper();
    public abstract Context getApplicationContext();
    public final CharSequence getText(int resId) {
        return getResources().getText(resId);
    }
    public final String getString(int resId) {
        return getResources().getString(resId);
    }
    public final String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }
    public abstract void setTheme(int resid);
    public abstract Resources.Theme getTheme();
    public final TypedArray obtainStyledAttributes(
            int[] attrs) {
        return getTheme().obtainStyledAttributes(attrs);
    }
    public final TypedArray obtainStyledAttributes(
            int resid, int[] attrs) throws Resources.NotFoundException {
        return getTheme().obtainStyledAttributes(resid, attrs);
    }
    public final TypedArray obtainStyledAttributes(
            AttributeSet set, int[] attrs) {
        return getTheme().obtainStyledAttributes(set, attrs, 0, 0);
    }
    public final TypedArray obtainStyledAttributes(
            AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
        return getTheme().obtainStyledAttributes(
            set, attrs, defStyleAttr, defStyleRes);
    }
    public abstract ClassLoader getClassLoader();
    public abstract String getPackageName();
    public abstract ApplicationInfo getApplicationInfo();
    public abstract String getPackageResourcePath();
    public abstract String getPackageCodePath();
    public abstract File getSharedPrefsFile(String name);
    public abstract SharedPreferences getSharedPreferences(String name,
            int mode);
    public abstract FileInputStream openFileInput(String name)
        throws FileNotFoundException;
    public abstract FileOutputStream openFileOutput(String name, int mode)
        throws FileNotFoundException;
    public abstract boolean deleteFile(String name);
    public abstract File getFileStreamPath(String name);
    public abstract File getFilesDir();
    public abstract File getExternalFilesDir(String type);
    public abstract File getCacheDir();
    public abstract File getExternalCacheDir();
    public abstract String[] fileList();
    public abstract File getDir(String name, int mode);
    public abstract SQLiteDatabase openOrCreateDatabase(String name,
            int mode, CursorFactory factory);
    public abstract boolean deleteDatabase(String name);
    public abstract File getDatabasePath(String name);
    public abstract String[] databaseList();
    @Deprecated
    public abstract Drawable getWallpaper();
    @Deprecated
    public abstract Drawable peekWallpaper();
    @Deprecated
    public abstract int getWallpaperDesiredMinimumWidth();
    @Deprecated
    public abstract int getWallpaperDesiredMinimumHeight();
    @Deprecated
    public abstract void setWallpaper(Bitmap bitmap) throws IOException;
    @Deprecated
    public abstract void setWallpaper(InputStream data) throws IOException;
    @Deprecated
    public abstract void clearWallpaper() throws IOException;
    public abstract void startActivity(Intent intent);
    public abstract void startIntentSender(IntentSender intent,
            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws IntentSender.SendIntentException;
    public abstract void sendBroadcast(Intent intent);
    public abstract void sendBroadcast(Intent intent,
            String receiverPermission);
    public abstract void sendOrderedBroadcast(Intent intent,
            String receiverPermission);
    public abstract void sendOrderedBroadcast(Intent intent,
            String receiverPermission, BroadcastReceiver resultReceiver,
            Handler scheduler, int initialCode, String initialData,
            Bundle initialExtras);
    public abstract void sendStickyBroadcast(Intent intent);
    public abstract void sendStickyOrderedBroadcast(Intent intent,
            BroadcastReceiver resultReceiver,
            Handler scheduler, int initialCode, String initialData,
            Bundle initialExtras);
    public abstract void removeStickyBroadcast(Intent intent);
    public abstract Intent registerReceiver(BroadcastReceiver receiver,
                                            IntentFilter filter);
    public abstract Intent registerReceiver(BroadcastReceiver receiver,
                                            IntentFilter filter,
                                            String broadcastPermission,
                                            Handler scheduler);
    public abstract void unregisterReceiver(BroadcastReceiver receiver);
    public abstract ComponentName startService(Intent service);
    public abstract boolean stopService(Intent service);
    public abstract boolean bindService(Intent service, ServiceConnection conn,
            int flags);
    public abstract void unbindService(ServiceConnection conn);
    public abstract boolean startInstrumentation(ComponentName className,
            String profileFile, Bundle arguments);
    public abstract Object getSystemService(String name);
    public static final String POWER_SERVICE = "power";
    public static final String WINDOW_SERVICE = "window";
    public static final String LAYOUT_INFLATER_SERVICE = "layout_inflater";
    public static final String ACCOUNT_SERVICE = "account";
    public static final String ACTIVITY_SERVICE = "activity";
    public static final String ALARM_SERVICE = "alarm";
    public static final String NOTIFICATION_SERVICE = "notification";
    public static final String ACCESSIBILITY_SERVICE = "accessibility";
    public static final String KEYGUARD_SERVICE = "keyguard";
    public static final String LOCATION_SERVICE = "location";
    public static final String SEARCH_SERVICE = "search";
    public static final String SENSOR_SERVICE = "sensor";
    public static final String STORAGE_SERVICE = "storage";
    public static final String WALLPAPER_SERVICE = "wallpaper";
    public static final String VIBRATOR_SERVICE = "vibrator";
    public static final String STATUS_BAR_SERVICE = "statusbar";
    public static final String CONNECTIVITY_SERVICE = "connectivity";
    public static final String THROTTLE_SERVICE = "throttle";
    public static final String NETWORKMANAGEMENT_SERVICE = "network_management";
    public static final String WIFI_SERVICE = "wifi";
    public static final String AUDIO_SERVICE = "audio";
    public static final String TELEPHONY_SERVICE = "phone";
    public static final String CLIPBOARD_SERVICE = "clipboard";
    public static final String INPUT_METHOD_SERVICE = "input_method";
    public static final String APPWIDGET_SERVICE = "appwidget";
    public static final String BACKUP_SERVICE = "backup";
    public static final String DROPBOX_SERVICE = "dropbox";
    public static final String DEVICE_POLICY_SERVICE = "device_policy";
    public static final String UI_MODE_SERVICE = "uimode";
    public abstract int checkPermission(String permission, int pid, int uid);
    public abstract int checkCallingPermission(String permission);
    public abstract int checkCallingOrSelfPermission(String permission);
    public abstract void enforcePermission(
            String permission, int pid, int uid, String message);
    public abstract void enforceCallingPermission(
            String permission, String message);
    public abstract void enforceCallingOrSelfPermission(
            String permission, String message);
    public abstract void grantUriPermission(String toPackage, Uri uri,
            int modeFlags);
    public abstract void revokeUriPermission(Uri uri, int modeFlags);
    public abstract int checkUriPermission(Uri uri, int pid, int uid, int modeFlags);
    public abstract int checkCallingUriPermission(Uri uri, int modeFlags);
    public abstract int checkCallingOrSelfUriPermission(Uri uri, int modeFlags);
    public abstract int checkUriPermission(Uri uri, String readPermission,
            String writePermission, int pid, int uid, int modeFlags);
    public abstract void enforceUriPermission(
            Uri uri, int pid, int uid, int modeFlags, String message);
    public abstract void enforceCallingUriPermission(
            Uri uri, int modeFlags, String message);
    public abstract void enforceCallingOrSelfUriPermission(
            Uri uri, int modeFlags, String message);
    public abstract void enforceUriPermission(
            Uri uri, String readPermission, String writePermission,
            int pid, int uid, int modeFlags, String message);
    public static final int CONTEXT_INCLUDE_CODE = 0x00000001;
    public static final int CONTEXT_IGNORE_SECURITY = 0x00000002;
    public static final int CONTEXT_RESTRICTED = 0x00000004;
    public abstract Context createPackageContext(String packageName,
            int flags) throws PackageManager.NameNotFoundException;
    public boolean isRestricted() {
        return false;
    }
}
