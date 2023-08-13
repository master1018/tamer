class BatchParser extends Parser {
    protected Identifier pkg;
    protected Imports imports;
    protected Vector classes;
    protected SourceClass sourceClass;
    protected Environment toplevelEnv;
    public BatchParser(Environment env, InputStream in) throws IOException {
        super(env, in);
        imports = new Imports(env);
        classes = new Vector();
        toplevelEnv = imports.newEnvironment(env);
    }
    public void packageDeclaration(long where, IdentifierToken t) {
        Identifier nm = t.getName();
        if (pkg == null) {
            pkg = t.getName();
            imports.setCurrentPackage(t);
        } else {
            env.error(where, "package.repeated");
        }
    }
    public void importClass(long pos, IdentifierToken t) {
        imports.addClass(t);
    }
    public void importPackage(long pos, IdentifierToken t) {
        imports.addPackage(t);
    }
    public ClassDefinition beginClass(long where, String doc, int mod,
                                      IdentifierToken t,
                                      IdentifierToken sup,
                                      IdentifierToken interfaces[]) {
        if (tracing) toplevelEnv.dtEnter("beginClass: " + sourceClass);
        SourceClass outerClass = sourceClass;
        if (outerClass == null && pkg != null) {
            t = new IdentifierToken(t.getWhere(),
                                    Identifier.lookup(pkg, t.getName()));
        }
        if ((mod & M_ANONYMOUS) != 0) {
            mod |= (M_FINAL | M_PRIVATE);
        }
        if ((mod & M_LOCAL) != 0) {
            mod |= M_PRIVATE;
        }
        if ((mod & M_INTERFACE) != 0) {
            mod |= M_ABSTRACT;
            if (outerClass != null) {
                mod |= M_STATIC;
            }
        }
        if (outerClass != null && outerClass.isInterface()) {
            if ((mod & (M_PRIVATE | M_PROTECTED)) == 0)
                mod |= M_PUBLIC;
            mod |= M_STATIC;
        }
        sourceClass = (SourceClass)
            toplevelEnv.makeClassDefinition(toplevelEnv, where, t,
                                            doc, mod, sup,
                                            interfaces, outerClass);
        sourceClass.getClassDeclaration().setDefinition(sourceClass, CS_PARSED);
        env = new Environment(toplevelEnv, sourceClass);
        if (tracing) toplevelEnv.dtEvent("beginClass: SETTING UP DEPENDENCIES");
        if (tracing) toplevelEnv.dtEvent("beginClass: ADDING TO CLASS LIST");
        classes.addElement(sourceClass);
        if (tracing) toplevelEnv.dtExit("beginClass: " + sourceClass);
        return sourceClass;
    }
    public ClassDefinition getCurrentClass() {
        return sourceClass;
    }
    public void endClass(long where, ClassDefinition c) {
        if (tracing) toplevelEnv.dtEnter("endClass: " + sourceClass);
        sourceClass.setEndPosition(where);
        SourceClass outerClass = (SourceClass) sourceClass.getOuterClass();
        sourceClass = outerClass;
        env = toplevelEnv;
        if (sourceClass != null)
            env = new Environment(env, sourceClass);
        if (tracing) toplevelEnv.dtExit("endClass: " + sourceClass);
    }
    public void defineField(long where, ClassDefinition c,
                            String doc, int mod, Type t,
                            IdentifierToken name, IdentifierToken args[],
                            IdentifierToken exp[], Node val) {
        Identifier nm = name.getName();
        if (sourceClass.isInterface()) {
            if ((mod & (M_PRIVATE | M_PROTECTED)) == 0)
                mod |= M_PUBLIC;
            if (t.isType(TC_METHOD)) {
                mod |= M_ABSTRACT;
            } else {
                mod |= M_STATIC | M_FINAL;
            }
        }
        if (nm.equals(idInit)) {
            Type rt = t.getReturnType();
            Identifier retname = !rt.isType(TC_CLASS) ? idStar 
                                                      : rt.getClassName();
            Identifier clsname = sourceClass.getLocalName();
            if (clsname.equals(retname)) {
                t = Type.tMethod(Type.tVoid, t.getArgumentTypes());
            } else if (clsname.equals(retname.getFlatName().getName())) {
                t = Type.tMethod(Type.tVoid, t.getArgumentTypes());
                env.error(where, "invalid.method.decl.qual");
            } else if (retname.isQualified() || retname.equals(idStar)) {
                env.error(where, "invalid.method.decl.name");
                return;
            } else {
                env.error(where, "invalid.method.decl");
                return;
            }
        }
        if (args == null && t.isType(TC_METHOD)) {
            args = new IdentifierToken[0];
        }
        if (exp == null && t.isType(TC_METHOD)) {
            exp = new IdentifierToken[0];
        }
        MemberDefinition f = env.makeMemberDefinition(env, where, sourceClass,
                                                    doc, mod, t, nm,
                                                    args, exp, val);
        if (env.dump()) {
            f.print(System.out);
        }
    }
}
