class Test6756768_2a {
    static int var = ++Test6756768_2.var;
}
public class Test6756768_2 {
    static int var = 1;
    static Object d2 = null;
    static void test_static_field() {
        int v = var;
        int v2 = Test6756768_2a.var;
        int v3 = var;
        var = v3;
    }
    public static void main(String[] args) {
        var = 1;
        test_static_field();
        if (var != 2) {
            throw new InternalError();
        }
    }
}
