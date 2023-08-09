class TypeExpression extends Expression {
    public TypeExpression(long where, Type type) {
        super(TYPE, where, type);
    }
    Type toType(Environment env, Context ctx) {
        return type;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        env.error(where, "invalid.term");
        type = Type.tError;
        return vset;
    }
    public Vset checkAmbigName(Environment env, Context ctx, Vset vset, Hashtable exp,
                               UnaryExpression loc) {
        return vset;
    }
    public Expression inline(Environment env, Context ctx) {
        return null;
    }
    public void print(PrintStream out) {
        out.print(type.toString());
    }
}
