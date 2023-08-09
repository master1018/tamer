public class RegexFileFilter extends AbstractFileFilter implements Serializable {
    private final Pattern pattern;
    public RegexFileFilter(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        this.pattern = Pattern.compile(pattern);
    }
    public RegexFileFilter(String pattern, IOCase caseSensitivity) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        int flags = 0;
        if (caseSensitivity != null && !caseSensitivity.isCaseSensitive()) {
            flags = Pattern.CASE_INSENSITIVE;
        }
        this.pattern = Pattern.compile(pattern, flags);
    }
    public RegexFileFilter(String pattern, int flags) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        this.pattern = Pattern.compile(pattern, flags);
    }
    public RegexFileFilter(Pattern pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern is missing");
        }
        this.pattern = pattern;
    }
    public boolean accept(File dir, String name) {
        return (pattern.matcher(name).matches());
    }
}
