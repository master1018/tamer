public class PackageListWriter extends PrintWriter {
    private Configuration configuration;
    public PackageListWriter(Configuration configuration) throws IOException {
        super(Util.genWriter(configuration, configuration.destDirName,
            DocletConstants.PACKAGE_LIST_FILE_NAME, configuration.docencoding));
        this.configuration = configuration;
    }
    public static void generate(Configuration configuration) {
        PackageListWriter packgen;
        try {
            packgen = new PackageListWriter(configuration);
            packgen.generatePackageListFile(configuration.root);
            packgen.close();
        } catch (IOException exc) {
            configuration.message.error("doclet.exception_encountered",
                exc.toString(), DocletConstants.PACKAGE_LIST_FILE_NAME);
            throw new DocletAbortException();
        }
    }
    protected void generatePackageListFile(RootDoc root) {
        PackageDoc[] packages = configuration.packages;
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < packages.length; i++) {
            if (!(configuration.nodeprecated && Util.isDeprecated(packages[i])))
                names.add(packages[i].name());
        }
        Collections.sort(names);
        for (int i = 0; i < names.size(); i++) {
            println(names.get(i));
        }
    }
}
