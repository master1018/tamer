public class HtmlDoclet extends AbstractDoclet {
    public HtmlDoclet() {
        configuration = (ConfigurationImpl) configuration();
    }
    public ConfigurationImpl configuration;
    public static boolean start(RootDoc root) {
        try {
            HtmlDoclet doclet = new HtmlDoclet();
            return doclet.start(doclet, root);
        } finally {
            ConfigurationImpl.reset();
        }
    }
    public Configuration configuration() {
        return ConfigurationImpl.getInstance();
    }
    protected void generateOtherFiles(RootDoc root, ClassTree classtree)
            throws Exception {
        super.generateOtherFiles(root, classtree);
        if (configuration.linksource) {
            if (configuration.destDirName.length() > 0) {
                SourceToHTMLConverter.convertRoot(configuration,
                    root, configuration.destDirName + File.separator
                    + DocletConstants.SOURCE_OUTPUT_DIR_NAME);
            } else {
                SourceToHTMLConverter.convertRoot(configuration,
                    root, DocletConstants.SOURCE_OUTPUT_DIR_NAME);
            }
        }
        if (configuration.topFile.length() == 0) {
            configuration.standardmessage.
                error("doclet.No_Non_Deprecated_Classes_To_Document");
            return;
        }
        boolean nodeprecated = configuration.nodeprecated;
        String configdestdir = configuration.destDirName;
        String confighelpfile = configuration.helpfile;
        String configstylefile = configuration.stylesheetfile;
        performCopy(configdestdir, confighelpfile);
        performCopy(configdestdir, configstylefile);
        Util.copyResourceFile(configuration, "background.gif", false);
        Util.copyResourceFile(configuration, "tab.gif", false);
        Util.copyResourceFile(configuration, "titlebar.gif", false);
        Util.copyResourceFile(configuration, "titlebar_end.gif", false);
        if (configuration.classuse) {
            ClassUseWriter.generate(configuration, classtree);
        }
        IndexBuilder indexbuilder = new IndexBuilder(configuration, nodeprecated);
        if (configuration.createtree) {
            TreeWriter.generate(configuration, classtree);
        }
        if (configuration.createindex) {
            if (configuration.splitindex) {
                SplitIndexWriter.generate(configuration, indexbuilder);
            } else {
                SingleIndexWriter.generate(configuration, indexbuilder);
            }
        }
        if (!(configuration.nodeprecatedlist || nodeprecated)) {
            DeprecatedListWriter.generate(configuration);
        }
        AllClassesFrameWriter.generate(configuration,
            new IndexBuilder(configuration, nodeprecated, true));
        FrameOutputWriter.generate(configuration);
        if (configuration.createoverview) {
            PackageIndexWriter.generate(configuration);
        }
        if (configuration.helpfile.length() == 0 &&
            !configuration.nohelp) {
            HelpWriter.generate(configuration);
        }
        if (configuration.stylesheetfile.length() == 0) {
            Util.copyFile(configuration, "stylesheet.css", Util.RESOURCESDIR,
                    (configdestdir.isEmpty()) ?
                        System.getProperty("user.dir") : configdestdir, false, true);
        }
    }
    protected void generateClassFiles(ClassDoc[] arr, ClassTree classtree) {
        Arrays.sort(arr);
        for(int i = 0; i < arr.length; i++) {
            if (!(configuration.isGeneratedDoc(arr[i]) && arr[i].isIncluded())) {
                continue;
            }
            ClassDoc prev = (i == 0)?
                null:
                arr[i-1];
            ClassDoc curr = arr[i];
            ClassDoc next = (i+1 == arr.length)?
                null:
                arr[i+1];
            try {
                if (curr.isAnnotationType()) {
                    AbstractBuilder annotationTypeBuilder =
                        configuration.getBuilderFactory()
                            .getAnnotationTypeBuilder((AnnotationTypeDoc) curr,
                                prev, next);
                    annotationTypeBuilder.build();
                } else {
                    AbstractBuilder classBuilder =
                        configuration.getBuilderFactory()
                            .getClassBuilder(curr, prev, next, classtree);
                    classBuilder.build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new DocletAbortException();
            }
        }
    }
    protected void generatePackageFiles(ClassTree classtree) throws Exception {
        PackageDoc[] packages = configuration.packages;
        if (packages.length > 1) {
            PackageIndexFrameWriter.generate(configuration);
        }
        PackageDoc prev = null, next;
        for (int i = 0; i < packages.length; i++) {
            if (!(configuration.nodeprecated && Util.isDeprecated(packages[i]))) {
                PackageFrameWriter.generate(configuration, packages[i]);
                next = (i + 1 < packages.length &&
                        packages[i + 1].name().length() > 0) ? packages[i + 1] : null;
                next = (i + 2 < packages.length && next == null) ? packages[i + 2] : next;
                AbstractBuilder packageSummaryBuilder =
                        configuration.getBuilderFactory().getPackageSummaryBuilder(
                        packages[i], prev, next);
                packageSummaryBuilder.build();
                if (configuration.createtree) {
                    PackageTreeWriter.generate(configuration,
                            packages[i], prev, next,
                            configuration.nodeprecated);
                }
                prev = packages[i];
            }
        }
    }
    public static int optionLength(String option) {
        return (ConfigurationImpl.getInstance()).optionLength(option);
    }
    public static boolean validOptions(String options[][],
            DocErrorReporter reporter) {
        return (ConfigurationImpl.getInstance()).validOptions(options, reporter);
    }
    private void performCopy(String configdestdir, String filename) {
        try {
            String destdir = (configdestdir.length() > 0) ?
                configdestdir + File.separatorChar: "";
            if (filename.length() > 0) {
                File helpstylefile = new File(filename);
                String parent = helpstylefile.getParent();
                String helpstylefilename = (parent == null)?
                    filename:
                    filename.substring(parent.length() + 1);
                File desthelpfile = new File(destdir + helpstylefilename);
                if (!desthelpfile.getCanonicalPath().equals(
                        helpstylefile.getCanonicalPath())) {
                    configuration.message.
                        notice((SourcePosition) null,
                            "doclet.Copying_File_0_To_File_1",
                            helpstylefile.toString(), desthelpfile.toString());
                    Util.copyFile(desthelpfile, helpstylefile);
                }
            }
        } catch (IOException exc) {
            configuration.message.
                error((SourcePosition) null,
                    "doclet.perform_copy_exception_encountered",
                    exc.toString());
            throw new DocletAbortException();
        }
    }
}
