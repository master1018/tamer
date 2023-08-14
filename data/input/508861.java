public class ComponentEvent extends AWTEvent {
    private static final long serialVersionUID = 8101406823902992965L;
    public static final int COMPONENT_FIRST = 100;
    public static final int COMPONENT_LAST = 103;
    public static final int COMPONENT_MOVED = 100;
    public static final int COMPONENT_RESIZED = 101;
    public static final int COMPONENT_SHOWN = 102;
    public static final int COMPONENT_HIDDEN = 103;
    public ComponentEvent(Component source, int id) {
        super(source, id);
    }
    public Component getComponent() {
        return (Component) source;
    }
    @Override
    public String paramString() {
        String idString = null;
        Component c = getComponent();
        switch (id) {
        case COMPONENT_MOVED:
            idString = "COMPONENT_MOVED"; 
            break;
        case COMPONENT_RESIZED:
            idString = "COMPONENT_RESIZED"; 
            break;
        case COMPONENT_SHOWN:
            return "COMPONENT_SHOWN"; 
        case COMPONENT_HIDDEN:
            return "COMPONENT_HIDDEN"; 
        default:
            return "unknown type"; 
        }
        return (idString + " (" + c.getX() + "," + c.getY() +  
                " " + c.getWidth()+ "x" + c.getHeight() + ")"); 
    }
}
