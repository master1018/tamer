public class DeclarationMaker {
    private AptEnv env;
    private Context context;
    private JavaCompiler javacompiler;
    private static final Context.Key<DeclarationMaker> declarationMakerKey =
            new Context.Key<DeclarationMaker>();
    public static DeclarationMaker instance(Context context) {
        DeclarationMaker instance = context.get(declarationMakerKey);
        if (instance == null) {
            instance = new DeclarationMaker(context);
        }
        return instance;
    }
    private DeclarationMaker(Context context) {
        context.put(declarationMakerKey, this);
        env = AptEnv.instance(context);
        this.context = context;
        this.javacompiler = JavaCompiler.instance(context);
    }
    private Map<PackageSymbol, PackageDeclaration> packageDecls =
            new HashMap<PackageSymbol, PackageDeclaration>();
    public PackageDeclaration getPackageDeclaration(PackageSymbol p) {
        PackageDeclaration res = packageDecls.get(p);
        if (res == null) {
            res = new PackageDeclarationImpl(env, p);
            packageDecls.put(p, res);
        }
        return res;
    }
    public PackageDeclaration getPackageDeclaration(String name) {
        PackageSymbol p = null;
        if (name.equals("") )
            p = env.symtab.unnamedPackage;
        else {
            if (!isJavaName(name))
                return null;
            Symbol s = nameToSymbol(name, false);
            if (s instanceof PackageSymbol) {
                p = (PackageSymbol) s;
                if (!p.exists())
                    return null;
            } else
                return null;
        }
        return getPackageDeclaration(p);
    }
    private Map<ClassSymbol, TypeDeclaration> typeDecls =
            new HashMap<ClassSymbol, TypeDeclaration>();
    public TypeDeclaration getTypeDeclaration(ClassSymbol c) {
        long flags = AptEnv.getFlags(c);        
        if (c.kind == Kinds.ERR) {
            return null;
        }
        TypeDeclaration res = typeDecls.get(c);
        if (res == null) {
            if ((flags & Flags.ANNOTATION) != 0) {
                res = new AnnotationTypeDeclarationImpl(env, c);
            } else if ((flags & Flags.INTERFACE) != 0) {
                res = new InterfaceDeclarationImpl(env, c);
            } else if ((flags & Flags.ENUM) != 0) {
                res = new EnumDeclarationImpl(env, c);
            } else {
                res = new ClassDeclarationImpl(env, c);
            }
            typeDecls.put(c, res);
        }
        return res;
    }
    public TypeDeclaration getTypeDeclaration(String name) {
        if (!isJavaName(name))
            return null;
        Symbol s = nameToSymbol(name, true);
        if (s instanceof ClassSymbol) {
            ClassSymbol c = (ClassSymbol) s;
            return getTypeDeclaration(c);
        } else
            return null;
    }
    private Symbol nameToSymbol(String name, boolean classCache) {
        Symbol s = null;
        Name nameName = env.names.fromString(name);
        if (classCache)
            s = env.symtab.classes.get(nameName);
        else
            s = env.symtab.packages.get(nameName);
        if (s != null && s.exists())
            return s;
        s = javacompiler.resolveIdent(name);
        if (s.kind == Kinds.ERR  )
            return null;
        if (s.kind == Kinds.PCK)
            s.complete();
        return s;
    }
    private Map<MethodSymbol, ExecutableDeclaration> executableDecls =
            new HashMap<MethodSymbol, ExecutableDeclaration>();
    ExecutableDeclaration getExecutableDeclaration(MethodSymbol m) {
        ExecutableDeclaration res = executableDecls.get(m);
        if (res == null) {
            if (m.isConstructor()) {
                res = new ConstructorDeclarationImpl(env, m);
            } else if (isAnnotationTypeElement(m)) {
                res = new AnnotationTypeElementDeclarationImpl(env, m);
            } else {
                res = new MethodDeclarationImpl(env, m);
            }
            executableDecls.put(m, res);
        }
        return res;
    }
    private Map<VarSymbol, FieldDeclaration> fieldDecls =
            new HashMap<VarSymbol, FieldDeclaration>();
    FieldDeclaration getFieldDeclaration(VarSymbol v) {
        FieldDeclaration res = fieldDecls.get(v);
        if (res == null) {
            if (hasFlag(v, Flags.ENUM)) {
                res = new EnumConstantDeclarationImpl(env, v);
            } else {
                res = new FieldDeclarationImpl(env, v);
            }
            fieldDecls.put(v, res);
        }
        return res;
    }
    ParameterDeclaration getParameterDeclaration(VarSymbol v) {
        return new ParameterDeclarationImpl(env, v);
    }
    public TypeParameterDeclaration getTypeParameterDeclaration(TypeSymbol t) {
        return new TypeParameterDeclarationImpl(env, t);
    }
    AnnotationMirror getAnnotationMirror(Attribute.Compound a, Declaration decl) {
        return new AnnotationMirrorImpl(env, a, decl);
    }
    public static boolean isJavaIdentifier(String id) {
        return javax.lang.model.SourceVersion.isIdentifier(id);
    }
    public static boolean isJavaName(String name) {
        for(String id: name.split("\\.")) {
            if (! isJavaIdentifier(id))
                return false;
        }
        return true;
    }
    private static boolean isAnnotationTypeElement(MethodSymbol m) {
        return hasFlag(m.enclClass(), Flags.ANNOTATION);
    }
    private static boolean hasFlag(Symbol s, long flag) {
        return AptEnv.hasFlag(s, flag);
    }
}
