class ForStatement extends Statement {
    Statement init;
    Expression cond;
    Expression inc;
    Statement body;
    public ForStatement(long where, Statement init, Expression cond, Expression inc, Statement body) {
        super(FOR, where);
        this.init = init;
        this.cond = cond;
        this.inc = inc;
        this.body = body;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        vset = reach(env, vset);
        Context initctx = new Context(ctx, this);
        if (init != null) {
            vset = init.checkBlockStatement(env, initctx, vset, exp);
        }
        CheckContext newctx = new CheckContext(initctx, this);
        Vset vsEntry = vset.copy();
        ConditionVars cvars;
        if (cond != null) {
            cvars = cond.checkCondition(env, newctx, vset, exp);
            cond = convert(env, newctx, Type.tBoolean, cond);
        } else {
            cvars = new ConditionVars();
            cvars.vsFalse = Vset.DEAD_END;
            cvars.vsTrue = vset;
        }
        vset = body.check(env, newctx, cvars.vsTrue, exp);
        vset = vset.join(newctx.vsContinue);
        if (inc != null) {
            vset = inc.check(env, newctx, vset, exp);
        }
        initctx.checkBackBranch(env, this, vsEntry, vset);
        vset = newctx.vsBreak.join(cvars.vsFalse);
        return ctx.removeAdditionalVars(vset);
    }
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        if (init != null) {
            Statement body[] = {init, this};
            init = null;
            return new CompoundStatement(where, body).inline(env, ctx);
        }
        if (cond != null) {
            cond = cond.inlineValue(env, ctx);
        }
        if (body != null) {
            body = body.inline(env, ctx);
        }
        if (inc != null) {
            inc = inc.inline(env, ctx);
        }
        return this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        ForStatement s = (ForStatement)clone();
        if (init != null) {
            s.init = init.copyInline(ctx, valNeeded);
        }
        if (cond != null) {
            s.cond = cond.copyInline(ctx);
        }
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        if (inc != null) {
            s.inc = inc.copyInline(ctx);
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 2;
        if (init != null) {
            cost += init.costInline(thresh, env, ctx);
        }
        if (cond != null) {
            cost += cond.costInline(thresh, env, ctx);
        }
        if (body != null) {
            cost += body.costInline(thresh, env, ctx);
        }
        if (inc != null) {
            cost += inc.costInline(thresh, env, ctx);
        }
        return cost;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        if (init != null) {
            init.code(env, newctx, asm);
        }
        Label l1 = new Label();
        Label l2 = new Label();
        asm.add(where, opc_goto, l2);
        asm.add(l1);
        if (body != null) {
            body.code(env, newctx, asm);
        }
        asm.add(newctx.contLabel);
        if (inc != null) {
            inc.code(env, newctx, asm);
        }
        asm.add(l2);
        if (cond != null) {
            cond.codeBranch(env, newctx, asm, l1, true);
        } else {
            asm.add(where, opc_goto, l1);
        }
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("for (");
        if (init != null) {
            init.print(out, indent);
            out.print(" ");
        } else {
            out.print("; ");
        }
        if (cond != null) {
            cond.print(out);
            out.print(" ");
        }
        out.print("; ");
        if (inc != null) {
            inc.print(out);
        }
        out.print(") ");
        if (body != null) {
            body.print(out, indent);
        } else {
            out.print(";");
        }
    }
}
