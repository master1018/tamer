public class SocksConnectTimeout {
    static ServerSocket serverSocket;
    static final boolean debug = true;
    static final Phaser startPhaser = new Phaser(2);
    static final Phaser finishPhaser = new Phaser(2);
    static int failed, passed;
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(0);
            (new Thread() {
                @Override
                public void run() { serve(); }
            }).start();
            Proxy socksProxy = new Proxy(Proxy.Type.SOCKS,
                new InetSocketAddress(InetAddress.getLocalHost(), serverSocket.getLocalPort()));
            test(socksProxy);
        } catch (IOException e) {
            unexpected(e);
        } finally {
            close(serverSocket);
            if (failed > 0)
                throw new RuntimeException("Test Failed: passed:" + passed + ", failed:" + failed);
        }
    }
    static void test(Proxy proxy) {
        startPhaser.arriveAndAwaitAdvance();
        Socket socket = null;
        try {
            socket = new Socket(proxy);
            connectWithTimeout(socket);
            failed("connected successfully!");
        } catch (SocketTimeoutException socketTimeout) {
            debug("Passed: Received: " + socketTimeout);
            passed();
        } catch (Exception exception) {
            failed("Connect timeout test failed", exception);
        } finally {
            finishPhaser.arriveAndAwaitAdvance();
            close(socket);
        }
    }
    static void connectWithTimeout(Socket socket) throws IOException {
        socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 1234), 500);
    }
    static void serve() {
        Socket client = null;
        try {
            startPhaser.arriveAndAwaitAdvance();
            client = serverSocket.accept();
            finishPhaser.awaitAdvanceInterruptibly(finishPhaser.arrive(), 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            unexpected(e);
        } finally {
            close(client);
        }
    }
    static void debug(String message) {
        if (debug)
            System.out.println(message);
    }
    static void unexpected(Exception e ) {
        System.out.println("Unexcepted Exception: " + e);
    }
    static void close(Closeable closeable) {
        if (closeable != null) try { closeable.close(); } catch (IOException e) {unexpected(e);}
    }
    static void failed(String message) {
        System.out.println(message);
        failed++;
    }
    static void failed(String message, Exception e) {
        System.out.println(message);
        System.out.println(e);
        failed++;
    }
    static void passed() { passed++; };
}
