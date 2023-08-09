public class B4756443 {
    public static void main(String [] args) throws IOException
    {
        String testsrc = System.getProperty ("test.src");
        URL u;
        if (testsrc == null || "".equals (testsrc)) {
            u = new URL ("jar:file:./foo.jar!/a/b/test.xml");
        } else {
            File f = new File(testsrc + File.separator
                            + "foo.jar");
            String s = f.toURL().toString();
            u = new URL ("jar:" + s + "!/a/b/test.xml");
        }
        System.out.println ("Testing with: " + u);
        URLConnection con = u.openConnection ();
        con.setUseCaches (false);
        con.connect ();
        long l = con.getLastModified ();
        URLConnection con1 = u.openConnection ();
        con1.setUseCaches (true);
        con1.connect ();
        long l1 = con1.getLastModified ();
        if (l != l1) {
            throw new RuntimeException ("l != l1 ("+l+"/"+l1+")");
        }
    }
}
