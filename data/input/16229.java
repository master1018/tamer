public class MemberSummaryBuilder extends AbstractMemberBuilder {
    public static final String NAME = "MemberSummary";
    private VisibleMemberMap[] visibleMemberMaps;
    private MemberSummaryWriter[] memberSummaryWriters;
    private ClassDoc classDoc;
    private MemberSummaryBuilder(Configuration configuration) {
        super(configuration);
    }
    public static MemberSummaryBuilder getInstance(
            ClassWriter classWriter, Configuration configuration)
            throws Exception {
        MemberSummaryBuilder builder = new MemberSummaryBuilder(configuration);
        builder.classDoc = classWriter.getClassDoc();
        builder.init(classWriter);
        return builder;
    }
    public static MemberSummaryBuilder getInstance(
            AnnotationTypeWriter annotationTypeWriter, Configuration configuration)
            throws Exception {
        MemberSummaryBuilder builder = new MemberSummaryBuilder(configuration);
        builder.classDoc = annotationTypeWriter.getAnnotationTypeDoc();
        builder.init(annotationTypeWriter);
        return builder;
    }
    private void init(Object writer) throws Exception {
        visibleMemberMaps =
                new VisibleMemberMap[VisibleMemberMap.NUM_MEMBER_TYPES];
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            visibleMemberMaps[i] =
                    new VisibleMemberMap(
                    classDoc,
                    i,
                    configuration.nodeprecated);
        }
        memberSummaryWriters =
                new MemberSummaryWriter[VisibleMemberMap.NUM_MEMBER_TYPES];
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            if (classDoc.isAnnotationType()) {
                memberSummaryWriters[i] =
                    visibleMemberMaps[i].noVisibleMembers()?
                        null :
                        configuration.getWriterFactory().getMemberSummaryWriter(
                        (AnnotationTypeWriter) writer, i);
            } else {
                memberSummaryWriters[i] =
                    visibleMemberMaps[i].noVisibleMembers()?
                        null :
                        configuration.getWriterFactory().getMemberSummaryWriter(
                        (ClassWriter) writer, i);
            }
        }
    }
    public String getName() {
        return NAME;
    }
    public VisibleMemberMap getVisibleMemberMap(int type) {
        return visibleMemberMaps[type];
    }
    public MemberSummaryWriter getMemberSummaryWriter(int type) {
        return memberSummaryWriters[type];
    }
    public List<ProgramElementDoc> members(int type) {
        return visibleMemberMaps[type].getLeafClassMembers(configuration);
    }
    public boolean hasMembersToDocument() {
        if (classDoc instanceof AnnotationTypeDoc) {
            return ((AnnotationTypeDoc) classDoc).elements().length > 0;
        }
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            VisibleMemberMap members = visibleMemberMaps[i];
            if (!members.noVisibleMembers()) {
                return true;
            }
        }
        return false;
    }
    public void buildEnumConstantsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.ENUM_CONSTANTS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.ENUM_CONSTANTS];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }
    public void buildAnnotationTypeOptionalMemberSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }
    public void buildAnnotationTypeRequiredMemberSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }
    public void buildFieldsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.FIELDS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.FIELDS];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }
    public void buildNestedClassesSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.INNERCLASSES];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.INNERCLASSES];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }
    public void buildMethodsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.METHODS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.METHODS];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }
    public void buildConstructorsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.CONSTRUCTORS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.CONSTRUCTORS];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }
    private void buildSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap, LinkedList<Content> summaryTreeList) {
        List<ProgramElementDoc> members = new ArrayList<ProgramElementDoc>(visibleMemberMap.getLeafClassMembers(
                configuration));
        if (members.size() > 0) {
            Collections.sort(members);
            Content tableTree = writer.getSummaryTableTree(classDoc);
            for (int i = 0; i < members.size(); i++) {
                ProgramElementDoc member = members.get(i);
                Tag[] firstSentenceTags = member.firstSentenceTags();
                if (member instanceof MethodDoc && firstSentenceTags.length == 0) {
                    DocFinder.Output inheritedDoc =
                            DocFinder.search(new DocFinder.Input((MethodDoc) member));
                    if (inheritedDoc.holder != null &&
                            inheritedDoc.holder.firstSentenceTags().length > 0) {
                        firstSentenceTags = inheritedDoc.holder.firstSentenceTags();
                    }
                }
                writer.addMemberSummary(classDoc, member, firstSentenceTags, tableTree, i);
            }
            summaryTreeList.add(tableTree);
        }
    }
    private void buildInheritedSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap, LinkedList<Content> summaryTreeList) {
        for (Iterator<ClassDoc> iter = visibleMemberMap.getVisibleClassesList().iterator();
                iter.hasNext();) {
            ClassDoc inhclass = iter.next();
            if (! (inhclass.isPublic() ||
                    Util.isLinkable(inhclass, configuration))) {
                continue;
            }
            if (inhclass == classDoc) {
                continue;
            }
            List<ProgramElementDoc> inhmembers = visibleMemberMap.getMembersFor(inhclass);
            if (inhmembers.size() > 0) {
                Collections.sort(inhmembers);
                Content inheritedTree = writer.getInheritedSummaryHeader(inhclass);
                Content linksTree = writer.getInheritedSummaryLinksTree();
                for (int j = 0; j < inhmembers.size(); ++j) {
                    writer.addInheritedMemberSummary(
                            inhclass.isPackagePrivate() &&
                            ! Util.isLinkable(inhclass, configuration) ?
                            classDoc : inhclass,
                            inhmembers.get(j),
                            j == 0,
                            j == inhmembers.size() - 1, linksTree);
                }
                inheritedTree.addContent(linksTree);
                summaryTreeList.add(writer.getMemberTree(inheritedTree));
            }
        }
    }
    private void addSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap, boolean showInheritedSummary,
            Content memberSummaryTree) {
        LinkedList<Content> summaryTreeList = new LinkedList<Content>();
        buildSummary(writer, visibleMemberMap, summaryTreeList);
        if (showInheritedSummary)
            buildInheritedSummary(writer, visibleMemberMap, summaryTreeList);
        if (!summaryTreeList.isEmpty()) {
            Content memberTree = writer.getMemberSummaryHeader(
                    classDoc, memberSummaryTree);
            for (int i = 0; i < summaryTreeList.size(); i++) {
                memberTree.addContent(summaryTreeList.get(i));
            }
            memberSummaryTree.addContent(writer.getMemberTree(memberTree));
        }
    }
}
