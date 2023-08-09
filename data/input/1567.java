public class AnnotationTypeRequiredMemberBuilder extends AbstractMemberBuilder {
    protected ClassDoc classDoc;
    protected VisibleMemberMap visibleMemberMap;
    protected AnnotationTypeRequiredMemberWriter writer;
    protected List<ProgramElementDoc> members;
    protected int currentMemberIndex;
    protected AnnotationTypeRequiredMemberBuilder(Configuration configuration) {
        super(configuration);
    }
    public static AnnotationTypeRequiredMemberBuilder getInstance(
            Configuration configuration, ClassDoc classDoc,
            AnnotationTypeRequiredMemberWriter writer) {
        AnnotationTypeRequiredMemberBuilder builder =
            new AnnotationTypeRequiredMemberBuilder(configuration);
        builder.classDoc = classDoc;
        builder.writer = writer;
        builder.visibleMemberMap = new VisibleMemberMap(classDoc,
            VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED, configuration.nodeprecated);
        builder.members = new ArrayList<ProgramElementDoc>(
            builder.visibleMemberMap.getMembersFor(classDoc));
        if (configuration.getMemberComparator() != null) {
            Collections.sort(builder.members,
                configuration.getMemberComparator());
        }
        return builder;
    }
    public String getName() {
        return "AnnotationTypeRequiredMemberDetails";
    }
    public List<ProgramElementDoc> members(ClassDoc classDoc) {
        return visibleMemberMap.getMembersFor(classDoc);
    }
    public VisibleMemberMap getVisibleMemberMap() {
        return visibleMemberMap;
    }
    public boolean hasMembersToDocument() {
        return members.size() > 0;
    }
    public void buildAnnotationTypeRequiredMember(XMLNode node, Content memberDetailsTree) {
        buildAnnotationTypeMember(node, memberDetailsTree);
    }
    public void buildAnnotationTypeMember(XMLNode node, Content memberDetailsTree) {
        if (writer == null) {
            return;
        }
        int size = members.size();
        if (size > 0) {
            writer.addAnnotationDetailsTreeHeader(
                    classDoc, memberDetailsTree);
            for (currentMemberIndex = 0; currentMemberIndex < size;
            currentMemberIndex++) {
                Content annotationDocTree = writer.getAnnotationDocTreeHeader(
                        (MemberDoc) members.get(currentMemberIndex),
                        memberDetailsTree);
                buildChildren(node, annotationDocTree);
                memberDetailsTree.addContent(writer.getAnnotationDoc(
                        annotationDocTree, (currentMemberIndex == size - 1)));
            }
        }
    }
    public void buildSignature(XMLNode node, Content annotationDocTree) {
        annotationDocTree.addContent(
                writer.getSignature((MemberDoc) members.get(currentMemberIndex)));
    }
    public void buildDeprecationInfo(XMLNode node, Content annotationDocTree) {
        writer.addDeprecated((MemberDoc) members.get(currentMemberIndex),
                annotationDocTree);
    }
    public void buildMemberComments(XMLNode node, Content annotationDocTree) {
        if(! configuration.nocomment){
            writer.addComments((MemberDoc) members.get(currentMemberIndex),
                    annotationDocTree);
        }
    }
    public void buildTagInfo(XMLNode node, Content annotationDocTree) {
        writer.addTags((MemberDoc) members.get(currentMemberIndex),
                annotationDocTree);
    }
    public AnnotationTypeRequiredMemberWriter getWriter() {
        return writer;
    }
}
