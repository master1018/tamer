public class VerifySignedJar {
    private static void Unreached (Object o)
        throws Exception
    {
        throw new Exception ("Expected exception was not thrown");
    }
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."), "thawjar.jar");
        JarFile jf = new JarFile(f);
        try {
            for (Enumeration e = jf.entries(); e.hasMoreElements();)
                jf.getInputStream((ZipEntry) e.nextElement());
            ZipEntry ze = jf.getEntry("getprop.class");
            JarEntry je = jf.getJarEntry("getprop.class");
            try { Unreached (jf.getEntry(null)); }
            catch (NullPointerException e) {}
            try { Unreached (jf.getJarEntry(null)); }
            catch (NullPointerException e) {}
            try { Unreached (jf.getInputStream(null)); }
            catch (NullPointerException e) {}
        } catch (SecurityException se) {
            throw new Exception("Got SecurityException when verifying signed " +
                "jar:" + se);
        }
    }
}
