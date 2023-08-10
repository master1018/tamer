class FalseExpr extends ConvertibleBooleanExpr {
    public boolean eval(Node node, ExprContext context) {
        return false;
    }
}
