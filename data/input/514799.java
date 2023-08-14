public class DrmRightsManager {
    public static final String DRM_MIMETYPE_RIGHTS_XML_STRING = "application/vnd.oma.drm.rights+xml";
    public static final String DRM_MIMETYPE_RIGHTS_WBXML_STRING = "application/vnd.oma.drm.rights+wbxml";
    private static final int DRM_MIMETYPE_RIGHTS_XML = 3;
    private static final int DRM_MIMETYPE_RIGHTS_WBXML = 4;
    private static final int DRM_MIMETYPE_MESSAGE = 1;
    private static final int JNI_DRM_SUCCESS = 0;
    private static final int JNI_DRM_FAILURE = -1;
    private static DrmRightsManager singleton = null;
    protected DrmRightsManager() {
    }
    public static synchronized DrmRightsManager getInstance() {
        if (singleton == null) {
            singleton = new DrmRightsManager();
        }
        return singleton;
    }
    public synchronized DrmRights installRights(InputStream rightsData, int len, String mimeTypeStr) throws DrmException, IOException {
        int mimeType = 0;
        if (DRM_MIMETYPE_RIGHTS_XML_STRING.equals(mimeTypeStr))
            mimeType = DRM_MIMETYPE_RIGHTS_XML;
        else if (DRM_MIMETYPE_RIGHTS_WBXML_STRING.equals(mimeTypeStr))
            mimeType = DRM_MIMETYPE_RIGHTS_WBXML;
        else if (DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING.equals(mimeTypeStr))
            mimeType = DRM_MIMETYPE_MESSAGE;
        else
            throw new IllegalArgumentException("mimeType must be DRM_MIMETYPE_RIGHTS_XML or DRM_MIMETYPE_RIGHTS_WBXML or DRM_MIMETYPE_MESSAGE");
        if (len <= 0)
            return null;
        DrmRights rights = new DrmRights();
        int res = nativeInstallDrmRights(rightsData, len, mimeType, rights);
        if (JNI_DRM_FAILURE == res)
            throw new DrmException("nativeInstallDrmRights() returned JNI_DRM_FAILURE");
        return rights;
    }
    public synchronized DrmRights queryRights(DrmRawContent content) {
        DrmRights rights = new DrmRights();
        int res = nativeQueryRights(content, rights);
        if (JNI_DRM_FAILURE == res)
            return null;
        return rights;
    }
    public synchronized List getRightsList() {
        List rightsList = new ArrayList();
        int num = nativeGetNumOfRights();
        if (JNI_DRM_FAILURE == num)
            return null;
        if (num > 0) {
            DrmRights[] rightsArray = new DrmRights[num];
            int i;
            for (i = 0; i < num; i++)
                rightsArray[i] = new DrmRights();
            num = nativeGetRightsList(rightsArray, num);
            if (JNI_DRM_FAILURE == num)
                return null;
            for (i = 0; i < num; i++)
                rightsList.add(rightsArray[i]);
        }
        return rightsList;
    }
    public synchronized void deleteRights(DrmRights rights) {
        int res = nativeDeleteRights(rights);
        if (JNI_DRM_FAILURE == res)
            return;
    }
    private native int nativeInstallDrmRights(InputStream data, int len, int mimeType, DrmRights rights);
    private native int nativeQueryRights(DrmRawContent content, DrmRights rights);
    private native int nativeGetNumOfRights();
    private native int nativeGetRightsList(DrmRights[] rights, int numRights);
    private native int nativeDeleteRights(DrmRights rights);
    static {
        try {
            System.loadLibrary("drm1_jni");
        }
        catch (UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load libdrm1_jni.so");
        }
    }
}
