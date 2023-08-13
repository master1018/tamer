class CharExpression extends IntegerExpression {
    public CharExpression(long where, char value) {
        super(CHARVAL, where, Type.tChar, value);
    }
    public void print(PrintStream out) {
        out.print(value + "c");
    }
}
