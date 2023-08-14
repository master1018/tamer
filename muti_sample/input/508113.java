public class ImageManager {
    private static final String TAG = "ImageManager";
    private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
    public static enum DataLocation {
        NONE, INTERNAL, EXTERNAL, ALL
    }
    public static final Bitmap DEFAULT_THUMBNAIL = Bitmap.createBitmap(32, 32, Bitmap.Config.RGB_565);
    public static final Bitmap NO_IMAGE_BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
    public static final int SORT_ASCENDING = 1;
    public static final int SORT_DESCENDING = 2;
    public static final int INCLUDE_IMAGES = (1 << 0);
    public static final int INCLUDE_DRM_IMAGES = (1 << 1);
    public static final int INCLUDE_VIDEOS = (1 << 2);
    public static final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
    public static final String CAMERA_IMAGE_BUCKET_ID = getBucketId(CAMERA_IMAGE_BUCKET_NAME);
    public static String getBucketId(String path) {
        return String.valueOf(path.toLowerCase().hashCode());
    }
    public static void ensureOSXCompatibleFolder() {
        File nnnAAAAA = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/100ANDRO");
        if ((!nnnAAAAA.exists()) && (!nnnAAAAA.mkdir())) {
            Log.e(TAG, "create NNNAAAAA file: " + nnnAAAAA.getPath() + " failed");
        }
    }
    public static int roundOrientation(int orientationInput) {
        int orientation = orientationInput;
        if (orientation == -1) {
            orientation = 0;
        }
        orientation = orientation % 360;
        int retVal;
        if (orientation < (0 * 90) + 45) {
            retVal = 0;
        } else if (orientation < (1 * 90) + 45) {
            retVal = 90;
        } else if (orientation < (2 * 90) + 45) {
            retVal = 180;
        } else if (orientation < (3 * 90) + 45) {
            retVal = 270;
        } else {
            retVal = 0;
        }
        return retVal;
    }
    public static boolean isImageMimeType(String mimeType) {
        return mimeType.startsWith("image/");
    }
    public static boolean isVideoMimeType(String mimeType) {
        return mimeType.startsWith("video/");
    }
    public static void setImageSize(ContentResolver cr, Uri uri, long size) {
        ContentValues values = new ContentValues();
        values.put(Images.Media.SIZE, size);
        cr.update(uri, values, null, null);
    }
    public static Uri addImage(ContentResolver cr, String title, long dateAdded,
            long dateTaken, Double latitude, Double longitude, String directory,
            String filename, Bitmap source, byte[] jpegData, int[] degree) {
        OutputStream outputStream = null;
        String filePath = directory + "/" + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) dir.mkdirs();
            File file = new File(directory, filename);
            outputStream = new FileOutputStream(file);
            if (source != null) {
                source.compress(CompressFormat.JPEG, 75, outputStream);
                degree[0] = 0;
            } else {
                outputStream.write(jpegData);
                degree[0] = getExifOrientation(filePath);
            }
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
            return null;
        } catch (IOException ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            Util.closeSilently(outputStream);
        }
        long size = new File(directory, filename).length();
        ContentValues values = new ContentValues(11);
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DISPLAY_NAME, filename);
        values.put(Images.Media.DATE_TAKEN, dateTaken);
        values.put(Images.Media.DATE_MODIFIED, dateTaken);
        values.put(Images.Media.DATE_ADDED, dateAdded);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, degree[0]);
        values.put(Images.Media.DATA, filePath);
        values.put(Images.Media.SIZE, size);
        if (latitude != null && longitude != null) {
            values.put(Images.Media.LATITUDE, latitude.floatValue());
            values.put(Images.Media.LONGITUDE, longitude.floatValue());
        }
        return cr.insert(STORAGE_URI, values);
    }
    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Log.e(TAG, "cannot read exif", ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }
    private static class AddImageCancelable extends BaseCancelable<Void> {
        private final Uri mUri;
        private final ContentResolver mCr;
        private final byte[] mJpegData;
        public AddImageCancelable(Uri uri, ContentResolver cr, int orientation, Bitmap source, byte[] jpegData) {
            if (source == null && jpegData == null || uri == null) {
                throw new IllegalArgumentException("source cannot be null");
            }
            mUri = uri;
            mCr = cr;
            mJpegData = jpegData;
        }
        @Override
        protected Void execute() throws InterruptedException, ExecutionException {
            boolean complete = false;
            try {
                String[] projection = new String[] { ImageColumns._ID, ImageColumns.MINI_THUMB_MAGIC };
                Cursor c = mCr.query(mUri, projection, null, null, null);
                try {
                    c.moveToPosition(0);
                } finally {
                    c.close();
                }
                ContentValues values = new ContentValues();
                values.put(ImageColumns.MINI_THUMB_MAGIC, 0);
                mCr.update(mUri, values, null, null);
                OutputStream outputStream = null;
                try {
                    outputStream = mCr.openOutputStream(mUri);
                    if (outputStream != null) {
                        outputStream.write(mJpegData);
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "Cannot open file: " + mUri, ex);
                } finally {
                    Util.closeSilently(outputStream);
                }
                complete = true;
                return null;
            } finally {
                if (!complete) {
                    try {
                        mCr.delete(mUri, null, null);
                    } catch (Throwable t) {
                    }
                }
            }
        }
    }
    public static Cancelable<Void> storeImage(Uri uri, ContentResolver cr, int orientation, Bitmap source, byte[] jpegData) {
        return new AddImageCancelable(uri, cr, orientation, source, jpegData);
    }
    static boolean isSingleImageMode(String uriString) {
        return !uriString.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                && !uriString.startsWith(MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
    }
    private static boolean checkFsWritable() {
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }
    public static boolean quickHasStorage() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    public static boolean hasStorage() {
        return hasStorage(true);
    }
    public static boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    private static final Cursor query(final ContentResolver resolver, final Uri uri, final String[] projection,
            final String selection, final String[] selectionArgs, final String sortOrder) {
        try {
            if (resolver == null) {
                return null;
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }
    }
    public static final boolean isMediaScannerScanning(final ContentResolver cr) {
        boolean result = false;
        final Cursor cursor = query(cr, MediaStore.getMediaScannerUri(), new String[] { MediaStore.MEDIA_SCANNER_VOLUME }, null,
                null, null);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                result = "external".equals(cursor.getString(0));
            }
            cursor.close();
        }
        return result;
    }
    public static String getLastImageThumbPath() {
        return Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails/image_last_thumb";
    }
    public static String getLastVideoThumbPath() {
        return Environment.getExternalStorageDirectory().toString() + "/DCIM/.thumbnails/video_last_thumb";
    }
}
