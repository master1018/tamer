public class SignedJarFileGetInputStream {
    public static void main(String args[]) throws Throwable {
        JarFile jar = new JarFile(
            new File(System.getProperty("test.src", "."), "Signed.jar"));
        for (Enumeration e = jar.entries(); e.hasMoreElements();) {
            JarEntry entry = (JarEntry) e.nextElement();
            InputStream is = jar.getInputStream(new ZipEntry(entry.getName()));
            is.close();
        }
    }
}
