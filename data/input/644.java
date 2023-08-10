public class MessageTypeRouter implements Application {
    private static final HapiLog log = HapiLogFactory.getHapiLog(MessageTypeRouter.class);
    private HashMap apps;
    public MessageTypeRouter() {
        apps = new HashMap(20);
    }
    public boolean canProcess(Message in) {
        boolean can = false;
        try {
            Application matches = this.getMatchingApplication(in);
            if (matches != null) can = true;
        } catch (HL7Exception e) {
            can = false;
        }
        return can;
    }
    public Message processMessage(Message in) throws ApplicationException {
        Message out;
        try {
            Application matchingApp = this.getMatchingApplication(in);
            out = matchingApp.processMessage(in);
        } catch (HL7Exception e) {
            throw new ApplicationException("Error internally routing message: " + e.toString(), e);
        }
        return out;
    }
    public Message processMessage(Message in, Socket socket) throws ApplicationException {
        Message out;
        try {
            Application matchingApp = this.getMatchingApplication(in);
            if (matchingApp instanceof HL7Application) {
                out = ((HL7Application) matchingApp).processMessage(in, socket);
            } else {
                out = matchingApp.processMessage(in);
            }
        } catch (HL7Exception e) {
            throw new ApplicationException("Error internally routing message: " + e.toString(), e);
        }
        return out;
    }
    public synchronized void registerApplication(String messageType, String triggerEvent, Application handler) {
        this.apps.put(getKey(messageType, triggerEvent), handler);
        StringBuffer buf = new StringBuffer();
        buf.append(handler.getClass().getName());
        buf.append(" registered to handle ");
        buf.append(messageType);
        buf.append("^");
        buf.append(triggerEvent);
        buf.append(" messages");
        log.info(buf.toString());
    }
    private Application getMatchingApplication(Message message) throws HL7Exception {
        Terser t = new Terser(message);
        String messageType = t.get("/MSH-9-1");
        String triggerEvent = t.get("/MSH-9-2");
        return this.getMatchingApplication(messageType, triggerEvent);
    }
    private synchronized Application getMatchingApplication(String messageType, String triggerEvent) {
        Application matchingApp = null;
        Object o = this.apps.get(getKey(messageType, triggerEvent));
        if (o == null) o = this.apps.get(getKey(messageType, "*"));
        if (o == null) o = this.apps.get(getKey("*", triggerEvent));
        if (o == null) o = this.apps.get(getKey("*", "*"));
        if (o != null) matchingApp = (Application) o;
        return matchingApp;
    }
    private String getKey(String messageType, String triggerEvent) {
        return messageType + "|" + triggerEvent;
    }
}
