public class PackageWriterImpl extends HtmlDocletWriter
    implements PackageSummaryWriter {
    protected PackageDoc prev;
    protected PackageDoc next;
    protected PackageDoc packageDoc;
    private static final String OUTPUT_FILE_NAME = "package-summary.html";
    public PackageWriterImpl(ConfigurationImpl configuration,
        PackageDoc packageDoc, PackageDoc prev, PackageDoc next)
    throws IOException {
        super(configuration, DirectoryManager.getDirectoryPath(packageDoc), OUTPUT_FILE_NAME,
             DirectoryManager.getRelativePath(packageDoc.name()));
        this.prev = prev;
        this.next = next;
        this.packageDoc = packageDoc;
    }
    public String getOutputFileName() {
        return OUTPUT_FILE_NAME;
    }
    public Content getPackageHeader(String heading) {
        String pkgName = packageDoc.name();
        Content bodyTree = getBody(true, getWindowTitle(pkgName));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.header);
        Content annotationContent = new HtmlTree(HtmlTag.P);
        addAnnotationInfo(packageDoc, annotationContent);
        div.addContent(annotationContent);
        Content tHeading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, true,
                HtmlStyle.title, packageLabel);
        tHeading.addContent(getSpace());
        Content packageHead = new RawHtml(heading);
        tHeading.addContent(packageHead);
        div.addContent(tHeading);
        addDeprecationInfo(div);
        if (packageDoc.inlineTags().length > 0 && ! configuration.nocomment) {
            HtmlTree docSummaryDiv = new HtmlTree(HtmlTag.DIV);
            docSummaryDiv.addStyle(HtmlStyle.docSummary);
            addSummaryComment(packageDoc, docSummaryDiv);
            div.addContent(docSummaryDiv);
            Content space = getSpace();
            Content descLink = getHyperLink("", "package_description",
                    descriptionLabel, "", "");
            Content descPara = new HtmlTree(HtmlTag.P, seeLabel, space, descLink);
            div.addContent(descPara);
        }
        bodyTree.addContent(div);
        return bodyTree;
    }
    public Content getContentHeader() {
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.contentContainer);
        return div;
    }
    public void addDeprecationInfo(Content div) {
        Tag[] deprs = packageDoc.tags("deprecated");
        if (Util.isDeprecated(packageDoc)) {
            HtmlTree deprDiv = new HtmlTree(HtmlTag.DIV);
            deprDiv.addStyle(HtmlStyle.deprecatedContent);
            Content deprPhrase = HtmlTree.SPAN(HtmlStyle.strong, deprecatedPhrase);
            deprDiv.addContent(deprPhrase);
            if (deprs.length > 0) {
                Tag[] commentTags = deprs[0].inlineTags();
                if (commentTags.length > 0) {
                    addInlineDeprecatedComment(packageDoc, deprs[0], deprDiv);
                }
            }
            div.addContent(deprDiv);
        }
    }
    public Content getSummaryHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public void addClassesSummary(ClassDoc[] classes, String label,
            String tableSummary, String[] tableHeader, Content summaryContentTree) {
        if(classes.length > 0) {
            Arrays.sort(classes);
            Content caption = getTableCaption(label);
            Content table = HtmlTree.TABLE(HtmlStyle.packageSummary, 0, 3, 0,
                    tableSummary, caption);
            table.addContent(getSummaryTableHeader(tableHeader, "col"));
            Content tbody = new HtmlTree(HtmlTag.TBODY);
            for (int i = 0; i < classes.length; i++) {
                if (!Util.isCoreClass(classes[i]) ||
                    !configuration.isGeneratedDoc(classes[i])) {
                    continue;
                }
                Content classContent = new RawHtml(getLink(new LinkInfoImpl(
                        LinkInfoImpl.CONTEXT_PACKAGE, classes[i], false)));
                Content tdClass = HtmlTree.TD(HtmlStyle.colFirst, classContent);
                HtmlTree tr = HtmlTree.TR(tdClass);
                if (i%2 == 0)
                    tr.addStyle(HtmlStyle.altColor);
                else
                    tr.addStyle(HtmlStyle.rowColor);
                HtmlTree tdClassDescription = new HtmlTree(HtmlTag.TD);
                tdClassDescription.addStyle(HtmlStyle.colLast);
                if (Util.isDeprecated(classes[i])) {
                    tdClassDescription.addContent(deprecatedLabel);
                    if (classes[i].tags("deprecated").length > 0) {
                        addSummaryDeprecatedComment(classes[i],
                            classes[i].tags("deprecated")[0], tdClassDescription);
                    }
                }
                else
                    addSummaryComment(classes[i], tdClassDescription);
                tr.addContent(tdClassDescription);
                tbody.addContent(tr);
            }
            table.addContent(tbody);
            Content li = HtmlTree.LI(HtmlStyle.blockList, table);
            summaryContentTree.addContent(li);
        }
    }
    public void addPackageDescription(Content packageContentTree) {
        if (packageDoc.inlineTags().length > 0) {
            packageContentTree.addContent(getMarkerAnchor("package_description"));
            Content h2Content = new StringContent(
                    configuration.getText("doclet.Package_Description",
                    packageDoc.name()));
            packageContentTree.addContent(HtmlTree.HEADING(HtmlConstants.PACKAGE_HEADING,
                    true, h2Content));
            addInlineComment(packageDoc, packageContentTree);
        }
    }
    public void addPackageTags(Content packageContentTree) {
        addTagsInfo(packageDoc, packageContentTree);
    }
    public void addPackageFooter(Content contentTree) {
        addNavLinks(false, contentTree);
        addBottom(contentTree);
    }
    public void printDocument(Content contentTree) {
        printHtmlDocument(configuration.metakeywords.getMetaKeywords(packageDoc),
                true, contentTree);
    }
    protected Content getNavLinkClassUse() {
        Content useLink = getHyperLink("package-use.html", "",
                useLabel, "", "");
        Content li = HtmlTree.LI(useLink);
        return li;
    }
    public Content getNavLinkPrevious() {
        Content li;
        if (prev == null) {
            li = HtmlTree.LI(prevpackageLabel);
        } else {
            String path = DirectoryManager.getRelativePath(packageDoc.name(),
                                                           prev.name());
            li = HtmlTree.LI(getHyperLink(path + "package-summary.html", "",
                prevpackageLabel, "", ""));
        }
        return li;
    }
    public Content getNavLinkNext() {
        Content li;
        if (next == null) {
            li = HtmlTree.LI(nextpackageLabel);
        } else {
            String path = DirectoryManager.getRelativePath(packageDoc.name(),
                                                           next.name());
            li = HtmlTree.LI(getHyperLink(path + "package-summary.html", "",
                nextpackageLabel, "", ""));
        }
        return li;
    }
    protected Content getNavLinkTree() {
        Content useLink = getHyperLink("package-tree.html", "",
                treeLabel, "", "");
        Content li = HtmlTree.LI(useLink);
        return li;
    }
    protected Content getNavLinkPackage() {
        Content li = HtmlTree.LI(HtmlStyle.navBarCell1Rev, packageLabel);
        return li;
    }
}
