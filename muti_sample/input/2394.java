public class WDesktopPeer implements DesktopPeer {
    private static String ACTION_OPEN_VERB = "open";
    private static String ACTION_EDIT_VERB = "edit";
    private static String ACTION_PRINT_VERB = "print";
    public boolean isSupported(Action action) {
        return true;
    }
    public void open(File file) throws IOException {
        this.ShellExecute(file.toURI(), ACTION_OPEN_VERB);
    }
    public void edit(File file) throws IOException {
        this.ShellExecute(file.toURI(), ACTION_EDIT_VERB);
    }
    public void print(File file) throws IOException {
        this.ShellExecute(file.toURI(), ACTION_PRINT_VERB);
    }
    public void mail(URI uri) throws IOException {
        this.ShellExecute(uri, ACTION_OPEN_VERB);
    }
    public void browse(URI uri) throws IOException {
        this.ShellExecute(uri, ACTION_OPEN_VERB);
    }
    private void ShellExecute(URI uri, String verb) throws IOException {
        String errmsg = ShellExecute(uri.toString(), verb);
        if (errmsg != null) {
            throw new IOException("Failed to " + verb + " " + uri
                    + ". Error message: " + errmsg);
        }
    }
    private static native String ShellExecute(String uri, String verb);
}
