public class Starter implements Runnable {
    private String id;
    private DelegatingLoader dl;
    private String startClass;
    private static DelegatingLoader saLoader, sbLoader;
    public static void log(String line) {
        System.out.println(line);
    }
    public static void main(String[] args) {
        URL[] urlsa = new URL[1];
        URL[] urlsb = new URL[1];
        try {
            String testDir = System.getProperty("test.classes", ".");
            String sep = System.getProperty("file.separator");
            urlsa[0] = new URL("file:
            urlsb[0] = new URL("file:
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        saLoader = new DelegatingLoader(urlsa);
        sbLoader = new DelegatingLoader(urlsb);
        String[] saClasses = { "comSA.SupBob", "comSA.Alice" };
        String[] sbClasses = { "comSB.SupAlice", "comSB.Bob" };
        saLoader.setDelegate(sbClasses, sbLoader);
        sbLoader.setDelegate(saClasses, saLoader);
        String testType = args[0];
        if (testType.equals("one-way")) {
            test("comSA.Alice", "comSA.SupBob");
        } else if (testType.equals("cross")) {
            test("comSA.Alice", "comSB.Bob");
        } else {
            System.out.println("ERROR: unsupported - " + testType);
        }
    }
    private static void test(String clsForSA, String clsForSB) {
        Starter ia = new Starter("SA", saLoader, clsForSA);
        Starter ib = new Starter("SB", sbLoader, clsForSB);
        new Thread(ia).start();
        new Thread(ib).start();
    }
    public static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            log("Thread interrupted");
        }
    }
    private Starter(String id, DelegatingLoader dl, String startClass) {
        this.id = id;
        this.dl = dl;
        this.startClass = startClass;
    }
    public void run() {
        log("Spawned thread " + id + " running");
        try {
            Class.forName(startClass, true, dl);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        log("Thread " + id + " terminating");
    }
}
