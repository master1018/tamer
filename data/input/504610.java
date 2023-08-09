public class Clash {
    public static void main(String[] args) {
        InvocationHandler handler = new ClashInvocationHandler();
        try {
            Proxy.newProxyInstance(Clash.class.getClassLoader(),
                new Class[] { Interface1A.class, Interface1A.class },
                handler);
            System.err.println("Dupe did not throw expected exception");
        } catch (IllegalArgumentException iae) {
            System.out.println("Dupe threw expected exception");
        }
        try {
            Proxy.newProxyInstance(Clash.class.getClassLoader(),
                new Class[] { Interface1A.class, Interface1B.class },
                handler);
            System.err.println("Clash did not throw expected exception");
        } catch (IllegalArgumentException iae) {
            System.out.println("Clash threw expected exception");
        }
    }
}
interface Interface1A {
    public int thisIsOkay();
    public float thisIsTrouble();
}
interface Interface1B {
    public int thisIsOkay();
    public Object thisIsTrouble();
}
class ClashInvocationHandler implements InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        return null;
    }
}
