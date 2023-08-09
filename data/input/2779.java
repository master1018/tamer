public class UngrabEvent extends AWTEvent {
    private final static int UNGRAB_EVENT_ID = 1998;
    public UngrabEvent(Component source) {
        super(source, UNGRAB_EVENT_ID);
    }
    public String toString() {
        return "sun.awt.UngrabEvent[" + getSource() + "]";
    }
}
