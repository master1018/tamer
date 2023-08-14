public class TestProvider2 extends TestProvider {
    public static final Class loadClassReturn =
        (new Object() { }).getClass();
    public static final Class loadProxyClassReturn =
        (new Object() { }).getClass();
    public static final ClassLoader getClassLoaderReturn =
        URLClassLoader.newInstance(new URL[0]);
    public static final String getClassAnnotationReturn = new String();
    public static List invocations =
        Collections.synchronizedList(new ArrayList(1));
    public TestProvider2() {
        System.err.println("TestProvider2()");
    }
    public Class loadClass(String codebase, String name,
                           ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        invocations.add(new Invocation(loadClassMethod,
            new Object[] { codebase, name, defaultLoader }));
        return loadClassReturn;
    }
    public Class loadProxyClass(String codebase, String[] interfaces,
                                ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        invocations.add(new Invocation(loadProxyClassMethod,
            new Object[] { codebase, interfaces, defaultLoader }));
        return loadProxyClassReturn;
    }
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException
    {
        invocations.add(new Invocation(
            getClassLoaderMethod, new Object[] { codebase }));
        return getClassLoaderReturn;
    }
    public String getClassAnnotation(Class<?> cl) {
        invocations.add(new Invocation(
            getClassAnnotationMethod, new Object[] { cl }));
        return getClassAnnotationReturn;
    }
}
