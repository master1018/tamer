class BinaryShiftExpression extends BinaryExpression {
    public BinaryShiftExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }
    Expression eval() {
        if (left.op == LONGVAL && right.op == INTVAL) {
            return eval(((LongExpression)left).value,
                        ((IntExpression)right).value);
        }
        return super.eval();
    }
    void selectType(Environment env, Context ctx, int tm) {
        if (left.type == Type.tLong) {
            type = Type.tLong;
        } else if (left.type.inMask(TM_INTEGER)) {
            type = Type.tInt;
            left = convert(env, ctx, type, left);
        } else {
            type = Type.tError;
        }
        if (right.type.inMask(TM_INTEGER)) {
            right = new ConvertExpression(where, Type.tInt, right);
        } else {
            right = convert(env, ctx, Type.tInt, right);
        }
    }
}
