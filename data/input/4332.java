public class FrameOutputWriter extends HtmlDocletWriter {
    int noOfPackages;
    private final String SCROLL_YES = "yes";
    public FrameOutputWriter(ConfigurationImpl configuration,
                             String filename) throws IOException {
        super(configuration, filename);
    noOfPackages = configuration.packages.length;
    }
    public static void generate(ConfigurationImpl configuration) {
        FrameOutputWriter framegen;
        String filename = "";
        try {
            filename = "index.html";
            framegen = new FrameOutputWriter(configuration, filename);
            framegen.generateFrameFile();
            framegen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generateFrameFile() {
        Content frameset = getFrameDetails();
        if (configuration.windowtitle.length() > 0) {
            printFramesetDocument(configuration.windowtitle, configuration.notimestamp,
                    frameset);
        } else {
            printFramesetDocument(configuration.getText("doclet.Generated_Docs_Untitled"),
                    configuration.notimestamp, frameset);
        }
    }
    protected void addFrameWarning(Content contentTree) {
        Content noframes = new HtmlTree(HtmlTag.NOFRAMES);
        Content noScript = HtmlTree.NOSCRIPT(
                HtmlTree.DIV(getResource("doclet.No_Script_Message")));
        noframes.addContent(noScript);
        Content noframesHead = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                getResource("doclet.Frame_Alert"));
        noframes.addContent(noframesHead);
        Content p = HtmlTree.P(getResource("doclet.Frame_Warning_Message",
                getHyperLinkString(configuration.topFile,
                configuration.getText("doclet.Non_Frame_Version"))));
        noframes.addContent(p);
        contentTree.addContent(noframes);
    }
    protected Content getFrameDetails() {
        HtmlTree frameset = HtmlTree.FRAMESET("20%,80%", null, "Documentation frame",
                "top.loadFrames()");
        if (noOfPackages <= 1) {
            addAllClassesFrameTag(frameset);
        } else if (noOfPackages > 1) {
            HtmlTree leftFrameset = HtmlTree.FRAMESET(null, "30%,70%", "Left frames",
                "top.loadFrames()");
            addAllPackagesFrameTag(leftFrameset);
            addAllClassesFrameTag(leftFrameset);
            frameset.addContent(leftFrameset);
        }
        addClassFrameTag(frameset);
        addFrameWarning(frameset);
        return frameset;
    }
    private void addAllPackagesFrameTag(Content contentTree) {
        HtmlTree frame = HtmlTree.FRAME("overview-frame.html", "packageListFrame",
                configuration.getText("doclet.All_Packages"));
        contentTree.addContent(frame);
    }
    private void addAllClassesFrameTag(Content contentTree) {
        HtmlTree frame = HtmlTree.FRAME("allclasses-frame.html", "packageFrame",
                configuration.getText("doclet.All_classes_and_interfaces"));
        contentTree.addContent(frame);
    }
    private void addClassFrameTag(Content contentTree) {
        HtmlTree frame = HtmlTree.FRAME(configuration.topFile, "classFrame",
                configuration.getText("doclet.Package_class_and_interface_descriptions"),
                SCROLL_YES);
        contentTree.addContent(frame);
    }
}
