class VarDeclarationStatement extends Statement {
    LocalMember field;
    Expression expr;
    public VarDeclarationStatement(long where, Expression expr) {
        super(VARDECLARATION, where);
        this.expr = expr;
    }
    public VarDeclarationStatement(long where, LocalMember field, Expression expr) {
        super(VARDECLARATION, where);
        this.field = field;
        this.expr = expr;
    }
    Vset checkDeclaration(Environment env, Context ctx, Vset vset, int mod, Type t, Hashtable exp) {
        if (labels != null) {
            env.error(where, "declaration.with.label", labels[0]);
        }
        if (field != null) {
            if (ctx.getLocalClass(field.getName()) != null
                && field.isInnerClass()) {
                env.error(where, "local.class.redefined", field.getName());
            }
            ctx.declare(env, field);
            if (field.isInnerClass()) {
                ClassDefinition body = field.getInnerClass();
                try {
                    vset = body.checkLocalClass(env, ctx, vset,
                                                null, null, null);
                } catch (ClassNotFound ee) {
                    env.error(where, "class.not.found", ee.name, opNames[op]);
                }
                return vset;
            }
            vset.addVar(field.number);
            return (expr != null) ? expr.checkValue(env, ctx, vset, exp) : vset;
        }
        Expression e = expr;
        if (e.op == ASSIGN) {
            expr = ((AssignExpression)e).right;
            e = ((AssignExpression)e).left;
        } else {
            expr = null;
        }
        boolean declError = t.isType(TC_ERROR);
        while (e.op == ARRAYACCESS) {
            ArrayAccessExpression array = (ArrayAccessExpression)e;
            if (array.index != null) {
                env.error(array.index.where, "array.dim.in.type");
                declError = true;
            }
            e = array.right;
            t = Type.tArray(t);
        }
        if (e.op == IDENT) {
            Identifier id = ((IdentifierExpression)e).id;
            if (ctx.getLocalField(id) != null) {
                env.error(where, "local.redefined", id);
            }
            field = new LocalMember(e.where, ctx.field.getClassDefinition(), mod, t, id);
            ctx.declare(env, field);
            if (expr != null) {
                vset = expr.checkInitializer(env, ctx, vset, t, exp);
                expr = convert(env, ctx, t, expr);
                field.setValue(expr); 
                if (field.isConstant()) {
                    field.addModifiers(M_INLINEABLE);
                }
                vset.addVar(field.number);
            } else if (declError) {
                vset.addVar(field.number);
            } else {
                vset.addVarUnassigned(field.number);
            }
            return vset;
        }
        env.error(e.where, "invalid.decl");
        return vset;
    }
    public Statement inline(Environment env, Context ctx) {
        if (field.isInnerClass()) {
            ClassDefinition body = field.getInnerClass();
            body.inlineLocalClass(env);
            return null;
        }
        if (env.opt() && !field.isUsed()) {
            return new ExpressionStatement(where, expr).inline(env, ctx);
        }
        ctx.declare(env, field);
        if (expr != null) {
            expr = expr.inlineValue(env, ctx);
            field.setValue(expr); 
            if (env.opt() && (field.writecount == 0)) {
                if (expr.op == IDENT) {
                    IdentifierExpression e = (IdentifierExpression)expr;
                    if (e.field.isLocal() && ((ctx = ctx.getInlineContext()) != null) &&
                        (((LocalMember)e.field).number < ctx.varNumber)) {
                        field.setValue(expr);
                        field.addModifiers(M_INLINEABLE);
                    }
                }
                if (expr.isConstant() || (expr.op == THIS) || (expr.op == SUPER)) {
                    field.setValue(expr);
                    field.addModifiers(M_INLINEABLE);
                }
            }
        }
        return this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        VarDeclarationStatement s = (VarDeclarationStatement)clone();
        if (expr != null) {
            s.expr = expr.copyInline(ctx);
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (field != null && field.isInnerClass()) {
            return thresh;      
        }
        return (expr != null) ? expr.costInline(thresh, env, ctx) : 0;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        if (expr != null && !expr.type.isType(TC_VOID)) {
            ctx.declare(env, field);
            expr.codeValue(env, ctx, asm);
            asm.add(where, opc_istore + field.getType().getTypeCodeOffset(),
                    new LocalVariable(field, field.number));
        } else {
            ctx.declare(env, field);
            if (expr != null) {
                expr.code(env, ctx, asm);
            }
        }
    }
    public void print(PrintStream out, int indent) {
        out.print("local ");
        if (field != null) {
            out.print(field + "#" + field.hashCode());
            if (expr != null) {
                out.print(" = ");
                expr.print(out);
            }
        } else {
            expr.print(out);
            out.print(";");
        }
    }
}
