public class UriImportExport {
    static final PrintStream log = System.out;
    static int failures = 0;
    static void testPath(String s) {
        Path path = Paths.get(s);
        log.println(path);
        URI uri = path.toUri();
        log.println("  --> " + uri);
        Path result = Paths.get(uri);
        log.println("  --> " + result);
        if (!result.equals(path.toAbsolutePath())) {
            log.println("FAIL: Expected " + path + ", got " + result);
            failures++;
        }
        log.println();
    }
    static void testPath(String s, String expectedUri) {
        Path path = Paths.get(s);
        log.println(path);
        URI uri = path.toUri();
        log.println("  --> " + uri);
        if (!uri.toString().equals(expectedUri)) {
            log.println("FAILED: Expected " + expectedUri + ", got " + uri);
            failures++;
            return;
        }
        Path result = Paths.get(uri);
        log.println("  --> " + result);
        if (!result.equals(path.toAbsolutePath())) {
            log.println("FAIL: Expected " + path + ", got " + result);
            failures++;
        }
        log.println();
    }
    static void testUri(String s) throws Exception {
        URI uri = URI.create(s);
        log.println(uri);
        Path path = Paths.get(uri);
        log.println("  --> " + path);
        URI result = path.toUri();
        log.println("  --> " + result);
        if (!result.equals(uri)) {
            log.println("FAIL: Expected " + uri + ", got " + result);
            failures++;
        }
        log.println();
    }
    static void testBadUri(String s) throws Exception {
        URI uri = URI.create(s);
        log.println(uri);
        try {
            Path path = Paths.get(uri);
            log.format(" --> %s  FAIL: Expected IllegalArgumentException\n", path);
            failures++;
        } catch (IllegalArgumentException expected) {
            log.println("  --> IllegalArgumentException (expected)");
        }
        log.println();
    }
    public static void main(String[] args) throws Exception {
        testBadUri("file:foo");
        testBadUri("file:/foo?q");
        testBadUri("file:/foo#f");
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            testPath("C:\\doesnotexist");
            testPath("C:doesnotexist");
            testPath("\\\\server.nowhere.oracle.com\\share\\");
            testPath("\\\\fe80--203-baff-fe5a-749ds1.ipv6-literal.net\\share\\missing",
                "file:
        } else {
            testPath("doesnotexist");
            testPath("/doesnotexist");
            testPath("/does not exist");
            testUri("file:
            testUri("file:
            testUri("file:/foo/bar/doesnotexist");
            testUri("file:
            testBadUri("file:foo");
            testBadUri("file:
            testBadUri("file:
        }
        if (failures > 0)
            throw new RuntimeException(failures + " test(s) failed");
    }
}
