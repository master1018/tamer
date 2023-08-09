public final class ClassFinder {
    public static Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
            if (loader != null) {
                return Class.forName(name, false, loader);
            }
        } catch (ClassNotFoundException exception) {
        } catch (SecurityException exception) {
        }
        return Class.forName(name);
    }
    public static Class<?> findClass(String name, ClassLoader loader) throws ClassNotFoundException {
        if (loader != null) {
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException exception) {
            } catch (SecurityException exception) {
            }
        }
        return findClass(name);
    }
    public static Class<?> resolveClass(String name) throws ClassNotFoundException {
        return resolveClass(name, null);
    }
    public static Class<?> resolveClass(String name, ClassLoader loader) throws ClassNotFoundException {
        Class<?> type = PrimitiveTypeMap.getType(name);
        return (type == null)
                ? findClass(name, loader)
                : type;
    }
    private ClassFinder() {
    }
}
