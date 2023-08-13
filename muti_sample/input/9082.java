final class TestEncoder extends XMLEncoder {
    private Expression expression;
    private TestEncoder() {
        super(System.out);
    }
    @Override
    public void writeExpression(Expression expression) {
        if (this.expression == null) {
            this.expression = expression;
        }
        super.writeExpression(expression);
    }
    public static void test(Object provider, Object object, Object value) {
        System.setSecurityManager(new SecurityManager());
        TestEncoder encoder = new TestEncoder();
        encoder.setPersistenceDelegate(
                object.getClass(),
                encoder.getPersistenceDelegate(provider.getClass()));
        encoder.writeObject(object);
        encoder.close();
        if (encoder.expression != null) {
            for (Object argument : encoder.expression.getArguments()) {
                if (value.equals(argument)) {
                    throw new Error("Found private value!");
                }
            }
        }
    }
}
