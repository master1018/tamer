class BinaryLogicalExpression extends BinaryExpression {
    public BinaryLogicalExpression(int op, long where, Expression left, Expression right) {
        super(op, where, Type.tBoolean, left, right);
    }
    public Vset checkValue(Environment env, Context ctx,
                           Vset vset, Hashtable exp) {
        ConditionVars cvars = new ConditionVars();
        checkCondition(env, ctx, vset, exp, cvars);
        return cvars.vsTrue.join(cvars.vsFalse);
    }
    abstract
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable exp, ConditionVars cvars);
    public Expression inline(Environment env, Context ctx) {
        left = left.inlineValue(env, ctx);
        right = right.inlineValue(env, ctx);
        return this;
    }
}
