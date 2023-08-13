public class LoadLibraryAction implements java.security.PrivilegedAction<Void> {
    private String theLib;
    public LoadLibraryAction(String theLib) {
        this.theLib = theLib;
    }
    public Void run() {
        System.loadLibrary(theLib);
        return null;
    }
}
