public class SkipSiblings {
    static final Random rand = new Random();
    static final Set<Path> skipped = new HashSet<Path>();
    static void check(Path path) {
        if (skipped.contains(path.getParent()))
            throw new RuntimeException(path + " should not have been visited");
    }
    static boolean skip(Path path) {
        Path parent = path.getParent();
        if (parent != null && rand.nextBoolean()) {
            skipped.add(parent);
            return true;
        }
        return false;
    }
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(args[0]);
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                check(dir);
                if (skip(dir))
                    return FileVisitResult.SKIP_SIBLINGS;
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                check(file);
                if (skip(file))
                    return FileVisitResult.SKIP_SIBLINGS;
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException x) {
                if (x != null)
                    throw new RuntimeException(x);
                check(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
