public class Test6707226 {
    public static void main(String[] args) throws Exception {
        Object value = new Object();
        Expression expression = new Expression(value, Object.class, "new", null);
        if (!value.equals(expression.getValue()))
            throw new Error("the value is updated unexpectedly");
        expression.execute();
        if (value.equals(expression.getValue()))
            throw new Error("the value is not updated as expected");
    }
}
