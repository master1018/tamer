class BinaryBitExpression extends BinaryExpression {
    public BinaryBitExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_BOOLEAN) != 0) {
            type = Type.tBoolean;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        left = convert(env, ctx, type, left);
        right = convert(env, ctx, type, right);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        left.codeValue(env, ctx, asm);
        right.codeValue(env, ctx, asm);
        codeOperation(env, ctx, asm);
    }
}
