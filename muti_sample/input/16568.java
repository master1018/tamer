class SuperExpression extends ThisExpression {
    public SuperExpression(long where) {
        super(SUPER, where);
    }
    public SuperExpression(long where, Expression outerArg) {
        super(where, outerArg);
        op = SUPER;
    }
    public SuperExpression(long where, Context ctx) {
        super(where, ctx);
        op = SUPER;
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = checkCommon(env, ctx, vset, exp);
        if (type != Type.tError) {
            env.error(where, "undef.var.super", idSuper);
        }
        return vset;
    }
    public Vset checkAmbigName(Environment env, Context ctx,
                               Vset vset, Hashtable exp,
                               UnaryExpression loc) {
        return checkCommon(env, ctx, vset, exp);
    }
    private Vset checkCommon(Environment env, Context ctx, Vset vset, Hashtable exp) {
        ClassDeclaration superClass = ctx.field.getClassDefinition().getSuperClass();
        if (superClass == null) {
            env.error(where, "undef.var", idSuper);
            type = Type.tError;
            return vset;
        }
        vset = super.checkValue(env, ctx, vset, exp);
        type = superClass.getType();
        return vset;
    }
}
