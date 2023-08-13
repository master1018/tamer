public abstract class SubWriterHolderWriter extends HtmlDocletWriter {
    public SubWriterHolderWriter(ConfigurationImpl configuration,
                                 String filename) throws IOException {
        super(configuration, filename);
    }
    public SubWriterHolderWriter(ConfigurationImpl configuration,
                                 String path, String filename, String relpath)
                                 throws IOException {
        super(configuration, path, filename, relpath);
    }
    public void printTypeSummaryHeader() {
        tdIndex();
        font("-1");
        code();
    }
    public void printTypeSummaryFooter() {
        codeEnd();
        fontEnd();
        tdEnd();
    }
    public void addSummaryHeader(AbstractMemberWriter mw, ClassDoc cd,
            Content memberTree) {
        mw.addSummaryAnchor(cd, memberTree);
        mw.addSummaryLabel(memberTree);
    }
    public Content getSummaryTableTree(AbstractMemberWriter mw, ClassDoc cd) {
        Content table = HtmlTree.TABLE(HtmlStyle.overviewSummary, 0, 3, 0,
                mw.getTableSummary(), getTableCaption(mw.getCaption()));
        table.addContent(getSummaryTableHeader(mw.getSummaryTableHeader(cd), "col"));
        return table;
    }
    public void printTableHeadingBackground(String str) {
        tableIndexDetail();
        tableHeaderStart("#CCCCFF", 1);
        strong(str);
        tableHeaderEnd();
        tableEnd();
    }
    public void addInheritedSummaryHeader(AbstractMemberWriter mw, ClassDoc cd,
            Content inheritedTree) {
        mw.addInheritedSummaryAnchor(cd, inheritedTree);
        mw.addInheritedSummaryLabel(cd, inheritedTree);
    }
    public void printSummaryFooter(AbstractMemberWriter mw, ClassDoc cd) {
        tableEnd();
        space();
    }
    public void printInheritedSummaryFooter(AbstractMemberWriter mw, ClassDoc cd) {
        codeEnd();
        summaryRowEnd();
        trEnd();
        tableEnd();
        space();
    }
    protected void addIndexComment(Doc member, Content contentTree) {
        addIndexComment(member, member.firstSentenceTags(), contentTree);
    }
    protected void printIndexComment(Doc member, Tag[] firstSentenceTags) {
        Tag[] deprs = member.tags("deprecated");
        if (Util.isDeprecated((ProgramElementDoc) member)) {
            strongText("doclet.Deprecated");
            space();
            if (deprs.length > 0) {
                printInlineDeprecatedComment(member, deprs[0]);
            }
            return;
        } else {
            ClassDoc cd = ((ProgramElementDoc)member).containingClass();
            if (cd != null && Util.isDeprecated(cd)) {
                strongText("doclet.Deprecated"); space();
            }
        }
        printSummaryComment(member, firstSentenceTags);
    }
    protected void addIndexComment(Doc member, Tag[] firstSentenceTags,
            Content tdSummary) {
        Tag[] deprs = member.tags("deprecated");
        Content div;
        if (Util.isDeprecated((ProgramElementDoc) member)) {
            Content strong = HtmlTree.STRONG(deprecatedPhrase);
            div = HtmlTree.DIV(HtmlStyle.block, strong);
            div.addContent(getSpace());
            if (deprs.length > 0) {
                addInlineDeprecatedComment(member, deprs[0], div);
            }
            tdSummary.addContent(div);
            return;
        } else {
            ClassDoc cd = ((ProgramElementDoc)member).containingClass();
            if (cd != null && Util.isDeprecated(cd)) {
                Content strong = HtmlTree.STRONG(deprecatedPhrase);
                div = HtmlTree.DIV(HtmlStyle.block, strong);
                div.addContent(getSpace());
                tdSummary.addContent(div);
            }
        }
        addSummaryComment(member, firstSentenceTags, tdSummary);
    }
    public void addSummaryType(AbstractMemberWriter mw, ProgramElementDoc member,
            Content tdSummaryType) {
        mw.addSummaryType(member, tdSummaryType);
    }
    public void addSummaryLinkComment(AbstractMemberWriter mw,
            ProgramElementDoc member, Content contentTree) {
        addSummaryLinkComment(mw, member, member.firstSentenceTags(), contentTree);
    }
    public void printSummaryLinkComment(AbstractMemberWriter mw,
                                        ProgramElementDoc member,
                                        Tag[] firstSentenceTags) {
        codeEnd();
        println();
        br();
        printNbsps();
        printIndexComment(member, firstSentenceTags);
        summaryRowEnd();
        trEnd();
    }
    public void addSummaryLinkComment(AbstractMemberWriter mw,
            ProgramElementDoc member, Tag[] firstSentenceTags, Content tdSummary) {
        addIndexComment(member, firstSentenceTags, tdSummary);
    }
    public void addInheritedMemberSummary(AbstractMemberWriter mw, ClassDoc cd,
            ProgramElementDoc member, boolean isFirst, Content linksTree) {
        if (! isFirst) {
            linksTree.addContent(", ");
        }
        mw.addInheritedSummaryLink(cd, member, linksTree);
    }
    public void printMemberHeader() {
        hr();
    }
    public void printMemberFooter() {
    }
    public Content getContentHeader() {
        HtmlTree div = new HtmlTree(HtmlTag.DIV);
        div.addStyle(HtmlStyle.contentContainer);
        return div;
    }
    public Content getMemberTreeHeader() {
        HtmlTree li = new HtmlTree(HtmlTag.LI);
        li.addStyle(HtmlStyle.blockList);
        return li;
    }
    public Content getMemberTree(Content contentTree) {
        Content ul = HtmlTree.UL(HtmlStyle.blockList, contentTree);
        return ul;
    }
    public Content getMemberSummaryTree(Content contentTree) {
        return getMemberTree(HtmlStyle.summary, contentTree);
    }
    public Content getMemberDetailsTree(Content contentTree) {
        return getMemberTree(HtmlStyle.details, contentTree);
    }
    public Content getMemberTree(HtmlStyle style, Content contentTree) {
        Content div = HtmlTree.DIV(style, getMemberTree(contentTree));
        return div;
    }
}
