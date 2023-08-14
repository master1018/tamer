public class ContextThreadGroup extends ThreadGroup {
    final ContextStorage context = new ContextStorage();
    public ContextThreadGroup(String name) {
        super(name);
    }
    public void dispose() {
        context.shutdown();
    }
}
