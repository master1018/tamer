public class AnnotationProcessorEnvironmentImpl implements AnnotationProcessorEnvironment {
    Collection<TypeDeclaration> spectypedecls;
    Collection<TypeDeclaration> typedecls;
    Map<String, String> origOptions;
    DeclarationMaker declMaker;
    Declarations declUtils;
    Types typeUtils;
    Messager messager;
    FilerImpl filer;
    Bark bark;
    Set<RoundCompleteListener> roundCompleteListeners;
    public AnnotationProcessorEnvironmentImpl(Collection<TypeDeclaration> spectypedecls,
                                              Collection<TypeDeclaration> typedecls,
                                              Map<String, String> origOptions,
                                              Context context) {
        this.spectypedecls = Collections.unmodifiableCollection(spectypedecls);
        this.typedecls = Collections.unmodifiableCollection(typedecls);
        this.origOptions = Collections.unmodifiableMap(origOptions);
        declMaker = DeclarationMaker.instance(context);
        declUtils = DeclarationsImpl.instance(context);
        typeUtils = TypesImpl.instance(context);
        messager = MessagerImpl.instance(context);
        filer = FilerImpl.instance(context);
        bark = Bark.instance(context);
        roundCompleteListeners = new LinkedHashSet<RoundCompleteListener>();
    }
    public Map<String,String> getOptions() {
        return origOptions;
    }
    public Messager getMessager() {
        return messager;
    }
    public Filer getFiler() {
        return filer;
    }
    public Collection<TypeDeclaration> getSpecifiedTypeDeclarations() {
        return spectypedecls;
    }
    public PackageDeclaration getPackage(String name) {
        return declMaker.getPackageDeclaration(name);
    }
    public TypeDeclaration getTypeDeclaration(String name) {
        return declMaker.getTypeDeclaration(name);
    }
    public Collection<TypeDeclaration> getTypeDeclarations() {
        return typedecls;
    }
    public Collection<Declaration> getDeclarationsAnnotatedWith(
                                                AnnotationTypeDeclaration a) {
        CollectingAP proc = new CollectingAP(this, a);
        proc.process();
        return proc.decls;
    }
    private static class CollectingAP implements AnnotationProcessor {
        AnnotationProcessorEnvironment env;
        Collection<Declaration> decls;
        AnnotationTypeDeclaration atd;
        CollectingAP(AnnotationProcessorEnvironment env,
                     AnnotationTypeDeclaration atd) {
            this.env = env;
            this.atd = atd;
            decls = new HashSet<Declaration>();
        }
        private class CollectingVisitor extends SimpleDeclarationVisitor {
            public void visitDeclaration(Declaration d) {
                for(AnnotationMirror am: d.getAnnotationMirrors()) {
                    if (am.getAnnotationType().getDeclaration().equals(CollectingAP.this.atd))
                        CollectingAP.this.decls.add(d);
                }
            }
        }
        public void process() {
            for(TypeDeclaration d: env.getSpecifiedTypeDeclarations())
                d.accept(getSourceOrderDeclarationScanner(new CollectingVisitor(),
                                                          NO_OP));
        }
    }
    public Declarations getDeclarationUtils() {
        return declUtils;
    }
    public Types getTypeUtils() {
        return typeUtils;
    }
    public void addListener(AnnotationProcessorListener listener) {
        if (listener == null)
            throw new NullPointerException();
        else {
            if (listener instanceof RoundCompleteListener)
                roundCompleteListeners.add((RoundCompleteListener)listener);
        }
    }
    public void removeListener(AnnotationProcessorListener listener) {
        if (listener == null)
            throw new NullPointerException();
        else
            roundCompleteListeners.remove(listener);
    }
    public void roundComplete() {
        RoundState roundState  = new RoundStateImpl(bark.nerrors > 0,
                                                    filer.getSourceFileNames().size() > 0,
                                                    filer.getClassFileNames().size() > 0,
                                                    origOptions);
        RoundCompleteEvent roundCompleteEvent = new RoundCompleteEventImpl(this, roundState);
        filer.roundOver();
        for(RoundCompleteListener rcl: roundCompleteListeners)
            rcl.roundComplete(roundCompleteEvent);
    }
}
