public class MaxDepth {
    public static void main(String[] args) throws Exception {
        final Path top = Paths.get(args[0]);
        for (int i=0; i<5; i++) {
            Set<FileVisitOption> opts = Collections.emptySet();
            final int maxDepth = i;
            Files.walkFileTree(top, opts, maxDepth, new SimpleFileVisitor<Path>() {
                private int depth(Path file) {
                    Path rp = file.relativize(top);
                    return (rp.getFileName().toString().equals("")) ? 0 : rp.getNameCount();
                }
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    int d = depth(dir);
                    if (d == maxDepth)
                        throw new RuntimeException("Should not open directories at maxDepth");
                    if (d > maxDepth)
                        throw new RuntimeException("Too deep");
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    int d = depth(file);
                    if (d > maxDepth)
                        throw new RuntimeException("Too deep");
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
