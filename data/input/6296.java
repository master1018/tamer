public class B5105410 {
    public static void main (String[] args) throws Exception {
        URL url = new URL ("jar:file:./foo2.jar!/bar.txt");
        URLConnection urlc = url.openConnection ();
        urlc.setUseCaches (false);
        InputStream is = urlc.getInputStream();
        is.read();
        is.close();
        File file = new File ("foo2.jar");
        if (!file.delete ()) {
            throw new RuntimeException ("Could not delete foo2.jar");
        }
        if (file.exists()) {
            throw new RuntimeException ("foo2.jar still exists");
        }
    }
}
