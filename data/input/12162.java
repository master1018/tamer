class UnloadEventTarg {
    static int loadersFinalized = 0;
    public static void main(String[] args) throws ClassNotFoundException {
        loadup("first");
        loadup("second");
        if (!forceUnload()) {
            System.err.println("Unable to force unload");
        }
        lastStop();
    }
    static void loadup(String id) throws ClassNotFoundException {
        ClassLoaderTarg cl = new ClassLoaderTarg(id);
        cl.findClass("Unload1Targ").getFields();
        cl.findClass("Unload2Targ").getFields();
    }
    static boolean forceUnload() {
        List holdAlot = new ArrayList();
        for (int chunk=10000000; chunk > 10000; chunk = chunk / 2) {
            if (loadersFinalized > 1) {
                return true;
            }
            try {
                while(true) {
                    holdAlot.add(new byte[chunk]);
                    System.err.println("Allocated " + chunk);
                }
            }
            catch ( Throwable thrown ) {  
                System.gc();
            }
            System.runFinalization();
        }
        return false;
    }
    static void classLoaderFinalized(String id) {
        System.err.println("finalizing ClassLoaderTarg - " + id);
        loadersFinalized++;
    }
    static void unloading1() {
        System.err.println("unloading Unload1Targ");
    }
    static void unloading2() {
        System.err.println("unloading Unload2Targ");
    }
    static void lastStop() {
        System.err.println("UnloadEventTarg exiting");
    }
}
