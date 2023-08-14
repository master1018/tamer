class FloatExpression extends ConstantExpression {
    float value;
    public FloatExpression(long where, float value) {
        super(FLOATVAL, where, Type.tFloat);
        this.value = value;
    }
    public Object getValue() {
        return new Float(value);
    }
    public boolean equals(int i) {
        return value == i;
    }
    public boolean equalsDefault() {
        return (Float.floatToIntBits(value) == 0);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, new Float(value));
    }
    public void print(PrintStream out) {
        out.print(value +"F");
    }
}
