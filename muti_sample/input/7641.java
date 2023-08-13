class BinaryArithmeticExpression extends BinaryExpression {
    public BinaryArithmeticExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
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
        left = convert(env, ctx, type, left);
        right = convert(env, ctx, type, right);
    }
}
