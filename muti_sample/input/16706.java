public abstract class ProviderFactory {
    protected ProviderFactory() {}
    public abstract <T extends Provider> T createProvider(Class<T> cls);
    public static ProviderFactory getDefaultFactory() {
        HashSet<ProviderFactory> factories = new HashSet<ProviderFactory>();
        String prop = AccessController.doPrivileged(
            new GetPropertyAction("com.sun.tracing.dtrace"));
        if ( (prop == null || !prop.equals("disable")) &&
             DTraceProviderFactory.isSupported() ) {
            factories.add(new DTraceProviderFactory());
        }
        prop = AccessController.doPrivileged(
            new GetPropertyAction("sun.tracing.stream"));
        if (prop != null) {
            for (String spec : prop.split(",")) {
                PrintStream ps = getPrintStreamFromSpec(spec);
                if (ps != null) {
                    factories.add(new PrintStreamProviderFactory(ps));
                }
            }
        }
        if (factories.size() == 0) {
            return new NullProviderFactory();
        } else if (factories.size() == 1) {
            return factories.toArray(new ProviderFactory[1])[0];
        } else {
            return new MultiplexProviderFactory(factories);
        }
    }
    private static PrintStream getPrintStreamFromSpec(final String spec) {
        try {
            final int fieldpos = spec.lastIndexOf('.');
            final Class<?> cls = Class.forName(spec.substring(0, fieldpos));
            Field f = AccessController.doPrivileged(new PrivilegedExceptionAction<Field>() {
                public Field run() throws NoSuchFieldException {
                    return cls.getField(spec.substring(fieldpos + 1));
                }
            });
            return (PrintStream)f.get(null);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (PrivilegedActionException e) {
            throw new AssertionError(e);
        }
    }
}
