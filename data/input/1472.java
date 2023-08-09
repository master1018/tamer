public class FilerImpl implements Filer {
    private enum FileKind {
        SOURCE {
            void register(File file, String name, FilerImpl that) throws IOException {
                if (that.filesCreated.contains(new File(that.locations.get(CLASS_TREE),
                                                        that.nameToPath(name, ".class")))) {
                    that.bark.aptWarning("CorrespondingClassFile", name);
                    if (that.opts.get("-XclassesAsDecls") != null)
                        throw new IOException();
                }
                that.sourceFileNames.add(file.getPath());
            }
        },
        CLASS  {
            void register(File file, String name, FilerImpl that) throws IOException {
                if (that.filesCreated.contains(new File(that.locations.get(SOURCE_TREE),
                                                        that.nameToPath(name, ".java")))) {
                    that.bark.aptWarning("CorrespondingSourceFile", name);
                    if (that.opts.get("-XclassesAsDecls") != null)
                        throw new IOException();
                }
                that.classFileNames.add(name);
            }
        },
        OTHER  {
            void register(File file, String name, FilerImpl that) throws IOException {}
        };
        abstract void register(File file, String name, FilerImpl that) throws IOException;
    }
    private final Options opts;
    private final DeclarationMaker declMaker;
    private final com.sun.tools.apt.main.AptJavaCompiler comp;
    private final static String DEFAULT_ENCODING =
            new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();
    private String encoding;    
    private final EnumMap<Location, File> locations;    
    private static final Context.Key<FilerImpl> filerKey =
            new Context.Key<FilerImpl>();
    private Collection<Flushable> wc;
    private Bark bark;
    private final Set<File> filesCreated;
    private HashSet<String> sourceFileNames = new HashSet<String>();
    private HashSet<String> classFileNames  = new HashSet<String>();
    private boolean roundOver;
    public static FilerImpl instance(Context context) {
        FilerImpl instance = context.get(filerKey);
        if (instance == null) {
            instance = new FilerImpl(context);
        }
        return instance;
    }
    public void flush() {
        for(Flushable opendedFile: wc) {
            try {
                opendedFile.flush();
                if (opendedFile instanceof FileOutputStream) {
                    try {
                        ((FileOutputStream) opendedFile).getFD().sync() ;
                    } catch (java.io.SyncFailedException sfe) {}
                }
            } catch (IOException e) { }
        }
    }
    private FilerImpl(Context context) {
        context.put(filerKey, this);
        opts = Options.instance(context);
        declMaker = DeclarationMaker.instance(context);
        bark = Bark.instance(context);
        comp = com.sun.tools.apt.main.AptJavaCompiler.instance(context);
        roundOver = false;
        this.filesCreated = comp.getAggregateGenFiles();
        encoding = opts.get("-encoding");
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        wc = new HashSet<Flushable>();
        locations = new EnumMap<Location, File>(Location.class);
        String s = opts.get("-s");      
        String d = opts.get("-d");      
        locations.put(SOURCE_TREE, new File(s != null ? s : "."));
        locations.put(CLASS_TREE,  new File(d != null ? d : "."));
    }
    public PrintWriter createSourceFile(String name) throws IOException {
        String pathname = nameToPath(name, ".java");
        File file = new File(locations.get(SOURCE_TREE),
                             pathname);
        PrintWriter pw = getPrintWriter(file, encoding, name, FileKind.SOURCE);
        return pw;
    }
    public OutputStream createClassFile(String name) throws IOException {
        String pathname = nameToPath(name, ".class");
        File file = new File(locations.get(CLASS_TREE),
                             pathname);
        OutputStream os = getOutputStream(file, name, FileKind.CLASS);
        return os;
    }
    public PrintWriter createTextFile(Location loc,
                                      String pkg,
                                      File relPath,
                                      String charsetName) throws IOException {
        File file = (pkg.length() == 0)
                        ? relPath
                        : new File(nameToPath(pkg), relPath.getPath());
        if (charsetName == null) {
            charsetName = encoding;
        }
        return getPrintWriter(loc, file.getPath(), charsetName, null, FileKind.OTHER);
    }
    public OutputStream createBinaryFile(Location loc,
                                         String pkg,
                                         File relPath) throws IOException {
        File file = (pkg.length() == 0)
                        ? relPath
                        : new File(nameToPath(pkg), relPath.getPath());
        return getOutputStream(loc, file.getPath(), null, FileKind.OTHER);
    }
    private String nameToPath(String name, String suffix) throws IOException {
        if (!DeclarationMaker.isJavaIdentifier(name.replace('.', '_'))) {
            bark.aptWarning("IllegalFileName", name);
            throw new IOException();
        }
        return name.replace('.', File.separatorChar) + suffix;
    }
    private String nameToPath(String name) throws IOException {
        return nameToPath(name, "");
    }
    private PrintWriter getPrintWriter(Location loc, String pathname,
                                       String encoding, String name, FileKind kind) throws IOException {
        File file = new File(locations.get(loc), pathname);
        return getPrintWriter(file, encoding, name, kind);
    }
    private PrintWriter getPrintWriter(File file,
                                       String encoding, String name, FileKind kind) throws IOException {
        prepareFile(file, name, kind);
        PrintWriter pw =
            new PrintWriter(
                    new BufferedWriter(
                         new OutputStreamWriter(new FileOutputStream(file),
                                                encoding)));
        wc.add(pw);
        return pw;
    }
    private OutputStream getOutputStream(Location loc, String pathname, String name, FileKind kind)
                                                        throws IOException {
        File file = new File(locations.get(loc), pathname);
        return getOutputStream(file, name, kind);
    }
    private OutputStream getOutputStream(File file, String name, FileKind kind) throws IOException {
        prepareFile(file, name, kind);
        OutputStream os = new FileOutputStream(file);
        wc.add(os);
        return os;
    }
    public Set<String> getSourceFileNames() {
        return sourceFileNames;
    }
    public Set<String> getClassFileNames() {
        return classFileNames;
    }
    public void roundOver() {
        roundOver = true;
    }
    private void prepareFile(File file, String name, FileKind kind) throws IOException {
        if (roundOver) {
            bark.aptWarning("NoNewFilesAfterRound", file.toString());
            throw new IOException();
        }
        if (filesCreated.contains(file)) {
            bark.aptWarning("FileReopening", file.toString());
            throw new IOException();
        } else {
            if (file.exists()) {
                file.delete();
            } else {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    if(!parent.mkdirs()) {
                        bark.aptWarning("BadParentDirectory", file.toString());
                        throw new IOException();
                    }
                }
            }
            kind.register(file, name, this);
            filesCreated.add(file);
        }
    }
}
