public final class FontManagerFactory {
    private static FontManager instance = null;
    private static final String DEFAULT_CLASS;
    static {
        if (FontUtilities.isWindows)
            DEFAULT_CLASS = "sun.awt.Win32FontManager";
        else
            DEFAULT_CLASS = "sun.awt.X11FontManager";
    }
    public static synchronized FontManager getInstance() {
        if (instance != null) {
            return instance;
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    String fmClassName =
                            System.getProperty("sun.font.fontmanager",
                                               DEFAULT_CLASS);
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    Class fmClass = Class.forName(fmClassName, true, cl);
                    instance = (FontManager) fmClass.newInstance();
                } catch (ClassNotFoundException ex) {
                    InternalError err = new InternalError();
                    err.initCause(ex);
                    throw err;
                } catch (InstantiationException ex) {
                    InternalError err = new InternalError();
                    err.initCause(ex);
                    throw err;
                } catch (IllegalAccessException ex) {
                    InternalError err = new InternalError();
                    err.initCause(ex);
                    throw err;
                }
                return null;
            }
        });
        return instance;
    }
}
