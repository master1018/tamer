public class DefaultLoaderRepository {
    public static Class<?> loadClass(String className)
        throws ClassNotFoundException {
        return javax.management.loading.DefaultLoaderRepository.loadClass(className);
    }
    public static Class<?> loadClassWithout(ClassLoader loader,String className)
        throws ClassNotFoundException {
        return javax.management.loading.DefaultLoaderRepository.loadClassWithout(loader, className);
    }
 }
