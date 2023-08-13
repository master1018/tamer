public final class ComposedCharIter {
    public static final int DONE = NormalizerBase.DONE;
    private static int chars[];
    private static String decomps[];
    private static int decompNum;
    static {
        int maxNum = 2000;     
        chars = new int[maxNum];
        decomps = new String[maxNum];
        decompNum = NormalizerImpl.getDecompose(chars, decomps);
    }
    public ComposedCharIter() { }
    public int next() {
        if (curChar == decompNum - 1) {
            return DONE;
        }
        return chars[++curChar];
    }
    public String decomposition() {
        return decomps[curChar];
    }
    private int curChar = -1;
}
