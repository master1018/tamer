public class PackageVersionTest {
    private static final File  javaHome = new File(System.getProperty("java.home"));
    public final static int JAVA5_PACKAGE_MAJOR_VERSION = 150;
    public final static int JAVA5_PACKAGE_MINOR_VERSION = 7;
    public final static int JAVA6_PACKAGE_MAJOR_VERSION = 160;
    public final static int JAVA6_PACKAGE_MINOR_VERSION = 1;
    public static void main(String... args) {
        if (!javaHome.getName().endsWith("jre")) {
            throw new RuntimeException("Error: requires an SDK to run");
        }
        File out = new File("test.pack");
        createClassFile("Test5");
        createClassFile("Test6");
        createClassFile("Test7");
        verify6991164();
        verifyPack("Test5.class", JAVA5_PACKAGE_MAJOR_VERSION,
                JAVA5_PACKAGE_MINOR_VERSION);
        verifyPack("Test6.class", JAVA6_PACKAGE_MAJOR_VERSION,
                JAVA6_PACKAGE_MINOR_VERSION);
        verifyPack("Test7.class", JAVA6_PACKAGE_MAJOR_VERSION,
                JAVA6_PACKAGE_MINOR_VERSION);
        verifyPack("Test6.java", JAVA5_PACKAGE_MAJOR_VERSION,
                JAVA5_PACKAGE_MINOR_VERSION);
    }
    static void verify6991164() {
        Unpacker unpacker = Pack200.newUnpacker();
        String versionStr = unpacker.toString();
        String expected = "Pack200, Vendor: " +
                System.getProperty("java.vendor") + ", Version: " +
                JAVA6_PACKAGE_MAJOR_VERSION + "." + JAVA6_PACKAGE_MINOR_VERSION;
        if (!versionStr.equals(expected)) {
            System.out.println("Expected: " + expected);
            System.out.println("Obtained: " + versionStr);
            throw new RuntimeException("did not get expected string " + expected);
        }
    }
    static void createClassFile(String name) {
        createJavaFile(name);
        String target = name.substring(name.length() - 1);
        String javacCmds[] = {
            "-source",
            "5",
            "-target",
            name.substring(name.length() - 1),
            name + ".java"
        };
        Utils.compiler(javacCmds);
    }
    static void createJavaFile(String name) {
        PrintStream ps = null;
        FileOutputStream fos = null;
        File outputFile = new File(name + ".java");
        outputFile.delete();
        try {
            fos = new FileOutputStream(outputFile);
            ps = new PrintStream(fos);
            ps.format("public class %s {}", name);
        } catch (IOException ioe) {
            throw new RuntimeException("creation of test file failed");
        } finally {
            Utils.close(ps);
            Utils.close(fos);
        }
    }
    static void verifyPack(String filename, int expected_major, int expected_minor) {
        File jarFileName = new File("test.jar");
        jarFileName.delete();
        String jargs[] = {
            "cvf",
            jarFileName.getName(),
            filename
        };
        Utils.jar(jargs);
        JarFile jfin = null;
        try {
            jfin = new JarFile(jarFileName);
            Packer packer = Pack200.newPacker();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            packer.pack(jfin, baos);
            baos.flush();
            baos.close();
            byte[] buf = baos.toByteArray();
            int minor = buf[4] & 0x000000ff;
            int major = buf[5] & 0x000000ff;
            if (major != expected_major || minor != expected_minor) {
                String msg =
                        String.format("test fails: expected:%d.%d but got %d.%d\n",
                        expected_major, expected_minor,
                        major, minor);
                throw new Error(msg);
            }
            System.out.println(filename + ": OK");
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } finally {
            Utils.close((Closeable) jfin);
        }
    }
}
