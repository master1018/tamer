public class SSLSessionBindingEvent extends EventObject implements Serializable {
    private static final long serialVersionUID = 3989172637106345L;
    private final String name;
    public SSLSessionBindingEvent(SSLSession session, String name) {
        super(session);
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public SSLSession getSession() {
        return (SSLSession) this.source;
    }
}
