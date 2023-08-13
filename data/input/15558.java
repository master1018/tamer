public abstract class AbstractPackageIndexWriter extends HtmlDocletWriter {
    protected PackageDoc[] packages;
    public AbstractPackageIndexWriter(ConfigurationImpl configuration,
                                      String filename) throws IOException {
        super(configuration, filename);
        this.relativepathNoSlash = ".";
        packages = configuration.packages;
    }
    protected abstract void addNavigationBarHeader(Content body);
    protected abstract void addNavigationBarFooter(Content body);
    protected abstract void addOverviewHeader(Content body);
    protected abstract void addPackagesList(PackageDoc[] packages, String text,
            String tableSummary, Content body);
    protected void buildPackageIndexFile(String title, boolean includeScript) throws IOException {
        String windowOverview = configuration.getText(title);
        Content body = getBody(includeScript, getWindowTitle(windowOverview));
        addNavigationBarHeader(body);
        addOverviewHeader(body);
        addIndex(body);
        addOverview(body);
        addNavigationBarFooter(body);
        printHtmlDocument(configuration.metakeywords.getOverviewMetaKeywords(title,
                configuration.doctitle), includeScript, body);
    }
    protected void addOverview(Content body) throws IOException {
    }
    protected void addIndex(Content body) {
        addIndexContents(packages, "doclet.Package_Summary",
                configuration.getText("doclet.Member_Table_Summary",
                configuration.getText("doclet.Package_Summary"),
                configuration.getText("doclet.packages")), body);
    }
    protected void addIndexContents(PackageDoc[] packages, String text,
            String tableSummary, Content body) {
        if (packages.length > 0) {
            Arrays.sort(packages);
            addAllClassesLink(body);
            addPackagesList(packages, text, tableSummary, body);
        }
    }
    protected void addConfigurationTitle(Content body) {
        if (configuration.doctitle.length() > 0) {
            Content title = new RawHtml(configuration.doctitle);
            Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING,
                    HtmlStyle.title, title);
            Content div = HtmlTree.DIV(HtmlStyle.header, heading);
            body.addContent(div);
        }
    }
    protected Content getNavLinkContents() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, overviewLabel);
        return li;
    }
    protected void addAllClassesLink(Content body) {
    }
}
