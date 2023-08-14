public class WalkWithSecurity {
    public static void main(String[] args) throws IOException {
        String policyFile = args[0];
        ExpectedResult expectedResult = ExpectedResult.valueOf(args[1].toUpperCase());
        String here = System.getProperty("user.dir");
        String testSrc = System.getProperty("test.src");
        if (testSrc == null)
            throw new RuntimeException("This test must be run by jtreg");
        Path dir = Paths.get(testSrc);
        if (Files.isSameFile(Paths.get(here), dir))
            throw new RuntimeException("Working directory cannot be " + dir);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            if (!stream.iterator().hasNext())
                throw new RuntimeException(testSrc + " is empty");
        }
        System.setProperty("java.security.policy",
            dir.resolve(policyFile).toString());
        System.setSecurityManager(new SecurityManager());
        CountingVisitor visitor = new CountingVisitor();
        SecurityException exception = null;
        try {
            Files.walkFileTree(dir, visitor);
        } catch (SecurityException se) {
            exception = se;
        }
        switch (expectedResult) {
            case PASS:
                if (exception != null) {
                    exception.printStackTrace();
                    throw new RuntimeException("SecurityException not expected");
                }
                if (visitor.count() == 0)
                    throw new RuntimeException("No files visited");
                break;
            case FAIL:
                if (exception == null)
                    throw new RuntimeException("SecurityException expected");
                if (visitor.count() > 0)
                    throw new RuntimeException("Files were visited");
                break;
            case TOP_ONLY:
                if (exception != null) {
                    exception.printStackTrace();
                    throw new RuntimeException("SecurityException not expected");
                }
                if (visitor.count() == 0)
                    throw new RuntimeException("Starting file not visited");
                if (visitor.count() > 1)
                    throw new RuntimeException("More than starting file visited");
                break;
            default:
                throw new RuntimeException("Should not get here");
        }
    }
    static enum ExpectedResult {
        PASS,
        FAIL,
        TOP_ONLY;
    }
    static class CountingVisitor extends SimpleFileVisitor<Path> {
        private int count;
        int count() {
            return count;
        }
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            System.out.println(dir);
            count++;
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            System.out.println(file);
            count++;
            return FileVisitResult.CONTINUE;
        }
    }
}
