public class Clash4 {
    public static void main(String[] args) {
        InvocationHandler handler = new Clash4InvocationHandler();
        try {
            Proxy.newProxyInstance(Clash.class.getClassLoader(),
                new Class[] {
                    Interface4a.class,
                    Interface4aa.class,
                    Interface4base.class,
                    Interface4b.class,
                    Interface4bb.class },
                handler);
            System.err.println("Clash4 did not throw expected exception");
        } catch (IllegalArgumentException iae) {
            System.out.println("Clash4 threw expected exception");
        }
    }
}
class R4base { int mBlah;  }
class R4a extends R4base { int mBlah_a;  }
class R4aa extends R4a { int mBlah_aa;  }
class R4b extends R4base { int mBlah_b;  }
class R4bb extends R4b { int mBlah_bb;  }
interface Interface4base {
    public R4base thisIsTrouble();
}
interface Interface4a {
    public R4a thisIsTrouble();
}
interface Interface4aa {
    public R4aa thisIsTrouble();
}
interface Interface4b {
    public R4b thisIsTrouble();
}
interface Interface4bb {
    public R4bb thisIsTrouble();
}
class Clash4InvocationHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        return null;
    }
}
