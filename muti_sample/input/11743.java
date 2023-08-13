public class ChatServer implements Runnable {
    private final List<Client> connections = Collections.synchronizedList(new ArrayList<Client>());
    private int port;
    private final AsynchronousServerSocketChannel listener;
    private final AsynchronousChannelGroup channelGroup;
    public ChatServer(int port) throws IOException {
        channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                Executors.defaultThreadFactory());
        this.port = port;
        listener = createListener(channelGroup);
    }
    public SocketAddress getSocketAddress() throws IOException {
        return listener.getLocalAddress();
    }
    public void run() {
        listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                listener.accept(null, this);
                handleNewConnection(result);
            }
            @Override
            public void failed(Throwable exc, Void attachment) {
            }
        });
    }
    public void shutdown() throws InterruptedException, IOException {
        channelGroup.shutdownNow();
        channelGroup.awaitTermination(1, TimeUnit.SECONDS);
    }
    private AsynchronousServerSocketChannel createListener(AsynchronousChannelGroup channelGroup) throws IOException {
        final AsynchronousServerSocketChannel listener = openChannel(channelGroup);
        listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        listener.bind(new InetSocketAddress(port));
        return listener;
    }
    private AsynchronousServerSocketChannel openChannel(AsynchronousChannelGroup channelGroup) throws IOException {
        return AsynchronousServerSocketChannel.open(channelGroup);
    }
    private void handleNewConnection(AsynchronousSocketChannel channel) {
        Client client = new Client(channel, new ClientReader(this, new NameReader(this)));
        try {
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        } catch (IOException e) {
        }
        connections.add(client);
        client.run();
    }
    public void writeMessageToClients(Client client, String message) {
        synchronized (connections) {
            for (Client clientConnection : connections) {
                if (clientConnection != client) {
                    clientConnection.writeMessageFrom(client, message);
                }
            }
        }
    }
    public void removeClient(Client client) {
        connections.remove(client);
    }
    private static void usage() {
        System.err.println("ChatServer [-port <port number>]");
        System.exit(1);
    }
    public static void main(String[] args) throws IOException {
        int port = 5000;
        if (args.length != 0 && args.length != 2) {
            usage();
        } else if (args.length == 2) {
            try {
                if (args[0].equals("-port")) {
                    port = Integer.parseInt(args[1]);
                } else {
                    usage();
                }
            } catch (NumberFormatException e) {
                usage();
            }
        }
        System.out.println("Running on port " + port);
        new ChatServer(port).run();
    }
}
