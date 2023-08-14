public class TerminateWalk {
    static final Random rand = new Random();
    static boolean terminated;
    static FileVisitResult maybeTerminate() {
        if (terminated)
            throw new RuntimeException("FileVisitor invoked after termination");
        if (rand.nextInt(10) == 0) {
            terminated = true;
            return FileVisitResult.TERMINATE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(args[0]);
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return maybeTerminate();
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                return maybeTerminate();
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException x) {
                return maybeTerminate();
            }
        });
    }
}
