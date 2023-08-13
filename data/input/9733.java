public class DelegatingLoader extends URLClassLoader {
    private DelegatingLoader delLoader;
    private String[] delClasses;
    static {
        boolean supportParallel = false;
        try {
            Class c = Class.forName("java.lang.ClassLoader");
            Method m = c.getDeclaredMethod("registerAsParallelCapable",
                    new Class[0]);
            m.setAccessible(true);
            Object result = (Boolean) m.invoke(null);
            if (result instanceof Boolean) {
                supportParallel = ((Boolean) result).booleanValue();
            } else {
                System.out.println("Error: ClassLoader.registerAsParallelCapable() did not return a boolean!");
                System.exit(1);
            }
        } catch (NoSuchMethodException nsme) {
            System.out.println("No ClassLoader.registerAsParallelCapable() API");
        } catch (NoSuchMethodError nsme2) {
            System.out.println("No ClassLoader.registerAsParallelCapable() API");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println("Parallel ClassLoader registration: " +
                    supportParallel);
    }
    public DelegatingLoader(URL urls[]) {
        super(urls);
        System.out.println("DelegatingLoader using URL " + urls[0]);
    }
    public void setDelegate(String[] delClasses, DelegatingLoader delLoader) {
        this.delClasses = delClasses;
        this.delLoader = delLoader;
    }
    public Class loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
        for (int i = 0; i < delClasses.length; i++) {
            if (delClasses[i].equals(className)) {
                Starter.log("Delegating class loading for " + className);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    return null;
                }
                return delLoader.loadClass(className, resolve);
            }
        }
        Starter.log("Loading local class " + className);
            return super.loadClass(className, resolve);
    }
}
