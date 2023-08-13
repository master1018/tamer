public class RootDocImpl extends DocImpl implements RootDoc {
    private List<ClassDocImpl> cmdLineClasses;
    private List<PackageDocImpl> cmdLinePackages;
    private List<String[]> options;
    public RootDocImpl(DocEnv env, List<JCClassDecl> classes, List<String> packages, List<String[]> options) {
        super(env, null);
        this.options = options;
        setPackages(env, packages);
        setClasses(env, classes);
    }
    public RootDocImpl(DocEnv env, List<String> classes, List<String[]> options) {
        super(env, null);
        this.options = options;
        cmdLinePackages = List.nil();
        ListBuffer<ClassDocImpl> classList = new ListBuffer<ClassDocImpl>();
        for (String className : classes) {
            ClassDocImpl c = env.loadClass(className);
            if (c == null)
                env.error(null, "javadoc.class_not_found", className);
            else
                classList = classList.append(c);
        }
        cmdLineClasses = classList.toList();
    }
    private void setClasses(DocEnv env, List<JCClassDecl> classes) {
        ListBuffer<ClassDocImpl> result = new ListBuffer<ClassDocImpl>();
        for (JCClassDecl def : classes) {
            if (env.shouldDocument(def.sym)) {
                ClassDocImpl cd = env.getClassDoc(def.sym);
                if (cd != null) {
                    cd.isIncluded = true;
                    result.append(cd);
                } 
            } 
        }
        cmdLineClasses = result.toList();
    }
    private void setPackages(DocEnv env, List<String> packages) {
        ListBuffer<PackageDocImpl> packlist = new ListBuffer<PackageDocImpl>();
        for (String name : packages) {
            PackageDocImpl pkg = env.lookupPackage(name);
            if (pkg != null) {
                pkg.isIncluded = true;
                packlist.append(pkg);
            } else {
                env.warning(null, "main.no_source_files_for_package", name);
            }
        }
        cmdLinePackages = packlist.toList();
    }
    public String[][] options() {
        return options.toArray(new String[options.length()][]);
    }
    public PackageDoc[] specifiedPackages() {
        return (PackageDoc[])cmdLinePackages
            .toArray(new PackageDocImpl[cmdLinePackages.length()]);
    }
    public ClassDoc[] specifiedClasses() {
        ListBuffer<ClassDocImpl> classesToDocument = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl cd : cmdLineClasses) {
            cd.addAllClasses(classesToDocument, true);
        }
        return (ClassDoc[])classesToDocument.toArray(new ClassDocImpl[classesToDocument.length()]);
    }
    public ClassDoc[] classes() {
        ListBuffer<ClassDocImpl> classesToDocument = new ListBuffer<ClassDocImpl>();
        for (ClassDocImpl cd : cmdLineClasses) {
            cd.addAllClasses(classesToDocument, true);
        }
        for (PackageDocImpl pd : cmdLinePackages) {
            pd.addAllClassesTo(classesToDocument);
        }
        return classesToDocument.toArray(new ClassDocImpl[classesToDocument.length()]);
    }
    public ClassDoc classNamed(String qualifiedName) {
        return env.lookupClass(qualifiedName);
    }
    public PackageDoc packageNamed(String name) {
        return env.lookupPackage(name);
    }
    public String name() {
        return "*RootDocImpl*";
    }
    public String qualifiedName() {
        return "*RootDocImpl*";
    }
    public boolean isIncluded() {
        return false;
    }
    public void printError(String msg) {
        env.printError(msg);
    }
    public void printError(SourcePosition pos, String msg) {
        env.printError(pos, msg);
    }
    public void printWarning(String msg) {
        env.printWarning(msg);
    }
    public void printWarning(SourcePosition pos, String msg) {
        env.printWarning(pos, msg);
    }
    public void printNotice(String msg) {
        env.printNotice(msg);
    }
    public void printNotice(SourcePosition pos, String msg) {
        env.printNotice(pos, msg);
    }
    private JavaFileObject getOverviewPath() {
        for (String[] opt : options) {
            if (opt[0].equals("-overview")) {
                if (env.fileManager instanceof StandardJavaFileManager) {
                    StandardJavaFileManager fm = (StandardJavaFileManager) env.fileManager;
                    return fm.getJavaFileObjects(opt[1]).iterator().next();
                }
            }
        }
        return null;
    }
    @Override
    protected String documentation() {
        if (documentation == null) {
            int cnt = options.length();
            JavaFileObject overviewPath = getOverviewPath();
            if (overviewPath == null) {
                documentation = "";
            } else {
                try {
                    documentation = readHTMLDocumentation(
                        overviewPath.openInputStream(),
                        overviewPath);
                } catch (IOException exc) {
                    documentation = "";
                    env.error(null, "javadoc.File_Read_Error", overviewPath.getName());
                }
            }
        }
        return documentation;
    }
    @Override
    public SourcePosition position() {
        JavaFileObject path;
        return ((path = getOverviewPath()) == null) ?
            null :
            SourcePositionImpl.make(path, Position.NOPOS, null);
    }
    public Locale getLocale() {
        return env.doclocale.locale;
    }
}
