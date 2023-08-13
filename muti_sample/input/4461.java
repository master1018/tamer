public class DocType extends Content{
    private String docType;
    private static DocType transitional;
    private static DocType frameset;
    private DocType(String type, String dtd) {
        docType = "<!DOCTYPE HTML PUBLIC \"-
                "
    }
    public static DocType Transitional() {
        if (transitional == null)
            transitional = new DocType("Transitional", "http:
        return transitional;
    }
    public static DocType Frameset() {
        if (frameset == null)
            frameset = new DocType("Frameset", "http:
        return frameset;
    }
    public void addContent(Content content) {
        throw new DocletAbortException();
    }
    public void addContent(String stringContent) {
        throw new DocletAbortException();
    }
    public boolean isEmpty() {
        return (docType.length() == 0);
    }
    public void write(StringBuilder contentBuilder) {
        contentBuilder.append(docType);
    }
}
