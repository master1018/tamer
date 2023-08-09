public class SystemBackupAgent extends BackupAgentHelper {
    private static final String TAG = "SystemBackupAgent";
    private static final String WALLPAPER_IMAGE = "/data/data/com.android.settings/files/wallpaper";
    private static final String WALLPAPER_INFO = "/data/system/wallpaper_info.xml";
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
            ParcelFileDescriptor newState) throws IOException {
        WallpaperManagerService wallpaper = (WallpaperManagerService)ServiceManager.getService(
                Context.WALLPAPER_SERVICE);
        String[] files = new String[] { WALLPAPER_IMAGE, WALLPAPER_INFO };
        if (wallpaper != null && wallpaper.mName != null && wallpaper.mName.length() > 0) {
            files = new String[] { WALLPAPER_INFO };
        }
        addHelper("wallpaper", new AbsoluteFileBackupHelper(SystemBackupAgent.this, files));
        super.onBackup(oldState, data, newState);
    }
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState)
            throws IOException {
        addHelper("wallpaper", new AbsoluteFileBackupHelper(SystemBackupAgent.this,
                new String[] { WALLPAPER_IMAGE, WALLPAPER_INFO }));
        addHelper("system_files", new AbsoluteFileBackupHelper(SystemBackupAgent.this,
                new String[] { WALLPAPER_IMAGE }));
        boolean success = false;
        try {
            super.onRestore(data, appVersionCode, newState);
            WallpaperManagerService wallpaper = (WallpaperManagerService)ServiceManager.getService(
                    Context.WALLPAPER_SERVICE);
            wallpaper.settingsRestored();
        } catch (IOException ex) {
            Slog.d(TAG, "restore failed", ex);
            (new File(WALLPAPER_IMAGE)).delete();
            (new File(WALLPAPER_INFO)).delete();
        }
    }
}
