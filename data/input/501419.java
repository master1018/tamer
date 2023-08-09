public class PrefixFileFilter extends AbstractFileFilter implements Serializable {
    private final String[] prefixes;
    private final IOCase caseSensitivity;
    public PrefixFileFilter(String prefix) {
        this(prefix, IOCase.SENSITIVE);
    }
    public PrefixFileFilter(String prefix, IOCase caseSensitivity) {
        if (prefix == null) {
            throw new IllegalArgumentException("The prefix must not be null");
        }
        this.prefixes = new String[] {prefix};
        this.caseSensitivity = (caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity);
    }
    public PrefixFileFilter(String[] prefixes) {
        this(prefixes, IOCase.SENSITIVE);
    }
    public PrefixFileFilter(String[] prefixes, IOCase caseSensitivity) {
        if (prefixes == null) {
            throw new IllegalArgumentException("The array of prefixes must not be null");
        }
        this.prefixes = prefixes;
        this.caseSensitivity = (caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity);
    }
    public PrefixFileFilter(List prefixes) {
        this(prefixes, IOCase.SENSITIVE);
    }
    public PrefixFileFilter(List prefixes, IOCase caseSensitivity) {
        if (prefixes == null) {
            throw new IllegalArgumentException("The list of prefixes must not be null");
        }
        this.prefixes = (String[]) prefixes.toArray(new String[prefixes.size()]);
        this.caseSensitivity = (caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity);
    }
    public boolean accept(File file) {
        String name = file.getName();
        for (int i = 0; i < this.prefixes.length; i++) {
            if (caseSensitivity.checkStartsWith(name, prefixes[i])) {
                return true;
            }
        }
        return false;
    }
    public boolean accept(File file, String name) {
        for (int i = 0; i < prefixes.length; i++) {
            if (caseSensitivity.checkStartsWith(name, prefixes[i])) {
                return true;
            }
        }
        return false;
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(super.toString());
        buffer.append("(");
        if (prefixes != null) {
            for (int i = 0; i < prefixes.length; i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                buffer.append(prefixes[i]);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }
}
