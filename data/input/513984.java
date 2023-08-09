public class NativeRegEx {
    public static native int open(String pattern, int flags);
    public static native int clone(int regex);
    public static native void close(int regex);
    public static native void setText(int regex, String text);
    public static native boolean matches(int regex, int startIndex);
    public static native boolean lookingAt(int regex, int startIndex);
    public static native boolean find(int regex, int startIndex);
    public static native boolean findNext(int regex);
    public static native int groupCount(int regex);
    public static native void startEnd(int regex, int[] startEnd);
    public static native void setRegion(int regex, int start, int end);
    public static native int regionStart(int regex);
    public static native int regionEnd(int regex);
    public static native void useTransparentBounds(int regex, boolean value);
    public static native boolean hasTransparentBounds(int regex);
    public static native void useAnchoringBounds(int regex, boolean value);
    public static native boolean hasAnchoringBounds(int regex);
    public static native boolean hitEnd(int regex);
    public static native boolean requireEnd(int regex);
    public static native void reset(int regex, int position);
}
