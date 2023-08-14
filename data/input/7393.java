class IdentifierExpression extends Expression {
    Identifier id;
    MemberDefinition field;
    Expression implementation;
    public IdentifierExpression(long where, Identifier id) {
        super(IDENT, where, Type.tError);
        this.id = id;
    }
    public IdentifierExpression(IdentifierToken id) {
        this(id.getWhere(), id.getName());
    }
    public IdentifierExpression(long where, MemberDefinition field) {
        super(IDENT, where, field.getType());
        this.id = field.getName();
        this.field = field;
    }
    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }
    public boolean equals(Identifier id) {
        return this.id.equals(id);
    }
    private Vset assign(Environment env, Context ctx, Vset vset) {
        if (field.isLocal()) {
            LocalMember local = (LocalMember)field;
            if (local.scopeNumber < ctx.frameNumber) {
                env.error(where, "assign.to.uplevel", id);
            }
            if (local.isFinal()) {
                if (!local.isBlankFinal()) {
                    env.error(where, "assign.to.final", id);
                } else if (!vset.testVarUnassigned(local.number)) {
                    env.error(where, "assign.to.blank.final", id);
                }
            }
            vset.addVar(local.number);
            local.writecount++;
        } else if (field.isFinal()) {
            vset = FieldExpression.checkFinalAssign(env, ctx, vset,
                                                    where, field);
        }
        return vset;
    }
    private Vset get(Environment env, Context ctx, Vset vset) {
        if (field.isLocal()) {
            LocalMember local = (LocalMember)field;
            if (local.scopeNumber < ctx.frameNumber && !local.isFinal()) {
                env.error(where, "invalid.uplevel", id);
            }
            if (!vset.testVar(local.number)) {
                env.error(where, "var.not.initialized", id);
                vset.addVar(local.number);
            }
            local.readcount++;
        } else {
            if (!field.isStatic()) {
                if (!vset.testVar(ctx.getThisNumber())) {
                    env.error(where, "access.inst.before.super", id);
                    implementation = null;
                }
            }
            if (field.isBlankFinal()) {
                int number = ctx.getFieldNumber(field);
                if (number >= 0 && !vset.testVar(number)) {
                    env.error(where, "var.not.initialized", id);
                }
            }
        }
        return vset;
    }
    boolean bind(Environment env, Context ctx) {
        try {
            field = ctx.getField(env, id);
            if (field == null) {
                for (ClassDefinition cdef = ctx.field.getClassDefinition();
                     cdef != null; cdef = cdef.getOuterClass()) {
                    if (cdef.findAnyMethod(env, id) != null) {
                        env.error(where, "invalid.var", id,
                                  ctx.field.getClassDeclaration());
                        return false;
                    }
                }
                env.error(where, "undef.var", id);
                return false;
            }
            type = field.getType();
            if (!ctx.field.getClassDefinition().canAccess(env, field)) {
                env.error(where, "no.field.access",
                          id, field.getClassDeclaration(),
                          ctx.field.getClassDeclaration());
                return false;
            }
            if (field.isLocal()) {
                LocalMember local = (LocalMember)field;
                if (local.scopeNumber < ctx.frameNumber) {
                    implementation = ctx.makeReference(env, local);
                }
            } else {
                MemberDefinition f = field;
                if (f.reportDeprecated(env)) {
                    env.error(where, "warn.field.is.deprecated",
                              id, f.getClassDefinition());
                }
                ClassDefinition fclass = f.getClassDefinition();
                if (fclass != ctx.field.getClassDefinition()) {
                    MemberDefinition f2 = ctx.getApparentField(env, id);
                    if (f2 != null && f2 != f) {
                        ClassDefinition c = ctx.findScope(env, fclass);
                        if (c == null)  c = f.getClassDefinition();
                        if (f2.isLocal()) {
                            env.error(where, "inherited.hides.local",
                                      id, c.getClassDeclaration());
                        } else {
                            env.error(where, "inherited.hides.field",
                                      id, c.getClassDeclaration(),
                                      f2.getClassDeclaration());
                        }
                    }
                }
                if (f.isStatic()) {
                    Expression base = new TypeExpression(where,
                                        f.getClassDeclaration().getType());
                    implementation = new FieldExpression(where, null, f);
                } else {
                    Expression base = ctx.findOuterLink(env, where, f);
                    if (base != null) {
                        implementation = new FieldExpression(where, base, f);
                    }
                }
            }
            if (!ctx.canReach(env, field)) {
                env.error(where, "forward.ref",
                          id, field.getClassDeclaration());
                return false;
            }
            return true;
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, ctx.field);
        } catch (AmbiguousMember e) {
            env.error(where, "ambig.field", id,
                      e.field1.getClassDeclaration(),
                      e.field2.getClassDeclaration());
        }
        return false;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        if (field != null) {
            return vset;
        }
        if (bind(env, ctx)) {
            vset = get(env, ctx, vset);
            ctx.field.getClassDefinition().addDependency(field.getClassDeclaration());
            if (implementation != null)
                vset = implementation.checkValue(env, ctx, vset, exp);
        }
        return vset;
    }
    public Vset checkLHS(Environment env, Context ctx,
                         Vset vset, Hashtable exp) {
        if (!bind(env, ctx))
            return vset;
        vset = assign(env, ctx, vset);
        if (implementation != null)
            vset = implementation.checkValue(env, ctx, vset, exp);
        return vset;
    }
    public Vset checkAssignOp(Environment env, Context ctx,
                              Vset vset, Hashtable exp, Expression outside) {
        if (!bind(env, ctx))
            return vset;
        vset = assign(env, ctx, get(env, ctx, vset));
        if (implementation != null)
            vset = implementation.checkValue(env, ctx, vset, exp);
        return vset;
    }
    public FieldUpdater getAssigner(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.getAssigner(env, ctx);
        return null;
    }
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.getUpdater(env, ctx);
        return null;
    }
    public Vset checkAmbigName(Environment env, Context ctx, Vset vset, Hashtable exp,
                               UnaryExpression loc) {
        try {
            if (ctx.getField(env, id) != null) {
                return checkValue(env, ctx, vset, exp);
            }
        } catch (ClassNotFound ee) {
        } catch (AmbiguousMember ee) {
        }
        ClassDefinition c = toResolvedType(env, ctx, true);
        if (c != null) {
            loc.right = new TypeExpression(where, c.getType());
            return vset;
        }
        type = Type.tPackage;
        return vset;
    }
    private ClassDefinition toResolvedType(Environment env, Context ctx,
                                           boolean pkgOK) {
        Identifier rid = ctx.resolveName(env, id);
        Type t = Type.tClass(rid);
        if (pkgOK && !env.classExists(t)) {
            return null;
        }
        if (env.resolve(where, ctx.field.getClassDefinition(), t)) {
            try {
                ClassDefinition c = env.getClassDefinition(t);
                if (c.isMember()) {
                    ClassDefinition sc = ctx.findScope(env, c.getOuterClass());
                    if (sc != c.getOuterClass()) {
                        Identifier rid2 = ctx.getApparentClassName(env, id);
                        if (!rid2.equals(idNull) && !rid2.equals(rid)) {
                            env.error(where, "inherited.hides.type",
                                      id, sc.getClassDeclaration());
                        }
                    }
                }
                if (!c.getLocalName().equals(id.getFlatName().getName())) {
                    env.error(where, "illegal.mangled.name", id, c);
                }
                return c;
            } catch (ClassNotFound ee) {
            }
        }
        return null;
    }
    Type toType(Environment env, Context ctx) {
        ClassDefinition c = toResolvedType(env, ctx, false);
        if (c != null) {
            return c.getType();
        }
        return Type.tError;
    }
    public boolean isConstant() {
        if (implementation != null)
            return implementation.isConstant();
        if (field != null) {
            return field.isConstant();
        }
        return false;
    }
    public Expression inline(Environment env, Context ctx) {
        return null;
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        if (field == null) {
            return this;
        }
        try {
            if (field.isLocal()) {
                if (field.isInlineable(env, false)) {
                    Expression e = (Expression)field.getValue(env);
                    return (e == null) ? this : e.inlineValue(env, ctx);
                }
                return this;
            }
            return this;
        } catch (ClassNotFound e) {
            throw new CompilerError(e);
        }
    }
    public Expression inlineLHS(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineLHS(env, ctx);
        return this;
    }
    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        IdentifierExpression e =
            (IdentifierExpression)super.copyInline(ctx);
        if (field != null && field.isLocal()) {
            e.field = ((LocalMember)field).getCurrentInlineCopy(ctx);
        }
        return e;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (implementation != null)
            return implementation.costInline(thresh, env, ctx);
        return super.costInline(thresh, env, ctx);
    }
    int codeLValue(Environment env, Context ctx, Assembler asm) {
        return 0;
    }
    void codeLoad(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_iload + type.getTypeCodeOffset(),
                new Integer(((LocalMember)field).number));
    }
    void codeStore(Environment env, Context ctx, Assembler asm) {
        LocalMember local = (LocalMember)field;
        asm.add(where, opc_istore + type.getTypeCodeOffset(),
                new LocalVariable(local, local.number));
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeLValue(env, ctx, asm);
        codeLoad(env, ctx, asm);
    }
    public void print(PrintStream out) {
        out.print(id + "#" + ((field != null) ? field.hashCode() : 0));
        if (implementation != null) {
            out.print("/IMPL=");
            implementation.print(out);
        }
    }
}
