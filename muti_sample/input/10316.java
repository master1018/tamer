class A extends BasicPermission {
    public A(String name) { super(name); }
}
class B extends BasicPermission {
    public B(String name) { super(name); }
}
public class EqualsImplies {
    public static void main(String[]args) throws Exception {
      Permission p1 = new A("foo");
      Permission p2 = new B("foo");
      if (p1.implies(p2) || p2.implies(p1) || p1.equals(p2)) {
          throw new Exception("Test failed");
      }
      if (! (p1.implies(p1) && p1.equals(p1))) {
          throw new Exception("Test failed");
      }
    }
}
