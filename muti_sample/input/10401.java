class NameReader implements DataReader {
    private final StringBuilder buffer = new StringBuilder();
    private final ChatServer chatServer;
    private boolean once = true;
    private static final String NEWLINE = "\n";
    public NameReader(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
    @Override
    public void beforeRead(Client client) {
        if (once) {
            client.writeStringMessage("Name: ");
            once = false;
        }
    }
    public boolean acceptsMessages() {
        return false;
    }
    @Override
    public void onData(Client client, ByteBuffer buffer, int bytes) {
        buffer.flip();
        String name;
        name = this.buffer.append(new String(buffer.array(), 0, bytes)).toString();
        if (name.contains(NEWLINE)) {
            onUserNameRead(client, name);
        }
    }
    private void onUserNameRead(Client client, String name) {
        String[] strings = name.split(NEWLINE, 2);
        client.setUserName(strings[0].trim());
        sendRemainingParts(client, strings);
        client.setReader(new ClientReader(chatServer, new MessageReader(chatServer)));
        client.writeStringMessage("Welcome " + client.getUserName() + "\n");
    }
    private void sendRemainingParts(Client client, String[] strings) {
        for (int i = 1; i < strings.length; ++i) {
            client.appendMessage(strings[i]);
        }
    }
}
