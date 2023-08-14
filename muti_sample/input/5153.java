public class ServiceConfiguration {
    public static void installServiceConfigurationFile() {
        String filename = "java.rmi.server.RMIClassLoaderSpi";
        File dstDir = new File(TestLibrary.getProperty("test.classes", "."),
                               "META-INF/services");
        if (!dstDir.exists()) {
            if (!dstDir.mkdirs()) {
                throw new RuntimeException(
                    "could not create META-INF/services directory " + dstDir);
            }
        }
        File dstFile = new File(dstDir, filename);
        File srcDir = new File(TestLibrary.getProperty("test.src", "."));
        File srcFile = new File(srcDir, filename);
        try {
            TestLibrary.copyFile(srcFile, dstFile);
        } catch (IOException e) {
            throw new RuntimeException("could not install " + dstFile, e);
        }
    }
}
