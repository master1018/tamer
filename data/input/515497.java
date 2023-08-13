class NewInstance {
    static Object newInstance (ClassLoader classLoader, String className)
        throws ClassNotFoundException, IllegalAccessException,
            InstantiationException
    {
        Class driverClass;
        if (classLoader == null) {
            driverClass = Class.forName(className);
        } else {
            driverClass = classLoader.loadClass(className);
        }
        return driverClass.newInstance();
    }
    static ClassLoader getClassLoader ()
    {
        Method m = null;
        try {
            m = Thread.class.getMethod("getContextClassLoader");
        } catch (NoSuchMethodException e) {
            return NewInstance.class.getClassLoader();
        }
        try {
            return (ClassLoader) m.invoke(Thread.currentThread());
        } catch (IllegalAccessException e) {
            throw new UnknownError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new UnknownError(e.getMessage());
        }
    }
}
