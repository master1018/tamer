public class PrintFileTree {
    public static void main(String[] args) throws Exception {
        boolean followLinks = false;
        boolean printCycles = false;
        int i = 0;
        while (i < (args.length-1)) {
            switch (args[i]) {
                case "-follow"      : followLinks = true; break;
                case "-printCycles" : printCycles = true;  break;
                default:
                    throw new RuntimeException(args[i] + " not recognized");
            }
            i++;
        }
        Path dir = Paths.get(args[i]);
        Set<FileVisitOption> options = new HashSet<FileVisitOption>();
        if (followLinks)
            options.add(FileVisitOption.FOLLOW_LINKS);
        final boolean follow = followLinks;
        final boolean reportCycles = printCycles;
        Files.walkFileTree(dir, options, Integer.MAX_VALUE, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println(dir);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                System.out.println(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException
            {
                if (exc != null)
                    throw exc;
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc)
                throws IOException
            {
                if (follow && (exc instanceof FileSystemLoopException)) {
                    if (reportCycles)
                        System.out.println(file);
                    return FileVisitResult.CONTINUE;
                } else {
                    throw exc;
                }
            }
        });
    }
}
