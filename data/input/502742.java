public class Find implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -7104607303959381785L;
    private final String regex;
    public Find(String regex) {
        this.regex = regex;
    }
    public boolean matches(Object actual) {
        return (actual instanceof String)
                && Pattern.compile(regex).matcher((String) actual).find();
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("find(\"" + regex.replaceAll("\\\\", "\\\\\\\\") + "\")");
    }
}
