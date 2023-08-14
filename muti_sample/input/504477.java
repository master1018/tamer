public class WebStorageSizeManagerUnitTests extends AndroidTestCase {
    private long mNewQuota;
    private class MockQuotaUpdater implements WebStorage.QuotaUpdater {
        public void updateQuota(long newQuota) {
            mNewQuota = newQuota;
        }
    }
    private class MockDiskInfo implements WebStorageSizeManager.DiskInfo {
        private long mFreeSize;
        private long mTotalSize;
        public long getFreeSpaceSizeBytes() {
            return mFreeSize;
        }
        public long getTotalSizeBytes() {
            return mTotalSize;
        }
        public void setFreeSpaceSizeBytes(long freeSize) {
            mFreeSize = freeSize;
        }
        public void setTotalSizeBytes(long totalSize) {
            mTotalSize = totalSize;
        }
    }
    public class MockAppCacheInfo implements WebStorageSizeManager.AppCacheInfo {
        private long mAppCacheSize;
        public long getAppCacheSizeBytes() {
            return mAppCacheSize;
        }
        public void setAppCacheSizeBytes(long appCacheSize) {
            mAppCacheSize = appCacheSize;
        }
    }
    private MockQuotaUpdater mQuotaUpdater = new MockQuotaUpdater();
    private final MockDiskInfo mDiskInfo = new MockDiskInfo();
    private final MockAppCacheInfo mAppCacheInfo = new MockAppCacheInfo();
    private long bytes(double megabytes) {
        return (new Double(megabytes * 1024 * 1024)).longValue();
    }
    public void testCallbacks() {
        long totalUsedQuota = 0;
        final long quotaIncrease = WebStorageSizeManager.QUOTA_INCREASE_STEP;  
        mDiskInfo.setTotalSizeBytes(bytes(75));
        mDiskInfo.setFreeSpaceSizeBytes(bytes(24));
        mAppCacheInfo.setAppCacheSizeBytes(0);
        WebStorageSizeManager manager = new WebStorageSizeManager(null, mDiskInfo, mAppCacheInfo);
        long origin1Quota = 0;
        long origin1EstimatedSize = bytes(3.5);
        manager.onExceededDatabaseQuota("1", "1", origin1Quota, origin1EstimatedSize, totalUsedQuota, mQuotaUpdater);
        assertEquals(origin1EstimatedSize, mNewQuota);
        origin1Quota = mNewQuota;
        totalUsedQuota += origin1Quota;
        long origin2Quota = 0;
        long origin2EstimatedSize = bytes(2.5);
        manager.onExceededDatabaseQuota("2", "2", origin2Quota, origin2EstimatedSize, totalUsedQuota, mQuotaUpdater);
        assertEquals(origin2EstimatedSize, mNewQuota);
        origin2Quota = mNewQuota;
        totalUsedQuota += origin2Quota;
        manager.onExceededDatabaseQuota("1", "1", origin1Quota, 0, totalUsedQuota, mQuotaUpdater);
        assertEquals(origin1EstimatedSize + quotaIncrease, mNewQuota);
        totalUsedQuota -= origin1Quota;
        origin1Quota = mNewQuota;
        totalUsedQuota += origin1Quota;
        manager.onExceededDatabaseQuota("2", "2", origin2Quota, 0, totalUsedQuota, mQuotaUpdater);
        assertEquals(origin2EstimatedSize + quotaIncrease, mNewQuota);
        totalUsedQuota -= origin2Quota;
        origin2Quota = mNewQuota;
        totalUsedQuota += origin2Quota;
        long origin3Quota = 0;
        long origin3EstimatedSize = bytes(5);
        manager.onExceededDatabaseQuota("3", "3", origin3Quota, origin3EstimatedSize, totalUsedQuota, mQuotaUpdater);
        assertEquals(0, mNewQuota);  
        origin3Quota = mNewQuota;
        totalUsedQuota += origin3Quota;
        manager.onExceededDatabaseQuota("1", "1", origin1Quota, 0, totalUsedQuota, mQuotaUpdater);
        assertEquals(origin1Quota + quotaIncrease, mNewQuota);
        totalUsedQuota -= origin1Quota;
        origin1Quota = mNewQuota;
        totalUsedQuota += origin1Quota;
        manager.onExceededDatabaseQuota("1", "1", origin1Quota, 0, totalUsedQuota, mQuotaUpdater);
        assertEquals(origin1Quota, mNewQuota);
        manager.onExceededDatabaseQuota("4", "4", 0, bytes(1), totalUsedQuota, mQuotaUpdater);
        assertEquals(0, mNewQuota);
        mAppCacheInfo.setAppCacheSizeBytes(bytes(2));
        manager.onReachedMaxAppCacheSize(bytes(2), totalUsedQuota, mQuotaUpdater);
        assertEquals(0, mNewQuota);
        totalUsedQuota -= origin2Quota;
        origin2Quota = 0;
        manager.onReachedMaxAppCacheSize(bytes(1.5), totalUsedQuota, mQuotaUpdater);
        mAppCacheInfo.setAppCacheSizeBytes(mAppCacheInfo.getAppCacheSizeBytes() + bytes(2.5));
        assertEquals(mAppCacheInfo.getAppCacheSizeBytes(), mNewQuota - WebStorageSizeManager.APPCACHE_MAXSIZE_PADDING);
        long origin4Quota = 0;
        long origin4EstimatedSize = bytes(1.5);
        manager.onExceededDatabaseQuota("4", "4", origin4Quota, origin4EstimatedSize, totalUsedQuota, mQuotaUpdater);
        assertEquals(bytes(1.5), mNewQuota);
        origin4Quota = mNewQuota;
        totalUsedQuota += origin4Quota;
    }
    public void testCalculateGlobalLimit() {
        long fileSystemSize = 78643200;  
        long freeSpaceSize = 25165824;  
        long maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(12582912, maxSize);  
        fileSystemSize = 78643200;  
        freeSpaceSize = 60 * 1024 * 1024;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(19922944, maxSize);  
        fileSystemSize = 8589934592L;  
        freeSpaceSize = 4294967296L;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(536870912L, maxSize);  
        fileSystemSize = -14;
        freeSpaceSize = 21;
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(0, maxSize);
        fileSystemSize = 100;
        freeSpaceSize = 101;
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(0, maxSize);
        fileSystemSize = 3774873; 
        freeSpaceSize = 2560000;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(2097152, maxSize);  
        fileSystemSize = 4404019; 
        freeSpaceSize = 3774873;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(2097152, maxSize);  
        fileSystemSize = 4404019; 
        freeSpaceSize = 4404019;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(3145728, maxSize);  
        fileSystemSize = 1048576; 
        freeSpaceSize = 1048575;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(0, maxSize);
        fileSystemSize = 3774873; 
        freeSpaceSize = 2097151;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(0, maxSize);
        fileSystemSize = 3774873; 
        freeSpaceSize = 2097151;  
        maxSize = WebStorageSizeManager.calculateGlobalLimit(fileSystemSize, freeSpaceSize);
        assertEquals(0, maxSize);
    }
    public void testManyDatabasesOnOneOrigin() {
        long totalUsedQuota = 0;
        mDiskInfo.setTotalSizeBytes(bytes(100));
        mDiskInfo.setFreeSpaceSizeBytes(bytes(100));
        assertEquals(bytes(13), WebStorageSizeManager.calculateGlobalLimit(
                mDiskInfo.getTotalSizeBytes(), mDiskInfo.getFreeSpaceSizeBytes()));
        mAppCacheInfo.setAppCacheSizeBytes(0);
        WebStorageSizeManager manager = new WebStorageSizeManager(null, mDiskInfo, mAppCacheInfo);
        long originQuota = 0;
        long database1EstimatedSize = bytes(2);
        manager.onExceededDatabaseQuota("1", "1", originQuota, database1EstimatedSize,
                totalUsedQuota, mQuotaUpdater);
        assertEquals(database1EstimatedSize, mNewQuota);
        originQuota = mNewQuota;
        totalUsedQuota = originQuota;
        long database2EstimatedSize = bytes(3.5);
        manager.onExceededDatabaseQuota("1", "2", originQuota, database2EstimatedSize,
                totalUsedQuota, mQuotaUpdater);
        assertEquals(database1EstimatedSize + database2EstimatedSize, mNewQuota);
        originQuota = mNewQuota;
        totalUsedQuota = originQuota;
        long database3EstimatedSize = bytes(50);
        manager.onExceededDatabaseQuota("1", "3", originQuota, database3EstimatedSize,
                totalUsedQuota, mQuotaUpdater);
        assertEquals(originQuota, mNewQuota);
        long database4EstimatedSize = bytes(2);
        manager.onExceededDatabaseQuota("1", "4", originQuota, database4EstimatedSize,
                totalUsedQuota, mQuotaUpdater);
        assertEquals(database1EstimatedSize + database2EstimatedSize + database4EstimatedSize,
                mNewQuota);
        originQuota = mNewQuota;
        totalUsedQuota = originQuota;
        manager.onExceededDatabaseQuota("1", "1", originQuota, 0, totalUsedQuota, mQuotaUpdater);
        assertEquals(database1EstimatedSize + database2EstimatedSize + database4EstimatedSize +
                bytes(1), mNewQuota);
        originQuota = mNewQuota;
        totalUsedQuota = originQuota;
        long database5EstimatedSize = bytes(0.5);
        manager.onExceededDatabaseQuota("1", "5", originQuota, database5EstimatedSize,
                totalUsedQuota, mQuotaUpdater);
        assertEquals(database1EstimatedSize + database2EstimatedSize + database4EstimatedSize +
                bytes(1) + database5EstimatedSize, mNewQuota);
    }
}
