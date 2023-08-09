public class ContainerEvent extends ComponentEvent {
    private static final long serialVersionUID = -4114942250539772041L;
    public static final int CONTAINER_FIRST = 300;
    public static final int CONTAINER_LAST = 301;
    public static final int COMPONENT_ADDED = 300;
    public static final int COMPONENT_REMOVED = 301;
    private Component child;
    public ContainerEvent(Component src, int id, Component child) {
        super(src, id);
        this.child = child;
    }
    public Component getChild() {
        return child;
    }
    @Override
    public String paramString() {
        String idString = null;
        switch (id) {
        case COMPONENT_ADDED:
            idString = "COMPONENT_ADDED"; 
            break;
        case COMPONENT_REMOVED:
            idString = "COMPONENT_REMOVED"; 
            break;
        default:
            idString = "unknown type"; 
        }
        return (idString + ",child=" + child.getName()); 
    }
}
