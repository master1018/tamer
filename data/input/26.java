public class InheritedMethods {
    public static void main(String[] args) throws Exception { new InheritedMethods(); }
    InheritedMethods() throws Exception {
        Class c = Foo.class;
        Method m = c.getMethod("removeAll", new Class[] { Collection.class });
        if (m.getDeclaringClass() != java.util.List.class) {
          throw new RuntimeException("TEST FAILED");
        }
    }
    interface Foo extends List { }
}
