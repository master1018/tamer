class WebStorageSizeManager {
    private final static boolean LOGV_ENABLED = com.android.browser.Browser.LOGV_ENABLED;
    private final static boolean LOGD_ENABLED = com.android.browser.Browser.LOGD_ENABLED;
    private final static String LOGTAG = "browser";
    public final static long ORIGIN_DEFAULT_QUOTA = 3 * 1024 * 1024;  
    public final static long QUOTA_INCREASE_STEP = 1 * 1024 * 1024;  
    public final static long APPCACHE_MAXSIZE_PADDING = 512 * 1024; 
    private final static int OUT_OF_SPACE_ID = 1;
    private static long mLastOutOfSpaceNotificationTime = -1;
    private final static long NOTIFICATION_INTERVAL = 5 * 60 * 1000;
    private final static long RESET_NOTIFICATION_INTERVAL = 3 * 1000;
    private final Context mContext;
    private final long mGlobalLimit;
    private long mAppCacheMaxSize;
    public interface DiskInfo {
        public long getFreeSpaceSizeBytes();
        public long getTotalSizeBytes();
    };
    private DiskInfo mDiskInfo;
    public static class StatFsDiskInfo implements DiskInfo {
        private StatFs mFs;
        public StatFsDiskInfo(String path) {
            mFs = new StatFs(path);
        }
        public long getFreeSpaceSizeBytes() {
            return mFs.getAvailableBlocks() * mFs.getBlockSize();
        }
        public long getTotalSizeBytes() {
            return mFs.getBlockCount() * mFs.getBlockSize();
        }
    };
    public interface AppCacheInfo {
        public long getAppCacheSizeBytes();
    };
    public static class WebKitAppCacheInfo implements AppCacheInfo {
        private final static String APPCACHE_FILE = "ApplicationCache.db";
        private String mAppCachePath;
        public WebKitAppCacheInfo(String path) {
            mAppCachePath = path;
        }
        public long getAppCacheSizeBytes() {
            File file = new File(mAppCachePath
                    + File.separator
                    + APPCACHE_FILE);
            return file.length();
        }
    };
    public WebStorageSizeManager(Context ctx, DiskInfo diskInfo,
            AppCacheInfo appCacheInfo) {
        mContext = ctx;
        mDiskInfo = diskInfo;
        mGlobalLimit = getGlobalLimit();
        mAppCacheMaxSize = Math.max(mGlobalLimit / 4,
                appCacheInfo.getAppCacheSizeBytes());
    }
    public long getAppCacheMaxSize() {
        return mAppCacheMaxSize;
    }
    public void onExceededDatabaseQuota(String url,
        String databaseIdentifier, long currentQuota, long estimatedSize,
        long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
        if(LOGV_ENABLED) {
            Log.v(LOGTAG,
                  "Received onExceededDatabaseQuota for "
                  + url
                  + ":"
                  + databaseIdentifier
                  + "(current quota: "
                  + currentQuota
                  + ", total used quota: "
                  + totalUsedQuota
                  + ")");
        }
        long totalUnusedQuota = mGlobalLimit - totalUsedQuota - mAppCacheMaxSize;
        if (totalUnusedQuota <= 0) {
            if (totalUsedQuota > 0) {
                scheduleOutOfSpaceNotification();
            }
            quotaUpdater.updateQuota(currentQuota);
            if(LOGV_ENABLED) {
                Log.v(LOGTAG, "onExceededDatabaseQuota: out of space.");
            }
            return;
        }
        long newOriginQuota = currentQuota;
        if (newOriginQuota == 0) {
            if (totalUnusedQuota >= estimatedSize) {
                newOriginQuota = estimatedSize;
            } else {
                if (LOGV_ENABLED) {
                    Log.v(LOGTAG,
                            "onExceededDatabaseQuota: Unable to satisfy" +
                            " estimatedSize for the new database " +
                            " (estimatedSize: " + estimatedSize +
                            ", unused quota: " + totalUnusedQuota);
                }
                newOriginQuota = 0;
            }
        } else {
            long quotaIncrease = estimatedSize == 0 ?
                    Math.min(QUOTA_INCREASE_STEP, totalUnusedQuota) :
                    estimatedSize;
            newOriginQuota += quotaIncrease;
            if (quotaIncrease > totalUnusedQuota) {
                newOriginQuota = currentQuota;
            }
        }
        quotaUpdater.updateQuota(newOriginQuota);
        if(LOGV_ENABLED) {
            Log.v(LOGTAG, "onExceededDatabaseQuota set new quota to "
                    + newOriginQuota);
        }
    }
    public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
            WebStorage.QuotaUpdater quotaUpdater) {
        if(LOGV_ENABLED) {
            Log.v(LOGTAG, "Received onReachedMaxAppCacheSize with spaceNeeded "
                  + spaceNeeded + " bytes.");
        }
        long totalUnusedQuota = mGlobalLimit - totalUsedQuota - mAppCacheMaxSize;
        if (totalUnusedQuota < spaceNeeded + APPCACHE_MAXSIZE_PADDING) {
            if (totalUsedQuota > 0) {
                scheduleOutOfSpaceNotification();
            }
            quotaUpdater.updateQuota(0);
            if(LOGV_ENABLED) {
                Log.v(LOGTAG, "onReachedMaxAppCacheSize: out of space.");
            }
            return;
        }
        mAppCacheMaxSize += spaceNeeded + APPCACHE_MAXSIZE_PADDING;
        quotaUpdater.updateQuota(mAppCacheMaxSize);
        if(LOGV_ENABLED) {
            Log.v(LOGTAG, "onReachedMaxAppCacheSize set new max size to "
                    + mAppCacheMaxSize);
        }
    }
    static void resetLastOutOfSpaceNotificationTime() {
        mLastOutOfSpaceNotificationTime = System.currentTimeMillis() -
            NOTIFICATION_INTERVAL + RESET_NOTIFICATION_INTERVAL;
    }
    private long getGlobalLimit() {
        long freeSpace = mDiskInfo.getFreeSpaceSizeBytes();
        long fileSystemSize = mDiskInfo.getTotalSizeBytes();
        return calculateGlobalLimit(fileSystemSize, freeSpace);
    }
     static long calculateGlobalLimit(long fileSystemSizeBytes,
            long freeSpaceBytes) {
        if (fileSystemSizeBytes <= 0
                || freeSpaceBytes <= 0
                || freeSpaceBytes > fileSystemSizeBytes) {
            return 0;
        }
        long fileSystemSizeRatio =
            2 << ((int) Math.floor(Math.log10(
                    fileSystemSizeBytes / (1024 * 1024))));
        long maxSizeBytes = (long) Math.min(Math.floor(
                fileSystemSizeBytes / fileSystemSizeRatio),
                Math.floor(freeSpaceBytes / 2));
        long maxSizeStepBytes = 1024 * 1024;
        if (maxSizeBytes < maxSizeStepBytes) {
            return 0;
        }
        long roundingExtra = maxSizeBytes % maxSizeStepBytes == 0 ? 0 : 1;
        return (maxSizeStepBytes
                * ((maxSizeBytes / maxSizeStepBytes) + roundingExtra));
    }
    private void scheduleOutOfSpaceNotification() {
        if(LOGV_ENABLED) {
            Log.v(LOGTAG, "scheduleOutOfSpaceNotification called.");
        }
        if (mContext == null) {
            return;
        }
        if ((mLastOutOfSpaceNotificationTime == -1) ||
            (System.currentTimeMillis() - mLastOutOfSpaceNotificationTime > NOTIFICATION_INTERVAL)) {
            int icon = android.R.drawable.stat_sys_warning;
            CharSequence title = mContext.getString(
                    R.string.webstorage_outofspace_notification_title);
            CharSequence text = mContext.getString(
                    R.string.webstorage_outofspace_notification_text);
            long when = System.currentTimeMillis();
            Intent intent = new Intent(mContext, WebsiteSettingsActivity.class);
            PendingIntent contentIntent =
                PendingIntent.getActivity(mContext, 0, intent, 0);
            Notification notification = new Notification(icon, title, when);
            notification.setLatestEventInfo(mContext, title, text, contentIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager mgr =
                (NotificationManager) mContext.getSystemService(ns);
            if (mgr != null) {
                mLastOutOfSpaceNotificationTime = System.currentTimeMillis();
                mgr.notify(OUT_OF_SPACE_ID, notification);
            }
        }
    }
}
