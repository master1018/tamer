public class Test4431684 {
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."),
                          "JavaApplication1.jar");
        JarFile jf = new JarFile(f);
        Enumeration entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = (JarEntry)entries.nextElement();
            if(je.getName().endsWith("class")) {
                byte[] buffer = new byte[8192];
                InputStream is = jf.getInputStream(je);
                int n;
                while ((n = is.read(buffer, 0, buffer.length)) != -1) {
                }
                is.close();
                if(je.getCodeSigners() == null) {
                    throw new RuntimeException("FAIL: Cannot get code signers");
                }
            }
        }
    }
}
