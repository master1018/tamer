public class Infer {
    protected static final Context.Key<Infer> inferKey =
        new Context.Key<Infer>();
    public static final Type anyPoly = new Type(NONE, null);
    Symtab syms;
    Types types;
    Check chk;
    Resolve rs;
    JCDiagnostic.Factory diags;
    public static Infer instance(Context context) {
        Infer instance = context.get(inferKey);
        if (instance == null)
            instance = new Infer(context);
        return instance;
    }
    protected Infer(Context context) {
        context.put(inferKey, this);
        syms = Symtab.instance(context);
        types = Types.instance(context);
        rs = Resolve.instance(context);
        chk = Check.instance(context);
        diags = JCDiagnostic.Factory.instance(context);
        ambiguousNoInstanceException =
            new NoInstanceException(true, diags);
        unambiguousNoInstanceException =
            new NoInstanceException(false, diags);
        invalidInstanceException =
            new InvalidInstanceException(diags);
    }
    public static class InferenceException extends Resolve.InapplicableMethodException {
        private static final long serialVersionUID = 0;
        InferenceException(JCDiagnostic.Factory diags) {
            super(diags);
        }
    }
    public static class NoInstanceException extends InferenceException {
        private static final long serialVersionUID = 1;
        boolean isAmbiguous; 
        NoInstanceException(boolean isAmbiguous, JCDiagnostic.Factory diags) {
            super(diags);
            this.isAmbiguous = isAmbiguous;
        }
    }
    public static class InvalidInstanceException extends InferenceException {
        private static final long serialVersionUID = 2;
        InvalidInstanceException(JCDiagnostic.Factory diags) {
            super(diags);
        }
    }
    private final NoInstanceException ambiguousNoInstanceException;
    private final NoInstanceException unambiguousNoInstanceException;
    private final InvalidInstanceException invalidInstanceException;
    Mapping fromTypeVarFun = new Mapping("fromTypeVarFun") {
            public Type apply(Type t) {
                if (t.tag == TYPEVAR) return new UndetVar(t);
                else return t.map(this);
            }
        };
    Mapping getInstFun = new Mapping("getInstFun") {
            public Type apply(Type t) {
                switch (t.tag) {
                    case UNKNOWN:
                        throw ambiguousNoInstanceException
                            .setMessage("undetermined.type");
                    case UNDETVAR:
                        UndetVar that = (UndetVar) t;
                        if (that.inst == null)
                            throw ambiguousNoInstanceException
                                .setMessage("type.variable.has.undetermined.type",
                                            that.qtype);
                        return isConstraintCyclic(that) ?
                            that.qtype :
                            apply(that.inst);
                        default:
                            return t.map(this);
                }
            }
            private boolean isConstraintCyclic(UndetVar uv) {
                Types.UnaryVisitor<Boolean> constraintScanner =
                        new Types.UnaryVisitor<Boolean>() {
                    List<Type> seen = List.nil();
                    Boolean visit(List<Type> ts) {
                        for (Type t : ts) {
                            if (visit(t)) return true;
                        }
                        return false;
                    }
                    public Boolean visitType(Type t, Void ignored) {
                        return false;
                    }
                    @Override
                    public Boolean visitClassType(ClassType t, Void ignored) {
                        if (t.isCompound()) {
                            return visit(types.supertype(t)) ||
                                    visit(types.interfaces(t));
                        } else {
                            return visit(t.getTypeArguments());
                        }
                    }
                    @Override
                    public Boolean visitWildcardType(WildcardType t, Void ignored) {
                        return visit(t.type);
                    }
                    @Override
                    public Boolean visitUndetVar(UndetVar t, Void ignored) {
                        if (seen.contains(t)) {
                            return true;
                        } else {
                            seen = seen.prepend(t);
                            return visit(t.inst);
                        }
                    }
                };
                return constraintScanner.visit(uv);
            }
        };
    void maximizeInst(UndetVar that, Warner warn) throws NoInstanceException {
        List<Type> hibounds = Type.filter(that.hibounds, errorFilter);
        if (that.inst == null) {
            if (hibounds.isEmpty())
                that.inst = syms.objectType;
            else if (hibounds.tail.isEmpty())
                that.inst = hibounds.head;
            else
                that.inst = types.glb(hibounds);
        }
        if (that.inst == null ||
            that.inst.isErroneous())
            throw ambiguousNoInstanceException
                .setMessage("no.unique.maximal.instance.exists",
                            that.qtype, hibounds);
    }
        private boolean isSubClass(Type t, final List<Type> ts) {
            t = t.baseType();
            if (t.tag == TYPEVAR) {
                List<Type> bounds = types.getBounds((TypeVar)t);
                for (Type s : ts) {
                    if (!types.isSameType(t, s.baseType())) {
                        for (Type bound : bounds) {
                            if (!isSubClass(bound, List.of(s.baseType())))
                                return false;
                        }
                    }
                }
            } else {
                for (Type s : ts) {
                    if (!t.tsym.isSubClass(s.baseType().tsym, types))
                        return false;
                }
            }
            return true;
        }
    private Filter<Type> errorFilter = new Filter<Type>() {
        @Override
        public boolean accepts(Type t) {
            return !t.isErroneous();
        }
    };
    void minimizeInst(UndetVar that, Warner warn) throws NoInstanceException {
        List<Type> lobounds = Type.filter(that.lobounds, errorFilter);
        if (that.inst == null) {
            if (lobounds.isEmpty())
                that.inst = syms.botType;
            else if (lobounds.tail.isEmpty())
                that.inst = lobounds.head.isPrimitive() ? syms.errType : lobounds.head;
            else {
                that.inst = types.lub(lobounds);
            }
            if (that.inst == null || that.inst.tag == ERROR)
                    throw ambiguousNoInstanceException
                        .setMessage("no.unique.minimal.instance.exists",
                                    that.qtype, lobounds);
            List<Type> hibounds = Type.filter(that.hibounds, errorFilter);
            if (hibounds.isEmpty())
                return;
            Type hb = null;
            if (hibounds.tail.isEmpty())
                hb = hibounds.head;
            else for (List<Type> bs = hibounds;
                      bs.nonEmpty() && hb == null;
                      bs = bs.tail) {
                if (isSubClass(bs.head, hibounds))
                    hb = types.fromUnknownFun.apply(bs.head);
            }
            if (hb == null ||
                !types.isSubtypeUnchecked(hb, hibounds, warn) ||
                !types.isSubtypeUnchecked(that.inst, hb, warn))
                throw ambiguousNoInstanceException;
        }
    }
    public Type instantiateExpr(ForAll that,
                                Type to,
                                Warner warn) throws InferenceException {
        List<Type> undetvars = Type.map(that.tvars, fromTypeVarFun);
        for (List<Type> l = undetvars; l.nonEmpty(); l = l.tail) {
            UndetVar uv = (UndetVar) l.head;
            TypeVar tv = (TypeVar)uv.qtype;
            ListBuffer<Type> hibounds = new ListBuffer<Type>();
            for (Type t : that.getConstraints(tv, ConstraintKind.EXTENDS)) {
                hibounds.append(types.subst(t, that.tvars, undetvars));
            }
            List<Type> inst = that.getConstraints(tv, ConstraintKind.EQUAL);
            if (inst.nonEmpty() && inst.head.tag != BOT) {
                uv.inst = inst.head;
            }
            uv.hibounds = hibounds.toList();
        }
        Type qtype1 = types.subst(that.qtype, that.tvars, undetvars);
        if (!types.isSubtype(qtype1,
                qtype1.tag == UNDETVAR ? types.boxedTypeOrType(to) : to)) {
            throw unambiguousNoInstanceException
                .setMessage("infer.no.conforming.instance.exists",
                            that.tvars, that.qtype, to);
        }
        for (List<Type> l = undetvars; l.nonEmpty(); l = l.tail)
            maximizeInst((UndetVar) l.head, warn);
        List<Type> targs = Type.map(undetvars, getInstFun);
        if (Type.containsAny(targs, that.tvars)) {
            targs = types.subst(targs,
                    that.tvars,
                    instaniateAsUninferredVars(undetvars, that.tvars));
        }
        return chk.checkType(warn.pos(), that.inst(targs, types), to);
    }
    private List<Type> instaniateAsUninferredVars(List<Type> undetvars, List<Type> tvars) {
        ListBuffer<Type> new_targs = ListBuffer.lb();
        for (Type t : undetvars) {
            UndetVar uv = (UndetVar)t;
            Type newArg = new CapturedType(t.tsym.name, t.tsym, uv.inst, syms.botType, null);
            new_targs = new_targs.append(newArg);
        }
        for (Type t : new_targs.toList()) {
            CapturedType ct = (CapturedType)t;
            ct.bound = types.subst(ct.bound, tvars, new_targs.toList());
            WildcardType wt = new WildcardType(ct.bound, BoundKind.EXTENDS, syms.boundClass);
            ct.wildcard = wt;
        }
        return new_targs.toList();
    }
    public Type instantiateMethod(final Env<AttrContext> env,
                                  List<Type> tvars,
                                  MethodType mt,
                                  final Symbol msym,
                                  final List<Type> argtypes,
                                  final boolean allowBoxing,
                                  final boolean useVarargs,
                                  final Warner warn) throws InferenceException {
        List<Type> undetvars = Type.map(tvars, fromTypeVarFun);
        List<Type> formals = mt.argtypes;
        final List<Type> capturedArgs = types.capture(argtypes);
        List<Type> actuals = capturedArgs;
        List<Type> actualsNoCapture = argtypes;
        Type varargsFormal = useVarargs ? formals.last() : null;
        if (varargsFormal == null &&
                actuals.size() != formals.size()) {
            throw unambiguousNoInstanceException
                .setMessage("infer.arg.length.mismatch");
        }
        while (actuals.nonEmpty() && formals.head != varargsFormal) {
            Type formal = formals.head;
            Type actual = actuals.head.baseType();
            Type actualNoCapture = actualsNoCapture.head.baseType();
            if (actual.tag == FORALL)
                actual = instantiateArg((ForAll)actual, formal, tvars, warn);
            Type undetFormal = types.subst(formal, tvars, undetvars);
            boolean works = allowBoxing
                ? types.isConvertible(actual, undetFormal, warn)
                : types.isSubtypeUnchecked(actual, undetFormal, warn);
            if (!works) {
                throw unambiguousNoInstanceException
                    .setMessage("infer.no.conforming.assignment.exists",
                                tvars, actualNoCapture, formal);
            }
            formals = formals.tail;
            actuals = actuals.tail;
            actualsNoCapture = actualsNoCapture.tail;
        }
        if (formals.head != varargsFormal) 
            throw unambiguousNoInstanceException.setMessage("infer.arg.length.mismatch");
        if (useVarargs) {
            Type elemType = types.elemtype(varargsFormal);
            Type elemUndet = types.subst(elemType, tvars, undetvars);
            while (actuals.nonEmpty()) {
                Type actual = actuals.head.baseType();
                Type actualNoCapture = actualsNoCapture.head.baseType();
                if (actual.tag == FORALL)
                    actual = instantiateArg((ForAll)actual, elemType, tvars, warn);
                boolean works = types.isConvertible(actual, elemUndet, warn);
                if (!works) {
                    throw unambiguousNoInstanceException
                        .setMessage("infer.no.conforming.assignment.exists",
                                    tvars, actualNoCapture, elemType);
                }
                actuals = actuals.tail;
                actualsNoCapture = actualsNoCapture.tail;
            }
        }
        for (Type t : undetvars)
            minimizeInst((UndetVar) t, warn);
        ListBuffer<Type> restvars = new ListBuffer<Type>();
        final ListBuffer<Type> restundet = new ListBuffer<Type>();
        ListBuffer<Type> insttypes = new ListBuffer<Type>();
        ListBuffer<Type> undettypes = new ListBuffer<Type>();
        for (Type t : undetvars) {
            UndetVar uv = (UndetVar)t;
            if (uv.inst.tag == BOT) {
                restvars.append(uv.qtype);
                restundet.append(uv);
                insttypes.append(uv.qtype);
                undettypes.append(uv);
                uv.inst = null;
            } else {
                insttypes.append(uv.inst);
                undettypes.append(uv.inst);
            }
        }
        checkWithinBounds(tvars, undettypes.toList(), warn);
        mt = (MethodType)types.subst(mt, tvars, insttypes.toList());
        if (!restvars.isEmpty()) {
            final List<Type> inferredTypes = insttypes.toList();
            final List<Type> all_tvars = tvars; 
            return new UninferredMethodType(mt, restvars.toList()) {
                @Override
                List<Type> getConstraints(TypeVar tv, ConstraintKind ck) {
                    for (Type t : restundet.toList()) {
                        UndetVar uv = (UndetVar)t;
                        if (uv.qtype == tv) {
                            switch (ck) {
                                case EXTENDS: return uv.hibounds.appendList(types.subst(types.getBounds(tv), all_tvars, inferredTypes));
                                case SUPER: return uv.lobounds;
                                case EQUAL: return uv.inst != null ? List.of(uv.inst) : List.<Type>nil();
                            }
                        }
                    }
                    return List.nil();
                }
                @Override
                void check(List<Type> inferred, Types types) throws NoInstanceException {
                    checkArgumentsAcceptable(env, capturedArgs, getParameterTypes(), allowBoxing, useVarargs, warn);
                    checkWithinBounds(all_tvars,
                           types.subst(inferredTypes, tvars, inferred), warn);
                    if (useVarargs) {
                        chk.checkVararg(env.tree.pos(), getParameterTypes(), msym);
                    }
            }};
        }
        else {
            checkArgumentsAcceptable(env, capturedArgs, mt.getParameterTypes(), allowBoxing, useVarargs, warn);
            return mt;
        }
    }
        static abstract class UninferredMethodType extends DelegatedType {
            final List<Type> tvars;
            public UninferredMethodType(MethodType mtype, List<Type> tvars) {
                super(METHOD, new MethodType(mtype.argtypes, null, mtype.thrown, mtype.tsym));
                this.tvars = tvars;
                asMethodType().restype = new UninferredReturnType(tvars, mtype.restype);
            }
            @Override
            public MethodType asMethodType() {
                return qtype.asMethodType();
            }
            @Override
            public Type map(Mapping f) {
                return qtype.map(f);
            }
            void instantiateReturnType(Type restype, List<Type> inferred, Types types) throws NoInstanceException {
                qtype = new MethodType(types.subst(getParameterTypes(), tvars, inferred),
                                       restype,
                                       types.subst(UninferredMethodType.this.getThrownTypes(), tvars, inferred),
                                       UninferredMethodType.this.qtype.tsym);
                check(inferred, types);
            }
            abstract void check(List<Type> inferred, Types types) throws NoInstanceException;
            abstract List<Type> getConstraints(TypeVar tv, ConstraintKind ck);
            class UninferredReturnType extends ForAll {
                public UninferredReturnType(List<Type> tvars, Type restype) {
                    super(tvars, restype);
                }
                @Override
                public Type inst(List<Type> actuals, Types types) {
                    Type newRestype = super.inst(actuals, types);
                    instantiateReturnType(newRestype, actuals, types);
                    return newRestype;
                }
                @Override
                public List<Type> getConstraints(TypeVar tv, ConstraintKind ck) {
                    return UninferredMethodType.this.getConstraints(tv, ck);
                }
            }
        }
        private void checkArgumentsAcceptable(Env<AttrContext> env, List<Type> actuals, List<Type> formals,
                boolean allowBoxing, boolean useVarargs, Warner warn) {
            try {
                rs.checkRawArgumentsAcceptable(env, actuals, formals,
                       allowBoxing, useVarargs, warn);
            }
            catch (Resolve.InapplicableMethodException ex) {
                throw invalidInstanceException.setMessage(ex.getDiagnostic());
            }
        }
    private Type instantiateArg(ForAll that,
                                Type to,
                                List<Type> tvars,
                                Warner warn) throws InferenceException {
        List<Type> targs;
        try {
            return instantiateExpr(that, to, warn);
        } catch (NoInstanceException ex) {
            Type to1 = to;
            for (List<Type> l = tvars; l.nonEmpty(); l = l.tail)
                to1 = types.subst(to1, List.of(l.head), List.of(syms.unknownType));
            return instantiateExpr(that, to1, warn);
        }
    }
    void checkWithinBounds(List<Type> tvars,
                                   List<Type> arguments,
                                   Warner warn)
        throws InvalidInstanceException {
        for (List<Type> tvs = tvars, args = arguments;
             tvs.nonEmpty();
             tvs = tvs.tail, args = args.tail) {
            if (args.head instanceof UndetVar ||
                    tvars.head.getUpperBound().isErroneous()) continue;
            List<Type> bounds = types.subst(types.getBounds((TypeVar)tvs.head), tvars, arguments);
            if (!types.isSubtypeUnchecked(args.head, bounds, warn))
                throw invalidInstanceException
                    .setMessage("inferred.do.not.conform.to.bounds",
                                args.head, bounds);
        }
    }
    Type instantiatePolymorphicSignatureInstance(Env<AttrContext> env, Type site,
                                            Name name,
                                            MethodSymbol spMethod,  
                                            List<Type> argtypes) {
        final Type restype;
        switch (env.next.tree.getTag()) {
            case JCTree.TYPECAST:
                JCTypeCast castTree = (JCTypeCast)env.next.tree;
                restype = (TreeInfo.skipParens(castTree.expr) == env.tree) ?
                    castTree.clazz.type :
                    syms.objectType;
                break;
            case JCTree.EXEC:
                JCTree.JCExpressionStatement execTree =
                        (JCTree.JCExpressionStatement)env.next.tree;
                restype = (TreeInfo.skipParens(execTree.expr) == env.tree) ?
                    syms.voidType :
                    syms.objectType;
                break;
            default:
                restype = syms.objectType;
        }
        List<Type> paramtypes = Type.map(argtypes, implicitArgType);
        List<Type> exType = spMethod != null ?
            spMethod.getThrownTypes() :
            List.of(syms.throwableType); 
        MethodType mtype = new MethodType(paramtypes,
                                          restype,
                                          exType,
                                          syms.methodClass);
        return mtype;
    }
        Mapping implicitArgType = new Mapping ("implicitArgType") {
                public Type apply(Type t) {
                    t = types.erasure(t);
                    if (t.tag == BOT)
                        t = types.boxedClass(syms.voidType).type;
                    return t;
                }
        };
    }
