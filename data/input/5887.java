public class SplitIndexWriter extends AbstractIndexWriter {
    protected int prev;
    protected int next;
    public SplitIndexWriter(ConfigurationImpl configuration,
                            String path, String filename,
                            String relpath, IndexBuilder indexbuilder,
                            int prev, int next) throws IOException {
        super(configuration, path, filename, relpath, indexbuilder);
        this.prev = prev;
        this.next = next;
    }
    public static void generate(ConfigurationImpl configuration,
                                IndexBuilder indexbuilder) {
        SplitIndexWriter indexgen;
        String filename = "";
        String path = DirectoryManager.getPath("index-files");
        String relpath = DirectoryManager.getRelativePath("index-files");
        try {
            for (int i = 0; i < indexbuilder.elements().length; i++) {
                int j = i + 1;
                int prev = (j == 1)? -1: i;
                int next = (j == indexbuilder.elements().length)? -1: j + 1;
                filename = "index-" + j +".html";
                indexgen = new SplitIndexWriter(configuration,
                                                path, filename, relpath,
                                                indexbuilder, prev, next);
                indexgen.generateIndexFile((Character)indexbuilder.
                                                                 elements()[i]);
                indexgen.close();
            }
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generateIndexFile(Character unicode) throws IOException {
        String title = configuration.getText("doclet.Window_Split_Index",
                unicode.toString());
        Content body = getBody(true, getWindowTitle(title));
        addTop(body);
        addNavLinks(true, body);
        HtmlTree divTree = new HtmlTree(HtmlTag.DIV);
        divTree.addStyle(HtmlStyle.contentContainer);
        addLinksForIndexes(divTree);
        addContents(unicode, indexbuilder.getMemberList(unicode), divTree);
        addLinksForIndexes(divTree);
        body.addContent(divTree);
        addNavLinks(false, body);
        addBottom(body);
        printHtmlDocument(null, true, body);
    }
    protected void addLinksForIndexes(Content contentTree) {
        Object[] unicodeChars = indexbuilder.elements();
        for (int i = 0; i < unicodeChars.length; i++) {
            int j = i + 1;
            contentTree.addContent(getHyperLink("index-" + j + ".html",
                    new StringContent(unicodeChars[i].toString())));
            contentTree.addContent(getSpace());
        }
    }
    public Content getNavLinkPrevious() {
        Content prevletterLabel = getResource("doclet.Prev_Letter");
        if (prev == -1) {
            return HtmlTree.LI(prevletterLabel);
        }
        else {
            Content prevLink = getHyperLink("index-" + prev + ".html", "",
                    prevletterLabel);
            return HtmlTree.LI(prevLink);
        }
    }
    public Content getNavLinkNext() {
        Content nextletterLabel = getResource("doclet.Next_Letter");
        if (next == -1) {
            return HtmlTree.LI(nextletterLabel);
        }
        else {
            Content nextLink = getHyperLink("index-" + next + ".html","",
                    nextletterLabel);
            return HtmlTree.LI(nextLink);
        }
    }
}
