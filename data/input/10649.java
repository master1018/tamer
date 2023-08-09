class CompoundStatement extends Statement {
    Statement args[];
    public CompoundStatement(long where, Statement args[]) {
        super(STAT, where);
        this.args = args;
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] == null) {
                args[i] = new CompoundStatement(where, new Statement[0]);
            }
        }
    }
    public void insertStatement(Statement s) {
        Statement newargs[] = new Statement[1+args.length];
        newargs[0] = s;
        for (int i = 0 ; i < args.length ; i++) {
            newargs[i+1] = args[i];
        }
        this.args = newargs;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        if (args.length > 0) {
            vset = reach(env, vset);
            CheckContext newctx = new CheckContext(ctx, this);
            Environment newenv = Context.newEnvironment(env, newctx);
            for (int i = 0 ; i < args.length ; i++) {
                vset = args[i].checkBlockStatement(newenv, newctx, vset, exp);
            }
            vset = vset.join(newctx.vsBreak);
        }
        return ctx.removeAdditionalVars(vset);
    }
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        boolean expand = false;
        int count = 0;
        for (int i = 0 ; i < args.length ; i++) {
            Statement s = args[i];
            if (s != null) {
                if ((s = s.inline(env, ctx)) != null) {
                    if ((s.op == STAT) && (s.labels == null)) {
                        count += ((CompoundStatement)s).args.length;
                    } else {
                        count++;
                    }
                    expand = true;
                }
                args[i] = s;
            }
        }
        switch (count) {
          case 0:
            return null;
          case 1:
            for (int i = args.length ; i-- > 0 ;) {
                if (args[i] != null) {
                    return eliminate(env, args[i]);
                }
            }
            break;
        }
        if (expand || (count != args.length)) {
            Statement newArgs[] = new Statement[count];
            for (int i = args.length ; i-- > 0 ;) {
                Statement s = args[i];
                if (s != null) {
                    if ((s.op == STAT) && (s.labels == null)) {
                        Statement a[] = ((CompoundStatement)s).args;
                        for (int j = a.length ; j-- > 0 ; ) {
                            newArgs[--count] = a[j];
                        }
                    } else {
                        newArgs[--count] = s;
                    }
                }
            }
            args = newArgs;
        }
        return this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        CompoundStatement s = (CompoundStatement)clone();
        s.args = new Statement[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            s.args[i] = args[i].copyInline(ctx, valNeeded);
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 0;
        for (int i = 0 ; (i < args.length) && (cost < thresh) ; i++) {
            cost += args[i].costInline(thresh, env, ctx);
        }
        return cost;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        for (int i = 0 ; i < args.length ; i++) {
            args[i].code(env, newctx, asm);
        }
        asm.add(newctx.breakLabel);
    }
    public Expression firstConstructor() {
        return (args.length > 0) ? args[0].firstConstructor() : null;
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("{\n");
        for (int i = 0 ; i < args.length ; i++) {
            printIndent(out, indent+1);
            if (args[i] != null) {
                args[i].print(out, indent + 1);
            } else {
                out.print("<empty>");
            }
            out.print("\n");
        }
        printIndent(out, indent);
        out.print("}");
    }
}
