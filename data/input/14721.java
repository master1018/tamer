public class TurkCert {
    public static void main(String[] args) throws Exception{
        Locale.setDefault(new Locale("TR", "tr"));
        File f = new File(System.getProperty("test.src","."), "test.jar");
        JarFile jf = new JarFile(f, true);
        JarEntry je = (JarEntry)jf.getEntry("test.class");
        InputStream is = jf.getInputStream(je);
        byte[] b = new byte[1024];
        while (is.read(b) != -1) {
        }
        if (je.getCertificates() == null)
            throw new Exception("Null certificate for test.class.");
    }
}
