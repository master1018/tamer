public class ClassDocImpl extends ProgramElementDocImpl implements ClassDoc {
    public final ClassType type;        
    protected final ClassSymbol tsym;
    boolean isIncluded = false;         
    private SerializedForm serializedForm;
    public ClassDocImpl(DocEnv env, ClassSymbol sym) {
        this(env, sym, null, null, null);
    }
    public ClassDocImpl(DocEnv env, ClassSymbol sym, String documentation,
                        JCClassDecl tree, Position.LineMap lineMap) {
        super(env, sym, documentation, tree, lineMap);
        this.type = (ClassType)sym.type;
        this.tsym = sym;
    }
    protected long getFlags() {
        return getFlags(tsym);
    }
    static long getFlags(ClassSymbol clazz) {
        while (true) {
            try {
                return clazz.flags();
            } catch (CompletionFailure ex) {
            }
        }
    }
    static boolean isAnnotationType(ClassSymbol clazz) {
        return (getFlags(clazz) & Flags.ANNOTATION) != 0;
    }
    protected ClassSymbol getContainingClass() {
        return tsym.owner.enclClass();
    }
    @Override
    public boolean isClass() {
        return !Modifier.isInterface(getModifiers());
    }
    @Override
    public boolean isOrdinaryClass() {
        if (isEnum() || isInterface() || isAnnotationType()) {
            return false;
        }
        for (Type t = type; t.tag == TypeTags.CLASS; t = env.types.supertype(t)) {
            if (t.tsym == env.syms.errorType.tsym ||
                t.tsym == env.syms.exceptionType.tsym) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isEnum() {
        return (getFlags() & Flags.ENUM) != 0
               &&
               !env.legacyDoclet;
    }
    @Override
    public boolean isInterface() {
        return Modifier.isInterface(getModifiers());
    }
    @Override
    public boolean isException() {
        if (isEnum() || isInterface() || isAnnotationType()) {
            return false;
        }
        for (Type t = type; t.tag == TypeTags.CLASS; t = env.types.supertype(t)) {
            if (t.tsym == env.syms.exceptionType.tsym) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isError() {
        if (isEnum() || isInterface() || isAnnotationType()) {
            return false;
        }
        for (Type t = type; t.tag == TypeTags.CLASS; t = env.types.supertype(t)) {
            if (t.tsym == env.syms.errorType.tsym) {
                return true;
            }
        }
        return false;
    }
    public boolean isThrowable() {
        if (isEnum() || isInterface() || isAnnotationType()) {
            return false;
        }
        for (Type t = type; t.tag == TypeTags.CLASS; t = env.types.supertype(t)) {
            if (t.tsym == env.syms.throwableType.tsym) {
                return true;
            }
        }
        return false;
    }
    public boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }
    public boolean isSynthetic() {
        return (getFlags() & Flags.SYNTHETIC) != 0;
    }
    public boolean isIncluded() {
        if (isIncluded) {
            return true;
        }
        if (env.shouldDocument(tsym)) {
            if (containingPackage().isIncluded()) {
                return isIncluded=true;
            }
            ClassDoc outer = containingClass();
            if (outer != null && outer.isIncluded()) {
                return isIncluded=true;
            }
        }
        return false;
    }
    @Override
    public PackageDoc containingPackage() {
        PackageDocImpl p = env.getPackageDoc(tsym.packge());
        if (p.setDocPath == false) {
            FileObject docPath;
            try {
                Location location = env.fileManager.hasLocation(StandardLocation.SOURCE_PATH)
                    ? StandardLocation.SOURCE_PATH : StandardLocation.CLASS_PATH;
                docPath = env.fileManager.getFileForInput(
                        location, p.qualifiedName(), "package.html");
            } catch (IOException e) {
                docPath = null;
            }
            if (docPath == null) {
                SourcePosition po = position();
                if (env.fileManager instanceof StandardJavaFileManager &&
                        po instanceof SourcePositionImpl) {
                    URI uri = ((SourcePositionImpl) po).filename.toUri();
                    if ("file".equals(uri.getScheme())) {
                        File f = new File(uri);
                        File dir = f.getParentFile();
                        if (dir != null) {
                            File pf = new File(dir, "package.html");
                            if (pf.exists()) {
                                StandardJavaFileManager sfm = (StandardJavaFileManager) env.fileManager;
                                docPath = sfm.getJavaFileObjects(pf).iterator().next();
                            }
                        }
                    }
                }
            }
            p.setDocPath(docPath);
        }
        return p;
    }
    public String name() {
        return getClassName(tsym, false);
    }
    public String qualifiedName() {
        return getClassName(tsym, true);
    }
    public String typeName() {
        return name();
    }
    public String qualifiedTypeName() {
        return qualifiedName();
    }
    public String simpleTypeName() {
        return tsym.name.toString();
    }
    @Override
    public String toString() {
        return classToString(env, tsym, true);
    }
    static String getClassName(ClassSymbol c, boolean full) {
        if (full) {
            return c.getQualifiedName().toString();
        } else {
            String n = "";
            for ( ; c != null; c = c.owner.enclClass()) {
                n = c.name + (n.equals("") ? "" : ".") + n;
            }
            return n;
        }
    }
    static String classToString(DocEnv env, ClassSymbol c, boolean full) {
        StringBuilder s = new StringBuilder();
        if (!c.isInner()) {             
            s.append(getClassName(c, full));
        } else {
            ClassSymbol encl = c.owner.enclClass();
            s.append(classToString(env, encl, full))
             .append('.')
             .append(c.name);
        }
        s.append(TypeMaker.typeParametersString(env, c, full));
        return s.toString();
    }
    static boolean isGeneric(ClassSymbol c) {
        return c.type.allparams().nonEmpty();
    }
    public TypeVariable[] typeParameters() {
        if (env.legacyDoclet) {
            return new TypeVariable[0];
        }
        TypeVariable res[] = new TypeVariable[type.getTypeArguments().length()];
        TypeMaker.getTypes(env, type.getTypeArguments(), res);
        return res;
    }
    public ParamTag[] typeParamTags() {
        return (env.legacyDoclet)
            ? new ParamTag[0]
            : comment().typeParamTags();
    }
    @Override
    public String modifiers() {
        return Modifier.toString(modifierSpecifier());
    }
    @Override
    public int modifierSpecifier() {
        int modifiers = getModifiers();
        return (isInterface() || isAnnotationType())
                ? modifiers & ~Modifier.ABSTRACT
                : modifiers;
    }
    public ClassDoc superclass() {
        if (isInterface() || isAnnotationType()) return null;
        if (tsym == env.syms.objectType.tsym) return null;
        ClassSymbol c = (ClassSymbol)env.types.supertype(type).tsym;
        if (c == null || c == tsym) c = (ClassSymbol)env.syms.objectType.tsym;
        return env.getClassDoc(c);
    }
    public com.sun.javadoc.Type superclassType() {
        if (isInterface() || isAnnotationType() ||
                (tsym == env.syms.objectType.tsym))
            return null;
        Type sup = env.types.supertype(type);
        return TypeMaker.getType(env,
                                 (sup != type) ? sup : env.syms.objectType);
    }
    public boolean subclassOf(ClassDoc cd) {
        return tsym.isSubClass(((ClassDocImpl)cd).tsym, env.types);
    }
    public ClassDoc[] interfaces() {
        ListBuffer<ClassDocImpl> ta = new ListBuffer<ClassDocImpl>();
        for (Type t : env.types.interfaces(type)) {
            ta.append(env.getClassDoc((ClassSymbol)t.tsym));
        }
        return ta.toArray(new ClassDocImpl[ta.length()]);
    }
    public com.sun.javadoc.Type[] interfaceTypes() {
        return TypeMaker.getTypes(env, env.types.interfaces(type));
    }
    public FieldDoc[] fields(boolean filter) {
        return fields(filter, false);
    }
    public FieldDoc[] fields() {
        return fields(true, false);
    }
    public FieldDoc[] enumConstants() {
        return fields(false, true);
    }
    private FieldDoc[] fields(boolean filter, boolean enumConstants) {
        List<FieldDocImpl> fields = List.nil();
        for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null && e.sym.kind == VAR) {
                VarSymbol s = (VarSymbol)e.sym;
                boolean isEnum = ((s.flags() & Flags.ENUM) != 0) &&
                                 !env.legacyDoclet;
                if (isEnum == enumConstants &&
                        (!filter || env.shouldDocument(s))) {
                    fields = fields.prepend(env.getFieldDoc(s));
                }
            }
        }
        return fields.toArray(new FieldDocImpl[fields.length()]);
    }
    public MethodDoc[] methods(boolean filter) {
        Names names = tsym.name.table.names;
        List<MethodDocImpl> methods = List.nil();
        for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null &&
                e.sym.kind == Kinds.MTH && e.sym.name != names.init) {
                MethodSymbol s = (MethodSymbol)e.sym;
                if (!filter || env.shouldDocument(s)) {
                    methods = methods.prepend(env.getMethodDoc(s));
                }
            }
        }
        return methods.toArray(new MethodDocImpl[methods.length()]);
    }
    public MethodDoc[] methods() {
        return methods(true);
    }
    public ConstructorDoc[] constructors(boolean filter) {
        Names names = tsym.name.table.names;
        List<ConstructorDocImpl> constructors = List.nil();
        for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null &&
                e.sym.kind == Kinds.MTH && e.sym.name == names.init) {
                MethodSymbol s = (MethodSymbol)e.sym;
                if (!filter || env.shouldDocument(s)) {
                    constructors = constructors.prepend(env.getConstructorDoc(s));
                }
            }
        }
        return constructors.toArray(new ConstructorDocImpl[constructors.length()]);
    }
    public ConstructorDoc[] constructors() {
        return constructors(true);
    }
    void addAllClasses(ListBuffer<ClassDocImpl> l, boolean filtered) {
        try {
            if (isSynthetic()) return;
            if (!JavadocTool.isValidClassName(tsym.name.toString())) return;
            if (filtered && !env.shouldDocument(tsym)) return;
            if (l.contains(this)) return;
            l.append(this);
            List<ClassDocImpl> more = List.nil();
            for (Scope.Entry e = tsym.members().elems; e != null;
                 e = e.sibling) {
                if (e.sym != null && e.sym.kind == Kinds.TYP) {
                    ClassSymbol s = (ClassSymbol)e.sym;
                    ClassDocImpl c = env.getClassDoc(s);
                    if (c.isSynthetic()) continue;
                    if (c != null) more = more.prepend(c);
                }
            }
            for (; more.nonEmpty(); more=more.tail) {
                more.head.addAllClasses(l, filtered);
            }
        } catch (CompletionFailure e) {
        }
    }
    public ClassDoc[] innerClasses(boolean filter) {
        ListBuffer<ClassDocImpl> innerClasses = new ListBuffer<ClassDocImpl>();
        for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null && e.sym.kind == Kinds.TYP) {
                ClassSymbol s = (ClassSymbol)e.sym;
                if ((s.flags_field & Flags.SYNTHETIC) != 0) continue;
                if (!filter || env.isVisible(s)) {
                    innerClasses.prepend(env.getClassDoc(s));
                }
            }
        }
        return innerClasses.toArray(new ClassDocImpl[innerClasses.length()]);
    }
    public ClassDoc[] innerClasses() {
        return innerClasses(true);
    }
    public ClassDoc findClass(String className) {
        ClassDoc searchResult = searchClass(className);
        if (searchResult == null) {
            ClassDocImpl enclosingClass = (ClassDocImpl)containingClass();
            while (enclosingClass != null && enclosingClass.containingClass() != null) {
                enclosingClass = (ClassDocImpl)enclosingClass.containingClass();
            }
            searchResult = enclosingClass == null ?
                null : enclosingClass.searchClass(className);
        }
        return searchResult;
    }
    private ClassDoc searchClass(String className) {
        Names names = tsym.name.table.names;
        ClassDoc cd = env.lookupClass(className);
        if (cd != null) {
            return cd;
        }
        ClassDoc innerClasses[] = innerClasses();
        for (int i = 0; i < innerClasses.length; i++) {
            if (innerClasses[i].name().equals(className) ||
                innerClasses[i].name().endsWith(className)) {
                return innerClasses[i];
            } else {
                ClassDoc innercd = ((ClassDocImpl) innerClasses[i]).searchClass(className);
                if (innercd != null) {
                    return innercd;
                }
            }
        }
        cd = containingPackage().findClass(className);
        if (cd != null) {
            return cd;
        }
        if (tsym.completer != null) {
            tsym.complete();
        }
        if (tsym.sourcefile != null) {
            Env<AttrContext> compenv = env.enter.getEnv(tsym);
            if (compenv == null) return null;
            Scope s = compenv.toplevel.namedImportScope;
            for (Scope.Entry e = s.lookup(names.fromString(className)); e.scope != null; e = e.next()) {
                if (e.sym.kind == Kinds.TYP) {
                    ClassDoc c = env.getClassDoc((ClassSymbol)e.sym);
                    return c;
                }
            }
            s = compenv.toplevel.starImportScope;
            for (Scope.Entry e = s.lookup(names.fromString(className)); e.scope != null; e = e.next()) {
                if (e.sym.kind == Kinds.TYP) {
                    ClassDoc c = env.getClassDoc((ClassSymbol)e.sym);
                    return c;
                }
            }
        }
        return null; 
    }
    private boolean hasParameterTypes(MethodSymbol method, String[] argTypes) {
        if (argTypes == null) {
            return true;
        }
        int i = 0;
        List<Type> types = method.type.getParameterTypes();
        if (argTypes.length != types.length()) {
            return false;
        }
        for (Type t : types) {
            String argType = argTypes[i++];
            if (i == argTypes.length) {
                argType = argType.replace("...", "[]");
            }
            if (!hasTypeName(env.types.erasure(t), argType)) {  
                return false;
            }
        }
        return true;
    }
    private boolean hasTypeName(Type t, String name) {
        return
            name.equals(TypeMaker.getTypeName(t, true))
            ||
            name.equals(TypeMaker.getTypeName(t, false))
            ||
            (qualifiedName() + "." + name).equals(TypeMaker.getTypeName(t, true));
    }
    public MethodDocImpl findMethod(String methodName, String[] paramTypes) {
        return searchMethod(methodName, paramTypes, new HashSet<ClassDocImpl>());
    }
    private MethodDocImpl searchMethod(String methodName,
                                       String[] paramTypes, Set<ClassDocImpl> searched) {
        Names names = tsym.name.table.names;
        if (names.init.contentEquals(methodName)) {
            return null;
        }
        ClassDocImpl cdi;
        MethodDocImpl mdi;
        if (searched.contains(this)) {
            return null;
        }
        searched.add(this);
        Scope.Entry e = tsym.members().lookup(names.fromString(methodName));
        if (paramTypes == null) {
            MethodSymbol lastFound = null;
            for (; e.scope != null; e = e.next()) {
                if (e.sym.kind == Kinds.MTH) {
                    if (e.sym.name.toString().equals(methodName)) {
                        lastFound = (MethodSymbol)e.sym;
                    }
                }
            }
            if (lastFound != null) {
                return env.getMethodDoc(lastFound);
            }
        } else {
            for (; e.scope != null; e = e.next()) {
                if (e.sym != null &&
                    e.sym.kind == Kinds.MTH) {
                    if (hasParameterTypes((MethodSymbol)e.sym, paramTypes)) {
                        return env.getMethodDoc((MethodSymbol)e.sym);
                    }
                }
            }
        }
        cdi = (ClassDocImpl)superclass();
        if (cdi != null) {
            mdi = cdi.searchMethod(methodName, paramTypes, searched);
            if (mdi != null) {
                return mdi;
            }
        }
        ClassDoc intf[] = interfaces();
        for (int i = 0; i < intf.length; i++) {
            cdi = (ClassDocImpl)intf[i];
            mdi = cdi.searchMethod(methodName, paramTypes, searched);
            if (mdi != null) {
                return mdi;
            }
        }
        cdi = (ClassDocImpl)containingClass();
        if (cdi != null) {
            mdi = cdi.searchMethod(methodName, paramTypes, searched);
            if (mdi != null) {
                return mdi;
            }
        }
        return null;
    }
    public ConstructorDoc findConstructor(String constrName,
                                          String[] paramTypes) {
        Names names = tsym.name.table.names;
        for (Scope.Entry e = tsym.members().lookup(names.fromString("<init>")); e.scope != null; e = e.next()) {
            if (e.sym.kind == Kinds.MTH) {
                if (hasParameterTypes((MethodSymbol)e.sym, paramTypes)) {
                    return env.getConstructorDoc((MethodSymbol)e.sym);
                }
            }
        }
        return null;
    }
    public FieldDoc findField(String fieldName) {
        return searchField(fieldName, new HashSet<ClassDocImpl>());
    }
    private FieldDocImpl searchField(String fieldName, Set<ClassDocImpl> searched) {
        Names names = tsym.name.table.names;
        if (searched.contains(this)) {
            return null;
        }
        searched.add(this);
        for (Scope.Entry e = tsym.members().lookup(names.fromString(fieldName)); e.scope != null; e = e.next()) {
            if (e.sym.kind == Kinds.VAR) {
                return env.getFieldDoc((VarSymbol)e.sym);
            }
        }
        ClassDocImpl cdi = (ClassDocImpl)containingClass();
        if (cdi != null) {
            FieldDocImpl fdi = cdi.searchField(fieldName, searched);
            if (fdi != null) {
                return fdi;
            }
        }
        cdi = (ClassDocImpl)superclass();
        if (cdi != null) {
            FieldDocImpl fdi = cdi.searchField(fieldName, searched);
            if (fdi != null) {
                return fdi;
            }
        }
        ClassDoc intf[] = interfaces();
        for (int i = 0; i < intf.length; i++) {
            cdi = (ClassDocImpl)intf[i];
            FieldDocImpl fdi = cdi.searchField(fieldName, searched);
            if (fdi != null) {
                return fdi;
            }
        }
        return null;
    }
    @Deprecated
    public ClassDoc[] importedClasses() {
        if (tsym.sourcefile == null) return new ClassDoc[0];
        ListBuffer<ClassDocImpl> importedClasses = new ListBuffer<ClassDocImpl>();
        Env<AttrContext> compenv = env.enter.getEnv(tsym);
        if (compenv == null) return new ClassDocImpl[0];
        Name asterisk = tsym.name.table.names.asterisk;
        for (JCTree t : compenv.toplevel.defs) {
            if (t.getTag() == JCTree.IMPORT) {
                JCTree imp = ((JCImport) t).qualid;
                if ((TreeInfo.name(imp) != asterisk) &&
                        (imp.type.tsym.kind & Kinds.TYP) != 0) {
                    importedClasses.append(
                            env.getClassDoc((ClassSymbol)imp.type.tsym));
                }
            }
        }
        return importedClasses.toArray(new ClassDocImpl[importedClasses.length()]);
    }
    @Deprecated
    public PackageDoc[] importedPackages() {
        if (tsym.sourcefile == null) return new PackageDoc[0];
        ListBuffer<PackageDocImpl> importedPackages = new ListBuffer<PackageDocImpl>();
        Names names = tsym.name.table.names;
        importedPackages.append(env.getPackageDoc(env.reader.enterPackage(names.java_lang)));
        Env<AttrContext> compenv = env.enter.getEnv(tsym);
        if (compenv == null) return new PackageDocImpl[0];
        for (JCTree t : compenv.toplevel.defs) {
            if (t.getTag() == JCTree.IMPORT) {
                JCTree imp = ((JCImport) t).qualid;
                if (TreeInfo.name(imp) == names.asterisk) {
                    JCFieldAccess sel = (JCFieldAccess)imp;
                    Symbol s = sel.selected.type.tsym;
                    PackageDocImpl pdoc = env.getPackageDoc(s.packge());
                    if (!importedPackages.contains(pdoc))
                        importedPackages.append(pdoc);
                }
            }
        }
        return importedPackages.toArray(new PackageDocImpl[importedPackages.length()]);
    }
    public String dimension() {
        return "";
    }
    public ClassDoc asClassDoc() {
        return this;
    }
    public AnnotationTypeDoc asAnnotationTypeDoc() {
        return null;
    }
    public ParameterizedType asParameterizedType() {
        return null;
    }
    public TypeVariable asTypeVariable() {
        return null;
    }
    public WildcardType asWildcardType() {
        return null;
    }
    public boolean isPrimitive() {
        return false;
    }
    public boolean isSerializable() {
        try {
            return env.types.isSubtype(type, env.syms.serializableType);
        } catch (CompletionFailure ex) {
            return false;
        }
    }
    public boolean isExternalizable() {
        try {
            return env.types.isSubtype(type, env.externalizableSym.type);
        } catch (CompletionFailure ex) {
            return false;
        }
    }
    public MethodDoc[] serializationMethods() {
        if (serializedForm == null) {
            serializedForm = new SerializedForm(env, tsym, this);
        }
        return serializedForm.methods();
    }
    public FieldDoc[] serializableFields() {
        if (serializedForm == null) {
            serializedForm = new SerializedForm(env, tsym, this);
        }
        return serializedForm.fields();
    }
    public boolean definesSerializableFields() {
        if (!isSerializable() || isExternalizable()) {
            return false;
        } else {
            if (serializedForm == null) {
                serializedForm = new SerializedForm(env, tsym, this);
            }
            return serializedForm.definesSerializableFields();
        }
    }
    boolean isRuntimeException() {
        return tsym.isSubClass(env.syms.runtimeExceptionType.tsym, env.types);
    }
    @Override
    public SourcePosition position() {
        if (tsym.sourcefile == null) return null;
        return SourcePositionImpl.make(tsym.sourcefile,
                                       (tree==null) ? Position.NOPOS : tree.pos,
                                       lineMap);
    }
}
