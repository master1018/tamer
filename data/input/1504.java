class DoStatement extends Statement {
    Statement body;
    Expression cond;
    public DoStatement(long where, Statement body, Expression cond) {
        super(DO, where);
        this.body = body;
        this.cond = cond;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env,ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        Vset vsEntry = vset.copy();
        vset = body.check(env, newctx, reach(env, vset), exp);
        vset = vset.join(newctx.vsContinue);
        ConditionVars cvars =
            cond.checkCondition(env, newctx, vset, exp);
        cond = convert(env, newctx, Type.tBoolean, cond);
        ctx.checkBackBranch(env, this, vsEntry, cvars.vsTrue);
        vset = newctx.vsBreak.join(cvars.vsFalse);
        return ctx.removeAdditionalVars(vset);
    }
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        if (body != null) {
            body = body.inline(env, ctx);
        }
        cond = cond.inlineValue(env, ctx);
        return this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        DoStatement s = (DoStatement)clone();
        s.cond = cond.copyInline(ctx);
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + cond.costInline(thresh, env, ctx)
                + ((body != null) ? body.costInline(thresh, env, ctx) : 0);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        Label l1 = new Label();
        asm.add(l1);
        CodeContext newctx = new CodeContext(ctx, this);
        if (body != null) {
            body.code(env, newctx, asm);
        }
        asm.add(newctx.contLabel);
        cond.codeBranch(env, newctx, asm, l1, true);
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("do ");
        body.print(out, indent);
        out.print(" while ");
        cond.print(out);
        out.print(";");
    }
}
