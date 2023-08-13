class NullExpression extends ConstantExpression {
    public NullExpression(long where) {
        super(NULL, where, Type.tNull);
    }
    public boolean equals(int i) {
        return i == 0;
    }
    public boolean isNull() {
        return true;
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_aconst_null);
    }
    public void print(PrintStream out) {
        out.print("null");
    }
}
