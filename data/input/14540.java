public abstract class Content {
    public String toString() {
        StringBuilder contentBuilder = new StringBuilder();
        write(contentBuilder);
        return contentBuilder.toString();
    }
    public abstract void addContent(Content content);
    public abstract void addContent(String stringContent);
    public abstract void write(StringBuilder contentBuilder);
    public abstract boolean isEmpty();
    public boolean isValid() {
        return !isEmpty();
    }
    protected static <T> T nullCheck(T t) {
        t.getClass();
        return t;
    }
    protected boolean endsWithNewLine(StringBuilder contentBuilder) {
        int contentLength = contentBuilder.length();
        if (contentLength == 0) {
            return true;
        }
        int nlLength = DocletConstants.NL.length();
        if (contentLength < nlLength) {
            return false;
        }
        int contentIndex = contentLength - 1;
        int nlIndex = nlLength - 1;
        while (nlIndex >= 0) {
            if (contentBuilder.charAt(contentIndex) != DocletConstants.NL.charAt(nlIndex)) {
                return false;
            }
            contentIndex--;
            nlIndex--;
        }
        return true;
    }
}
