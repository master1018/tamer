abstract class Base6891750 extends Thread {
    abstract public long m();
}
class Other6891750 extends Base6891750 {
    public long m() {
        return 0;
    }
}
public class Test6891750 extends Base6891750 {
    Base6891750 d;
    volatile long  value = 9;
    static int limit = 400000;
    Test6891750() {
        d = this;
    }
    public long m() {
        return value;
    }
    public long test(boolean doit) {
        if (doit) {
            long total0 = 0;
            long total1 = 0;
            long total2 = 0;
            long total3 = 0;
            long total4 = 0;
            long total5 = 0;
            long total6 = 0;
            long total7 = 0;
            long total8 = 0;
            long total9 = 0;
            for (int i = 0; i < limit; i++) {
                total0 += d.m();
                total1 += d.m();
                total2 += d.m();
                total3 += d.m();
                total4 += d.m();
                total5 += d.m();
                total6 += d.m();
                total7 += d.m();
                total8 += d.m();
                total9 += d.m();
            }
            return total0 + total1 + total2 + total3 + total4 + total5 + total6 + total7 + total8 + total9;
        }
        return 0;
    }
    public void run() {
        long result = test(true);
        for (int i = 0; i < 300; i++) {
            long result2 = test(true);
            if (result != result2) {
                throw new InternalError(result + " != " + result2);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        Test6891750 Test6891750 = new Test6891750();
        for (int i = 0; i < 200000; i++) {
            Test6891750.test(false);
        }
        Test6891750.start();
        Thread.sleep(2000);
        new Other6891750();
    }
}
