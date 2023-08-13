public class ExtDirsChange {
    public static void main(String args[]) throws Exception {
        System.out.println("java.ext.dirs: " +
            System.getProperty("java.ext.dirs"));
        try {
            ExtDirsA a = new ExtDirsA();
            a.go();
            throw new Exception("Test Failed (Setup problem)");
        } catch (SecurityException se) {
            System.out.println("Setup OK");
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                System.setProperty("java.ext.dirs",
                    "ExtDirsA" + File.pathSeparator + "ExtDirsB");
                System.out.println("java.ext.dirs: " +
                    System.getProperty("java.ext.dirs"));
                return null;
            }
        });
        try {
            ExtDirsA a = new ExtDirsA();
            a.go();
            throw new Exception("Test Failed (Setup before refresh problem)");
        } catch (SecurityException se) {
            System.out.println("Setup before refresh OK");
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Policy.getPolicy().refresh();
                return null;
            }
        });
        try {
            ExtDirsA a = new ExtDirsA();
            a.go();
            System.out.println("Test Succeeded");
        } catch (SecurityException se) {
            se.printStackTrace();
            System.out.println("Test Failed");
            throw se;
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                System.setProperty("java.ext.dirs", " ");
                System.out.println("java.ext.dirs: " +
                    System.getProperty("java.ext.dirs"));
                Policy.getPolicy().refresh();
                return null;
            }
        });
        try {
            ExtDirsA a = new ExtDirsA();
            a.go();
            throw new Exception("Blank Test Failed");
        } catch (SecurityException se) {
            System.out.println("Blank Test OK");
        }
    }
}
