public class AllClassesFrameWriter extends HtmlDocletWriter {
    public static final String OUTPUT_FILE_NAME_FRAMES = "allclasses-frame.html";
    public static final String OUTPUT_FILE_NAME_NOFRAMES = "allclasses-noframe.html";
    protected IndexBuilder indexbuilder;
    final HtmlTree BR = new HtmlTree(HtmlTag.BR);
    public AllClassesFrameWriter(ConfigurationImpl configuration,
                                 String filename, IndexBuilder indexbuilder)
                              throws IOException {
        super(configuration, filename);
        this.indexbuilder = indexbuilder;
    }
    public static void generate(ConfigurationImpl configuration,
                                IndexBuilder indexbuilder) {
        AllClassesFrameWriter allclassgen;
        String filename = OUTPUT_FILE_NAME_FRAMES;
        try {
            allclassgen = new AllClassesFrameWriter(configuration,
                                                    filename, indexbuilder);
            allclassgen.buildAllClassesFile(true);
            allclassgen.close();
            filename = OUTPUT_FILE_NAME_NOFRAMES;
            allclassgen = new AllClassesFrameWriter(configuration,
                                                    filename, indexbuilder);
            allclassgen.buildAllClassesFile(false);
            allclassgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.
                     error("doclet.exception_encountered",
                           exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void buildAllClassesFile(boolean wantFrames) throws IOException {
        String label = configuration.getText("doclet.All_Classes");
        Content body = getBody(false, getWindowTitle(label));
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING,
                HtmlStyle.bar, allclassesLabel);
        body.addContent(heading);
        Content ul = new HtmlTree(HtmlTag.UL);
        addAllClasses(ul, wantFrames);
        Content div = HtmlTree.DIV(HtmlStyle.indexContainer, ul);
        body.addContent(div);
        printHtmlDocument(null, false, body);
    }
    protected void addAllClasses(Content content, boolean wantFrames) {
        for (int i = 0; i < indexbuilder.elements().length; i++) {
            Character unicode = (Character)((indexbuilder.elements())[i]);
            addContents(indexbuilder.getMemberList(unicode), wantFrames, content);
        }
    }
    protected void addContents(List<Doc> classlist, boolean wantFrames,
            Content content) {
        for (int i = 0; i < classlist.size(); i++) {
            ClassDoc cd = (ClassDoc)classlist.get(i);
            if (!Util.isCoreClass(cd)) {
                continue;
            }
            String label = italicsClassName(cd, false);
            Content linkContent;
            if(wantFrames){
                linkContent = new RawHtml(getLink(new LinkInfoImpl(
                        LinkInfoImpl.ALL_CLASSES_FRAME, cd, label, "classFrame")));
            } else {
                linkContent = new RawHtml(getLink(new LinkInfoImpl(cd, label)));
            }
            Content li = HtmlTree.LI(linkContent);
            content.addContent(li);
        }
    }
}
