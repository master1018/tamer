public class ImageListUber implements IImageList {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageListUber";
    private final IImageList [] mSubList;
    private final PriorityQueue<MergeSlot> mQueue;
    private long[] mSkipList;
    private int mSkipListSize;
    private int [] mSkipCounts;
    private int mLastListIndex;
    public ImageListUber(IImageList [] sublist, int sort) {
        mSubList = sublist.clone();
        mQueue = new PriorityQueue<MergeSlot>(4,
                sort == ImageManager.SORT_ASCENDING
                ? new AscendingComparator()
                : new DescendingComparator());
        mSkipList = new long[16];
        mSkipListSize = 0;
        mSkipCounts = new int[mSubList.length];
        mLastListIndex = -1;
        mQueue.clear();
        for (int i = 0, n = mSubList.length; i < n; ++i) {
            IImageList list = mSubList[i];
            MergeSlot slot = new MergeSlot(list, i);
            if (slot.next()) mQueue.add(slot);
        }
    }
    public HashMap<String, String> getBucketIds() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (IImageList list : mSubList) {
            hashMap.putAll(list.getBucketIds());
        }
        return hashMap;
    }
    public int getCount() {
        int count = 0;
        for (IImageList subList : mSubList) {
            count += subList.getCount();
        }
        return count;
    }
    public boolean isEmpty() {
        for (IImageList subList : mSubList) {
            if (!subList.isEmpty()) return false;
        }
        return true;
    }
    public IImage getImageAt(int index) {
        if (index < 0 || index > getCount()) {
            throw new IndexOutOfBoundsException(
                    "index " + index + " out of range max is " + getCount());
        }
        int skipCounts[] = mSkipCounts;
        Arrays.fill(skipCounts, 0);
        int skipCount = 0;
        for (int i = 0, n = mSkipListSize; i < n; ++i) {
            long v = mSkipList[i];
            int offset = (int) (v & 0xFFFFFFFF);
            int which  = (int) (v >> 32);
            if (skipCount + offset > index) {
                int subindex = mSkipCounts[which] + (index - skipCount);
                return mSubList[which].getImageAt(subindex);
            }
            skipCount += offset;
            mSkipCounts[which] += offset;
        }
        for (; true; ++skipCount) {
            MergeSlot slot = nextMergeSlot();
            if (slot == null) return null;
            if (skipCount == index) {
                IImage result = slot.mImage;
                if (slot.next()) mQueue.add(slot);
                return result;
            }
            if (slot.next()) mQueue.add(slot);
        }
    }
    private MergeSlot nextMergeSlot() {
        MergeSlot slot = mQueue.poll();
        if (slot == null) return null;
        if (slot.mListIndex == mLastListIndex) {
            int lastIndex = mSkipListSize - 1;
            ++mSkipList[lastIndex];
        } else {
            mLastListIndex = slot.mListIndex;
            if (mSkipList.length == mSkipListSize) {
                long [] temp = new long[mSkipListSize * 2];
                System.arraycopy(mSkipList, 0, temp, 0, mSkipListSize);
                mSkipList = temp;
            }
            mSkipList[mSkipListSize++] = (((long) mLastListIndex) << 32) | 1;
        }
        return slot;
    }
    public IImage getImageForUri(Uri uri) {
        for (IImageList sublist : mSubList) {
            IImage image = sublist.getImageForUri(uri);
            if (image != null) return image;
        }
        return null;
    }
    private void modifySkipCountForDeletedImage(int index) {
        int skipCount = 0;
        for (int i = 0, n = mSkipListSize; i < n; i++) {
            long v = mSkipList[i];
            int offset = (int) (v & 0xFFFFFFFF);
            if (skipCount + offset > index) {
                mSkipList[i] = v - 1;
                break;
            }
            skipCount += offset;
        }
    }
    private boolean removeImage(IImage image, int index) {
        IImageList list = image.getContainer();
        if (list != null && list.removeImage(image)) {
            modifySkipCountForDeletedImage(index);
            return true;
        }
        return false;
    }
    public boolean removeImage(IImage image) {
        return removeImage(image, getImageIndex(image));
    }
    public boolean removeImageAt(int index) {
        IImage image = getImageAt(index);
        if (image == null) return false;
        return removeImage(image, index);
    }
    public synchronized int getImageIndex(IImage image) {
        IImageList list = image.getContainer();
        int listIndex = Util.indexOf(mSubList, list);
        if (listIndex == -1) {
            throw new IllegalArgumentException();
        }
        int listOffset = list.getImageIndex(image);
        int skipCount = 0;
        for (int i = 0, n = mSkipListSize; i < n; ++i) {
            long value = mSkipList[i];
            int offset = (int) (value & 0xFFFFFFFF);
            int which  = (int) (value >> 32);
            if (which == listIndex) {
                if (listOffset < offset) {
                    return skipCount + listOffset;
                }
                listOffset -= offset;
            }
            skipCount += offset;
        }
        for (; true; ++skipCount) {
            MergeSlot slot = nextMergeSlot();
            if (slot == null) return -1;
            if (slot.mImage == image) {
                if (slot.next()) mQueue.add(slot);
                return skipCount;
            }
            if (slot.next()) mQueue.add(slot);
        }
    }
    private static class DescendingComparator implements Comparator<MergeSlot> {
        public int compare(MergeSlot m1, MergeSlot m2) {
            if (m1.mDateTaken != m2.mDateTaken) {
                return m1.mDateTaken < m2.mDateTaken ? 1 : -1;
            }
            return m1.mListIndex - m2.mListIndex;
        }
    }
    private static class AscendingComparator implements Comparator<MergeSlot> {
        public int compare(MergeSlot m1, MergeSlot m2) {
            if (m1.mDateTaken != m2.mDateTaken) {
                return m1.mDateTaken < m2.mDateTaken ? -1 : 1;
            }
            return m1.mListIndex - m2.mListIndex;
        }
    }
    private static class MergeSlot {
        private int mOffset = -1;
        private final IImageList mList;
        int mListIndex;
        long mDateTaken;
        IImage mImage;
        public MergeSlot(IImageList list, int index) {
            mList = list;
            mListIndex = index;
        }
        public boolean next() {
            if (mOffset >= mList.getCount() - 1) return false;
            mImage = mList.getImageAt(++mOffset);
            mDateTaken = mImage.getDateTaken();
            return true;
        }
    }
    public void close() {
        for (int i = 0, n = mSubList.length; i < n; ++i) {
            mSubList[i].close();
        }
    }
}
