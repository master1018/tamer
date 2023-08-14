class StringExpression extends ConstantExpression {
    String value;
    public StringExpression(long where, String value) {
        super(STRINGVAL, where, Type.tString);
        this.value = value;
    }
    public boolean equals(String s) {
        return value.equals(s);
    }
    public boolean isNonNull() {
        return true;            
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, this);
    }
    public Object getValue() {
        return value;
    }
    public int hashCode() {
        return value.hashCode() ^ 3213;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof StringExpression)) {
            return value.equals(((StringExpression)obj).value);
        }
        return false;
    }
    public void print(PrintStream out) {
        out.print("\"" + value + "\"");
    }
}
