public class EnumConstantBuilder extends AbstractMemberBuilder {
    private ClassDoc classDoc;
    private VisibleMemberMap visibleMemberMap;
    private EnumConstantWriter writer;
    private List<ProgramElementDoc> enumConstants;
    private int currentEnumConstantsIndex;
    private EnumConstantBuilder(Configuration configuration) {
        super(configuration);
    }
    public static EnumConstantBuilder getInstance(
            Configuration configuration,
            ClassDoc classDoc,
            EnumConstantWriter writer) {
        EnumConstantBuilder builder = new EnumConstantBuilder(configuration);
        builder.classDoc = classDoc;
        builder.writer = writer;
        builder.visibleMemberMap =
                new VisibleMemberMap(
                classDoc,
                VisibleMemberMap.ENUM_CONSTANTS,
                configuration.nodeprecated);
        builder.enumConstants =
                new ArrayList<ProgramElementDoc>(builder.visibleMemberMap.getMembersFor(classDoc));
        if (configuration.getMemberComparator() != null) {
            Collections.sort(
                    builder.enumConstants,
                    configuration.getMemberComparator());
        }
        return builder;
    }
    public String getName() {
        return "EnumConstantDetails";
    }
    public List<ProgramElementDoc> members(ClassDoc classDoc) {
        return visibleMemberMap.getMembersFor(classDoc);
    }
    public VisibleMemberMap getVisibleMemberMap() {
        return visibleMemberMap;
    }
    public boolean hasMembersToDocument() {
        return enumConstants.size() > 0;
    }
    public void buildEnumConstant(XMLNode node, Content memberDetailsTree) {
        if (writer == null) {
            return;
        }
        int size = enumConstants.size();
        if (size > 0) {
            Content enumConstantsDetailsTree = writer.getEnumConstantsDetailsTreeHeader(
                    classDoc, memberDetailsTree);
            for (currentEnumConstantsIndex = 0; currentEnumConstantsIndex < size;
                    currentEnumConstantsIndex++) {
                Content enumConstantsTree = writer.getEnumConstantsTreeHeader(
                        (FieldDoc) enumConstants.get(currentEnumConstantsIndex),
                        enumConstantsDetailsTree);
                buildChildren(node, enumConstantsTree);
                enumConstantsDetailsTree.addContent(writer.getEnumConstants(
                        enumConstantsTree, (currentEnumConstantsIndex == size - 1)));
            }
            memberDetailsTree.addContent(
                    writer.getEnumConstantsDetails(enumConstantsDetailsTree));
        }
    }
    public void buildSignature(XMLNode node, Content enumConstantsTree) {
        enumConstantsTree.addContent(writer.getSignature(
                (FieldDoc) enumConstants.get(currentEnumConstantsIndex)));
    }
    public void buildDeprecationInfo(XMLNode node, Content enumConstantsTree) {
        writer.addDeprecated(
                (FieldDoc) enumConstants.get(currentEnumConstantsIndex),
                enumConstantsTree);
    }
    public void buildEnumConstantComments(XMLNode node, Content enumConstantsTree) {
        if (!configuration.nocomment) {
            writer.addComments(
                    (FieldDoc) enumConstants.get(currentEnumConstantsIndex),
                    enumConstantsTree);
        }
    }
    public void buildTagInfo(XMLNode node, Content enumConstantsTree) {
        writer.addTags(
                (FieldDoc) enumConstants.get(currentEnumConstantsIndex),
                enumConstantsTree);
    }
    public EnumConstantWriter getWriter() {
        return writer;
    }
}
