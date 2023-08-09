public class BalancedParentheses {
    static boolean separateServerThread = true;
    volatile int serverPort = 0;
    volatile static boolean serverReady = false;
    void doServerSide() throws Exception {
        ServerSocket serverSock = new ServerSocket(serverPort);
        serverPort = serverSock.getLocalPort();
        serverReady = true;
        Socket socket = serverSock.accept();
        System.out.println("Server: Connection accepted");
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        while (is.read() != -1) {
            is.skip(is.available());
            break;
        }
        byte[] bindResponse = {0x30, 0x0C, 0x02, 0x01, 0x01, 0x61, 0x07, 0x0A,
                               0x01, 0x00, 0x04, 0x00, 0x04, 0x00};
        os.write(bindResponse);
        os.flush();
        while (is.read() != -1) {
            is.skip(is.available());
        }
        is.close();
        os.close();
        socket.close();
        serverSock.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap:
        env.put("com.sun.jndi.ldap.read.timeout", "1000");
        DirContext context = new InitialDirContext(env);
        SearchControls scs = new SearchControls();
        scs.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
            NamingEnumeration answer = context.search(
                                        "o=sun,c=us", "(&(cn=Bob)))", scs);
        } catch (InvalidSearchFilterException isfe) {
            System.out.println("Expected exception: " + isfe.getMessage());
        } catch (NamingException ne) {
            throw new Exception("Expect a InvalidSearchFilterException", ne);
        }
        try {
            NamingEnumeration answer = context.search(
                                        "o=sun,c=us", ")(&(cn=Bob)", scs);
        } catch (InvalidSearchFilterException isfe) {
            System.out.println("Expected exception: " + isfe.getMessage());
        } catch (NamingException ne) {
            throw new Exception("Expect a InvalidSearchFilterException", ne);
        }
        try {
            NamingEnumeration answer = context.search(
                                        "o=sun,c=us", "(&(cn=Bob))", scs);
        } catch (InvalidSearchFilterException isfe) {
            throw new Exception("Unexpected ISFE", isfe);
        } catch (NamingException ne) {
            System.out.println("Expected exception: " + ne.getMessage());
        }
        context.close();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    void startServer(boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide();
                    } catch (Exception e) {
                        System.err.println("Server died...");
                        System.err.println(e);
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            doServerSide();
        }
    }
    void startClient(boolean newThread) throws Exception {
        if (newThread) {
            clientThread = new Thread() {
                public void run() {
                    try {
                        doClientSide();
                    } catch (Exception e) {
                        System.err.println("Client died...");
                        clientException = e;
                    }
                }
            };
            clientThread.start();
        } else {
            doClientSide();
        }
    }
    BalancedParentheses() throws Exception {
        if (separateServerThread) {
            startServer(true);
            startClient(false);
        } else {
            startClient(true);
            startServer(false);
        }
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }
        if (serverException != null) {
            System.out.print("Server Exception:");
            throw serverException;
        }
        if (clientException != null) {
            System.out.print("Client Exception:");
            throw clientException;
        }
    }
    public static void main(String[] args) throws Exception {
        new BalancedParentheses();
    }
}
