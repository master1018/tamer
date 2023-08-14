public class Clash2 {
    public static void main(String[] args) {
        InvocationHandler handler = new Clash2InvocationHandler();
        try {
            Proxy.newProxyInstance(Clash.class.getClassLoader(),
                new Class[] { Interface2A.class, Interface2B.class },
                handler);
            System.err.println("Clash2 did not throw expected exception");
        } catch (IllegalArgumentException iae) {
            System.out.println("Clash2 threw expected exception");
        }
    }
}
interface Interface2A {
    public int thisIsOkay();
    public int thisIsTrouble();
}
interface Interface2B {
    public int thisIsOkay();
    public short thisIsTrouble();
}
class Clash2InvocationHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        return null;
    }
}
