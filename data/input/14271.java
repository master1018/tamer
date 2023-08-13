public class ChainedAssignment {
    private int a = 0;
    private int b = 0;
    private int c = 0;
    static private int sa = 0;
    static private int sb = 0;
    static private int sc = 0;
    private class Inner {
        void test1() throws Exception {
            (a) = (b) = 1;
            if (a != 1 || b != 1) {
                throw new Exception("FAILED (11)");
            }
            System.out.println(a + " " + b + " " + c);
            a = b = c;
            if (a != 0 || b != 0) {
                throw new Exception("FAILED (12)");
            }
            System.out.println(a + " " + b + " " + c);
            a = (b) += 5;
            if (a != 5 || b != 5) {
                throw new Exception("FAILED (13)");
            }
            System.out.println(a + " " + b + " " + c);
        }
        void test2() throws Exception {
            sa = sb = 1;
            if (sa != 1 || sb != 1) {
                throw new Exception("FAILED (21)");
            }
            System.out.println(sa + " " + sb + " " + sc);
            sa = sb = sc;
            if (sa != 0 || sb != 0) {
                throw new Exception("FAILED (22)");
            }
            System.out.println(sa + " " + sb + " " + sc);
            sa = sb += 5;
            if (sa != 5 || sb != 5) {
                throw new Exception("FAILED (23)");
            }
            System.out.println(sa + " " + sb + " " + sc);
        }
    }
    public static void main(String[] args) throws Exception {
        ChainedAssignment outer = new ChainedAssignment();
        Inner inner = outer.new Inner();
        inner.test1();
        inner.test2();
    }
}
