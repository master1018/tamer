class DeclarationStatement extends Statement {
    int mod;
    Expression type;
    Statement args[];
    public DeclarationStatement(long where, int mod, Expression type, Statement args[]) {
        super(DECLARATION, where);
        this.mod = mod;
        this.type = type;
        this.args = args;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        env.error(where, "invalid.decl");
        return checkBlockStatement(env, ctx, vset, exp);
    }
    Vset checkBlockStatement(Environment env, Context ctx, Vset vset, Hashtable exp) {
        if (labels != null) {
            env.error(where, "declaration.with.label", labels[0]);
        }
        vset = reach(env, vset);
        Type t = type.toType(env, ctx);
        for (int i = 0 ; i < args.length ; i++) {
            vset = args[i].checkDeclaration(env, ctx, vset, mod, t, exp);
        }
        return vset;
    }
    public Statement inline(Environment env, Context ctx) {
        int n = 0;
        for (int i = 0 ; i < args.length ; i++) {
            if ((args[i] = args[i].inline(env, ctx)) != null) {
                n++;
            }
        }
        return (n == 0) ? null : this;
    }
    public Statement copyInline(Context ctx, boolean valNeeded) {
        DeclarationStatement s = (DeclarationStatement)clone();
        if (type != null) {
            s.type = type.copyInline(ctx);
        }
        s.args = new Statement[args.length];
        for (int i = 0; i < args.length; i++){
            if (args[i] != null){
                s.args[i] = args[i].copyInline(ctx, valNeeded);
            }
        }
        return s;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 1;
        for (int i = 0; i < args.length; i++){
            if (args[i] != null){
                cost += args[i].costInline(thresh, env, ctx);
            }
        }
        return cost;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                args[i].code(env, ctx, asm);
            }
        }
    }
    public void print(PrintStream out, int indent) {
        out.print("declare ");
        super.print(out, indent);
        type.print(out);
        out.print(" ");
        for (int i = 0 ; i < args.length ; i++) {
            if (i > 0) {
                out.print(", ");
            }
            if (args[i] != null)  {
                args[i].print(out);
            } else {
                out.print("<empty>");
            }
        }
        out.print(";");
    }
}
