public class DocletTestConverter extends signature.converter.util.AbstractTestSourceConverter {
    static String sourcepath;
    static String separator;
    static {
        separator = System.getProperty("file.separator");
        sourcepath = System.getProperty("java.io.tmpdir");
        sourcepath = sourcepath + separator + "cts" + separator;
        System.out.println(">> "+sourcepath);
    }
    public IApi convert(Visibility visibility, Set<CompilationUnit> units) throws IOException {
        try {
            Set<String> packages = new HashSet<String>();
            for(CompilationUnit u : units){
                putSource(u);
                String p = ModelUtil.getPackageName(u.getName());
                assert p.length() > 0 : "default package not supported by doclets";
                packages.add(p);
            }
            RootDoc root = getRootDoc(visibility, packages);
            DocletToSigConverter converter = new DocletToSigConverter();
            IApi api = converter.convertDocletRoot("Doclet Test", root, visibility, packages);
            return api;
        }
        finally {
            removeSources();
        }
    }
    public static void putSource(CompilationUnit source) throws IOException {
        String directory = sourcepath;
        String filename = source.getName();    
        int pos = filename.indexOf(".");
        while(pos > 0) {
            directory = directory + filename.substring(0, pos) + separator;
            filename = filename.substring(pos+1);
            pos = filename.indexOf(".");
        }
        File file = new File(directory, filename + ".java");
        File parent = file.getParentFile();
        parent.mkdirs();
        FileWriter wr = new FileWriter(file);
        wr.write(source.getSource());
        wr.close();
    }
    private static void removeSources() {
        File file = new File(sourcepath);
        removeFile(file);
    }
    private static void removeFile(File file){
        if(file.isDirectory()){
            for(File f : file.listFiles()) removeFile(f);
        }
        file.delete();
    }
    private static RootDoc getRootDoc(Visibility visibility, java.util.Set<String> packages) throws IOException  {
        long accessModifier = 0;
        if(visibility == Visibility.PUBLIC){
            accessModifier = 
                com.sun.tools.javac.code.Flags.PUBLIC;    
        }
        if(visibility == Visibility.PROTECTED){
            accessModifier = 
                com.sun.tools.javac.code.Flags.PUBLIC    
                | com.sun.tools.javac.code.Flags.PROTECTED;    
        }
        if(visibility == Visibility.PACKAGE){
            accessModifier = 
                com.sun.tools.javac.code.Flags.PUBLIC    
                | com.sun.tools.javac.code.Flags.PROTECTED    
                | com.sun.tools.javadoc.ModifierFilter.PACKAGE; 
        }
        if(visibility == Visibility.PRIVATE){
            accessModifier = 
                com.sun.tools.javac.code.Flags.PUBLIC    
                | com.sun.tools.javac.code.Flags.PROTECTED    
                | com.sun.tools.javadoc.ModifierFilter.PACKAGE 
                | com.sun.tools.javac.code.Flags.PRIVATE;    
        }
        ModifierFilter showAccess = new ModifierFilter(accessModifier);
        boolean breakiterator = false;
        boolean quiet = false;
        boolean legacy = false;
        boolean docClasses = false;
        String docLocale = "";
        String encoding = null;
        ListBuffer<String> javaNames = new ListBuffer<String>();
        for(String p : packages)
            javaNames.append(p);
        ListBuffer<String[]> options = new ListBuffer<String[]>();
        options.append(new String[]{"-sourcepath", sourcepath});
        ListBuffer<String> subPackages = new ListBuffer<String>();
        ListBuffer<String> excludedPackages = new ListBuffer<String>();
        Context context = new Context();
        Options compOpts = Options.instance(context);
        compOpts.put("-sourcepath", sourcepath);
        Constructor<Messager> c;
        try {
            c = Messager.class.getDeclaredConstructor(Context.class, String.class, PrintWriter.class, PrintWriter.class, PrintWriter.class);
            c.setAccessible(true);
            PrintWriter err = new PrintWriter(new StringWriter());
            PrintWriter warn = new PrintWriter(new StringWriter());
            PrintWriter notice = new PrintWriter(new StringWriter());
            c.newInstance(context, "SigTest", err, warn, notice);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JavadocTool comp = JavadocTool.make0(context);
        RootDocImpl root = comp.getRootDocImpl(docLocale, encoding, showAccess,
                javaNames.toList(), options.toList(), breakiterator,
                subPackages.toList(), excludedPackages.toList(), docClasses,
                legacy, quiet);
        return root;
    }
}
