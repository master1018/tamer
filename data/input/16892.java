public class FieldBuilder extends AbstractMemberBuilder {
    private ClassDoc classDoc;
    private VisibleMemberMap visibleMemberMap;
    private FieldWriter writer;
    private List<ProgramElementDoc> fields;
    private int currentFieldIndex;
    private FieldBuilder(Configuration configuration) {
        super(configuration);
    }
    public static FieldBuilder getInstance(
            Configuration configuration,
            ClassDoc classDoc,
            FieldWriter writer) {
        FieldBuilder builder = new FieldBuilder(configuration);
        builder.classDoc = classDoc;
        builder.writer = writer;
        builder.visibleMemberMap =
                new VisibleMemberMap(
                classDoc,
                VisibleMemberMap.FIELDS,
                configuration.nodeprecated);
        builder.fields =
                new ArrayList<ProgramElementDoc>(builder.visibleMemberMap.getLeafClassMembers(
                configuration));
        if (configuration.getMemberComparator() != null) {
            Collections.sort(
                    builder.fields,
                    configuration.getMemberComparator());
        }
        return builder;
    }
    public String getName() {
        return "FieldDetails";
    }
    public List<ProgramElementDoc> members(ClassDoc classDoc) {
        return visibleMemberMap.getMembersFor(classDoc);
    }
    public VisibleMemberMap getVisibleMemberMap() {
        return visibleMemberMap;
    }
    public boolean hasMembersToDocument() {
        return fields.size() > 0;
    }
    public void buildFieldDoc(XMLNode node, Content memberDetailsTree) {
        if (writer == null) {
            return;
        }
        int size = fields.size();
        if (size > 0) {
            Content fieldDetailsTree = writer.getFieldDetailsTreeHeader(
                    classDoc, memberDetailsTree);
            for (currentFieldIndex = 0; currentFieldIndex < size;
                    currentFieldIndex++) {
                Content fieldDocTree = writer.getFieldDocTreeHeader(
                        (FieldDoc) fields.get(currentFieldIndex),
                        fieldDetailsTree);
                buildChildren(node, fieldDocTree);
                fieldDetailsTree.addContent(writer.getFieldDoc(
                        fieldDocTree, (currentFieldIndex == size - 1)));
            }
            memberDetailsTree.addContent(
                    writer.getFieldDetails(fieldDetailsTree));
        }
    }
    public void buildSignature(XMLNode node, Content fieldDocTree) {
        fieldDocTree.addContent(
                writer.getSignature((FieldDoc) fields.get(currentFieldIndex)));
    }
    public void buildDeprecationInfo(XMLNode node, Content fieldDocTree) {
        writer.addDeprecated(
                (FieldDoc) fields.get(currentFieldIndex), fieldDocTree);
    }
    public void buildFieldComments(XMLNode node, Content fieldDocTree) {
        if (!configuration.nocomment) {
            writer.addComments((FieldDoc) fields.get(currentFieldIndex), fieldDocTree);
        }
    }
    public void buildTagInfo(XMLNode node, Content fieldDocTree) {
        writer.addTags((FieldDoc) fields.get(currentFieldIndex), fieldDocTree);
    }
    public FieldWriter getWriter() {
        return writer;
    }
}
