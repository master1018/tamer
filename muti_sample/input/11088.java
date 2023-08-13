class LongExpression extends ConstantExpression {
    long value;
    public LongExpression(long where, long value) {
        super(LONGVAL, where, Type.tLong);
        this.value = value;
    }
    public Object getValue() {
        return new Long(value);
    }
    public boolean equals(int i) {
        return value == i;
    }
    public boolean equalsDefault() {
        return value == 0;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc2_w, new Long(value));
    }
    public void print(PrintStream out) {
        out.print(value + "L");
    }
}
