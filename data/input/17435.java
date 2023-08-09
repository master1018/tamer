class ByteExpression extends IntegerExpression {
    public ByteExpression(long where, byte value) {
        super(BYTEVAL, where, Type.tByte, value);
    }
    public void print(PrintStream out) {
        out.print(value + "b");
    }
}
