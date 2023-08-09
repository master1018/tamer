public final class MediaClustering {
    private static final int GEOGRAPHIC_DISTANCE_CUTOFF_IN_MILES = 20;
    private static final long MIN_CLUSTER_SPLIT_TIME_IN_MS = 60000L;
    private static final long MAX_CLUSTER_SPLIT_TIME_IN_MS = 7200000L;
    private static final int NUM_CLUSTERS_TARGETED = 9;
    private static final int MIN_MIN_CLUSTER_SIZE = 8;
    private static final int MAX_MIN_CLUSTER_SIZE = 15;
    private static final int MIN_MAX_CLUSTER_SIZE = 20;
    private static final int MAX_MAX_CLUSTER_SIZE = 50;
    private static int CLUSTER_SPLIT_MULTIPLIER = 3;
    private static final int MIN_PARTITION_CHANGE_FACTOR = 2;
    private static final int PARTITION_CLUSTER_SPLIT_TIME_FACTOR = 2;
    private ArrayList<Cluster> mClusters;
    private Cluster mCurrCluster;
    private boolean mIsPicassaAlbum = false;
    private long mClusterSplitTime = (MIN_CLUSTER_SPLIT_TIME_IN_MS + MAX_CLUSTER_SPLIT_TIME_IN_MS) / 2;
    private long mLargeClusterSplitTime = mClusterSplitTime / PARTITION_CLUSTER_SPLIT_TIME_FACTOR;
    private int mMinClusterSize = (MIN_MIN_CLUSTER_SIZE + MAX_MIN_CLUSTER_SIZE) / 2;
    private int mMaxClusterSize = (MIN_MAX_CLUSTER_SIZE + MAX_MAX_CLUSTER_SIZE) / 2;
    MediaClustering(boolean isPicassaAlbum) {
        mClusters = new ArrayList<Cluster>();
        mIsPicassaAlbum = isPicassaAlbum;
        mCurrCluster = new Cluster(mIsPicassaAlbum);
    }
    public void clear() {
        int numClusters = mClusters.size();
        for (int i = 0; i < numClusters; i++) {
            Cluster cluster = mClusters.get(i);
            cluster.clear();
        }
        if (mCurrCluster != null) {
            mCurrCluster.clear();
        }
    }
    public void setTimeRange(long timeRange, int numItems) {
        if (numItems != 0) {
            int meanItemsPerCluster = numItems / NUM_CLUSTERS_TARGETED;
            mMinClusterSize = meanItemsPerCluster / 2;
            mMaxClusterSize = meanItemsPerCluster * 2;
            mClusterSplitTime = timeRange / numItems * CLUSTER_SPLIT_MULTIPLIER;
        }
        mClusterSplitTime = Shared.clamp(mClusterSplitTime, MIN_CLUSTER_SPLIT_TIME_IN_MS, MAX_CLUSTER_SPLIT_TIME_IN_MS);
        mLargeClusterSplitTime = mClusterSplitTime / PARTITION_CLUSTER_SPLIT_TIME_FACTOR;
        mMinClusterSize = Shared.clamp(mMinClusterSize, MIN_MIN_CLUSTER_SIZE, MAX_MIN_CLUSTER_SIZE);
        mMaxClusterSize = Shared.clamp(mMaxClusterSize, MIN_MAX_CLUSTER_SIZE, MAX_MAX_CLUSTER_SIZE);
    }
    public void addItemForClustering(MediaItem mediaItem) {
        compute(mediaItem, false);
    }
    public void removeItemFromClustering(MediaItem mediaItem) {
        if (mCurrCluster.removeItem(mediaItem)) {
            return;
        }
        int numClusters = mClusters.size();
        for (int i = 0; i < numClusters; i++) {
            Cluster cluster = mClusters.get(i);
            if (cluster.removeItem(mediaItem)) {
                if (cluster.mNumItemsLoaded == 0) {
                    mClusters.remove(cluster);
                }
                return;
            }
        }
    }
    public void compute(MediaItem currentItem, boolean processAllItems) {
        if (currentItem != null) {
            int numClusters = mClusters.size();
            int numCurrClusterItems = mCurrCluster.mNumItemsLoaded;
            boolean geographicallySeparateItem = false;
            boolean itemAddedToCurrentCluster = false;
            if (numCurrClusterItems == 0) {
                mCurrCluster.addItem(currentItem);
            } else {
                MediaItem prevItem = mCurrCluster.getLastItem();
                if (isGeographicallySeparated(prevItem, currentItem)) {
                    mClusters.add(mCurrCluster);
                    geographicallySeparateItem = true;
                } else if (numCurrClusterItems > mMaxClusterSize) {
                    splitAndAddCurrentCluster();
                } else if (timeDistance(prevItem, currentItem) < mClusterSplitTime) {
                    mCurrCluster.addItem(currentItem);
                    itemAddedToCurrentCluster = true;
                } else if (numClusters > 0 && numCurrClusterItems < mMinClusterSize
                        && !mCurrCluster.mGeographicallySeparatedFromPrevCluster) {
                    mergeAndAddCurrentCluster();
                } else {
                    mClusters.add(mCurrCluster);
                }
                if (!itemAddedToCurrentCluster) {
                    mCurrCluster = new Cluster(mIsPicassaAlbum);
                    if (geographicallySeparateItem) {
                        mCurrCluster.mGeographicallySeparatedFromPrevCluster = true;
                    }
                    mCurrCluster.addItem(currentItem);
                }
            }
        }
        if (processAllItems && mCurrCluster.mNumItemsLoaded > 0) {
            int numClusters = mClusters.size();
            int numCurrClusterItems = mCurrCluster.mNumItemsLoaded;
            if (numCurrClusterItems > mMaxClusterSize) {
                splitAndAddCurrentCluster();
            } else if (numClusters > 0 && numCurrClusterItems < mMinClusterSize
                    && !mCurrCluster.mGeographicallySeparatedFromPrevCluster) {
                mergeAndAddCurrentCluster();
            } else {
                mClusters.add(mCurrCluster);
            }
            mCurrCluster = new Cluster(mIsPicassaAlbum);
        }
    }
    private void splitAndAddCurrentCluster() {
        ArrayList<MediaItem> currClusterItems = mCurrCluster.getItems();
        int numCurrClusterItems = mCurrCluster.mNumItemsLoaded;
        int secondPartitionStartIndex = getPartitionIndexForCurrentCluster();
        if (secondPartitionStartIndex != -1) {
            Cluster partitionedCluster = new Cluster(mIsPicassaAlbum);
            for (int j = 0; j < secondPartitionStartIndex; j++) {
                partitionedCluster.addItem(currClusterItems.get(j));
            }
            mClusters.add(partitionedCluster);
            partitionedCluster = new Cluster(mIsPicassaAlbum);
            for (int j = secondPartitionStartIndex; j < numCurrClusterItems; j++) {
                partitionedCluster.addItem(currClusterItems.get(j));
            }
            mClusters.add(partitionedCluster);
        } else {
            mClusters.add(mCurrCluster);
        }
    }
    private int getPartitionIndexForCurrentCluster() {
        int partitionIndex = -1;
        float largestChange = MIN_PARTITION_CHANGE_FACTOR;
        ArrayList<MediaItem> currClusterItems = mCurrCluster.getItems();
        int numCurrClusterItems = mCurrCluster.mNumItemsLoaded;
        int minClusterSize = mMinClusterSize;
        if (numCurrClusterItems > minClusterSize + 1) {
            for (int i = minClusterSize; i < numCurrClusterItems - minClusterSize; i++) {
                MediaItem prevItem = currClusterItems.get(i - 1);
                MediaItem currItem = currClusterItems.get(i);
                MediaItem nextItem = currClusterItems.get(i + 1);
                if (prevItem.isDateTakenValid() && currItem.isDateModifiedValid() && nextItem.isDateModifiedValid()) {
                    long diff1 = Math.abs(nextItem.mDateTakenInMs - currItem.mDateTakenInMs);
                    long diff2 = Math.abs(currItem.mDateTakenInMs - prevItem.mDateTakenInMs);
                    float change = Math.max(diff1 / (diff2 + 0.01f), diff2 / (diff1 + 0.01f));
                    if (change > largestChange) {
                        if (timeDistance(currItem, prevItem) > mLargeClusterSplitTime) {
                            partitionIndex = i;
                            largestChange = change;
                        } else if (timeDistance(nextItem, currItem) > mLargeClusterSplitTime) {
                            partitionIndex = i + 1;
                            largestChange = change;
                        }
                    }
                }
            }
        }
        return partitionIndex;
    }
    private void mergeAndAddCurrentCluster() {
        int numClusters = mClusters.size();
        Cluster prevCluster = mClusters.get(numClusters - 1);
        ArrayList<MediaItem> currClusterItems = mCurrCluster.getItems();
        int numCurrClusterItems = mCurrCluster.mNumItemsLoaded;
        if (prevCluster.mNumItemsLoaded < mMinClusterSize) {
            for (int i = 0; i < numCurrClusterItems; i++) {
                prevCluster.addItem(currClusterItems.get(i));
            }
            mClusters.set(numClusters - 1, prevCluster);
        } else {
            mClusters.add(mCurrCluster);
        }
    }
    public synchronized ArrayList<Cluster> getClusters() {
        int numCurrClusterItems = mCurrCluster.mNumItemsLoaded;
        if (numCurrClusterItems == 0) {
            return mClusters;
        }
        ArrayList<Cluster> mergedClusters = new ArrayList<Cluster>();
        mergedClusters.addAll(mClusters);
        if (numCurrClusterItems > 0) {
            mergedClusters.add(mCurrCluster);
        }
        return mergedClusters;
    }
    public ArrayList<Cluster> getClustersForDisplay() {
        return mClusters;
    }
    public static final class Cluster extends MediaSet {
        private boolean mGeographicallySeparatedFromPrevCluster = false;
        private boolean mClusterChanged = false;
        private boolean mIsPicassaAlbum = false;
        private static final String MMDDYY_FORMAT = "MMddyy";
        public Cluster(boolean isPicassaAlbum) {
            mIsPicassaAlbum = isPicassaAlbum;
        }
        public void generateCaption(Context context) {
            if (mClusterChanged) {
                Resources resources = context.getResources();
                long minTimestamp = -1L;
                long maxTimestamp = -1L;
                if (areTimestampsAvailable()) {
                    minTimestamp = mMinTimestamp;
                    maxTimestamp = mMaxTimestamp;
                } else if (areAddedTimestampsAvailable()) {
                    minTimestamp = mMinAddedTimestamp;
                    maxTimestamp = mMaxAddedTimestamp;
                }
                if (minTimestamp != -1L) {
                    if (mIsPicassaAlbum) {
                        minTimestamp -= App.CURRENT_TIME_ZONE.getOffset(minTimestamp);
                        maxTimestamp -= App.CURRENT_TIME_ZONE.getOffset(maxTimestamp);
                    }
                    String minDay = DateFormat.format(MMDDYY_FORMAT, minTimestamp).toString();
                    String maxDay = DateFormat.format(MMDDYY_FORMAT, maxTimestamp).toString();
                    if (minDay.substring(4).equals(maxDay.substring(4))) {
                        mName = DateUtils.formatDateRange(context, minTimestamp, maxTimestamp, DateUtils.FORMAT_ABBREV_ALL);
                        if (minDay.equals(maxDay)) {
                            int flags = DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE;
                            String dateRangeWithOptionalYear = DateUtils.formatDateTime(context, minTimestamp, flags);
                            String dateRangeWithYear = DateUtils.formatDateTime(context, minTimestamp, flags
                                    | DateUtils.FORMAT_SHOW_YEAR);
                            if (!dateRangeWithOptionalYear.equals(dateRangeWithYear)) {
                                long midTimestamp = (minTimestamp + maxTimestamp) / 2;
                                mName = DateUtils.formatDateRange(context, midTimestamp, midTimestamp, DateUtils.FORMAT_SHOW_TIME
                                        | flags);
                            }
                        }
                    } else {
                        int flags = DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE;
                        mName = DateUtils.formatDateRange(context, minTimestamp, maxTimestamp, flags);
                    }
                } else {
                    mName = resources.getString(Res.string.date_unknown);
                }
                updateNumExpectedItems();
                generateTitle(false);
                mClusterChanged = false;
            }
        }
        public void addItem(MediaItem item) {
            super.addItem(item);
            mClusterChanged = true;
        }
        public boolean removeItem(MediaItem item) {
            if (super.removeItem(item)) {
                mClusterChanged = true;
                return true;
            }
            return false;
        }
        public MediaItem getLastItem() {
            final ArrayList<MediaItem> items = super.getItems();
            if (items == null || mNumItemsLoaded == 0) {
                return null;
            } else {
                return items.get(mNumItemsLoaded - 1);
            }
        }
    }
    public static long timeDistance(MediaItem a, MediaItem b) {
        if (a == null || b == null) {
            return 0;
        }
        return Math.abs(a.mDateTakenInMs - b.mDateTakenInMs);
    }
    private static boolean isGeographicallySeparated(MediaItem a, MediaItem b) {
        if (a != null && b != null && a.isLatLongValid() && b.isLatLongValid()) {
            int distance = (int) (LocationMediaFilter.toMile(LocationMediaFilter.distanceBetween(a.mLatitude, a.mLongitude,
                    b.mLatitude, b.mLongitude)) + 0.5);
            if (distance > GEOGRAPHIC_DISTANCE_CUTOFF_IN_MILES) {
                return true;
            }
        }
        return false;
    }
}
