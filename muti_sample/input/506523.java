public class Clash3 {
    public static void main(String[] args) {
        InvocationHandler handler = new Clash3InvocationHandler();
        try {
            Proxy.newProxyInstance(Clash.class.getClassLoader(),
                new Class[] {
                    Interface3a.class,
                    Interface3base.class,
                    Interface3aa.class,
                    Interface3b.class },
                handler);
            System.err.println("Clash3 did not throw expected exception");
        } catch (IllegalArgumentException iae) {
            System.out.println("Clash3 threw expected exception");
        }
    }
}
class R3base implements I3 { int mBlah; public void x() {} }
class R3a extends R3base { int mBlah_a;  }
class R3aa extends R3a { int mBlah_aa;  }
class R3b implements I3 { int mBlah_b; public void x() {} }
interface I3 {
    void x();
}
interface Interface3base {
    public R3base thisIsTrouble();
}
interface Interface3a {
    public R3a thisIsTrouble();
}
interface Interface3aa {
    public R3aa thisIsTrouble();
}
interface Interface3b {
    public R3b thisIsTrouble();
}
class Clash3InvocationHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        return null;
    }
}
