class MediaThumbRequest {
    private static final String TAG = "MediaThumbRequest";
    static final int PRIORITY_LOW = 20;
    static final int PRIORITY_NORMAL = 10;
    static final int PRIORITY_HIGH = 5;
    static final int PRIORITY_CRITICAL = 0;
    static enum State {WAIT, DONE, CANCEL}
    private static final String[] THUMB_PROJECTION = new String[] {
        BaseColumns._ID 
    };
    ContentResolver mCr;
    String mPath;
    long mRequestTime = System.currentTimeMillis();
    int mCallingPid = Binder.getCallingPid();
    long mGroupId;
    int mPriority;
    Uri mUri;
    Uri mThumbUri;
    String mOrigColumnName;
    boolean mIsVideo;
    long mOrigId;
    State mState = State.WAIT;
    long mMagic;
    private BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private final Random mRandom = new Random();
    static Comparator<MediaThumbRequest> getComparator() {
        return new Comparator<MediaThumbRequest>() {
            public int compare(MediaThumbRequest r1, MediaThumbRequest r2) {
                if (r1.mPriority != r2.mPriority) {
                    return r1.mPriority < r2.mPriority ? -1 : 1;
                }
                return r1.mRequestTime == r2.mRequestTime ? 0 :
                        r1.mRequestTime < r2.mRequestTime ? -1 : 1;
            }
        };
    }
    MediaThumbRequest(ContentResolver cr, String path, Uri uri, int priority, long magic) {
        mCr = cr;
        mPath = path;
        mPriority = priority;
        mMagic = magic;
        mUri = uri;
        mIsVideo = "video".equals(uri.getPathSegments().get(1));
        mOrigId = ContentUris.parseId(uri);
        mThumbUri = mIsVideo
                ? Video.Thumbnails.EXTERNAL_CONTENT_URI
                : Images.Thumbnails.EXTERNAL_CONTENT_URI;
        mOrigColumnName = mIsVideo
                ? Video.Thumbnails.VIDEO_ID
                : Images.Thumbnails.IMAGE_ID;
        String groupIdParam = uri.getQueryParameter("group_id");
        if (groupIdParam != null) {
            mGroupId = Long.parseLong(groupIdParam);
        }
    }
    Uri updateDatabase(Bitmap thumbnail) {
        Cursor c = mCr.query(mThumbUri, THUMB_PROJECTION,
                mOrigColumnName+ " = " + mOrigId, null, null);
        if (c == null) return null;
        try {
            if (c.moveToFirst()) {
                return ContentUris.withAppendedId(mThumbUri, c.getLong(0));
            }
        } finally {
            if (c != null) c.close();
        }
        ContentValues values = new ContentValues(4);
        values.put(Images.Thumbnails.KIND, Images.Thumbnails.MINI_KIND);
        values.put(mOrigColumnName, mOrigId);
        values.put(Images.Thumbnails.WIDTH, thumbnail.getWidth());
        values.put(Images.Thumbnails.HEIGHT, thumbnail.getHeight());
        try {
            return mCr.insert(mThumbUri, values);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        }
    }
    void execute() throws IOException {
        MiniThumbFile miniThumbFile = MiniThumbFile.instance(mUri);
        long magic = mMagic;
        if (magic != 0) {
            long fileMagic = miniThumbFile.getMagic(mOrigId);
            if (fileMagic == magic) {
                Cursor c = null;
                ParcelFileDescriptor pfd = null;
                try {
                    c = mCr.query(mThumbUri, THUMB_PROJECTION,
                            mOrigColumnName + " = " + mOrigId, null, null);
                    if (c != null && c.moveToFirst()) {
                        pfd = mCr.openFileDescriptor(
                                mThumbUri.buildUpon().appendPath(c.getString(0)).build(), "r");
                    }
                } catch (IOException ex) {
                } finally {
                    if (c != null) c.close();
                    if (pfd != null) {
                        pfd.close();
                        return;
                    }
                }
            }
        }
        Bitmap bitmap = null;
        if (mPath != null) {
            if (mIsVideo) {
                bitmap = ThumbnailUtils.createVideoThumbnail(mPath,
                        Video.Thumbnails.MINI_KIND);
            } else {
                bitmap = ThumbnailUtils.createImageThumbnail(mPath,
                        Images.Thumbnails.MINI_KIND);
            }
            if (bitmap == null) {
                Log.w(TAG, "Can't create mini thumbnail for " + mPath);
                return;
            }
            Uri uri = updateDatabase(bitmap);
            if (uri != null) {
                OutputStream thumbOut = mCr.openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, thumbOut);
                thumbOut.close();
            }
        }
        bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                        ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
                        ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL,
                        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        if (bitmap != null) {
            ByteArrayOutputStream miniOutStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, miniOutStream);
            bitmap.recycle();
            byte [] data = null;
            try {
                miniOutStream.close();
                data = miniOutStream.toByteArray();
            } catch (java.io.IOException ex) {
                Log.e(TAG, "got exception ex " + ex);
            }
            if (data != null) {
                do {
                    magic = mRandom.nextLong();
                } while (magic == 0);
                miniThumbFile.saveMiniThumbToFile(data, mOrigId, magic);
                ContentValues values = new ContentValues();
                values.put(ImageColumns.MINI_THUMB_MAGIC, magic);
                mCr.update(mUri, values, null, null);
            }
        } else {
            Log.w(TAG, "can't create bitmap for thumbnail.");
        }
    }
}
