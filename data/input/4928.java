class BinaryCompareExpression extends BinaryExpression {
    public BinaryCompareExpression(int op, long where, Expression left, Expression right) {
        super(op, where, Type.tBoolean, left, right);
    }
    void selectType(Environment env, Context ctx, int tm) {
        Type t = Type.tInt;
        if ((tm & TM_DOUBLE) != 0) {
            t = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            t = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            t = Type.tLong;
        }
        left = convert(env, ctx, t, left);
        right = convert(env, ctx, t, right);
    }
}
