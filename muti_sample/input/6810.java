public class AnnotationTypeRequiredMemberWriterImpl extends AbstractMemberWriter
    implements AnnotationTypeRequiredMemberWriter, MemberSummaryWriter {
    public AnnotationTypeRequiredMemberWriterImpl(SubWriterHolderWriter writer,
        AnnotationTypeDoc annotationType) {
        super(writer, annotationType);
    }
    public Content getMemberSummaryHeader(ClassDoc classDoc,
            Content memberSummaryTree) {
        memberSummaryTree.addContent(
                HtmlConstants.START_OF_ANNOTATION_TYPE_REQUIRED_MEMBER_SUMMARY);
        Content memberTree = writer.getMemberTreeHeader();
        writer.addSummaryHeader(this, classDoc, memberTree);
        return memberTree;
    }
    public void addAnnotationDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree) {
        if (!writer.printedAnnotationHeading) {
            memberDetailsTree.addContent(writer.getMarkerAnchor(
                    "annotation_type_element_detail"));
            Content heading = HtmlTree.HEADING(HtmlConstants.DETAILS_HEADING,
                    writer.annotationTypeDetailsLabel);
            memberDetailsTree.addContent(heading);
            writer.printedAnnotationHeading = true;
        }
    }
    public Content getAnnotationDocTreeHeader(MemberDoc member,
            Content annotationDetailsTree) {
        annotationDetailsTree.addContent(
                writer.getMarkerAnchor(member.name() +
                ((ExecutableMemberDoc) member).signature()));
        Content annotationDocTree = writer.getMemberTreeHeader();
        Content heading = new HtmlTree(HtmlConstants.MEMBER_HEADING);
        heading.addContent(member.name());
        annotationDocTree.addContent(heading);
        return annotationDocTree;
    }
    public Content getSignature(MemberDoc member) {
        Content pre = new HtmlTree(HtmlTag.PRE);
        writer.addAnnotationInfo(member, pre);
        addModifiers(member, pre);
        Content link = new RawHtml(
                writer.getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_MEMBER,
                getType(member))));
        pre.addContent(link);
        pre.addContent(writer.getSpace());
        if (configuration().linksource) {
            Content memberName = new StringContent(member.name());
            writer.addSrcLink(member, memberName, pre);
        } else {
            addName(member.name(), pre);
        }
        return pre;
    }
    public void addDeprecated(MemberDoc member, Content annotationDocTree) {
        addDeprecatedInfo(member, annotationDocTree);
    }
    public void addComments(MemberDoc member, Content annotationDocTree) {
        addComment(member, annotationDocTree);
    }
    public void addTags(MemberDoc member, Content annotationDocTree) {
        writer.addTagsInfo(member, annotationDocTree);
    }
    public Content getAnnotationDetails(Content annotationDetailsTree) {
        return getMemberTree(annotationDetailsTree);
    }
    public Content getAnnotationDoc(Content annotationDocTree,
            boolean isLastContent) {
        return getMemberTree(annotationDocTree, isLastContent);
    }
    public void close() throws IOException {
        writer.close();
    }
    public void addSummaryLabel(Content memberTree) {
        Content label = HtmlTree.HEADING(HtmlConstants.SUMMARY_HEADING,
                writer.getResource("doclet.Annotation_Type_Required_Member_Summary"));
        memberTree.addContent(label);
    }
    public String getTableSummary() {
        return configuration().getText("doclet.Member_Table_Summary",
                configuration().getText("doclet.Annotation_Type_Required_Member_Summary"),
                configuration().getText("doclet.annotation_type_required_members"));
    }
    public String getCaption() {
        return configuration().getText("doclet.Annotation_Type_Required_Members");
    }
    public String[] getSummaryTableHeader(ProgramElementDoc member) {
        String[] header = new String[] {
            writer.getModifierTypeHeader(),
            configuration().getText("doclet.0_and_1",
                    configuration().getText("doclet.Annotation_Type_Required_Member"),
                    configuration().getText("doclet.Description"))
        };
        return header;
    }
    public void addSummaryAnchor(ClassDoc cd, Content memberTree) {
        memberTree.addContent(writer.getMarkerAnchor(
                "annotation_type_required_element_summary"));
    }
    public void addInheritedSummaryAnchor(ClassDoc cd, Content inheritedTree) {
    }
    public void addInheritedSummaryLabel(ClassDoc cd, Content inheritedTree) {
    }
    protected void addSummaryLink(int context, ClassDoc cd, ProgramElementDoc member,
            Content tdSummary) {
        Content strong = HtmlTree.STRONG(new RawHtml(
                writer.getDocLink(context, (MemberDoc) member, member.name(), false)));
        Content code = HtmlTree.CODE(strong);
        tdSummary.addContent(code);
    }
    protected void addInheritedSummaryLink(ClassDoc cd,
            ProgramElementDoc member, Content linksTree) {
    }
    protected void addSummaryType(ProgramElementDoc member, Content tdSummaryType) {
        MemberDoc m = (MemberDoc)member;
        addModifierAndType(m, getType(m), tdSummaryType);
    }
    protected Content getDeprecatedLink(ProgramElementDoc member) {
        return writer.getDocLink(LinkInfoImpl.CONTEXT_MEMBER,
                (MemberDoc) member, ((MemberDoc)member).qualifiedName());
    }
    protected Content getNavSummaryLink(ClassDoc cd, boolean link) {
        if (link) {
            return writer.getHyperLink("", "annotation_type_required_element_summary",
                    writer.getResource("doclet.navAnnotationTypeRequiredMember"));
        } else {
            return writer.getResource("doclet.navAnnotationTypeRequiredMember");
        }
    }
    protected void addNavDetailLink(boolean link, Content liNav) {
        if (link) {
            liNav.addContent(writer.getHyperLink("", "annotation_type_element_detail",
                    writer.getResource("doclet.navAnnotationTypeMember")));
        } else {
            liNav.addContent(writer.getResource("doclet.navAnnotationTypeMember"));
        }
    }
    private Type getType(MemberDoc member) {
        if (member instanceof FieldDoc) {
            return ((FieldDoc) member).type();
        } else {
            return ((MethodDoc) member).returnType();
        }
    }
}
