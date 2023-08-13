public class NoWaitForReplyTest {
    public static void main(String[] args) throws Exception {
        boolean passed = false;
        DummyServer ldapServer = new DummyServer();
        ldapServer.start();
        Hashtable env = new Hashtable(11);
        env.put(Context.PROVIDER_URL, "ldap:
            ldapServer.getPortNumber());
        env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.read.timeout", "10000");
        env.put("com.sun.jndi.ldap.search.waitForReply", "false");
        env.put("java.naming.ldap.version", "3");
        try {
            System.out.println("Client: connecting to the server");
            DirContext ctx = new InitialDirContext(env);
            SearchControls scl = new SearchControls();
            scl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            System.out.println("Client: performing search");
            NamingEnumeration answer =
            ctx.search("ou=People,o=JNDITutorial", "(objectClass=*)", scl);
            passed = true;
            System.out.println("Client: did not wait until first reply");
            ctx.close();
        } catch (NamingException e) {
        }
        ldapServer.interrupt();
        if (!passed) {
            throw new Exception(
                "Test FAILED: should not have waited until first search reply");
        }
        System.out.println("Test PASSED");
    }
    static class DummyServer extends Thread {
        private final ServerSocket serverSocket;
        DummyServer() throws IOException {
            this.serverSocket = new ServerSocket(0);
            System.out.println("Server: listening on port " + serverSocket.getLocalPort());
        }
        public int getPortNumber() {
            return serverSocket.getLocalPort();
        }
        public void run() {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Server: accepted a connection");
                InputStream in = socket.getInputStream();
                while (!isInterrupted()) {
                   in.skip(in.available());
                }
            } catch (Exception e) {
            } finally {
                System.out.println("Server: shutting down");
                try {
                    serverSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
