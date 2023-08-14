public class StringContent extends Content{
    private StringBuilder stringContent;
    public StringContent() {
        stringContent = new StringBuilder();
    }
    public StringContent(String initialContent) {
        stringContent = new StringBuilder(
                Util.escapeHtmlChars(nullCheck(initialContent)));
    }
    public void addContent(Content content) {
        throw new DocletAbortException();
    }
    public void addContent(String strContent) {
        stringContent.append(Util.escapeHtmlChars(nullCheck(strContent)));
    }
    public boolean isEmpty() {
        return (stringContent.length() == 0);
    }
    public String toString() {
        return stringContent.toString();
    }
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append(stringContent);
    }
}
