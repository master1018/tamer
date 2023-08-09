public class NamingExceptionEvent extends java.util.EventObject {
    private NamingException exception;
    public NamingExceptionEvent(EventContext source, NamingException exc) {
        super(source);
        exception = exc;
    }
    public NamingException getException() {
        return exception;
    }
    public EventContext getEventContext() {
        return (EventContext)getSource();
    }
    public void dispatch(NamingListener listener) {
        listener.namingExceptionThrown(this);
    }
    private static final long serialVersionUID = -4877678086134736336L;
}
