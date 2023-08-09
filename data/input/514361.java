public class Excludes {
    private final List<Pattern> patterns;
    public Excludes(List<Pattern> patterns) {
        this.patterns = patterns;
    }
    public boolean exclude(String path) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(path).find()) {
                return true;
            }
        }
        return false;
    }
}
