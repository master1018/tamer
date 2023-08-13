public class MediaMetadataRetriever
{
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    @SuppressWarnings("unused")
    private int mNativeContext;
    public MediaMetadataRetriever() {
        native_setup();
    }
    public native void setMode(int mode);
    public native int getMode();
    public native void setDataSource(String path) throws IllegalArgumentException;
    public native void setDataSource(FileDescriptor fd, long offset, long length)
            throws IllegalArgumentException;
    public void setDataSource(FileDescriptor fd)
            throws IllegalArgumentException {
        setDataSource(fd, 0, 0x7ffffffffffffffL);
    }
    public void setDataSource(Context context, Uri uri)
        throws IllegalArgumentException, SecurityException {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        String scheme = uri.getScheme();
        if(scheme == null || scheme.equals("file")) {
            setDataSource(uri.getPath());
            return;
        }
        AssetFileDescriptor fd = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            try {
                fd = resolver.openAssetFileDescriptor(uri, "r");
            } catch(FileNotFoundException e) {
                throw new IllegalArgumentException();
            }
            if (fd == null) {
                throw new IllegalArgumentException();
            }
            FileDescriptor descriptor = fd.getFileDescriptor();
            if (!descriptor.valid()) {
                throw new IllegalArgumentException();
            }
            if (fd.getDeclaredLength() < 0) {
                setDataSource(descriptor);
            } else {
                setDataSource(descriptor, fd.getStartOffset(), fd.getDeclaredLength());
            }
            return;
        } catch (SecurityException ex) {
        } finally {
            try {
                if (fd != null) {
                    fd.close();
                }
            } catch(IOException ioEx) {
            }
        }
        setDataSource(uri.toString());
    }
    public native String extractMetadata(int keyCode);
    public native Bitmap captureFrame();
    public native byte[] extractAlbumArt();
    public native void release();
    private native void native_setup();
    private static native void native_init();
    private native final void native_finalize();
    @Override
    protected void finalize() throws Throwable {
        try {
            native_finalize();
        } finally {
            super.finalize();
        }
    }
    public static final int MODE_GET_METADATA_ONLY  = 0x01;
    public static final int MODE_CAPTURE_FRAME_ONLY = 0x02;
    public static final int METADATA_KEY_CD_TRACK_NUMBER = 0;
    public static final int METADATA_KEY_ALBUM           = 1;
    public static final int METADATA_KEY_ARTIST          = 2;
    public static final int METADATA_KEY_AUTHOR          = 3;
    public static final int METADATA_KEY_COMPOSER        = 4;
    public static final int METADATA_KEY_DATE            = 5;
    public static final int METADATA_KEY_GENRE           = 6;
    public static final int METADATA_KEY_TITLE           = 7;
    public static final int METADATA_KEY_YEAR            = 8;
    public static final int METADATA_KEY_DURATION        = 9;
    public static final int METADATA_KEY_NUM_TRACKS      = 10;
    public static final int METADATA_KEY_IS_DRM_CRIPPLED = 11;
    public static final int METADATA_KEY_CODEC           = 12;
    public static final int METADATA_KEY_RATING          = 13;
    public static final int METADATA_KEY_COMMENT         = 14;
    public static final int METADATA_KEY_COPYRIGHT       = 15;
    public static final int METADATA_KEY_BIT_RATE        = 16;
    public static final int METADATA_KEY_FRAME_RATE      = 17;
    public static final int METADATA_KEY_VIDEO_FORMAT    = 18;
    public static final int METADATA_KEY_VIDEO_HEIGHT    = 19;
    public static final int METADATA_KEY_VIDEO_WIDTH     = 20;
    public static final int METADATA_KEY_WRITER          = 21;
    public static final int METADATA_KEY_MIMETYPE        = 22;
    public static final int METADATA_KEY_DISCNUMBER      = 23;
    public static final int METADATA_KEY_ALBUMARTIST     = 24;
}
