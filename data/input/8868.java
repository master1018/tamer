public class ConstantsSummaryWriterImpl extends HtmlDocletWriter
        implements ConstantsSummaryWriter {
    ConfigurationImpl configuration;
    private ClassDoc currentClassDoc;
    private final String constantsTableSummary;
    private final String[] constantsTableHeader;
    public ConstantsSummaryWriterImpl(ConfigurationImpl configuration)
            throws IOException {
        super(configuration, ConfigurationImpl.CONSTANTS_FILE_NAME);
        this.configuration = configuration;
        constantsTableSummary = configuration.getText("doclet.Constants_Table_Summary",
                configuration.getText("doclet.Constants_Summary"));
        constantsTableHeader = new String[] {
            getModifierTypeHeader(),
            configuration.getText("doclet.ConstantField"),
            configuration.getText("doclet.Value")
        };
    }
    public Content getHeader() {
        String label = configuration.getText("doclet.Constants_Summary");
        Content bodyTree = getBody(true, getWindowTitle(label));
        addTop(bodyTree);
        addNavLinks(true, bodyTree);
        return bodyTree;
    }
    public Content getContentsHeader() {
        return new HtmlTree(HtmlTag.UL);
    }
    public void addLinkToPackageContent(PackageDoc pkg, String parsedPackageName,
            Set<String> printedPackageHeaders, Content contentListTree) {
        String packageName = pkg.name();
        Content link;
        if (packageName.length() == 0) {
            link = getHyperLink("#" + DocletConstants.UNNAMED_PACKAGE_ANCHOR,
                    "", defaultPackageLabel, "", "");
        } else {
            Content packageNameContent = getPackageLabel(parsedPackageName);
            packageNameContent.addContent(".*");
            link = getHyperLink("#" + parsedPackageName,
                    "", packageNameContent, "", "");
            printedPackageHeaders.add(parsedPackageName);
        }
        contentListTree.addContent(HtmlTree.LI(link));
    }
    public Content getContentsList(Content contentListTree) {
        Content titleContent = getResource(
                "doclet.Constants_Summary");
        Content pHeading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, true,
                HtmlStyle.title, titleContent);
        Content div = HtmlTree.DIV(HtmlStyle.header, pHeading);
        Content headingContent = getResource(
                "doclet.Contents");
        div.addContent(HtmlTree.HEADING(HtmlConstants.CONTENT_HEADING, true,
                headingContent));
        div.addContent(contentListTree);
        return div;
    }
    public Content getConstantSummaries() {
        HtmlTree summariesDiv = new HtmlTree(HtmlTag.DIV);
        summariesDiv.addStyle(HtmlStyle.constantValuesContainer);
        return summariesDiv;
    }
    public void addPackageName(PackageDoc pkg, String parsedPackageName,
            Content summariesTree) {
        Content pkgNameContent;
        if (parsedPackageName.length() == 0) {
            summariesTree.addContent(getMarkerAnchor(
                    DocletConstants.UNNAMED_PACKAGE_ANCHOR));
            pkgNameContent = defaultPackageLabel;
        } else {
            summariesTree.addContent(getMarkerAnchor(
                    parsedPackageName));
            pkgNameContent = getPackageLabel(parsedPackageName);
        }
        Content headingContent = new StringContent(".*");
        Content heading = HtmlTree.HEADING(HtmlConstants.PACKAGE_HEADING, true,
                pkgNameContent);
        heading.addContent(headingContent);
        summariesTree.addContent(heading);
    }
    public Content getClassConstantHeader() {
        HtmlTree ul = new HtmlTree(HtmlTag.UL);
        ul.addStyle(HtmlStyle.blockList);
        return ul;
    }
    public Content getConstantMembersHeader(ClassDoc cd) {
        String classlink = (cd.isPublic() || cd.isProtected())?
            getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CONSTANT_SUMMARY, cd,
                false)) :
            cd.qualifiedName();
        String name = cd.containingPackage().name();
        if (name.length() > 0) {
            return getClassName(name + "." + classlink);
        } else {
            return getClassName(classlink);
        }
    }
    protected Content getClassName(String classStr) {
        Content table = HtmlTree.TABLE(0, 3, 0, constantsTableSummary,
                getTableCaption(classStr));
        table.addContent(getSummaryTableHeader(constantsTableHeader, "col"));
        return table;
    }
    public void addConstantMembers(ClassDoc cd, List<FieldDoc> fields,
            Content classConstantTree) {
        currentClassDoc = cd;
        Content tbody = new HtmlTree(HtmlTag.TBODY);
        for (int i = 0; i < fields.size(); ++i) {
            HtmlTree tr = new HtmlTree(HtmlTag.TR);
            if (i%2 == 0)
                tr.addStyle(HtmlStyle.altColor);
            else
                tr.addStyle(HtmlStyle.rowColor);
            addConstantMember(fields.get(i), tr);
            tbody.addContent(tr);
        }
        Content table = getConstantMembersHeader(cd);
        table.addContent(tbody);
        Content li = HtmlTree.LI(HtmlStyle.blockList, table);
        classConstantTree.addContent(li);
    }
    private void addConstantMember(FieldDoc member, HtmlTree trTree) {
        trTree.addContent(getTypeColumn(member));
        trTree.addContent(getNameColumn(member));
        trTree.addContent(getValue(member));
    }
    private Content getTypeColumn(FieldDoc member) {
        Content anchor = getMarkerAnchor(currentClassDoc.qualifiedName() +
                "." + member.name());
        Content tdType = HtmlTree.TD(HtmlStyle.colFirst, anchor);
        Content code = new HtmlTree(HtmlTag.CODE);
        StringTokenizer mods = new StringTokenizer(member.modifiers());
        while(mods.hasMoreTokens()) {
            Content modifier = new StringContent(mods.nextToken());
            code.addContent(modifier);
            code.addContent(getSpace());
        }
        Content type = new RawHtml(getLink(new LinkInfoImpl(
                LinkInfoImpl.CONTEXT_CONSTANT_SUMMARY, member.type())));
        code.addContent(type);
        tdType.addContent(code);
        return tdType;
    }
    private Content getNameColumn(FieldDoc member) {
        Content nameContent = new RawHtml(getDocLink(
                LinkInfoImpl.CONTEXT_CONSTANT_SUMMARY, member, member.name(), false));
        Content code = HtmlTree.CODE(nameContent);
        return HtmlTree.TD(code);
    }
    private Content getValue(FieldDoc member) {
        Content valueContent = new StringContent(member.constantValueExpression());
        Content code = HtmlTree.CODE(valueContent);
        return HtmlTree.TD(HtmlStyle.colLast, code);
    }
    public void addFooter(Content contentTree) {
        addNavLinks(false, contentTree);
        addBottom(contentTree);
    }
    public void printDocument(Content contentTree) {
        printHtmlDocument(null, true, contentTree);
    }
}
