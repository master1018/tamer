public class B6449504 {
    public static void main (String[] args) throws Exception {
        boolean caching = args[0].equals ("caching");
        String dirname = System.getProperty ("test.classes");
        File f = new File (dirname);
        dirname = f.toURI().toString();
        String u = "jar:"+ dirname + "/bar.jar";
        URL url = new URL (u+"!/DoesNotExist.txt");
        System.out.println ("url = " + url);
        JarURLConnection j1 = (JarURLConnection)url.openConnection();
        URL url2 = new URL (u+"!/test.txt");
        System.out.println ("url2 = " + url2);
        JarURLConnection j2 = (JarURLConnection)url2.openConnection();
        j1.setUseCaches (caching);
        j2.setUseCaches (caching);
        j2.connect ();
        try {
            InputStream is = j1.getInputStream ();
        } catch (IOException e) {
            System.out.println ("Got expected exception from j1 ");
        }
        InputStream is = j2.getInputStream ();
        readAndClose (is);
        System.out.println ("OK");
    }
    static void readAndClose (InputStream is) throws IOException {
        while (is.read() != -1) ;
        is.close();
    }
}
