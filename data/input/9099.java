public class JarNoManifest {
  public static void main(String[] args) throws Exception {
    File f = new File(System.getProperty("test.src","."), "no-manifest.jar");
    JarFile jar = new JarFile(f);
    ZipEntry entry = jar.getEntry("JarNoManifest.java");
    InputStream in = jar.getInputStream(entry);
  }
}
