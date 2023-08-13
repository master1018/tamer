public class Touch implements AnnotationProcessorFactory {
    static class TouchProc implements AnnotationProcessor {
        AnnotationProcessorEnvironment ape;
        TouchProc(AnnotationProcessorEnvironment ape) {
            this.ape = ape;
        }
        public void process() {
            boolean result;
            Collection<TypeDeclaration> tdecls = ape.getSpecifiedTypeDeclarations();
            if (tdecls.size() == 1) {
                for(TypeDeclaration decl: tdecls) {
                    if (! decl.getSimpleName().equals("Touch") )
                        return;
                }
                try {
                    java.io.File f = new java.io.File("touched");
                    result = f.createNewFile();
                    Filer filer = ape.getFiler();
                    PrintWriter pw = filer.createSourceFile("HelloWorld");
                    pw.println("public class HelloWorld {");
                    pw.println("  public static void main(String argv[]) {");
                    pw.println("    System.out.println(\"Hello World\");");
                    pw.println("  }");
                    pw.println("}");
                    OutputStream os = filer.createClassFile("Empty");
                    FileInputStream fis = new FileInputStream("Empty.clazz");
                    int datum;
                    while((datum = fis.read()) != -1)
                        os.write(datum);
                } catch (java.io.IOException e) {
                    result = false;
                }
                if (!result)
                    throw new RuntimeException("touched file already exists or other error");
            }
        }
    }
    static Collection<String> supportedTypes;
    static {
        String types[] = {"*"};
        supportedTypes = Collections.unmodifiableCollection(Arrays.asList(types));
    }
    static Collection<String> supportedOptions;
    static {
        String options[] = {""};
        supportedOptions = Collections.unmodifiableCollection(Arrays.asList(options));
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    public Collection<String> supportedAnnotationTypes() {
        return supportedTypes;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                        AnnotationProcessorEnvironment env) {
        return new TouchProc(env);
    }
}
