class AndExpression extends BinaryLogicalExpression {
    public AndExpression(long where, Expression left, Expression right) {
        super(AND, where, left, right);
    }
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable exp, ConditionVars cvars) {
        left.checkCondition(env, ctx, vset, exp, cvars);
        left = convert(env, ctx, Type.tBoolean, left);
        Vset vsTrue = cvars.vsTrue.copy();
        Vset vsFalse = cvars.vsFalse.copy();
        right.checkCondition(env, ctx, vsTrue, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);
        cvars.vsFalse = cvars.vsFalse.join(vsFalse);
    }
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a && b);
    }
    Expression simplify() {
        if (left.equals(true)) {
            return right;
        }
        if (right.equals(false)) {
            return new CommaExpression(where, left, right).simplify();
        }
        if (right.equals(true)) {
            return left;
        }
        if (left.equals(false)) {
            return left;
        }
        return this;
    }
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (whenTrue) {
            Label lbl2 = new Label();
            left.codeBranch(env, ctx, asm, lbl2, false);
            right.codeBranch(env, ctx, asm, lbl, true);
            asm.add(lbl2);
        } else {
            left.codeBranch(env, ctx, asm, lbl, false);
            right.codeBranch(env, ctx, asm, lbl, false);
        }
    }
}
