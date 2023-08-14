public abstract class AbstractDoclet {
    public Configuration configuration;
    private static final String TOOLKIT_DOCLET_NAME = new
        com.sun.tools.doclets.formats.html.HtmlDoclet().getClass().getName();
    private boolean isValidDoclet(AbstractDoclet doclet) {
        if (! doclet.getClass().getName().equals(TOOLKIT_DOCLET_NAME)) {
            configuration.message.error("doclet.Toolkit_Usage_Violation",
                TOOLKIT_DOCLET_NAME);
            return false;
        }
        return true;
    }
    public boolean start(AbstractDoclet doclet, RootDoc root) {
        configuration = configuration();
        configuration.root = root;
        if (! isValidDoclet(doclet)) {
            return false;
        }
        try {
            doclet.startGeneration(root);
        } catch (Exception exc) {
            exc.printStackTrace();
            return false;
        }
        return true;
    }
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
    public abstract Configuration configuration();
    private void startGeneration(RootDoc root) throws Exception {
        if (root.classes().length == 0) {
            configuration.message.
                error("doclet.No_Public_Classes_To_Document");
            return;
        }
        configuration.setOptions();
        configuration.getDocletSpecificMsg().notice("doclet.build_version",
            configuration.getDocletSpecificBuildDate());
        ClassTree classtree = new ClassTree(configuration, configuration.nodeprecated);
        generateClassFiles(root, classtree);
        if (configuration.sourcepath != null && configuration.sourcepath.length() > 0) {
            StringTokenizer pathTokens = new StringTokenizer(configuration.sourcepath,
                String.valueOf(File.pathSeparatorChar));
            boolean first = true;
            while(pathTokens.hasMoreTokens()){
                Util.copyDocFiles(configuration,
                    pathTokens.nextToken() + File.separator,
                    DocletConstants.DOC_FILES_DIR_NAME, first);
                first = false;
            }
        }
        PackageListWriter.generate(configuration);
        generatePackageFiles(classtree);
        generateOtherFiles(root, classtree);
        configuration.tagletManager.printReport();
    }
    protected void generateOtherFiles(RootDoc root, ClassTree classtree) throws Exception {
        BuilderFactory builderFactory = configuration.getBuilderFactory();
        AbstractBuilder constantsSummaryBuilder = builderFactory.getConstantsSummaryBuider();
        constantsSummaryBuilder.build();
        AbstractBuilder serializedFormBuilder = builderFactory.getSerializedFormBuilder();
        serializedFormBuilder.build();
    }
    protected abstract void generatePackageFiles(ClassTree classtree) throws Exception;
    protected abstract void generateClassFiles(ClassDoc[] arr, ClassTree classtree);
    protected void generateClassFiles(RootDoc root, ClassTree classtree) {
        generateClassFiles(classtree);
        PackageDoc[] packages = root.specifiedPackages();
        for (int i = 0; i < packages.length; i++) {
            generateClassFiles(packages[i].allClasses(), classtree);
        }
    }
    private void generateClassFiles(ClassTree classtree) {
        String[] packageNames = configuration.classDocCatalog.packageNames();
        for (int packageNameIndex = 0; packageNameIndex < packageNames.length;
                packageNameIndex++) {
            generateClassFiles(configuration.classDocCatalog.allClasses(
                packageNames[packageNameIndex]), classtree);
        }
    }
}
