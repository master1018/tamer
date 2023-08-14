public class ExtDirsA {
    public void go() {
        java.security.AccessController.doPrivileged
                (new java.security.PrivilegedAction() {
                public Object run() {
                    System.out.println
                        ("user.name = " + System.getProperty("user.name"));
                    ExtDirsB b = new ExtDirsB();
                    b.go();
                    return null;
                }
        });
    }
}
