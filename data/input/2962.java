public class bug6795356 {
    volatile static WeakReference<ProtectionDomain> weakRef;
    public static void main(String[] args) throws Exception {
        ProtectionDomain domain = new ProtectionDomain(null, null);
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                UIManager.getLookAndFeel();
                return null;
            }
        }, new AccessControlContext(new ProtectionDomain[]{domain}));
        weakRef = new WeakReference<ProtectionDomain>(domain);
        domain = null;
        Util.generateOOME();
        if (weakRef.get() != null) {
            throw new RuntimeException("Memory leak found!");
        }
        System.out.println("Test passed");
    }
}
