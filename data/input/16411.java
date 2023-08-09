class RemainderExpression extends DivRemExpression {
    public RemainderExpression(long where, Expression left, Expression right) {
        super(REM, where, left, right);
    }
    Expression eval(int a, int b) {
        return new IntExpression(where, a % b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a % b);
    }
    Expression eval(float a, float b) {
        return new FloatExpression(where, a % b);
    }
    Expression eval(double a, double b) {
        return new DoubleExpression(where, a % b);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_irem + type.getTypeCodeOffset());
    }
}
