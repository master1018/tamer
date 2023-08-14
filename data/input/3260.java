public class PackageFrameWriter extends HtmlDocletWriter {
    private PackageDoc packageDoc;
    private Set<ClassDoc> documentedClasses;
    public static final String OUTPUT_FILE_NAME = "package-frame.html";
    public PackageFrameWriter(ConfigurationImpl configuration,
                              PackageDoc packageDoc)
                              throws IOException {
        super(configuration, DirectoryManager.getDirectoryPath(packageDoc), OUTPUT_FILE_NAME, DirectoryManager.getRelativePath(packageDoc));
        this.packageDoc = packageDoc;
        if (configuration.root.specifiedPackages().length == 0) {
            documentedClasses = new HashSet<ClassDoc>(Arrays.asList(configuration.root.classes()));
        }
    }
    public static void generate(ConfigurationImpl configuration,
            PackageDoc packageDoc) {
        PackageFrameWriter packgen;
        try {
            packgen = new PackageFrameWriter(configuration, packageDoc);
            String pkgName = Util.getPackageName(packageDoc);
            Content body = packgen.getBody(false, packgen.getWindowTitle(pkgName));
            Content pkgNameContent = new RawHtml(pkgName);
            Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, HtmlStyle.bar,
                    packgen.getTargetPackageLink(packageDoc, "classFrame", pkgNameContent));
            body.addContent(heading);
            HtmlTree div = new HtmlTree(HtmlTag.DIV);
            div.addStyle(HtmlStyle.indexContainer);
            packgen.addClassListing(div);
            body.addContent(div);
            packgen.printHtmlDocument(
                    configuration.metakeywords.getMetaKeywords(packageDoc), false, body);
            packgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                    "doclet.exception_encountered",
                    exc.toString(), OUTPUT_FILE_NAME);
            throw new DocletAbortException();
        }
    }
    protected void addClassListing(Content contentTree) {
        Configuration config = configuration();
        if (packageDoc.isIncluded()) {
            addClassKindListing(packageDoc.interfaces(),
                getResource("doclet.Interfaces"), contentTree);
            addClassKindListing(packageDoc.ordinaryClasses(),
                getResource("doclet.Classes"), contentTree);
            addClassKindListing(packageDoc.enums(),
                getResource("doclet.Enums"), contentTree);
            addClassKindListing(packageDoc.exceptions(),
                getResource("doclet.Exceptions"), contentTree);
            addClassKindListing(packageDoc.errors(),
                getResource("doclet.Errors"), contentTree);
            addClassKindListing(packageDoc.annotationTypes(),
                getResource("doclet.AnnotationTypes"), contentTree);
        } else {
            String name = Util.getPackageName(packageDoc);
            addClassKindListing(config.classDocCatalog.interfaces(name),
                getResource("doclet.Interfaces"), contentTree);
            addClassKindListing(config.classDocCatalog.ordinaryClasses(name),
                getResource("doclet.Classes"), contentTree);
            addClassKindListing(config.classDocCatalog.enums(name),
                getResource("doclet.Enums"), contentTree);
            addClassKindListing(config.classDocCatalog.exceptions(name),
                getResource("doclet.Exceptions"), contentTree);
            addClassKindListing(config.classDocCatalog.errors(name),
                getResource("doclet.Errors"), contentTree);
            addClassKindListing(config.classDocCatalog.annotationTypes(name),
                getResource("doclet.AnnotationTypes"), contentTree);
        }
    }
    protected void addClassKindListing(ClassDoc[] arr, Content labelContent,
            Content contentTree) {
        if(arr.length > 0) {
            Arrays.sort(arr);
            boolean printedHeader = false;
            HtmlTree ul = new HtmlTree(HtmlTag.UL);
            ul.addAttr(HtmlAttr.TITLE, labelContent.toString());
            for (int i = 0; i < arr.length; i++) {
                if (documentedClasses != null &&
                        !documentedClasses.contains(arr[i])) {
                    continue;
                }
                if (!Util.isCoreClass(arr[i]) || !
                        configuration.isGeneratedDoc(arr[i])) {
                    continue;
                }
                if (!printedHeader) {
                    Content heading = HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING,
                            true, labelContent);
                    contentTree.addContent(heading);
                    printedHeader = true;
                }
                Content link = new RawHtml (getLink(new LinkInfoImpl(
                        LinkInfoImpl.PACKAGE_FRAME, arr[i],
                        (arr[i].isInterface() ? italicsText(arr[i].name()) :
                            arr[i].name()),"classFrame")));
                Content li = HtmlTree.LI(link);
                ul.addContent(li);
            }
            contentTree.addContent(ul);
        }
    }
}
