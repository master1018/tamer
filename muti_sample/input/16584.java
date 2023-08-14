class JavadocClassReader extends ClassReader {
    public static JavadocClassReader instance0(Context context) {
        ClassReader instance = context.get(classReaderKey);
        if (instance == null)
            instance = new JavadocClassReader(context);
        return (JavadocClassReader)instance;
    }
    public static void preRegister(Context context) {
        context.put(classReaderKey, new Context.Factory<ClassReader>() {
            public ClassReader make(Context c) {
                return new JavadocClassReader(c);
            }
        });
    }
    private DocEnv docenv;
    private EnumSet<JavaFileObject.Kind> all = EnumSet.of(JavaFileObject.Kind.CLASS,
                                                          JavaFileObject.Kind.SOURCE,
                                                          JavaFileObject.Kind.HTML);
    private EnumSet<JavaFileObject.Kind> noSource = EnumSet.of(JavaFileObject.Kind.CLASS,
                                                               JavaFileObject.Kind.HTML);
    private JavadocClassReader(Context context) {
        super(context, true);
        docenv = DocEnv.instance(context);
        preferSource = true;
    }
    @Override
    protected EnumSet<JavaFileObject.Kind> getPackageFileKinds() {
        return docenv.docClasses ? noSource : all;
    }
    @Override
    protected void extraFileActions(PackageSymbol pack, JavaFileObject fo) {
        if (fo.isNameCompatible("package", JavaFileObject.Kind.HTML))
            docenv.getPackageDoc(pack).setDocPath(fo);
    }
}
