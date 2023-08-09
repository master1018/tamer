class IntExpression extends IntegerExpression {
    public IntExpression(long where, int value) {
        super(INTVAL, where, Type.tInt, value);
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof IntExpression)) {
            return value == ((IntExpression)obj).value;
        }
        return false;
    }
    public int hashCode() {
        return value;
    }
    public void print(PrintStream out) {
        out.print(value);
    }
}
