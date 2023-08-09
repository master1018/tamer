public class FontManagerNativeLibrary {
    static {
        java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
            public Object run() {
               System.loadLibrary("awt");
               if (FontUtilities.isOpenJDK &&
                   System.getProperty("os.name").startsWith("Windows")) {
                   System.loadLibrary("freetype");
               }
               System.loadLibrary("fontmanager");
               return null;
            }
        });
    }
    public static void load() {}
}
