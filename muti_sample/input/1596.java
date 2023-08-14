public class PackageIndexFrameWriter extends AbstractPackageIndexWriter {
    public PackageIndexFrameWriter(ConfigurationImpl configuration,
                                   String filename) throws IOException {
        super(configuration, filename);
    }
    public static void generate(ConfigurationImpl configuration) {
        PackageIndexFrameWriter packgen;
        String filename = "overview-frame.html";
        try {
            packgen = new PackageIndexFrameWriter(configuration, filename);
            packgen.buildPackageIndexFile("doclet.Window_Overview", false);
            packgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void addPackagesList(PackageDoc[] packages, String text,
            String tableSummary, Content body) {
        Content heading = HtmlTree.HEADING(HtmlConstants.PACKAGE_HEADING, true,
                packagesLabel);
        Content div = HtmlTree.DIV(HtmlStyle.indexContainer, heading);
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addAttr(HtmlAttr.TITLE, packagesLabel.toString());
        for(int i = 0; i < packages.length; i++) {
            if (packages[i] != null &&
                    (!(configuration.nodeprecated && Util.isDeprecated(packages[i])))) {
                ul.addContent(getPackage(packages[i]));
            }
        }
        div.addContent(ul);
        body.addContent(div);
    }
    protected Content getPackage(PackageDoc pd) {
        Content packageLinkContent;
        Content packageLabel;
        if (pd.name().length() > 0) {
            packageLabel = getPackageLabel(pd.name());
            packageLinkContent = getHyperLink(pathString(pd,
                    "package-frame.html"), "", packageLabel, "",
                    "packageFrame");
        } else {
            packageLabel = new RawHtml("&lt;unnamed package&gt;");
            packageLinkContent = getHyperLink("package-frame.html",
                    "", packageLabel, "", "packageFrame");
        }
        Content li = HtmlTree.LI(packageLinkContent);
        return li;
    }
    protected void addNavigationBarHeader(Content body) {
        Content headerContent;
        if (configuration.packagesheader.length() > 0) {
            headerContent = new RawHtml(replaceDocRootDir(configuration.packagesheader));
        } else {
            headerContent = new RawHtml(replaceDocRootDir(configuration.header));
        }
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, true,
                HtmlStyle.bar, headerContent);
        body.addContent(heading);
    }
    protected void addOverviewHeader(Content body) {
    }
    protected void addAllClassesLink(Content body) {
        Content linkContent = getHyperLink("allclasses-frame.html", "",
                allclassesLabel, "", "packageFrame");
        Content div = HtmlTree.DIV(HtmlStyle.indexHeader, linkContent);
        body.addContent(div);
    }
    protected void addNavigationBarFooter(Content body) {
        Content p = HtmlTree.P(getSpace());
        body.addContent(p);
    }
}
