public class FieldWriterImpl extends AbstractMemberWriter
    implements FieldWriter, MemberSummaryWriter {
    public FieldWriterImpl(SubWriterHolderWriter writer, ClassDoc classdoc) {
        super(writer, classdoc);
    }
    public FieldWriterImpl(SubWriterHolderWriter writer) {
        super(writer);
    }
    public Content getMemberSummaryHeader(ClassDoc classDoc,
            Content memberSummaryTree) {
        memberSummaryTree.addContent(HtmlConstants.START_OF_FIELD_SUMMARY);
        Content memberTree = writer.getMemberTreeHeader();
        writer.addSummaryHeader(this, classDoc, memberTree);
        return memberTree;
    }
    public Content getFieldDetailsTreeHeader(ClassDoc classDoc,
            Content memberDetailsTree) {
        memberDetailsTree.addContent(HtmlConstants.START_OF_FIELD_DETAILS);
        Content fieldDetailsTree = writer.getMemberTreeHeader();
        fieldDetailsTree.addContent(writer.getMarkerAnchor("field_detail"));
        Content heading = HtmlTree.HEADING(HtmlConstants.DETAILS_HEADING,
                writer.fieldDetailsLabel);
        fieldDetailsTree.addContent(heading);
        return fieldDetailsTree;
    }
    public Content getFieldDocTreeHeader(FieldDoc field,
            Content fieldDetailsTree) {
        fieldDetailsTree.addContent(
                writer.getMarkerAnchor(field.name()));
        Content fieldDocTree = writer.getMemberTreeHeader();
        Content heading = new HtmlTree(HtmlConstants.MEMBER_HEADING);
        heading.addContent(field.name());
        fieldDocTree.addContent(heading);
        return fieldDocTree;
    }
    public Content getSignature(FieldDoc field) {
        Content pre = new HtmlTree(HtmlTag.PRE);
        writer.addAnnotationInfo(field, pre);
        addModifiers(field, pre);
        Content fieldlink = new RawHtml(writer.getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_MEMBER,
                field.type())));
        pre.addContent(fieldlink);
        pre.addContent(" ");
        if (configuration().linksource) {
            Content fieldName = new StringContent(field.name());
            writer.addSrcLink(field, fieldName, pre);
        } else {
            addName(field.name(), pre);
        }
        return pre;
    }
    public void addDeprecated(FieldDoc field, Content fieldDocTree) {
        addDeprecatedInfo(field, fieldDocTree);
    }
    public void addComments(FieldDoc field, Content fieldDocTree) {
        ClassDoc holder = field.containingClass();
        if (field.inlineTags().length > 0) {
            if (holder.equals(classdoc) ||
                    (! (holder.isPublic() || Util.isLinkable(holder, configuration())))) {
                writer.addInlineComment(field, fieldDocTree);
            } else {
                Content link = new RawHtml(
                        writer.getDocLink(LinkInfoImpl.CONTEXT_FIELD_DOC_COPY,
                        holder, field,
                        holder.isIncluded() ?
                            holder.typeName() : holder.qualifiedTypeName(),
                            false));
                Content codeLink = HtmlTree.CODE(link);
                Content strong = HtmlTree.STRONG(holder.isClass()?
                   writer.descfrmClassLabel : writer.descfrmInterfaceLabel);
                strong.addContent(writer.getSpace());
                strong.addContent(codeLink);
                fieldDocTree.addContent(HtmlTree.DIV(HtmlStyle.block, strong));
                writer.addInlineComment(field, fieldDocTree);
            }
        }
    }
    public void addTags(FieldDoc field, Content fieldDocTree) {
        writer.addTagsInfo(field, fieldDocTree);
    }
    public Content getFieldDetails(Content fieldDetailsTree) {
        return getMemberTree(fieldDetailsTree);
    }
    public Content getFieldDoc(Content fieldDocTree,
            boolean isLastContent) {
        return getMemberTree(fieldDocTree, isLastContent);
    }
    public void close() throws IOException {
        writer.close();
    }
    public int getMemberKind() {
        return VisibleMemberMap.FIELDS;
    }
    public void addSummaryLabel(Content memberTree) {
        Content label = HtmlTree.HEADING(HtmlConstants.SUMMARY_HEADING,
                writer.getResource("doclet.Field_Summary"));
        memberTree.addContent(label);
    }
    public String getTableSummary() {
        return configuration().getText("doclet.Member_Table_Summary",
                configuration().getText("doclet.Field_Summary"),
                configuration().getText("doclet.fields"));
    }
    public String getCaption() {
        return configuration().getText("doclet.Fields");
    }
    public String[] getSummaryTableHeader(ProgramElementDoc member) {
        String[] header = new String[] {
            writer.getModifierTypeHeader(),
            configuration().getText("doclet.0_and_1",
                    configuration().getText("doclet.Field"),
                    configuration().getText("doclet.Description"))
        };
        return header;
    }
    public void addSummaryAnchor(ClassDoc cd, Content memberTree) {
        memberTree.addContent(writer.getMarkerAnchor("field_summary"));
    }
    public void addInheritedSummaryAnchor(ClassDoc cd, Content inheritedTree) {
        inheritedTree.addContent(writer.getMarkerAnchor(
                "fields_inherited_from_class_" + configuration().getClassName(cd)));
    }
    public void addInheritedSummaryLabel(ClassDoc cd, Content inheritedTree) {
        Content classLink = new RawHtml(writer.getPreQualifiedClassLink(
                LinkInfoImpl.CONTEXT_MEMBER, cd, false));
        Content label = new StringContent(cd.isClass() ?
            configuration().getText("doclet.Fields_Inherited_From_Class") :
            configuration().getText("doclet.Fields_Inherited_From_Interface"));
        Content labelHeading = HtmlTree.HEADING(HtmlConstants.INHERITED_SUMMARY_HEADING,
                label);
        labelHeading.addContent(writer.getSpace());
        labelHeading.addContent(classLink);
        inheritedTree.addContent(labelHeading);
    }
    protected void addSummaryLink(int context, ClassDoc cd, ProgramElementDoc member,
            Content tdSummary) {
        Content strong = HtmlTree.STRONG(new RawHtml(
                writer.getDocLink(context, cd , (MemberDoc) member, member.name(), false)));
        Content code = HtmlTree.CODE(strong);
        tdSummary.addContent(code);
    }
    protected void addInheritedSummaryLink(ClassDoc cd,
            ProgramElementDoc member, Content linksTree) {
        linksTree.addContent(new RawHtml(
                writer.getDocLink(LinkInfoImpl.CONTEXT_MEMBER, cd, (MemberDoc)member,
                member.name(), false)));
    }
    protected void addSummaryType(ProgramElementDoc member, Content tdSummaryType) {
        FieldDoc field = (FieldDoc)member;
        addModifierAndType(field, field.type(), tdSummaryType);
    }
    protected Content getDeprecatedLink(ProgramElementDoc member) {
        return writer.getDocLink(LinkInfoImpl.CONTEXT_MEMBER,
                (MemberDoc) member, ((FieldDoc)member).qualifiedName());
    }
    protected Content getNavSummaryLink(ClassDoc cd, boolean link) {
        if (link) {
            return writer.getHyperLink("", (cd == null)?
                "field_summary":
                "fields_inherited_from_class_" +
                configuration().getClassName(cd),
                writer.getResource("doclet.navField"));
        } else {
            return writer.getResource("doclet.navField");
        }
    }
    protected void addNavDetailLink(boolean link, Content liNav) {
        if (link) {
            liNav.addContent(writer.getHyperLink("", "field_detail",
                    writer.getResource("doclet.navField")));
        } else {
            liNav.addContent(writer.getResource("doclet.navField"));
        }
    }
}
