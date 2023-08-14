public class RawHtml extends Content{
    private String rawHtmlContent;
    public static final Content nbsp = new RawHtml("&nbsp;");
    public RawHtml(String rawHtml) {
        rawHtmlContent = nullCheck(rawHtml);
    }
    public void addContent(Content content) {
        throw new DocletAbortException();
    }
    public void addContent(String stringContent) {
        throw new DocletAbortException();
    }
    public boolean isEmpty() {
        return rawHtmlContent.isEmpty();
    }
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append(rawHtmlContent);
    }
}
