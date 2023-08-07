public class SigoaObject extends ID {
    private static final long serialVersionUID = 1L;
    public SigoaObject(final SigoaObjectGroup parent, final String simpleName, final IID resolve) {
        super((parent != null) ? parent : SigoaObjectGroup.OPTIMIZATION_ROOT, simpleName, resolve);
    }
    public SigoaObject(final SigoaObjectGroup parent, final String simpleName) {
        this(parent, simpleName, null);
    }
    protected static final String processName(final String name, final Class<?> clazz) {
        return SigoaObjectGroup.processName(name, clazz);
    }
    protected static final IHost currentHost() {
        final Thread t;
        t = Thread.currentThread();
        if (t instanceof IHost) {
            return ((IHost) t);
        }
        return DummyHost.INSTANCE;
    }
    protected static final IRandomizer currentRandomizer() {
        return currentHost().getRandomizer();
    }
}
