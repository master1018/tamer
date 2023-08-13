public class Test4809008 {
    public static void main(String[] args) throws Exception {
        printMemory("Start Memory");
        int introspected = 200;
        for (int i = 0; i < introspected; i++) {
            ClassLoader cl = new SimpleClassLoader();
            Class type = cl.loadClass("Bean");
            type.newInstance();
            BeanInfo info = Introspector.getBeanInfo(type);
            cl = null;
            type = null;
            info = null;
            System.gc();
        }
        System.runFinalization();
        printMemory("End Memory");
        int finalized = SimpleClassLoader.numFinalizers;
        System.out.println(introspected + " classes introspected");
        System.out.println(finalized + " classes finalized");
        if (finalized < (introspected >> 1)) {
            throw new Error("ClassLoaders not finalized: " + finalized);
        }
    }
    private static void printMemory(String message) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        System.out.println(message);
        System.out.println("\tfree:  " + free);
        System.out.println("\ttotal: " + total);
    }
}
