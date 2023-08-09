class UnresTest2 {
    static boolean checkCasts(Object obj) {
        boolean foo = false;
        try {
            UnresClass un = (UnresClass) obj;
            assert(false);
        } catch (NoClassDefFoundError ncdfe) {
        }
        try {
            foo = obj instanceof UnresClass;
            assert(false);
        } catch (NoClassDefFoundError ncdfe) {
        }
        return foo;
    }
    public static void run() {
        System.out.println("UnresTest2...");
        UnresClass un;
        UnresStuff stuff = new UnresStuff();
        try {
            un = new UnresClass();
            assert(false);
        } catch (NoClassDefFoundError ncdfe) {
        }
        try {
            UnresClass[] uar = new UnresClass[3];
            assert(false);
        } catch (NoClassDefFoundError ncdfe) {
        }
        checkCasts(stuff);
        System.out.println("UnresTest2 done");
    }
}
