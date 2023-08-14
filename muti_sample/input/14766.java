public class Resolve {
    protected static final Context.Key<Resolve> resolveKey =
        new Context.Key<Resolve>();
    Names names;
    Log log;
    Symtab syms;
    Check chk;
    Infer infer;
    ClassReader reader;
    TreeInfo treeinfo;
    Types types;
    JCDiagnostic.Factory diags;
    public final boolean boxingEnabled; 
    public final boolean varargsEnabled; 
    public final boolean allowMethodHandles;
    private final boolean debugResolve;
    Scope polymorphicSignatureScope;
    public static Resolve instance(Context context) {
        Resolve instance = context.get(resolveKey);
        if (instance == null)
            instance = new Resolve(context);
        return instance;
    }
    protected Resolve(Context context) {
        context.put(resolveKey, this);
        syms = Symtab.instance(context);
        varNotFound = new
            SymbolNotFoundError(ABSENT_VAR);
        wrongMethod = new
            InapplicableSymbolError(syms.errSymbol);
        wrongMethods = new
            InapplicableSymbolsError(syms.errSymbol);
        methodNotFound = new
            SymbolNotFoundError(ABSENT_MTH);
        typeNotFound = new
            SymbolNotFoundError(ABSENT_TYP);
        names = Names.instance(context);
        log = Log.instance(context);
        chk = Check.instance(context);
        infer = Infer.instance(context);
        reader = ClassReader.instance(context);
        treeinfo = TreeInfo.instance(context);
        types = Types.instance(context);
        diags = JCDiagnostic.Factory.instance(context);
        Source source = Source.instance(context);
        boxingEnabled = source.allowBoxing();
        varargsEnabled = source.allowVarargs();
        Options options = Options.instance(context);
        debugResolve = options.isSet("debugresolve");
        Target target = Target.instance(context);
        allowMethodHandles = target.hasMethodHandles();
        polymorphicSignatureScope = new Scope(syms.noSymbol);
        inapplicableMethodException = new InapplicableMethodException(diags);
    }
    final SymbolNotFoundError varNotFound;
    final InapplicableSymbolError wrongMethod;
    final InapplicableSymbolsError wrongMethods;
    final SymbolNotFoundError methodNotFound;
    final SymbolNotFoundError typeNotFound;
    static boolean isStatic(Env<AttrContext> env) {
        return env.info.staticLevel > env.outer.info.staticLevel;
    }
    static boolean isInitializer(Env<AttrContext> env) {
        Symbol owner = env.info.scope.owner;
        return owner.isConstructor() ||
            owner.owner.kind == TYP &&
            (owner.kind == VAR ||
             owner.kind == MTH && (owner.flags() & BLOCK) != 0) &&
            (owner.flags() & STATIC) == 0;
    }
    public boolean isAccessible(Env<AttrContext> env, TypeSymbol c) {
        return isAccessible(env, c, false);
    }
    public boolean isAccessible(Env<AttrContext> env, TypeSymbol c, boolean checkInner) {
        boolean isAccessible = false;
        switch ((short)(c.flags() & AccessFlags)) {
            case PRIVATE:
                isAccessible =
                    env.enclClass.sym.outermostClass() ==
                    c.owner.outermostClass();
                break;
            case 0:
                isAccessible =
                    env.toplevel.packge == c.owner 
                    ||
                    env.toplevel.packge == c.packge()
                    ||
                    env.enclMethod != null &&
                    (env.enclMethod.mods.flags & ANONCONSTR) != 0;
                break;
            default: 
            case PUBLIC:
                isAccessible = true;
                break;
            case PROTECTED:
                isAccessible =
                    env.toplevel.packge == c.owner 
                    ||
                    env.toplevel.packge == c.packge()
                    ||
                    isInnerSubClass(env.enclClass.sym, c.owner);
                break;
        }
        return (checkInner == false || c.type.getEnclosingType() == Type.noType) ?
            isAccessible :
            isAccessible && isAccessible(env, c.type.getEnclosingType(), checkInner);
    }
        private boolean isInnerSubClass(ClassSymbol c, Symbol base) {
            while (c != null && !c.isSubClass(base, types)) {
                c = c.owner.enclClass();
            }
            return c != null;
        }
    boolean isAccessible(Env<AttrContext> env, Type t) {
        return isAccessible(env, t, false);
    }
    boolean isAccessible(Env<AttrContext> env, Type t, boolean checkInner) {
        return (t.tag == ARRAY)
            ? isAccessible(env, types.elemtype(t))
            : isAccessible(env, t.tsym, checkInner);
    }
    public boolean isAccessible(Env<AttrContext> env, Type site, Symbol sym) {
        return isAccessible(env, site, sym, false);
    }
    public boolean isAccessible(Env<AttrContext> env, Type site, Symbol sym, boolean checkInner) {
        if (sym.name == names.init && sym.owner != site.tsym) return false;
        switch ((short)(sym.flags() & AccessFlags)) {
        case PRIVATE:
            return
                (env.enclClass.sym == sym.owner 
                 ||
                 env.enclClass.sym.outermostClass() ==
                 sym.owner.outermostClass())
                &&
                sym.isInheritedIn(site.tsym, types);
        case 0:
            return
                (env.toplevel.packge == sym.owner.owner 
                 ||
                 env.toplevel.packge == sym.packge())
                &&
                isAccessible(env, site, checkInner)
                &&
                sym.isInheritedIn(site.tsym, types)
                &&
                notOverriddenIn(site, sym);
        case PROTECTED:
            return
                (env.toplevel.packge == sym.owner.owner 
                 ||
                 env.toplevel.packge == sym.packge()
                 ||
                 isProtectedAccessible(sym, env.enclClass.sym, site)
                 ||
                 env.info.selectSuper && (sym.flags() & STATIC) == 0 && sym.kind != TYP)
                &&
                isAccessible(env, site, checkInner)
                &&
                notOverriddenIn(site, sym);
        default: 
            return isAccessible(env, site, checkInner) && notOverriddenIn(site, sym);
        }
    }
    private boolean notOverriddenIn(Type site, Symbol sym) {
        if (sym.kind != MTH || sym.isConstructor() || sym.isStatic())
            return true;
        else {
            Symbol s2 = ((MethodSymbol)sym).implementation(site.tsym, types, true);
            return (s2 == null || s2 == sym || sym.owner == s2.owner ||
                    s2.isPolymorphicSignatureGeneric() ||
                    !types.isSubSignature(types.memberType(site, s2), types.memberType(site, sym)));
        }
    }
        private
        boolean isProtectedAccessible(Symbol sym, ClassSymbol c, Type site) {
            while (c != null &&
                   !(c.isSubClass(sym.owner, types) &&
                     (c.flags() & INTERFACE) == 0 &&
                     ((sym.flags() & STATIC) != 0 || sym.kind == TYP || site.tsym.isSubClass(c, types))))
                c = c.owner.enclClass();
            return c != null;
        }
    Type rawInstantiate(Env<AttrContext> env,
                        Type site,
                        Symbol m,
                        List<Type> argtypes,
                        List<Type> typeargtypes,
                        boolean allowBoxing,
                        boolean useVarargs,
                        Warner warn)
        throws Infer.InferenceException {
        boolean polymorphicSignature = m.isPolymorphicSignatureGeneric() && allowMethodHandles;
        if (useVarargs && (m.flags() & VARARGS) == 0)
            throw inapplicableMethodException.setMessage();
        Type mt = types.memberType(site, m);
        List<Type> tvars = null;
        if (env.info.tvars != null) {
            tvars = types.newInstances(env.info.tvars);
            mt = types.subst(mt, env.info.tvars, tvars);
        }
        if (typeargtypes == null) typeargtypes = List.nil();
        if (mt.tag != FORALL && typeargtypes.nonEmpty()) {
        } else if (mt.tag == FORALL && typeargtypes.nonEmpty()) {
            ForAll pmt = (ForAll) mt;
            if (typeargtypes.length() != pmt.tvars.length())
                throw inapplicableMethodException.setMessage("arg.length.mismatch"); 
            List<Type> formals = pmt.tvars;
            List<Type> actuals = typeargtypes;
            while (formals.nonEmpty() && actuals.nonEmpty()) {
                List<Type> bounds = types.subst(types.getBounds((TypeVar)formals.head),
                                                pmt.tvars, typeargtypes);
                for (; bounds.nonEmpty(); bounds = bounds.tail)
                    if (!types.isSubtypeUnchecked(actuals.head, bounds.head, warn))
                        throw inapplicableMethodException.setMessage("explicit.param.do.not.conform.to.bounds",actuals.head, bounds);
                formals = formals.tail;
                actuals = actuals.tail;
            }
            mt = types.subst(pmt.qtype, pmt.tvars, typeargtypes);
        } else if (mt.tag == FORALL) {
            ForAll pmt = (ForAll) mt;
            List<Type> tvars1 = types.newInstances(pmt.tvars);
            tvars = tvars.appendList(tvars1);
            mt = types.subst(pmt.qtype, pmt.tvars, tvars1);
        }
        boolean instNeeded = tvars.tail != null || 
                polymorphicSignature;
        for (List<Type> l = argtypes;
             l.tail != null && !instNeeded;
             l = l.tail) {
            if (l.head.tag == FORALL) instNeeded = true;
        }
        if (instNeeded)
            return polymorphicSignature ?
                infer.instantiatePolymorphicSignatureInstance(env, site, m.name, (MethodSymbol)m, argtypes) :
                infer.instantiateMethod(env,
                                    tvars,
                                    (MethodType)mt,
                                    m,
                                    argtypes,
                                    allowBoxing,
                                    useVarargs,
                                    warn);
        checkRawArgumentsAcceptable(env, argtypes, mt.getParameterTypes(),
                                allowBoxing, useVarargs, warn);
        return mt;
    }
    Type instantiate(Env<AttrContext> env,
                     Type site,
                     Symbol m,
                     List<Type> argtypes,
                     List<Type> typeargtypes,
                     boolean allowBoxing,
                     boolean useVarargs,
                     Warner warn) {
        try {
            return rawInstantiate(env, site, m, argtypes, typeargtypes,
                                  allowBoxing, useVarargs, warn);
        } catch (InapplicableMethodException ex) {
            return null;
        }
    }
    boolean argumentsAcceptable(Env<AttrContext> env,
                                List<Type> argtypes,
                                List<Type> formals,
                                boolean allowBoxing,
                                boolean useVarargs,
                                Warner warn) {
        try {
            checkRawArgumentsAcceptable(env, argtypes, formals, allowBoxing, useVarargs, warn);
            return true;
        } catch (InapplicableMethodException ex) {
            return false;
        }
    }
    void checkRawArgumentsAcceptable(Env<AttrContext> env,
                                List<Type> argtypes,
                                List<Type> formals,
                                boolean allowBoxing,
                                boolean useVarargs,
                                Warner warn) {
        Type varargsFormal = useVarargs ? formals.last() : null;
        if (varargsFormal == null &&
                argtypes.size() != formals.size()) {
            throw inapplicableMethodException.setMessage("arg.length.mismatch"); 
        }
        while (argtypes.nonEmpty() && formals.head != varargsFormal) {
            boolean works = allowBoxing
                ? types.isConvertible(argtypes.head, formals.head, warn)
                : types.isSubtypeUnchecked(argtypes.head, formals.head, warn);
            if (!works)
                throw inapplicableMethodException.setMessage("no.conforming.assignment.exists",
                        argtypes.head,
                        formals.head);
            argtypes = argtypes.tail;
            formals = formals.tail;
        }
        if (formals.head != varargsFormal)
            throw inapplicableMethodException.setMessage("arg.length.mismatch"); 
        if (useVarargs) {
            Type elt = types.elemtype(varargsFormal);
            while (argtypes.nonEmpty()) {
                if (!types.isConvertible(argtypes.head, elt, warn))
                    throw inapplicableMethodException.setMessage("varargs.argument.mismatch",
                            argtypes.head,
                            elt);
                argtypes = argtypes.tail;
            }
            if (!isAccessible(env, elt)) {
                Symbol location = env.enclClass.sym;
                throw inapplicableMethodException.setMessage("inaccessible.varargs.type",
                            elt,
                            Kinds.kindName(location),
                            location);
            }
        }
        return;
    }
        public static class InapplicableMethodException extends RuntimeException {
            private static final long serialVersionUID = 0;
            JCDiagnostic diagnostic;
            JCDiagnostic.Factory diags;
            InapplicableMethodException(JCDiagnostic.Factory diags) {
                this.diagnostic = null;
                this.diags = diags;
            }
            InapplicableMethodException setMessage() {
                this.diagnostic = null;
                return this;
            }
            InapplicableMethodException setMessage(String key) {
                this.diagnostic = key != null ? diags.fragment(key) : null;
                return this;
            }
            InapplicableMethodException setMessage(String key, Object... args) {
                this.diagnostic = key != null ? diags.fragment(key, args) : null;
                return this;
            }
            InapplicableMethodException setMessage(JCDiagnostic diag) {
                this.diagnostic = diag;
                return this;
            }
            public JCDiagnostic getDiagnostic() {
                return diagnostic;
            }
        }
        private final InapplicableMethodException inapplicableMethodException;
    Symbol findField(Env<AttrContext> env,
                     Type site,
                     Name name,
                     TypeSymbol c) {
        while (c.type.tag == TYPEVAR)
            c = c.type.getUpperBound().tsym;
        Symbol bestSoFar = varNotFound;
        Symbol sym;
        Scope.Entry e = c.members().lookup(name);
        while (e.scope != null) {
            if (e.sym.kind == VAR && (e.sym.flags_field & SYNTHETIC) == 0) {
                return isAccessible(env, site, e.sym)
                    ? e.sym : new AccessError(env, site, e.sym);
            }
            e = e.next();
        }
        Type st = types.supertype(c.type);
        if (st != null && (st.tag == CLASS || st.tag == TYPEVAR)) {
            sym = findField(env, site, name, st.tsym);
            if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        for (List<Type> l = types.interfaces(c.type);
             bestSoFar.kind != AMBIGUOUS && l.nonEmpty();
             l = l.tail) {
            sym = findField(env, site, name, l.head.tsym);
            if (bestSoFar.kind < AMBIGUOUS && sym.kind < AMBIGUOUS &&
                sym.owner != bestSoFar.owner)
                bestSoFar = new AmbiguityError(bestSoFar, sym);
            else if (sym.kind < bestSoFar.kind)
                bestSoFar = sym;
        }
        return bestSoFar;
    }
    public VarSymbol resolveInternalField(DiagnosticPosition pos, Env<AttrContext> env,
                                          Type site, Name name) {
        Symbol sym = findField(env, site, name, site.tsym);
        if (sym.kind == VAR) return (VarSymbol)sym;
        else throw new FatalError(
                 diags.fragment("fatal.err.cant.locate.field",
                                name));
    }
    Symbol findVar(Env<AttrContext> env, Name name) {
        Symbol bestSoFar = varNotFound;
        Symbol sym;
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            Scope.Entry e = env1.info.scope.lookup(name);
            while (e.scope != null &&
                   (e.sym.kind != VAR ||
                    (e.sym.flags_field & SYNTHETIC) != 0))
                e = e.next();
            sym = (e.scope != null)
                ? e.sym
                : findField(
                    env1, env1.enclClass.sym.type, name, env1.enclClass.sym);
            if (sym.exists()) {
                if (staticOnly &&
                    sym.kind == VAR &&
                    sym.owner.kind == TYP &&
                    (sym.flags() & STATIC) == 0)
                    return new StaticError(sym);
                else
                    return sym;
            } else if (sym.kind < bestSoFar.kind) {
                bestSoFar = sym;
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
        }
        sym = findField(env, syms.predefClass.type, name, syms.predefClass);
        if (sym.exists())
            return sym;
        if (bestSoFar.exists())
            return bestSoFar;
        Scope.Entry e = env.toplevel.namedImportScope.lookup(name);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            Type origin = e.getOrigin().owner.type;
            if (sym.kind == VAR) {
                if (e.sym.owner.type != origin)
                    sym = sym.clone(e.getOrigin().owner);
                return isAccessible(env, origin, sym)
                    ? sym : new AccessError(env, origin, sym);
            }
        }
        Symbol origin = null;
        e = env.toplevel.starImportScope.lookup(name);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            if (sym.kind != VAR)
                continue;
            if (bestSoFar.kind < AMBIGUOUS && sym.owner != bestSoFar.owner)
                return new AmbiguityError(bestSoFar, sym);
            else if (bestSoFar.kind >= VAR) {
                origin = e.getOrigin().owner;
                bestSoFar = isAccessible(env, origin.type, sym)
                    ? sym : new AccessError(env, origin.type, sym);
            }
        }
        if (bestSoFar.kind == VAR && bestSoFar.owner.type != origin.type)
            return bestSoFar.clone(origin);
        else
            return bestSoFar;
    }
    Warner noteWarner = new Warner();
    @SuppressWarnings("fallthrough")
    Symbol selectBest(Env<AttrContext> env,
                      Type site,
                      List<Type> argtypes,
                      List<Type> typeargtypes,
                      Symbol sym,
                      Symbol bestSoFar,
                      boolean allowBoxing,
                      boolean useVarargs,
                      boolean operator) {
        if (sym.kind == ERR) return bestSoFar;
        if (!sym.isInheritedIn(site.tsym, types)) return bestSoFar;
        Assert.check(sym.kind < AMBIGUOUS);
        try {
            rawInstantiate(env, site, sym, argtypes, typeargtypes,
                               allowBoxing, useVarargs, Warner.noWarnings);
        } catch (InapplicableMethodException ex) {
            switch (bestSoFar.kind) {
            case ABSENT_MTH:
                return wrongMethod.setWrongSym(sym, ex.getDiagnostic());
            case WRONG_MTH:
                wrongMethods.addCandidate(currentStep, wrongMethod.sym, wrongMethod.explanation);
            case WRONG_MTHS:
                return wrongMethods.addCandidate(currentStep, sym, ex.getDiagnostic());
            default:
                return bestSoFar;
            }
        }
        if (!isAccessible(env, site, sym)) {
            return (bestSoFar.kind == ABSENT_MTH)
                ? new AccessError(env, site, sym)
                : bestSoFar;
            }
        return (bestSoFar.kind > AMBIGUOUS)
            ? sym
            : mostSpecific(sym, bestSoFar, env, site,
                           allowBoxing && operator, useVarargs);
    }
    Symbol mostSpecific(Symbol m1,
                        Symbol m2,
                        Env<AttrContext> env,
                        final Type site,
                        boolean allowBoxing,
                        boolean useVarargs) {
        switch (m2.kind) {
        case MTH:
            if (m1 == m2) return m1;
            boolean m1SignatureMoreSpecific = signatureMoreSpecific(env, site, m1, m2, allowBoxing, useVarargs);
            boolean m2SignatureMoreSpecific = signatureMoreSpecific(env, site, m2, m1, allowBoxing, useVarargs);
            if (m1SignatureMoreSpecific && m2SignatureMoreSpecific) {
                Type mt1 = types.memberType(site, m1);
                Type mt2 = types.memberType(site, m2);
                if (!types.overrideEquivalent(mt1, mt2))
                    return ambiguityError(m1, m2);
                if ((m1.flags() & BRIDGE) != (m2.flags() & BRIDGE))
                    return ((m1.flags() & BRIDGE) != 0) ? m2 : m1;
                TypeSymbol m1Owner = (TypeSymbol)m1.owner;
                TypeSymbol m2Owner = (TypeSymbol)m2.owner;
                if (types.asSuper(m1Owner.type, m2Owner) != null &&
                    ((m1.owner.flags_field & INTERFACE) == 0 ||
                     (m2.owner.flags_field & INTERFACE) != 0) &&
                    m1.overrides(m2, m1Owner, types, false))
                    return m1;
                if (types.asSuper(m2Owner.type, m1Owner) != null &&
                    ((m2.owner.flags_field & INTERFACE) == 0 ||
                     (m1.owner.flags_field & INTERFACE) != 0) &&
                    m2.overrides(m1, m2Owner, types, false))
                    return m2;
                boolean m1Abstract = (m1.flags() & ABSTRACT) != 0;
                boolean m2Abstract = (m2.flags() & ABSTRACT) != 0;
                if (m1Abstract && !m2Abstract) return m2;
                if (m2Abstract && !m1Abstract) return m1;
                if (!m1Abstract && !m2Abstract)
                    return ambiguityError(m1, m2);
                if (!types.isSameTypes(m1.erasure(types).getParameterTypes(),
                                       m2.erasure(types).getParameterTypes()))
                    return ambiguityError(m1, m2);
                Symbol mostSpecific;
                if (types.returnTypeSubstitutable(mt1, mt2))
                    mostSpecific = m1;
                else if (types.returnTypeSubstitutable(mt2, mt1))
                    mostSpecific = m2;
                else {
                    return ambiguityError(m1, m2);
                }
                List<Type> allThrown = chk.intersect(mt1.getThrownTypes(), mt2.getThrownTypes());
                Type newSig = types.createMethodTypeWithThrown(mostSpecific.type, allThrown);
                MethodSymbol result = new MethodSymbol(
                        mostSpecific.flags(),
                        mostSpecific.name,
                        newSig,
                        mostSpecific.owner) {
                    @Override
                    public MethodSymbol implementation(TypeSymbol origin, Types types, boolean checkResult) {
                        if (origin == site.tsym)
                            return this;
                        else
                            return super.implementation(origin, types, checkResult);
                    }
                };
                return result;
            }
            if (m1SignatureMoreSpecific) return m1;
            if (m2SignatureMoreSpecific) return m2;
            return ambiguityError(m1, m2);
        case AMBIGUOUS:
            AmbiguityError e = (AmbiguityError)m2;
            Symbol err1 = mostSpecific(m1, e.sym, env, site, allowBoxing, useVarargs);
            Symbol err2 = mostSpecific(m1, e.sym2, env, site, allowBoxing, useVarargs);
            if (err1 == err2) return err1;
            if (err1 == e.sym && err2 == e.sym2) return m2;
            if (err1 instanceof AmbiguityError &&
                err2 instanceof AmbiguityError &&
                ((AmbiguityError)err1).sym == ((AmbiguityError)err2).sym)
                return ambiguityError(m1, m2);
            else
                return ambiguityError(err1, err2);
        default:
            throw new AssertionError();
        }
    }
    private boolean signatureMoreSpecific(Env<AttrContext> env, Type site, Symbol m1, Symbol m2, boolean allowBoxing, boolean useVarargs) {
        noteWarner.clear();
        Type mtype1 = types.memberType(site, adjustVarargs(m1, m2, useVarargs));
        Type mtype2 = instantiate(env, site, adjustVarargs(m2, m1, useVarargs),
                types.lowerBoundArgtypes(mtype1), null,
                allowBoxing, false, noteWarner);
        return mtype2 != null &&
                !noteWarner.hasLint(Lint.LintCategory.UNCHECKED);
    }
    private Symbol adjustVarargs(Symbol to, Symbol from, boolean useVarargs) {
        List<Type> fromArgs = from.type.getParameterTypes();
        List<Type> toArgs = to.type.getParameterTypes();
        if (useVarargs &&
                (from.flags() & VARARGS) != 0 &&
                (to.flags() & VARARGS) != 0) {
            Type varargsTypeFrom = fromArgs.last();
            Type varargsTypeTo = toArgs.last();
            ListBuffer<Type> args = ListBuffer.lb();
            if (toArgs.length() < fromArgs.length()) {
                while (fromArgs.head != varargsTypeFrom) {
                    args.append(toArgs.head == varargsTypeTo ? types.elemtype(varargsTypeTo) : toArgs.head);
                    fromArgs = fromArgs.tail;
                    toArgs = toArgs.head == varargsTypeTo ?
                        toArgs :
                        toArgs.tail;
                }
            } else {
                args.appendList(toArgs.reverse().tail.reverse());
            }
            args.append(types.elemtype(varargsTypeTo));
            Type mtype = types.createMethodTypeWithParameters(to.type, args.toList());
            return new MethodSymbol(to.flags_field & ~VARARGS, to.name, mtype, to.owner);
        } else {
            return to;
        }
    }
    Symbol ambiguityError(Symbol m1, Symbol m2) {
        if (((m1.flags() | m2.flags()) & CLASH) != 0) {
            return (m1.flags() & CLASH) == 0 ? m1 : m2;
        } else {
            return new AmbiguityError(m1, m2);
        }
    }
    Symbol findMethod(Env<AttrContext> env,
                      Type site,
                      Name name,
                      List<Type> argtypes,
                      List<Type> typeargtypes,
                      boolean allowBoxing,
                      boolean useVarargs,
                      boolean operator) {
        Symbol bestSoFar = methodNotFound;
        return findMethod(env,
                          site,
                          name,
                          argtypes,
                          typeargtypes,
                          site.tsym.type,
                          true,
                          bestSoFar,
                          allowBoxing,
                          useVarargs,
                          operator,
                          new HashSet<TypeSymbol>());
    }
    private Symbol findMethod(Env<AttrContext> env,
                              Type site,
                              Name name,
                              List<Type> argtypes,
                              List<Type> typeargtypes,
                              Type intype,
                              boolean abstractok,
                              Symbol bestSoFar,
                              boolean allowBoxing,
                              boolean useVarargs,
                              boolean operator,
                              Set<TypeSymbol> seen) {
        for (Type ct = intype; ct.tag == CLASS || ct.tag == TYPEVAR; ct = types.supertype(ct)) {
            while (ct.tag == TYPEVAR)
                ct = ct.getUpperBound();
            ClassSymbol c = (ClassSymbol)ct.tsym;
            if (!seen.add(c)) return bestSoFar;
            if ((c.flags() & (ABSTRACT | INTERFACE | ENUM)) == 0)
                abstractok = false;
            for (Scope.Entry e = c.members().lookup(name);
                 e.scope != null;
                 e = e.next()) {
                if (e.sym.kind == MTH &&
                    (e.sym.flags_field & SYNTHETIC) == 0) {
                    bestSoFar = selectBest(env, site, argtypes, typeargtypes,
                                           e.sym, bestSoFar,
                                           allowBoxing,
                                           useVarargs,
                                           operator);
                }
            }
            if (name == names.init)
                break;
            if (abstractok) {
                Symbol concrete = methodNotFound;
                if ((bestSoFar.flags() & ABSTRACT) == 0)
                    concrete = bestSoFar;
                for (List<Type> l = types.interfaces(c.type);
                     l.nonEmpty();
                     l = l.tail) {
                    bestSoFar = findMethod(env, site, name, argtypes,
                                           typeargtypes,
                                           l.head, abstractok, bestSoFar,
                                           allowBoxing, useVarargs, operator, seen);
                }
                if (concrete != bestSoFar &&
                    concrete.kind < ERR  && bestSoFar.kind < ERR &&
                    types.isSubSignature(concrete.type, bestSoFar.type))
                    bestSoFar = concrete;
            }
        }
        return bestSoFar;
    }
    Symbol findFun(Env<AttrContext> env, Name name,
                   List<Type> argtypes, List<Type> typeargtypes,
                   boolean allowBoxing, boolean useVarargs) {
        Symbol bestSoFar = methodNotFound;
        Symbol sym;
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            sym = findMethod(
                env1, env1.enclClass.sym.type, name, argtypes, typeargtypes,
                allowBoxing, useVarargs, false);
            if (sym.exists()) {
                if (staticOnly &&
                    sym.kind == MTH &&
                    sym.owner.kind == TYP &&
                    (sym.flags() & STATIC) == 0) return new StaticError(sym);
                else return sym;
            } else if (sym.kind < bestSoFar.kind) {
                bestSoFar = sym;
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
        }
        sym = findMethod(env, syms.predefClass.type, name, argtypes,
                         typeargtypes, allowBoxing, useVarargs, false);
        if (sym.exists())
            return sym;
        Scope.Entry e = env.toplevel.namedImportScope.lookup(name);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            Type origin = e.getOrigin().owner.type;
            if (sym.kind == MTH) {
                if (e.sym.owner.type != origin)
                    sym = sym.clone(e.getOrigin().owner);
                if (!isAccessible(env, origin, sym))
                    sym = new AccessError(env, origin, sym);
                bestSoFar = selectBest(env, origin,
                                       argtypes, typeargtypes,
                                       sym, bestSoFar,
                                       allowBoxing, useVarargs, false);
            }
        }
        if (bestSoFar.exists())
            return bestSoFar;
        e = env.toplevel.starImportScope.lookup(name);
        for (; e.scope != null; e = e.next()) {
            sym = e.sym;
            Type origin = e.getOrigin().owner.type;
            if (sym.kind == MTH) {
                if (e.sym.owner.type != origin)
                    sym = sym.clone(e.getOrigin().owner);
                if (!isAccessible(env, origin, sym))
                    sym = new AccessError(env, origin, sym);
                bestSoFar = selectBest(env, origin,
                                       argtypes, typeargtypes,
                                       sym, bestSoFar,
                                       allowBoxing, useVarargs, false);
            }
        }
        return bestSoFar;
    }
    Symbol loadClass(Env<AttrContext> env, Name name) {
        try {
            ClassSymbol c = reader.loadClass(name);
            return isAccessible(env, c) ? c : new AccessError(c);
        } catch (ClassReader.BadClassFile err) {
            throw err;
        } catch (CompletionFailure ex) {
            return typeNotFound;
        }
    }
    Symbol findMemberType(Env<AttrContext> env,
                          Type site,
                          Name name,
                          TypeSymbol c) {
        Symbol bestSoFar = typeNotFound;
        Symbol sym;
        Scope.Entry e = c.members().lookup(name);
        while (e.scope != null) {
            if (e.sym.kind == TYP) {
                return isAccessible(env, site, e.sym)
                    ? e.sym
                    : new AccessError(env, site, e.sym);
            }
            e = e.next();
        }
        Type st = types.supertype(c.type);
        if (st != null && st.tag == CLASS) {
            sym = findMemberType(env, site, name, st.tsym);
            if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        for (List<Type> l = types.interfaces(c.type);
             bestSoFar.kind != AMBIGUOUS && l.nonEmpty();
             l = l.tail) {
            sym = findMemberType(env, site, name, l.head.tsym);
            if (bestSoFar.kind < AMBIGUOUS && sym.kind < AMBIGUOUS &&
                sym.owner != bestSoFar.owner)
                bestSoFar = new AmbiguityError(bestSoFar, sym);
            else if (sym.kind < bestSoFar.kind)
                bestSoFar = sym;
        }
        return bestSoFar;
    }
    Symbol findGlobalType(Env<AttrContext> env, Scope scope, Name name) {
        Symbol bestSoFar = typeNotFound;
        for (Scope.Entry e = scope.lookup(name); e.scope != null; e = e.next()) {
            Symbol sym = loadClass(env, e.sym.flatName());
            if (bestSoFar.kind == TYP && sym.kind == TYP &&
                bestSoFar != sym)
                return new AmbiguityError(bestSoFar, sym);
            else if (sym.kind < bestSoFar.kind)
                bestSoFar = sym;
        }
        return bestSoFar;
    }
    Symbol findType(Env<AttrContext> env, Name name) {
        Symbol bestSoFar = typeNotFound;
        Symbol sym;
        boolean staticOnly = false;
        for (Env<AttrContext> env1 = env; env1.outer != null; env1 = env1.outer) {
            if (isStatic(env1)) staticOnly = true;
            for (Scope.Entry e = env1.info.scope.lookup(name);
                 e.scope != null;
                 e = e.next()) {
                if (e.sym.kind == TYP) {
                    if (staticOnly &&
                        e.sym.type.tag == TYPEVAR &&
                        e.sym.owner.kind == TYP) return new StaticError(e.sym);
                    return e.sym;
                }
            }
            sym = findMemberType(env1, env1.enclClass.sym.type, name,
                                 env1.enclClass.sym);
            if (staticOnly && sym.kind == TYP &&
                sym.type.tag == CLASS &&
                sym.type.getEnclosingType().tag == CLASS &&
                env1.enclClass.sym.type.isParameterized() &&
                sym.type.getEnclosingType().isParameterized())
                return new StaticError(sym);
            else if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
            JCClassDecl encl = env1.baseClause ? (JCClassDecl)env1.tree : env1.enclClass;
            if ((encl.sym.flags() & STATIC) != 0)
                staticOnly = true;
        }
        if (env.tree.getTag() != JCTree.IMPORT) {
            sym = findGlobalType(env, env.toplevel.namedImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
            sym = findGlobalType(env, env.toplevel.packge.members(), name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
            sym = findGlobalType(env, env.toplevel.starImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        return bestSoFar;
    }
    Symbol findIdent(Env<AttrContext> env, Name name, int kind) {
        Symbol bestSoFar = typeNotFound;
        Symbol sym;
        if ((kind & VAR) != 0) {
            sym = findVar(env, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        if ((kind & TYP) != 0) {
            sym = findType(env, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        if ((kind & PCK) != 0) return reader.enterPackage(name);
        else return bestSoFar;
    }
    Symbol findIdentInPackage(Env<AttrContext> env, TypeSymbol pck,
                              Name name, int kind) {
        Name fullname = TypeSymbol.formFullName(name, pck);
        Symbol bestSoFar = typeNotFound;
        PackageSymbol pack = null;
        if ((kind & PCK) != 0) {
            pack = reader.enterPackage(fullname);
            if (pack.exists()) return pack;
        }
        if ((kind & TYP) != 0) {
            Symbol sym = loadClass(env, fullname);
            if (sym.exists()) {
                if (name == sym.name) return sym;
            }
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        return (pack != null) ? pack : bestSoFar;
    }
    Symbol findIdentInType(Env<AttrContext> env, Type site,
                           Name name, int kind) {
        Symbol bestSoFar = typeNotFound;
        Symbol sym;
        if ((kind & VAR) != 0) {
            sym = findField(env, site, name, site.tsym);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        if ((kind & TYP) != 0) {
            sym = findMemberType(env, site, name, site.tsym);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        return bestSoFar;
    }
    Symbol access(Symbol sym,
                  DiagnosticPosition pos,
                  Symbol location,
                  Type site,
                  Name name,
                  boolean qualified,
                  List<Type> argtypes,
                  List<Type> typeargtypes) {
        if (sym.kind >= AMBIGUOUS) {
            ResolveError errSym = (ResolveError)sym;
            if (!site.isErroneous() &&
                !Type.isErroneous(argtypes) &&
                (typeargtypes==null || !Type.isErroneous(typeargtypes)))
                logResolveError(errSym, pos, location, site, name, argtypes, typeargtypes);
            sym = errSym.access(name, qualified ? site.tsym : syms.noSymbol);
        }
        return sym;
    }
    Symbol access(Symbol sym,
                  DiagnosticPosition pos,
                  Type site,
                  Name name,
                  boolean qualified,
                  List<Type> argtypes,
                  List<Type> typeargtypes) {
        return access(sym, pos, site.tsym, site, name, qualified, argtypes, typeargtypes);
    }
    Symbol access(Symbol sym,
                  DiagnosticPosition pos,
                  Symbol location,
                  Type site,
                  Name name,
                  boolean qualified) {
        if (sym.kind >= AMBIGUOUS)
            return access(sym, pos, location, site, name, qualified, List.<Type>nil(), null);
        else
            return sym;
    }
    Symbol access(Symbol sym,
                  DiagnosticPosition pos,
                  Type site,
                  Name name,
                  boolean qualified) {
        return access(sym, pos, site.tsym, site, name, qualified);
    }
    void checkNonAbstract(DiagnosticPosition pos, Symbol sym) {
        if ((sym.flags() & ABSTRACT) != 0)
            log.error(pos, "abstract.cant.be.accessed.directly",
                      kindName(sym), sym, sym.location());
    }
    public void printscopes(Scope s) {
        while (s != null) {
            if (s.owner != null)
                System.err.print(s.owner + ": ");
            for (Scope.Entry e = s.elems; e != null; e = e.sibling) {
                if ((e.sym.flags() & ABSTRACT) != 0)
                    System.err.print("abstract ");
                System.err.print(e.sym + " ");
            }
            System.err.println();
            s = s.next;
        }
    }
    void printscopes(Env<AttrContext> env) {
        while (env.outer != null) {
            System.err.println("------------------------------");
            printscopes(env.info.scope);
            env = env.outer;
        }
    }
    public void printscopes(Type t) {
        while (t.tag == CLASS) {
            printscopes(t.tsym.members());
            t = types.supertype(t);
        }
    }
    Symbol resolveIdent(DiagnosticPosition pos, Env<AttrContext> env,
                        Name name, int kind) {
        return access(
            findIdent(env, name, kind),
            pos, env.enclClass.sym.type, name, false);
    }
    Symbol resolveMethod(DiagnosticPosition pos,
                         Env<AttrContext> env,
                         Name name,
                         List<Type> argtypes,
                         List<Type> typeargtypes) {
        Symbol sym = startResolution();
        List<MethodResolutionPhase> steps = methodResolutionSteps;
        while (steps.nonEmpty() &&
               steps.head.isApplicable(boxingEnabled, varargsEnabled) &&
               sym.kind >= ERRONEOUS) {
            currentStep = steps.head;
            sym = findFun(env, name, argtypes, typeargtypes,
                    steps.head.isBoxingRequired,
                    env.info.varArgs = steps.head.isVarargsRequired);
            methodResolutionCache.put(steps.head, sym);
            steps = steps.tail;
        }
        if (sym.kind >= AMBIGUOUS) {
            MethodResolutionPhase errPhase =
                    firstErroneousResolutionPhase();
            sym = access(methodResolutionCache.get(errPhase),
                    pos, env.enclClass.sym.type, name, false, argtypes, typeargtypes);
            env.info.varArgs = errPhase.isVarargsRequired;
        }
        return sym;
    }
    private Symbol startResolution() {
        wrongMethod.clear();
        wrongMethods.clear();
        return methodNotFound;
    }
    Symbol resolveQualifiedMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                  Type site, Name name, List<Type> argtypes,
                                  List<Type> typeargtypes) {
        return resolveQualifiedMethod(pos, env, site.tsym, site, name, argtypes, typeargtypes);
    }
    Symbol resolveQualifiedMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                  Symbol location, Type site, Name name, List<Type> argtypes,
                                  List<Type> typeargtypes) {
        Symbol sym = startResolution();
        List<MethodResolutionPhase> steps = methodResolutionSteps;
        while (steps.nonEmpty() &&
               steps.head.isApplicable(boxingEnabled, varargsEnabled) &&
               sym.kind >= ERRONEOUS) {
            currentStep = steps.head;
            sym = findMethod(env, site, name, argtypes, typeargtypes,
                    steps.head.isBoxingRequired(),
                    env.info.varArgs = steps.head.isVarargsRequired(), false);
            methodResolutionCache.put(steps.head, sym);
            steps = steps.tail;
        }
        if (sym.kind >= AMBIGUOUS) {
            if (site.tsym.isPolymorphicSignatureGeneric()) {
                env.info.varArgs = false;
                sym = findPolymorphicSignatureInstance(env,
                        site, name, null, argtypes);
            }
            else {
                MethodResolutionPhase errPhase =
                        firstErroneousResolutionPhase();
                sym = access(methodResolutionCache.get(errPhase),
                        pos, location, site, name, true, argtypes, typeargtypes);
                env.info.varArgs = errPhase.isVarargsRequired;
            }
        } else if (allowMethodHandles && sym.isPolymorphicSignatureGeneric()) {
            env.info.varArgs = false;
            sym = findPolymorphicSignatureInstance(env,
                    site, name, (MethodSymbol)sym, argtypes);
        }
        return sym;
    }
    Symbol findPolymorphicSignatureInstance(Env<AttrContext> env, Type site,
                                            Name name,
                                            MethodSymbol spMethod,  
                                            List<Type> argtypes) {
        Type mtype = infer.instantiatePolymorphicSignatureInstance(env,
                site, name, spMethod, argtypes);
        long flags = ABSTRACT | HYPOTHETICAL | POLYMORPHIC_SIGNATURE |
                    (spMethod != null ?
                        spMethod.flags() & Flags.AccessFlags :
                        Flags.PUBLIC | Flags.STATIC);
        Symbol m = null;
        for (Scope.Entry e = polymorphicSignatureScope.lookup(name);
             e.scope != null;
             e = e.next()) {
            Symbol sym = e.sym;
            if (types.isSameType(mtype, sym.type) &&
                (sym.flags() & Flags.STATIC) == (flags & Flags.STATIC) &&
                types.isSameType(sym.owner.type, site)) {
               m = sym;
               break;
            }
        }
        if (m == null) {
            m = new MethodSymbol(flags, name, mtype, site.tsym);
            polymorphicSignatureScope.enter(m);
        }
        return m;
    }
    public MethodSymbol resolveInternalMethod(DiagnosticPosition pos, Env<AttrContext> env,
                                        Type site, Name name,
                                        List<Type> argtypes,
                                        List<Type> typeargtypes) {
        Symbol sym = resolveQualifiedMethod(
            pos, env, site.tsym, site, name, argtypes, typeargtypes);
        if (sym.kind == MTH) return (MethodSymbol)sym;
        else throw new FatalError(
                 diags.fragment("fatal.err.cant.locate.meth",
                                name));
    }
    Symbol resolveConstructor(DiagnosticPosition pos,
                              Env<AttrContext> env,
                              Type site,
                              List<Type> argtypes,
                              List<Type> typeargtypes) {
        Symbol sym = startResolution();
        List<MethodResolutionPhase> steps = methodResolutionSteps;
        while (steps.nonEmpty() &&
               steps.head.isApplicable(boxingEnabled, varargsEnabled) &&
               sym.kind >= ERRONEOUS) {
            currentStep = steps.head;
            sym = resolveConstructor(pos, env, site, argtypes, typeargtypes,
                    steps.head.isBoxingRequired(),
                    env.info.varArgs = steps.head.isVarargsRequired());
            methodResolutionCache.put(steps.head, sym);
            steps = steps.tail;
        }
        if (sym.kind >= AMBIGUOUS) {
            MethodResolutionPhase errPhase = firstErroneousResolutionPhase();
            sym = access(methodResolutionCache.get(errPhase),
                    pos, site, names.init, true, argtypes, typeargtypes);
            env.info.varArgs = errPhase.isVarargsRequired();
        }
        return sym;
    }
    Symbol resolveDiamond(DiagnosticPosition pos,
                              Env<AttrContext> env,
                              Type site,
                              List<Type> argtypes,
                              List<Type> typeargtypes) {
        Symbol sym = startResolution();
        List<MethodResolutionPhase> steps = methodResolutionSteps;
        while (steps.nonEmpty() &&
               steps.head.isApplicable(boxingEnabled, varargsEnabled) &&
               sym.kind >= ERRONEOUS) {
            currentStep = steps.head;
            sym = resolveConstructor(pos, env, site, argtypes, typeargtypes,
                    steps.head.isBoxingRequired(),
                    env.info.varArgs = steps.head.isVarargsRequired());
            methodResolutionCache.put(steps.head, sym);
            steps = steps.tail;
        }
        if (sym.kind >= AMBIGUOUS) {
            final JCDiagnostic details = sym.kind == WRONG_MTH ?
                ((InapplicableSymbolError)sym).explanation :
                null;
            Symbol errSym = new ResolveError(WRONG_MTH, "diamond error") {
                @Override
                JCDiagnostic getDiagnostic(DiagnosticType dkind, DiagnosticPosition pos,
                        Symbol location, Type site, Name name, List<Type> argtypes, List<Type> typeargtypes) {
                    String key = details == null ?
                        "cant.apply.diamond" :
                        "cant.apply.diamond.1";
                    return diags.create(dkind, log.currentSource(), pos, key,
                            diags.fragment("diamond", site.tsym), details);
                }
            };
            MethodResolutionPhase errPhase = firstErroneousResolutionPhase();
            sym = access(errSym, pos, site, names.init, true, argtypes, typeargtypes);
            env.info.varArgs = errPhase.isVarargsRequired();
        }
        return sym;
    }
    Symbol resolveConstructor(DiagnosticPosition pos, Env<AttrContext> env,
                              Type site, List<Type> argtypes,
                              List<Type> typeargtypes,
                              boolean allowBoxing,
                              boolean useVarargs) {
        Symbol sym = findMethod(env, site,
                                names.init, argtypes,
                                typeargtypes, allowBoxing,
                                useVarargs, false);
        chk.checkDeprecated(pos, env.info.scope.owner, sym);
        return sym;
    }
    public MethodSymbol resolveInternalConstructor(DiagnosticPosition pos, Env<AttrContext> env,
                                        Type site,
                                        List<Type> argtypes,
                                        List<Type> typeargtypes) {
        Symbol sym = resolveConstructor(
            pos, env, site, argtypes, typeargtypes);
        if (sym.kind == MTH) return (MethodSymbol)sym;
        else throw new FatalError(
                 diags.fragment("fatal.err.cant.locate.ctor", site));
    }
    Symbol resolveOperator(DiagnosticPosition pos, int optag,
                           Env<AttrContext> env, List<Type> argtypes) {
        Name name = treeinfo.operatorName(optag);
        Symbol sym = findMethod(env, syms.predefClass.type, name, argtypes,
                                null, false, false, true);
        if (boxingEnabled && sym.kind >= WRONG_MTHS)
            sym = findMethod(env, syms.predefClass.type, name, argtypes,
                             null, true, false, true);
        return access(sym, pos, env.enclClass.sym.type, name,
                      false, argtypes, null);
    }
    Symbol resolveUnaryOperator(DiagnosticPosition pos, int optag, Env<AttrContext> env, Type arg) {
        return resolveOperator(pos, optag, env, List.of(arg));
    }
    Symbol resolveBinaryOperator(DiagnosticPosition pos,
                                 int optag,
                                 Env<AttrContext> env,
                                 Type left,
                                 Type right) {
        return resolveOperator(pos, optag, env, List.of(left, right));
    }
    Symbol resolveSelf(DiagnosticPosition pos,
                       Env<AttrContext> env,
                       TypeSymbol c,
                       Name name) {
        Env<AttrContext> env1 = env;
        boolean staticOnly = false;
        while (env1.outer != null) {
            if (isStatic(env1)) staticOnly = true;
            if (env1.enclClass.sym == c) {
                Symbol sym = env1.info.scope.lookup(name).sym;
                if (sym != null) {
                    if (staticOnly) sym = new StaticError(sym);
                    return access(sym, pos, env.enclClass.sym.type,
                                  name, true);
                }
            }
            if ((env1.enclClass.sym.flags() & STATIC) != 0) staticOnly = true;
            env1 = env1.outer;
        }
        log.error(pos, "not.encl.class", c);
        return syms.errSymbol;
    }
    Symbol resolveSelfContaining(DiagnosticPosition pos,
                                 Env<AttrContext> env,
                                 Symbol member,
                                 boolean isSuperCall) {
        Name name = names._this;
        Env<AttrContext> env1 = isSuperCall ? env.outer : env;
        boolean staticOnly = false;
        if (env1 != null) {
            while (env1 != null && env1.outer != null) {
                if (isStatic(env1)) staticOnly = true;
                if (env1.enclClass.sym.isSubClass(member.owner, types)) {
                    Symbol sym = env1.info.scope.lookup(name).sym;
                    if (sym != null) {
                        if (staticOnly) sym = new StaticError(sym);
                        return access(sym, pos, env.enclClass.sym.type,
                                      name, true);
                    }
                }
                if ((env1.enclClass.sym.flags() & STATIC) != 0)
                    staticOnly = true;
                env1 = env1.outer;
            }
        }
        log.error(pos, "encl.class.required", member);
        return syms.errSymbol;
    }
    Type resolveImplicitThis(DiagnosticPosition pos, Env<AttrContext> env, Type t) {
        return resolveImplicitThis(pos, env, t, false);
    }
    Type resolveImplicitThis(DiagnosticPosition pos, Env<AttrContext> env, Type t, boolean isSuperCall) {
        Type thisType = (((t.tsym.owner.kind & (MTH|VAR)) != 0)
                         ? resolveSelf(pos, env, t.getEnclosingType().tsym, names._this)
                         : resolveSelfContaining(pos, env, t.tsym, isSuperCall)).type;
        if (env.info.isSelfCall && thisType.tsym == env.enclClass.sym)
            log.error(pos, "cant.ref.before.ctor.called", "this");
        return thisType;
    }
    public void logAccessError(Env<AttrContext> env, JCTree tree, Type type) {
        AccessError error = new AccessError(env, type.getEnclosingType(), type.tsym);
        logResolveError(error, tree.pos(), type.getEnclosingType().tsym, type.getEnclosingType(), null, null, null);
    }
    private void logResolveError(ResolveError error,
            DiagnosticPosition pos,
            Symbol location,
            Type site,
            Name name,
            List<Type> argtypes,
            List<Type> typeargtypes) {
        JCDiagnostic d = error.getDiagnostic(JCDiagnostic.DiagnosticType.ERROR,
                pos, location, site, name, argtypes, typeargtypes);
        if (d != null) {
            d.setFlag(DiagnosticFlag.RESOLVE_ERROR);
            log.report(d);
        }
    }
    private final LocalizedString noArgs = new LocalizedString("compiler.misc.no.args");
    public Object methodArguments(List<Type> argtypes) {
        return argtypes.isEmpty() ? noArgs : argtypes;
    }
    private abstract class ResolveError extends Symbol {
        final String debugName;
        ResolveError(int kind, String debugName) {
            super(kind, 0, null, null, null);
            this.debugName = debugName;
        }
        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            throw new AssertionError();
        }
        @Override
        public String toString() {
            return debugName;
        }
        @Override
        public boolean exists() {
            return false;
        }
        protected Symbol access(Name name, TypeSymbol location) {
            return types.createErrorType(name, location, syms.errSymbol.type).tsym;
        }
        abstract JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes);
        boolean isOperator(Name name) {
            int i = 0;
            while (i < name.getByteLength() &&
                   "+-~!*/%&|^<>=".indexOf(name.getByteAt(i)) >= 0) i++;
            return i > 0 && i == name.getByteLength();
        }
    }
    abstract class InvalidSymbolError extends ResolveError {
        Symbol sym;
        InvalidSymbolError(int kind, Symbol sym, String debugName) {
            super(kind, debugName);
            this.sym = sym;
        }
        @Override
        public boolean exists() {
            return true;
        }
        @Override
        public String toString() {
             return super.toString() + " wrongSym=" + sym;
        }
        @Override
        public Symbol access(Name name, TypeSymbol location) {
            if (sym.kind >= AMBIGUOUS)
                return ((ResolveError)sym).access(name, location);
            else if ((sym.kind & ERRONEOUS) == 0 && (sym.kind & TYP) != 0)
                return types.createErrorType(name, location, sym.type).tsym;
            else
                return sym;
        }
    }
    class SymbolNotFoundError extends ResolveError {
        SymbolNotFoundError(int kind) {
            super(kind, "symbol not found error");
        }
        @Override
        JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes) {
            argtypes = argtypes == null ? List.<Type>nil() : argtypes;
            typeargtypes = typeargtypes == null ? List.<Type>nil() : typeargtypes;
            if (name == names.error)
                return null;
            if (isOperator(name)) {
                boolean isUnaryOp = argtypes.size() == 1;
                String key = argtypes.size() == 1 ?
                    "operator.cant.be.applied" :
                    "operator.cant.be.applied.1";
                Type first = argtypes.head;
                Type second = !isUnaryOp ? argtypes.tail.head : null;
                return diags.create(dkind, log.currentSource(), pos,
                        key, name, first, second);
            }
            boolean hasLocation = false;
            if (location == null) {
                location = site.tsym;
            }
            if (!location.name.isEmpty()) {
                if (location.kind == PCK && !site.tsym.exists()) {
                    return diags.create(dkind, log.currentSource(), pos,
                        "doesnt.exist", location);
                }
                hasLocation = !location.name.equals(names._this) &&
                        !location.name.equals(names._super);
            }
            boolean isConstructor = kind == ABSENT_MTH &&
                    name == names.table.names.init;
            KindName kindname = isConstructor ? KindName.CONSTRUCTOR : absentKind(kind);
            Name idname = isConstructor ? site.tsym.name : name;
            String errKey = getErrorKey(kindname, typeargtypes.nonEmpty(), hasLocation);
            if (hasLocation) {
                return diags.create(dkind, log.currentSource(), pos,
                        errKey, kindname, idname, 
                        typeargtypes, argtypes, 
                        getLocationDiag(location, site)); 
            }
            else {
                return diags.create(dkind, log.currentSource(), pos,
                        errKey, kindname, idname, 
                        typeargtypes, argtypes); 
            }
        }
        private String getErrorKey(KindName kindname, boolean hasTypeArgs, boolean hasLocation) {
            String key = "cant.resolve";
            String suffix = hasLocation ? ".location" : "";
            switch (kindname) {
                case METHOD:
                case CONSTRUCTOR: {
                    suffix += ".args";
                    suffix += hasTypeArgs ? ".params" : "";
                }
            }
            return key + suffix;
        }
        private JCDiagnostic getLocationDiag(Symbol location, Type site) {
            if (location.kind == VAR) {
                return diags.fragment("location.1",
                    kindName(location),
                    location,
                    location.type);
            } else {
                return diags.fragment("location",
                    typeKindName(site),
                    site,
                    null);
            }
        }
    }
    class InapplicableSymbolError extends InvalidSymbolError {
        JCDiagnostic explanation;
        InapplicableSymbolError(Symbol sym) {
            super(WRONG_MTH, sym, "inapplicable symbol error");
        }
        InapplicableSymbolError setWrongSym(Symbol sym, JCDiagnostic explanation) {
            this.sym = sym;
            if (this.sym == sym && explanation != null)
                this.explanation = explanation; 
            return this;
        }
        InapplicableSymbolError setWrongSym(Symbol sym) {
            this.sym = sym;
            return this;
        }
        @Override
        public String toString() {
            return super.toString() + " explanation=" + explanation;
        }
        @Override
        JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes) {
            if (name == names.error)
                return null;
            if (isOperator(name)) {
                boolean isUnaryOp = argtypes.size() == 1;
                String key = argtypes.size() == 1 ?
                    "operator.cant.be.applied" :
                    "operator.cant.be.applied.1";
                Type first = argtypes.head;
                Type second = !isUnaryOp ? argtypes.tail.head : null;
                return diags.create(dkind, log.currentSource(), pos,
                        key, name, first, second);
            }
            else {
                Symbol ws = sym.asMemberOf(site, types);
                return diags.create(dkind, log.currentSource(), pos,
                          "cant.apply.symbol" + (explanation != null ? ".1" : ""),
                          kindName(ws),
                          ws.name == names.init ? ws.owner.name : ws.name,
                          methodArguments(ws.type.getParameterTypes()),
                          methodArguments(argtypes),
                          kindName(ws.owner),
                          ws.owner.type,
                          explanation);
            }
        }
        void clear() {
            explanation = null;
        }
        @Override
        public Symbol access(Name name, TypeSymbol location) {
            return types.createErrorType(name, location, syms.errSymbol.type).tsym;
        }
    }
    class InapplicableSymbolsError extends ResolveError {
        private List<Candidate> candidates = List.nil();
        InapplicableSymbolsError(Symbol sym) {
            super(WRONG_MTHS, "inapplicable symbols");
        }
        @Override
        JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes) {
            if (candidates.nonEmpty()) {
                JCDiagnostic err = diags.create(dkind,
                        log.currentSource(),
                        pos,
                        "cant.apply.symbols",
                        name == names.init ? KindName.CONSTRUCTOR : absentKind(kind),
                        getName(),
                        argtypes);
                return new JCDiagnostic.MultilineDiagnostic(err, candidateDetails(site));
            } else {
                return new SymbolNotFoundError(ABSENT_MTH).getDiagnostic(dkind, pos,
                    location, site, name, argtypes, typeargtypes);
            }
        }
        List<JCDiagnostic> candidateDetails(Type site) {
            List<JCDiagnostic> details = List.nil();
            for (Candidate c : candidates)
                details = details.prepend(c.getDiagnostic(site));
            return details.reverse();
        }
        Symbol addCandidate(MethodResolutionPhase currentStep, Symbol sym, JCDiagnostic details) {
            Candidate c = new Candidate(currentStep, sym, details);
            if (c.isValid() && !candidates.contains(c))
                candidates = candidates.append(c);
            return this;
        }
        void clear() {
            candidates = List.nil();
        }
        private Name getName() {
            Symbol sym = candidates.head.sym;
            return sym.name == names.init ?
                sym.owner.name :
                sym.name;
        }
        private class Candidate {
            final MethodResolutionPhase step;
            final Symbol sym;
            final JCDiagnostic details;
            private Candidate(MethodResolutionPhase step, Symbol sym, JCDiagnostic details) {
                this.step = step;
                this.sym = sym;
                this.details = details;
            }
            JCDiagnostic getDiagnostic(Type site) {
                return diags.fragment("inapplicable.method",
                        Kinds.kindName(sym),
                        sym.location(site, types),
                        sym.asMemberOf(site, types),
                        details);
            }
            @Override
            public boolean equals(Object o) {
                if (o instanceof Candidate) {
                    Symbol s1 = this.sym;
                    Symbol s2 = ((Candidate)o).sym;
                    if  ((s1 != s2 &&
                        (s1.overrides(s2, s1.owner.type.tsym, types, false) ||
                        (s2.overrides(s1, s2.owner.type.tsym, types, false)))) ||
                        ((s1.isConstructor() || s2.isConstructor()) && s1.owner != s2.owner))
                        return true;
                }
                return false;
            }
            boolean isValid() {
                return  (((sym.flags() & VARARGS) != 0 && step == VARARITY) ||
                          (sym.flags() & VARARGS) == 0 && step == (boxingEnabled ? BOX : BASIC));
            }
        }
    }
    class AccessError extends InvalidSymbolError {
        private Env<AttrContext> env;
        private Type site;
        AccessError(Symbol sym) {
            this(null, null, sym);
        }
        AccessError(Env<AttrContext> env, Type site, Symbol sym) {
            super(HIDDEN, sym, "access error");
            this.env = env;
            this.site = site;
            if (debugResolve)
                log.error("proc.messager", sym + " @ " + site + " is inaccessible.");
        }
        @Override
        public boolean exists() {
            return false;
        }
        @Override
        JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes) {
            if (sym.owner.type.tag == ERROR)
                return null;
            if (sym.name == names.init && sym.owner != site.tsym) {
                return new SymbolNotFoundError(ABSENT_MTH).getDiagnostic(dkind,
                        pos, location, site, name, argtypes, typeargtypes);
            }
            else if ((sym.flags() & PUBLIC) != 0
                || (env != null && this.site != null
                    && !isAccessible(env, this.site))) {
                return diags.create(dkind, log.currentSource(),
                        pos, "not.def.access.class.intf.cant.access",
                    sym, sym.location());
            }
            else if ((sym.flags() & (PRIVATE | PROTECTED)) != 0) {
                return diags.create(dkind, log.currentSource(),
                        pos, "report.access", sym,
                        asFlagSet(sym.flags() & (PRIVATE | PROTECTED)),
                        sym.location());
            }
            else {
                return diags.create(dkind, log.currentSource(),
                        pos, "not.def.public.cant.access", sym, sym.location());
            }
        }
    }
    class StaticError extends InvalidSymbolError {
        StaticError(Symbol sym) {
            super(STATICERR, sym, "static error");
        }
        @Override
        JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes) {
            Symbol errSym = ((sym.kind == TYP && sym.type.tag == CLASS)
                ? types.erasure(sym.type).tsym
                : sym);
            return diags.create(dkind, log.currentSource(), pos,
                    "non-static.cant.be.ref", kindName(sym), errSym);
        }
    }
    class AmbiguityError extends InvalidSymbolError {
        Symbol sym2;
        AmbiguityError(Symbol sym1, Symbol sym2) {
            super(AMBIGUOUS, sym1, "ambiguity error");
            this.sym2 = sym2;
        }
        @Override
        JCDiagnostic getDiagnostic(JCDiagnostic.DiagnosticType dkind,
                DiagnosticPosition pos,
                Symbol location,
                Type site,
                Name name,
                List<Type> argtypes,
                List<Type> typeargtypes) {
            AmbiguityError pair = this;
            while (true) {
                if (pair.sym.kind == AMBIGUOUS)
                    pair = (AmbiguityError)pair.sym;
                else if (pair.sym2.kind == AMBIGUOUS)
                    pair = (AmbiguityError)pair.sym2;
                else break;
            }
            Name sname = pair.sym.name;
            if (sname == names.init) sname = pair.sym.owner.name;
            return diags.create(dkind, log.currentSource(),
                      pos, "ref.ambiguous", sname,
                      kindName(pair.sym),
                      pair.sym,
                      pair.sym.location(site, types),
                      kindName(pair.sym2),
                      pair.sym2,
                      pair.sym2.location(site, types));
        }
    }
    enum MethodResolutionPhase {
        BASIC(false, false),
        BOX(true, false),
        VARARITY(true, true);
        boolean isBoxingRequired;
        boolean isVarargsRequired;
        MethodResolutionPhase(boolean isBoxingRequired, boolean isVarargsRequired) {
           this.isBoxingRequired = isBoxingRequired;
           this.isVarargsRequired = isVarargsRequired;
        }
        public boolean isBoxingRequired() {
            return isBoxingRequired;
        }
        public boolean isVarargsRequired() {
            return isVarargsRequired;
        }
        public boolean isApplicable(boolean boxingEnabled, boolean varargsEnabled) {
            return (varargsEnabled || !isVarargsRequired) &&
                   (boxingEnabled || !isBoxingRequired);
        }
    }
    private Map<MethodResolutionPhase, Symbol> methodResolutionCache =
        new HashMap<MethodResolutionPhase, Symbol>(MethodResolutionPhase.values().length);
    final List<MethodResolutionPhase> methodResolutionSteps = List.of(BASIC, BOX, VARARITY);
    private MethodResolutionPhase currentStep = null;
    private MethodResolutionPhase firstErroneousResolutionPhase() {
        MethodResolutionPhase bestSoFar = BASIC;
        Symbol sym = methodNotFound;
        List<MethodResolutionPhase> steps = methodResolutionSteps;
        while (steps.nonEmpty() &&
               steps.head.isApplicable(boxingEnabled, varargsEnabled) &&
               sym.kind >= WRONG_MTHS) {
            sym = methodResolutionCache.get(steps.head);
            bestSoFar = steps.head;
            steps = steps.tail;
        }
        return bestSoFar;
    }
}
