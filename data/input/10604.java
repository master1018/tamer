public class ConstantsSummaryBuilder extends AbstractBuilder {
    public static final String ROOT = "ConstantSummary";
    public static final int MAX_CONSTANT_VALUE_INDEX_LENGTH = 2;
    protected ConstantsSummaryWriter writer;
    protected Set<ClassDoc> classDocsWithConstFields;
    protected Set<String> printedPackageHeaders;
    private PackageDoc currentPackage;
    private ClassDoc currentClass;
    private Content contentTree;
    private ConstantsSummaryBuilder(Configuration configuration) {
        super(configuration);
    }
    public static ConstantsSummaryBuilder getInstance(
        Configuration configuration, ConstantsSummaryWriter writer) {
        ConstantsSummaryBuilder builder = new ConstantsSummaryBuilder(
            configuration);
        builder.writer = writer;
        builder.classDocsWithConstFields = new HashSet<ClassDoc>();
        return builder;
    }
    public void build() throws IOException {
        if (writer == null) {
            return;
        }
        build(LayoutParser.getInstance(configuration).parseXML(ROOT), contentTree);
    }
    public String getName() {
        return ROOT;
    }
    public void buildConstantSummary(XMLNode node, Content contentTree) throws Exception {
        contentTree = writer.getHeader();
        buildChildren(node, contentTree);
        writer.addFooter(contentTree);
        writer.printDocument(contentTree);
        writer.close();
    }
    public void buildContents(XMLNode node, Content contentTree) {
        Content contentListTree = writer.getContentsHeader();
        PackageDoc[] packages = configuration.packages;
        printedPackageHeaders = new HashSet<String>();
        for (int i = 0; i < packages.length; i++) {
            if (hasConstantField(packages[i]) && ! hasPrintedPackageIndex(packages[i].name())) {
                writer.addLinkToPackageContent(packages[i],
                    parsePackageName(packages[i].name()),
                    printedPackageHeaders, contentListTree);
            }
        }
        contentTree.addContent(writer.getContentsList(contentListTree));
    }
    public void buildConstantSummaries(XMLNode node, Content contentTree) {
        PackageDoc[] packages = configuration.packages;
        printedPackageHeaders = new HashSet<String>();
        Content summariesTree = writer.getConstantSummaries();
        for (int i = 0; i < packages.length; i++) {
            if (hasConstantField(packages[i])) {
                currentPackage = packages[i];
                buildChildren(node, summariesTree);
            }
        }
        contentTree.addContent(summariesTree);
    }
    public void buildPackageHeader(XMLNode node, Content summariesTree) {
        String parsedPackageName = parsePackageName(currentPackage.name());
        if (! printedPackageHeaders.contains(parsedPackageName)) {
            writer.addPackageName(currentPackage,
                parsePackageName(currentPackage.name()), summariesTree);
            printedPackageHeaders.add(parsedPackageName);
        }
    }
    public void buildClassConstantSummary(XMLNode node, Content summariesTree) {
        ClassDoc[] classes = currentPackage.name().length() > 0 ?
            currentPackage.allClasses() :
            configuration.classDocCatalog.allClasses(
                DocletConstants.DEFAULT_PACKAGE_NAME);
        Arrays.sort(classes);
        Content classConstantTree = writer.getClassConstantHeader();
        for (int i = 0; i < classes.length; i++) {
            if (! classDocsWithConstFields.contains(classes[i]) ||
                ! classes[i].isIncluded()) {
                continue;
            }
            currentClass = classes[i];
            buildChildren(node, classConstantTree);
        }
        summariesTree.addContent(classConstantTree);
    }
    public void buildConstantMembers(XMLNode node, Content classConstantTree) {
        new ConstantFieldBuilder(currentClass).buildMembersSummary(node, classConstantTree);
    }
    private boolean hasConstantField(PackageDoc pkg) {
        ClassDoc[] classes;
        if (pkg.name().length() > 0) {
            classes = pkg.allClasses();
        } else {
            classes = configuration.classDocCatalog.allClasses(
                DocletConstants.DEFAULT_PACKAGE_NAME);
        }
        boolean found = false;
        for (int j = 0; j < classes.length; j++){
            if (classes[j].isIncluded() && hasConstantField(classes[j])) {
                found = true;
            }
        }
        return found;
    }
    private boolean hasConstantField (ClassDoc classDoc) {
        VisibleMemberMap visibleMemberMapFields = new VisibleMemberMap(classDoc,
            VisibleMemberMap.FIELDS, configuration.nodeprecated);
        List<?> fields = visibleMemberMapFields.getLeafClassMembers(configuration);
        for (Iterator<?> iter = fields.iterator(); iter.hasNext(); ) {
            FieldDoc field = (FieldDoc) iter.next();
            if (field.constantValueExpression() != null) {
                classDocsWithConstFields.add(classDoc);
                return true;
            }
        }
        return false;
    }
    private boolean hasPrintedPackageIndex(String pkgname) {
        String[] list = printedPackageHeaders.toArray(new String[] {});
        for (int i = 0; i < list.length; i++) {
            if (pkgname.startsWith(list[i])) {
                return true;
            }
        }
        return false;
    }
    private class ConstantFieldBuilder {
        protected VisibleMemberMap visibleMemberMapFields = null;
        protected VisibleMemberMap visibleMemberMapEnumConst = null;
        protected ClassDoc classdoc;
        public ConstantFieldBuilder(ClassDoc classdoc) {
            this.classdoc = classdoc;
            visibleMemberMapFields = new VisibleMemberMap(classdoc,
                VisibleMemberMap.FIELDS, configuration.nodeprecated);
            visibleMemberMapEnumConst = new VisibleMemberMap(classdoc,
                VisibleMemberMap.ENUM_CONSTANTS, configuration.nodeprecated);
        }
        protected void buildMembersSummary(XMLNode node, Content classConstantTree) {
            List<FieldDoc> members = new ArrayList<FieldDoc>(members());
            if (members.size() > 0) {
                Collections.sort(members);
                writer.addConstantMembers(classdoc, members, classConstantTree);
            }
        }
        protected List<FieldDoc> members() {
            List<ProgramElementDoc> l = visibleMemberMapFields.getLeafClassMembers(configuration);
            l.addAll(visibleMemberMapEnumConst.getLeafClassMembers(configuration));
            Iterator<ProgramElementDoc> iter;
            if(l != null){
                iter = l.iterator();
            } else {
                return null;
            }
            List<FieldDoc> inclList = new LinkedList<FieldDoc>();
            FieldDoc member;
            while(iter.hasNext()){
                member = (FieldDoc)iter.next();
                if(member.constantValue() != null){
                    inclList.add(member);
                }
            }
            return inclList;
        }
    }
    private String parsePackageName(String pkgname) {
        int index = -1;
        for (int j = 0; j < MAX_CONSTANT_VALUE_INDEX_LENGTH; j++) {
            index = pkgname.indexOf(".", index + 1);
        }
        if (index != -1) {
            pkgname = pkgname.substring(0, index);
        }
        return pkgname;
    }
}
