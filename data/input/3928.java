class ShiftRightExpression extends BinaryShiftExpression {
    public ShiftRightExpression(long where, Expression left, Expression right) {
        super(RSHIFT, where, left, right);
    }
    Expression eval(int a, int b) {
        return new IntExpression(where, a >> b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a >> b);
    }
    Expression simplify() {
        if (right.equals(0)) {
            return left;
        }
        if (left.equals(0)) {
            return new CommaExpression(where, right, left).simplify();
        }
        return this;
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ishr + type.getTypeCodeOffset());
    }
}
