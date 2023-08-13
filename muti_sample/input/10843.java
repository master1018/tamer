public class T6271292 extends SuperClass {
    public static void main(String[] args) {
        SuperClass s = new SuperClass((args[0].equals("0")) ? 0 : 1);
        s.test();
    }
    T6271292(boolean b) {
        super(b ? 1 : 2);
    }
}
class SuperClass {
    double d;
    SuperClass(double dd) { d = dd; }
    double test() {
        if (d == 0) {
            return d;
        } else if (d > 0) {
            return d++;
        } else {
            return d--;
        }
    }
}
