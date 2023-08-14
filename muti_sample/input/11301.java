public class DocEnv {
    protected static final Context.Key<DocEnv> docEnvKey =
        new Context.Key<DocEnv>();
    public static DocEnv instance(Context context) {
        DocEnv instance = context.get(docEnvKey);
        if (instance == null)
            instance = new DocEnv(context);
        return instance;
    }
    private Messager messager;
    DocLocale doclocale;
    Symtab syms;
    JavadocClassReader reader;
    JavadocEnter enter;
    Names names;
    private String encoding;
    final Symbol externalizableSym;
    ModifierFilter showAccess;
    boolean breakiterator;
    boolean quiet = false;
    Check chk;
    Types types;
    JavaFileManager fileManager;
    boolean docClasses = false;
    boolean legacyDoclet = true;
    private boolean silent = false;
    private DocEnv(Context context) {
        context.put(docEnvKey, this);
        messager = Messager.instance0(context);
        syms = Symtab.instance(context);
        reader = JavadocClassReader.instance0(context);
        enter = JavadocEnter.instance0(context);
        names = Names.instance(context);
        externalizableSym = reader.enterClass(names.fromString("java.io.Externalizable"));
        chk = Check.instance(context);
        types = Types.instance(context);
        fileManager = context.get(JavaFileManager.class);
        this.doclocale = new DocLocale(this, "", breakiterator);
    }
    public void setSilent(boolean silent) {
        this.silent = silent;
    }
    public ClassDocImpl lookupClass(String name) {
        ClassSymbol c = getClassSymbol(name);
        if (c != null) {
            return getClassDoc(c);
        } else {
            return null;
        }
    }
    public ClassDocImpl loadClass(String name) {
        try {
            ClassSymbol c = reader.loadClass(names.fromString(name));
            return getClassDoc(c);
        } catch (CompletionFailure ex) {
            chk.completionError(null, ex);
            return null;
        }
    }
    public PackageDocImpl lookupPackage(String name) {
        PackageSymbol p = syms.packages.get(names.fromString(name));
        ClassSymbol c = getClassSymbol(name);
        if (p != null && c == null) {
            return getPackageDoc(p);
        } else {
            return null;
        }
    }
        ClassSymbol getClassSymbol(String name) {
            int nameLen = name.length();
            char[] nameChars = name.toCharArray();
            int idx = name.length();
            for (;;) {
                ClassSymbol s = syms.classes.get(names.fromChars(nameChars, 0, nameLen));
                if (s != null)
                    return s; 
                idx = name.substring(0, idx).lastIndexOf('.');
                if (idx < 0) break;
                nameChars[idx] = '$';
            }
            return null;
        }
    public void setLocale(String localeName) {
        doclocale = new DocLocale(this, localeName, breakiterator);
        messager.reset();
    }
    public boolean shouldDocument(VarSymbol sym) {
        long mod = sym.flags();
        if ((mod & Flags.SYNTHETIC) != 0) {
            return false;
        }
        return showAccess.checkModifier(translateModifiers(mod));
    }
    public boolean shouldDocument(MethodSymbol sym) {
        long mod = sym.flags();
        if ((mod & Flags.SYNTHETIC) != 0) {
            return false;
        }
        return showAccess.checkModifier(translateModifiers(mod));
    }
    public boolean shouldDocument(ClassSymbol sym) {
        return
            (sym.flags_field&Flags.SYNTHETIC) == 0 && 
            (docClasses || getClassDoc(sym).tree != null) &&
            isVisible(sym);
    }
    protected boolean isVisible(ClassSymbol sym) {
        long mod = sym.flags_field;
        if (!showAccess.checkModifier(translateModifiers(mod))) {
            return false;
        }
        ClassSymbol encl = sym.owner.enclClass();
        return (encl == null || (mod & Flags.STATIC) != 0 || isVisible(encl));
    }
    public void printError(String msg) {
        if (silent)
            return;
        messager.printError(msg);
    }
    public void error(DocImpl doc, String key) {
        if (silent)
            return;
        messager.error(doc==null ? null : doc.position(), key);
    }
    public void error(SourcePosition pos, String key) {
        if (silent)
            return;
        messager.error(pos, key);
    }
    public void printError(SourcePosition pos, String msg) {
        if (silent)
            return;
        messager.printError(pos, msg);
    }
    public void error(DocImpl doc, String key, String a1) {
        if (silent)
            return;
        messager.error(doc==null ? null : doc.position(), key, a1);
    }
    public void error(DocImpl doc, String key, String a1, String a2) {
        if (silent)
            return;
        messager.error(doc==null ? null : doc.position(), key, a1, a2);
    }
    public void error(DocImpl doc, String key, String a1, String a2, String a3) {
        if (silent)
            return;
        messager.error(doc==null ? null : doc.position(), key, a1, a2, a3);
    }
    public void printWarning(String msg) {
        if (silent)
            return;
        messager.printWarning(msg);
    }
    public void warning(DocImpl doc, String key) {
        if (silent)
            return;
        messager.warning(doc==null ? null : doc.position(), key);
    }
    public void printWarning(SourcePosition pos, String msg) {
        if (silent)
            return;
        messager.printWarning(pos, msg);
    }
    public void warning(DocImpl doc, String key, String a1) {
        if (silent)
            return;
        messager.warning(doc==null ? null : doc.position(), key, a1);
    }
    public void warning(DocImpl doc, String key, String a1, String a2) {
        if (silent)
            return;
        messager.warning(doc==null ? null : doc.position(), key, a1, a2);
    }
    public void warning(DocImpl doc, String key, String a1, String a2, String a3) {
        if (silent)
            return;
        messager.warning(doc==null ? null : doc.position(), key, a1, a2, a3);
    }
    public void warning(DocImpl doc, String key, String a1, String a2, String a3,
                        String a4) {
        if (silent)
            return;
        messager.warning(doc==null ? null : doc.position(), key, a1, a2, a3, a4);
    }
    public void printNotice(String msg) {
        if (silent || quiet)
            return;
        messager.printNotice(msg);
    }
    public void notice(String key) {
        if (silent || quiet)
            return;
        messager.notice(key);
    }
    public void printNotice(SourcePosition pos, String msg) {
        if (silent || quiet)
            return;
        messager.printNotice(pos, msg);
    }
    public void notice(String key, String a1) {
        if (silent || quiet)
            return;
        messager.notice(key, a1);
    }
    public void notice(String key, String a1, String a2) {
        if (silent || quiet)
            return;
        messager.notice(key, a1, a2);
    }
    public void notice(String key, String a1, String a2, String a3) {
        if (silent || quiet)
            return;
        messager.notice(key, a1, a2, a3);
    }
    public void exit() {
        messager.exit();
    }
    private Map<PackageSymbol, PackageDocImpl> packageMap =
            new HashMap<PackageSymbol, PackageDocImpl>();
    public PackageDocImpl getPackageDoc(PackageSymbol pack) {
        PackageDocImpl result = packageMap.get(pack);
        if (result != null) return result;
        result = new PackageDocImpl(this, pack);
        packageMap.put(pack, result);
        return result;
    }
    void makePackageDoc(PackageSymbol pack, String docComment, JCCompilationUnit tree) {
        PackageDocImpl result = packageMap.get(pack);
        if (result != null) {
            if (docComment != null) result.setRawCommentText(docComment);
            if (tree != null) result.setTree(tree);
        } else {
            result = new PackageDocImpl(this, pack, docComment, tree);
            packageMap.put(pack, result);
        }
    }
    private Map<ClassSymbol, ClassDocImpl> classMap =
            new HashMap<ClassSymbol, ClassDocImpl>();
    ClassDocImpl getClassDoc(ClassSymbol clazz) {
        ClassDocImpl result = classMap.get(clazz);
        if (result != null) return result;
        if (isAnnotationType(clazz)) {
            result = new AnnotationTypeDocImpl(this, clazz);
        } else {
            result = new ClassDocImpl(this, clazz);
        }
        classMap.put(clazz, result);
        return result;
    }
    void makeClassDoc(ClassSymbol clazz, String docComment, JCClassDecl tree, Position.LineMap lineMap) {
        ClassDocImpl result = classMap.get(clazz);
        if (result != null) {
            if (docComment != null) result.setRawCommentText(docComment);
            if (tree != null) result.setTree(tree);
            return;
        }
        if (isAnnotationType(tree)) {   
            result = new AnnotationTypeDocImpl(this, clazz, docComment, tree, lineMap);
        } else {
            result = new ClassDocImpl(this, clazz, docComment, tree, lineMap);
        }
        classMap.put(clazz, result);
    }
    private static boolean isAnnotationType(ClassSymbol clazz) {
        return ClassDocImpl.isAnnotationType(clazz);
    }
    private static boolean isAnnotationType(JCClassDecl tree) {
        return (tree.mods.flags & Flags.ANNOTATION) != 0;
    }
    private Map<VarSymbol, FieldDocImpl> fieldMap =
            new HashMap<VarSymbol, FieldDocImpl>();
    FieldDocImpl getFieldDoc(VarSymbol var) {
        FieldDocImpl result = fieldMap.get(var);
        if (result != null) return result;
        result = new FieldDocImpl(this, var);
        fieldMap.put(var, result);
        return result;
    }
    void makeFieldDoc(VarSymbol var, String docComment, JCVariableDecl tree, Position.LineMap lineMap) {
        FieldDocImpl result = fieldMap.get(var);
        if (result != null) {
            if (docComment != null) result.setRawCommentText(docComment);
            if (tree != null) result.setTree(tree);
        } else {
            result = new FieldDocImpl(this, var, docComment, tree, lineMap);
            fieldMap.put(var, result);
        }
    }
    private Map<MethodSymbol, ExecutableMemberDocImpl> methodMap =
            new HashMap<MethodSymbol, ExecutableMemberDocImpl>();
    void makeMethodDoc(MethodSymbol meth, String docComment,
                       JCMethodDecl tree, Position.LineMap lineMap) {
        MethodDocImpl result = (MethodDocImpl)methodMap.get(meth);
        if (result != null) {
            if (docComment != null) result.setRawCommentText(docComment);
            if (tree != null) result.setTree(tree);
        } else {
            result = new MethodDocImpl(this, meth, docComment, tree, lineMap);
            methodMap.put(meth, result);
        }
    }
    public MethodDocImpl getMethodDoc(MethodSymbol meth) {
        assert !meth.isConstructor() : "not expecting a constructor symbol";
        MethodDocImpl result = (MethodDocImpl)methodMap.get(meth);
        if (result != null) return result;
        result = new MethodDocImpl(this, meth);
        methodMap.put(meth, result);
        return result;
    }
    void makeConstructorDoc(MethodSymbol meth, String docComment,
                            JCMethodDecl tree, Position.LineMap lineMap) {
        ConstructorDocImpl result = (ConstructorDocImpl)methodMap.get(meth);
        if (result != null) {
            if (docComment != null) result.setRawCommentText(docComment);
            if (tree != null) result.setTree(tree);
        } else {
            result = new ConstructorDocImpl(this, meth, docComment, tree, lineMap);
            methodMap.put(meth, result);
        }
    }
    public ConstructorDocImpl getConstructorDoc(MethodSymbol meth) {
        assert meth.isConstructor() : "expecting a constructor symbol";
        ConstructorDocImpl result = (ConstructorDocImpl)methodMap.get(meth);
        if (result != null) return result;
        result = new ConstructorDocImpl(this, meth);
        methodMap.put(meth, result);
        return result;
    }
    void makeAnnotationTypeElementDoc(MethodSymbol meth,
                                      String docComment, JCMethodDecl tree, Position.LineMap lineMap) {
        AnnotationTypeElementDocImpl result =
            (AnnotationTypeElementDocImpl)methodMap.get(meth);
        if (result != null) {
            if (docComment != null) result.setRawCommentText(docComment);
            if (tree != null) result.setTree(tree);
        } else {
            result =
                new AnnotationTypeElementDocImpl(this, meth, docComment, tree, lineMap);
            methodMap.put(meth, result);
        }
    }
    public AnnotationTypeElementDocImpl getAnnotationTypeElementDoc(
            MethodSymbol meth) {
        AnnotationTypeElementDocImpl result =
            (AnnotationTypeElementDocImpl)methodMap.get(meth);
        if (result != null) return result;
        result = new AnnotationTypeElementDocImpl(this, meth);
        methodMap.put(meth, result);
        return result;
    }
    ParameterizedTypeImpl getParameterizedType(ClassType t) {
        return new ParameterizedTypeImpl(this, t);
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    static int translateModifiers(long flags) {
        int result = 0;
        if ((flags & Flags.ABSTRACT) != 0)
            result |= Modifier.ABSTRACT;
        if ((flags & Flags.FINAL) != 0)
            result |= Modifier.FINAL;
        if ((flags & Flags.INTERFACE) != 0)
            result |= Modifier.INTERFACE;
        if ((flags & Flags.NATIVE) != 0)
            result |= Modifier.NATIVE;
        if ((flags & Flags.PRIVATE) != 0)
            result |= Modifier.PRIVATE;
        if ((flags & Flags.PROTECTED) != 0)
            result |= Modifier.PROTECTED;
        if ((flags & Flags.PUBLIC) != 0)
            result |= Modifier.PUBLIC;
        if ((flags & Flags.STATIC) != 0)
            result |= Modifier.STATIC;
        if ((flags & Flags.SYNCHRONIZED) != 0)
            result |= Modifier.SYNCHRONIZED;
        if ((flags & Flags.TRANSIENT) != 0)
            result |= Modifier.TRANSIENT;
        if ((flags & Flags.VOLATILE) != 0)
            result |= Modifier.VOLATILE;
        return result;
    }
}
