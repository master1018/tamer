class JSSEServer extends CipherTest.Server {
    SSLServerSocket serverSocket;
    JSSEServer(CipherTest cipherTest) throws Exception {
        super(cipherTest);
        SSLContext serverContext = SSLContext.getInstance("TLS");
        serverContext.init(new KeyManager[] {cipherTest.keyManager}, new TrustManager[] {cipherTest.trustManager}, cipherTest.secureRandom);
        SSLServerSocketFactory factory = (SSLServerSocketFactory)serverContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket)factory.createServerSocket(cipherTest.serverPort);
        cipherTest.serverPort = serverSocket.getLocalPort();
        serverSocket.setEnabledCipherSuites(factory.getSupportedCipherSuites());
    }
    public void run() {
        System.out.println("JSSE Server listening on port " + cipherTest.serverPort);
        Executor exec = Executors.newFixedThreadPool
                            (cipherTest.THREADS, DaemonThreadFactory.INSTANCE);
        try {
            while (true) {
                final SSLSocket socket = (SSLSocket)serverSocket.accept();
                socket.setSoTimeout(cipherTest.TIMEOUT);
                Runnable r = new Runnable() {
                    public void run() {
                        try {
                            InputStream in = socket.getInputStream();
                            OutputStream out = socket.getOutputStream();
                            handleRequest(in, out);
                            out.flush();
                            socket.close();
                            socket.getSession().invalidate();
                        } catch (IOException e) {
                            cipherTest.setFailed();
                            e.printStackTrace();
                        } finally {
                            if (socket != null) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    cipherTest.setFailed();
                                    System.out.println("Exception closing socket on server side:");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                };
                exec.execute(r);
            }
        } catch (IOException e) {
            cipherTest.setFailed();
            e.printStackTrace();
        }
    }
}
