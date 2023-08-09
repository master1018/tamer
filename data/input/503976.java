class UnresTest1 {
    public static void run() {
        System.out.println("UnresTest1...");
        UnresStuff stuff = new UnresStuff();
        try {
            int x = stuff.instField;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {       
            int x = stuff.instField;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            stuff.instField = 5;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            double d = stuff.wideInstField;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            stuff.wideInstField = 0.0;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            int y = UnresStuff.staticField;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            UnresStuff.staticField = 17;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            double d = UnresStuff.wideStaticField;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            UnresStuff.wideStaticField = 1.0;
            assert(false);
        } catch (NoSuchFieldError nsfe) {
        }
        try {
            stuff.virtualMethod();
            assert(false);
        } catch (NoSuchMethodError nsfe) {
        }
        try {
            UnresStuff.staticMethod();
            assert(false);
        } catch (NoSuchMethodError nsfe) {
        }
    }
}
