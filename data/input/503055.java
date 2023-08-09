public class DrmRawContent {
    public static final String DRM_MIMETYPE_MESSAGE_STRING = "application/vnd.oma.drm.message";
    public static final String DRM_MIMETYPE_CONTENT_STRING = "application/vnd.oma.drm.content";
    public static final int DRM_FORWARD_LOCK = 1;
    public static final int DRM_COMBINED_DELIVERY = 2;
    public static final int DRM_SEPARATE_DELIVERY = 3;
    public static final int DRM_SEPARATE_DELIVERY_DM = 4;
    public static final int DRM_UNKNOWN_DATA_LEN = -1;
    private static final int DRM_MIMETYPE_MESSAGE = 1;
    private static final int DRM_MIMETYPE_CONTENT = 2;
    private static final int JNI_DRM_SUCCESS = 0;
    private static final int JNI_DRM_FAILURE = -1;
    private static final int JNI_DRM_EOF = -2;
    private static final int JNI_DRM_UNKNOWN_DATA_LEN = -3;
    private BufferedInputStream inData;
    private int inDataLen;
    private int id;
    private String rightsIssuer;
    private String mediaType;
    private int rawType;
    public DrmRawContent(InputStream inRawdata, int len, String mimeTypeStr) throws DrmException, IOException {
        int mimeType;
        id = -1;
        inData = new BufferedInputStream(inRawdata, 1024);
        inDataLen = len;
        if (DRM_MIMETYPE_MESSAGE_STRING.equals(mimeTypeStr))
            mimeType = DRM_MIMETYPE_MESSAGE;
        else if (DRM_MIMETYPE_CONTENT_STRING.equals(mimeTypeStr))
            mimeType = DRM_MIMETYPE_CONTENT;
        else
            throw new IllegalArgumentException("mimeType must be DRM_MIMETYPE_MESSAGE or DRM_MIMETYPE_CONTENT");
        if (len <= 0)
            throw new IllegalArgumentException("len must be > 0");
        id = nativeConstructDrmContent(inData, inDataLen, mimeType);
        if (JNI_DRM_FAILURE == id)
            throw new DrmException("nativeConstructDrmContent() returned JNI_DRM_FAILURE");
        rightsIssuer = nativeGetRightsAddress();
        rawType = nativeGetDeliveryMethod();
        if (JNI_DRM_FAILURE == rawType)
            throw new DrmException("nativeGetDeliveryMethod() returned JNI_DRM_FAILURE");
        mediaType = nativeGetContentType();
        if (null == mediaType)
            throw new DrmException("nativeGetContentType() returned null");
    }
    public String getRightsAddress() {
        return rightsIssuer;
    }
    public int getRawType() {
        return rawType;
    }
    public InputStream getContentInputStream(DrmRights rights) {
        if (null == rights)
            throw new NullPointerException();
        return new DrmInputStream(rights);
    }
    public String getContentType() {
        return mediaType;
    }
    public int getContentLength(DrmRights rights) throws DrmException {
        if (null == rights)
            throw new NullPointerException();
        int mediaLen = nativeGetContentLength();
        if (JNI_DRM_FAILURE == mediaLen)
            throw new DrmException("nativeGetContentLength() returned JNI_DRM_FAILURE");
        if (JNI_DRM_UNKNOWN_DATA_LEN == mediaLen)
            return DRM_UNKNOWN_DATA_LEN;
        return mediaLen;
    }
    class DrmInputStream extends InputStream
    {
        private boolean isClosed;
        private int offset;
        private byte[] b;
        public DrmInputStream(DrmRights rights) {
            isClosed = false;
            offset = 0;
            b = new byte[1];
        }
        public int available() throws IOException {
            int len = nativeGetContentLength();
            if (JNI_DRM_FAILURE == len)
                throw new IOException();
            if (JNI_DRM_UNKNOWN_DATA_LEN == len)
                return 0;
            int availableLen = len - offset;
            if (availableLen < 0)
                throw new IOException();
            return availableLen;
        }
        public int read() throws IOException {
            int res;
            res = read(b, 0, 1);
            if (-1 == res)
                return -1;
            return b[0] & 0xff;
        }
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        public int read(byte[] b, int off, int len) throws IOException {
            if (null == b)
                throw new NullPointerException();
            if (off < 0 || len < 0 || off + len > b.length)
                throw new IndexOutOfBoundsException();
            if (true == isClosed)
                throw new IOException();
            if (0 == len)
                return 0;
            len = nativeReadContent(b, off, len, offset);
            if (JNI_DRM_FAILURE == len)
                throw new IOException();
            else if (JNI_DRM_EOF == len)
                return -1;
            offset += len;
            return len;
        }
        public boolean markSupported() {
            return false;
        }
        public void mark(int readlimit) {
        }
        public void reset() throws IOException {
            throw new IOException();
        }
        public long skip(long n) throws IOException {
            return 0;
        }
        public void close() {
            isClosed = true;
        }
    }
    private native int nativeConstructDrmContent(InputStream data, int len, int mimeType);
    private native String nativeGetRightsAddress();
    private native int nativeGetDeliveryMethod();
    private native int nativeReadContent(byte[] buf, int bufOff, int len, int mediaOff);
    private native String nativeGetContentType();
    private native int nativeGetContentLength();
    protected native void finalize();
    static {
        try {
            System.loadLibrary("drm1_jni");
        }
        catch (UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load libdrm1_jni.so");
        }
    }
}
