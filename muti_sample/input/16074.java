class DoubleExpression extends ConstantExpression {
    double value;
    public DoubleExpression(long where, double value) {
        super(DOUBLEVAL, where, Type.tDouble);
        this.value = value;
    }
    public Object getValue() {
        return new Double(value);
    }
    public boolean equals(int i) {
        return value == i;
    }
    public boolean equalsDefault() {
        return (Double.doubleToLongBits(value) == 0);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc2_w, new Double(value));
    }
    public void print(PrintStream out) {
        out.print(value + "D");
    }
}
