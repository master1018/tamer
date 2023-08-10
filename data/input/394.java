public class ChatRoomSM extends ActorSM {
    HashMap<String, ActorAddress> clients;
    public ChatRoomSM() {
        setBehaviorClass(new ChatRoomCS("ChatRoom"));
    }
    public void initInstance() {
        super.initInstance();
        clients = new HashMap<String, ActorAddress>();
    }
    void addClient(String alias, ActorAddress address) {
        clients.put(alias, address);
        trace.traceTask("User: " + alias + " is now connected to the chat room");
    }
    void send(String message, ActorAddress sender) {
        Collection<ActorAddress> en = clients.values();
        Vector<ActorAddress> receivers = new Vector();
        for (Iterator<ActorAddress> addressIterator = en.iterator(); addressIterator.hasNext(); ) {
            ActorAddress actorAddress = addressIterator.next();
            if (actorAddress.equals(sender)) {
                receivers.addElement(actorAddress);
                AFPropertyMsg pm = new AFPropertyMsg("Message").setProperty("text", message);
                sendMessage(pm, actorAddress);
            }
        }
        trace.traceTask("Message: " + message + " is send to " + receivers);
    }
    public void remove(String alias) {
        clients.remove(alias);
        trace.traceTask("User: " + alias + " is removed");
    }
    public boolean hasClients() {
        return !clients.isEmpty();
    }
}
