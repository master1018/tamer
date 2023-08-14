public final class LatencyTimer
{
    final String TAG = "LatencyTimer";
    final int mSampleSize;
    final int mScaleFactor;
    volatile HashMap<String, long[]> store = new HashMap<String, long[]>();
    public LatencyTimer(int sampleSize, int scaleFactor) {
        if (scaleFactor == 0) {
            scaleFactor = 1;
        }
        mScaleFactor = scaleFactor;
        mSampleSize = sampleSize;
    }
    public void sample(String tag, long delta) {
        long[] array = getArray(tag);
        final int index = (int) array[mSampleSize]++;
        array[index] = delta;
        if (array[mSampleSize] == mSampleSize) {
            long totalDelta = 0;
            for (long d : array) {
                totalDelta += d/mScaleFactor;
            }
            array[mSampleSize] = 0;
            Log.i(TAG, tag + " average = " + totalDelta / mSampleSize);
        }
    }
    private long[] getArray(String tag) {
        long[] data = store.get(tag);
        if (data == null) {
            synchronized(store) {
                data = store.get(tag);
                if (data == null) {
                    data = new long[mSampleSize + 1];
                    store.put(tag, data);
                    data[mSampleSize] = 0;
                }
            }
        }
        return data;
    }
}
