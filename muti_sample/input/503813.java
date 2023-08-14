public class JavaSourceToDexUtil {
    public dex.structure.DexFile getFrom(JavaSource source) throws IOException{
        return getAllFrom(Collections.singleton(source));
    }
    public dex.structure.DexFile getFrom(JavaSource... source) throws IOException{
        return getAllFrom(new HashSet<JavaSource>(Arrays.asList(source)));
    }
    public dex.structure.DexFile getAllFrom(Set<JavaSource> sources) throws IOException{
        return getFrom(sources, null);
    }
    public dex.structure.DexFile getFrom(Set<JavaSource> sources,
            Set<String> classesToDex) throws IOException {
        Set<MemoryByteCode> byteCodeInMemory = compileToByteCode(sources);
        byte[] dexCode = convertToDexCode(byteCodeInMemory, classesToDex);
        DexBuffer dexBuffer = new DexBuffer(dexCode);
        DexFileReader reader = new DexFileReader();
        return reader.read(dexBuffer);
    }
    private byte[] convertToDexCode(Set<MemoryByteCode> byteCodeInMemory, Set<String> classNamesToDex) throws IOException {
        CfOptions cfOptions = new CfOptions();
        DexFile dexFile = new DexFile();
        for (MemoryByteCode memoryByteCode : byteCodeInMemory) {
            if(classNamesToDex == null || classNamesToDex.contains(memoryByteCode.getName())) {
            ClassDefItem classDefItem = CfTranslator.translate(memoryByteCode.getName().replace('.', '/') +".class", memoryByteCode.getBytes(), cfOptions);
            dexFile.add(classDefItem);
            }
        }
        return dexFile.toDex(null, false);
    }
    public Set<MemoryByteCode> compileToByteCode(Set<JavaSource> source) {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diacol = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager sjfm = javac.getStandardFileManager(diacol,
                null, null);
        SpecialJavaFileManager xfm = new SpecialJavaFileManager(sjfm);
        CompilationTask compile = javac.getTask(null, xfm, diacol, Arrays
                .asList(new String[] {"-classpath", "."}), null, source);
        boolean success = compile.call();
        if(!success) {
        StringBuilder errorMessage = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> dia : diacol.getDiagnostics()) {
                errorMessage.append(dia);
            }
            throw new IllegalStateException(errorMessage.toString());
        }
        return xfm.getAllMemoryByteCodes();
    }
}
