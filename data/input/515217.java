public final class PlatformLibrary {    
    static {
        System.loadLibrary("platform_library_jni");
    }
    private int mJniInt = -1;
    public PlatformLibrary() {}
    public int getInt(boolean bad) {
        int result = getJniInt(bad);
        String reverse = reverseString("Android!");
        Log.i("PlatformLibrary", "getInt: " + result + ", '" + reverse + "'");
        return mJniInt;
    }
    private static void yodel(String msg) {
        Log.d("PlatformLibrary", "yodel: " + msg);
    }
    native private int getJniInt(boolean bad);
    native private static String reverseString(String str);
}
