class MessageReader implements DataReader {
    private final ChatServer chatServer;
    public MessageReader(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
    public boolean acceptsMessages() {
        return true;
    }
    @Override
    public void beforeRead(Client client) {
        String message = client.nextMessage();
        while (message != null) {
            chatServer.writeMessageToClients(client, message);
            message = client.nextMessage();
        }
    }
    @Override
    public void onData(Client client, ByteBuffer buffer, int bytes) {
        buffer.flip();
        client.appendMessage(new String(buffer.array(), 0, bytes));
    }
}
