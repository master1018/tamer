public class SingleIndexWriter extends AbstractIndexWriter {
    public SingleIndexWriter(ConfigurationImpl configuration,
                             String filename,
                             IndexBuilder indexbuilder) throws IOException {
        super(configuration, filename, indexbuilder);
        relativepathNoSlash = ".";
        relativePath = "./";
    }
    public static void generate(ConfigurationImpl configuration,
                                IndexBuilder indexbuilder) {
        SingleIndexWriter indexgen;
        String filename = "index-all.html";
        try {
            indexgen = new SingleIndexWriter(configuration,
                                             filename, indexbuilder);
            indexgen.generateIndexFile();
            indexgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generateIndexFile() throws IOException {
        String title = configuration.getText("doclet.Window_Single_Index");
        Content body = getBody(true, getWindowTitle(title));
        addTop(body);
        addNavLinks(true, body);
        HtmlTree divTree = new HtmlTree(HtmlTag.DIV);
        divTree.addStyle(HtmlStyle.contentContainer);
        addLinksForIndexes(divTree);
        for (int i = 0; i < indexbuilder.elements().length; i++) {
            Character unicode = (Character)((indexbuilder.elements())[i]);
            addContents(unicode, indexbuilder.getMemberList(unicode), divTree);
        }
        addLinksForIndexes(divTree);
        body.addContent(divTree);
        addNavLinks(false, body);
        addBottom(body);
        printHtmlDocument(null, true, body);
    }
    protected void addLinksForIndexes(Content contentTree) {
        for (int i = 0; i < indexbuilder.elements().length; i++) {
            String unicode = (indexbuilder.elements())[i].toString();
            contentTree.addContent(
                    getHyperLink("#_" + unicode + "_", new StringContent(unicode)));
            contentTree.addContent(getSpace());
        }
    }
}
