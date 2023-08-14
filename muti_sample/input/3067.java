public class Desktop {
    public static enum Action {
        OPEN,
        EDIT,
        PRINT,
        MAIL,
        BROWSE
    };
    private DesktopPeer peer;
    private Desktop() {
        peer = Toolkit.getDefaultToolkit().createDesktopPeer(this);
    }
    public static synchronized Desktop getDesktop(){
        if (GraphicsEnvironment.isHeadless()) throw new HeadlessException();
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop API is not " +
                                                    "supported on the current platform");
        }
        sun.awt.AppContext context = sun.awt.AppContext.getAppContext();
        Desktop desktop = (Desktop)context.get(Desktop.class);
        if (desktop == null) {
            desktop = new Desktop();
            context.put(Desktop.class, desktop);
        }
        return desktop;
    }
    public static boolean isDesktopSupported(){
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        if (defaultToolkit instanceof SunToolkit) {
            return ((SunToolkit)defaultToolkit).isDesktopSupported();
        }
        return false;
    }
    public boolean isSupported(Action action) {
        return peer.isSupported(action);
    }
    private static void checkFileValidation(File file){
        if (file == null) throw new NullPointerException("File must not be null");
        if (!file.exists()) {
            throw new IllegalArgumentException("The file: "
                                               + file.getPath() + " doesn't exist.");
        }
        file.canRead();
    }
    private void checkActionSupport(Action actionType){
        if (!isSupported(actionType)) {
            throw new UnsupportedOperationException("The " + actionType.name()
                                                    + " action is not supported on the current platform!");
        }
    }
    private void checkAWTPermission(){
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AWTPermission(
                                   "showWindowWithoutWarningBanner"));
        }
    }
    public void open(File file) throws IOException {
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.OPEN);
        checkFileValidation(file);
        peer.open(file);
    }
    public void edit(File file) throws IOException {
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.EDIT);
        file.canWrite();
        checkFileValidation(file);
        peer.edit(file);
    }
    public void print(File file) throws IOException {
        checkExec();
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPrintJobAccess();
        }
        checkActionSupport(Action.PRINT);
        checkFileValidation(file);
        peer.print(file);
    }
    public void browse(URI uri) throws IOException {
        SecurityException securityException = null;
        try {
            checkAWTPermission();
            checkExec();
        } catch (SecurityException e) {
            securityException = e;
        }
        checkActionSupport(Action.BROWSE);
        if (uri == null) {
            throw new NullPointerException();
        }
        if (securityException == null) {
            peer.browse(uri);
            return;
        }
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to convert URI to URL", e);
        }
        sun.awt.DesktopBrowse db = sun.awt.DesktopBrowse.getInstance();
        if (db == null) {
            throw securityException;
        }
        db.browse(url);
    }
    public void mail() throws IOException {
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.MAIL);
        URI mailtoURI = null;
        try{
            mailtoURI = new URI("mailto:?");
            peer.mail(mailtoURI);
        } catch (URISyntaxException e){
        }
    }
    public  void mail(URI mailtoURI) throws IOException {
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.MAIL);
        if (mailtoURI == null) throw new NullPointerException();
        if (!"mailto".equalsIgnoreCase(mailtoURI.getScheme())) {
            throw new IllegalArgumentException("URI scheme is not \"mailto\"");
        }
        peer.mail(mailtoURI);
    }
    private void checkExec() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new FilePermission("<<ALL FILES>>",
                                                  SecurityConstants.FILE_EXECUTE_ACTION));
        }
    }
}
