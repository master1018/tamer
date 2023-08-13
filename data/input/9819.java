class DivRemExpression extends BinaryArithmeticExpression {
    public DivRemExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left, right);
    }
    public Expression inline(Environment env, Context ctx) {
        if (type.inMask(TM_INTEGER)) {
            right = right.inlineValue(env, ctx);
            if (right.isConstant() && !right.equals(0)) {
                left = left.inline(env, ctx);
                return left;
            } else {
                left = left.inlineValue(env, ctx);
                try {
                    return eval().simplify();
                } catch (ArithmeticException e) {
                    env.error(where, "arithmetic.exception");
                    return this;
                }
            }
        } else {
            return super.inline(env, ctx);
        }
    }
}
