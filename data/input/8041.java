public class ClassDeclApf2 implements AnnotationProcessorFactory {
    static int round = -1;
    private static final Collection<String> supportedAnnotations
        = unmodifiableCollection(Arrays.asList("*"));
    private static final Collection<String> supportedOptions = emptySet();
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                               AnnotationProcessorEnvironment env) {
        return new ClassDeclAp(env);
    }
    private static class ClassDeclAp implements AnnotationProcessor {
        private final AnnotationProcessorEnvironment env;
        ClassDeclAp(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        void drain(InputStream is, OutputStream os) {
            try {
            while (is.available() > 0 )
                os.write(is.read());
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        public void process() {
            int size = env.getSpecifiedTypeDeclarations().size();
            Filer f = env.getFiler();
            try {
                round++;
                switch (size) {
                case 3:
                    if (round == 0) {
                        drain(new FileInputStream("./tmp/classes/Round1Class.class"),
                              f.createClassFile("Round1Class"));
                    } else
                        throw new RuntimeException("Got " + size + " decl's in round " + round);
                    break;
                case 1:
                    if (round == 1) {
                        f.createSourceFile("AhOne").println("public class AhOne {}");
                        System.out.println("Before writing AndAhTwoClass");
                        drain(new FileInputStream("./tmp/classes/AndAhTwoClass.class"),
                              f.createClassFile("AndAhTwoClass"));
                        System.out.println("After writing AndAhTwoClass");
                    } else
                        throw new RuntimeException("Got " + size + " decl's in round " + round);
                    break;
                case 2:
                    if (round != 2) {
                        throw new RuntimeException("Got " + size + " decl's in round " + round);
                    }
                    break;
                default:
                    throw new RuntimeException("Unexpected number of declarations:" + size +
                                               "\n Specified:" + env.getSpecifiedTypeDeclarations() +
                                               "\n Included:" + env.getTypeDeclarations() );
                }
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}
