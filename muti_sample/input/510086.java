public final class MediaBucketList {
    private static final Boolean TRUE = new Boolean(true);
    private static final Boolean FALSE = new Boolean(false);
    private ArrayList<MediaBucket> mBuckets = new ArrayList<MediaBucket>(1024);
    private boolean mDirtyCount;
    private boolean mDirtyAcceleratedLookup;
    private int mCount;
    private HashMap<MediaItem, Boolean> mCachedItems = new HashMap<MediaItem, Boolean>(1024);
    public static MediaItem getFirstItemSelection(ArrayList<MediaBucket> buckets) {
        MediaItem item = null;
        if (buckets != null) {
            int numBuckets = buckets.size();
            for (int i = 0; i < numBuckets; i++) {
                MediaBucket bucket = buckets.get(0);
                if (bucket != null && !isSetSelection(bucket)) {
                    ArrayList<MediaItem> items = bucket.mediaItems;
                    if (items != null && items.size() > 0) {
                        item = items.get(0);
                        break;
                    }
                }
            }
        }
        return item;
    }
    public static MediaSet getFirstSetSelection(ArrayList<MediaBucket> buckets) {
        MediaSet set = null;
        if (buckets != null) {
            int numBuckets = buckets.size();
            for (int i = 0; i < numBuckets; i++) {
                MediaBucket bucket = buckets.get(0);
                if (bucket != null && isSetSelection(bucket)) {
                    set = bucket.mediaSet;
                }
            }
        }
        return set;
    }
    public ArrayList<MediaBucket> get() {
        return mBuckets;
    }
    public int size() {
        if (mDirtyCount) {
            ArrayList<MediaBucket> buckets = mBuckets;
            int numBuckets = buckets.size();
            int count = 0;
            for (int i = 0; i < numBuckets; ++i) {
                MediaBucket bucket = buckets.get(i);
                int numItems = 0;
                if (bucket.mediaItems == null && bucket.mediaSet != null) {
                    numItems = bucket.mediaSet.getNumItems();
                    if (numItems == 0) {
                        numItems = 1;
                    }
                } else if (bucket.mediaItems != null && bucket.mediaItems != null) {
                    numItems = bucket.mediaItems.size();
                }
                count += numItems;
            }
            mCount = count;
            mDirtyCount = false;
        }
        return mCount;
    }
    public void add(int slotId, MediaFeed feed, boolean removeIfAlreadyAdded) {
        if (slotId == Shared.INVALID) {
            return;
        }
        setDirty();
        final ArrayList<MediaBucket> selectedBuckets = mBuckets;
        final int numSelectedBuckets = selectedBuckets.size();
        MediaSet mediaSetToAdd = null;
        ArrayList<MediaItem> selectedItems = null;
        MediaBucket bucket = null;
        final boolean hasExpandedMediaSet = feed.hasExpandedMediaSet();
        if (!hasExpandedMediaSet) {
            ArrayList<MediaSet> mediaSets = feed.getMediaSets();
            if (slotId >= mediaSets.size()) {
                return;
            }
            mediaSetToAdd = mediaSets.get(slotId);
        } else {
            int numSlots = feed.getNumSlots();
            if (slotId < numSlots) {
                MediaSet set = feed.getSetForSlot(slotId);
                if (set != null) {
                    ArrayList<MediaItem> items = set.getItems();
                    if (set.getNumItems() > 0) {
                        mediaSetToAdd = items.get(0).mParentMediaSet;
                    }
                }
            }
        }
        for (int i = 0; i < numSelectedBuckets; ++i) {
            final MediaBucket bucketCompare = selectedBuckets.get(i);
            if (bucketCompare.mediaSet != null && mediaSetToAdd != null && bucketCompare.mediaSet.mId == mediaSetToAdd.mId) {
                if (!hasExpandedMediaSet) {
                    if (removeIfAlreadyAdded) {
                        selectedBuckets.remove(bucketCompare);
                    }
                    return;
                } else {
                    bucket = bucketCompare;
                    break;
                }
            }
        }
        if (bucket == null) {
            bucket = new MediaBucket();
            bucket.mediaSet = mediaSetToAdd;
            bucket.mediaItems = selectedItems;
            selectedBuckets.add(bucket);
        }
        if (hasExpandedMediaSet) {
            int numSlots = feed.getNumSlots();
            if (slotId < numSlots) {
                MediaSet set = feed.getSetForSlot(slotId);
                if (set != null) {
                    ArrayList<MediaItem> items = set.getItems();
                    int numItems = set.getNumItems();
                    selectedItems = bucket.mediaItems;
                    if (selectedItems == null) {
                        selectedItems = new ArrayList<MediaItem>(numItems);
                        bucket.mediaItems = selectedItems;
                    }
                    for (int i = 0; i < numItems; ++i) {
                        MediaItem item = items.get(i);
                        int numPresentItems = selectedItems.size();
                        boolean foundIndex = false;
                        for (int j = 0; j < numPresentItems; ++j) {
                            final MediaItem selectedItem = selectedItems.get(j);
                            if (selectedItem != null && item != null && selectedItem.mId == item.mId) {
                                foundIndex = true;
                                if (removeIfAlreadyAdded) {
                                    selectedItems.remove(j);
                                }
                                break;
                            }
                        }
                        if (foundIndex == false) {
                            selectedItems.add(item);
                        }
                    }
                }
            }
        }
        setDirty();
    }
    public boolean find(MediaItem item) {
        HashMap<MediaItem, Boolean> cachedItems = mCachedItems;
        if (mDirtyAcceleratedLookup) {
            cachedItems.clear();
            mDirtyAcceleratedLookup = false;
        }
        Boolean itemAdded = cachedItems.get(item);
        if (itemAdded == null) {
            ArrayList<MediaBucket> selectedBuckets = mBuckets;
            int numSelectedBuckets = selectedBuckets.size();
            for (int i = 0; i < numSelectedBuckets; ++i) {
                MediaBucket bucket = selectedBuckets.get(i);
                ArrayList<MediaItem> mediaItems = bucket.mediaItems;
                if (mediaItems == null) {
                    MediaSet parentMediaSet = item.mParentMediaSet;
                    if (parentMediaSet != null && parentMediaSet.equals(bucket.mediaSet)) {
                        cachedItems.put(item, TRUE);
                        return true;
                    }
                } else {
                    int numMediaItems = mediaItems.size();
                    for (int j = 0; j < numMediaItems; ++j) {
                        MediaItem itemCompare = mediaItems.get(j);
                        if (itemCompare == item) {
                            cachedItems.put(item, TRUE);
                            return true;
                        }
                    }
                }
            }
            cachedItems.put(item, FALSE);
            return false;
        } else {
            return itemAdded.booleanValue();
        }
    }
    public void clear() {
        mBuckets.clear();
        setDirty();
    }
    private void setDirty() {
        mDirtyCount = true;
        mDirtyAcceleratedLookup = true;
    }
    protected static boolean isSetSelection(ArrayList<MediaBucket> buckets) {
        if (buckets != null) {
            int numBuckets = buckets.size();
            if (numBuckets == 0) {
                return false;
            } else if (numBuckets == 1) {
                return isSetSelection(buckets.get(0));
            } else {
                return true;
            }
        }
        return false;
    }
    protected static boolean isSetSelection(MediaBucket bucket) {
        return (bucket.mediaSet != null && bucket.mediaItems == null) ? true : false;
    }
    protected static boolean isMultipleItemSelection(ArrayList<MediaBucket> buckets) {
        if (buckets != null) {
            int numBuckets = buckets.size();
            if (numBuckets == 0) {
                return false;
            } else {
                return isMultipleSetSelection(buckets.get(0));
            }
        }
        return false;
    }
    protected static boolean isMultipleSetSelection(MediaBucket bucket) {
        return (bucket.mediaItems != null && bucket.mediaItems.size() > 1) ? true : false;
    }
}
