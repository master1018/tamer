public final class java_beans_Statement extends AbstractTest<Statement> {
    public static void main(String[] args) {
        new java_beans_Statement().test(true);
    }
    protected Statement getObject() {
        return new Statement("target", "method", new String[] {"arg1", "arg2"});
    }
    protected Statement getAnotherObject() {
        return null; 
    }
}
