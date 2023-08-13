public class NativeCMM {
    private static HashMap<ICC_Profile, Long> profileHandles = new HashMap<ICC_Profile, Long>();
    private static boolean isCMMLoaded;
    public static void addHandle(ICC_Profile key, long handle) {
        profileHandles.put(key, new Long(handle));
    }
    public static void removeHandle(ICC_Profile key) {
        profileHandles.remove(key);
    }
    public static long getHandle(ICC_Profile key) {
        return profileHandles.get(key).longValue();
    }
    public static native long cmmOpenProfile(byte[] data);
    public static native void cmmCloseProfile(long profileID);
    public static native int cmmGetProfileSize(long profileID);
    public static native void cmmGetProfile(long profileID, byte[] data);
    public static native int cmmGetProfileElementSize(long profileID, int signature);
    public static native void cmmGetProfileElement(long profileID, int signature,
                                           byte[] data);
    public static native void cmmSetProfileElement(long profileID, int tagSignature,
                                           byte[] data);
    public static native long cmmCreateMultiprofileTransform(
            long[] profileHandles,
            int[] renderingIntents
        );
    public static native void cmmDeleteTransform(long transformHandle);
    public static native void cmmTranslateColors(long transformHandle,
            NativeImageFormat src,
            NativeImageFormat dest);
    static void loadCMM() {
        if (!isCMMLoaded) {
            AccessController.doPrivileged(
                  new PrivilegedAction<Void>() {
                    public Void run() {
                        System.loadLibrary("lcmm"); 
                        return null;
                    }
            } );
            isCMMLoaded = true;
        }
    }
    static {
        loadCMM();
    }
}
