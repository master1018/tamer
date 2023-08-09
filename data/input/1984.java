public class CausedFocusEvent extends FocusEvent {
    public enum Cause {
        UNKNOWN,
        MOUSE_EVENT,
        TRAVERSAL,
        TRAVERSAL_UP,
        TRAVERSAL_DOWN,
        TRAVERSAL_FORWARD,
        TRAVERSAL_BACKWARD,
        MANUAL_REQUEST,
        AUTOMATIC_TRAVERSE,
        ROLLBACK,
        NATIVE_SYSTEM,
        ACTIVATION,
        CLEAR_GLOBAL_FOCUS_OWNER,
        RETARGETED
    };
    private final Cause cause;
    public Cause getCause() {
        return cause;
    }
    public String toString() {
        return "java.awt.FocusEvent[" + super.paramString() + ",cause=" + cause + "] on " + getSource();
    }
    public CausedFocusEvent(Component source, int id, boolean temporary,
                            Component opposite, Cause cause) {
        super(source, id, temporary, opposite);
        if (cause == null) {
            cause = Cause.UNKNOWN;
        }
        this.cause = cause;
    }
    public static FocusEvent retarget(FocusEvent e, Component newSource) {
        if (e == null) return null;
        return new CausedFocusEvent(newSource, e.getID(), e.isTemporary(), e.getOppositeComponent(),
                                    (e instanceof CausedFocusEvent) ? ((CausedFocusEvent)e).getCause() : Cause.RETARGETED);
    }
}
