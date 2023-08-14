public class SerializedFormWriterImpl extends SubWriterHolderWriter
    implements com.sun.tools.doclets.internal.toolkit.SerializedFormWriter {
    private static final String FILE_NAME = "serialized-form.html";
    public SerializedFormWriterImpl() throws IOException {
        super(ConfigurationImpl.getInstance(), FILE_NAME);
    }
    public void writeHeader(String header) {
        printHtmlHeader(header, null, true);
        printTop();
        navLinks(true);
        hr();
        center();
        h1();
        print(header);
        h1End();
        centerEnd();
    }
    public Content getHeader(String header) {
        Content bodyTree = getBody(true, getWindowTitle(header));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        Content h1Content = new StringContent(header);
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, true,
                HtmlStyle.title, h1Content);
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        bodyTree.addContent(div);
        return bodyTree;
    }
    public Content getSerializedSummariesHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public Content getPackageSerializedHeader() {
        HtmlTree li = new HtmlTree(HtmlTag.LI);
        li.addStyle(HtmlStyle.blockList);
        return li;
    }
    public Content getPackageHeader(String packageName) {
        Content heading = HtmlTree.HEADING(HtmlConstants.PACKAGE_HEADING, true,
                packageLabel);
        heading.addContent(getSpace());
        heading.addContent(packageName);
        return heading;
    }
    public Content getClassSerializedHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public Content getClassHeader(ClassDoc classDoc) {
        String classLink = (classDoc.isPublic() || classDoc.isProtected())?
            getLink(new LinkInfoImpl(classDoc,
            configuration.getClassName(classDoc))):
            classDoc.qualifiedName();
        Content li = HtmlTree.LI(HtmlStyle.blockList, getMarkerAnchor(
                classDoc.qualifiedName()));
        String superClassLink =
            classDoc.superclassType() != null ?
                getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_SERIALIZED_FORM,
                classDoc.superclassType())) :
                null;
        String className = superClassLink == null ?
            configuration.getText(
            "doclet.Class_0_implements_serializable", classLink) :
            configuration.getText(
            "doclet.Class_0_extends_implements_serializable", classLink,
            superClassLink);
        Content classNameContent = new RawHtml(className);
        li.addContent(HtmlTree.HEADING(HtmlConstants.SERIALIZED_MEMBER_HEADING,
                classNameContent));
        return li;
    }
    public Content getSerialUIDInfoHeader() {
        HtmlTree dl = new HtmlTree(HtmlTag.DL);
        dl.addStyle(HtmlStyle.nameValue);
        return dl;
    }
    public void addSerialUIDInfo(String header, String serialUID,
            Content serialUidTree) {
        Content headerContent = new StringContent(header);
        serialUidTree.addContent(HtmlTree.DT(headerContent));
        Content serialContent = new StringContent(serialUID);
        serialUidTree.addContent(HtmlTree.DD(serialContent));
    }
    public Content getClassContentHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public Content getSerializedContent(Content serializedTreeContent) {
        Content divContent = HtmlTree.DIV(HtmlStyle.serializedFormContainer,
                serializedTreeContent);
        return divContent;
    }
    public void addFooter(Content serializedTree) {
        addNavLinks(false, serializedTree);
        addBottom(serializedTree);
    }
    public void printDocument(Content serializedTree) {
        printHtmlDocument(null, true, serializedTree);
    }
    private void tableHeader() {
        tableIndexSummary();
        trBgcolorStyle("#CCCCFF", "TableSubHeadingColor");
    }
    private void tableFooter() {
        fontEnd();
        thEnd(); trEnd(); tableEnd();
    }
    public SerialFieldWriter getSerialFieldWriter(ClassDoc classDoc) {
        return new HtmlSerialFieldWriter(this, classDoc);
    }
    public SerialMethodWriter getSerialMethodWriter(ClassDoc classDoc) {
        return new HtmlSerialMethodWriter(this, classDoc);
    }
}
