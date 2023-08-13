public abstract class SurfaceManagerFactory {
    private static SurfaceManagerFactory instance;
    public synchronized static SurfaceManagerFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("No SurfaceManagerFactory set.");
        }
        return instance;
    }
    public synchronized static void setInstance(SurfaceManagerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must be non-null");
        }
        if (instance != null) {
            throw new IllegalStateException("The surface manager factory is already initialized");
        }
        instance = factory;
    }
     public abstract VolatileSurfaceManager
         createVolatileManager(SunVolatileImage image, Object context);
}
