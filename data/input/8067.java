public class CachePermissionsTest {
    public static void main(String[] args) {
        boolean isFileCacheExpected =
            Boolean.valueOf(args[0]).booleanValue();
        System.out.println("Is file cache expected: " + isFileCacheExpected);
        ImageIO.setUseCache(true);
        System.out.println("java.io.tmpdir is " + System.getProperty("java.io.tmpdir"));
        if (args.length > 1) {
            String testsrc = System.getProperty("test.src", ".");
            String policy = testsrc + File.separator + args[1];
            System.out.println("Policy file: " + policy);
            System.setProperty("java.security.policy", policy);
            System.out.println("Install security manager...");
            System.setSecurityManager(new SecurityManager());
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            boolean isFileCache = ios.isCachedFile();
            System.out.println("Is file cache used: " + isFileCache);
            if (isFileCache !=isFileCacheExpected) {
                System.out.println("WARNING: file chace usage is not as expected!");
            }
            System.out.println("Verify data writing...");
            for (int i = 0; i < 8192; i++) {
                ios.writeInt(i);
            }
            System.out.println("Verify data reading...");
            ios.seek(0L);
            for (int i = 0; i < 8192; i++) {
                int j = ios.readInt();
                if (i != j) {
                    throw new RuntimeException("Wrong data in the stream " + j + " instead of " + i);
                }
            }
            System.out.println("Verify stream closing...");
            ios.close();
        } catch (IOException e) {
            throw new RuntimeException("Test FAILED.", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Test FAILED.", e);
        }
    }
}
