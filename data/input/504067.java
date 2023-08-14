class MappedByteBufferFactory {
    static final Constructor<?> constructor;
    static {
        constructor = AccessController
                .doPrivileged(new PrivilegedAction<Constructor<?>>() {
                    public Constructor<?> run() {
                        try {
                            Class<?> wrapperClazz = ClassLoader
                                    .getSystemClassLoader().loadClass(
                                            "java.nio.MappedByteBufferAdapter"); 
                            Constructor<?> result = wrapperClazz
                                    .getConstructor(new Class[] {
                                            PlatformAddress.class, int.class,
                                            int.class, int.class });
                            result.setAccessible(true);
                            return result;
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }
    static MappedByteBuffer getBuffer(PlatformAddress addr, int mapmode,
            long size, int offset) throws Exception {
        return (MappedByteBuffer) constructor.newInstance(new Object[] { addr,
                new Integer((int) size), new Integer(offset),
                new Integer(mapmode) });
    }
}
