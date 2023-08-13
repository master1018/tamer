class BitXorExpression extends BinaryBitExpression {
    public BitXorExpression(long where, Expression left, Expression right) {
        super(BITXOR, where, left, right);
    }
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a ^ b);
    }
    Expression eval(int a, int b) {
        return new IntExpression(where, a ^ b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a ^ b);
    }
    Expression simplify() {
        if (left.equals(true)) {
            return new NotExpression(where, right);
        }
        if (right.equals(true)) {
            return new NotExpression(where, left);
        }
        if (left.equals(false) || left.equals(0)) {
            return right;
        }
        if (right.equals(false) || right.equals(0)) {
            return left;
        }
        return this;
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ixor + type.getTypeCodeOffset());
    }
}
