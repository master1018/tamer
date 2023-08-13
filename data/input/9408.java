class PositiveExpression extends UnaryExpression {
    public PositiveExpression(long where, Expression right) {
        super(POS, where, right.type, right);
    }
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_DOUBLE) != 0) {
            type = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            type = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        right = convert(env, ctx, type, right);
    }
    Expression simplify() {
        return right;
    }
}
