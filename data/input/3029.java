public class UnpackerMemoryTest {
    private static void createPackFile(File packFile) throws IOException {
        File tFile = new File("test.dat");
        FileOutputStream fos = null;
        PrintStream ps = null;
        String jarFileName = Utils.baseName(packFile, Utils.PACK_FILE_EXT)
                + Utils.JAR_FILE_EXT;
        JarFile jarFile = null;
        try {
            fos = new FileOutputStream(tFile);
            ps = new PrintStream(fos);
            ps.println("A quick brown fox");
            Utils.jar("cvf", jarFileName, tFile.getName());
            jarFile = new JarFile(jarFileName);
            Utils.pack(jarFile, packFile);
        } finally {
            Utils.close(ps);
            tFile.delete();
            Utils.close(jarFile);
        }
    }
    public static void main(String[] args) throws Exception {
        String name = "foo";
        File packFile = new File(name + Utils.PACK_FILE_EXT);
        createPackFile(packFile);
        if (!packFile.exists()) {
           throw new RuntimeException(packFile + " not found");
        }
        File jarOut = new File(name + ".out");
        for (int i = 0; i < 2000; i++) {
            JarOutputStream jarOS = null;
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(jarOut);
                jarOS = new JarOutputStream(fos);
                System.out.println("Unpacking[" + i + "]" + packFile);
                Utils.unpackn(packFile, jarOS);
            }  finally {
                Utils.close(jarOS);
                Utils.close(fos);
            }
        }
    }
}
