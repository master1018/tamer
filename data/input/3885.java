public class WarnAPF implements AnnotationProcessorFactory {
    static class WarnAP implements AnnotationProcessor {
        AnnotationProcessorEnvironment env;
        WarnAP(AnnotationProcessorEnvironment env) {
            this.env = env;
        }
        public void process() {
            Messager messager = env.getMessager();
            messager.printWarning("Beware the ides of March!");
            for(TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations()) {
                messager.printNotice(typeDecl.getPosition(),  "You are about to be warned");
                messager.printWarning(typeDecl.getPosition(), "Strange class name");
                for(AnnotationMirror annotMirror : typeDecl.getAnnotationMirrors()) {
                    messager.printNotice("MIRROR " + annotMirror.getPosition().toString());
                     Map<AnnotationTypeElementDeclaration,AnnotationValue> map =
                         annotMirror.getElementValues();
                     if (map.keySet().size() > 0)
                         for(AnnotationTypeElementDeclaration key : map.keySet() ) {
                             AnnotationValue annotValue = map.get(key);
                             Object o = annotValue.getValue();
                             messager.printNotice("VALUE " + annotValue.getPosition().toString());
                         }
                     else {
                         Collection<AnnotationTypeElementDeclaration> ateds =
                         annotMirror.getAnnotationType().getDeclaration().getMethods();
                         for(AnnotationTypeElementDeclaration ated : ateds ) {
                             AnnotationValue annotValue = ated.getDefaultValue();
                             Object o = annotValue.getValue();
                             messager.printNotice("VALUE " + "HelloAnnotation.java:5");
                         }
                     }
                }
            }
        }
    }
    static final Collection<String> supportedTypes;
    static {
        String types[] = {"*"};
        supportedTypes = unmodifiableCollection(Arrays.asList(types));
    }
    public Collection<String> supportedAnnotationTypes() {return supportedTypes;}
    static final Collection<String> supportedOptions;
    static {
        String options[] = {""};
        supportedOptions = unmodifiableCollection(Arrays.asList(options));
    }
    public Collection<String> supportedOptions() {return supportedOptions;}
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
                                               AnnotationProcessorEnvironment env) {
        return new WarnAP(env);
    }
}
