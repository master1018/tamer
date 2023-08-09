public class AnnotationTypeBuilder extends AbstractBuilder {
    public static final String ROOT = "AnnotationTypeDoc";
    private AnnotationTypeDoc annotationTypeDoc;
    private AnnotationTypeWriter writer;
    private Content contentTree;
    private AnnotationTypeBuilder(Configuration configuration) {
        super(configuration);
    }
    public static AnnotationTypeBuilder getInstance(Configuration configuration,
        AnnotationTypeDoc annotationTypeDoc, AnnotationTypeWriter writer)
    throws Exception {
        AnnotationTypeBuilder builder = new AnnotationTypeBuilder(configuration);
        builder.configuration = configuration;
        builder.annotationTypeDoc = annotationTypeDoc;
        builder.writer = writer;
        if(containingPackagesSeen == null) {
            containingPackagesSeen = new HashSet<String>();
        }
        return builder;
    }
    public void build() throws IOException {
        build(LayoutParser.getInstance(configuration).parseXML(ROOT), contentTree);
    }
    public String getName() {
        return ROOT;
    }
     public void buildAnnotationTypeDoc(XMLNode node, Content contentTree) throws Exception {
        contentTree = writer.getHeader(configuration.getText("doclet.AnnotationType") +
                " " + annotationTypeDoc.name());
        Content annotationContentTree = writer.getAnnotationContentHeader();
         buildChildren(node, annotationContentTree);
         contentTree.addContent(annotationContentTree);
         writer.addFooter(contentTree);
         writer.printDocument(contentTree);
         writer.close();
         copyDocFiles();
     }
     private void copyDocFiles() {
        PackageDoc containingPackage = annotationTypeDoc.containingPackage();
        if((configuration.packages == null ||
                Arrays.binarySearch(configuration.packages,
                                    containingPackage) < 0) &&
           ! containingPackagesSeen.contains(containingPackage.name())){
            Util.copyDocFiles(configuration,
                Util.getPackageSourcePath(configuration,
                    annotationTypeDoc.containingPackage()) +
                DirectoryManager.getDirectoryPath(
                    annotationTypeDoc.containingPackage())
                    + File.separator, DocletConstants.DOC_FILES_DIR_NAME, true);
            containingPackagesSeen.add(containingPackage.name());
        }
     }
    public void buildAnnotationTypeInfo(XMLNode node, Content annotationContentTree) {
        Content annotationInfoTree = writer.getAnnotationInfoTreeHeader();
        buildChildren(node, annotationInfoTree);
        annotationContentTree.addContent(writer.getAnnotationInfo(annotationInfoTree));
    }
    public void buildDeprecationInfo (XMLNode node, Content annotationInfoTree) {
        writer.addAnnotationTypeDeprecationInfo(annotationInfoTree);
    }
    public void buildAnnotationTypeSignature(XMLNode node, Content annotationInfoTree) {
        StringBuffer modifiers = new StringBuffer(
                annotationTypeDoc.modifiers() + " ");
        writer.addAnnotationTypeSignature(Util.replaceText(
                modifiers.toString(), "interface", "@interface"), annotationInfoTree);
    }
    public void buildAnnotationTypeDescription(XMLNode node, Content annotationInfoTree) {
        writer.addAnnotationTypeDescription(annotationInfoTree);
    }
    public void buildAnnotationTypeTagInfo(XMLNode node, Content annotationInfoTree) {
        writer.addAnnotationTypeTagInfo(annotationInfoTree);
    }
    public void buildMemberSummary(XMLNode node, Content annotationContentTree)
            throws Exception {
        Content memberSummaryTree = writer.getMemberTreeHeader();
        configuration.getBuilderFactory().
                getMemberSummaryBuilder(writer).buildChildren(node, memberSummaryTree);
        annotationContentTree.addContent(writer.getMemberSummaryTree(memberSummaryTree));
    }
    public void buildAnnotationTypeMemberDetails(XMLNode node, Content annotationContentTree) {
        Content memberDetailsTree = writer.getMemberTreeHeader();
        buildChildren(node, memberDetailsTree);
        if (memberDetailsTree.isValid()) {
            Content memberDetails = writer.getMemberTreeHeader();
            writer.addAnnotationDetailsMarker(memberDetails);
            memberDetails.addContent(writer.getMemberTree(memberDetailsTree));
            annotationContentTree.addContent(writer.getMemberDetailsTree(memberDetails));
        }
    }
    public void buildAnnotationTypeOptionalMemberDetails(XMLNode node, Content memberDetailsTree)
            throws Exception {
        configuration.getBuilderFactory().
                getAnnotationTypeOptionalMemberBuilder(writer).buildChildren(node, memberDetailsTree);
    }
    public void buildAnnotationTypeRequiredMemberDetails(XMLNode node, Content memberDetailsTree)
            throws Exception {
        configuration.getBuilderFactory().
                getAnnotationTypeRequiredMemberBuilder(writer).buildChildren(node, memberDetailsTree);
    }
}
