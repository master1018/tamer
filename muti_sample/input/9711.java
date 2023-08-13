public class T7007157 {
    public static void main(String... args) throws IOException {
        File sdkHome = Utils.JavaSDK;
        File testJar = new File(new File(sdkHome, "lib"), "tools.jar");
        JarFile jarFile = new JarFile(testJar);
        File packFile = new File("foo.pack");
        Pack200.Packer packer = Pack200.newPacker();
        Map<String, String> p = packer.properties();
        p.put(packer.EFFORT, "1");  
        p.put(packer.SEGMENT_LIMIT, "10000");
        p.put(packer.DEFLATE_HINT, packer.FALSE);
        p.put(packer.KEEP_FILE_ORDER, packer.TRUE);
        p.put(packer.CODE_ATTRIBUTE_PFX + "StackMapTable", packer.STRIP);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(packFile);
            packer.pack(jarFile, fos);
        } finally {
            Utils.close(fos);
            Utils.close(jarFile);
        }
    }
}
