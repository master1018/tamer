public class PackageIndexWriter extends AbstractPackageIndexWriter {
    private RootDoc root;
    private Map<String,List<PackageDoc>> groupPackageMap;
    private List<String> groupList;
    public PackageIndexWriter(ConfigurationImpl configuration,
                              String filename)
                       throws IOException {
        super(configuration, filename);
        this.root = configuration.root;
        groupPackageMap = configuration.group.groupPackages(packages);
        groupList = configuration.group.getGroupList();
    }
    public static void generate(ConfigurationImpl configuration) {
        PackageIndexWriter packgen;
        String filename = "overview-summary.html";
        try {
            packgen = new PackageIndexWriter(configuration, filename);
            packgen.buildPackageIndexFile("doclet.Window_Overview_Summary", true);
            packgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    protected void addIndex(Content body) {
        for (int i = 0; i < groupList.size(); i++) {
        String groupname = groupList.get(i);
        List<PackageDoc> list = groupPackageMap.get(groupname);
            if (list != null && list.size() > 0) {
                addIndexContents(list.toArray(new PackageDoc[list.size()]),
                        groupname, configuration.getText("doclet.Member_Table_Summary",
                        groupname, configuration.getText("doclet.packages")), body);
            }
        }
    }
    protected void addPackagesList(PackageDoc[] packages, String text,
            String tableSummary, Content body) {
        Content table = HtmlTree.TABLE(HtmlStyle.overviewSummary, 0, 3, 0, tableSummary,
                getTableCaption(text));
        table.addContent(getSummaryTableHeader(packageTableHeader, "col"));
        Content tbody = new HtmlTree(HtmlTag.TBODY);
        addPackagesList(packages, tbody);
        table.addContent(tbody);
        Content div = HtmlTree.DIV(HtmlStyle.contentContainer, table);
        body.addContent(div);
    }
    protected void addPackagesList(PackageDoc[] packages, Content tbody) {
        for (int i = 0; i < packages.length; i++) {
            if (packages[i] != null && packages[i].name().length() > 0) {
                if (configuration.nodeprecated && Util.isDeprecated(packages[i]))
                    continue;
                Content packageLinkContent = getPackageLink(packages[i],
                        getPackageName(packages[i]));
                Content tdPackage = HtmlTree.TD(HtmlStyle.colFirst, packageLinkContent);
                HtmlTree tdSummary = new HtmlTree(HtmlTag.TD);
                tdSummary.addStyle(HtmlStyle.colLast);
                addSummaryComment(packages[i], tdSummary);
                HtmlTree tr = HtmlTree.TR(tdPackage);
                tr.addContent(tdSummary);
                if (i%2 == 0)
                    tr.addStyle(HtmlStyle.altColor);
                else
                    tr.addStyle(HtmlStyle.rowColor);
                tbody.addContent(tr);
            }
        }
    }
    protected void addOverviewHeader(Content body) {
        if (root.inlineTags().length > 0) {
            HtmlTree subTitleDiv = new HtmlTree(HtmlTag.DIV);
            subTitleDiv.addStyle(HtmlStyle.subTitle);
            addSummaryComment(root, subTitleDiv);
            Content div = HtmlTree.DIV(HtmlStyle.header, subTitleDiv);
            Content see = seeLabel;
            see.addContent(" ");
            Content descPara = HtmlTree.P(see);
            Content descLink = getHyperLink("", "overview_description",
                descriptionLabel, "", "");
            descPara.addContent(descLink);
            div.addContent(descPara);
            body.addContent(div);
        }
    }
    protected void addOverviewComment(Content htmltree) {
        if (root.inlineTags().length > 0) {
            htmltree.addContent(getMarkerAnchor("overview_description"));
            HtmlTree div = new HtmlTree(HtmlTag.DIV);
            div.addStyle(HtmlStyle.subTitle);
            addInlineComment(root, div);
            htmltree.addContent(div);
        }
    }
    protected void addOverview(Content body) throws IOException {
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.footer);
        addOverviewComment(div);
        addTagsInfo(root, div);
        body.addContent(div);
    }
    protected void addNavigationBarHeader(Content body) {
        addTop(body);
        addNavLinks(true, body);
        addConfigurationTitle(body);
    }
    protected void addNavigationBarFooter(Content body) {
        addNavLinks(false, body);
        addBottom(body);
    }
}
