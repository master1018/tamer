public class VCardEntryCommitter implements VCardEntryHandler {
    public static String LOG_TAG = "VCardEntryComitter";
    private final ContentResolver mContentResolver;
    private long mTimeToCommit;
    private ArrayList<Uri> mCreatedUris = new ArrayList<Uri>();
    public VCardEntryCommitter(ContentResolver resolver) {
        mContentResolver = resolver;
    }
    public void onStart() {
    }
    public void onEnd() {
        if (VCardConfig.showPerformanceLog()) {
            Log.d(LOG_TAG, String.format("time to commit entries: %d ms", mTimeToCommit));
        }
    }
    public void onEntryCreated(final VCardEntry contactStruct) {
        long start = System.currentTimeMillis();
        mCreatedUris.add(contactStruct.pushIntoContentResolver(mContentResolver));
        mTimeToCommit += System.currentTimeMillis() - start;
    }
   public ArrayList<Uri> getCreatedUris() {
        return mCreatedUris;
    }
}