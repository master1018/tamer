public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final int LOG_SIZE = 65536;
    private static final File TOMBSTONE_DIR = new File("/data/tombstones");
    private static final String OLD_UPDATER_PACKAGE =
        "com.google.android.systemupdater";
    private static final String OLD_UPDATER_CLASS =
        "com.google.android.systemupdater.SystemUpdateReceiver";
    private static FileObserver sTombstoneObserver = null;
    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            ContentResolver res = context.getContentResolver();
            if (Settings.System.getInt(res, Settings.System.SHOW_PROCESSES, 0) != 0) {
                Intent loadavg = new Intent(context, com.android.server.LoadAverageService.class);
                context.startService(loadavg);
            }
        } catch (Exception e) {
            Slog.e(TAG, "Can't start load average service", e);
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    logBootEvents(context);
                } catch (Exception e) {
                    Slog.e(TAG, "Can't log boot events", e);
                }
                try {
                    removeOldUpdatePackages(context);
                } catch (Exception e) {
                    Slog.e(TAG, "Can't remove old update packages", e);
                }
            }
        }.start();
    }
    private void removeOldUpdatePackages(Context ctx) {
        Downloads.ByUri.removeAllDownloadsByPackage(
            ctx, OLD_UPDATER_PACKAGE, OLD_UPDATER_CLASS);
    }
    private void logBootEvents(Context ctx) throws IOException {
        final DropBoxManager db = (DropBoxManager) ctx.getSystemService(Context.DROPBOX_SERVICE);
        final SharedPreferences prefs = ctx.getSharedPreferences("log_files", Context.MODE_PRIVATE);
        final String headers = new StringBuilder(512)
            .append("Build: ").append(Build.FINGERPRINT).append("\n")
            .append("Hardware: ").append(Build.BOARD).append("\n")
            .append("Bootloader: ").append(Build.BOOTLOADER).append("\n")
            .append("Radio: ").append(Build.RADIO).append("\n")
            .append("Kernel: ")
            .append(FileUtils.readTextFile(new File("/proc/version"), 1024, "...\n"))
            .append("\n").toString();
        String recovery = RecoverySystem.handleAftermath();
        if (recovery != null && db != null) {
            db.addText("SYSTEM_RECOVERY_LOG", headers + recovery);
        }
        if (SystemProperties.getLong("ro.runtime.firstboot", 0) == 0) {
            String now = Long.toString(System.currentTimeMillis());
            SystemProperties.set("ro.runtime.firstboot", now);
            if (db != null) db.addText("SYSTEM_BOOT", headers);
            addFileToDropBox(db, prefs, headers, "/proc/last_kmsg",
                    -LOG_SIZE, "SYSTEM_LAST_KMSG");
            addFileToDropBox(db, prefs, headers, "/cache/recovery/log",
                    -LOG_SIZE, "SYSTEM_RECOVERY_LOG");
            addFileToDropBox(db, prefs, headers, "/data/dontpanic/apanic_console",
                    -LOG_SIZE, "APANIC_CONSOLE");
            addFileToDropBox(db, prefs, headers, "/data/dontpanic/apanic_threads",
                    -LOG_SIZE, "APANIC_THREADS");
        } else {
            if (db != null) db.addText("SYSTEM_RESTART", headers);
        }
        File[] tombstoneFiles = TOMBSTONE_DIR.listFiles();
        for (int i = 0; tombstoneFiles != null && i < tombstoneFiles.length; i++) {
            addFileToDropBox(db, prefs, headers, tombstoneFiles[i].getPath(),
                    LOG_SIZE, "SYSTEM_TOMBSTONE");
        }
        sTombstoneObserver = new FileObserver(TOMBSTONE_DIR.getPath(), FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, String path) {
                try {
                    String filename = new File(TOMBSTONE_DIR, path).getPath();
                    addFileToDropBox(db, prefs, headers, filename, LOG_SIZE, "SYSTEM_TOMBSTONE");
                } catch (IOException e) {
                    Slog.e(TAG, "Can't log tombstone", e);
                }
            }
        };
        sTombstoneObserver.startWatching();
    }
    private static void addFileToDropBox(
            DropBoxManager db, SharedPreferences prefs,
            String headers, String filename, int maxSize, String tag) throws IOException {
        if (db == null || !db.isTagEnabled(tag)) return;  
        File file = new File(filename);
        long fileTime = file.lastModified();
        if (fileTime <= 0) return;  
        if (prefs != null) {
            long lastTime = prefs.getLong(filename, 0);
            if (lastTime == fileTime) return;  
            prefs.edit().putLong(filename, fileTime).commit();
        }
        Slog.i(TAG, "Copying " + filename + " to DropBox (" + tag + ")");
        db.addText(tag, headers + FileUtils.readTextFile(file, maxSize, "[[TRUNCATED]]\n"));
    }
}
