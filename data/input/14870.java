public final class java_beans_Expression extends AbstractTest<Expression> {
    public static void main(String[] args) {
        new java_beans_Expression().test(true);
    }
    protected Expression getObject() {
        return new Expression("target", "equals", new String[] {"argument"});
    }
    protected Expression getAnotherObject() {
        return null; 
    }
}
