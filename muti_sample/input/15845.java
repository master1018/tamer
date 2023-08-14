class FieldExpression extends UnaryExpression {
    Identifier id;
    MemberDefinition field;
    Expression implementation;
    ClassDefinition clazz;
    private ClassDefinition superBase;
    public FieldExpression(long where, Expression right, Identifier id) {
        super(FIELD, where, Type.tError, right);
        this.id = id;
    }
    public FieldExpression(long where, Expression right, MemberDefinition field) {
        super(FIELD, where, field.getType(), right);
        this.id = field.getName();
        this.field = field;
    }
    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }
    private boolean isQualSuper() {
        return superBase != null;
    }
    static public Identifier toIdentifier(Expression e) {
        StringBuffer buf = new StringBuffer();
        while (e.op == FIELD) {
            FieldExpression fe = (FieldExpression)e;
            if (fe.id == idThis || fe.id == idClass) {
                return null;
            }
            buf.insert(0, fe.id);
            buf.insert(0, '.');
            e = fe.right;
        }
        if (e.op != IDENT) {
            return null;
        }
        buf.insert(0, ((IdentifierExpression)e).id);
        return Identifier.lookup(buf.toString());
    }
    Type toType(Environment env, Context ctx) {
        Identifier id = toIdentifier(this);
        if (id == null) {
            env.error(where, "invalid.type.expr");
            return Type.tError;
        }
        Type t = Type.tClass(ctx.resolveName(env, id));
        if (env.resolve(where, ctx.field.getClassDefinition(), t)) {
            return t;
        }
        return Type.tError;
    }
    public Vset checkAmbigName(Environment env, Context ctx,
                               Vset vset, Hashtable exp,
                               UnaryExpression loc) {
        if (id == idThis || id == idClass) {
            loc = null;         
        }
        return checkCommon(env, ctx, vset, exp, loc, false);
    }
    public Vset checkValue(Environment env, Context ctx,
                           Vset vset, Hashtable exp) {
        vset = checkCommon(env, ctx, vset, exp, null, false);
        if (id == idSuper && type != Type.tError) {
            env.error(where, "undef.var.super", idSuper);
        }
        return vset;
    }
    static void reportFailedPackagePrefix(Environment env, Expression right) {
        reportFailedPackagePrefix(env, right, false);
    }
    static void reportFailedPackagePrefix(Environment env,
                                          Expression right,
                                          boolean mustBeType) {
        Expression idp = right;
        while (idp instanceof UnaryExpression)
            idp = ((UnaryExpression)idp).right;
        IdentifierExpression ie = (IdentifierExpression)idp;
        try {
            env.resolve(ie.id);
        } catch (AmbiguousClass e) {
            env.error(right.where, "ambig.class", e.name1, e.name2);
            return;
        } catch (ClassNotFound e) {
        }
        if (idp == right) {
            if (mustBeType) {
                env.error(ie.where, "undef.class", ie.id);
            } else {
                env.error(ie.where, "undef.var.or.class", ie.id);
            }
        } else {
            if (mustBeType) {
                env.error(ie.where, "undef.class.or.package", ie.id);
            } else {
                env.error(ie.where, "undef.var.class.or.package", ie.id);
            }
        }
    }
    private Expression
    implementFieldAccess(Environment env, Context ctx, Expression base, boolean isLHS) {
        ClassDefinition abase = accessBase(env, ctx);
        if (abase != null) {
            if (field.isFinal()) {
                Expression e = (Expression)field.getValue();
                if ((e != null) && e.isConstant() && !isLHS) {
                    return e.copyInline(ctx);
                }
            }
            MemberDefinition af = abase.getAccessMember(env, ctx, field, isQualSuper());
            if (!isLHS) {
                if (field.isStatic()) {
                    Expression args[] = { };
                    Expression call =
                        new MethodExpression(where, null, af, args);
                    return new CommaExpression(where, base, call);
                } else {
                    Expression args[] = { base };
                    return new MethodExpression(where, null, af, args);
                }
            }
        }
        return null;
    }
    private ClassDefinition accessBase(Environment env, Context ctx) {
        if (field.isPrivate()) {
            ClassDefinition cdef = field.getClassDefinition();
            ClassDefinition ctxClass = ctx.field.getClassDefinition();
            if (cdef == ctxClass){
                return null;
            }
            return cdef;
        } else if (field.isProtected()) {
            if (superBase == null) {
                return null;
            }
            ClassDefinition cdef = field.getClassDefinition();
            ClassDefinition ctxClass = ctx.field.getClassDefinition();
            if (cdef.inSamePackage(ctxClass)) {
                return null;
            }
            return superBase;
        } else {
            return null;
        }
    }
    static boolean isTypeAccessible(long where,
                                    Environment env,
                                    Type t,
                                    ClassDefinition c) {
        switch (t.getTypeCode()) {
          case TC_CLASS:
            try {
                Identifier nm = t.getClassName();
                ClassDefinition def = env.getClassDefinition(t);
                return c.canAccess(env, def.getClassDeclaration());
            } catch (ClassNotFound e) {}  
            return true;
          case TC_ARRAY:
            return isTypeAccessible(where, env, t.getElementType(), c);
          default:
            return true;
        }
    }
    private Vset checkCommon(Environment env, Context ctx,
                             Vset vset, Hashtable exp,
                             UnaryExpression loc, boolean isLHS) {
        if (id == idClass) {
            Type t = right.toType(env, ctx);
            if (!t.isType(TC_CLASS) && !t.isType(TC_ARRAY)) {
                if (t.isType(TC_ERROR)) {
                    type = Type.tClassDesc;
                    return vset;
                }
                String wrc = null;
                switch (t.getTypeCode()) {
                  case TC_VOID: wrc = "Void"; break;
                  case TC_BOOLEAN: wrc = "Boolean"; break;
                  case TC_BYTE: wrc = "Byte"; break;
                  case TC_CHAR: wrc = "Character"; break;
                  case TC_SHORT: wrc = "Short"; break;
                  case TC_INT: wrc = "Integer"; break;
                  case TC_FLOAT: wrc = "Float"; break;
                  case TC_LONG: wrc = "Long"; break;
                  case TC_DOUBLE: wrc = "Double"; break;
                  default:
                      env.error(right.where, "invalid.type.expr");
                      return vset;
                }
                Identifier wid = Identifier.lookup(idJavaLang+"."+wrc);
                Expression wcls = new TypeExpression(where, Type.tClass(wid));
                implementation = new FieldExpression(where, wcls, idTYPE);
                vset = implementation.checkValue(env, ctx, vset, exp);
                type = implementation.type; 
                return vset;
            }
            if (t.isVoidArray()) {
                type = Type.tClassDesc;
                env.error(right.where, "void.array");
                return vset;
            }
            long fwhere = ctx.field.getWhere();
            ClassDefinition fcls = ctx.field.getClassDefinition();
            MemberDefinition lookup = fcls.getClassLiteralLookup(fwhere);
            String sig = t.getTypeSignature();
            String className;
            if (t.isType(TC_CLASS)) {
                className = sig.substring(1, sig.length()-1)
                    .replace(SIGC_PACKAGE, '.');
            } else {
                className = sig.replace(SIGC_PACKAGE, '.');
            }
            if (fcls.isInterface()) {
                implementation =
                    makeClassLiteralInlineRef(env, ctx, lookup, className);
            } else {
                ClassDefinition inClass = lookup.getClassDefinition();
                MemberDefinition cfld =
                    getClassLiteralCache(env, ctx, className, inClass);
                implementation =
                    makeClassLiteralCacheRef(env, ctx, lookup, cfld, className);
            }
            vset = implementation.checkValue(env, ctx, vset, exp);
            type = implementation.type; 
            return vset;
        }
        if (field != null) {
            implementation = implementFieldAccess(env, ctx, right, isLHS);
            return (right == null) ?
                vset : right.checkAmbigName(env, ctx, vset, exp, this);
        }
        vset = right.checkAmbigName(env, ctx, vset, exp, this);
        if (right.type == Type.tPackage) {
            if (loc == null) {
                FieldExpression.reportFailedPackagePrefix(env, right);
                return vset;
            }
            Identifier nm = toIdentifier(this);
            if ((nm != null) && env.classExists(nm)) {
                loc.right = new TypeExpression(where, Type.tClass(nm));
                ClassDefinition ctxClass = ctx.field.getClassDefinition();
                env.resolve(where, ctxClass, loc.right.type);
                return vset;
            }
            type = Type.tPackage;
            return vset;
        }
        ClassDefinition ctxClass = ctx.field.getClassDefinition();
        boolean staticRef = (right instanceof TypeExpression);
        try {
            if (!right.type.isType(TC_CLASS)) {
                if (right.type.isType(TC_ARRAY) && id.equals(idLength)) {
                    if (!FieldExpression.isTypeAccessible(where, env, right.type, ctxClass)) {
                        ClassDeclaration cdecl = ctxClass.getClassDeclaration();
                        if (staticRef) {
                            env.error(where, "no.type.access",
                                      id, right.type.toString(), cdecl);
                        } else {
                            env.error(where, "cant.access.member.type",
                                      id, right.type.toString(), cdecl);
                        }
                    }
                    type = Type.tInt;
                    implementation = new LengthExpression(where, right);
                    return vset;
                }
                if (!right.type.isType(TC_ERROR)) {
                    env.error(where, "invalid.field.reference", id, right.type);
                }
                return vset;
            }
            ClassDefinition sourceClass = ctxClass;
            if (right instanceof FieldExpression) {
                Identifier id = ((FieldExpression)right).id;
                if (id == idThis) {
                    sourceClass = ((FieldExpression)right).clazz;
                } else if (id == idSuper) {
                    sourceClass = ((FieldExpression)right).clazz;
                    superBase = sourceClass;
                }
            }
            clazz = env.getClassDefinition(right.type);
            if (id == idThis || id == idSuper) {
                if (!staticRef) {
                    env.error(right.where, "invalid.type.expr");
                }
                if (ctx.field.isSynthetic())
                    throw new CompilerError("synthetic qualified this");
                implementation = ctx.findOuterLink(env, where, clazz, null, true);
                vset = implementation.checkValue(env, ctx, vset, exp);
                if (id == idSuper) {
                    type = clazz.getSuperClass().getType();
                } else {
                    type = clazz.getType();
                }
                return vset;
            }
            field = clazz.getVariable(env, id, sourceClass);
            if (field == null && staticRef && loc != null) {
                field = clazz.getInnerClass(env, id);
                if (field != null) {
                    return checkInnerClass(env, ctx, vset, exp, loc);
                }
            }
            if (field == null) {
                if ((field = clazz.findAnyMethod(env, id)) != null) {
                    env.error(where, "invalid.field",
                              id, field.getClassDeclaration());
                } else {
                    env.error(where, "no.such.field", id, clazz);
                }
                return vset;
            }
            if (!FieldExpression.isTypeAccessible(where, env, right.type, sourceClass)) {
                ClassDeclaration cdecl = sourceClass.getClassDeclaration();
                if (staticRef) {
                    env.error(where, "no.type.access",
                              id, right.type.toString(), cdecl);
                } else {
                    env.error(where, "cant.access.member.type",
                              id, right.type.toString(), cdecl);
                }
            }
            type = field.getType();
            if (!sourceClass.canAccess(env, field)) {
                env.error(where, "no.field.access",
                          id, clazz, sourceClass.getClassDeclaration());
                return vset;
            }
            if (staticRef && !field.isStatic()) {
                env.error(where, "no.static.field.access", id, clazz);
                return vset;
            } else {
                implementation = implementFieldAccess(env, ctx, right, isLHS);
            }
            if (field.isProtected()
                && !(right instanceof SuperExpression
                     || (right instanceof FieldExpression &&
                         ((FieldExpression)right).id == idSuper))
                && !sourceClass.protectedAccess(env, field, right.type)) {
                env.error(where, "invalid.protected.field.use",
                          field.getName(), field.getClassDeclaration(),
                          right.type);
                return vset;
            }
            if ((!field.isStatic()) &&
                (right.op == THIS) && !vset.testVar(ctx.getThisNumber())) {
                env.error(where, "access.inst.before.super", id);
            }
            if (field.reportDeprecated(env)) {
                env.error(where, "warn."+"field.is.deprecated",
                          id, field.getClassDefinition());
            }
            if (sourceClass == ctxClass) {
                ClassDefinition declarer = field.getClassDefinition();
                if (declarer.isPackagePrivate() &&
                    !declarer.getName().getQualifier()
                    .equals(sourceClass.getName().getQualifier())) {
                    field =
                        MemberDefinition.makeProxyMember(field, clazz, env);
                }
            }
            sourceClass.addDependency(field.getClassDeclaration());
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, ctx.field);
        } catch (AmbiguousMember e) {
            env.error(where, "ambig.field",
                      id, e.field1.getClassDeclaration(), e.field2.getClassDeclaration());
        }
        return vset;
    }
    public FieldUpdater getAssigner(Environment env, Context ctx) {
        if (field == null) {
            return null;
        }
        ClassDefinition abase = accessBase(env, ctx);
        if (abase != null) {
            MemberDefinition setter = abase.getUpdateMember(env, ctx, field, isQualSuper());
            Expression base = (right == null) ? null : right.copyInline(ctx);
            return new FieldUpdater(where, field, base, null, setter);
        }
        return null;
    }
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        if (field == null) {
            return null;
        }
        ClassDefinition abase = accessBase(env, ctx);
        if (abase != null) {
            MemberDefinition getter = abase.getAccessMember(env, ctx, field, isQualSuper());
            MemberDefinition setter = abase.getUpdateMember(env, ctx, field, isQualSuper());
            Expression base = (right == null) ? null : right.copyInline(ctx);
            return new FieldUpdater(where, field, base, getter, setter);
        }
        return null;
    }
    private Vset checkInnerClass(Environment env, Context ctx,
                                 Vset vset, Hashtable exp,
                                 UnaryExpression loc) {
        ClassDefinition inner = field.getInnerClass();
        type = inner.getType();
        if (!inner.isTopLevel()) {
            env.error(where, "inner.static.ref", inner.getName());
        }
        Expression te = new TypeExpression(where, type);
        ClassDefinition ctxClass = ctx.field.getClassDefinition();
        try {
            if (!ctxClass.canAccess(env, field)) {
                ClassDefinition clazz = env.getClassDefinition(right.type);
                env.error(where, "no.type.access",
                          id, clazz, ctxClass.getClassDeclaration());
                return vset;
            }
            if (field.isProtected()
                && !(right instanceof SuperExpression
                     || (right instanceof FieldExpression &&
                         ((FieldExpression)right).id == idSuper))
                && !ctxClass.protectedAccess(env, field, right.type)){
                env.error(where, "invalid.protected.field.use",
                          field.getName(), field.getClassDeclaration(),
                          right.type);
                return vset;
            }
            inner.noteUsedBy(ctxClass, where, env);
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, ctx.field);
        }
        ctxClass.addDependency(field.getClassDeclaration());
        if (loc == null)
            return te.checkValue(env, ctx, vset, exp);
        loc.right = te;
        return vset;
    }
    public Vset checkLHS(Environment env, Context ctx,
                         Vset vset, Hashtable exp) {
        boolean hadField = (field != null);
        checkCommon(env, ctx, vset, exp, null, true);
        if (implementation != null) {
            return super.checkLHS(env, ctx, vset, exp);
        }
        if (field != null && field.isFinal() && !hadField) {
            if (field.isBlankFinal()) {
                if (field.isStatic()) {
                    if (right != null) {
                        env.error(where, "qualified.static.final.assign");
                    }
                } else {
                    if ((right != null) && (right.op != THIS)) {
                        env.error(where, "bad.qualified.final.assign", field.getName());
                        return vset;
                    }
                }
                vset = checkFinalAssign(env, ctx, vset, where, field);
            } else {
                env.error(where, "assign.to.final", id);
            }
        }
        return vset;
    }
    public Vset checkAssignOp(Environment env, Context ctx,
                              Vset vset, Hashtable exp, Expression outside) {
        checkCommon(env, ctx, vset, exp, null, true);
        if (implementation != null) {
            return super.checkLHS(env, ctx, vset, exp);
        }
        if (field != null && field.isFinal()) {
            env.error(where, "assign.to.final", id);
        }
        return vset;
    }
    public static Vset checkFinalAssign(Environment env, Context ctx,
                                        Vset vset, long where,
                                        MemberDefinition field) {
        if (field.isBlankFinal()
            && field.getClassDefinition() == ctx.field.getClassDefinition()) {
            int number = ctx.getFieldNumber(field);
            if (number >= 0 && vset.testVarUnassigned(number)) {
                vset = vset.addVar(number);
            } else {
                Identifier id = field.getName();
                env.error(where, "assign.to.blank.final", id);
            }
        } else {
            Identifier id = field.getName();
            env.error(where, "assign.to.final", id);
        }
        return vset;
    }
    private static MemberDefinition getClassLiteralCache(Environment env,
                                                         Context ctx,
                                                         String className,
                                                         ClassDefinition c) {
        String lname;
        if (!className.startsWith(SIG_ARRAY)) {
            lname = prefixClass + className.replace('.', '$');
        } else {
            lname = prefixArray + className.substring(1);
            lname = lname.replace(SIGC_ARRAY, '$'); 
            if (className.endsWith(SIG_ENDCLASS)) {
                lname = lname.substring(0, lname.length() - 1);
                lname = lname.replace('.', '$');
            }
        }
        Identifier fname = Identifier.lookup(lname);
        MemberDefinition cfld;
        try {
            cfld = c.getVariable(env, fname, c);
        } catch (ClassNotFound ee) {
            return null;
        } catch (AmbiguousMember ee) {
            return null;
        }
        if (cfld != null && cfld.getClassDefinition() == c) {
            return cfld;
        }
        return env.makeMemberDefinition(env, c.getWhere(),
                                        c, null,
                                        M_STATIC | M_SYNTHETIC, 
                                        Type.tClassDesc, fname,
                                        null, null, null);
    }
    private Expression makeClassLiteralCacheRef(Environment env, Context ctx,
                                                MemberDefinition lookup,
                                                MemberDefinition cfld,
                                                String className) {
        Expression ccls = new TypeExpression(where,
                                             cfld.getClassDefinition()
                                             .getType());
        Expression cache = new FieldExpression(where, ccls, cfld);
        Expression cacheOK =
            new NotEqualExpression(where, cache.copyInline(ctx),
                                   new NullExpression(where));
        Expression lcls =
            new TypeExpression(where, lookup.getClassDefinition() .getType());
        Expression name = new StringExpression(where, className);
        Expression namearg[] = { name };
        Expression setCache = new MethodExpression(where, lcls,
                                                   lookup, namearg);
        setCache = new AssignExpression(where, cache.copyInline(ctx),
                                        setCache);
        return new ConditionalExpression(where, cacheOK, cache, setCache);
    }
    private Expression makeClassLiteralInlineRef(Environment env, Context ctx,
                                                 MemberDefinition lookup,
                                                 String className) {
        Expression lcls =
            new TypeExpression(where, lookup.getClassDefinition().getType());
        Expression name = new StringExpression(where, className);
        Expression namearg[] = { name };
        Expression getClass = new MethodExpression(where, lcls,
                                                   lookup, namearg);
        return getClass;
    }
    public boolean isConstant() {
        if (implementation != null)
            return implementation.isConstant();
        if ((field != null)
            && (right == null || right instanceof TypeExpression
                || (right.op == THIS && right.where == where))) {
            return field.isConstant();
        }
        return false;
    }
    public Expression inline(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inline(env, ctx);
        Expression e = inlineValue(env, ctx);
        if (e instanceof FieldExpression) {
            FieldExpression fe = (FieldExpression) e;
            if ((fe.right != null) && (fe.right.op==THIS))
                return null;
            }
        return e;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        try {
            if (field == null) {
                return this;
            }
            if (field.isFinal()) {
                Expression e = (Expression)field.getValue(env);
                if ((e != null) && e.isConstant()) {
                    e = e.copyInline(ctx);
                    e.where = where;
                    return new CommaExpression(where, right, e).inlineValue(env, ctx);
                }
            }
            if (right != null) {
                if (field.isStatic()) {
                    Expression e = right.inline(env, ctx);
                    right = null;
                    if (e != null) {
                        return new CommaExpression(where, e, this);
                    }
                } else {
                    right = right.inlineValue(env, ctx);
                }
            }
            return this;
        } catch (ClassNotFound e) {
            throw new CompilerError(e);
        }
    }
    public Expression inlineLHS(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineLHS(env, ctx);
        if (right != null) {
            if (field.isStatic()) {
                Expression e = right.inline(env, ctx);
                right = null;
                if (e != null) {
                    return new CommaExpression(where, e, this);
                }
            } else {
                right = right.inlineValue(env, ctx);
            }
        }
        return this;
    }
    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        return super.copyInline(ctx);
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (implementation != null)
            return implementation.costInline(thresh, env, ctx);
        if (ctx == null) {
            return 3 + ((right == null) ? 0
                                        : right.costInline(thresh, env, ctx));
        }
        ClassDefinition ctxClass = ctx.field.getClassDefinition();
        try {
            if (    ctxClass.permitInlinedAccess(env, field.getClassDeclaration())
                 && ctxClass.permitInlinedAccess(env, field)) {
                if (right == null) {
                    return 3;
                } else {
                    ClassDeclaration rt = env.getClassDeclaration(right.type);
                    if (ctxClass.permitInlinedAccess(env, rt)) {
                        return 3 + right.costInline(thresh, env, ctx);
                    }
                }
            }
        } catch (ClassNotFound e) {
        }
        return thresh;
    }
    int codeLValue(Environment env, Context ctx, Assembler asm) {
        if (implementation != null)
            throw new CompilerError("codeLValue");
        if (field.isStatic()) {
            if (right != null) {
                right.code(env, ctx, asm);
                return 1;
            }
            return 0;
        }
        right.codeValue(env, ctx, asm);
        return 1;
    }
    void codeLoad(Environment env, Context ctx, Assembler asm) {
        if (field == null) {
            throw new CompilerError("should not be null");
        }
        if (field.isStatic()) {
            asm.add(where, opc_getstatic, field);
        } else {
            asm.add(where, opc_getfield, field);
        }
    }
    void codeStore(Environment env, Context ctx, Assembler asm) {
        if (field.isStatic()) {
            asm.add(where, opc_putstatic, field);
        } else {
            asm.add(where, opc_putfield, field);
        }
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeLValue(env, ctx, asm);
        codeLoad(env, ctx, asm);
    }
    public void print(PrintStream out) {
        out.print("(");
        if (right != null) {
            right.print(out);
        } else {
            out.print("<empty>");
        }
        out.print("." + id + ")");
        if (implementation != null) {
            out.print("/IMPL=");
            implementation.print(out);
        }
    }
}
