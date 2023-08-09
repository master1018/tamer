class CaseStatement extends Statement {
    Expression expr;
    public CaseStatement(long where, Expression expr) {
        super(CASE, where);
        this.expr = expr;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        if (expr != null) {
            expr.checkValue(env, ctx, vset, exp);
            expr = convert(env, ctx, Type.tInt, expr);
            expr = expr.inlineValue(env, ctx);
        }
        return vset.clearDeadEnd();
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 6;
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        if (expr == null) {
            out.print("default");
        } else {
            out.print("case ");
            expr.print(out);
        }
        out.print(":");
    }
}
