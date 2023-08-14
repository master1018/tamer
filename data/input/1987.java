public class InheritedInterfaceMethods {
  public static void main(String[] args) {
    Method[] methods = InheritedInterfaceMethodsC.class.getMethods();
    for (int i = 0; i < methods.length; i++) {
      if (methods[i].getName().equals("a")) {
        return;
      }
    }
    throw new RuntimeException("TEST FAILED");
  }
}
interface InheritedInterfaceMethodsA {
  public void a();
}
interface InheritedInterfaceMethodsB extends InheritedInterfaceMethodsA {
  public void b();
}
interface InheritedInterfaceMethodsC extends InheritedInterfaceMethodsB {
  public void c();
}
