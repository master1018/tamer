public class ReferencesTest {
    private static final int CLASS_LOADER_COUNT = 20;
    private static ClassLoader[] loaders = new ClassLoader[CLASS_LOADER_COUNT];
    private static WeakReference[] weakLoaders = new WeakReference[CLASS_LOADER_COUNT];
    public static void main(String[] args) throws Exception {
        URL testDirectory = new File(System.getProperty("test.classes", ".")).toURL();
        for (int i = 0; i < loaders.length; i++) {
            URL[] urls = { testDirectory };
            loaders[i] = new URLClassLoader(urls);
            weakLoaders[i] = new WeakReference(loaders[i]);
        }
        loadBundles(0, CLASS_LOADER_COUNT / 2);
        report("After loading resource bundles for first half of class loaders: ");
        for (int i = 0; i < CLASS_LOADER_COUNT / 2; i++) {
            loaders[i] = null;
        }
        System.gc();
        report("After releasing first half of class loaders: ");
        loadBundles(CLASS_LOADER_COUNT / 2, CLASS_LOADER_COUNT);
        report("After loading resource bundles for second half of class loaders: ");
        for (int i = CLASS_LOADER_COUNT / 2; i < CLASS_LOADER_COUNT; i++) {
            loaders[i] = null;
        }
        System.gc();
        report("After releasing second half of class loaders: ");
        if (countLoaders(0, CLASS_LOADER_COUNT / 2) > 0) {
            throw new RuntimeException("Too many class loaders not reclaimed yet.");
        }
    }
    private static void report(String when) throws Exception {
        int first = countLoaders(0, CLASS_LOADER_COUNT / 2);
        int second = countLoaders(CLASS_LOADER_COUNT / 2, CLASS_LOADER_COUNT);
        Class clazz = ResourceBundle.class;
        Field cacheList = clazz.getDeclaredField("cacheList");
        cacheList.setAccessible(true);
        int cacheSize = ((Map)cacheList.get(clazz)).size();
        System.out.println(when);
        System.out.println("    " + first + " loaders alive in first half");
        System.out.println("    " + second + " loaders alive in second half");
        System.out.println("    " + cacheSize + " entries in resource bundle cache");
    }
    private static void loadBundles(int start, int end) throws Exception {
        for (int i = start; i < end; i++) {
            try {
                ResourceBundle.getBundle("NonExistantBundle", Locale.US, loaders[i]);
            } catch (MissingResourceException e) {
            }
            ResourceBundle.getBundle("ReferencesTestBundle", Locale.US, loaders[i]);
        }
    }
    private static int countLoaders(int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if (weakLoaders[i].get() != null) {
                count++;
            }
        }
        return count;
    }
}
