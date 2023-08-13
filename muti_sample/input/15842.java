public final class java_beans_EventHandler extends AbstractTest<EventHandler> {
    public static void main(String[] args) {
        new java_beans_EventHandler().test(true);
    }
    protected EventHandler getObject() {
        return new EventHandler("target", "action", "property", "listener");
    }
    protected EventHandler getAnotherObject() {
        return null; 
    }
}
