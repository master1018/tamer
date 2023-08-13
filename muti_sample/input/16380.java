public class TestUnalignedLoad6769124 {
    static long l1v = 0x200000003L;
    static long l2v = 0x400000005L;
    static double d1v = Double.MAX_VALUE;
    static double d2v = Double.MIN_VALUE;
    public static void main(String[] args) {
        long l1 = l1v;
        double d1 = d1v;
        long l2 = l2v;
        double d2 = d2v;
        for (int i = 0; i < 10000000; i++) {
        }
        boolean error = false;
        if (l1 != l1v) {
            System.out.println(l1 + " != " + l1v);
            error = true;
        }
        if (l2 != l2v) {
            System.out.println(l2 + " != " + l2v);
            error = true;
        }
        if (d1 != d1v) {
            System.out.println(d1 + " != " + d1v);
            error = true;
        }
        if (d2 != d2v) {
            System.out.println(d2 + " != " + d2v);
            error = true;
        }
        if (error) {
            throw new InternalError();
        }
    }
}
