public class Eclipse {
    public static void generateFrom(Configuration c) throws IOException {
        StringBuilder classpath = new StringBuilder();
        classpath.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<classpath>\n");
        List<Pattern> patterns = new ArrayList<Pattern>();
        File precedence = new File("path-precedence");
        if (precedence.exists()) {
            Configuration.parseFile(precedence, patterns);
        } else {
            patterns.add(Pattern.compile("^(?!out/)"));
        }
        patterns.add(Pattern.compile(".*"));
        List<Bucket> buckets = new ArrayList<Bucket>(patterns.size());
        for (Pattern pattern : patterns) {
            buckets.add(new Bucket(pattern));
        }
        OUTER: for (File sourceRoot : c.sourceRoots) {
            String path = sourceRoot.getPath().substring(2);
            for (Bucket bucket : buckets) {
                if (bucket.matches(path)) {
                    bucket.sourceRoots.add(sourceRoot);
                    continue OUTER;
                }
            }
        }
        for (Bucket bucket : buckets) {
            for (File sourceRoot : bucket.sourceRoots) {
                classpath.append("  <classpathentry kind=\"src\"");
                CharSequence excluding = constructExcluding(sourceRoot, c);
                if (excluding.length() > 0) {
                    classpath.append(" excluding=\"")
                            .append(excluding).append("\"");
                }
                classpath.append(" path=\"")
                        .append(trimmed(sourceRoot)).append("\"/>\n");
            }
        }
        for (File jar : c.jarFiles) {
            classpath.append("  <classpathentry kind=\"lib\" path=\"")
                    .append(trimmed(jar)).append("\"/>\n");
        }
        classpath.append("  <classpathentry kind=\"output\" path=\""
                + "out/eclipse\"/>\n");
        classpath.append("</classpath>\n");
        Files.toFile(classpath.toString(), new File(".classpath"));
    }
    private static CharSequence constructExcluding(File sourceRoot,
            Configuration c) {
        StringBuilder classpath = new StringBuilder();
        String path = sourceRoot.getPath();
        SortedSet<File> nextRoots = c.sourceRoots.tailSet(sourceRoot);
        int count = 0;
        for (File nextRoot : nextRoots) {
            if (count == 0) {
                count++;
                continue;
            }
            String nextPath = nextRoot.getPath();
            if (!nextPath.startsWith(path)) {
                break;
            }
            if (count > 1) {
                classpath.append('|');
            }
            classpath.append(nextPath.substring(path.length() + 1))
                    .append('/');
            count++;
        }
        SortedSet<File> excludedDirs = c.excludedDirs.tailSet(sourceRoot);
        for (File excludedDir : excludedDirs) {
            String excludedPath = excludedDir.getPath();
            if (!excludedPath.startsWith(path)) {
                break;
            }
            if (count > 1) {
                classpath.append('|');
            }
            classpath.append(excludedPath.substring(path.length() + 1))
                    .append('/');
            count++;
        }
        return classpath;
    }
    private static String trimmed(File file) {
        return file.getPath().substring(2);
    }
    private static class Bucket {
        private final Pattern pattern;
        private final List<File> sourceRoots = new ArrayList<File>();
        private Bucket(Pattern pattern) {
            this.pattern = pattern;
        }
        private boolean matches(String path) {
            return pattern.matcher(path).find();
        }
    }
}
