public class Test6378821 {
    static final int[]  ia = new int[]  { 0x12345678 };
    static final long[] la = new long[] { 0x12345678abcdefL };
    public static void main(String [] args) {
        Integer.bitCount(1);
        Long.bitCount(1);
        sub(ia[0]);
        sub(la[0]);
        sub(ia);
        sub(la);
    }
    static void check(int i, int expected, int result) {
        if (result != expected) {
            throw new InternalError("Wrong population count for " + i + ": " + result + " != " + expected);
        }
    }
    static void check(long l, int expected, int result) {
        if (result != expected) {
            throw new InternalError("Wrong population count for " + l + ": " + result + " != " + expected);
        }
    }
    static void sub(int i)     { check(i,     fint(i),  fcomp(i) ); }
    static void sub(int[] ia)  { check(ia[0], fint(ia), fcomp(ia)); }
    static void sub(long l)    { check(l,     fint(l),  fcomp(l) ); }
    static void sub(long[] la) { check(la[0], fint(la), fcomp(la)); }
    static int fint (int i)     { return Integer.bitCount(i); }
    static int fcomp(int i)     { return Integer.bitCount(i); }
    static int fint (int[] ia)  { return Integer.bitCount(ia[0]); }
    static int fcomp(int[] ia)  { return Integer.bitCount(ia[0]); }
    static int fint (long l)    { return Long.bitCount(l); }
    static int fcomp(long l)    { return Long.bitCount(l); }
    static int fint (long[] la) { return Long.bitCount(la[0]); }
    static int fcomp(long[] la) { return Long.bitCount(la[0]); }
}
