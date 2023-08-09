public class SorryClosed {
    public static void main(String args[]) throws IOException {
        File file = new File(System.getProperty("test.src","."), "test.jar");
        String testEntryName = "test.class";
        try {
            JarFile f = new JarFile(file);
            ZipEntry e = f.getEntry(testEntryName);
            f.close();
            f.getInputStream(e);
        } catch (IllegalStateException e) {} 
        try {
            JarFile f = new JarFile(file);
            f.close();
            f.getEntry(testEntryName);
        } catch (IllegalStateException e) {} 
        try {
            JarFile f = new JarFile(file);
            f.close();
            f.getJarEntry(testEntryName);
        } catch (IllegalStateException e) {} 
        try {
            JarFile f = new JarFile(file);
            f.close();
            f.getManifest();
        } catch (IllegalStateException e) {} 
    }
}
