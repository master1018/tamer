class ExprExpression extends UnaryExpression {
    public ExprExpression(long where, Expression right) {
        super(EXPR, where, right.type, right);
    }
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable exp, ConditionVars cvars) {
        right.checkCondition(env, ctx, vset, exp, cvars);
        type = right.type;
    }
    public Vset checkAssignOp(Environment env, Context ctx,
                              Vset vset, Hashtable exp, Expression outside) {
        vset = right.checkAssignOp(env, ctx, vset, exp, outside);
        type = right.type;
        return vset;
    }
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return right.getUpdater(env, ctx);
    }
    public boolean isNull() {
        return right.isNull();
    }
    public boolean isNonNull() {
        return right.isNonNull();
    }
    public Object getValue() {
        return right.getValue();
    }
    protected StringBuffer inlineValueSB(Environment env,
                                         Context ctx,
                                         StringBuffer buffer) {
        return right.inlineValueSB(env, ctx, buffer);
    }
    void selectType(Environment env, Context ctx, int tm) {
        type = right.type;
    }
    Expression simplify() {
        return right;
    }
}
