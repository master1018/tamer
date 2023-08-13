public class PatternSyntaxException extends IllegalArgumentException {
    private static final long serialVersionUID = -3864639126226059218L;
    private String desc;
    private String pattern;
    private int index = -1;
    public PatternSyntaxException(String description, String pattern, int index) {
        this.desc = description;
        this.pattern = pattern;
        this.index = index;
    }
    public String getPattern() {
        return pattern;
    }
    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("Syntax error");
        if (desc != null) {
            builder.append(' ');
            builder.append(desc);
        }
        if (index >= 0) {
            builder.append(" near index " + index + ":");
        }
        if (pattern != null) {
            builder.append('\n');
            builder.append(pattern);
            if (index >= 0) {
                char[] spaces = new char[index];
                Arrays.fill(spaces, ' ');
                builder.append('\n');
                builder.append(spaces);
                builder.append('^');
            }
        }
        return builder.toString();
    }
    public String getDescription() {
        return desc;
    }
    public int getIndex() {
        return index;
    }
}
