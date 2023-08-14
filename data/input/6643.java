class ShortExpression extends IntegerExpression {
    public ShortExpression(long where, short value) {
        super(SHORTVAL, where, Type.tShort, value);
    }
    public void print(PrintStream out) {
        out.print(value + "s");
    }
}
