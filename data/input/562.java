public class MethodBuilder extends AbstractMemberBuilder {
    private int currentMethodIndex;
    private ClassDoc classDoc;
    private VisibleMemberMap visibleMemberMap;
    private MethodWriter writer;
    private List<ProgramElementDoc> methods;
    private MethodBuilder(Configuration configuration) {
        super(configuration);
    }
    public static MethodBuilder getInstance(
            Configuration configuration,
            ClassDoc classDoc,
            MethodWriter writer) {
        MethodBuilder builder = new MethodBuilder(configuration);
        builder.classDoc = classDoc;
        builder.writer = writer;
        builder.visibleMemberMap =
                new VisibleMemberMap(
                classDoc,
                VisibleMemberMap.METHODS,
                configuration.nodeprecated);
        builder.methods =
                new ArrayList<ProgramElementDoc>(builder.visibleMemberMap.getLeafClassMembers(
                configuration));
        if (configuration.getMemberComparator() != null) {
            Collections.sort(
                    builder.methods,
                    configuration.getMemberComparator());
        }
        return builder;
    }
    public String getName() {
        return "MethodDetails";
    }
    public List<ProgramElementDoc> members(ClassDoc classDoc) {
        return visibleMemberMap.getMembersFor(classDoc);
    }
    public VisibleMemberMap getVisibleMemberMap() {
        return visibleMemberMap;
    }
    public boolean hasMembersToDocument() {
        return methods.size() > 0;
    }
    public void buildMethodDoc(XMLNode node, Content memberDetailsTree) {
        if (writer == null) {
            return;
        }
        int size = methods.size();
        if (size > 0) {
            Content methodDetailsTree = writer.getMethodDetailsTreeHeader(
                    classDoc, memberDetailsTree);
            for (currentMethodIndex = 0; currentMethodIndex < size;
                    currentMethodIndex++) {
                Content methodDocTree = writer.getMethodDocTreeHeader(
                        (MethodDoc) methods.get(currentMethodIndex),
                        methodDetailsTree);
                buildChildren(node, methodDocTree);
                methodDetailsTree.addContent(writer.getMethodDoc(
                        methodDocTree, (currentMethodIndex == size - 1)));
            }
            memberDetailsTree.addContent(
                    writer.getMethodDetails(methodDetailsTree));
        }
    }
    public void buildSignature(XMLNode node, Content methodDocTree) {
        methodDocTree.addContent(
                writer.getSignature((MethodDoc) methods.get(currentMethodIndex)));
    }
    public void buildDeprecationInfo(XMLNode node, Content methodDocTree) {
        writer.addDeprecated(
                (MethodDoc) methods.get(currentMethodIndex), methodDocTree);
    }
    public void buildMethodComments(XMLNode node, Content methodDocTree) {
        if (!configuration.nocomment) {
            MethodDoc method = (MethodDoc) methods.get(currentMethodIndex);
            if (method.inlineTags().length == 0) {
                DocFinder.Output docs = DocFinder.search(
                        new DocFinder.Input(method));
                method = docs.inlineTags != null && docs.inlineTags.length > 0 ?
                    (MethodDoc) docs.holder : method;
            }
            writer.addComments(method.containingClass(), method, methodDocTree);
        }
    }
    public void buildTagInfo(XMLNode node, Content methodDocTree) {
        writer.addTags((MethodDoc) methods.get(currentMethodIndex),
                methodDocTree);
    }
    public MethodWriter getWriter() {
        return writer;
    }
}
