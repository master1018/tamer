public class PackageMain {
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("foo.bar.Baz");
        System.out.println("c=" + c);
        System.out.println("cl=" + c.getClassLoader());
        Package p = c.getPackage();
        System.out.println("p=" + p);
        Deprecated d = p.getAnnotation(Deprecated.class);
        if (d == null) throw new Error();
    }
}
