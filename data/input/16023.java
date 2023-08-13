class SubtractExpression extends BinaryArithmeticExpression {
    public SubtractExpression(long where, Expression left, Expression right) {
        super(SUB, where, left, right);
    }
    Expression eval(int a, int b) {
        return new IntExpression(where, a - b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a - b);
    }
    Expression eval(float a, float b) {
        return new FloatExpression(where, a - b);
    }
    Expression eval(double a, double b) {
        return new DoubleExpression(where, a - b);
    }
    Expression simplify() {
        if (type.inMask(TM_INTEGER)) {
            if (left.equals(0)) {
                return new NegativeExpression(where, right);
            }
            if (right.equals(0)) {
                return left;
            }
        }
        return this;
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_isub + type.getTypeCodeOffset());
    }
}
