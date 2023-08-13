public class PhantomTouch implements AnnotationProcessorFactory {
    static class PhantomTouchProc implements AnnotationProcessor {
        AnnotationProcessorEnvironment ape;
        PhantomTouchProc(AnnotationProcessorEnvironment ape) {
            this.ape = ape;
        }
        public void process() {
            if (ape.getSpecifiedTypeDeclarations().size() == 0) {
                boolean result;
                try {
                    java.io.File f = new java.io.File("touched");
                    result = f.createNewFile();
                    if (result) {
                        PrintWriter pw = ape.getFiler().createSourceFile("HelloWorld");
                        pw.println("public class HelloWorld {");
                        pw.println("  
                        pw.println("  public static void main(String argv[]) {");
                        pw.println("    System.out.println(\"Hello World\");");
                        pw.println("  }");
                        pw.println("}");
                    } else
                        throw new RuntimeException("touched file already exists!");
                } catch (java.io.IOException e) {
                    result = false;
                }
            }
        }
    }
    static final Collection<String> supportedOptions;
    static final Collection<String> supportedTypes;
    static {
        String options[] = {""};
        supportedOptions = Collections.unmodifiableCollection(Arrays.asList(options));
        String types[] = {"*"};
        supportedTypes = Collections.unmodifiableCollection(Arrays.asList(types));
    }
    public Collection<String> supportedAnnotationTypes() {return supportedTypes;}
    public Collection<String> supportedOptions() {return supportedOptions;}
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                        AnnotationProcessorEnvironment env) {
        return new PhantomTouchProc(env);
    }
}
