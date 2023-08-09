class NaryExpression extends UnaryExpression {
    Expression args[];
    NaryExpression(int op, long where, Type type, Expression right, Expression args[]) {
        super(op, where, type, right);
        this.args = args;
    }
    public Expression copyInline(Context ctx) {
        NaryExpression e = (NaryExpression)clone();
        if (right != null) {
            e.right = right.copyInline(ctx);
        }
        e.args = new Expression[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                e.args[i] = args[i].copyInline(ctx);
            }
        }
        return e;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 3;
        if (right != null)
            cost += right.costInline(thresh, env, ctx);
        for (int i = 0 ; (i < args.length) && (cost < thresh) ; i++) {
            if (args[i] != null) {
                cost += args[i].costInline(thresh, env, ctx);
            }
        }
        return cost;
    }
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + "#" + hashCode());
        if (right != null) {
            out.print(" ");
            right.print(out);
        }
        for (int i = 0 ; i < args.length ; i++) {
            out.print(" ");
            if (args[i] != null) {
                args[i].print(out);
            } else {
                out.print("<null>");
            }
        }
        out.print(")");
    }
}
