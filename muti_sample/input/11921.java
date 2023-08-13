public class Check {
    protected static final Context.Key<Check> checkKey =
        new Context.Key<Check>();
    private final Names names;
    private final Log log;
    private final Symtab syms;
    private final Enter enter;
    private final Infer infer;
    private final Types types;
    private final JCDiagnostic.Factory diags;
    private final boolean skipAnnotations;
    private boolean warnOnSyntheticConflicts;
    private boolean suppressAbortOnBadClassFile;
    private boolean enableSunApiLintControl;
    private final TreeInfo treeinfo;
    private Lint lint;
    private MethodSymbol method;
    public static Check instance(Context context) {
        Check instance = context.get(checkKey);
        if (instance == null)
            instance = new Check(context);
        return instance;
    }
    protected Check(Context context) {
        context.put(checkKey, this);
        names = Names.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        enter = Enter.instance(context);
        infer = Infer.instance(context);
        this.types = Types.instance(context);
        diags = JCDiagnostic.Factory.instance(context);
        Options options = Options.instance(context);
        lint = Lint.instance(context);
        treeinfo = TreeInfo.instance(context);
        Source source = Source.instance(context);
        allowGenerics = source.allowGenerics();
        allowAnnotations = source.allowAnnotations();
        allowCovariantReturns = source.allowCovariantReturns();
        allowSimplifiedVarargs = source.allowSimplifiedVarargs();
        complexInference = options.isSet(COMPLEXINFERENCE);
        skipAnnotations = options.isSet("skipAnnotations");
        warnOnSyntheticConflicts = options.isSet("warnOnSyntheticConflicts");
        suppressAbortOnBadClassFile = options.isSet("suppressAbortOnBadClassFile");
        enableSunApiLintControl = options.isSet("enableSunApiLintControl");
        Target target = Target.instance(context);
        syntheticNameChar = target.syntheticNameChar();
        boolean verboseDeprecated = lint.isEnabled(LintCategory.DEPRECATION);
        boolean verboseUnchecked = lint.isEnabled(LintCategory.UNCHECKED);
        boolean verboseSunApi = lint.isEnabled(LintCategory.SUNAPI);
        boolean enforceMandatoryWarnings = source.enforceMandatoryWarnings();
        deprecationHandler = new MandatoryWarningHandler(log, verboseDeprecated,
                enforceMandatoryWarnings, "deprecated", LintCategory.DEPRECATION);
        uncheckedHandler = new MandatoryWarningHandler(log, verboseUnchecked,
                enforceMandatoryWarnings, "unchecked", LintCategory.UNCHECKED);
        sunApiHandler = new MandatoryWarningHandler(log, verboseSunApi,
                enforceMandatoryWarnings, "sunapi", null);
        deferredLintHandler = DeferredLintHandler.immediateHandler;
    }
    boolean allowGenerics;
    boolean allowAnnotations;
    boolean allowCovariantReturns;
    boolean allowSimplifiedVarargs;
    boolean complexInference;
    char syntheticNameChar;
    public Map<Name,ClassSymbol> compiled = new HashMap<Name, ClassSymbol>();
    private MandatoryWarningHandler deprecationHandler;
    private MandatoryWarningHandler uncheckedHandler;
    private MandatoryWarningHandler sunApiHandler;
    private DeferredLintHandler deferredLintHandler;
    Lint setLint(Lint newLint) {
        Lint prev = lint;
        lint = newLint;
        return prev;
    }
    DeferredLintHandler setDeferredLintHandler(DeferredLintHandler newDeferredLintHandler) {
        DeferredLintHandler prev = deferredLintHandler;
        deferredLintHandler = newDeferredLintHandler;
        return prev;
    }
    MethodSymbol setMethod(MethodSymbol newMethod) {
        MethodSymbol prev = method;
        method = newMethod;
        return prev;
    }
    void warnDeprecated(DiagnosticPosition pos, Symbol sym) {
        if (!lint.isSuppressed(LintCategory.DEPRECATION))
            deprecationHandler.report(pos, "has.been.deprecated", sym, sym.location());
    }
    public void warnUnchecked(DiagnosticPosition pos, String msg, Object... args) {
        if (!lint.isSuppressed(LintCategory.UNCHECKED))
            uncheckedHandler.report(pos, msg, args);
    }
    void warnUnsafeVararg(DiagnosticPosition pos, String key, Object... args) {
        if (lint.isEnabled(LintCategory.VARARGS) && allowSimplifiedVarargs)
            log.warning(LintCategory.VARARGS, pos, key, args);
    }
    public void warnSunApi(DiagnosticPosition pos, String msg, Object... args) {
        if (!lint.isSuppressed(LintCategory.SUNAPI))
            sunApiHandler.report(pos, msg, args);
    }
    public void warnStatic(DiagnosticPosition pos, String msg, Object... args) {
        if (lint.isEnabled(LintCategory.STATIC))
            log.warning(LintCategory.STATIC, pos, msg, args);
    }
    public void reportDeferredDiagnostics() {
        deprecationHandler.reportDeferredDiagnostic();
        uncheckedHandler.reportDeferredDiagnostic();
        sunApiHandler.reportDeferredDiagnostic();
    }
    public Type completionError(DiagnosticPosition pos, CompletionFailure ex) {
        log.error(pos, "cant.access", ex.sym, ex.getDetailValue());
        if (ex instanceof ClassReader.BadClassFile
                && !suppressAbortOnBadClassFile) throw new Abort();
        else return syms.errType;
    }
    Type typeError(DiagnosticPosition pos, Object problem, Type found, Type req) {
        log.error(pos, "prob.found.req",
                  problem, found, req);
        return types.createErrorType(found);
    }
    Type typeError(DiagnosticPosition pos, String problem, Type found, Type req, Object explanation) {
        log.error(pos, "prob.found.req.1", problem, found, req, explanation);
        return types.createErrorType(found);
    }
    Type typeTagError(DiagnosticPosition pos, Object required, Object found) {
        if (found instanceof Type && ((Type)found).tag == VOID) {
            log.error(pos, "illegal.start.of.type");
            return syms.errType;
        }
        log.error(pos, "type.found.req", found, required);
        return types.createErrorType(found instanceof Type ? (Type)found : syms.errType);
    }
    void earlyRefError(DiagnosticPosition pos, Symbol sym) {
        log.error(pos, "cant.ref.before.ctor.called", sym);
    }
    void duplicateError(DiagnosticPosition pos, Symbol sym) {
        if (!sym.type.isErroneous()) {
            log.error(pos, "already.defined", sym, sym.location());
        }
    }
    void varargsDuplicateError(DiagnosticPosition pos, Symbol sym1, Symbol sym2) {
        if (!sym1.type.isErroneous() && !sym2.type.isErroneous()) {
            log.error(pos, "array.and.varargs", sym1, sym2, sym2.location());
        }
    }
    void checkTransparentVar(DiagnosticPosition pos, VarSymbol v, Scope s) {
        if (s.next != null) {
            for (Scope.Entry e = s.next.lookup(v.name);
                 e.scope != null && e.sym.owner == v.owner;
                 e = e.next()) {
                if (e.sym.kind == VAR &&
                    (e.sym.owner.kind & (VAR | MTH)) != 0 &&
                    v.name != names.error) {
                    duplicateError(pos, e.sym);
                    return;
                }
            }
        }
    }
    void checkTransparentClass(DiagnosticPosition pos, ClassSymbol c, Scope s) {
        if (s.next != null) {
            for (Scope.Entry e = s.next.lookup(c.name);
                 e.scope != null && e.sym.owner == c.owner;
                 e = e.next()) {
                if (e.sym.kind == TYP && e.sym.type.tag != TYPEVAR &&
                    (e.sym.owner.kind & (VAR | MTH)) != 0 &&
                    c.name != names.error) {
                    duplicateError(pos, e.sym);
                    return;
                }
            }
        }
    }
    boolean checkUniqueClassName(DiagnosticPosition pos, Name name, Scope s) {
        for (Scope.Entry e = s.lookup(name); e.scope == s; e = e.next()) {
            if (e.sym.kind == TYP && e.sym.name != names.error) {
                duplicateError(pos, e.sym);
                return false;
            }
        }
        for (Symbol sym = s.owner; sym != null; sym = sym.owner) {
            if (sym.kind == TYP && sym.name == name && sym.name != names.error) {
                duplicateError(pos, sym);
                return true;
            }
        }
        return true;
    }
    Name localClassName(ClassSymbol c) {
        for (int i=1; ; i++) {
            Name flatname = names.
                fromString("" + c.owner.enclClass().flatname +
                           syntheticNameChar + i +
                           c.name);
            if (compiled.get(flatname) == null) return flatname;
        }
    }
    Type checkType(DiagnosticPosition pos, Type found, Type req) {
        return checkType(pos, found, req, "incompatible.types");
    }
    Type checkType(DiagnosticPosition pos, Type found, Type req, String errKey) {
        if (req.tag == ERROR)
            return req;
        if (found.tag == FORALL)
            return instantiatePoly(pos, (ForAll)found, req, convertWarner(pos, found, req));
        if (req.tag == NONE)
            return found;
        if (types.isAssignable(found, req, convertWarner(pos, found, req)))
            return found;
        if (found.tag <= DOUBLE && req.tag <= DOUBLE)
            return typeError(pos, diags.fragment("possible.loss.of.precision"), found, req);
        if (found.isSuperBound()) {
            log.error(pos, "assignment.from.super-bound", found);
            return types.createErrorType(found);
        }
        if (req.isExtendsBound()) {
            log.error(pos, "assignment.to.extends-bound", req);
            return types.createErrorType(found);
        }
        return typeError(pos, diags.fragment(errKey), found, req);
    }
    Type instantiatePoly(DiagnosticPosition pos, ForAll t, Type pt, Warner warn) throws Infer.NoInstanceException {
        if (pt == Infer.anyPoly && complexInference) {
            return t;
        } else if (pt == Infer.anyPoly || pt.tag == NONE) {
            Type newpt = t.qtype.tag <= VOID ? t.qtype : syms.objectType;
            return instantiatePoly(pos, t, newpt, warn);
        } else if (pt.tag == ERROR) {
            return pt;
        } else {
            try {
                return infer.instantiateExpr(t, pt, warn);
            } catch (Infer.NoInstanceException ex) {
                if (ex.isAmbiguous) {
                    JCDiagnostic d = ex.getDiagnostic();
                    log.error(pos,
                              "undetermined.type" + (d!=null ? ".1" : ""),
                              t, d);
                    return types.createErrorType(pt);
                } else {
                    JCDiagnostic d = ex.getDiagnostic();
                    return typeError(pos,
                                     diags.fragment("incompatible.types" + (d!=null ? ".1" : ""), d),
                                     t, pt);
                }
            } catch (Infer.InvalidInstanceException ex) {
                JCDiagnostic d = ex.getDiagnostic();
                log.error(pos, "invalid.inferred.types", t.tvars, d);
                return types.createErrorType(pt);
            }
        }
    }
    Type checkCastable(DiagnosticPosition pos, Type found, Type req) {
        if (found.tag == FORALL) {
            instantiatePoly(pos, (ForAll) found, req, castWarner(pos, found, req));
            return req;
        } else if (types.isCastable(found, req, castWarner(pos, found, req))) {
            return req;
        } else {
            return typeError(pos,
                             diags.fragment("inconvertible.types"),
                             found, req);
        }
    }
        boolean isTypeVar(Type t) {
            return t.tag == TYPEVAR || t.tag == ARRAY && isTypeVar(types.elemtype(t));
        }
    private boolean checkExtends(Type a, TypeVar bs) {
         if (a.isUnbound()) {
             return true;
         } else if (a.tag != WILDCARD) {
             a = types.upperBound(a);
             return types.isSubtype(a, bs.bound);
         } else if (a.isExtendsBound()) {
             return types.isCastable(bs.getUpperBound(), types.upperBound(a), Warner.noWarnings);
         } else if (a.isSuperBound()) {
             return !types.notSoftSubtype(types.lowerBound(a), bs.getUpperBound());
         }
         return true;
     }
    Type checkNonVoid(DiagnosticPosition pos, Type t) {
        if (t.tag == VOID) {
            log.error(pos, "void.not.allowed.here");
            return types.createErrorType(t);
        } else {
            return t;
        }
    }
    Type checkClassType(DiagnosticPosition pos, Type t) {
        if (t.tag != CLASS && t.tag != ERROR)
            return typeTagError(pos,
                                diags.fragment("type.req.class"),
                                (t.tag == TYPEVAR)
                                ? diags.fragment("type.parameter", t)
                                : t);
        else
            return t;
    }
    Type checkClassType(DiagnosticPosition pos, Type t, boolean noBounds) {
        t = checkClassType(pos, t);
        if (noBounds && t.isParameterized()) {
            List<Type> args = t.getTypeArguments();
            while (args.nonEmpty()) {
                if (args.head.tag == WILDCARD)
                    return typeTagError(pos,
                                        diags.fragment("type.req.exact"),
                                        args.head);
                args = args.tail;
            }
        }
        return t;
    }
    Type checkReifiableReferenceType(DiagnosticPosition pos, Type t) {
        if (t.tag != CLASS && t.tag != ARRAY && t.tag != ERROR) {
            return typeTagError(pos,
                                diags.fragment("type.req.class.array"),
                                t);
        } else if (!types.isReifiable(t)) {
            log.error(pos, "illegal.generic.type.for.instof");
            return types.createErrorType(t);
        } else {
            return t;
        }
    }
    Type checkRefType(DiagnosticPosition pos, Type t) {
        switch (t.tag) {
        case CLASS:
        case ARRAY:
        case TYPEVAR:
        case WILDCARD:
        case ERROR:
            return t;
        default:
            return typeTagError(pos,
                                diags.fragment("type.req.ref"),
                                t);
        }
    }
    List<Type> checkRefTypes(List<JCExpression> trees, List<Type> types) {
        List<JCExpression> tl = trees;
        for (List<Type> l = types; l.nonEmpty(); l = l.tail) {
            l.head = checkRefType(tl.head.pos(), l.head);
            tl = tl.tail;
        }
        return types;
    }
    Type checkNullOrRefType(DiagnosticPosition pos, Type t) {
        switch (t.tag) {
        case CLASS:
        case ARRAY:
        case TYPEVAR:
        case WILDCARD:
        case BOT:
        case ERROR:
            return t;
        default:
            return typeTagError(pos,
                                diags.fragment("type.req.ref"),
                                t);
        }
    }
    boolean checkDisjoint(DiagnosticPosition pos, long flags, long set1, long set2) {
        if ((flags & set1) != 0 && (flags & set2) != 0) {
            log.error(pos,
                      "illegal.combination.of.modifiers",
                      asFlagSet(TreeInfo.firstFlag(flags & set1)),
                      asFlagSet(TreeInfo.firstFlag(flags & set2)));
            return false;
        } else
            return true;
    }
    Type checkDiamond(JCNewClass tree, Type t) {
        if (!TreeInfo.isDiamond(tree) ||
                t.isErroneous()) {
            return checkClassType(tree.clazz.pos(), t, true);
        } else if (tree.def != null) {
            log.error(tree.clazz.pos(),
                    "cant.apply.diamond.1",
                    t, diags.fragment("diamond.and.anon.class", t));
            return types.createErrorType(t);
        } else if (t.tsym.type.getTypeArguments().isEmpty()) {
            log.error(tree.clazz.pos(),
                "cant.apply.diamond.1",
                t, diags.fragment("diamond.non.generic", t));
            return types.createErrorType(t);
        } else if (tree.typeargs != null &&
                tree.typeargs.nonEmpty()) {
            log.error(tree.clazz.pos(),
                "cant.apply.diamond.1",
                t, diags.fragment("diamond.and.explicit.params", t));
            return types.createErrorType(t);
        } else {
            return t;
        }
    }
    void checkVarargsMethodDecl(Env<AttrContext> env, JCMethodDecl tree) {
        MethodSymbol m = tree.sym;
        if (!allowSimplifiedVarargs) return;
        boolean hasTrustMeAnno = m.attribute(syms.trustMeType.tsym) != null;
        Type varargElemType = null;
        if (m.isVarArgs()) {
            varargElemType = types.elemtype(tree.params.last().type);
        }
        if (hasTrustMeAnno && !isTrustMeAllowedOnMethod(m)) {
            if (varargElemType != null) {
                log.error(tree,
                        "varargs.invalid.trustme.anno",
                        syms.trustMeType.tsym,
                        diags.fragment("varargs.trustme.on.virtual.varargs", m));
            } else {
                log.error(tree,
                            "varargs.invalid.trustme.anno",
                            syms.trustMeType.tsym,
                            diags.fragment("varargs.trustme.on.non.varargs.meth", m));
            }
        } else if (hasTrustMeAnno && varargElemType != null &&
                            types.isReifiable(varargElemType)) {
            warnUnsafeVararg(tree,
                            "varargs.redundant.trustme.anno",
                            syms.trustMeType.tsym,
                            diags.fragment("varargs.trustme.on.reifiable.varargs", varargElemType));
        }
        else if (!hasTrustMeAnno && varargElemType != null &&
                !types.isReifiable(varargElemType)) {
            warnUnchecked(tree.params.head.pos(), "unchecked.varargs.non.reifiable.type", varargElemType);
        }
    }
        private boolean isTrustMeAllowedOnMethod(Symbol s) {
            return (s.flags() & VARARGS) != 0 &&
                (s.isConstructor() ||
                    (s.flags() & (STATIC | FINAL)) != 0);
        }
    void checkVararg(DiagnosticPosition pos, List<Type> argtypes, Symbol msym) {
        Type argtype = argtypes.last();
        if (!types.isReifiable(argtype) &&
                (!allowSimplifiedVarargs ||
                msym.attribute(syms.trustMeType.tsym) == null ||
                !isTrustMeAllowedOnMethod(msym))) {
            warnUnchecked(pos,
                              "unchecked.generic.array.creation",
                              argtype);
        }
    }
    public boolean checkValidGenericType(Type t) {
        return firstIncompatibleTypeArg(t) == null;
    }
        private Type firstIncompatibleTypeArg(Type type) {
            List<Type> formals = type.tsym.type.allparams();
            List<Type> actuals = type.allparams();
            List<Type> args = type.getTypeArguments();
            List<Type> forms = type.tsym.type.getTypeArguments();
            ListBuffer<Type> tvars_buf = new ListBuffer<Type>();
            while (args.nonEmpty() && forms.nonEmpty()) {
                tvars_buf.append(types.substBound(((TypeVar)forms.head),
                                                  formals,
                                                  actuals));
                args = args.tail;
                forms = forms.tail;
            }
            args = type.getTypeArguments();
            List<Type> tvars_cap = types.substBounds(formals,
                                      formals,
                                      types.capture(type).allparams());
            while (args.nonEmpty() && tvars_cap.nonEmpty()) {
                args.head.withTypeVar((TypeVar)tvars_cap.head);
                args = args.tail;
                tvars_cap = tvars_cap.tail;
            }
            args = type.getTypeArguments();
            List<Type> tvars = tvars_buf.toList();
            while (args.nonEmpty() && tvars.nonEmpty()) {
                Type actual = types.subst(args.head,
                    type.tsym.type.getTypeArguments(),
                    tvars_buf.toList());
                if (!isTypeArgErroneous(actual) &&
                        !tvars.head.getUpperBound().isErroneous() &&
                        !checkExtends(actual, (TypeVar)tvars.head)) {
                    return args.head;
                }
                args = args.tail;
                tvars = tvars.tail;
            }
            args = type.getTypeArguments();
            tvars = tvars_buf.toList();
            for (Type arg : types.capture(type).getTypeArguments()) {
                if (arg.tag == TYPEVAR &&
                        arg.getUpperBound().isErroneous() &&
                        !tvars.head.getUpperBound().isErroneous() &&
                        !isTypeArgErroneous(args.head)) {
                    return args.head;
                }
                tvars = tvars.tail;
                args = args.tail;
            }
            return null;
        }
        boolean isTypeArgErroneous(Type t) {
            return isTypeArgErroneous.visit(t);
        }
        Types.UnaryVisitor<Boolean> isTypeArgErroneous = new Types.UnaryVisitor<Boolean>() {
            public Boolean visitType(Type t, Void s) {
                return t.isErroneous();
            }
            @Override
            public Boolean visitTypeVar(TypeVar t, Void s) {
                return visit(t.getUpperBound());
            }
            @Override
            public Boolean visitCapturedType(CapturedType t, Void s) {
                return visit(t.getUpperBound()) ||
                        visit(t.getLowerBound());
            }
            @Override
            public Boolean visitWildcardType(WildcardType t, Void s) {
                return visit(t.type);
            }
        };
    long checkFlags(DiagnosticPosition pos, long flags, Symbol sym, JCTree tree) {
        long mask;
        long implicit = 0;
        switch (sym.kind) {
        case VAR:
            if (sym.owner.kind != TYP)
                mask = LocalVarFlags;
            else if ((sym.owner.flags_field & INTERFACE) != 0)
                mask = implicit = InterfaceVarFlags;
            else
                mask = VarFlags;
            break;
        case MTH:
            if (sym.name == names.init) {
                if ((sym.owner.flags_field & ENUM) != 0) {
                    implicit = PRIVATE;
                    mask = PRIVATE;
                } else
                    mask = ConstructorFlags;
            }  else if ((sym.owner.flags_field & INTERFACE) != 0)
                mask = implicit = InterfaceMethodFlags;
            else {
                mask = MethodFlags;
            }
            if (((flags|implicit) & Flags.ABSTRACT) == 0)
              implicit |= sym.owner.flags_field & STRICTFP;
            break;
        case TYP:
            if (sym.isLocal()) {
                mask = LocalClassFlags;
                if (sym.name.isEmpty()) { 
                    mask |= STATIC;
                    implicit |= FINAL;
                }
                if ((sym.owner.flags_field & STATIC) == 0 &&
                    (flags & ENUM) != 0)
                    log.error(pos, "enums.must.be.static");
            } else if (sym.owner.kind == TYP) {
                mask = MemberClassFlags;
                if (sym.owner.owner.kind == PCK ||
                    (sym.owner.flags_field & STATIC) != 0)
                    mask |= STATIC;
                else if ((flags & ENUM) != 0)
                    log.error(pos, "enums.must.be.static");
                if ((flags & (INTERFACE | ENUM)) != 0 ) implicit = STATIC;
            } else {
                mask = ClassFlags;
            }
            if ((flags & INTERFACE) != 0) implicit |= ABSTRACT;
            if ((flags & ENUM) != 0) {
                mask &= ~(ABSTRACT | FINAL);
                implicit |= implicitEnumFinalFlag(tree);
            }
            implicit |= sym.owner.flags_field & STRICTFP;
            break;
        default:
            throw new AssertionError();
        }
        long illegal = flags & StandardFlags & ~mask;
        if (illegal != 0) {
            if ((illegal & INTERFACE) != 0) {
                log.error(pos, "intf.not.allowed.here");
                mask |= INTERFACE;
            }
            else {
                log.error(pos,
                          "mod.not.allowed.here", asFlagSet(illegal));
            }
        }
        else if ((sym.kind == TYP ||
                  checkDisjoint(pos, flags,
                                ABSTRACT,
                                PRIVATE | STATIC))
                 &&
                 checkDisjoint(pos, flags,
                               ABSTRACT | INTERFACE,
                               FINAL | NATIVE | SYNCHRONIZED)
                 &&
                 checkDisjoint(pos, flags,
                               PUBLIC,
                               PRIVATE | PROTECTED)
                 &&
                 checkDisjoint(pos, flags,
                               PRIVATE,
                               PUBLIC | PROTECTED)
                 &&
                 checkDisjoint(pos, flags,
                               FINAL,
                               VOLATILE)
                 &&
                 (sym.kind == TYP ||
                  checkDisjoint(pos, flags,
                                ABSTRACT | NATIVE,
                                STRICTFP))) {
        }
        return flags & (mask | ~StandardFlags) | implicit;
    }
    private long implicitEnumFinalFlag(JCTree tree) {
        if (tree.getTag() != JCTree.CLASSDEF) return 0;
        class SpecialTreeVisitor extends JCTree.Visitor {
            boolean specialized;
            SpecialTreeVisitor() {
                this.specialized = false;
            };
            @Override
            public void visitTree(JCTree tree) {  }
            @Override
            public void visitVarDef(JCVariableDecl tree) {
                if ((tree.mods.flags & ENUM) != 0) {
                    if (tree.init instanceof JCNewClass &&
                        ((JCNewClass) tree.init).def != null) {
                        specialized = true;
                    }
                }
            }
        }
        SpecialTreeVisitor sts = new SpecialTreeVisitor();
        JCClassDecl cdef = (JCClassDecl) tree;
        for (JCTree defs: cdef.defs) {
            defs.accept(sts);
            if (sts.specialized) return 0;
        }
        return FINAL;
    }
    void validate(JCTree tree, Env<AttrContext> env) {
        validate(tree, env, true);
    }
    void validate(JCTree tree, Env<AttrContext> env, boolean checkRaw) {
        new Validator(env).validateTree(tree, checkRaw, true);
    }
    void validate(List<? extends JCTree> trees, Env<AttrContext> env) {
        for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
            validate(l.head, env);
    }
    class Validator extends JCTree.Visitor {
        boolean isOuter;
        Env<AttrContext> env;
        Validator(Env<AttrContext> env) {
            this.env = env;
        }
        @Override
        public void visitTypeArray(JCArrayTypeTree tree) {
            tree.elemtype.accept(this);
        }
        @Override
        public void visitTypeApply(JCTypeApply tree) {
            if (tree.type.tag == CLASS) {
                List<JCExpression> args = tree.arguments;
                List<Type> forms = tree.type.tsym.type.getTypeArguments();
                Type incompatibleArg = firstIncompatibleTypeArg(tree.type);
                if (incompatibleArg != null) {
                    for (JCTree arg : tree.arguments) {
                        if (arg.type == incompatibleArg) {
                            log.error(arg, "not.within.bounds", incompatibleArg, forms.head);
                        }
                        forms = forms.tail;
                     }
                 }
                forms = tree.type.tsym.type.getTypeArguments();
                boolean is_java_lang_Class = tree.type.tsym.flatName() == names.java_lang_Class;
                while (args.nonEmpty() && forms.nonEmpty()) {
                    validateTree(args.head,
                            !(isOuter && is_java_lang_Class),
                            false);
                    args = args.tail;
                    forms = forms.tail;
                }
                if (tree.type.getEnclosingType().isRaw())
                    log.error(tree.pos(), "improperly.formed.type.inner.raw.param");
                if (tree.clazz.getTag() == JCTree.SELECT)
                    visitSelectInternal((JCFieldAccess)tree.clazz);
            }
        }
        @Override
        public void visitTypeParameter(JCTypeParameter tree) {
            validateTrees(tree.bounds, true, isOuter);
            checkClassBounds(tree.pos(), tree.type);
        }
        @Override
        public void visitWildcard(JCWildcard tree) {
            if (tree.inner != null)
                validateTree(tree.inner, true, isOuter);
        }
        @Override
        public void visitSelect(JCFieldAccess tree) {
            if (tree.type.tag == CLASS) {
                visitSelectInternal(tree);
                if (tree.selected.type.isParameterized() && tree.type.tsym.type.getTypeArguments().nonEmpty())
                    log.error(tree.pos(), "improperly.formed.type.param.missing");
            }
        }
        public void visitSelectInternal(JCFieldAccess tree) {
            if (tree.type.tsym.isStatic() &&
                tree.selected.type.isParameterized()) {
                log.error(tree.pos(), "cant.select.static.class.from.param.type");
            } else {
                tree.selected.accept(this);
            }
        }
        @Override
        public void visitTree(JCTree tree) {
        }
        public void validateTree(JCTree tree, boolean checkRaw, boolean isOuter) {
            try {
                if (tree != null) {
                    this.isOuter = isOuter;
                    tree.accept(this);
                    if (checkRaw)
                        checkRaw(tree, env);
                }
            } catch (CompletionFailure ex) {
                completionError(tree.pos(), ex);
            }
        }
        public void validateTrees(List<? extends JCTree> trees, boolean checkRaw, boolean isOuter) {
            for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
                validateTree(l.head, checkRaw, isOuter);
        }
        void checkRaw(JCTree tree, Env<AttrContext> env) {
            if (lint.isEnabled(LintCategory.RAW) &&
                tree.type.tag == CLASS &&
                !TreeInfo.isDiamond(tree) &&
                !env.enclClass.name.isEmpty() &&  
                tree.type.isRaw()) {
                log.warning(LintCategory.RAW,
                        tree.pos(), "raw.class.use", tree.type, tree.type.tsym.type);
            }
        }
    }
    boolean subset(Type t, List<Type> ts) {
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            if (types.isSubtype(t, l.head)) return true;
        return false;
    }
    boolean intersects(Type t, List<Type> ts) {
        for (List<Type> l = ts; l.nonEmpty(); l = l.tail)
            if (types.isSubtype(t, l.head) || types.isSubtype(l.head, t)) return true;
        return false;
    }
    List<Type> incl(Type t, List<Type> ts) {
        return subset(t, ts) ? ts : excl(t, ts).prepend(t);
    }
    List<Type> excl(Type t, List<Type> ts) {
        if (ts.isEmpty()) {
            return ts;
        } else {
            List<Type> ts1 = excl(t, ts.tail);
            if (types.isSubtype(ts.head, t)) return ts1;
            else if (ts1 == ts.tail) return ts;
            else return ts1.prepend(ts.head);
        }
    }
    List<Type> union(List<Type> ts1, List<Type> ts2) {
        List<Type> ts = ts1;
        for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
            ts = incl(l.head, ts);
        return ts;
    }
    List<Type> diff(List<Type> ts1, List<Type> ts2) {
        List<Type> ts = ts1;
        for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
            ts = excl(l.head, ts);
        return ts;
    }
    public List<Type> intersect(List<Type> ts1, List<Type> ts2) {
        List<Type> ts = List.nil();
        for (List<Type> l = ts1; l.nonEmpty(); l = l.tail)
            if (subset(l.head, ts2)) ts = incl(l.head, ts);
        for (List<Type> l = ts2; l.nonEmpty(); l = l.tail)
            if (subset(l.head, ts1)) ts = incl(l.head, ts);
        return ts;
    }
    boolean isUnchecked(ClassSymbol exc) {
        return
            exc.kind == ERR ||
            exc.isSubClass(syms.errorType.tsym, types) ||
            exc.isSubClass(syms.runtimeExceptionType.tsym, types);
    }
    boolean isUnchecked(Type exc) {
        return
            (exc.tag == TYPEVAR) ? isUnchecked(types.supertype(exc)) :
            (exc.tag == CLASS) ? isUnchecked((ClassSymbol)exc.tsym) :
            exc.tag == BOT;
    }
    boolean isUnchecked(DiagnosticPosition pos, Type exc) {
        try {
            return isUnchecked(exc);
        } catch (CompletionFailure ex) {
            completionError(pos, ex);
            return true;
        }
    }
    boolean isHandled(Type exc, List<Type> handled) {
        return isUnchecked(exc) || subset(exc, handled);
    }
    List<Type> unhandled(List<Type> thrown, List<Type> handled) {
        List<Type> unhandled = List.nil();
        for (List<Type> l = thrown; l.nonEmpty(); l = l.tail)
            if (!isHandled(l.head, handled)) unhandled = unhandled.prepend(l.head);
        return unhandled;
    }
    static int protection(long flags) {
        switch ((short)(flags & AccessFlags)) {
        case PRIVATE: return 3;
        case PROTECTED: return 1;
        default:
        case PUBLIC: return 0;
        case 0: return 2;
        }
    }
    Object cannotOverride(MethodSymbol m, MethodSymbol other) {
        String key;
        if ((other.owner.flags() & INTERFACE) == 0)
            key = "cant.override";
        else if ((m.owner.flags() & INTERFACE) == 0)
            key = "cant.implement";
        else
            key = "clashes.with";
        return diags.fragment(key, m, m.location(), other, other.location());
    }
    Object uncheckedOverrides(MethodSymbol m, MethodSymbol other) {
        String key;
        if ((other.owner.flags() & INTERFACE) == 0)
            key = "unchecked.override";
        else if ((m.owner.flags() & INTERFACE) == 0)
            key = "unchecked.implement";
        else
            key = "unchecked.clash.with";
        return diags.fragment(key, m, m.location(), other, other.location());
    }
    Object varargsOverrides(MethodSymbol m, MethodSymbol other) {
        String key;
        if ((other.owner.flags() & INTERFACE) == 0)
            key = "varargs.override";
        else  if ((m.owner.flags() & INTERFACE) == 0)
            key = "varargs.implement";
        else
            key = "varargs.clash.with";
        return diags.fragment(key, m, m.location(), other, other.location());
    }
    void checkOverride(JCTree tree,
                       MethodSymbol m,
                       MethodSymbol other,
                       ClassSymbol origin) {
        if ((m.flags() & (SYNTHETIC|BRIDGE)) != 0 || (other.flags() & SYNTHETIC) != 0) {
            return;
        }
        if ((m.flags() & STATIC) != 0 &&
                   (other.flags() & STATIC) == 0) {
            log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.static",
                      cannotOverride(m, other));
            return;
        }
        if ((other.flags() & FINAL) != 0 ||
                 (m.flags() & STATIC) == 0 &&
                 (other.flags() & STATIC) != 0) {
            log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.meth",
                      cannotOverride(m, other),
                      asFlagSet(other.flags() & (FINAL | STATIC)));
            return;
        }
        if ((m.owner.flags() & ANNOTATION) != 0) {
            return;
        }
        if ((origin.flags() & INTERFACE) == 0 &&
                 protection(m.flags()) > protection(other.flags())) {
            log.error(TreeInfo.diagnosticPositionFor(m, tree), "override.weaker.access",
                      cannotOverride(m, other),
                      other.flags() == 0 ?
                          Flag.PACKAGE :
                          asFlagSet(other.flags() & AccessFlags));
            return;
        }
        Type mt = types.memberType(origin.type, m);
        Type ot = types.memberType(origin.type, other);
        List<Type> mtvars = mt.getTypeArguments();
        List<Type> otvars = ot.getTypeArguments();
        Type mtres = mt.getReturnType();
        Type otres = types.subst(ot.getReturnType(), otvars, mtvars);
        overrideWarner.clear();
        boolean resultTypesOK =
            types.returnTypeSubstitutable(mt, ot, otres, overrideWarner);
        if (!resultTypesOK) {
            if (!allowCovariantReturns &&
                m.owner != origin &&
                m.owner.isSubClass(other.owner, types)) {
            } else {
                log.error(TreeInfo.diagnosticPositionFor(m, tree),
                          "override.incompatible.ret",
                          cannotOverride(m, other),
                          mtres, otres);
                return;
            }
        } else if (overrideWarner.hasNonSilentLint(LintCategory.UNCHECKED)) {
            warnUnchecked(TreeInfo.diagnosticPositionFor(m, tree),
                    "override.unchecked.ret",
                    uncheckedOverrides(m, other),
                    mtres, otres);
        }
        List<Type> otthrown = types.subst(ot.getThrownTypes(), otvars, mtvars);
        List<Type> unhandledErased = unhandled(mt.getThrownTypes(), types.erasure(otthrown));
        List<Type> unhandledUnerased = unhandled(mt.getThrownTypes(), otthrown);
        if (unhandledErased.nonEmpty()) {
            log.error(TreeInfo.diagnosticPositionFor(m, tree),
                      "override.meth.doesnt.throw",
                      cannotOverride(m, other),
                      unhandledUnerased.head);
            return;
        }
        else if (unhandledUnerased.nonEmpty()) {
            warnUnchecked(TreeInfo.diagnosticPositionFor(m, tree),
                          "override.unchecked.thrown",
                         cannotOverride(m, other),
                         unhandledUnerased.head);
            return;
        }
        if ((((m.flags() ^ other.flags()) & Flags.VARARGS) != 0)
            && lint.isEnabled(LintCategory.OVERRIDES)) {
            log.warning(TreeInfo.diagnosticPositionFor(m, tree),
                        ((m.flags() & Flags.VARARGS) != 0)
                        ? "override.varargs.missing"
                        : "override.varargs.extra",
                        varargsOverrides(m, other));
        }
        if ((other.flags() & BRIDGE) != 0) {
            log.warning(TreeInfo.diagnosticPositionFor(m, tree), "override.bridge",
                        uncheckedOverrides(m, other));
        }
        if (!isDeprecatedOverrideIgnorable(other, origin)) {
            checkDeprecated(TreeInfo.diagnosticPositionFor(m, tree), m, other);
        }
    }
        private boolean isDeprecatedOverrideIgnorable(MethodSymbol m, ClassSymbol origin) {
            ClassSymbol mc = m.enclClass();
            Type st = types.supertype(origin.type);
            if (st.tag != CLASS)
                return true;
            MethodSymbol stimpl = m.implementation((ClassSymbol)st.tsym, types, false);
            if (mc != null && ((mc.flags() & INTERFACE) != 0)) {
                List<Type> intfs = types.interfaces(origin.type);
                return (intfs.contains(mc.type) ? false : (stimpl != null));
            }
            else
                return (stimpl != m);
        }
    Warner overrideWarner = new Warner();
    public void checkCompatibleConcretes(DiagnosticPosition pos, Type site) {
        Type sup = types.supertype(site);
        if (sup.tag != CLASS) return;
        for (Type t1 = sup;
             t1.tsym.type.isParameterized();
             t1 = types.supertype(t1)) {
            for (Scope.Entry e1 = t1.tsym.members().elems;
                 e1 != null;
                 e1 = e1.sibling) {
                Symbol s1 = e1.sym;
                if (s1.kind != MTH ||
                    (s1.flags() & (STATIC|SYNTHETIC|BRIDGE)) != 0 ||
                    !s1.isInheritedIn(site.tsym, types) ||
                    ((MethodSymbol)s1).implementation(site.tsym,
                                                      types,
                                                      true) != s1)
                    continue;
                Type st1 = types.memberType(t1, s1);
                int s1ArgsLength = st1.getParameterTypes().length();
                if (st1 == s1.type) continue;
                for (Type t2 = sup;
                     t2.tag == CLASS;
                     t2 = types.supertype(t2)) {
                    for (Scope.Entry e2 = t2.tsym.members().lookup(s1.name);
                         e2.scope != null;
                         e2 = e2.next()) {
                        Symbol s2 = e2.sym;
                        if (s2 == s1 ||
                            s2.kind != MTH ||
                            (s2.flags() & (STATIC|SYNTHETIC|BRIDGE)) != 0 ||
                            s2.type.getParameterTypes().length() != s1ArgsLength ||
                            !s2.isInheritedIn(site.tsym, types) ||
                            ((MethodSymbol)s2).implementation(site.tsym,
                                                              types,
                                                              true) != s2)
                            continue;
                        Type st2 = types.memberType(t2, s2);
                        if (types.overrideEquivalent(st1, st2))
                            log.error(pos, "concrete.inheritance.conflict",
                                      s1, t1, s2, t2, sup);
                    }
                }
            }
        }
    }
    public boolean checkCompatibleAbstracts(DiagnosticPosition pos,
                                            Type t1,
                                            Type t2) {
        return checkCompatibleAbstracts(pos, t1, t2,
                                        types.makeCompoundType(t1, t2));
    }
    public boolean checkCompatibleAbstracts(DiagnosticPosition pos,
                                            Type t1,
                                            Type t2,
                                            Type site) {
        return firstIncompatibility(pos, t1, t2, site) == null;
    }
    private Symbol firstIncompatibility(DiagnosticPosition pos, Type t1, Type t2, Type site) {
        Map<TypeSymbol,Type> interfaces1 = new HashMap<TypeSymbol,Type>();
        closure(t1, interfaces1);
        Map<TypeSymbol,Type> interfaces2;
        if (t1 == t2)
            interfaces2 = interfaces1;
        else
            closure(t2, interfaces1, interfaces2 = new HashMap<TypeSymbol,Type>());
        for (Type t3 : interfaces1.values()) {
            for (Type t4 : interfaces2.values()) {
                Symbol s = firstDirectIncompatibility(pos, t3, t4, site);
                if (s != null) return s;
            }
        }
        return null;
    }
    private void closure(Type t, Map<TypeSymbol,Type> typeMap) {
        if (t.tag != CLASS) return;
        if (typeMap.put(t.tsym, t) == null) {
            closure(types.supertype(t), typeMap);
            for (Type i : types.interfaces(t))
                closure(i, typeMap);
        }
    }
    private void closure(Type t, Map<TypeSymbol,Type> typesSkip, Map<TypeSymbol,Type> typeMap) {
        if (t.tag != CLASS) return;
        if (typesSkip.get(t.tsym) != null) return;
        if (typeMap.put(t.tsym, t) == null) {
            closure(types.supertype(t), typesSkip, typeMap);
            for (Type i : types.interfaces(t))
                closure(i, typesSkip, typeMap);
        }
    }
    private Symbol firstDirectIncompatibility(DiagnosticPosition pos, Type t1, Type t2, Type site) {
        for (Scope.Entry e1 = t1.tsym.members().elems; e1 != null; e1 = e1.sibling) {
            Symbol s1 = e1.sym;
            Type st1 = null;
            if (s1.kind != MTH || !s1.isInheritedIn(site.tsym, types)) continue;
            Symbol impl = ((MethodSymbol)s1).implementation(site.tsym, types, false);
            if (impl != null && (impl.flags() & ABSTRACT) == 0) continue;
            for (Scope.Entry e2 = t2.tsym.members().lookup(s1.name); e2.scope != null; e2 = e2.next()) {
                Symbol s2 = e2.sym;
                if (s1 == s2) continue;
                if (s2.kind != MTH || !s2.isInheritedIn(site.tsym, types)) continue;
                if (st1 == null) st1 = types.memberType(t1, s1);
                Type st2 = types.memberType(t2, s2);
                if (types.overrideEquivalent(st1, st2)) {
                    List<Type> tvars1 = st1.getTypeArguments();
                    List<Type> tvars2 = st2.getTypeArguments();
                    Type rt1 = st1.getReturnType();
                    Type rt2 = types.subst(st2.getReturnType(), tvars2, tvars1);
                    boolean compat =
                        types.isSameType(rt1, rt2) ||
                        rt1.tag >= CLASS && rt2.tag >= CLASS &&
                        (types.covariantReturnType(rt1, rt2, Warner.noWarnings) ||
                         types.covariantReturnType(rt2, rt1, Warner.noWarnings)) ||
                         checkCommonOverriderIn(s1,s2,site);
                    if (!compat) {
                        log.error(pos, "types.incompatible.diff.ret",
                            t1, t2, s2.name +
                            "(" + types.memberType(t2, s2).getParameterTypes() + ")");
                        return s2;
                    }
                } else if (checkNameClash((ClassSymbol)site.tsym, s1, s2) &&
                        !checkCommonOverriderIn(s1, s2, site)) {
                    log.error(pos,
                            "name.clash.same.erasure.no.override",
                            s1, s1.location(),
                            s2, s2.location());
                    return s2;
                }
            }
        }
        return null;
    }
    boolean checkCommonOverriderIn(Symbol s1, Symbol s2, Type site) {
        Map<TypeSymbol,Type> supertypes = new HashMap<TypeSymbol,Type>();
        Type st1 = types.memberType(site, s1);
        Type st2 = types.memberType(site, s2);
        closure(site, supertypes);
        for (Type t : supertypes.values()) {
            for (Scope.Entry e = t.tsym.members().lookup(s1.name); e.scope != null; e = e.next()) {
                Symbol s3 = e.sym;
                if (s3 == s1 || s3 == s2 || s3.kind != MTH || (s3.flags() & (BRIDGE|SYNTHETIC)) != 0) continue;
                Type st3 = types.memberType(site,s3);
                if (types.overrideEquivalent(st3, st1) && types.overrideEquivalent(st3, st2)) {
                    if (s3.owner == site.tsym) {
                        return true;
                    }
                    List<Type> tvars1 = st1.getTypeArguments();
                    List<Type> tvars2 = st2.getTypeArguments();
                    List<Type> tvars3 = st3.getTypeArguments();
                    Type rt1 = st1.getReturnType();
                    Type rt2 = st2.getReturnType();
                    Type rt13 = types.subst(st3.getReturnType(), tvars3, tvars1);
                    Type rt23 = types.subst(st3.getReturnType(), tvars3, tvars2);
                    boolean compat =
                        rt13.tag >= CLASS && rt23.tag >= CLASS &&
                        (types.covariantReturnType(rt13, rt1, Warner.noWarnings) &&
                         types.covariantReturnType(rt23, rt2, Warner.noWarnings));
                    if (compat)
                        return true;
                }
            }
        }
        return false;
    }
    void checkOverride(JCTree tree, MethodSymbol m) {
        ClassSymbol origin = (ClassSymbol)m.owner;
        if ((origin.flags() & ENUM) != 0 && names.finalize.equals(m.name))
            if (m.overrides(syms.enumFinalFinalize, origin, types, false)) {
                log.error(tree.pos(), "enum.no.finalize");
                return;
            }
        for (Type t = origin.type; t.tag == CLASS;
             t = types.supertype(t)) {
            if (t != origin.type) {
                checkOverride(tree, t, origin, m);
            }
            for (Type t2 : types.interfaces(t)) {
                checkOverride(tree, t2, origin, m);
            }
        }
    }
    void checkOverride(JCTree tree, Type site, ClassSymbol origin, MethodSymbol m) {
        TypeSymbol c = site.tsym;
        Scope.Entry e = c.members().lookup(m.name);
        while (e.scope != null) {
            if (m.overrides(e.sym, origin, types, false)) {
                if ((e.sym.flags() & ABSTRACT) == 0) {
                    checkOverride(tree, m, (MethodSymbol)e.sym, origin);
                }
            }
            e = e.next();
        }
    }
    private boolean checkNameClash(ClassSymbol origin, Symbol s1, Symbol s2) {
        ClashFilter cf = new ClashFilter(origin.type);
        return (cf.accepts(s1) &&
                cf.accepts(s2) &&
                types.hasSameArgs(s1.erasure(types), s2.erasure(types)));
    }
    void checkAllDefined(DiagnosticPosition pos, ClassSymbol c) {
        try {
            MethodSymbol undef = firstUndef(c, c);
            if (undef != null) {
                if ((c.flags() & ENUM) != 0 &&
                    types.supertype(c.type).tsym == syms.enumSym &&
                    (c.flags() & FINAL) == 0) {
                    c.flags_field |= ABSTRACT;
                } else {
                    MethodSymbol undef1 =
                        new MethodSymbol(undef.flags(), undef.name,
                                         types.memberType(c.type, undef), undef.owner);
                    log.error(pos, "does.not.override.abstract",
                              c, undef1, undef1.location());
                }
            }
        } catch (CompletionFailure ex) {
            completionError(pos, ex);
        }
    }
        private MethodSymbol firstUndef(ClassSymbol impl, ClassSymbol c) {
            MethodSymbol undef = null;
            if (c == impl || (c.flags() & (ABSTRACT | INTERFACE)) != 0) {
                Scope s = c.members();
                for (Scope.Entry e = s.elems;
                     undef == null && e != null;
                     e = e.sibling) {
                    if (e.sym.kind == MTH &&
                        (e.sym.flags() & (ABSTRACT|IPROXY)) == ABSTRACT) {
                        MethodSymbol absmeth = (MethodSymbol)e.sym;
                        MethodSymbol implmeth = absmeth.implementation(impl, types, true);
                        if (implmeth == null || implmeth == absmeth)
                            undef = absmeth;
                    }
                }
                if (undef == null) {
                    Type st = types.supertype(c.type);
                    if (st.tag == CLASS)
                        undef = firstUndef(impl, (ClassSymbol)st.tsym);
                }
                for (List<Type> l = types.interfaces(c.type);
                     undef == null && l.nonEmpty();
                     l = l.tail) {
                    undef = firstUndef(impl, (ClassSymbol)l.head.tsym);
                }
            }
            return undef;
        }
    void checkNonCyclicDecl(JCClassDecl tree) {
        CycleChecker cc = new CycleChecker();
        cc.scan(tree);
        if (!cc.errorFound && !cc.partialCheck) {
            tree.sym.flags_field |= ACYCLIC;
        }
    }
    class CycleChecker extends TreeScanner {
        List<Symbol> seenClasses = List.nil();
        boolean errorFound = false;
        boolean partialCheck = false;
        private void checkSymbol(DiagnosticPosition pos, Symbol sym) {
            if (sym != null && sym.kind == TYP) {
                Env<AttrContext> classEnv = enter.getEnv((TypeSymbol)sym);
                if (classEnv != null) {
                    DiagnosticSource prevSource = log.currentSource();
                    try {
                        log.useSource(classEnv.toplevel.sourcefile);
                        scan(classEnv.tree);
                    }
                    finally {
                        log.useSource(prevSource.getFile());
                    }
                } else if (sym.kind == TYP) {
                    checkClass(pos, sym, List.<JCTree>nil());
                }
            } else {
                partialCheck = true;
            }
        }
        @Override
        public void visitSelect(JCFieldAccess tree) {
            super.visitSelect(tree);
            checkSymbol(tree.pos(), tree.sym);
        }
        @Override
        public void visitIdent(JCIdent tree) {
            checkSymbol(tree.pos(), tree.sym);
        }
        @Override
        public void visitTypeApply(JCTypeApply tree) {
            scan(tree.clazz);
        }
        @Override
        public void visitTypeArray(JCArrayTypeTree tree) {
            scan(tree.elemtype);
        }
        @Override
        public void visitClassDef(JCClassDecl tree) {
            List<JCTree> supertypes = List.nil();
            if (tree.getExtendsClause() != null) {
                supertypes = supertypes.prepend(tree.getExtendsClause());
            }
            if (tree.getImplementsClause() != null) {
                for (JCTree intf : tree.getImplementsClause()) {
                    supertypes = supertypes.prepend(intf);
                }
            }
            checkClass(tree.pos(), tree.sym, supertypes);
        }
        void checkClass(DiagnosticPosition pos, Symbol c, List<JCTree> supertypes) {
            if ((c.flags_field & ACYCLIC) != 0)
                return;
            if (seenClasses.contains(c)) {
                errorFound = true;
                noteCyclic(pos, (ClassSymbol)c);
            } else if (!c.type.isErroneous()) {
                try {
                    seenClasses = seenClasses.prepend(c);
                    if (c.type.tag == CLASS) {
                        if (supertypes.nonEmpty()) {
                            scan(supertypes);
                        }
                        else {
                            ClassType ct = (ClassType)c.type;
                            if (ct.supertype_field == null ||
                                    ct.interfaces_field == null) {
                                partialCheck = true;
                                return;
                            }
                            checkSymbol(pos, ct.supertype_field.tsym);
                            for (Type intf : ct.interfaces_field) {
                                checkSymbol(pos, intf.tsym);
                            }
                        }
                        if (c.owner.kind == TYP) {
                            checkSymbol(pos, c.owner);
                        }
                    }
                } finally {
                    seenClasses = seenClasses.tail;
                }
            }
        }
    }
    void checkNonCyclic(DiagnosticPosition pos, Type t) {
        checkNonCyclicInternal(pos, t);
    }
    void checkNonCyclic(DiagnosticPosition pos, TypeVar t) {
        checkNonCyclic1(pos, t, List.<TypeVar>nil());
    }
    private void checkNonCyclic1(DiagnosticPosition pos, Type t, List<TypeVar> seen) {
        final TypeVar tv;
        if  (t.tag == TYPEVAR && (t.tsym.flags() & UNATTRIBUTED) != 0)
            return;
        if (seen.contains(t)) {
            tv = (TypeVar)t;
            tv.bound = types.createErrorType(t);
            log.error(pos, "cyclic.inheritance", t);
        } else if (t.tag == TYPEVAR) {
            tv = (TypeVar)t;
            seen = seen.prepend(tv);
            for (Type b : types.getBounds(tv))
                checkNonCyclic1(pos, b, seen);
        }
    }
    private boolean checkNonCyclicInternal(DiagnosticPosition pos, Type t) {
        boolean complete = true; 
        Symbol c = t.tsym;
        if ((c.flags_field & ACYCLIC) != 0) return true;
        if ((c.flags_field & LOCKED) != 0) {
            noteCyclic(pos, (ClassSymbol)c);
        } else if (!c.type.isErroneous()) {
            try {
                c.flags_field |= LOCKED;
                if (c.type.tag == CLASS) {
                    ClassType clazz = (ClassType)c.type;
                    if (clazz.interfaces_field != null)
                        for (List<Type> l=clazz.interfaces_field; l.nonEmpty(); l=l.tail)
                            complete &= checkNonCyclicInternal(pos, l.head);
                    if (clazz.supertype_field != null) {
                        Type st = clazz.supertype_field;
                        if (st != null && st.tag == CLASS)
                            complete &= checkNonCyclicInternal(pos, st);
                    }
                    if (c.owner.kind == TYP)
                        complete &= checkNonCyclicInternal(pos, c.owner.type);
                }
            } finally {
                c.flags_field &= ~LOCKED;
            }
        }
        if (complete)
            complete = ((c.flags_field & UNATTRIBUTED) == 0) && c.completer == null;
        if (complete) c.flags_field |= ACYCLIC;
        return complete;
    }
    private void noteCyclic(DiagnosticPosition pos, ClassSymbol c) {
        log.error(pos, "cyclic.inheritance", c);
        for (List<Type> l=types.interfaces(c.type); l.nonEmpty(); l=l.tail)
            l.head = types.createErrorType((ClassSymbol)l.head.tsym, Type.noType);
        Type st = types.supertype(c.type);
        if (st.tag == CLASS)
            ((ClassType)c.type).supertype_field = types.createErrorType((ClassSymbol)st.tsym, Type.noType);
        c.type = types.createErrorType(c, c.type);
        c.flags_field |= ACYCLIC;
    }
    void checkImplementations(JCClassDecl tree) {
        checkImplementations(tree, tree.sym);
    }
        void checkImplementations(JCClassDecl tree, ClassSymbol ic) {
            ClassSymbol origin = tree.sym;
            for (List<Type> l = types.closure(ic.type); l.nonEmpty(); l = l.tail) {
                ClassSymbol lc = (ClassSymbol)l.head.tsym;
                if ((allowGenerics || origin != lc) && (lc.flags() & ABSTRACT) != 0) {
                    for (Scope.Entry e=lc.members().elems; e != null; e=e.sibling) {
                        if (e.sym.kind == MTH &&
                            (e.sym.flags() & (STATIC|ABSTRACT)) == ABSTRACT) {
                            MethodSymbol absmeth = (MethodSymbol)e.sym;
                            MethodSymbol implmeth = absmeth.implementation(origin, types, false);
                            if (implmeth != null && implmeth != absmeth &&
                                (implmeth.owner.flags() & INTERFACE) ==
                                (origin.flags() & INTERFACE)) {
                                checkOverride(tree, implmeth, absmeth, origin);
                            }
                        }
                    }
                }
            }
        }
    void checkCompatibleSupertypes(DiagnosticPosition pos, Type c) {
        List<Type> supertypes = types.interfaces(c);
        Type supertype = types.supertype(c);
        if (supertype.tag == CLASS &&
            (supertype.tsym.flags() & ABSTRACT) != 0)
            supertypes = supertypes.prepend(supertype);
        for (List<Type> l = supertypes; l.nonEmpty(); l = l.tail) {
            if (allowGenerics && !l.head.getTypeArguments().isEmpty() &&
                !checkCompatibleAbstracts(pos, l.head, l.head, c))
                return;
            for (List<Type> m = supertypes; m != l; m = m.tail)
                if (!checkCompatibleAbstracts(pos, l.head, m.head, c))
                    return;
        }
        checkCompatibleConcretes(pos, c);
    }
    void checkConflicts(DiagnosticPosition pos, Symbol sym, TypeSymbol c) {
        for (Type ct = c.type; ct != Type.noType ; ct = types.supertype(ct)) {
            for (Scope.Entry e = ct.tsym.members().lookup(sym.name); e.scope == ct.tsym.members(); e = e.next()) {
                if (sym.kind == e.sym.kind &&
                    types.isSameType(types.erasure(sym.type), types.erasure(e.sym.type)) &&
                    sym != e.sym &&
                    (sym.flags() & Flags.SYNTHETIC) != (e.sym.flags() & Flags.SYNTHETIC) &&
                    (sym.flags() & IPROXY) == 0 && (e.sym.flags() & IPROXY) == 0 &&
                    (sym.flags() & BRIDGE) == 0 && (e.sym.flags() & BRIDGE) == 0) {
                    syntheticError(pos, (e.sym.flags() & SYNTHETIC) == 0 ? e.sym : sym);
                    return;
                }
            }
        }
    }
    void checkOverrideClashes(DiagnosticPosition pos, Type site, MethodSymbol sym) {
         ClashFilter cf = new ClashFilter(site);
         for (Symbol s1 : types.membersClosure(site, false).getElementsByName(sym.name, cf)) {
            for (Symbol s2 : types.membersClosure(site, false).getElementsByName(sym.name, cf)) {
                if (s1 == s2 || !sym.overrides(s2, site.tsym, types, false)) continue;
                if (!types.isSubSignature(sym.type, types.memberType(site, s1), false) &&
                        types.hasSameArgs(s1.erasure(types), s2.erasure(types))) {
                    sym.flags_field |= CLASH;
                    String key = s2 == sym ?
                            "name.clash.same.erasure.no.override" :
                            "name.clash.same.erasure.no.override.1";
                    log.error(pos,
                            key,
                            sym, sym.location(),
                            s1, s1.location(),
                            s2, s2.location());
                    return;
                }
            }
        }
    }
    void checkHideClashes(DiagnosticPosition pos, Type site, MethodSymbol sym) {
        ClashFilter cf = new ClashFilter(site);
        for (Symbol s : types.membersClosure(site, true).getElementsByName(sym.name, cf)) {
            if (!types.isSubSignature(sym.type, types.memberType(site, s), false) &&
                    types.hasSameArgs(s.erasure(types), sym.erasure(types))) {
                log.error(pos,
                        "name.clash.same.erasure.no.hide",
                        sym, sym.location(),
                        s, s.location());
                return;
             }
         }
     }
     private class ClashFilter implements Filter<Symbol> {
         Type site;
         ClashFilter(Type site) {
             this.site = site;
         }
         boolean shouldSkip(Symbol s) {
             return (s.flags() & CLASH) != 0 &&
                s.owner == site.tsym;
         }
         public boolean accepts(Symbol s) {
             return s.kind == MTH &&
                     (s.flags() & SYNTHETIC) == 0 &&
                     !shouldSkip(s) &&
                     s.isInheritedIn(site.tsym, types) &&
                     !s.isConstructor();
         }
     }
    private void syntheticError(DiagnosticPosition pos, Symbol sym) {
        if (!sym.type.isErroneous()) {
            if (warnOnSyntheticConflicts) {
                log.warning(pos, "synthetic.name.conflict", sym, sym.location());
            }
            else {
                log.error(pos, "synthetic.name.conflict", sym, sym.location());
            }
        }
    }
    void checkClassBounds(DiagnosticPosition pos, Type type) {
        checkClassBounds(pos, new HashMap<TypeSymbol,Type>(), type);
    }
        void checkClassBounds(DiagnosticPosition pos,
                              Map<TypeSymbol,Type> seensofar,
                              Type type) {
            if (type.isErroneous()) return;
            for (List<Type> l = types.interfaces(type); l.nonEmpty(); l = l.tail) {
                Type it = l.head;
                Type oldit = seensofar.put(it.tsym, it);
                if (oldit != null) {
                    List<Type> oldparams = oldit.allparams();
                    List<Type> newparams = it.allparams();
                    if (!types.containsTypeEquivalent(oldparams, newparams))
                        log.error(pos, "cant.inherit.diff.arg",
                                  it.tsym, Type.toString(oldparams),
                                  Type.toString(newparams));
                }
                checkClassBounds(pos, seensofar, it);
            }
            Type st = types.supertype(type);
            if (st != null) checkClassBounds(pos, seensofar, st);
        }
    void checkNotRepeated(DiagnosticPosition pos, Type it, Set<Type> its) {
        if (its.contains(it))
            log.error(pos, "repeated.interface");
        else {
            its.add(it);
        }
    }
    void validateAnnotationTree(JCTree tree) {
        class AnnotationValidator extends TreeScanner {
            @Override
            public void visitAnnotation(JCAnnotation tree) {
                if (!tree.type.isErroneous()) {
                    super.visitAnnotation(tree);
                    validateAnnotation(tree);
                }
            }
        }
        tree.accept(new AnnotationValidator());
    }
    void validateAnnotationType(JCTree restype) {
        if (restype != null) {
            validateAnnotationType(restype.pos(), restype.type);
        }
    }
    void validateAnnotationType(DiagnosticPosition pos, Type type) {
        if (type.isPrimitive()) return;
        if (types.isSameType(type, syms.stringType)) return;
        if ((type.tsym.flags() & Flags.ENUM) != 0) return;
        if ((type.tsym.flags() & Flags.ANNOTATION) != 0) return;
        if (types.lowerBound(type).tsym == syms.classType.tsym) return;
        if (types.isArray(type) && !types.isArray(types.elemtype(type))) {
            validateAnnotationType(pos, types.elemtype(type));
            return;
        }
        log.error(pos, "invalid.annotation.member.type");
    }
    void validateAnnotationMethod(DiagnosticPosition pos, MethodSymbol m) {
        for (Type sup = syms.annotationType; sup.tag == CLASS; sup = types.supertype(sup)) {
            Scope s = sup.tsym.members();
            for (Scope.Entry e = s.lookup(m.name); e.scope != null; e = e.next()) {
                if (e.sym.kind == MTH &&
                    (e.sym.flags() & (PUBLIC | PROTECTED)) != 0 &&
                    types.overrideEquivalent(m.type, e.sym.type))
                    log.error(pos, "intf.annotation.member.clash", e.sym, sup);
            }
        }
    }
    public void validateAnnotations(List<JCAnnotation> annotations, Symbol s) {
        if (skipAnnotations) return;
        for (JCAnnotation a : annotations)
            validateAnnotation(a, s);
    }
    public void validateAnnotation(JCAnnotation a, Symbol s) {
        validateAnnotationTree(a);
        if (!annotationApplicable(a, s))
            log.error(a.pos(), "annotation.type.not.applicable");
        if (a.annotationType.type.tsym == syms.overrideType.tsym) {
            if (!isOverrider(s))
                log.error(a.pos(), "method.does.not.override.superclass");
        }
    }
    boolean isOverrider(Symbol s) {
        if (s.kind != MTH || s.isStatic())
            return false;
        MethodSymbol m = (MethodSymbol)s;
        TypeSymbol owner = (TypeSymbol)m.owner;
        for (Type sup : types.closure(owner.type)) {
            if (sup == owner.type)
                continue; 
            Scope scope = sup.tsym.members();
            for (Scope.Entry e = scope.lookup(m.name); e.scope != null; e = e.next()) {
                if (!e.sym.isStatic() && m.overrides(e.sym, owner, types, true))
                    return true;
            }
        }
        return false;
    }
    boolean annotationApplicable(JCAnnotation a, Symbol s) {
        Attribute.Compound atTarget =
            a.annotationType.type.tsym.attribute(syms.annotationTargetType.tsym);
        if (atTarget == null) return true;
        Attribute atValue = atTarget.member(names.value);
        if (!(atValue instanceof Attribute.Array)) return true; 
        Attribute.Array arr = (Attribute.Array) atValue;
        for (Attribute app : arr.values) {
            if (!(app instanceof Attribute.Enum)) return true; 
            Attribute.Enum e = (Attribute.Enum) app;
            if (e.value.name == names.TYPE)
                { if (s.kind == TYP) return true; }
            else if (e.value.name == names.FIELD)
                { if (s.kind == VAR && s.owner.kind != MTH) return true; }
            else if (e.value.name == names.METHOD)
                { if (s.kind == MTH && !s.isConstructor()) return true; }
            else if (e.value.name == names.PARAMETER)
                { if (s.kind == VAR &&
                      s.owner.kind == MTH &&
                      (s.flags() & PARAMETER) != 0)
                    return true;
                }
            else if (e.value.name == names.CONSTRUCTOR)
                { if (s.kind == MTH && s.isConstructor()) return true; }
            else if (e.value.name == names.LOCAL_VARIABLE)
                { if (s.kind == VAR && s.owner.kind == MTH &&
                      (s.flags() & PARAMETER) == 0)
                    return true;
                }
            else if (e.value.name == names.ANNOTATION_TYPE)
                { if (s.kind == TYP && (s.flags() & ANNOTATION) != 0)
                    return true;
                }
            else if (e.value.name == names.PACKAGE)
                { if (s.kind == PCK) return true; }
            else if (e.value.name == names.TYPE_USE)
                { if (s.kind == TYP ||
                      s.kind == VAR ||
                      (s.kind == MTH && !s.isConstructor() &&
                       s.type.getReturnType().tag != VOID))
                    return true;
                }
            else
                return true; 
        }
        return false;
    }
    public void validateAnnotation(JCAnnotation a) {
        Set<MethodSymbol> members = new TreeSet<MethodSymbol>(new Comparator<Symbol>() {
            public int compare(Symbol t, Symbol t1) {
                return t.name.compareTo(t1.name);
            }
        });
        for (Scope.Entry e = a.annotationType.type.tsym.members().elems;
             e != null;
             e = e.sibling)
            if (e.sym.kind == MTH)
                members.add((MethodSymbol) e.sym);
        for (JCTree arg : a.args) {
            if (arg.getTag() != JCTree.ASSIGN) continue; 
            JCAssign assign = (JCAssign) arg;
            Symbol m = TreeInfo.symbol(assign.lhs);
            if (m == null || m.type.isErroneous()) continue;
            if (!members.remove(m))
                log.error(assign.lhs.pos(), "duplicate.annotation.member.value",
                          m.name, a.type);
        }
        ListBuffer<Name> missingDefaults = ListBuffer.lb();
        for (MethodSymbol m : members) {
            if (m.defaultValue == null && !m.type.isErroneous()) {
                missingDefaults.append(m.name);
            }
        }
        if (missingDefaults.nonEmpty()) {
            String key = (missingDefaults.size() > 1)
                    ? "annotation.missing.default.value.1"
                    : "annotation.missing.default.value";
            log.error(a.pos(), key, a.type, missingDefaults);
        }
        if (a.annotationType.type.tsym != syms.annotationTargetType.tsym ||
            a.args.tail == null)
            return;
        if (a.args.head.getTag() != JCTree.ASSIGN) return; 
        JCAssign assign = (JCAssign) a.args.head;
        Symbol m = TreeInfo.symbol(assign.lhs);
        if (m.name != names.value) return;
        JCTree rhs = assign.rhs;
        if (rhs.getTag() != JCTree.NEWARRAY) return;
        JCNewArray na = (JCNewArray) rhs;
        Set<Symbol> targets = new HashSet<Symbol>();
        for (JCTree elem : na.elems) {
            if (!targets.add(TreeInfo.symbol(elem))) {
                log.error(elem.pos(), "repeated.annotation.target");
            }
        }
    }
    void checkDeprecatedAnnotation(DiagnosticPosition pos, Symbol s) {
        if (allowAnnotations &&
            lint.isEnabled(LintCategory.DEP_ANN) &&
            (s.flags() & DEPRECATED) != 0 &&
            !syms.deprecatedType.isErroneous() &&
            s.attribute(syms.deprecatedType.tsym) == null) {
            log.warning(LintCategory.DEP_ANN,
                    pos, "missing.deprecated.annotation");
        }
    }
    void checkDeprecated(final DiagnosticPosition pos, final Symbol other, final Symbol s) {
        if ((s.flags() & DEPRECATED) != 0 &&
                (other.flags() & DEPRECATED) == 0 &&
                s.outermostClass() != other.outermostClass()) {
            deferredLintHandler.report(new DeferredLintHandler.LintLogger() {
                @Override
                public void report() {
                    warnDeprecated(pos, s);
                }
            });
        };
    }
    void checkSunAPI(final DiagnosticPosition pos, final Symbol s) {
        if ((s.flags() & PROPRIETARY) != 0) {
            deferredLintHandler.report(new DeferredLintHandler.LintLogger() {
                public void report() {
                    if (enableSunApiLintControl)
                      warnSunApi(pos, "sun.proprietary", s);
                    else
                      log.strictWarning(pos, "sun.proprietary", s);
                }
            });
        }
    }
    void checkNonCyclicElements(JCClassDecl tree) {
        if ((tree.sym.flags_field & ANNOTATION) == 0) return;
        Assert.check((tree.sym.flags_field & LOCKED) == 0);
        try {
            tree.sym.flags_field |= LOCKED;
            for (JCTree def : tree.defs) {
                if (def.getTag() != JCTree.METHODDEF) continue;
                JCMethodDecl meth = (JCMethodDecl)def;
                checkAnnotationResType(meth.pos(), meth.restype.type);
            }
        } finally {
            tree.sym.flags_field &= ~LOCKED;
            tree.sym.flags_field |= ACYCLIC_ANN;
        }
    }
    void checkNonCyclicElementsInternal(DiagnosticPosition pos, TypeSymbol tsym) {
        if ((tsym.flags_field & ACYCLIC_ANN) != 0)
            return;
        if ((tsym.flags_field & LOCKED) != 0) {
            log.error(pos, "cyclic.annotation.element");
            return;
        }
        try {
            tsym.flags_field |= LOCKED;
            for (Scope.Entry e = tsym.members().elems; e != null; e = e.sibling) {
                Symbol s = e.sym;
                if (s.kind != Kinds.MTH)
                    continue;
                checkAnnotationResType(pos, ((MethodSymbol)s).type.getReturnType());
            }
        } finally {
            tsym.flags_field &= ~LOCKED;
            tsym.flags_field |= ACYCLIC_ANN;
        }
    }
    void checkAnnotationResType(DiagnosticPosition pos, Type type) {
        switch (type.tag) {
        case TypeTags.CLASS:
            if ((type.tsym.flags() & ANNOTATION) != 0)
                checkNonCyclicElementsInternal(pos, type.tsym);
            break;
        case TypeTags.ARRAY:
            checkAnnotationResType(pos, types.elemtype(type));
            break;
        default:
            break; 
        }
    }
    void checkCyclicConstructors(JCClassDecl tree) {
        Map<Symbol,Symbol> callMap = new HashMap<Symbol, Symbol>();
        for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
            JCMethodInvocation app = TreeInfo.firstConstructorCall(l.head);
            if (app == null) continue;
            JCMethodDecl meth = (JCMethodDecl) l.head;
            if (TreeInfo.name(app.meth) == names._this) {
                callMap.put(meth.sym, TreeInfo.symbol(app.meth));
            } else {
                meth.sym.flags_field |= ACYCLIC;
            }
        }
        Symbol[] ctors = new Symbol[0];
        ctors = callMap.keySet().toArray(ctors);
        for (Symbol caller : ctors) {
            checkCyclicConstructor(tree, caller, callMap);
        }
    }
    private void checkCyclicConstructor(JCClassDecl tree, Symbol ctor,
                                        Map<Symbol,Symbol> callMap) {
        if (ctor != null && (ctor.flags_field & ACYCLIC) == 0) {
            if ((ctor.flags_field & LOCKED) != 0) {
                log.error(TreeInfo.diagnosticPositionFor(ctor, tree),
                          "recursive.ctor.invocation");
            } else {
                ctor.flags_field |= LOCKED;
                checkCyclicConstructor(tree, callMap.remove(ctor), callMap);
                ctor.flags_field &= ~LOCKED;
            }
            ctor.flags_field |= ACYCLIC;
        }
    }
    int checkOperator(DiagnosticPosition pos,
                       OperatorSymbol operator,
                       int tag,
                       Type left,
                       Type right) {
        if (operator.opcode == ByteCodes.error) {
            log.error(pos,
                      "operator.cant.be.applied.1",
                      treeinfo.operatorName(tag),
                      left, right);
        }
        return operator.opcode;
    }
    void checkDivZero(DiagnosticPosition pos, Symbol operator, Type operand) {
        if (operand.constValue() != null
            && lint.isEnabled(LintCategory.DIVZERO)
            && operand.tag <= LONG
            && ((Number) (operand.constValue())).longValue() == 0) {
            int opc = ((OperatorSymbol)operator).opcode;
            if (opc == ByteCodes.idiv || opc == ByteCodes.imod
                || opc == ByteCodes.ldiv || opc == ByteCodes.lmod) {
                log.warning(LintCategory.DIVZERO, pos, "div.zero");
            }
        }
    }
    void checkEmptyIf(JCIf tree) {
        if (tree.thenpart.getTag() == JCTree.SKIP && tree.elsepart == null && lint.isEnabled(LintCategory.EMPTY))
            log.warning(LintCategory.EMPTY, tree.thenpart.pos(), "empty.if");
    }
    boolean checkUnique(DiagnosticPosition pos, Symbol sym, Scope s) {
        if (sym.type.isErroneous())
            return true;
        if (sym.owner.name == names.any) return false;
        for (Scope.Entry e = s.lookup(sym.name); e.scope == s; e = e.next()) {
            if (sym != e.sym &&
                    (e.sym.flags() & CLASH) == 0 &&
                    sym.kind == e.sym.kind &&
                    sym.name != names.error &&
                    (sym.kind != MTH || types.hasSameArgs(types.erasure(sym.type), types.erasure(e.sym.type)))) {
                if ((sym.flags() & VARARGS) != (e.sym.flags() & VARARGS)) {
                    varargsDuplicateError(pos, sym, e.sym);
                    return true;
                } else if (sym.kind == MTH && !types.hasSameArgs(sym.type, e.sym.type, false)) {
                    duplicateErasureError(pos, sym, e.sym);
                    sym.flags_field |= CLASH;
                    return true;
                } else {
                    duplicateError(pos, e.sym);
                    return false;
                }
            }
        }
        return true;
    }
    void duplicateErasureError(DiagnosticPosition pos, Symbol sym1, Symbol sym2) {
        if (!sym1.type.isErroneous() && !sym2.type.isErroneous()) {
            log.error(pos, "name.clash.same.erasure", sym1, sym2);
        }
    }
    boolean checkUniqueImport(DiagnosticPosition pos, Symbol sym, Scope s) {
        return checkUniqueImport(pos, sym, s, false);
    }
    boolean checkUniqueStaticImport(DiagnosticPosition pos, Symbol sym, Scope s) {
        return checkUniqueImport(pos, sym, s, true);
    }
    private boolean checkUniqueImport(DiagnosticPosition pos, Symbol sym, Scope s, boolean staticImport) {
        for (Scope.Entry e = s.lookup(sym.name); e.scope != null; e = e.next()) {
            boolean isClassDecl = e.scope == s;
            if ((isClassDecl || sym != e.sym) &&
                sym.kind == e.sym.kind &&
                sym.name != names.error) {
                if (!e.sym.type.isErroneous()) {
                    String what = e.sym.toString();
                    if (!isClassDecl) {
                        if (staticImport)
                            log.error(pos, "already.defined.static.single.import", what);
                        else
                            log.error(pos, "already.defined.single.import", what);
                    }
                    else if (sym != e.sym)
                        log.error(pos, "already.defined.this.unit", what);
                }
                return false;
            }
        }
        return true;
    }
    public void checkCanonical(JCTree tree) {
        if (!isCanonical(tree))
            log.error(tree.pos(), "import.requires.canonical",
                      TreeInfo.symbol(tree));
    }
        private boolean isCanonical(JCTree tree) {
            while (tree.getTag() == JCTree.SELECT) {
                JCFieldAccess s = (JCFieldAccess) tree;
                if (s.sym.owner != TreeInfo.symbol(s.selected))
                    return false;
                tree = s.selected;
            }
            return true;
        }
    private class ConversionWarner extends Warner {
        final String uncheckedKey;
        final Type found;
        final Type expected;
        public ConversionWarner(DiagnosticPosition pos, String uncheckedKey, Type found, Type expected) {
            super(pos);
            this.uncheckedKey = uncheckedKey;
            this.found = found;
            this.expected = expected;
        }
        @Override
        public void warn(LintCategory lint) {
            boolean warned = this.warned;
            super.warn(lint);
            if (warned) return; 
            switch (lint) {
                case UNCHECKED:
                    Check.this.warnUnchecked(pos(), "prob.found.req", diags.fragment(uncheckedKey), found, expected);
                    break;
                case VARARGS:
                    if (method != null &&
                            method.attribute(syms.trustMeType.tsym) != null &&
                            isTrustMeAllowedOnMethod(method) &&
                            !types.isReifiable(method.type.getParameterTypes().last())) {
                        Check.this.warnUnsafeVararg(pos(), "varargs.unsafe.use.varargs.param", method.params.last());
                    }
                    break;
                default:
                    throw new AssertionError("Unexpected lint: " + lint);
            }
        }
    }
    public Warner castWarner(DiagnosticPosition pos, Type found, Type expected) {
        return new ConversionWarner(pos, "unchecked.cast.to.type", found, expected);
    }
    public Warner convertWarner(DiagnosticPosition pos, Type found, Type expected) {
        return new ConversionWarner(pos, "unchecked.assign", found, expected);
    }
}
