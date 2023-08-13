public abstract class LangAccess {
    private static LangAccess theInstance = null;
    public static void setInstance(LangAccess instance) {
        if (theInstance != null) {
            throw new UnsupportedOperationException("already initialized");
        }
        theInstance = instance;
    }
    public static LangAccess getInstance() {
        ClassLoader calling = VMStack.getCallingClassLoader2();
        ClassLoader current = LangAccess.class.getClassLoader();
        if ((calling != null) && (calling != current)) {
            throw new SecurityException("LangAccess access denied");
        }
        if (theInstance == null) {
            throw new UnsupportedOperationException("not yet initialized");
        }
        return theInstance;
    }
    public abstract <T> T[] getEnumValuesInOrder(Class<T> clazz);
    public abstract void unpark(Thread thread);
    public abstract void parkFor(long nanos);
    public abstract void parkUntil(long time);
}
