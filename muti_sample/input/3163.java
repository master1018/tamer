public class ClassUnloadTest {
    static Instrumentation ins;
    public static void main(String args[]) throws Exception {
        String dir = args[0] + File.separator;
        String jar = dir + args[1];
        System.out.println(jar);
        URL u = (new File(dir)).toURL();
        URL urls[] = { u };
        Invoker i1 = new Invoker(urls, "Foo", "doSomething");
        Boolean result = (Boolean)i1.invoke((Object)null);
        if (result.booleanValue()) {
            throw new RuntimeException("Test configuration error - doSomething should not succeed");
        }
        ins.appendToSystemClassLoaderSearch( new JarFile(jar) );
        result = (Boolean)i1.invoke((Object)null);
        if (result.booleanValue()) {
            throw new RuntimeException("Test configuration error - doSomething should not succeed");
        }
        Invoker i2 = new Invoker(urls, "Foo", "doSomething");
        result = (Boolean)i2.invoke((Object)null);
        if (!result.booleanValue()) {
            throw new RuntimeException("Test configuration error - doSomething did not succeed");
        }
        i1 = i2 = null;
        System.gc();
    }
    static class Invoker {
        URLClassLoader cl;
        Method m;
        public Invoker(URL urls[], String cn, String mn, Class ... params)
            throws ClassNotFoundException, NoSuchMethodException
        {
            cl = new URLClassLoader(urls);
            Class c = Class.forName("Foo", true, cl);
            m = c.getDeclaredMethod(mn, params);
        }
        public Object invoke(Object ... args)
            throws IllegalAccessException, InvocationTargetException
        {
            return m.invoke(args);
        }
    }
    public static void premain(String args, Instrumentation i) {
        ins = i;
    }
}
