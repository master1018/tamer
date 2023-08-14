public class MediaSet {
    public static final int TYPE_SMART = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_USERDEFINED = 2;
    public long mId;
    public String mName;
    public boolean mFlagForDelete;
    public boolean mHasImages;
    public boolean mHasVideos;
    public int mType;
    public long mMinTimestamp = Long.MAX_VALUE;
    public long mMaxTimestamp = 0;
    public long mMinAddedTimestamp = Long.MAX_VALUE;
    public long mMaxAddedTimestamp = 0;
    public double mMinLatLatitude = LocationMediaFilter.LAT_MAX;
    public double mMinLatLongitude;
    public double mMaxLatLatitude = LocationMediaFilter.LAT_MIN;
    public double mMaxLatLongitude;
    public double mMinLonLatitude;
    public double mMinLonLongitude = LocationMediaFilter.LON_MAX;
    public double mMaxLonLatitude;
    public double mMaxLonLongitude = LocationMediaFilter.LON_MIN;
    public String mReverseGeocodedLocation;
    public boolean mLatLongDetermined = false;
    public boolean mReverseGeocodedLocationComputed = false;
    public boolean mReverseGeocodedLocationRequestMade = false;
    public String mTitleString;
    public String mTruncTitleString;
    public String mNoCountTitleString;
    public String mEditUri = null;
    public long mPicasaAlbumId = Shared.INVALID;
    public boolean mIsLocal = true;
    public DataSource mDataSource;
    public boolean mSyncPending = false;
    private ArrayList<MediaItem> mItems;
    private LongSparseArray<MediaItem> mItemsLookup;
    private LongSparseArray<MediaItem> mItemsLookupVideo;
    public int mNumItemsLoaded = 0;
    private int mNumExpectedItems = 0;
    private boolean mNumExpectedItemsCountAccurate = false;
    private int mCurrentLocation = 0;
    public MediaSet() {
        this(null);
    }
    public MediaSet(DataSource dataSource) {
        mItems = new ArrayList<MediaItem>(16);
        mItemsLookup = new LongSparseArray<MediaItem>();
        mItemsLookup.clear();
        mItemsLookupVideo = new LongSparseArray<MediaItem>();
        mItemsLookupVideo.clear();
        mDataSource = dataSource;
        MediaItem item = new MediaItem();
        item.mId = Shared.INVALID;
        item.mParentMediaSet = this;
        mItems.add(item);
        mNumExpectedItems = 16;
    }
    public ArrayList<MediaItem> getItems() {
        return mItems;
    }
    public void setNumExpectedItems(int numExpectedItems) {
        mItems.ensureCapacity(numExpectedItems);
        mNumExpectedItems = numExpectedItems;
        mNumExpectedItemsCountAccurate = true;
    }
    public int getNumExpectedItems() {
        return mNumExpectedItems;
    }
    public boolean setContainsValidItems() {
        if (mNumExpectedItems == 0)
            return false;
        return true;
    }
    public void updateNumExpectedItems() {
        mNumExpectedItems = mNumItemsLoaded;
        mNumExpectedItemsCountAccurate = true;
    }
    public int getNumItems() {
        return mItems.size();
    }
    public void clear() {
        mItems.clear();
        MediaItem item = new MediaItem();
        item.mId = Shared.INVALID;
        item.mParentMediaSet = this;
        mItems.add(item);
        mNumExpectedItems = 16;
        refresh();
        mItemsLookup.clear();
        mItemsLookupVideo.clear();
    }
    public void generateTitle(final boolean truncateTitle) {
        if (mName == null) {
            mName = "";
        }
        String size = (mNumExpectedItemsCountAccurate) ? "  (" + mNumExpectedItems + ")" : "";
        mTitleString = mName + size;
        if (truncateTitle) {
            int length = mName.length();
            mTruncTitleString = (length > 16) ? mName.substring(0, 12) + "..." + mName.substring(length - 4, length) + size : mName
                    + size;
            mNoCountTitleString = mName;
        } else {
            mTruncTitleString = mTitleString;
        }
    }
    public void addItem(final MediaItem itemToAdd) {
        if (itemToAdd == null) {
            return;
        }
        final LongSparseArray<MediaItem> lookup = (itemToAdd.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) ? mItemsLookup
                : mItemsLookupVideo;
        MediaItem lookupItem = lookup.get(itemToAdd.mId);
        if (lookupItem != null && !lookupItem.mFilePath.equals(itemToAdd.mFilePath)) {
            lookupItem = null;
        }
        final MediaItem item = (lookupItem == null) ? itemToAdd : lookupItem;
        item.mFlagForDelete = false;
        if (mItems.size() == 0) {
            mItems.add(item);
        } else if (mItems.get(0).mId == -1L) {
            mItems.set(0, item);
        } else {
            if (mItems.size() > mCurrentLocation) {
                mItems.set(mCurrentLocation, item);
            } else {
                mItems.add(mCurrentLocation, item);
            }
        }
        if (item.mId != Shared.INVALID) {
            if (lookupItem == null) {
                lookup.put(item.mId, item);
            }
            ++mNumItemsLoaded;
            ++mCurrentLocation;
        }
        if (item.isDateTakenValid()) {
            long dateTaken = item.mDateTakenInMs;
            if (dateTaken < mMinTimestamp) {
                mMinTimestamp = dateTaken;
            }
            if (dateTaken > mMaxTimestamp) {
                mMaxTimestamp = dateTaken;
            }
        } else if (item.isDateAddedValid()) {
            long dateAdded = item.mDateAddedInSec * 1000;
            if (dateAdded < mMinAddedTimestamp) {
                mMinAddedTimestamp = dateAdded;
            }
            if (dateAdded > mMaxAddedTimestamp) {
                mMaxAddedTimestamp = dateAdded;
            }
        }
        if (!item.isLatLongValid()) {
            return;
        }
        double itemLatitude = item.mLatitude;
        double itemLongitude = item.mLongitude;
        if (mMinLatLatitude > itemLatitude) {
            mMinLatLatitude = itemLatitude;
            mMinLatLongitude = itemLongitude;
            mLatLongDetermined = true;
        }
        if (mMaxLatLatitude < itemLatitude) {
            mMaxLatLatitude = itemLatitude;
            mMaxLatLongitude = itemLongitude;
            mLatLongDetermined = true;
        }
        if (mMinLonLongitude > itemLongitude) {
            mMinLonLatitude = itemLatitude;
            mMinLonLongitude = itemLongitude;
            mLatLongDetermined = true;
        }
        if (mMaxLonLongitude < itemLongitude) {
            mMaxLonLatitude = itemLatitude;
            mMaxLonLongitude = itemLongitude;
            mLatLongDetermined = true;
        }
    }
    public boolean removeItem(final MediaItem itemToRemove) {
        synchronized (mItems) {
            if (mItems.remove(itemToRemove)) {
                --mNumExpectedItems;
                --mNumItemsLoaded;
                --mCurrentLocation;
                final LongSparseArray<MediaItem> lookup = (itemToRemove.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) ? mItemsLookup
                        : mItemsLookupVideo;
                lookup.remove(itemToRemove.mId);
                return true;
            }
            return false;
        }
    }
    public void removeDuplicate(final MediaItem itemToRemove) {
        synchronized (mItems) {
            int numItems = mItems.size();
            boolean foundItem = false;
            for (int i = 0; i < numItems; ++i) {
                final MediaItem item = mItems.get(i);
                if (item == itemToRemove) {
                    if (foundItem == false) {
                        foundItem = true;
                    } else {
                        mItems.remove(i);
                        --mNumExpectedItems;
                        --mNumItemsLoaded;
                        --mCurrentLocation;
                        break;
                    }
                }
            }
        }
    }
    public boolean lookupContainsItem(final MediaItem item) {
        final LongSparseArray<MediaItem> lookupTable = (item.getMediaType() == MediaItem.MEDIA_TYPE_IMAGE) ? mItemsLookup
                : mItemsLookupVideo;
        MediaItem lookUp = lookupTable.get(item.mId);
        if (lookUp != null && lookUp.mFilePath.equals(item.mFilePath)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isTruncated() {
        return (mTitleString != null && !mTitleString.equals(mTruncTitleString));
    }
    public boolean areTimestampsAvailable() {
        return (mMinTimestamp < Long.MAX_VALUE && mMaxTimestamp > 0);
    }
    public boolean areAddedTimestampsAvailable() {
        return (mMinAddedTimestamp < Long.MAX_VALUE && mMaxAddedTimestamp > 0);
    }
    public boolean isPicassaSet() {
        if (isPicassaAlbum()) {
            return true;
        }
        int numItems = mItems.size();
        for (int i = 0; i < numItems; i++) {
            if (!mItems.get(i).isPicassaItem()) {
                return false;
            }
        }
        return true;
    }
    public boolean isPicassaAlbum() {
        return (mPicasaAlbumId != Shared.INVALID);
    }
    public void refresh() {
        mNumItemsLoaded = 0;
        mCurrentLocation = 0;
        final ArrayList<MediaItem> items = mItems;
        final int numItems = items.size();
        for (int i = 0; i < numItems; ++i) {
            MediaItem item = items.get(i);
            item.mFlagForDelete = true;
        }
    }
    public void checkForDeletedItems() {
        final ArrayList<MediaItem> items = mItems;
        final ArrayList<MediaItem> itemsToDelete = new ArrayList<MediaItem>();
        synchronized (items) {
            final int numItems = items.size();
            for (int i = 0; i < numItems; ++i) {
                MediaItem item = items.get(i);
                if (item.mFlagForDelete) {
                    itemsToDelete.add(item);
                }
            }
        }
        final int numItemsToDelete = itemsToDelete.size();
        for (int i = 0; i < numItemsToDelete; ++i) {
            removeItem(itemsToDelete.get(i));
        }
    }
}
