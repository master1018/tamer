public class EqualsCast {
    public static void main(String[] args) throws Exception {
        Map m1 = new MyProvider("foo", 69, "baz");
        Map m2 = new MyProvider("foo", 69, "baz");
        m1.equals(m2);
    }
}
class MyProvider extends Provider {
    private String name;
    public MyProvider(String name, double version, String info) {
        super(name, version, info);
        this.name = name;
        put("Signature.sigalg", "sigimpl");
    }
    public String getName() {
        return this.name;
    }
}
