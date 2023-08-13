public class ResourcesMgr {
    private static java.util.ResourceBundle bundle;
    private static java.util.ResourceBundle altBundle;
    public static String getString(String s) {
        if (bundle == null) {
            bundle = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<java.util.ResourceBundle>() {
                public java.util.ResourceBundle run() {
                    return (java.util.ResourceBundle.getBundle
                                ("sun.security.util.Resources"));
                }
            });
        }
        return bundle.getString(s);
    }
    public static String getString(String s, final String altBundleName) {
        if (altBundle == null) {
            altBundle = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<java.util.ResourceBundle>() {
                public java.util.ResourceBundle run() {
                    return (java.util.ResourceBundle.getBundle(altBundleName));
                }
            });
        }
        return altBundle.getString(s);
    }
}
