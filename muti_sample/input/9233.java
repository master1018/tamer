public class Cast {
    static class Foo {}
    public static void main(String argv[]) throws Exception {
        Object o = Foo.class.newInstance();
        Foo f = Foo.class.cast(o);
        if (f == null) throw new Error();
        Foo f2 = Foo.class.cast(null);
    }
}
