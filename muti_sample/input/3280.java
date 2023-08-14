class Reflect {
    private Reflect() {}
    private static void setAccessible(final AccessibleObject ao) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    ao.setAccessible(true);
                    return null;
                }});
    }
    static Field lookupField(String className, String fieldName) {
        try {
            Class<?> cl = Class.forName(className);
            Field f = cl.getDeclaredField(fieldName);
            setAccessible(f);
            return f;
        } catch (ClassNotFoundException x) {
            throw new AssertionError(x);
        } catch (NoSuchFieldException x) {
            throw new AssertionError(x);
        }
    }
}
