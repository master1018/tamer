public abstract class BaseImageList implements IImageList {
    private static final String TAG = "BaseImageList";
    private static final int CACHE_CAPACITY = 512;
    private final LruCache<Integer, BaseImage> mCache =
            new LruCache<Integer, BaseImage>(CACHE_CAPACITY);
    protected ContentResolver mContentResolver;
    protected int mSort;
    protected Uri mBaseUri;
    protected Cursor mCursor;
    protected String mBucketId;
    protected boolean mCursorDeactivated = false;
    public BaseImageList(ContentResolver resolver, Uri uri, int sort,
            String bucketId) {
        mSort = sort;
        mBaseUri = uri;
        mBucketId = bucketId;
        mContentResolver = resolver;
        mCursor = createCursor();
        if (mCursor == null) {
            Log.w(TAG, "createCursor returns null.");
        }
        mCache.clear();
    }
    public void close() {
        try {
            invalidateCursor();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Caught exception while deactivating cursor.", e);
        }
        mContentResolver = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }
    public Uri contentUri(long id) {
        try {
            long existingId = ContentUris.parseId(mBaseUri);
            if (existingId != id) Log.e(TAG, "id mismatch");
            return mBaseUri;
        } catch (NumberFormatException ex) {
            return ContentUris.withAppendedId(mBaseUri, id);
        }
    }
    public int getCount() {
        Cursor cursor = getCursor();
        if (cursor == null) return 0;
        synchronized (this) {
            return cursor.getCount();
        }
    }
    public boolean isEmpty() {
        return getCount() == 0;
    }
    private Cursor getCursor() {
        synchronized (this) {
            if (mCursor == null) return null;
            if (mCursorDeactivated) {
                mCursor.requery();
                mCursorDeactivated = false;
            }
            return mCursor;
        }
    }
    public IImage getImageAt(int i) {
        BaseImage result = mCache.get(i);
        if (result == null) {
            Cursor cursor = getCursor();
            if (cursor == null) return null;
            synchronized (this) {
                result = cursor.moveToPosition(i)
                        ? loadImageFromCursor(cursor)
                        : null;
                mCache.put(i, result);
            }
        }
        return result;
    }
    public boolean removeImage(IImage image) {
        if (mContentResolver.delete(image.fullSizeImageUri(), null, null) > 0) {
            ((BaseImage) image).onRemove();
            invalidateCursor();
            invalidateCache();
            return true;
        } else {
            return false;
        }
    }
    public boolean removeImageAt(int i) {
        return removeImage(getImageAt(i));
    }
    protected abstract Cursor createCursor();
    protected abstract BaseImage loadImageFromCursor(Cursor cursor);
    protected abstract long getImageId(Cursor cursor);
    protected void invalidateCursor() {
        if (mCursor == null) return;
        mCursor.deactivate();
        mCursorDeactivated = true;
    }
    protected void invalidateCache() {
        mCache.clear();
    }
    private static final Pattern sPathWithId = Pattern.compile("(.*)/\\d+");
    private static String getPathWithoutId(Uri uri) {
        String path = uri.getPath();
        Matcher matcher = sPathWithId.matcher(path);
        return matcher.matches() ? matcher.group(1) : path;
    }
    private boolean isChildImageUri(Uri uri) {
        Uri base = mBaseUri;
        return Util.equals(base.getScheme(), uri.getScheme())
                && Util.equals(base.getHost(), uri.getHost())
                && Util.equals(base.getAuthority(), uri.getAuthority())
                && Util.equals(base.getPath(), getPathWithoutId(uri));
    }
    public IImage getImageForUri(Uri uri) {
        if (!isChildImageUri(uri)) return null;
        long matchId;
        try {
            matchId = ContentUris.parseId(uri);
        } catch (NumberFormatException ex) {
            Log.i(TAG, "fail to get id in: " + uri, ex);
            return null;
        }
        Cursor cursor = getCursor();
        if (cursor == null) return null;
        synchronized (this) {
            cursor.moveToPosition(-1); 
            for (int i = 0; cursor.moveToNext(); ++i) {
                if (getImageId(cursor) == matchId) {
                    BaseImage image = mCache.get(i);
                    if (image == null) {
                        image = loadImageFromCursor(cursor);
                        mCache.put(i, image);
                    }
                    return image;
                }
            }
            return null;
        }
    }
    public int getImageIndex(IImage image) {
        return ((BaseImage) image).mIndex;
    }
    protected String sortOrder() {
        String ascending =
                (mSort == ImageManager.SORT_ASCENDING)
                ? " ASC"
                : " DESC";
        String dateExpr =
                "case ifnull(datetaken,0)" +
                " when 0 then date_modified*1000" +
                " else datetaken" +
                " end";
        return dateExpr + ascending + ", _id" + ascending;
    }
}
