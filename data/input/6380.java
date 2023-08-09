class SourceClass extends ClassDefinition {
    Environment toplevelEnv;
    SourceMember defConstructor;
    ConstantPool tab = new ConstantPool();
    Hashtable deps = new Hashtable(11);
    LocalMember thisArg;
    long endPosition;
    private Type dummyArgumentType = null;
    public SourceClass(Environment env, long where,
                       ClassDeclaration declaration, String documentation,
                       int modifiers, IdentifierToken superClass,
                       IdentifierToken interfaces[],
                       SourceClass outerClass, Identifier localName) {
        super(env.getSource(), where,
              declaration, modifiers, superClass, interfaces);
        setOuterClass(outerClass);
        this.toplevelEnv = env;
        this.documentation = documentation;
        if (ClassDefinition.containsDeprecated(documentation)) {
            this.modifiers |= M_DEPRECATED;
        }
        if (isStatic() && outerClass == null) {
            env.error(where, "static.class", this);
            this.modifiers &=~ M_STATIC;
        }
        if (isLocal() || (outerClass != null && !outerClass.isTopLevel())) {
            if (isInterface()) {
                env.error(where, "inner.interface");
            } else if (isStatic()) {
                env.error(where, "static.inner.class", this);
                this.modifiers &=~ M_STATIC;
                if (innerClassMember != null) {
                    innerClassMember.subModifiers(M_STATIC);
                }
            }
        }
        if (isPrivate() && outerClass == null) {
            env.error(where, "private.class", this);
            this.modifiers &=~ M_PRIVATE;
        }
        if (isProtected() && outerClass == null) {
            env.error(where, "protected.class", this);
            this.modifiers &=~ M_PROTECTED;
        }
        if (!isTopLevel() && !isLocal()) {
            LocalMember outerArg = ((SourceClass)outerClass).getThisArgument();
            UplevelReference r = getReference(outerArg);
            setOuterMember(r.getLocalField(env));
        }
        if (localName != null)
            setLocalName(localName);
        Identifier thisName = getLocalName();
        if (thisName != idNull) {
            for (ClassDefinition scope = outerClass; scope != null;
                  scope = scope.getOuterClass()) {
                Identifier outerName = scope.getLocalName();
                if (thisName.equals(outerName))
                    env.error(where, "inner.redefined", thisName);
            }
        }
    }
    public long getEndPosition() {
        return endPosition;
    }
    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }
    public String getAbsoluteName() {
        String AbsName = ((ClassFile)getSource()).getAbsoluteName();
        return AbsName;
    }
    public Imports getImports() {
        return toplevelEnv.getImports();
    }
    public LocalMember getThisArgument() {
        if (thisArg == null) {
            thisArg = new LocalMember(where, this, 0, getType(), idThis);
        }
        return thisArg;
    }
    public void addDependency(ClassDeclaration c) {
        if (tab != null) {
            tab.put(c);
        }
        if ( toplevelEnv.print_dependencies() && c != getClassDeclaration() ) {
            deps.put(c,c);
        }
    }
    public void addMember(Environment env, MemberDefinition f) {
        switch (f.getModifiers() & (M_PUBLIC | M_PRIVATE | M_PROTECTED)) {
        case M_PUBLIC:
        case M_PRIVATE:
        case M_PROTECTED:
        case 0:
            break;
        default:
            env.error(f.getWhere(), "inconsistent.modifier", f);
            if (f.isPublic()) {
                f.subModifiers(M_PRIVATE | M_PROTECTED);
            } else {
                f.subModifiers(M_PRIVATE);
            }
            break;
        }
        if (f.isStatic() && !isTopLevel() && !f.isSynthetic()) {
            if (f.isMethod()) {
                env.error(f.getWhere(), "static.inner.method", f, this);
                f.subModifiers(M_STATIC);
            } else if (f.isVariable()) {
                if (!f.isFinal() || f.isBlankFinal()) {
                    env.error(f.getWhere(), "static.inner.field", f.getName(), this);
                    f.subModifiers(M_STATIC);
                }
            } else {
                f.subModifiers(M_STATIC);
            }
        }
        if (f.isMethod()) {
            if (f.isConstructor()) {
                if (f.getClassDefinition().isInterface()) {
                    env.error(f.getWhere(), "intf.constructor");
                    return;
                }
                if (f.isNative() || f.isAbstract() ||
                      f.isStatic() || f.isSynchronized() || f.isFinal()) {
                    env.error(f.getWhere(), "constr.modifier", f);
                    f.subModifiers(M_NATIVE | M_ABSTRACT |
                                   M_STATIC | M_SYNCHRONIZED | M_FINAL);
                }
            } else if (f.isInitializer()) {
                if (f.getClassDefinition().isInterface()) {
                    env.error(f.getWhere(), "intf.initializer");
                    return;
                }
            }
            if ((f.getType().getReturnType()).isVoidArray()) {
                env.error(f.getWhere(), "void.array");
            }
            if (f.getClassDefinition().isInterface() &&
                (f.isStatic() || f.isSynchronized() || f.isNative()
                 || f.isFinal() || f.isPrivate() || f.isProtected())) {
                env.error(f.getWhere(), "intf.modifier.method", f);
                f.subModifiers(M_STATIC |  M_SYNCHRONIZED | M_NATIVE |
                               M_FINAL | M_PRIVATE);
            }
            if (f.isTransient()) {
                env.error(f.getWhere(), "transient.meth", f);
                f.subModifiers(M_TRANSIENT);
            }
            if (f.isVolatile()) {
                env.error(f.getWhere(), "volatile.meth", f);
                f.subModifiers(M_VOLATILE);
            }
            if (f.isAbstract()) {
                if (f.isPrivate()) {
                    env.error(f.getWhere(), "abstract.private.modifier", f);
                    f.subModifiers(M_PRIVATE);
                }
                if (f.isStatic()) {
                    env.error(f.getWhere(), "abstract.static.modifier", f);
                    f.subModifiers(M_STATIC);
                }
                if (f.isFinal()) {
                    env.error(f.getWhere(), "abstract.final.modifier", f);
                    f.subModifiers(M_FINAL);
                }
                if (f.isNative()) {
                    env.error(f.getWhere(), "abstract.native.modifier", f);
                    f.subModifiers(M_NATIVE);
                }
                if (f.isSynchronized()) {
                    env.error(f.getWhere(),"abstract.synchronized.modifier",f);
                    f.subModifiers(M_SYNCHRONIZED);
                }
            }
            if (f.isAbstract() || f.isNative()) {
                if (f.getValue() != null) {
                    env.error(f.getWhere(), "invalid.meth.body", f);
                    f.setValue(null);
                }
            } else {
                if (f.getValue() == null) {
                    if (f.isConstructor()) {
                        env.error(f.getWhere(), "no.constructor.body", f);
                    } else {
                        env.error(f.getWhere(), "no.meth.body", f);
                    }
                    f.addModifiers(M_ABSTRACT);
                }
            }
            Vector arguments = f.getArguments();
            if (arguments != null) {
                int argumentLength = arguments.size();
                Type argTypes[] = f.getType().getArgumentTypes();
                for (int i = 0; i < argTypes.length; i++) {
                    Object arg = arguments.elementAt(i);
                    long where = f.getWhere();
                    if (arg instanceof MemberDefinition) {
                        where = ((MemberDefinition)arg).getWhere();
                        arg = ((MemberDefinition)arg).getName();
                    }
                    if (argTypes[i].isType(TC_VOID)
                        || argTypes[i].isVoidArray()) {
                        env.error(where, "void.argument", arg);
                    }
                }
            }
        } else if (f.isInnerClass()) {
            if (f.isVolatile() ||
                f.isTransient() || f.isNative() || f.isSynchronized()) {
                env.error(f.getWhere(), "inner.modifier", f);
                f.subModifiers(M_VOLATILE | M_TRANSIENT |
                               M_NATIVE | M_SYNCHRONIZED);
            }
            if (f.getClassDefinition().isInterface() &&
                  (f.isPrivate() || f.isProtected())) {
                env.error(f.getWhere(), "intf.modifier.field", f);
                f.subModifiers(M_PRIVATE | M_PROTECTED);
                f.addModifiers(M_PUBLIC);
                ClassDefinition c = f.getInnerClass();
                c.subModifiers(M_PRIVATE | M_PROTECTED);
                c.addModifiers(M_PUBLIC);
            }
        } else {
            if (f.getType().isType(TC_VOID) || f.getType().isVoidArray()) {
                env.error(f.getWhere(), "void.inst.var", f.getName());
                return;
            }
            if (f.isSynchronized() || f.isAbstract() || f.isNative()) {
                env.error(f.getWhere(), "var.modifier", f);
                f.subModifiers(M_SYNCHRONIZED | M_ABSTRACT | M_NATIVE);
            }
            if (f.isStrict()) {
                env.error(f.getWhere(), "var.floatmodifier", f);
                f.subModifiers(M_STRICTFP);
            }
            if (f.isTransient() && isInterface()) {
                env.error(f.getWhere(), "transient.modifier", f);
                f.subModifiers(M_TRANSIENT);
            }
            if (f.isVolatile() && (isInterface() || f.isFinal())) {
                env.error(f.getWhere(), "volatile.modifier", f);
                f.subModifiers(M_VOLATILE);
            }
            if (f.isFinal() && (f.getValue() == null) && isInterface()) {
                env.error(f.getWhere(), "initializer.needed", f);
                f.subModifiers(M_FINAL);
            }
            if (f.getClassDefinition().isInterface() &&
                  (f.isPrivate() || f.isProtected())) {
                env.error(f.getWhere(), "intf.modifier.field", f);
                f.subModifiers(M_PRIVATE | M_PROTECTED);
                f.addModifiers(M_PUBLIC);
            }
        }
        if (!f.isInitializer()) {
            for (MemberDefinition f2 = getFirstMatch(f.getName());
                         f2 != null; f2 = f2.getNextMatch()) {
                if (f.isVariable() && f2.isVariable()) {
                    env.error(f.getWhere(), "var.multidef", f, f2);
                    return;
                } else if (f.isInnerClass() && f2.isInnerClass() &&
                           !f.getInnerClass().isLocal() &&
                           !f2.getInnerClass().isLocal()) {
                    env.error(f.getWhere(), "inner.class.multidef", f);
                    return;
                }
            }
        }
        super.addMember(env, f);
    }
    public Environment setupEnv(Environment env) {
        return new Environment(toplevelEnv, this);
    }
    public boolean reportDeprecated(Environment env) {
        return false;
    }
    public void noteUsedBy(ClassDefinition ref, long where, Environment env) {
        super.noteUsedBy(ref, where, env);
        ClassDefinition def = this;
        while (def.isInnerClass()) {
            def = def.getOuterClass();
        }
        if (def.isPublic()) {
            return;             
        }
        while (ref.isInnerClass()) {
            ref = ref.getOuterClass();
        }
        if (def.getSource().equals(ref.getSource())) {
            return;             
        }
        ((SourceClass)def).checkSourceFile(env, where);
    }
    public void check(Environment env) throws ClassNotFound {
        if (tracing) env.dtEnter("SourceClass.check: " + getName());
        if (isInsideLocal()) {
            if (tracing) env.dtEvent("SourceClass.check: INSIDE LOCAL " +
                                     getOuterClass().getName());
            getOuterClass().check(env);
        } else {
            if (isInnerClass()) {
                if (tracing) env.dtEvent("SourceClass.check: INNER CLASS " +
                                         getOuterClass().getName());
                ((SourceClass)getOuterClass()).maybeCheck(env);
            }
            Vset vset = new Vset();
            Context ctx = null;
            if (tracing)
                env.dtEvent("SourceClass.check: CHECK INTERNAL " + getName());
            vset = checkInternal(setupEnv(env), ctx, vset);
        }
        if (tracing) env.dtExit("SourceClass.check: " + getName());
    }
    private void maybeCheck(Environment env) throws ClassNotFound {
        if (tracing) env.dtEvent("SourceClass.maybeCheck: " + getName());
        ClassDeclaration c = getClassDeclaration();
        if (c.getStatus() == CS_PARSED) {
            c.setDefinition(this, CS_CHECKED);
            check(env);
        }
    }
    private Vset checkInternal(Environment env, Context ctx, Vset vset)
                throws ClassNotFound {
        Identifier nm = getClassDeclaration().getName();
        if (env.verbose()) {
            env.output("[checking class " + nm + "]");
        }
        classContext = ctx;
        basicCheck(Context.newEnvironment(env, ctx));
        ClassDeclaration sup = getSuperClass();
        if (sup != null) {
            long where = getWhere();
            where = IdentifierToken.getWhere(superClassId, where);
            env.resolveExtendsByName(where, this, sup.getName());
        }
        for (int i = 0 ; i < interfaces.length ; i++) {
            ClassDeclaration intf = interfaces[i];
            long where = getWhere();
            if (interfaceIds != null
                && interfaceIds.length == interfaces.length) {
                where = IdentifierToken.getWhere(interfaceIds[i], where);
            }
            env.resolveExtendsByName(where, this, intf.getName());
        }
        if (!isInnerClass() && !isInsideLocal()) {
            Identifier simpleName = nm.getName();
            try {
                Imports imports = toplevelEnv.getImports();
                Identifier ID = imports.resolve(env, simpleName);
                if (ID != getName())
                    env.error(where, "class.multidef.import", simpleName, ID);
            } catch (AmbiguousClass e) {
                Identifier ID = (e.name1 != getName()) ? e.name1 : e.name2;
                env.error(where, "class.multidef.import", simpleName, ID);
            }  catch (ClassNotFound e) {
            }
            if (isPublic()) {
                checkSourceFile(env, getWhere());
            }
        }
        vset = checkMembers(env, ctx, vset);
        return vset;
    }
    private boolean sourceFileChecked = false;
    public void checkSourceFile(Environment env, long where) {
        if (sourceFileChecked)  return;
        sourceFileChecked = true;
        String fname = getName().getName() + ".java";
        String src = ((ClassFile)getSource()).getName();
        if (!src.equals(fname)) {
            if (isPublic()) {
                env.error(where, "public.class.file", this, fname);
            } else {
                env.error(where, "warn.package.class.file", this, src, fname);
            }
        }
    }
    private boolean supersChecked = false;
    public ClassDeclaration getSuperClass(Environment env) {
        if (tracing) env.dtEnter("SourceClass.getSuperClass: " + this);
        if (superClass == null && superClassId != null && !supersChecked) {
            resolveTypeStructure(env);
        }
        if (tracing) env.dtExit("SourceClass.getSuperClass: " + this);
        return superClass;
    }
    private void checkSupers(Environment env) throws ClassNotFound {
        supersCheckStarted = true;
        if (tracing) env.dtEnter("SourceClass.checkSupers: " + this);
        if (isInterface()) {
            if (isFinal()) {
                Identifier nm = getClassDeclaration().getName();
                env.error(getWhere(), "final.intf", nm);
            }
        } else {
            if (getSuperClass(env) != null) {
                long where = getWhere();
                where = IdentifierToken.getWhere(superClassId, where);
                try {
                    ClassDefinition def =
                        getSuperClass().getClassDefinition(env);
                    def.resolveTypeStructure(env);
                    if (!extendsCanAccess(env, getSuperClass())) {
                        env.error(where, "cant.access.class", getSuperClass());
                        superClass = null;
                    } else if (def.isFinal()) {
                        env.error(where, "super.is.final", getSuperClass());
                        superClass = null;
                    } else if (def.isInterface()) {
                        env.error(where, "super.is.intf", getSuperClass());
                        superClass = null;
                    } else if (superClassOf(env, getSuperClass())) {
                        env.error(where, "cyclic.super");
                        superClass = null;
                    } else {
                        def.noteUsedBy(this, where, env);
                    }
                    if (superClass == null) {
                        def = null;
                    } else {
                        ClassDefinition sup = def;
                        for (;;) {
                            if (enclosingClassOf(sup)) {
                                env.error(where, "super.is.inner");
                                superClass = null;
                                break;
                            }
                            ClassDeclaration s = sup.getSuperClass(env);
                            if (s == null) {
                                break;
                            }
                            sup = s.getClassDefinition(env);
                        }
                    }
                } catch (ClassNotFound e) {
                reportError: {
                        try {
                            env.resolve(e.name);
                        } catch (AmbiguousClass ee) {
                            env.error(where,
                                      "ambig.class", ee.name1, ee.name2);
                            superClass = null;
                            break reportError;
                        } catch (ClassNotFound ee) {
                        }
                        env.error(where, "super.not.found", e.name, this);
                        superClass = null;
                    } 
                }
            } else {
                if (isAnonymous()) {
                    throw new CompilerError("anonymous super");
                } else  if (!getName().equals(idJavaLangObject)) {
                    throw new CompilerError("unresolved super");
                }
            }
        }
        supersChecked = true;
        for (int i = 0 ; i < interfaces.length ; i++) {
            ClassDeclaration intf = interfaces[i];
            long where = getWhere();
            if (interfaceIds != null
                && interfaceIds.length == interfaces.length) {
                where = IdentifierToken.getWhere(interfaceIds[i], where);
            }
            try {
                ClassDefinition def = intf.getClassDefinition(env);
                def.resolveTypeStructure(env);
                if (!extendsCanAccess(env, intf)) {
                    env.error(where, "cant.access.class", intf);
                } else if (!intf.getClassDefinition(env).isInterface()) {
                    env.error(where, "not.intf", intf);
                } else if (isInterface() && implementedBy(env, intf)) {
                    env.error(where, "cyclic.intf", intf);
                } else {
                    def.noteUsedBy(this, where, env);
                    continue;
                }
            } catch (ClassNotFound e) {
            reportError2: {
                    try {
                        env.resolve(e.name);
                    } catch (AmbiguousClass ee) {
                        env.error(where,
                                  "ambig.class", ee.name1, ee.name2);
                        superClass = null;
                        break reportError2;
                    } catch (ClassNotFound ee) {
                    }
                    env.error(where, "intf.not.found", e.name, this);
                    superClass = null;
                } 
            }
            ClassDeclaration newInterfaces[] =
                new ClassDeclaration[interfaces.length - 1];
            System.arraycopy(interfaces, 0, newInterfaces, 0, i);
            System.arraycopy(interfaces, i + 1, newInterfaces, i,
                             newInterfaces.length - i);
            interfaces = newInterfaces;
            --i;
        }
        if (tracing) env.dtExit("SourceClass.checkSupers: " + this);
    }
    private Vset checkMembers(Environment env, Context ctx, Vset vset)
            throws ClassNotFound {
        if (getError()) {
            return vset;
        }
        for (MemberDefinition f = getFirstMember();
                     f != null; f = f.getNextMember()) {
            if (f.isInnerClass()) {
                SourceClass cdef = (SourceClass) f.getInnerClass();
                if (cdef.isMember()) {
                    cdef.basicCheck(env);
                }
            }
        }
        if (isFinal() && isAbstract()) {
            env.error(where, "final.abstract", this.getName().getName());
        }
        if (!isInterface() && !isAbstract() && mustBeAbstract(env)) {
            modifiers |= M_ABSTRACT;
            Iterator iter = getPermanentlyAbstractMethods();
            while (iter.hasNext()) {
                MemberDefinition method = (MemberDefinition) iter.next();
                env.error(where, "abstract.class.cannot.override",
                          getClassDeclaration(), method,
                          method.getDefiningClassDeclaration());
            }
            iter = getMethods(env);
            while (iter.hasNext()) {
                MemberDefinition method = (MemberDefinition) iter.next();
                if (method.isAbstract()) {
                    env.error(where, "abstract.class",
                              getClassDeclaration(), method,
                              method.getDefiningClassDeclaration());
                }
            }
        }
        Context ctxInit = new Context(ctx);
        Vset vsInst = vset.copy();
        Vset vsClass = vset.copy();
        for (MemberDefinition f = getFirstMember();
                     f != null; f = f.getNextMember()) {
            if (f.isVariable() && f.isBlankFinal()) {
                int number = ctxInit.declareFieldNumber(f);
                if (f.isStatic()) {
                    vsClass = vsClass.addVarUnassigned(number);
                    vsInst = vsInst.addVar(number);
                } else {
                    vsInst = vsInst.addVarUnassigned(number);
                    vsClass = vsClass.addVar(number);
                }
            }
        }
        Context ctxInst = new Context(ctxInit, this);
        LocalMember thisArg = getThisArgument();
        int thisNumber = ctxInst.declare(env, thisArg);
        vsInst = vsInst.addVar(thisNumber);
        for (MemberDefinition f = getFirstMember();
                     f != null; f = f.getNextMember()) {
            try {
                if (f.isVariable() || f.isInitializer()) {
                    if (f.isStatic()) {
                        vsClass = f.check(env, ctxInit, vsClass);
                    } else {
                        vsInst = f.check(env, ctxInst, vsInst);
                    }
                }
            } catch (ClassNotFound ee) {
                env.error(f.getWhere(), "class.not.found", ee.name, this);
            }
        }
        checkBlankFinals(env, ctxInit, vsClass, true);
        for (MemberDefinition f = getFirstMember();
                     f != null; f = f.getNextMember()) {
            try {
                if (f.isConstructor()) {
                    Vset vsCon = f.check(env, ctxInit, vsInst.copy());
                    checkBlankFinals(env, ctxInit, vsCon, false);
                } else {
                    Vset vsFld = f.check(env, ctx, vset.copy());
                }
            } catch (ClassNotFound ee) {
                env.error(f.getWhere(), "class.not.found", ee.name, this);
            }
        }
        getClassDeclaration().setDefinition(this, CS_CHECKED);
        for (MemberDefinition f = getFirstMember();
                     f != null; f = f.getNextMember()) {
            if (f.isInnerClass()) {
                SourceClass cdef = (SourceClass) f.getInnerClass();
                if (!cdef.isInsideLocal()) {
                    cdef.maybeCheck(env);
                }
            }
        }
        return vset;
    }
    private void checkBlankFinals(Environment env, Context ctxInit, Vset vset,
                                  boolean isStatic) {
        for (int i = 0; i < ctxInit.getVarNumber(); i++) {
            if (!vset.testVar(i)) {
                MemberDefinition ff = ctxInit.getElement(i);
                if (ff != null && ff.isBlankFinal()
                    && ff.isStatic() == isStatic
                    && ff.getClassDefinition() == this) {
                    env.error(ff.getWhere(),
                              "final.var.not.initialized", ff.getName());
                }
            }
        }
    }
    private boolean basicChecking = false;
    private boolean basicCheckDone = false;
    protected void basicCheck(Environment env) throws ClassNotFound {
        if (tracing) env.dtEnter("SourceClass.basicCheck: " + getName());
        super.basicCheck(env);
        if (basicChecking || basicCheckDone) {
            if (tracing) env.dtExit("SourceClass.basicCheck: OK " + getName());
            return;
        }
        if (tracing) env.dtEvent("SourceClass.basicCheck: CHECKING " + getName());
        basicChecking = true;
        env = setupEnv(env);
        Imports imports = env.getImports();
        if (imports != null) {
            imports.resolve(env);
        }
        resolveTypeStructure(env);
        if (!isInterface()) {
            if (!hasConstructor()) {
                Node code = new CompoundStatement(getWhere(), new Statement[0]);
                Type t = Type.tMethod(Type.tVoid);
                int accessModifiers = getModifiers() &
                    (isInnerClass() ? (M_PUBLIC | M_PROTECTED) : M_PUBLIC);
                env.makeMemberDefinition(env, getWhere(), this, null,
                                         accessModifiers,
                                         t, idInit, null, null, code);
            }
        }
        if (doInheritanceChecks) {
            collectInheritedMethods(env);
        }
        basicChecking = false;
        basicCheckDone = true;
        if (tracing) env.dtExit("SourceClass.basicCheck: " + getName());
    }
    protected void addMirandaMethods(Environment env,
                                     Iterator mirandas) {
        while(mirandas.hasNext()) {
            MemberDefinition method =
                (MemberDefinition)mirandas.next();
            addMember(method);
        }
    }
    private boolean resolving = false;
    public void resolveTypeStructure(Environment env) {
        if (tracing)
            env.dtEnter("SourceClass.resolveTypeStructure: " + getName());
        ClassDefinition oc = getOuterClass();
        if (oc != null && oc instanceof SourceClass
            && !((SourceClass)oc).resolved) {
            ((SourceClass)oc).resolveTypeStructure(env);
        }
        if (resolved || resolving) {
            if (tracing)
                env.dtExit("SourceClass.resolveTypeStructure: OK " + getName());
            return;
        }
        resolving = true;
        if (tracing)
            env.dtEvent("SourceClass.resolveTypeStructure: RESOLVING " + getName());
        env = setupEnv(env);
        resolveSupers(env);
        try {
            checkSupers(env);
        } catch (ClassNotFound ee) {
            env.error(where, "class.not.found", ee.name, this);
        }
        for (MemberDefinition
                 f = getFirstMember() ; f != null ; f = f.getNextMember()) {
            if (f instanceof SourceMember)
                ((SourceMember)f).resolveTypeStructure(env);
        }
        resolving = false;
        resolved = true;
        for (MemberDefinition
                 f = getFirstMember() ; f != null ; f = f.getNextMember()) {
            if (f.isInitializer())  continue;
            if (!f.isMethod())  continue;
            for (MemberDefinition f2 = f; (f2 = f2.getNextMatch()) != null; ) {
                if (!f2.isMethod())  continue;
                if (f.getType().equals(f2.getType())) {
                    env.error(f.getWhere(), "meth.multidef", f);
                    continue;
                }
                if (f.getType().equalArguments(f2.getType())) {
                    env.error(f.getWhere(), "meth.redef.rettype", f, f2);
                    continue;
                }
            }
        }
        if (tracing)
            env.dtExit("SourceClass.resolveTypeStructure: " + getName());
    }
    protected void resolveSupers(Environment env) {
        if (tracing)
            env.dtEnter("SourceClass.resolveSupers: " + this);
        if (superClassId != null && superClass == null) {
            superClass = resolveSuper(env, superClassId);
            if (superClass == getClassDeclaration()
                && getName().equals(idJavaLangObject)) {
                    superClass = null;
                    superClassId = null;
            }
        }
        if (interfaceIds != null && interfaces == null) {
            interfaces = new ClassDeclaration[interfaceIds.length];
            for (int i = 0 ; i < interfaces.length ; i++) {
                interfaces[i] = resolveSuper(env, interfaceIds[i]);
                for (int j = 0; j < i; j++) {
                    if (interfaces[i] == interfaces[j]) {
                        Identifier id = interfaceIds[i].getName();
                        long where = interfaceIds[j].getWhere();
                        env.error(where, "intf.repeated", id);
                    }
                }
            }
        }
        if (tracing)
            env.dtExit("SourceClass.resolveSupers: " + this);
    }
    private ClassDeclaration resolveSuper(Environment env, IdentifierToken t) {
        Identifier name = t.getName();
        if (tracing)
            env.dtEnter("SourceClass.resolveSuper: " + name);
        if (isInnerClass())
            name = outerClass.resolveName(env, name);
        else
            name = env.resolveName(name);
        ClassDeclaration result = env.getClassDeclaration(name);
        if (tracing) env.dtExit("SourceClass.resolveSuper: " + name);
        return result;
    }
    public Vset checkLocalClass(Environment env, Context ctx, Vset vset,
                                ClassDefinition sup,
                                Expression args[], Type argTypes[]
                                ) throws ClassNotFound {
        env = setupEnv(env);
        if ((sup != null) != isAnonymous()) {
            throw new CompilerError("resolveAnonymousStructure");
        }
        if (isAnonymous()) {
            resolveAnonymousStructure(env, sup, args, argTypes);
        }
        vset = checkInternal(env, ctx, vset);
        return vset;
    }
    public void inlineLocalClass(Environment env) {
        for (MemberDefinition
                 f = getFirstMember(); f != null; f = f.getNextMember()) {
            if ((f.isVariable() || f.isInitializer()) && !f.isStatic()) {
                continue;       
            }
            try {
                ((SourceMember)f).inline(env);
            } catch (ClassNotFound ee) {
                env.error(f.getWhere(), "class.not.found", ee.name, this);
            }
        }
        if (getReferencesFrozen() != null && !inlinedLocalClass) {
            inlinedLocalClass = true;
            for (MemberDefinition
                     f = getFirstMember(); f != null; f = f.getNextMember()) {
                if (f.isConstructor()) {
                    ((SourceMember)f).addUplevelArguments();
                }
            }
        }
    }
    private boolean inlinedLocalClass = false;
    public Vset checkInsideClass(Environment env, Context ctx, Vset vset)
                throws ClassNotFound {
        if (!isInsideLocal() || isLocal()) {
            throw new CompilerError("checkInsideClass");
        }
        return checkInternal(env, ctx, vset);
    }
    private void resolveAnonymousStructure(Environment env,
                                           ClassDefinition sup,
                                           Expression args[], Type argTypes[]
                                           ) throws ClassNotFound {
        if (tracing) env.dtEvent("SourceClass.resolveAnonymousStructure: " +
                                 this + ", super " + sup);
        if (sup.isInterface()) {
            int ni = (interfaces == null) ? 0 : interfaces.length;
            ClassDeclaration i1[] = new ClassDeclaration[1+ni];
            if (ni > 0) {
                System.arraycopy(interfaces, 0, i1, 1, ni);
                if (interfaceIds != null && interfaceIds.length == ni) {
                    IdentifierToken id1[] = new IdentifierToken[1+ni];
                    System.arraycopy(interfaceIds, 0, id1, 1, ni);
                    id1[0] = new IdentifierToken(sup.getName());
                }
            }
            i1[0] = sup.getClassDeclaration();
            interfaces = i1;
            sup = toplevelEnv.getClassDefinition(idJavaLangObject);
        }
        superClass = sup.getClassDeclaration();
        if (hasConstructor()) {
            throw new CompilerError("anonymous constructor");
        }
        Type t = Type.tMethod(Type.tVoid, argTypes);
        IdentifierToken names[] = new IdentifierToken[argTypes.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = new IdentifierToken(args[i].getWhere(),
                                           Identifier.lookup("$"+i));
        }
        int outerArg = (sup.isTopLevel() || sup.isLocal()) ? 0 : 1;
        Expression superArgs[] = new Expression[-outerArg + args.length];
        for (int i = outerArg ; i < args.length ; i++) {
            superArgs[-outerArg + i] = new IdentifierExpression(names[i]);
        }
        long where = getWhere();
        Expression superExp;
        if (outerArg == 0) {
            superExp = new SuperExpression(where);
        } else {
            superExp = new SuperExpression(where,
                                           new IdentifierExpression(names[0]));
        }
        Expression superCall = new MethodExpression(where,
                                                    superExp, idInit,
                                                    superArgs);
        Statement body[] = { new ExpressionStatement(where, superCall) };
        Node code = new CompoundStatement(where, body);
        int mod = M_SYNTHETIC; 
        env.makeMemberDefinition(env, where, this, null,
                                mod, t, idInit, names, null, code);
    }
    private static int classModifierBits[] =
        { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL,
          ACC_INTERFACE, ACC_ABSTRACT, ACC_SUPER, M_ANONYMOUS, M_LOCAL,
          M_STRICTFP, ACC_STRICT};
    private static String classModifierNames[] =
        { "PUBLIC", "PRIVATE", "PROTECTED", "STATIC", "FINAL",
          "INTERFACE", "ABSTRACT", "SUPER", "ANONYMOUS", "LOCAL",
          "STRICTFP", "STRICT"};
    static String classModifierString(int mods) {
        String s = "";
        for (int i = 0; i < classModifierBits.length; i++) {
            if ((mods & classModifierBits[i]) != 0) {
                s = s + " " + classModifierNames[i];
                mods &= ~classModifierBits[i];
            }
        }
        if (mods != 0) {
            s = s + " ILLEGAL:" + Integer.toHexString(mods);
        }
        return s;
    }
    public MemberDefinition getAccessMember(Environment env, Context ctx,
                                          MemberDefinition field, boolean isSuper) {
        return getAccessMember(env, ctx, field, false, isSuper);
    }
    public MemberDefinition getUpdateMember(Environment env, Context ctx,
                                          MemberDefinition field, boolean isSuper) {
        if (!field.isVariable()) {
            throw new CompilerError("method");
        }
        return getAccessMember(env, ctx, field, true, isSuper);
    }
    private MemberDefinition getAccessMember(Environment env, Context ctx,
                                             MemberDefinition field,
                                             boolean isUpdate,
                                             boolean isSuper) {
        boolean isStatic = field.isStatic();
        boolean isMethod = field.isMethod();
        MemberDefinition af;
        for (af = getFirstMember(); af != null; af = af.getNextMember()) {
            if (af.getAccessMethodTarget() == field) {
                if (isMethod && af.isSuperAccessMethod() == isSuper) {
                    break;
                }
                int nargs = af.getType().getArgumentTypes().length;
                if (nargs == (isStatic ? 0 : 1)) {
                    break;
                }
            }
        }
        if (af != null) {
            if (!isUpdate) {
                return af;
            } else {
                MemberDefinition uf = af.getAccessUpdateMember();
                if (uf != null) {
                    return uf;
                }
            }
        } else if (isUpdate) {
            af = getAccessMember(env, ctx, field, false, isSuper);
        }
        Identifier anm;
        Type dummyType = null;
        if (field.isConstructor()) {
            anm = idInit;
            SourceClass outerMostClass = (SourceClass)getTopClass();
            dummyType = outerMostClass.dummyArgumentType;
            if (dummyType == null) {
                IdentifierToken sup =
                    new IdentifierToken(0, idJavaLangObject);
                IdentifierToken interfaces[] = {};
                IdentifierToken t = new IdentifierToken(0, idNull);
                int mod = M_ANONYMOUS | M_STATIC | M_SYNTHETIC;
                if (outerMostClass.isInterface()) {
                    mod |= M_PUBLIC;
                }
                ClassDefinition dummyClass =
                    toplevelEnv.makeClassDefinition(toplevelEnv,
                                                    0, t, null, mod,
                                                    sup, interfaces,
                                                    outerMostClass);
                dummyClass.getClassDeclaration().setDefinition(dummyClass, CS_PARSED);
                Expression argsX[] = {};
                Type argTypesX[] = {};
                try {
                    ClassDefinition supcls =
                        toplevelEnv.getClassDefinition(idJavaLangObject);
                    dummyClass.checkLocalClass(toplevelEnv, null,
                                               new Vset(), supcls, argsX, argTypesX);
                } catch (ClassNotFound ee) {};
                dummyType = dummyClass.getType();
                outerMostClass.dummyArgumentType = dummyType;
            }
        } else {
            for (int i = 0; ; i++) {
                anm = Identifier.lookup(prefixAccess + i);
                if (getFirstMatch(anm) == null) {
                    break;
                }
            }
        }
        Type argTypes[];
        Type t = field.getType();
        if (isStatic) {
            if (!isMethod) {
                if (!isUpdate) {
                    Type at[] = { };
                    argTypes = at;
                    t = Type.tMethod(t); 
                } else {
                    Type at[] = { t };
                    argTypes = at;
                    t = Type.tMethod(Type.tVoid, argTypes); 
                }
            } else {
                argTypes = t.getArgumentTypes();
            }
        } else {
            Type classType = this.getType();
            if (!isMethod) {
                if (!isUpdate) {
                    Type at[] = { classType };
                    argTypes = at;
                    t = Type.tMethod(t, argTypes); 
                } else {
                    Type at[] = { classType, t };
                    argTypes = at;
                    t = Type.tMethod(Type.tVoid, argTypes); 
                }
            } else {
                Type at[] = t.getArgumentTypes();
                int nargs = at.length;
                if (field.isConstructor()) {
                    MemberDefinition outerThisArg =
                        ((SourceMember)field).getOuterThisArg();
                    if (outerThisArg != null) {
                        if (at[0] != outerThisArg.getType()) {
                            throw new CompilerError("misplaced outer this");
                        }
                        argTypes = new Type[nargs];
                        argTypes[0] = dummyType;
                        for (int i = 1; i < nargs; i++) {
                            argTypes[i] = at[i];
                        }
                    } else {
                        argTypes = new Type[nargs+1];
                        argTypes[0] = dummyType;
                        for (int i = 0; i < nargs; i++) {
                            argTypes[i+1] = at[i];
                        }
                    }
                } else {
                    argTypes = new Type[nargs+1];
                    argTypes[0] = classType;
                    for (int i = 0; i < nargs; i++) {
                        argTypes[i+1] = at[i];
                    }
                }
                t = Type.tMethod(t.getReturnType(), argTypes);
            }
        }
        int nlen = argTypes.length;
        long where = field.getWhere();
        IdentifierToken names[] = new IdentifierToken[nlen];
        for (int i = 0; i < nlen; i++) {
            names[i] = new IdentifierToken(where, Identifier.lookup("$"+i));
        }
        Expression access = null;
        Expression thisArg = null;
        Expression args[] = null;
        if (isStatic) {
            args = new Expression[nlen];
            for (int i = 0 ; i < nlen ; i++) {
                args[i] = new IdentifierExpression(names[i]);
            }
        } else {
            if (field.isConstructor()) {
                thisArg = new ThisExpression(where);
                args = new Expression[nlen-1];
                for (int i = 1 ; i < nlen ; i++) {
                    args[i-1] = new IdentifierExpression(names[i]);
                }
            } else {
                thisArg = new IdentifierExpression(names[0]);
                args = new Expression[nlen-1];
                for (int i = 1 ; i < nlen ; i++) {
                    args[i-1] = new IdentifierExpression(names[i]);
                }
            }
            access = thisArg;
        }
        if (!isMethod) {
            access = new FieldExpression(where, access, field);
            if (isUpdate) {
                access = new AssignExpression(where, access, args[0]);
            }
        } else {
            access = new MethodExpression(where, access, field, args, isSuper);
        }
        Statement code;
        if (t.getReturnType().isType(TC_VOID)) {
            code = new ExpressionStatement(where, access);
        } else {
            code = new ReturnStatement(where, access);
        }
        Statement body[] = { code };
        code = new CompoundStatement(where, body);
        int mod = M_SYNTHETIC;
        if (!field.isConstructor()) {
            mod |= M_STATIC;
        }
        SourceMember newf = (SourceMember)
            env.makeMemberDefinition(env, where, this,
                                     null, mod, t, anm, names,
                                     field.getExceptionIds(), code);
        newf.setExceptions(field.getExceptions(env));
        newf.setAccessMethodTarget(field);
        if (isUpdate) {
            af.setAccessUpdateMember(newf);
        }
        newf.setIsSuperAccessMethod(isSuper);
        Context checkContext = newf.getClassDefinition().getClassContext();
        if (checkContext != null) {
            try {
                newf.check(env, checkContext, new Vset());
            } catch (ClassNotFound ee) {
                env.error(where, "class.not.found", ee.name, this);
            }
        }
        return newf;
    }
    SourceClass findLookupContext() {
        for (MemberDefinition f = getFirstMember();
             f != null;
             f = f.getNextMember()) {
            if (f.isInnerClass()) {
                SourceClass ic = (SourceClass)f.getInnerClass();
                if (!ic.isInterface()) {
                    return ic;
                }
            }
        }
        for (MemberDefinition f = getFirstMember();
             f != null;
             f = f.getNextMember()) {
            if (f.isInnerClass()) {
                SourceClass lc =
                    ((SourceClass)f.getInnerClass()).findLookupContext();
                if (lc != null) {
                    return lc;
                }
            }
        }
        return null;
    }
    private MemberDefinition lookup = null;
    public MemberDefinition getClassLiteralLookup(long fwhere) {
        if (lookup != null) {
            return lookup;
        }
        if (outerClass != null) {
            lookup = outerClass.getClassLiteralLookup(fwhere);
            return lookup;
        }
        ClassDefinition c = this;
        boolean needNewClass = false;
        if (isInterface()) {
            c = findLookupContext();
            if (c == null) {
                needNewClass = true;
                IdentifierToken sup =
                    new IdentifierToken(fwhere, idJavaLangObject);
                IdentifierToken interfaces[] = {};
                IdentifierToken t = new IdentifierToken(fwhere, idNull);
                int mod = M_PUBLIC | M_ANONYMOUS | M_STATIC | M_SYNTHETIC;
                c = (SourceClass)
                    toplevelEnv.makeClassDefinition(toplevelEnv,
                                                    fwhere, t, null, mod,
                                                    sup, interfaces, this);
            }
        }
        Identifier idDClass = Identifier.lookup(prefixClass);
        Type strarg[] = { Type.tString };
        long w = c.getWhere();
        IdentifierToken arg = new IdentifierToken(w, idDClass);
        Expression e = new IdentifierExpression(arg);
        Expression a1[] = { e };
        Identifier idForName = Identifier.lookup("forName");
        e = new MethodExpression(w, new TypeExpression(w, Type.tClassDesc),
                                 idForName, a1);
        Statement body = new ReturnStatement(w, e);
        Identifier idClassNotFound =
            Identifier.lookup("java.lang.ClassNotFoundException");
        Identifier idNoClassDefFound =
            Identifier.lookup("java.lang.NoClassDefFoundError");
        Type ctyp = Type.tClass(idClassNotFound);
        Type exptyp = Type.tClass(idNoClassDefFound);
        Identifier idGetMessage = Identifier.lookup("getMessage");
        e = new IdentifierExpression(w, idForName);
        e = new MethodExpression(w, e, idGetMessage, new Expression[0]);
        Expression a2[] = { e };
        e = new NewInstanceExpression(w, new TypeExpression(w, exptyp), a2);
        Statement handler = new CatchStatement(w, new TypeExpression(w, ctyp),
                                               new IdentifierToken(idForName),
                                               new ThrowStatement(w, e));
        Statement handlers[] = { handler };
        body = new TryStatement(w, body, handlers);
        Type mtype = Type.tMethod(Type.tClassDesc, strarg);
        IdentifierToken args[] = { arg };
        lookup = toplevelEnv.makeMemberDefinition(toplevelEnv, w,
                                                  c, null,
                                                  M_STATIC | M_SYNTHETIC,
                                                  mtype, idDClass,
                                                  args, null, body);
        if (needNewClass) {
            if (c.getClassDeclaration().getStatus() == CS_CHECKED) {
                throw new CompilerError("duplicate check");
            }
            c.getClassDeclaration().setDefinition(c, CS_PARSED);
            Expression argsX[] = {};
            Type argTypesX[] = {};
            try {
                ClassDefinition sup =
                    toplevelEnv.getClassDefinition(idJavaLangObject);
                c.checkLocalClass(toplevelEnv, null,
                                  new Vset(), sup, argsX, argTypesX);
            } catch (ClassNotFound ee) {};
        }
        return lookup;
    }
    private static Vector active = new Vector();
    public void compile(OutputStream out)
                throws InterruptedException, IOException {
        Environment env = toplevelEnv;
        synchronized (active) {
            while (active.contains(getName())) {
                active.wait();
            }
            active.addElement(getName());
        }
        try {
            compileClass(env, out);
        } catch (ClassNotFound e) {
            throw new CompilerError(e);
        } finally {
            synchronized (active) {
                active.removeElement(getName());
                active.notifyAll();
            }
        }
    }
    private static void assertModifiers(int mods, int required) {
        if ((mods & required) != required) {
            throw new CompilerError("illegal class modifiers");
        }
    }
    protected void compileClass(Environment env, OutputStream out)
                throws IOException, ClassNotFound {
        Vector variables = new Vector();
        Vector methods = new Vector();
        Vector innerClasses = new Vector();
        CompilerMember init = new CompilerMember(new MemberDefinition(getWhere(), this, M_STATIC, Type.tMethod(Type.tVoid), idClassInit, null, null), new Assembler());
        Context ctx = new Context((Context)null, init.field);
        for (ClassDefinition def = this; def.isInnerClass(); def = def.getOuterClass()) {
            innerClasses.addElement(def);
        }
        int ncsize = innerClasses.size();
        for (int i = ncsize; --i >= 0; )
            innerClasses.addElement(innerClasses.elementAt(i));
        for (int i = ncsize; --i >= 0; )
            innerClasses.removeElementAt(i);
        boolean haveDeprecated = this.isDeprecated();
        boolean haveSynthetic = this.isSynthetic();
        boolean haveConstantValue = false;
        boolean haveExceptions = false;
        for (SourceMember field = (SourceMember)getFirstMember();
             field != null;
             field = (SourceMember)field.getNextMember()) {
            haveDeprecated |= field.isDeprecated();
            haveSynthetic |= field.isSynthetic();
            try {
                if (field.isMethod()) {
                    haveExceptions |=
                        (field.getExceptions(env).length > 0);
                    if (field.isInitializer()) {
                        if (field.isStatic()) {
                            field.code(env, init.asm);
                        }
                    } else {
                        CompilerMember f =
                            new CompilerMember(field, new Assembler());
                        field.code(env, f.asm);
                        methods.addElement(f);
                    }
                } else if (field.isInnerClass()) {
                    innerClasses.addElement(field.getInnerClass());
                } else if (field.isVariable()) {
                    field.inline(env);
                    CompilerMember f = new CompilerMember(field, null);
                    variables.addElement(f);
                    if (field.isStatic()) {
                        field.codeInit(env, ctx, init.asm);
                    }
                    haveConstantValue |=
                        (field.getInitialValue() != null);
                }
            } catch (CompilerError ee) {
                ee.printStackTrace();
                env.error(field, 0, "generic",
                          field.getClassDeclaration() + ":" + field +
                          "@" + ee.toString(), null, null);
            }
        }
        if (!init.asm.empty()) {
           init.asm.add(getWhere(), opc_return, true);
            methods.addElement(init);
        }
        if (getNestError()) {
            return;
        }
        int nClassAttrs = 0;
        if (methods.size() > 0) {
            tab.put("Code");
        }
        if (haveConstantValue) {
            tab.put("ConstantValue");
        }
        String sourceFile = null;
        if (env.debug_source()) {
            sourceFile = ((ClassFile)getSource()).getName();
            tab.put("SourceFile");
            tab.put(sourceFile);
            nClassAttrs += 1;
        }
        if (haveExceptions) {
            tab.put("Exceptions");
        }
        if (env.debug_lines()) {
            tab.put("LineNumberTable");
        }
        if (haveDeprecated) {
            tab.put("Deprecated");
            if (this.isDeprecated()) {
                nClassAttrs += 1;
            }
        }
        if (haveSynthetic) {
            tab.put("Synthetic");
            if (this.isSynthetic()) {
                nClassAttrs += 1;
            }
        }
        if (env.coverage()) {
            nClassAttrs += 2;           
            tab.put("AbsoluteSourcePath");
            tab.put("TimeStamp");
            tab.put("CoverageTable");
        }
        if (env.debug_vars()) {
            tab.put("LocalVariableTable");
        }
        if (innerClasses.size() > 0) {
            tab.put("InnerClasses");
            nClassAttrs += 1;           
        }
        String absoluteSourcePath = "";
        long timeStamp = 0;
        if (env.coverage()) {
                absoluteSourcePath = getAbsoluteName();
                timeStamp = System.currentTimeMillis();
                tab.put(absoluteSourcePath);
        }
        tab.put(getClassDeclaration());
        if (getSuperClass() != null) {
            tab.put(getSuperClass());
        }
        for (int i = 0 ; i < interfaces.length ; i++) {
            tab.put(interfaces[i]);
        }
        CompilerMember[] ordered_methods =
            new CompilerMember[methods.size()];
        methods.copyInto(ordered_methods);
        java.util.Arrays.sort(ordered_methods);
        for (int i=0; i<methods.size(); i++)
            methods.setElementAt(ordered_methods[i], i);
        for (Enumeration e = methods.elements() ; e.hasMoreElements() ; ) {
            CompilerMember f = (CompilerMember)e.nextElement();
            try {
                f.asm.optimize(env);
                f.asm.collect(env, f.field, tab);
                tab.put(f.name);
                tab.put(f.sig);
                ClassDeclaration exp[] = f.field.getExceptions(env);
                for (int i = 0 ; i < exp.length ; i++) {
                    tab.put(exp[i]);
                }
            } catch (Exception ee) {
                ee.printStackTrace();
                env.error(f.field, -1, "generic", f.field.getName() + "@" + ee.toString(), null, null);
                f.asm.listing(System.out);
            }
        }
        for (Enumeration e = variables.elements() ; e.hasMoreElements() ; ) {
            CompilerMember f = (CompilerMember)e.nextElement();
            tab.put(f.name);
            tab.put(f.sig);
            Object val = f.field.getInitialValue();
            if (val != null) {
                tab.put((val instanceof String) ? new StringExpression(f.field.getWhere(), (String)val) : val);
            }
        }
        for (Enumeration e = innerClasses.elements();
             e.hasMoreElements() ; ) {
            ClassDefinition inner = (ClassDefinition)e.nextElement();
            tab.put(inner.getClassDeclaration());
            if (!inner.isLocal()) {
                ClassDefinition outer = inner.getOuterClass();
                tab.put(outer.getClassDeclaration());
            }
            Identifier inner_local_name = inner.getLocalName();
            if (inner_local_name != idNull) {
                tab.put(inner_local_name.toString());
            }
        }
        DataOutputStream data = new DataOutputStream(out);
        data.writeInt(JAVA_MAGIC);
        data.writeShort(toplevelEnv.getMinorVersion());
        data.writeShort(toplevelEnv.getMajorVersion());
        tab.write(env, data);
        int cmods = getModifiers() & MM_CLASS;
        if (isInterface()) {
            assertModifiers(cmods, ACC_ABSTRACT);
        } else {
            cmods |= ACC_SUPER;
        }
        if (outerClass != null) {
            if (isProtected()) cmods |= M_PUBLIC;
            if (outerClass.isInterface()) {
                assertModifiers(cmods, M_PUBLIC);
            }
        }
        data.writeShort(cmods);
        if (env.dumpModifiers()) {
            Identifier cn = getName();
            Identifier nm =
                Identifier.lookup(cn.getQualifier(), cn.getFlatName());
            System.out.println();
            System.out.println("CLASSFILE  " + nm);
            System.out.println("---" + classModifierString(cmods));
        }
        data.writeShort(tab.index(getClassDeclaration()));
        data.writeShort((getSuperClass() != null) ? tab.index(getSuperClass()) : 0);
        data.writeShort(interfaces.length);
        for (int i = 0 ; i < interfaces.length ; i++) {
            data.writeShort(tab.index(interfaces[i]));
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream(256);
        ByteArrayOutputStream attbuf = new ByteArrayOutputStream(256);
        DataOutputStream databuf = new DataOutputStream(buf);
        data.writeShort(variables.size());
        for (Enumeration e = variables.elements() ; e.hasMoreElements() ; ) {
            CompilerMember f = (CompilerMember)e.nextElement();
            Object val = f.field.getInitialValue();
            data.writeShort(f.field.getModifiers() & MM_FIELD);
            data.writeShort(tab.index(f.name));
            data.writeShort(tab.index(f.sig));
            int fieldAtts = (val != null ? 1 : 0);
            boolean dep = f.field.isDeprecated();
            boolean syn = f.field.isSynthetic();
            fieldAtts += (dep ? 1 : 0) + (syn ? 1 : 0);
            data.writeShort(fieldAtts);
            if (val != null) {
                data.writeShort(tab.index("ConstantValue"));
                data.writeInt(2);
                data.writeShort(tab.index((val instanceof String) ? new StringExpression(f.field.getWhere(), (String)val) : val));
            }
            if (dep) {
                data.writeShort(tab.index("Deprecated"));
                data.writeInt(0);
            }
            if (syn) {
                data.writeShort(tab.index("Synthetic"));
                data.writeInt(0);
            }
        }
        data.writeShort(methods.size());
        for (Enumeration e = methods.elements() ; e.hasMoreElements() ; ) {
            CompilerMember f = (CompilerMember)e.nextElement();
            int xmods = f.field.getModifiers() & MM_METHOD;
            if (((xmods & M_STRICTFP)!=0) || ((cmods & M_STRICTFP)!=0)) {
                xmods |= ACC_STRICT;
            } else {
                if (env.strictdefault()) {
                    xmods |= ACC_STRICT;
                }
            }
            data.writeShort(xmods);
            data.writeShort(tab.index(f.name));
            data.writeShort(tab.index(f.sig));
            ClassDeclaration exp[] = f.field.getExceptions(env);
            int methodAtts = ((exp.length > 0) ? 1 : 0);
            boolean dep = f.field.isDeprecated();
            boolean syn = f.field.isSynthetic();
            methodAtts += (dep ? 1 : 0) + (syn ? 1 : 0);
            if (!f.asm.empty()) {
                data.writeShort(methodAtts+1);
                f.asm.write(env, databuf, f.field, tab);
                int natts = 0;
                if (env.debug_lines()) {
                    natts++;
                }
                if (env.coverage()) {
                    natts++;
                }
                if (env.debug_vars()) {
                    natts++;
                }
                databuf.writeShort(natts);
                if (env.debug_lines()) {
                    f.asm.writeLineNumberTable(env, new DataOutputStream(attbuf), tab);
                    databuf.writeShort(tab.index("LineNumberTable"));
                    databuf.writeInt(attbuf.size());
                    attbuf.writeTo(buf);
                    attbuf.reset();
                }
                if (env.coverage()) {
                    f.asm.writeCoverageTable(env, (ClassDefinition)this, new DataOutputStream(attbuf), tab, f.field.getWhere());
                    databuf.writeShort(tab.index("CoverageTable"));
                    databuf.writeInt(attbuf.size());
                    attbuf.writeTo(buf);
                    attbuf.reset();
                }
                if (env.debug_vars()) {
                    f.asm.writeLocalVariableTable(env, f.field, new DataOutputStream(attbuf), tab);
                    databuf.writeShort(tab.index("LocalVariableTable"));
                    databuf.writeInt(attbuf.size());
                    attbuf.writeTo(buf);
                    attbuf.reset();
                }
                data.writeShort(tab.index("Code"));
                data.writeInt(buf.size());
                buf.writeTo(data);
                buf.reset();
            } else {
                if ((env.coverage()) && ((f.field.getModifiers() & M_NATIVE) > 0))
                    f.asm.addNativeToJcovTab(env, (ClassDefinition)this);
                data.writeShort(methodAtts);
            }
            if (exp.length > 0) {
                data.writeShort(tab.index("Exceptions"));
                data.writeInt(2 + exp.length * 2);
                data.writeShort(exp.length);
                for (int i = 0 ; i < exp.length ; i++) {
                    data.writeShort(tab.index(exp[i]));
                }
            }
            if (dep) {
                data.writeShort(tab.index("Deprecated"));
                data.writeInt(0);
            }
            if (syn) {
                data.writeShort(tab.index("Synthetic"));
                data.writeInt(0);
            }
        }
        data.writeShort(nClassAttrs);
        if (env.debug_source()) {
            data.writeShort(tab.index("SourceFile"));
            data.writeInt(2);
            data.writeShort(tab.index(sourceFile));
        }
        if (this.isDeprecated()) {
            data.writeShort(tab.index("Deprecated"));
            data.writeInt(0);
        }
        if (this.isSynthetic()) {
            data.writeShort(tab.index("Synthetic"));
            data.writeInt(0);
        }
        if (env.coverage()) {
            data.writeShort(tab.index("AbsoluteSourcePath"));
            data.writeInt(2);
            data.writeShort(tab.index(absoluteSourcePath));
            data.writeShort(tab.index("TimeStamp"));
            data.writeInt(8);
            data.writeLong(timeStamp);
        }
        if (innerClasses.size() > 0) {
            data.writeShort(tab.index("InnerClasses"));
            data.writeInt(2 + 2*4*innerClasses.size());
            data.writeShort(innerClasses.size());
            for (Enumeration e = innerClasses.elements() ;
                 e.hasMoreElements() ; ) {
                ClassDefinition inner = (ClassDefinition)e.nextElement();
                data.writeShort(tab.index(inner.getClassDeclaration()));
                if (inner.isLocal() || inner.isAnonymous()) {
                    data.writeShort(0);
                } else {
                    ClassDefinition outer = inner.getOuterClass();
                    data.writeShort(tab.index(outer.getClassDeclaration()));
                }
                Identifier inner_name = inner.getLocalName();
                if (inner_name == idNull) {
                    if (!inner.isAnonymous()) {
                        throw new CompilerError("compileClass(), anonymous");
                    }
                    data.writeShort(0);
                } else {
                    data.writeShort(tab.index(inner_name.toString()));
                }
                int imods = inner.getInnerClassMember().getModifiers()
                            & ACCM_INNERCLASS;
                if (inner.isInterface()) {
                    assertModifiers(imods, M_ABSTRACT | M_STATIC);
                }
                if (inner.getOuterClass().isInterface()) {
                    imods &= ~(M_PRIVATE | M_PROTECTED); 
                    assertModifiers(imods, M_PUBLIC | M_STATIC);
                }
                data.writeShort(imods);
                if (env.dumpModifiers()) {
                    Identifier fn = inner.getInnerClassMember().getName();
                    Identifier nm =
                        Identifier.lookup(fn.getQualifier(), fn.getFlatName());
                    System.out.println("INNERCLASS " + nm);
                    System.out.println("---" + classModifierString(imods));
                }
            }
        }
        data.flush();
        tab = null;
        if (env.covdata()) {
            Assembler CovAsm = new Assembler();
            CovAsm.GenVecJCov(env, (ClassDefinition)this, timeStamp);
        }
    }
    public void printClassDependencies(Environment env) {
        if ( toplevelEnv.print_dependencies() ) {
            String src = ((ClassFile)getSource()).getAbsoluteName();
            String className = Type.mangleInnerType(getName()).toString();
            long startLine = getWhere() >> WHEREOFFSETBITS;
            long endLine = getEndPosition() >> WHEREOFFSETBITS;
            System.out.println( "CLASS:"
                    + src               + ","
                    + startLine         + ","
                    + endLine   + ","
                    + className);
            for(Enumeration e = deps.elements();  e.hasMoreElements(); ) {
                ClassDeclaration data = (ClassDeclaration) e.nextElement();
                String depName =
                    Type.mangleInnerType(data.getName()).toString();
                env.output("CLDEP:" + className + "," + depName);
            }
        }
    }
}
