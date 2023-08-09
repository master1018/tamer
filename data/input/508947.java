public class DrmRights {
    public static final int DRM_PERMISSION_PLAY = 1;
    public static final int DRM_PERMISSION_DISPLAY = 2;
    public static final int DRM_PERMISSION_EXECUTE = 3;
    public static final int DRM_PERMISSION_PRINT = 4;
    private static final int JNI_DRM_SUCCESS = 0;
    private static final int JNI_DRM_FAILURE = -1;
    private String roId = "";
    public DrmRights() {
    }
    public DrmConstraintInfo getConstraint(int permission) {
        DrmConstraintInfo c = new DrmConstraintInfo();
        int res = nativeGetConstraintInfo(permission, c);
        if (JNI_DRM_FAILURE == res)
            return null;
        return c;
    }
    public boolean consumeRights(int permission) {
        int res = nativeConsumeRights(permission);
        if (JNI_DRM_FAILURE == res)
            return false;
        return true;
    }
    private native int nativeGetConstraintInfo(int permission, DrmConstraintInfo constraint);
    private native int nativeConsumeRights(int permission);
    static {
        try {
            System.loadLibrary("drm1_jni");
        }
        catch (UnsatisfiedLinkError ule) {
            System.err.println("WARNING: Could not load libdrm1_jni.so");
        }
    }
}
