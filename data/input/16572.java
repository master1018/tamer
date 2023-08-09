public class TestDeoptInt6769124 {
    static class A {
        volatile int vl;
        A(int v) {
            vl = v;
        }
    }
    static void m(int b) {
        A a = new A(10);
        int c;
        c = b + a.vl; 
        if(c != 20) {
            System.out.println("a (= " + a.vl + ") + b (= " + b + ") = c (= " + c + ") != 20");
            throw new InternalError();
        }
    }
    public static void main(String[] args) {
        m(10);
    }
}
