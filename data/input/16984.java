public class ConstructorWriterImpl extends AbstractExecutableMemberWriter
    implements ConstructorWriter, MemberSummaryWriter {
    private boolean foundNonPubConstructor = false;
    public ConstructorWriterImpl(SubWriterHolderWriter writer,
            ClassDoc classDoc) {
        super(writer, classDoc);
        VisibleMemberMap visibleMemberMap = new VisibleMemberMap(classDoc,
            VisibleMemberMap.CONSTRUCTORS, configuration().nodeprecated);
        List<ProgramElementDoc> constructors = new ArrayList<ProgramElementDoc>(visibleMemberMap.getMembersFor(classDoc));
        for (int i = 0; i < constructors.size(); i++) {
            if ((constructors.get(i)).isProtected() ||
                (constructors.get(i)).isPrivate()) {
                setFoundNonPubConstructor(true);
            }
        }
    }
    public ConstructorWriterImpl(SubWriterHolderWriter writer) {
        super(writer);
    }
    public Content getMemberSummaryHeader(ClassDoc classDoc,
            Content memberSummaryTree) {
        memberSummaryTree.addContent(HtmlConstants.START_OF_CONSTRUCTOR_SUMMARY);
        Content memberTree = writer.getMemberTreeHeader();
        writer.addSummaryHeader(this, classDoc, memberTree);
        return memberTree;
    }
    public Content getConstructorDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree) {
        memberDetailsTree.addContent(HtmlConstants.START_OF_CONSTRUCTOR_DETAILS);
        Content constructorDetailsTree = writer.getMemberTreeHeader();
        constructorDetailsTree.addContent(writer.getMarkerAnchor("constructor_detail"));
        Content heading = HtmlTree.HEADING(HtmlConstants.DETAILS_HEADING,
                writer.constructorDetailsLabel);
        constructorDetailsTree.addContent(heading);
        return constructorDetailsTree;
    }
    public Content getConstructorDocTreeHeader(ConstructorDoc constructor,
            Content constructorDetailsTree) {
        String erasureAnchor;
        if ((erasureAnchor = getErasureAnchor(constructor)) != null) {
            constructorDetailsTree.addContent(writer.getMarkerAnchor((erasureAnchor)));
        }
        constructorDetailsTree.addContent(
                writer.getMarkerAnchor(writer.getAnchor(constructor)));
        Content constructorDocTree = writer.getMemberTreeHeader();
        Content heading = new HtmlTree(HtmlConstants.MEMBER_HEADING);
        heading.addContent(constructor.name());
        constructorDocTree.addContent(heading);
        return constructorDocTree;
    }
    public Content getSignature(ConstructorDoc constructor) {
        writer.displayLength = 0;
        Content pre = new HtmlTree(HtmlTag.PRE);
        writer.addAnnotationInfo(constructor, pre);
        addModifiers(constructor, pre);
        if (configuration().linksource) {
            Content constructorName = new StringContent(constructor.name());
            writer.addSrcLink(constructor, constructorName, pre);
        } else {
            addName(constructor.name(), pre);
        }
        addParameters(constructor, pre);
        addExceptions(constructor, pre);
        return pre;
    }
    @Override
    public void setSummaryColumnStyle(HtmlTree tdTree) {
        if (foundNonPubConstructor)
            tdTree.addStyle(HtmlStyle.colLast);
        else
            tdTree.addStyle(HtmlStyle.colOne);
    }
    public void addDeprecated(ConstructorDoc constructor, Content constructorDocTree) {
        addDeprecatedInfo(constructor, constructorDocTree);
    }
    public void addComments(ConstructorDoc constructor, Content constructorDocTree) {
        addComment(constructor, constructorDocTree);
    }
    public void addTags(ConstructorDoc constructor, Content constructorDocTree) {
        writer.addTagsInfo(constructor, constructorDocTree);
    }
    public Content getConstructorDetails(Content constructorDetailsTree) {
        return getMemberTree(constructorDetailsTree);
    }
    public Content getConstructorDoc(Content constructorDocTree,
            boolean isLastContent) {
        return getMemberTree(constructorDocTree, isLastContent);
    }
    public void close() throws IOException {
        writer.close();
    }
    public void setFoundNonPubConstructor(boolean foundNonPubConstructor) {
        this.foundNonPubConstructor = foundNonPubConstructor;
    }
    public void addSummaryLabel(Content memberTree) {
        Content label = HtmlTree.HEADING(HtmlConstants.SUMMARY_HEADING,
                writer.getResource("doclet.Constructor_Summary"));
        memberTree.addContent(label);
    }
    public String getTableSummary() {
        return configuration().getText("doclet.Member_Table_Summary",
                configuration().getText("doclet.Constructor_Summary"),
                configuration().getText("doclet.constructors"));
    }
    public String getCaption() {
        return configuration().getText("doclet.Constructors");
    }
    public String[] getSummaryTableHeader(ProgramElementDoc member) {
        String[] header;
        if (foundNonPubConstructor) {
            header = new String[] {
                configuration().getText("doclet.Modifier"),
                configuration().getText("doclet.0_and_1",
                        configuration().getText("doclet.Constructor"),
                        configuration().getText("doclet.Description"))
            };
        }
        else {
            header = new String[] {
                configuration().getText("doclet.0_and_1",
                        configuration().getText("doclet.Constructor"),
                        configuration().getText("doclet.Description"))
            };
        }
        return header;
    }
    public void addSummaryAnchor(ClassDoc cd, Content memberTree) {
        memberTree.addContent(writer.getMarkerAnchor("constructor_summary"));
    }
    public void addInheritedSummaryAnchor(ClassDoc cd, Content inheritedTree) {
    }
    public void addInheritedSummaryLabel(ClassDoc cd, Content inheritedTree) {
    }
    public int getMemberKind() {
        return VisibleMemberMap.CONSTRUCTORS;
    }
    protected Content getNavSummaryLink(ClassDoc cd, boolean link) {
        if (link) {
            return writer.getHyperLink("", "constructor_summary",
                    writer.getResource("doclet.navConstructor"));
        } else {
            return writer.getResource("doclet.navConstructor");
        }
    }
    protected void addNavDetailLink(boolean link, Content liNav) {
        if (link) {
            liNav.addContent(writer.getHyperLink("", "constructor_detail",
                    writer.getResource("doclet.navConstructor")));
        } else {
            liNav.addContent(writer.getResource("doclet.navConstructor"));
        }
    }
    protected void addSummaryType(ProgramElementDoc member, Content tdSummaryType) {
        if (foundNonPubConstructor) {
            Content code = new HtmlTree(HtmlTag.CODE);
            if (member.isProtected()) {
                code.addContent("protected ");
            } else if (member.isPrivate()) {
                code.addContent("private ");
            } else if (member.isPublic()) {
                code.addContent(writer.getSpace());
            } else {
                code.addContent(
                        configuration().getText("doclet.Package_private"));
            }
            tdSummaryType.addContent(code);
        }
    }
}
