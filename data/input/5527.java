public class PackageTreeWriter extends AbstractTreeWriter {
    protected PackageDoc packagedoc;
    protected PackageDoc prev;
    protected PackageDoc next;
    public PackageTreeWriter(ConfigurationImpl configuration,
                             String path, String filename,
                             PackageDoc packagedoc,
                             PackageDoc prev, PackageDoc next)
                      throws IOException {
        super(configuration, path, filename,
              new ClassTree(
                configuration.classDocCatalog.allClasses(packagedoc),
                configuration),
              packagedoc);
        this.packagedoc = packagedoc;
        this.prev = prev;
        this.next = next;
    }
    public static void generate(ConfigurationImpl configuration,
                                PackageDoc pkg, PackageDoc prev,
                                PackageDoc next, boolean noDeprecated) {
        PackageTreeWriter packgen;
        String path = DirectoryManager.getDirectoryPath(pkg);
        String filename = "package-tree.html";
        try {
            packgen = new PackageTreeWriter(configuration, path, filename, pkg,
                prev, next);
            packgen.generatePackageTreeFile();
            packgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void generatePackageTreeFile() throws IOException {
        Content body = getPackageTreeHeader();
        Content headContent = getResource("doclet.Hierarchy_For_Package",
                Util.getPackageName(packagedoc));
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, false,
                HtmlStyle.title, headContent);
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        if (configuration.packages.length > 1) {
            addLinkToMainTree(div);
        }
        body.addContent(div);
        HtmlTree divTree = new HtmlTree(HtmlTag.DIV);
        divTree.addStyle(HtmlStyle.contentContainer);
        addTree(classtree.baseclasses(), "doclet.Class_Hierarchy", divTree);
        addTree(classtree.baseinterfaces(), "doclet.Interface_Hierarchy", divTree);
        addTree(classtree.baseAnnotationTypes(), "doclet.Annotation_Type_Hierarchy", divTree);
        addTree(classtree.baseEnums(), "doclet.Enum_Hierarchy", divTree);
        body.addContent(divTree);
        addNavLinks(false, body);
        addBottom(body);
        printHtmlDocument(null, true, body);
    }
    protected Content getPackageTreeHeader() {
        String title = packagedoc.name() + " " +
                configuration.getText("doclet.Window_Class_Hierarchy");
        Content bodyTree = getBody(true, getWindowTitle(title));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        return bodyTree;
    }
    protected void addLinkToMainTree(Content div) {
        Content span = HtmlTree.SPAN(HtmlStyle.strong,
                getResource("doclet.Package_Hierarchies"));
        div.addContent(span);
        HtmlTree ul = new HtmlTree (HtmlTag.UL);
        ul.addStyle(HtmlStyle.horizontal);
        ul.addContent(getNavLinkMainTree(configuration.getText("doclet.All_Packages")));
        div.addContent(ul);
    }
    protected Content getNavLinkPrevious() {
        if (prev == null) {
            return getNavLinkPrevious(null);
        } else {
            String path = DirectoryManager.getRelativePath(packagedoc.name(),
                    prev.name());
            return getNavLinkPrevious(path + "package-tree.html");
        }
    }
    protected Content getNavLinkNext() {
        if (next == null) {
            return getNavLinkNext(null);
        } else {
            String path = DirectoryManager.getRelativePath(packagedoc.name(),
                    next.name());
            return getNavLinkNext(path + "package-tree.html");
        }
    }
    protected Content getNavLinkPackage() {
        Content linkContent = getHyperLink("package-summary.html", "",
                packageLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
}
