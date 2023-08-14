public class JavacFiler implements Filer, Closeable {
    private static final String ALREADY_OPENED =
        "Output stream or writer has already been opened.";
    private static final String NOT_FOR_READING =
        "FileObject was not opened for reading.";
    private static final String NOT_FOR_WRITING =
        "FileObject was not opened for writing.";
    private class FilerOutputFileObject extends ForwardingFileObject<FileObject> {
        private boolean opened = false;
        private String name;
        FilerOutputFileObject(String name, FileObject fileObject) {
            super(fileObject);
            this.name = name;
        }
        @Override
        public synchronized OutputStream openOutputStream() throws IOException {
            if (opened)
                throw new IOException(ALREADY_OPENED);
            opened = true;
            return new FilerOutputStream(name, fileObject);
        }
        @Override
        public synchronized Writer openWriter() throws IOException {
            if (opened)
                throw new IOException(ALREADY_OPENED);
            opened = true;
            return new FilerWriter(name, fileObject);
        }
        @Override
        public InputStream openInputStream() throws IOException {
            throw new IllegalStateException(NOT_FOR_READING);
        }
        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            throw new IllegalStateException(NOT_FOR_READING);
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throw new IllegalStateException(NOT_FOR_READING);
        }
        @Override
        public boolean delete() {
            return false;
        }
    }
    private class FilerOutputJavaFileObject extends FilerOutputFileObject implements JavaFileObject {
        private final JavaFileObject javaFileObject;
        FilerOutputJavaFileObject(String name, JavaFileObject javaFileObject) {
            super(name, javaFileObject);
            this.javaFileObject = javaFileObject;
        }
        public JavaFileObject.Kind getKind() {
            return javaFileObject.getKind();
        }
        public boolean isNameCompatible(String simpleName,
                                        JavaFileObject.Kind kind) {
            return javaFileObject.isNameCompatible(simpleName, kind);
        }
        public NestingKind getNestingKind() {
            return javaFileObject.getNestingKind();
        }
        public Modifier getAccessLevel() {
            return javaFileObject.getAccessLevel();
        }
    }
    private class FilerInputFileObject extends ForwardingFileObject<FileObject> {
        FilerInputFileObject(FileObject fileObject) {
            super(fileObject);
        }
        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new IllegalStateException(NOT_FOR_WRITING);
        }
        @Override
        public Writer openWriter() throws IOException {
            throw new IllegalStateException(NOT_FOR_WRITING);
        }
        @Override
        public boolean delete() {
            return false;
        }
    }
    private class FilerInputJavaFileObject extends FilerInputFileObject implements JavaFileObject {
        private final JavaFileObject javaFileObject;
        FilerInputJavaFileObject(JavaFileObject javaFileObject) {
            super(javaFileObject);
            this.javaFileObject = javaFileObject;
        }
        public JavaFileObject.Kind getKind() {
            return javaFileObject.getKind();
        }
        public boolean isNameCompatible(String simpleName,
                                        JavaFileObject.Kind kind) {
            return javaFileObject.isNameCompatible(simpleName, kind);
        }
        public NestingKind getNestingKind() {
            return javaFileObject.getNestingKind();
        }
        public Modifier getAccessLevel() {
            return javaFileObject.getAccessLevel();
        }
    }
    private class FilerOutputStream extends FilterOutputStream {
        String typeName;
        FileObject fileObject;
        boolean closed = false;
        FilerOutputStream(String typeName, FileObject fileObject) throws IOException {
            super(fileObject.openOutputStream());
            this.typeName = typeName;
            this.fileObject = fileObject;
        }
        public synchronized void close() throws IOException {
            if (!closed) {
                closed = true;
                closeFileObject(typeName, fileObject);
                out.close();
            }
        }
    }
    private class FilerWriter extends FilterWriter {
        String typeName;
        FileObject fileObject;
        boolean closed = false;
        FilerWriter(String typeName, FileObject fileObject) throws IOException {
            super(fileObject.openWriter());
            this.typeName = typeName;
            this.fileObject = fileObject;
        }
        public synchronized void close() throws IOException {
            if (!closed) {
                closed = true;
                closeFileObject(typeName, fileObject);
                out.close();
            }
        }
    }
    JavaFileManager fileManager;
    Log log;
    Context context;
    boolean lastRound;
    private final boolean lint;
    private final Set<FileObject> fileObjectHistory;
    private final Set<String> openTypeNames;
    private Set<String> generatedSourceNames;
    private final Map<String, JavaFileObject> generatedClasses;
    private Set<JavaFileObject> generatedSourceFileObjects;
    private final Set<String> aggregateGeneratedSourceNames;
    private final Set<String> aggregateGeneratedClassNames;
    JavacFiler(Context context) {
        this.context = context;
        fileManager = context.get(JavaFileManager.class);
        log = Log.instance(context);
        fileObjectHistory = synchronizedSet(new LinkedHashSet<FileObject>());
        generatedSourceNames = synchronizedSet(new LinkedHashSet<String>());
        generatedSourceFileObjects = synchronizedSet(new LinkedHashSet<JavaFileObject>());
        generatedClasses = synchronizedMap(new LinkedHashMap<String, JavaFileObject>());
        openTypeNames  = synchronizedSet(new LinkedHashSet<String>());
        aggregateGeneratedSourceNames = new LinkedHashSet<String>();
        aggregateGeneratedClassNames  = new LinkedHashSet<String>();
        lint = (Lint.instance(context)).isEnabled(PROCESSING);
    }
    public JavaFileObject createSourceFile(CharSequence name,
                                           Element... originatingElements) throws IOException {
        return createSourceOrClassFile(true, name.toString());
    }
    public JavaFileObject createClassFile(CharSequence name,
                                           Element... originatingElements) throws IOException {
        return createSourceOrClassFile(false, name.toString());
    }
    private JavaFileObject createSourceOrClassFile(boolean isSourceFile, String name) throws IOException {
        if (lint) {
            int periodIndex = name.lastIndexOf(".");
            if (periodIndex != -1) {
                String base = name.substring(periodIndex);
                String extn = (isSourceFile ? ".java" : ".class");
                if (base.equals(extn))
                    log.warning("proc.suspicious.class.name", name, extn);
            }
        }
        checkNameAndExistence(name, isSourceFile);
        Location loc = (isSourceFile ? SOURCE_OUTPUT : CLASS_OUTPUT);
        JavaFileObject.Kind kind = (isSourceFile ?
                                    JavaFileObject.Kind.SOURCE :
                                    JavaFileObject.Kind.CLASS);
        JavaFileObject fileObject =
            fileManager.getJavaFileForOutput(loc, name, kind, null);
        checkFileReopening(fileObject, true);
        if (lastRound)
            log.warning("proc.file.create.last.round", name);
        if (isSourceFile)
            aggregateGeneratedSourceNames.add(name);
        else
            aggregateGeneratedClassNames.add(name);
        openTypeNames.add(name);
        return new FilerOutputJavaFileObject(name, fileObject);
    }
    public FileObject createResource(JavaFileManager.Location location,
                                     CharSequence pkg,
                                     CharSequence relativeName,
                                     Element... originatingElements) throws IOException {
        locationCheck(location);
        String strPkg = pkg.toString();
        if (strPkg.length() > 0)
            checkName(strPkg);
        FileObject fileObject =
            fileManager.getFileForOutput(location, strPkg,
                                         relativeName.toString(), null);
        checkFileReopening(fileObject, true);
        if (fileObject instanceof JavaFileObject)
            return new FilerOutputJavaFileObject(null, (JavaFileObject)fileObject);
        else
            return new FilerOutputFileObject(null, fileObject);
    }
    private void locationCheck(JavaFileManager.Location location) {
        if (location instanceof StandardLocation) {
            StandardLocation stdLoc = (StandardLocation) location;
            if (!stdLoc.isOutputLocation())
                throw new IllegalArgumentException("Resource creation not supported in location " +
                                                   stdLoc);
        }
    }
    public FileObject getResource(JavaFileManager.Location location,
                                  CharSequence pkg,
                                  CharSequence relativeName) throws IOException {
        String strPkg = pkg.toString();
        if (strPkg.length() > 0)
            checkName(strPkg);
        FileObject fileObject = fileManager.getFileForInput(location,
                    pkg.toString(),
                    relativeName.toString());
        if (fileObject == null) {
            String name = (pkg.length() == 0)
                    ? relativeName.toString() : (pkg + "/" + relativeName);
            throw new FileNotFoundException(name);
        }
        checkFileReopening(fileObject, false);
        return new FilerInputFileObject(fileObject);
    }
    private void checkName(String name) throws FilerException {
        checkName(name, false);
    }
    private void checkName(String name, boolean allowUnnamedPackageInfo) throws FilerException {
        if (!SourceVersion.isName(name) && !isPackageInfo(name, allowUnnamedPackageInfo)) {
            if (lint)
                log.warning("proc.illegal.file.name", name);
            throw new FilerException("Illegal name " + name);
        }
    }
    private boolean isPackageInfo(String name, boolean allowUnnamedPackageInfo) {
        final String PKG_INFO = "package-info";
        int periodIndex = name.lastIndexOf(".");
        if (periodIndex == -1) {
            return allowUnnamedPackageInfo ? name.equals(PKG_INFO) : false;
        } else {
            String prefix = name.substring(0, periodIndex);
            String simple = name.substring(periodIndex+1);
            return SourceVersion.isName(prefix) && simple.equals(PKG_INFO);
        }
    }
    private void checkNameAndExistence(String typename, boolean allowUnnamedPackageInfo) throws FilerException {
        checkName(typename, allowUnnamedPackageInfo);
        if (aggregateGeneratedSourceNames.contains(typename) ||
            aggregateGeneratedClassNames.contains(typename)) {
            if (lint)
                log.warning("proc.type.recreate", typename);
            throw new FilerException("Attempt to recreate a file for type " + typename);
        }
    }
    private void checkFileReopening(FileObject fileObject, boolean addToHistory) throws FilerException {
        for(FileObject veteran : fileObjectHistory) {
            if (fileManager.isSameFile(veteran, fileObject)) {
                if (lint)
                    log.warning("proc.file.reopening", fileObject.getName());
                throw new FilerException("Attempt to reopen a file for path " + fileObject.getName());
            }
        }
        if (addToHistory)
            fileObjectHistory.add(fileObject);
    }
    public boolean newFiles() {
        return (!generatedSourceNames.isEmpty())
            || (!generatedClasses.isEmpty());
    }
    public Set<String> getGeneratedSourceNames() {
        return generatedSourceNames;
    }
    public Set<JavaFileObject> getGeneratedSourceFileObjects() {
        return generatedSourceFileObjects;
    }
    public Map<String, JavaFileObject> getGeneratedClasses() {
        return generatedClasses;
    }
    public void warnIfUnclosedFiles() {
        if (!openTypeNames.isEmpty())
            log.warning("proc.unclosed.type.files", openTypeNames.toString());
    }
    public void newRound(Context context) {
        this.context = context;
        this.log = Log.instance(context);
        clearRoundState();
    }
    void setLastRound(boolean lastRound) {
        this.lastRound = lastRound;
    }
    public void close() {
        clearRoundState();
        fileObjectHistory.clear();
        openTypeNames.clear();
        aggregateGeneratedSourceNames.clear();
        aggregateGeneratedClassNames.clear();
    }
    private void clearRoundState() {
        generatedSourceNames.clear();
        generatedSourceFileObjects.clear();
        generatedClasses.clear();
    }
    public void displayState() {
        PrintWriter xout = context.get(Log.outKey);
        xout.println("File Object History : " +  fileObjectHistory);
        xout.println("Open Type Names     : " +  openTypeNames);
        xout.println("Gen. Src Names      : " +  generatedSourceNames);
        xout.println("Gen. Cls Names      : " +  generatedClasses.keySet());
        xout.println("Agg. Gen. Src Names : " +  aggregateGeneratedSourceNames);
        xout.println("Agg. Gen. Cls Names : " +  aggregateGeneratedClassNames);
    }
    public String toString() {
        return "javac Filer";
    }
    private void closeFileObject(String typeName, FileObject fileObject) {
        if ((typeName != null)) {
            if (!(fileObject instanceof JavaFileObject))
                throw new AssertionError("JavaFileOject not found for " + fileObject);
            JavaFileObject javaFileObject = (JavaFileObject)fileObject;
            switch(javaFileObject.getKind()) {
            case SOURCE:
                generatedSourceNames.add(typeName);
                generatedSourceFileObjects.add(javaFileObject);
                openTypeNames.remove(typeName);
                break;
            case CLASS:
                generatedClasses.put(typeName, javaFileObject);
                openTypeNames.remove(typeName);
                break;
            default:
                break;
            }
        }
    }
}
