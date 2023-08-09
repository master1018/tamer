class ConstantExpression extends Expression {
    public ConstantExpression(int op, long where, Type type) {
        super(op, where, type);
    }
    public boolean isConstant() {
        return true;
    }
}
