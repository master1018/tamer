class ClientReader {
    private final DataReader callback;
    private final ChatServer chatServer;
    ClientReader(ChatServer chatServer, DataReader callback) {
        this.chatServer = chatServer;
        this.callback = callback;
    }
    public boolean acceptsMessages() {
        return callback.acceptsMessages();
    }
    public void run(final Client client) {
        callback.beforeRead(client);
        client.read(new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (result < 1) {
                    client.close();
                    System.out.println("Closing connection to " + client);
                    chatServer.removeClient(client);
                } else {
                    callback.onData(client, buffer, result);
                    client.run();
                }
            }
            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                client.close();
                chatServer.removeClient(client);
            }
        });
    }
}
