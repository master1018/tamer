public class DrmImageList extends ImageList implements IImageList {
    private static final String[] DRM_IMAGE_PROJECTION = new String[] {
        DrmStore.Images._ID,
        DrmStore.Images.DATA,
        DrmStore.Images.MIME_TYPE,
    };
    private static final int INDEX_ID = 0;
    private static final int INDEX_DATA_PATH = 1;
    private static final int INDEX_MIME_TYPE = 2;
    public DrmImageList(ContentResolver resolver, Uri imageUri, int sort,
            String bucketId) {
        super(resolver, imageUri, sort, bucketId);
    }
    @Override
    protected String sortOrder() {
        return "_id ASC";
    }
    @Override
    protected Cursor createCursor() {
        return mContentResolver.query(
                mBaseUri, DRM_IMAGE_PROJECTION, null, null, sortOrder());
    }
    private static class DrmImage extends Image {
        protected DrmImage(BaseImageList container, ContentResolver cr,
                long id, int index, Uri uri, String dataPath,
                long miniThumbMagic, String mimeType, long dateTaken,
                String title, int rotation) {
            super(container, cr, id, index, uri, dataPath,
                    mimeType, dateTaken, title, rotation);
        }
        @Override
        public int getDegreesRotated() {
            return 0;
        }
        @Override
        public boolean isDrm() {
            return true;
        }
        @Override
        public boolean isReadonly() {
            return true;
        }
        @Override
        public Bitmap miniThumbBitmap() {
            return fullSizeBitmap(IImage.MINI_THUMB_TARGET_SIZE,
                    IImage.MINI_THUMB_MAX_NUM_PIXELS);
        }
        @Override
        public Bitmap thumbBitmap(boolean rotateAsNeeded) {
            return fullSizeBitmap(IImage.THUMBNAIL_TARGET_SIZE,
                    IImage.THUMBNAIL_MAX_NUM_PIXELS);
        }
    }
    @Override
    protected BaseImage loadImageFromCursor(Cursor cursor) {
        long id = cursor.getLong(INDEX_ID);
        String dataPath = cursor.getString(INDEX_DATA_PATH);
        String mimeType = cursor.getString(INDEX_MIME_TYPE);
        return new DrmImage(this, mContentResolver, id, cursor.getPosition(),
                contentUri(id), dataPath, 0, mimeType, 0, "DrmImage-" + id,
                0);
    }
}
