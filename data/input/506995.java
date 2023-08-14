public class BasicTest {
    public static void main(String[] args) {
        Mix proxyMe = new Mix();
        Object proxy = createProxy(proxyMe);
        if (!Proxy.isProxyClass(proxy.getClass()))
            System.err.println("not a proxy class?");
        if (Proxy.getInvocationHandler(proxy) == null)
            System.err.println("ERROR: Proxy.getInvocationHandler is null");
        Shapes shapes = (Shapes) proxy;
        shapes.circle(3);
        shapes.rectangle(10, 20);
        shapes.blob();
        Quads quads = (Quads) proxy;
        quads.rectangle(15, 25);
        quads.trapezoid(6, 81.18, 4);
        Colors colors = (Colors) proxy;
        colors.red(1.0f);
        colors.blue(777);
        colors.mauve("sorry");
        colors.blob();
        try {
            shapes.upChuck();
            System.out.println("Didn't get expected exception");
        } catch (IndexOutOfBoundsException ioobe) {
            System.out.println("Got expected ioobe");
        }
        try {
            shapes.upCheck();
            System.out.println("Didn't get expected exception");
        } catch (InterruptedException ie) {
            System.out.println("Got expected ie");
        }
        System.out.println("");
        Method[] methods = proxy.getClass().getDeclaredMethods();
        System.out.println("Proxy methods: " + Arrays.deepToString(methods));
        Method meth = methods[methods.length -1];
        System.out.println("Decl annos: " + Arrays.deepToString(meth.getDeclaredAnnotations()));
        Annotation[][] paramAnnos = meth.getParameterAnnotations();
        System.out.println("Param annos (" + paramAnnos.length + ") : "
            + Arrays.deepToString(paramAnnos));
        Field[] fields = proxy.getClass().getDeclaredFields();
        System.out.println("Proxy fields: " + Arrays.deepToString(fields));
    }
    static Object createProxy(Object proxyMe) {
        InvocationHandler handler = new MyInvocationHandler(proxyMe);
        Class proxyClass = Proxy.getProxyClass(Shapes.class.getClassLoader(),
                            new Class[] { Quads.class, Colors.class });
        Object proxy = null;
        try {
            Constructor<Class> cons;
            cons = proxyClass.getConstructor(
                            new Class[] { InvocationHandler.class });
            proxy = cons.newInstance(new Object[] { handler });
        } catch (NoSuchMethodException nsme) {
            System.err.println("failed: " + nsme);
        } catch (InstantiationException ie) {
            System.err.println("failed: " + ie);
        } catch (IllegalAccessException ie) {
            System.err.println("failed: " + ie);
        } catch (InvocationTargetException ite) {
            System.err.println("failed: " + ite);
        }
        return proxy;
    }
}
interface Shapes {
    public void circle(int r);
    public int rectangle(int x, int y);
    public String blob();
    public R0base checkMe();
    public void upChuck();
    public void upCheck() throws InterruptedException;
}
interface Quads extends Shapes {
    public int rectangle(int x, int y);
    public int square(int x, int y);
    public int trapezoid(int x, double off, int y);
    public R0a checkMe();
}
interface Colors {
    public int red(float howRed);
    public int green(double howGreen);
    public double blue(int howBlue);
    public int mauve(String apology);
    public String blob();
    public R0aa checkMe();
}
class R0base { int mBlah;  }
class R0a extends R0base { int mBlah_a;  }
class R0aa extends R0a { int mBlah_aa;  }
class Mix implements Quads, Colors {
    public void circle(int r) {
        System.out.println("--- circle " + r);
    }
    public int rectangle(int x, int y) {
        System.out.println("--- rectangle " + x + "," + y);
        return 4;
    }
    public int square(int x, int y) {
        System.out.println("--- square " + x + "," + y);
        return 4;
    }
    public int trapezoid(int x, double off, int y) {
        System.out.println("--- trap " + x + "," + y + "," + off);
        return 8;
    }
    public String blob() {
        System.out.println("--- blob");
        return "mix";
    }
    public int red(float howRed) {
        System.out.println("--- red " + howRed);
        return 0;
    }
    public int green(double howGreen) {
        System.out.println("--- green " + howGreen);
        return 1;
    }
    public double blue(int howBlue) {
        System.out.println("--- blue " + howBlue);
        return 2.54;
    }
    public int mauve(String apology) {
        System.out.println("--- mauve " + apology);
        return 3;
    }
    public R0aa checkMe() {
        return null;
    }
    public void upChuck() {
        throw new IndexOutOfBoundsException("upchuck");
    }
    public void upCheck() throws InterruptedException {
        throw new InterruptedException("upcheck");
    }
}
class MyInvocationHandler implements InvocationHandler {
    Object mObj;
    public MyInvocationHandler(Object obj) {
        mObj = obj;
    }
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        Object result = null;
        if (method.getDeclaringClass() == java.lang.Object.class) {
            if (method.getName().equals("toString"))
                return super.toString();
            else if (method.getName().equals("hashCode"))
                return Integer.valueOf(super.hashCode());
            else if (method.getName().equals("equals"))
                return Boolean.valueOf(super.equals(args[0]));
            else
                throw new RuntimeException("huh?");
        }
        System.out.println("Invoke " + method);
        if (args == null || args.length == 0) {
            System.out.println(" (no args)");
        } else {
            for (int i = 0; i < args.length; i++)
                System.out.println(" " + i + ": " + args[i]);
        }
        try {
            if (true)
                result = method.invoke(mObj, args);
            else
                result = -1;
            System.out.println("Success: method " + method.getName()
                + " res=" + result);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
        return result;
    }
}
