public class AppletEvent extends EventObject {
    private Object arg;
    private int id;
    public AppletEvent(Object source, int id, Object argument) {
        super(source);
        this.arg = argument;
        this.id = id;
    }
    public int getID() {
        return id;
    }
    public Object getArgument() {
        return arg;
    }
    public String toString() {
        String str = getClass().getName() + "[source=" + source + " + id="+ id;
        if (arg != null) {
            str += " + arg=" + arg;
        }
        str += " ]";
        return str;
    }
}
