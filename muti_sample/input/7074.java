class MultiplyExpression extends BinaryArithmeticExpression {
    public MultiplyExpression(long where, Expression left, Expression right) {
        super(MUL, where, left, right);
    }
    Expression eval(int a, int b) {
        return new IntExpression(where, a * b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a * b);
    }
    Expression eval(float a, float b) {
        return new FloatExpression(where, a * b);
    }
    Expression eval(double a, double b) {
        return new DoubleExpression(where, a * b);
    }
    Expression simplify() {
        if (left.equals(1)) {
            return right;
        }
        if (right.equals(1)) {
            return left;
        }
        return this;
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_imul + type.getTypeCodeOffset());
    }
}
