public class ConstructorBuilder extends AbstractMemberBuilder {
    public static final String NAME = "ConstructorDetails";
    private int currentConstructorIndex;
    private ClassDoc classDoc;
    private VisibleMemberMap visibleMemberMap;
    private ConstructorWriter writer;
    private List<ProgramElementDoc> constructors;
    private ConstructorBuilder(Configuration configuration) {
        super(configuration);
    }
    public static ConstructorBuilder getInstance(
            Configuration configuration,
            ClassDoc classDoc,
            ConstructorWriter writer) {
        ConstructorBuilder builder = new ConstructorBuilder(configuration);
        builder.classDoc = classDoc;
        builder.writer = writer;
        builder.visibleMemberMap =
                new VisibleMemberMap(
                classDoc,
                VisibleMemberMap.CONSTRUCTORS,
                configuration.nodeprecated);
        builder.constructors =
                new ArrayList<ProgramElementDoc>(builder.visibleMemberMap.getMembersFor(classDoc));
        for (int i = 0; i < builder.constructors.size(); i++) {
            if (builder.constructors.get(i).isProtected()
                    || builder.constructors.get(i).isPrivate()) {
                writer.setFoundNonPubConstructor(true);
            }
        }
        if (configuration.getMemberComparator() != null) {
            Collections.sort(
                    builder.constructors,
                    configuration.getMemberComparator());
        }
        return builder;
    }
    public String getName() {
        return NAME;
    }
    public boolean hasMembersToDocument() {
        return constructors.size() > 0;
    }
    public List<ProgramElementDoc> members(ClassDoc classDoc) {
        return visibleMemberMap.getMembersFor(classDoc);
    }
    public ConstructorWriter getWriter() {
        return writer;
    }
    public void buildConstructorDoc(XMLNode node, Content memberDetailsTree) {
        if (writer == null) {
            return;
        }
        int size = constructors.size();
        if (size > 0) {
            Content constructorDetailsTree = writer.getConstructorDetailsTreeHeader(
                    classDoc, memberDetailsTree);
            for (currentConstructorIndex = 0; currentConstructorIndex < size;
                    currentConstructorIndex++) {
                Content constructorDocTree = writer.getConstructorDocTreeHeader(
                        (ConstructorDoc) constructors.get(currentConstructorIndex),
                        constructorDetailsTree);
                buildChildren(node, constructorDocTree);
                constructorDetailsTree.addContent(writer.getConstructorDoc(
                        constructorDocTree, (currentConstructorIndex == size - 1)));
            }
            memberDetailsTree.addContent(
                    writer.getConstructorDetails(constructorDetailsTree));
        }
    }
    public void buildSignature(XMLNode node, Content constructorDocTree) {
        constructorDocTree.addContent(
                writer.getSignature(
                (ConstructorDoc) constructors.get(currentConstructorIndex)));
    }
    public void buildDeprecationInfo(XMLNode node, Content constructorDocTree) {
        writer.addDeprecated(
                (ConstructorDoc) constructors.get(currentConstructorIndex), constructorDocTree);
    }
    public void buildConstructorComments(XMLNode node, Content constructorDocTree) {
        if (!configuration.nocomment) {
            writer.addComments(
                    (ConstructorDoc) constructors.get(currentConstructorIndex),
                    constructorDocTree);
        }
    }
    public void buildTagInfo(XMLNode node, Content constructorDocTree) {
        writer.addTags((ConstructorDoc) constructors.get(currentConstructorIndex),
                constructorDocTree);
    }
}
