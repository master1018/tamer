public class T6751514 {
    static class Foo<X> {
        X x;
        Foo (X x) {
            this.x = x;
        }
    }
    static void test1(Foo<Integer> foo) {
        int start = foo.x;
        equals(foo.x += 1, start + 1);
        equals(foo.x++, start + 1);
        equals(++foo.x, start + 3);
        equals(foo.x--, start + 3);
        equals(foo.x -= 1, start + 1);
        equals(--foo.x, start);
    }
    static void test2(Foo<Integer> foo) {
        int start = foo.x;
        equals((foo.x) += 1, start + 1);
        equals((foo.x)++, start + 1);
        equals(++(foo.x), start + 3);
        equals((foo.x)--, start + 3);
        equals((foo.x) -= 1, start + 1);
        equals(--(foo.x), start);
    }
    static void test3(Foo<Integer> foo) {
        int start = foo.x;
        equals(((foo.x)) += 1, start + 1);
        equals(((foo.x))++, start + 1);
        equals(++((foo.x)), start + 3);
        equals(((foo.x))--, start + 3);
        equals(((foo.x)) -= 1, start + 1);
        equals(--((foo.x)), start);
    }
    public static void main(String[] args) {
        test1(new Foo<Integer>(1));
        test2(new Foo<Integer>(1));
        test3(new Foo<Integer>(1));
    }
    static void equals(int found, int req) {
        if (found != req) {
            throw new AssertionError("Error (expected: "+ req +
                                     " - found: " + found + ")");
        }
    }
}
