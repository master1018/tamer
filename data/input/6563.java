class NewInstanceExpression extends NaryExpression {
    MemberDefinition field;
    Expression outerArg;
    ClassDefinition body;
    MemberDefinition implMethod = null;
    public NewInstanceExpression(long where, Expression right, Expression args[]) {
        super(NEWINSTANCE, where, Type.tError, right, args);
    }
    public NewInstanceExpression(long where, Expression right,
                                 Expression args[],
                                 Expression outerArg, ClassDefinition body) {
        this(where, right, args);
        this.outerArg = outerArg;
        this.body = body;
    }
    public Expression getOuterArg() {
        return outerArg;
    }
    int precedence() {
        return 100;
    }
    public Expression order() {
        if (outerArg != null && opPrecedence[FIELD] > outerArg.precedence()) {
            UnaryExpression e = (UnaryExpression)outerArg;
            outerArg = e.right;
            e.right = order();
            return e;
        }
        return this;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        ClassDefinition def = null;
        Expression alreadyChecked = null;
        try {
            if (outerArg != null) {
                vset = outerArg.checkValue(env, ctx, vset, exp);
                alreadyChecked = outerArg;
                Identifier typeName = FieldExpression.toIdentifier(right);
                if (typeName != null && typeName.isQualified()) {
                    env.error(where, "unqualified.name.required", typeName);
                }
                if (typeName == null || !outerArg.type.isType(TC_CLASS)) {
                    if (!outerArg.type.isType(TC_ERROR)) {
                        env.error(where, "invalid.field.reference",
                                  idNew, outerArg.type);
                    }
                    outerArg = null;
                } else {
                    ClassDefinition oc = env.getClassDefinition(outerArg.type);
                    Identifier nm = oc.resolveInnerClass(env, typeName);
                    right = new TypeExpression(right.where, Type.tClass(nm));
                    env.resolve(right.where, ctx.field.getClassDefinition(),
                                right.type);
                }
            }
            if (!(right instanceof TypeExpression)) {
                right = new TypeExpression(right.where, right.toType(env, ctx));
            }
            if (right.type.isType(TC_CLASS))
                def = env.getClassDefinition(right.type);
        } catch (AmbiguousClass ee) {
            env.error(where, "ambig.class", ee.name1, ee.name2);
        } catch (ClassNotFound ee) {
            env.error(where, "class.not.found", ee.name, ctx.field);
        }
        Type t = right.type;
        boolean hasErrors = t.isType(TC_ERROR);
        if (!t.isType(TC_CLASS)) {
            if (!hasErrors) {
                env.error(where, "invalid.arg.type", t, opNames[op]);
                hasErrors = true;
            }
        }
        if (def == null) {
            type = Type.tError;
            return vset;
        }
        Expression args[] = this.args;
        args = NewInstanceExpression.
                insertOuterLink(env, ctx, where, def, outerArg, args);
        if (args.length > this.args.length)
            outerArg = args[0]; 
        else if (outerArg != null)
            outerArg = new CommaExpression(outerArg.where, outerArg, null);
        Type argTypes[] = new Type[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != alreadyChecked) {
                vset = args[i].checkValue(env, ctx, vset, exp);
            }
            argTypes[i] = args[i].type;
            hasErrors = hasErrors || argTypes[i].isType(TC_ERROR);
        }
        try {
            if (hasErrors) {
                type = Type.tError;
                return vset;
            }
            ClassDefinition sourceClass = ctx.field.getClassDefinition();
            ClassDeclaration c = env.getClassDeclaration(t);
            if (body != null) {
                Identifier packageName = sourceClass.getName().getQualifier();
                ClassDefinition superDef = null;
                if (def.isInterface()) {
                    superDef = env.getClassDefinition(idJavaLangObject);
                } else {
                    superDef = def;
                }
                MemberDefinition constructor =
                    superDef.matchAnonConstructor(env, packageName, argTypes);
                if (constructor != null) {
                    if (tracing)
                        env.dtEvent(
                              "NewInstanceExpression.checkValue: ANON CLASS " +
                              body + " SUPER " + def);
                    vset = body.checkLocalClass(env, ctx, vset,
                                                def, args,
                                                constructor.getType()
                                                .getArgumentTypes());
                    t = body.getClassDeclaration().getType();
                    def = body;
                }
            } else {
                if (def.isInterface()) {
                    env.error(where, "new.intf", c);
                    return vset;
                }
                if (def.mustBeAbstract(env)) {
                    env.error(where, "new.abstract", c);
                    return vset;
                }
            }
            field = def.matchMethod(env, sourceClass, idInit, argTypes);
            if (field == null) {
                MemberDefinition anyInit = def.findAnyMethod(env, idInit);
                if (anyInit != null &&
                    new MethodExpression(where, right, anyInit, args)
                        .diagnoseMismatch(env, args, argTypes))
                    return vset;
                String sig = c.getName().getName().toString();
                sig = Type.tMethod(Type.tError, argTypes).typeString(sig, false, false);
                env.error(where, "unmatched.constr", sig, c);
                return vset;
            }
            if (field.isPrivate()) {
                ClassDefinition cdef = field.getClassDefinition();
                if (cdef != sourceClass) {
                    implMethod = cdef.getAccessMember(env, ctx, field, false);
                }
            }
            if (def.mustBeAbstract(env)) {
                env.error(where, "new.abstract", c);
                return vset;
            }
            if (field.reportDeprecated(env)) {
                env.error(where, "warn.constr.is.deprecated",
                          field, field.getClassDefinition());
            }
            if (field.isProtected() &&
                !(sourceClass.getName().getQualifier().equals(
                   field.getClassDeclaration().getName().getQualifier()))) {
                env.error(where, "invalid.protected.constructor.use",
                          sourceClass);
            }
        } catch (ClassNotFound ee) {
            env.error(where, "class.not.found", ee.name, opNames[op]);
            return vset;
        } catch (AmbiguousMember ee) {
            env.error(where, "ambig.constr", ee.field1, ee.field2);
            return vset;
        }
        argTypes = field.getType().getArgumentTypes();
        for (int i = 0 ; i < args.length ; i++) {
            args[i] = convert(env, ctx, argTypes[i], args[i]);
        }
        if (args.length > this.args.length) {
            outerArg = args[0]; 
            for (int i = 1 ; i < args.length ; i++) {
                this.args[i-1] = args[i];
            }
        }
        ClassDeclaration exceptions[] = field.getExceptions(env);
        for (int i = 0 ; i < exceptions.length ; i++) {
            if (exp.get(exceptions[i]) == null) {
                exp.put(exceptions[i], this);
            }
        }
        type = t;
        return vset;
    }
    public static Expression[] insertOuterLink(Environment env, Context ctx,
                                               long where, ClassDefinition def,
                                               Expression outerArg,
                                               Expression args[]) {
        if (!def.isTopLevel() && !def.isLocal()) {
            Expression args2[] = new Expression[1+args.length];
            System.arraycopy(args, 0, args2, 1, args.length);
            try {
                if (outerArg == null)
                    outerArg = ctx.findOuterLink(env, where,
                                                 def.findAnyMethod(env, idInit));
            } catch (ClassNotFound e) {
            }
            args2[0] = outerArg;
            args = args2;
        }
        return args;
    }
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        return checkValue(env, ctx, vset, exp);
    }
    final int MAXINLINECOST = Statement.MAXINLINECOST;
    public Expression copyInline(Context ctx) {
        NewInstanceExpression e = (NewInstanceExpression)super.copyInline(ctx);
        if (outerArg != null) {
            e.outerArg = outerArg.copyInline(ctx);
        }
        return e;
    }
    Expression inlineNewInstance(Environment env, Context ctx, Statement s) {
        if (env.dump()) {
            System.out.println("INLINE NEW INSTANCE " + field + " in " + ctx.field);
        }
        LocalMember v[] = LocalMember.copyArguments(ctx, field);
        Statement body[] = new Statement[v.length + 2];
        int o = 1;
        if (outerArg != null && !outerArg.type.isType(TC_VOID)) {
            o = 2;
            body[1] = new VarDeclarationStatement(where, v[1], outerArg);
        } else if (outerArg != null) {
            body[0] = new ExpressionStatement(where, outerArg);
        }
        for (int i = 0 ; i < args.length ; i++) {
            body[i+o] = new VarDeclarationStatement(where, v[i+o], args[i]);
        }
        body[body.length - 1] = (s != null) ? s.copyInline(ctx, false) : null;
        LocalMember.doneWithArguments(ctx, v);
        return new InlineNewInstanceExpression(where, type, field, new CompoundStatement(where, body)).inline(env, ctx);
    }
    public Expression inline(Environment env, Context ctx) {
        return inlineValue(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (body != null) {
            body.inlineLocalClass(env);
        }
        ClassDefinition refc = field.getClassDefinition();
        UplevelReference r = refc.getReferencesFrozen();
        if (r != null) {
            r.willCodeArguments(env, ctx);
        }
        try {
            if (outerArg != null) {
                if (outerArg.type.isType(TC_VOID))
                    outerArg = outerArg.inline(env, ctx);
                else
                    outerArg = outerArg.inlineValue(env, ctx);
            }
            for (int i = 0 ; i < args.length ; i++) {
                args[i] = args[i].inlineValue(env, ctx);
            }
            if (false && env.opt() && field.isInlineable(env, false) &&
                (!ctx.field.isInitializer()) && ctx.field.isMethod() &&
                (ctx.getInlineMemberContext(field) == null)) {
                Statement s = (Statement)field.getValue(env);
                if ((s == null)
                    || (s.costInline(MAXINLINECOST, env, ctx) < MAXINLINECOST))  {
                    return inlineNewInstance(env, ctx, s);
                }
            }
        } catch (ClassNotFound e) {
            throw new CompilerError(e);
        }
        if (outerArg != null && outerArg.type.isType(TC_VOID)) {
            Expression e = outerArg;
            outerArg = null;
            return new CommaExpression(where, e, this);
        }
        return this;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (body != null) {
            return thresh;      
        }
        if (ctx == null) {
            return 2 + super.costInline(thresh, env, ctx);
        }
        ClassDefinition sourceClass = ctx.field.getClassDefinition();
        try {
            if (    sourceClass.permitInlinedAccess(env, field.getClassDeclaration())
                 && sourceClass.permitInlinedAccess(env, field)) {
                return 2 + super.costInline(thresh, env, ctx);
            }
        } catch (ClassNotFound e) {
        }
        return thresh;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        codeCommon(env, ctx, asm, false);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeCommon(env, ctx, asm, true);
    }
    private void codeCommon(Environment env, Context ctx, Assembler asm,
                            boolean forValue) {
        asm.add(where, opc_new, field.getClassDeclaration());
        if (forValue) {
            asm.add(where, opc_dup);
        }
        ClassDefinition refc = field.getClassDefinition();
        UplevelReference r = refc.getReferencesFrozen();
        if (r != null) {
            r.codeArguments(env, ctx, asm, where, field);
        }
        if (outerArg != null) {
            outerArg.codeValue(env, ctx, asm);
            switch (outerArg.op) {
            case THIS:
            case SUPER:
            case NEW:
                break;
            case FIELD: {
                MemberDefinition f = ((FieldExpression)outerArg).field;
                if (f != null && f.isNeverNull()) {
                    break;
                }
            }
            default:
                try {
                    ClassDefinition c = env.getClassDefinition(idJavaLangObject);
                    MemberDefinition getc = c.getFirstMatch(idGetClass);
                    asm.add(where, opc_dup);
                    asm.add(where, opc_invokevirtual, getc);
                    asm.add(where, opc_pop);
                } catch (ClassNotFound e) {
                }
            }
        }
        if (implMethod != null) {
            asm.add(where, opc_aconst_null);
        }
        for (int i = 0 ; i < args.length ; i++) {
            args[i].codeValue(env, ctx, asm);
        }
        asm.add(where, opc_invokespecial,
                ((implMethod != null) ? implMethod : field));
    }
}
