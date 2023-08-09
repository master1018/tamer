public class ORBClassLoader
{
    public static Class loadClass(String className)
        throws ClassNotFoundException
    {
        return ORBClassLoader.getClassLoader().loadClass(className);
    }
    public static ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null)
            return Thread.currentThread().getContextClassLoader();
        else
            return ClassLoader.getSystemClassLoader();
    }
}
