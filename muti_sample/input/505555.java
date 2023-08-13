public final class NativeTestTarget {
    private NativeTestTarget() {
    }
    public static native void emptyJniStaticMethod0();
    public static native void emptyJniStaticMethod6(int a, int b, int c,
        int d, int e, int f);
    public static native void emptyJniStaticMethod6L(String a, String[] b,
        int[][] c, Object d, Object[] e, Object[][][][] f);
    public static void emptyInlineMethod() {
    }
    public static native void emptyInternalStaticMethod();
}
