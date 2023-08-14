class OrExpression extends BinaryLogicalExpression {
    public OrExpression(long where, Expression left, Expression right) {
        super(OR, where, left, right);
    }
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable exp, ConditionVars cvars) {
        left.checkCondition(env, ctx, vset, exp, cvars);
        left = convert(env, ctx, Type.tBoolean, left);
        Vset vsTrue = cvars.vsTrue.copy();
        Vset vsFalse = cvars.vsFalse.copy();
        right.checkCondition(env, ctx, vsFalse, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);
        cvars.vsTrue = cvars.vsTrue.join(vsTrue);
    }
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a || b);
    }
    Expression simplify() {
        if (right.equals(false)) {
            return left;
        }
        if (left.equals(true)) {
            return left;
        }
        if (left.equals(false)) {
            return right;
        }
        if (right.equals(true)) {
            return new CommaExpression(where, left, right).simplify();
        }
        return this;
    }
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (whenTrue) {
            left.codeBranch(env, ctx, asm, lbl, true);
            right.codeBranch(env, ctx, asm, lbl, true);
        } else {
            Label lbl2 = new Label();
            left.codeBranch(env, ctx, asm, lbl2, true);
            right.codeBranch(env, ctx, asm, lbl, false);
            asm.add(lbl2);
        }
    }
}
