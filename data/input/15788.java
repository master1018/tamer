public class Apt extends ListBuffer<Env<AttrContext>> {
    java.util.Set<String> genSourceFileNames = new java.util.LinkedHashSet<String>();
    public java.util.Set<String> getSourceFileNames() {
        return genSourceFileNames;
    }
    java.util.Set<String> genClassFileNames  = new java.util.LinkedHashSet<String>();
    public java.util.Set<String> getClassFileNames() {
        return genClassFileNames;
    }
    AptEnv aptenv;
    private Context context;
    protected static final Context.Key<Apt> aptKey =
        new Context.Key<Apt>();
    public static Apt instance(Context context) {
        Apt instance = context.get(aptKey);
        if (instance == null)
            instance = new Apt(context);
        return instance;
    }
    protected Apt(Context context) {
        this.context = context;
        context.put(aptKey, this);
        aptenv = AptEnv.instance(context);
    }
    static class AptTreeScanner extends TreeScanner {
        private Set<String> annotationSet;
        private Collection<ClassSymbol> specifiedDeclCollection;
        private Collection<ClassSymbol> declCollection;
        public Set<String> getAnnotationSet() {
            return annotationSet;
        }
        public AptTreeScanner() {
            annotationSet = new  LinkedHashSet<String>();
            specifiedDeclCollection = new LinkedHashSet<ClassSymbol>();
            declCollection = new LinkedHashSet<ClassSymbol>();
        }
        public void visitTopLevel(JCTree.JCCompilationUnit tree) {
            super.visitTopLevel(tree);
            for(JCTree d: tree.defs) {
                if (d instanceof JCTree.JCClassDecl)
                    specifiedDeclCollection.add(((JCTree.JCClassDecl) d).sym);
            }
        }
        public void visitBlock(JCTree.JCBlock tree) {
            ; 
        }
        public void visitClassDef(JCTree.JCClassDecl tree) {
            if (tree.sym == null) {
                return;
            }
            super.visitClassDef(tree);
            declCollection.add(tree.sym);
        }
        public void visitMethodDef(JCTree.JCMethodDecl tree) {
            super.visitMethodDef(tree);
        }
        public void visitVarDef(JCTree.JCVariableDecl tree) {
            super.visitVarDef(tree);
        }
        public void visitAnnotation(JCTree.JCAnnotation tree) {
            super.visitAnnotation(tree);
            annotationSet.add(tree.type.tsym.toString());
        }
    }
    Set<String> computeAnnotationSet(Collection<ClassSymbol> classSymbols) {
        Set<String> annotationSet = new HashSet<String>();
        for(ClassSymbol classSymbol: classSymbols) {
            computeAnnotationSet(classSymbol, annotationSet);
        }
        return annotationSet;
    }
    void computeAnnotationSet(Symbol symbol, Set<String> annotationSet) {
        if (symbol != null ) {
            if (symbol.getAnnotationMirrors() != null)
                for(Attribute.Compound compound: symbol.getAnnotationMirrors())
                    annotationSet.add(compound.type.tsym.toString()); 
            if (symbol instanceof Symbol.MethodSymbol) 
                for(Symbol param: ((MethodSymbol) symbol).params())
                    computeAnnotationSet(param, annotationSet);
            if (symbol.members() != null) {
                for(Scope.Entry e = symbol.members().elems; e != null; e = e.sibling)
                    computeAnnotationSet(e.sym, annotationSet);
            }
        }
    }
    public void main(com.sun.tools.javac.util.List<JCTree.JCCompilationUnit> treeList,
                     ListBuffer<ClassSymbol> classes,
                     Map<String, String> origOptions,
                     ClassLoader aptCL,
                     AnnotationProcessorFactory providedFactory,
                     java.util.Set<Class<? extends AnnotationProcessorFactory> > productiveFactories) {
        Bark bark = Bark.instance(context);
        java.io.PrintWriter out = bark.warnWriter;
        Options options = Options.instance(context);
        Collection<TypeDeclaration> spectypedecls =     new LinkedHashSet<TypeDeclaration>();
        Collection<TypeDeclaration> typedecls =         new LinkedHashSet<TypeDeclaration>();
        Set<String> unmatchedAnnotations =              new LinkedHashSet<String>();
        Set<AnnotationTypeDeclaration> emptyATDS =      Collections.emptySet();
        Set<Class<? extends AnnotationProcessorFactory> > currentRoundFactories =
            new LinkedHashSet<Class<? extends AnnotationProcessorFactory> >();
        AptTreeScanner ats = new AptTreeScanner();
        for(JCTree t: treeList) {
            t.accept(ats);
        }
        for (ClassSymbol cs : ats.specifiedDeclCollection) {
            TypeDeclaration decl = aptenv.declMaker.getTypeDeclaration(cs);
            spectypedecls.add(decl);
        }
        for (ClassSymbol cs : ats.declCollection) {
            TypeDeclaration decl = aptenv.declMaker.getTypeDeclaration(cs);
            typedecls.add(decl);
        }
        unmatchedAnnotations.addAll(ats.getAnnotationSet());
        for(ClassSymbol cs : classes) {
            TypeDeclaration decl = aptenv.declMaker.getTypeDeclaration(cs);
            spectypedecls.add(decl);
            typedecls.add(decl);
            computeAnnotationSet(cs, unmatchedAnnotations);
        }
        if (options.get("-XListAnnotationTypes") != null) {
            out.println("Set of annotations found:" +
                        (new TreeSet<String>(unmatchedAnnotations)).toString());
        }
        AnnotationProcessorEnvironmentImpl trivAPE =
            new AnnotationProcessorEnvironmentImpl(spectypedecls, typedecls, origOptions, context);
        if (options.get("-XListDeclarations") != null) {
            out.println("Set of Specified Declarations:" +
                        spectypedecls);
            out.println("Set of Included Declarations: " +
                           typedecls);
        }
        if (options.get("-print") != null) {
            if (spectypedecls.size() == 0 )
                throw new UsageMessageNeededException();
            AnnotationProcessor proc = (new BootstrapAPF()).getProcessorFor(new HashSet<AnnotationTypeDeclaration>(),
                                                                            trivAPE);
            proc.process();
        } else {
            java.util.Iterator<AnnotationProcessorFactory> providers = null;
            {
                java.util.List<AnnotationProcessorFactory> list =
                    new LinkedList<AnnotationProcessorFactory>();
                String factoryName = options.get("-factory");
                if (providedFactory != null) {
                    list.add(providedFactory);
                    providers = list.iterator();
                } else if (factoryName != null) {
                    try {
                        AnnotationProcessorFactory factory =
                            (AnnotationProcessorFactory) (aptCL.loadClass(factoryName).newInstance());
                        list.add(factory);
                    } catch (ClassNotFoundException cnfe) {
                        bark.aptWarning("FactoryNotFound", factoryName);
                    } catch (ClassCastException cce) {
                        bark.aptWarning("FactoryWrongType", factoryName);
                    } catch (Exception e ) {
                        bark.aptWarning("FactoryCantInstantiate", factoryName);
                    } catch(Throwable t) {
                        throw new AnnotationProcessingError(t);
                    }
                    providers = list.iterator();
                } else {
                    @SuppressWarnings("unchecked")
                    Iterator<AnnotationProcessorFactory> iter =
                            sun.misc.Service.providers(AnnotationProcessorFactory.class, aptCL);
                    providers = iter;
                }
            }
            java.util.Map<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>> factoryToAnnotation =
                new LinkedHashMap<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>>();
            if (!providers.hasNext() && productiveFactories.size() == 0) {
                if (unmatchedAnnotations.size() > 0)
                    bark.aptWarning("NoAnnotationProcessors");
                if (spectypedecls.size() == 0)
                    throw new UsageMessageNeededException();
                return; 
            } else {
                if(unmatchedAnnotations.size() == 0)
                    unmatchedAnnotations.add("");
                Set<String> emptyStringSet = new HashSet<String>();
                emptyStringSet.add("");
                emptyStringSet = Collections.unmodifiableSet(emptyStringSet);
                while (providers.hasNext() ) {
                    Object provider = providers.next();
                    try {
                        Set<String> matchedStrings = new HashSet<String>();
                        AnnotationProcessorFactory apf = (AnnotationProcessorFactory) provider;
                        Collection<String> supportedTypes = apf.supportedAnnotationTypes();
                        Collection<Pattern> supportedTypePatterns = new LinkedList<Pattern>();
                        for(String s: supportedTypes)
                            supportedTypePatterns.add(importStringToPattern(s));
                        for(String s: unmatchedAnnotations) {
                            for(Pattern p: supportedTypePatterns) {
                                if (p.matcher(s).matches()) {
                                    matchedStrings.add(s);
                                    break;
                                }
                            }
                        }
                        unmatchedAnnotations.removeAll(matchedStrings);
                        if (options.get("-XPrintFactoryInfo") != null) {
                            out.println("Factory " + apf.getClass().getName() +
                                        " matches " +
                                        ((matchedStrings.size() == 0)?
                                         "nothing.": matchedStrings));
                        }
                        if (matchedStrings.size() > 0) {
                            Set<AnnotationTypeDeclaration> atds = new HashSet<AnnotationTypeDeclaration>();
                            if (!matchedStrings.equals(emptyStringSet)) {
                                for(String s: matchedStrings) {
                                    TypeDeclaration decl = aptenv.declMaker.getTypeDeclaration(s);
                                    AnnotationTypeDeclaration annotdecl;
                                    if (decl == null) {
                                        bark.aptError("DeclarationCreation", s);
                                    } else {
                                        try {
                                            annotdecl = (AnnotationTypeDeclaration)decl;
                                            atds.add(annotdecl);
                                        } catch (ClassCastException cce) {
                                            bark.aptError("BadDeclaration", s);
                                        }
                                    }
                                }
                            }
                            currentRoundFactories.add(apf.getClass());
                            productiveFactories.add(apf.getClass());
                            factoryToAnnotation.put(apf, atds);
                        } else if (productiveFactories.contains(apf.getClass())) {
                            currentRoundFactories.add(apf.getClass());
                            factoryToAnnotation.put(apf, emptyATDS );
                        }
                        if (unmatchedAnnotations.size() == 0)
                            break;
                    } catch (ClassCastException cce) {
                        bark.aptWarning("BadFactory", cce);
                    }
                }
                unmatchedAnnotations.remove("");
            }
            {
                java.util.Set<Class<? extends AnnotationProcessorFactory> > neglectedFactories =
                    new LinkedHashSet<Class<? extends AnnotationProcessorFactory>>(productiveFactories);
                neglectedFactories.removeAll(currentRoundFactories);
                for(Class<? extends AnnotationProcessorFactory> working : neglectedFactories) {
                    try {
                        AnnotationProcessorFactory factory = working.newInstance();
                        factoryToAnnotation.put(factory, emptyATDS);
                    } catch (Exception e ) {
                        bark.aptWarning("FactoryCantInstantiate", working.getName());
                    } catch(Throwable t) {
                        throw new AnnotationProcessingError(t);
                    }
                }
            }
            if (unmatchedAnnotations.size() > 0)
                bark.aptWarning("AnnotationsWithoutProcessors", unmatchedAnnotations);
            Set<AnnotationProcessor> processors = new LinkedHashSet<AnnotationProcessor>();
            if (spectypedecls.size() == 0 &&
                factoryToAnnotation.keySet().size() == 0 )
                throw new UsageMessageNeededException();
            try {
                for(Map.Entry<AnnotationProcessorFactory, Set<AnnotationTypeDeclaration>> entry :
                        factoryToAnnotation.entrySet()) {
                    AnnotationProcessorFactory  apFactory = entry.getKey();
                    AnnotationProcessor processor = apFactory.getProcessorFor(entry.getValue(),
                                                                              trivAPE);
                    if (processor != null)
                        processors.add(processor);
                    else
                        bark.aptWarning("NullProcessor", apFactory.getClass().getName());
                }
            } catch(Throwable t) {
                throw new AnnotationProcessingError(t);
            }
            LinkedList<AnnotationProcessor> temp = new LinkedList<AnnotationProcessor>();
            temp.addAll(processors);
            AnnotationProcessor proc = AnnotationProcessors.getCompositeAnnotationProcessor(temp);
            try {
                proc.process();
            } catch (Throwable t) {
                throw new AnnotationProcessingError(t);
            }
            trivAPE.roundComplete();
            FilerImpl filerimpl = (FilerImpl)trivAPE.getFiler();
            genSourceFileNames = filerimpl.getSourceFileNames();
            genClassFileNames = filerimpl.getClassFileNames();
            filerimpl.flush(); 
        }
    }
    Pattern importStringToPattern(String s) {
        if (com.sun.tools.javac.processing.JavacProcessingEnvironment.isValidImportString(s)) {
            return com.sun.tools.javac.processing.JavacProcessingEnvironment.validImportStringToPattern(s);
        } else {
            Bark bark = Bark.instance(context);
            bark.aptWarning("MalformedSupportedString", s);
            return com.sun.tools.javac.processing.JavacProcessingEnvironment.noMatches;
        }
    }
}
