public class AnnotationTypeOptionalMemberBuilder extends
    AnnotationTypeRequiredMemberBuilder {
    private AnnotationTypeOptionalMemberBuilder(Configuration configuration) {
        super(configuration);
    }
    public static AnnotationTypeOptionalMemberBuilder getInstance(
            Configuration configuration, ClassDoc classDoc,
            AnnotationTypeOptionalMemberWriter writer) {
        AnnotationTypeOptionalMemberBuilder builder =
            new AnnotationTypeOptionalMemberBuilder(configuration);
        builder.classDoc = classDoc;
        builder.writer = writer;
        builder.visibleMemberMap = new VisibleMemberMap(classDoc,
            VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL, configuration.nodeprecated);
        builder.members = new ArrayList<ProgramElementDoc>(
            builder.visibleMemberMap.getMembersFor(classDoc));
        if (configuration.getMemberComparator() != null) {
            Collections.sort(builder.members,
                configuration.getMemberComparator());
        }
        return builder;
    }
    @Override
    public String getName() {
        return "AnnotationTypeOptionalMemberDetails";
    }
    public void buildAnnotationTypeOptionalMember(XMLNode node, Content memberDetailsTree) {
        buildAnnotationTypeMember(node, memberDetailsTree);
    }
    public void buildDefaultValueInfo(XMLNode node, Content annotationDocTree) {
        ((AnnotationTypeOptionalMemberWriter) writer).addDefaultValueInfo(
                (MemberDoc) members.get(currentMemberIndex),
                annotationDocTree);
    }
    @Override
    public AnnotationTypeRequiredMemberWriter getWriter() {
        return writer;
    }
}
