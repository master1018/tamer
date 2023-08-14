public class HierarchicalState {
    protected HierarchicalState() {
    }
    protected void enter() {
    }
    protected boolean processMessage(Message msg) {
        return false;
    }
    protected void exit() {
    }
    public String getName() {
        String name = getClass().getName();
        int lastDollar = name.lastIndexOf('$');
        return name.substring(lastDollar + 1);
    }
}
