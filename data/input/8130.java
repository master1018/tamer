class CatchStatement extends Statement {
    int mod;
    Expression texpr;
    Identifier id;
    Statement body;
    LocalMember field;
    public CatchStatement(long where, Expression texpr, IdentifierToken id, Statement body) {
        super(CATCH, where);
        this.mod = id.getModifiers();
        this.texpr = texpr;
        this.id = id.getName();
        this.body = body;
    }
    @Deprecated
    public CatchStatement(long where, Expression texpr, Identifier id, Statement body) {
        super(CATCH, where);
        this.texpr = texpr;
        this.id = id;
        this.body = body;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = reach(env, vset);
        ctx = new Context(ctx, this);
        Type type = texpr.toType(env, ctx);
        try {
            if (ctx.getLocalField(id) != null) {
                env.error(where, "local.redefined", id);
            }
            if (type.isType(TC_ERROR)) {
            } else if (!type.isType(TC_CLASS)) {
                env.error(where, "catch.not.throwable", type);
            } else {
                ClassDefinition def = env.getClassDefinition(type);
                if (!def.subClassOf(env,
                               env.getClassDeclaration(idJavaLangThrowable))) {
                    env.error(where, "catch.not.throwable", def);
                }
            }
            field = new LocalMember(where, ctx.field.getClassDefinition(), mod, type, id);
            ctx.declare(env, field);
            vset.addVar(field.number);
            return body.check(env, ctx, vset, exp);
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, opNames[op]);
            return vset;
        }
    }
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        if (field.isUsed()) {
            ctx.declare(env, field);
        }
        if (body != null) {
            body = body.inline(env, ctx);
        }
        return this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        CatchStatement s = (CatchStatement)clone();
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        if (field != null) {
            s.field = field.copyInline(ctx);
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx){
        int cost = 1;
        if (body != null) {
            cost += body.costInline(thresh, env,ctx);
        }
        return cost;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        if (field.isUsed()) {
            newctx.declare(env, field);
            asm.add(where, opc_astore, new LocalVariable(field, field.number));
        } else {
            asm.add(where, opc_pop);
        }
        if (body != null) {
            body.code(env, newctx, asm);
        }
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("catch (");
        texpr.print(out);
        out.print(" " + id + ") ");
        if (body != null) {
            body.print(out, indent);
        } else {
            out.print("<empty>");
        }
    }
}
