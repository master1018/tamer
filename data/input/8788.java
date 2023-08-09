class ExpressionStatement extends Statement {
    Expression expr;
    public ExpressionStatement(long where, Expression expr) {
        super(EXPRESSION, where);
        this.expr = expr;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        return expr.check(env, ctx, reach(env, vset), exp);
    }
    public Statement inline(Environment env, Context ctx) {
        if (expr != null) {
            expr = expr.inline(env, ctx);
            return (expr == null) ? null : this;
        }
        return null;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        ExpressionStatement s = (ExpressionStatement)clone();
        s.expr = expr.copyInline(ctx);
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return expr.costInline(thresh, env, ctx);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        expr.code(env, ctx, asm);
    }
    public Expression firstConstructor() {
        return expr.firstConstructor();
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        if (expr != null) {
            expr.print(out);
        } else {
            out.print("<empty>");
        }
        out.print(";");
    }
}
