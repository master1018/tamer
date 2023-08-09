public class HtmlDocument extends Content {
    private List<Content> docContent = Collections.<Content>emptyList();
    public HtmlDocument(Content docType, Content docComment, Content htmlTree) {
        docContent = new ArrayList<Content>();
        addContent(nullCheck(docType));
        addContent(nullCheck(docComment));
        addContent(nullCheck(htmlTree));
    }
    public HtmlDocument(Content docType, Content htmlTree) {
        docContent = new ArrayList<Content>();
        addContent(nullCheck(docType));
        addContent(nullCheck(htmlTree));
    }
    public void addContent(Content htmlContent) {
        if (htmlContent.isValid())
            docContent.add(htmlContent);
    }
    public void addContent(String stringContent) {
        throw new DocletAbortException();
    }
    public boolean isEmpty() {
        return (docContent.isEmpty());
    }
    public void write(StringBuilder contentBuilder) {
        for (Content c : docContent)
            c.write(contentBuilder);
    }
}
