public class Nulls {
    static void npeExpected() {
        throw new RuntimeException("NullPointerException expected");
    }
    public static void main(String[] args) throws IOException {
        try {
            Files.walkFileTree(null, EnumSet.noneOf(FileVisitOption.class),
                Integer.MAX_VALUE, new SimpleFileVisitor<Path>(){});
            npeExpected();
        } catch (NullPointerException e) {
        }
        try {
            Files.walkFileTree(Paths.get("."), null, Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>(){});
            npeExpected();
        } catch (NullPointerException e) {
        }
        try {
            Files.walkFileTree(Paths.get("."), EnumSet.noneOf(FileVisitOption.class),
                -1, new SimpleFileVisitor<Path>(){});
            throw new RuntimeException("IllegalArgumentExpected expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            Set<FileVisitOption> opts = new HashSet<>(1);
            opts.add(null);
            Files.walkFileTree(Paths.get("."), opts, Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>(){});
            npeExpected();
        } catch (NullPointerException e) {
        }
        try {
            Files.walkFileTree(Paths.get("."), EnumSet.noneOf(FileVisitOption.class),
                Integer.MAX_VALUE, null);
            npeExpected();
        } catch (NullPointerException e) {
        }
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() { };
        boolean ranTheGauntlet = false;
        Path dir = Paths.get(".");
        BasicFileAttributes attrs = Files.readAttributes(dir, BasicFileAttributes.class);
        try { visitor.preVisitDirectory(null, attrs);
        } catch (NullPointerException x0) {
        try { visitor.preVisitDirectory(dir, null);
        } catch (NullPointerException x1) {
        try { visitor.visitFile(null, attrs);
        } catch (NullPointerException x2) {
        try {  visitor.visitFile(dir, null);
        } catch (NullPointerException x3) {
        try { visitor.visitFileFailed(null, new IOException());
        } catch (NullPointerException x4) {
        try { visitor.visitFileFailed(dir, null);
        } catch (NullPointerException x5) {
        try { visitor.postVisitDirectory(null, new IOException());
        } catch (NullPointerException x6) {
            ranTheGauntlet = true;
        }}}}}}}
        if (!ranTheGauntlet)
            throw new RuntimeException("A visit method did not throw NPE");
    }
}
