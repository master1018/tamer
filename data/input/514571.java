public class ContactPhotoLoader implements Callback {
    private static final String LOADER_THREAD_NAME = "ContactPhotoLoader";
    private static final int MESSAGE_REQUEST_LOADING = 1;
    private static final int MESSAGE_PHOTOS_LOADED = 2;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private final String[] COLUMNS = new String[] { Photo._ID, Photo.PHOTO };
    private final int mDefaultResourceId;
    private static class BitmapHolder {
        private static final int NEEDED = 0;
        private static final int LOADING = 1;
        private static final int LOADED = 2;
        int state;
        SoftReference<Bitmap> bitmapRef;
    }
    private final ConcurrentHashMap<Long, BitmapHolder> mBitmapCache =
            new ConcurrentHashMap<Long, BitmapHolder>();
    private final ConcurrentHashMap<ImageView, Long> mPendingRequests =
            new ConcurrentHashMap<ImageView, Long>();
    private final Handler mMainThreadHandler = new Handler(this);
    private LoaderThread mLoaderThread;
    private boolean mLoadingRequested;
    private boolean mPaused;
    private final Context mContext;
    public ContactPhotoLoader(Context context, int defaultResourceId) {
        mDefaultResourceId = defaultResourceId;
        mContext = context;
    }
    public void loadPhoto(ImageView view, long photoId) {
        if (photoId == 0) {
            view.setImageResource(mDefaultResourceId);
            mPendingRequests.remove(view);
        } else {
            boolean loaded = loadCachedPhoto(view, photoId);
            if (loaded) {
                mPendingRequests.remove(view);
            } else {
                mPendingRequests.put(view, photoId);
                if (!mPaused) {
                    requestLoading();
                }
            }
        }
    }
    private boolean loadCachedPhoto(ImageView view, long photoId) {
        BitmapHolder holder = mBitmapCache.get(photoId);
        if (holder == null) {
            holder = new BitmapHolder();
            mBitmapCache.put(photoId, holder);
        } else if (holder.state == BitmapHolder.LOADED) {
            if (holder.bitmapRef == null) {
                view.setImageResource(mDefaultResourceId);
                return true;
            }
            Bitmap bitmap = holder.bitmapRef.get();
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
                return true;
            }
            holder.bitmapRef = null;
        }
        view.setImageResource(mDefaultResourceId);
        holder.state = BitmapHolder.NEEDED;
        return false;
    }
    public void stop() {
        pause();
        if (mLoaderThread != null) {
            mLoaderThread.quit();
            mLoaderThread = null;
        }
        mPendingRequests.clear();
        mBitmapCache.clear();
    }
    public void clear() {
        mPendingRequests.clear();
        mBitmapCache.clear();
    }
    public void pause() {
        mPaused = true;
    }
    public void resume() {
        mPaused = false;
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }
    private void requestLoading() {
        if (!mLoadingRequested) {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOADING: {
                mLoadingRequested = false;
                if (!mPaused) {
                    if (mLoaderThread == null) {
                        mLoaderThread = new LoaderThread(mContext.getContentResolver());
                        mLoaderThread.start();
                    }
                    mLoaderThread.requestLoading();
                }
                return true;
            }
            case MESSAGE_PHOTOS_LOADED: {
                if (!mPaused) {
                    processLoadedImages();
                }
                return true;
            }
        }
        return false;
    }
    private void processLoadedImages() {
        Iterator<ImageView> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext()) {
            ImageView view = iterator.next();
            long photoId = mPendingRequests.get(view);
            boolean loaded = loadCachedPhoto(view, photoId);
            if (loaded) {
                iterator.remove();
            }
        }
        if (!mPendingRequests.isEmpty()) {
            requestLoading();
        }
    }
    private void cacheBitmap(long id, byte[] bytes) {
        if (mPaused) {
            return;
        }
        BitmapHolder holder = new BitmapHolder();
        holder.state = BitmapHolder.LOADED;
        if (bytes != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                holder.bitmapRef = new SoftReference<Bitmap>(bitmap);
            } catch (OutOfMemoryError e) {
            }
        }
        mBitmapCache.put(id, holder);
    }
    private void obtainPhotoIdsToLoad(ArrayList<Long> photoIds,
            ArrayList<String> photoIdsAsStrings) {
        photoIds.clear();
        photoIdsAsStrings.clear();
        Iterator<Long> iterator = mPendingRequests.values().iterator();
        while (iterator.hasNext()) {
            Long id = iterator.next();
            BitmapHolder holder = mBitmapCache.get(id);
            if (holder != null && holder.state == BitmapHolder.NEEDED) {
                holder.state = BitmapHolder.LOADING;
                photoIds.add(id);
                photoIdsAsStrings.add(id.toString());
            }
        }
    }
    private class LoaderThread extends HandlerThread implements Callback {
        private final ContentResolver mResolver;
        private final StringBuilder mStringBuilder = new StringBuilder();
        private final ArrayList<Long> mPhotoIds = Lists.newArrayList();
        private final ArrayList<String> mPhotoIdsAsStrings = Lists.newArrayList();
        private Handler mLoaderThreadHandler;
        public LoaderThread(ContentResolver resolver) {
            super(LOADER_THREAD_NAME);
            mResolver = resolver;
        }
        public void requestLoading() {
            if (mLoaderThreadHandler == null) {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
            mLoaderThreadHandler.sendEmptyMessage(0);
        }
        public boolean handleMessage(Message msg) {
            loadPhotosFromDatabase();
            mMainThreadHandler.sendEmptyMessage(MESSAGE_PHOTOS_LOADED);
            return true;
        }
        private void loadPhotosFromDatabase() {
            obtainPhotoIdsToLoad(mPhotoIds, mPhotoIdsAsStrings);
            int count = mPhotoIds.size();
            if (count == 0) {
                return;
            }
            mStringBuilder.setLength(0);
            mStringBuilder.append(Photo._ID + " IN(");
            for (int i = 0; i < count; i++) {
                if (i != 0) {
                    mStringBuilder.append(',');
                }
                mStringBuilder.append('?');
            }
            mStringBuilder.append(')');
            Cursor cursor = null;
            try {
                cursor = mResolver.query(Data.CONTENT_URI,
                        COLUMNS,
                        mStringBuilder.toString(),
                        mPhotoIdsAsStrings.toArray(EMPTY_STRING_ARRAY),
                        null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Long id = cursor.getLong(0);
                        byte[] bytes = cursor.getBlob(1);
                        cacheBitmap(id, bytes);
                        mPhotoIds.remove(id);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            count = mPhotoIds.size();
            for (int i = 0; i < count; i++) {
                cacheBitmap(mPhotoIds.get(i), null);
            }
        }
    }
}
