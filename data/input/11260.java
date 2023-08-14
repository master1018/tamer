public class UrgentDataTest {
    Object opref;
    boolean isClient, isServer;
    String  clHost;
    ServerSocket listener;
    Socket client, server;
    int  clPort;
    InputStream clis, sis;
    OutputStream clos, sos;
    static void usage () {
        System.out.println ("   usage: java UrgentDataTest <runs client and server together>");
        System.out.println ("   usage: java UrgentDataTest -server <runs server alone>");
        System.out.println ("   usage: java UrgentDataTest -client host port <runs client and connects to"+
            " specified server>");
    }
    public static void main (String args[]) {
        try {
            UrgentDataTest test = new UrgentDataTest ();
            if (args.length == 0) {
                test.listener = new ServerSocket (0);
                test.isClient = true;
                test.isServer = true;
                test.clHost = "127.0.0.1";
                test.clPort = test.listener.getLocalPort();
                test.run();
            } else if (args[0].equals ("-server")) {
                test.listener = new ServerSocket (0);
                System.out.println ("Server listening on port " + test.listener.getLocalPort());
                test.isClient = false;
                test.isServer = true;
                test.run();
                System.out.println ("Server: Completed OK");
            } else if (args[0].equals ("-client")) {
                test.isClient = true;
                test.isServer = false;
                test.clHost = args [1];
                test.clPort = Integer.parseInt (args[2]);
                test.run();
                System.out.println ("Client: Completed OK");
            } else {
                usage ();
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            usage();
        }
        catch (NumberFormatException e) {
            usage();
        }
        catch (Exception e) {
            e.printStackTrace ();
            throw new RuntimeException ("Exception caught");
        }
    }
    public void run () throws Exception {
        try {
            if (isClient) {
                client = new Socket (clHost, clPort);
                clis = client.getInputStream();
                clos = client.getOutputStream();
                client.setOOBInline (true);
                if (client.getOOBInline() != true) {
                    throw new RuntimeException ("Setting OOBINLINE failed");
                }
            }
            if (isServer) {
                server = listener.accept ();
                sis = server.getInputStream();
                sos = server.getOutputStream();
            }
            if (isClient) {
                clos.write ("Hello".getBytes ());
                client.sendUrgentData (100);
                clos.write ("world".getBytes ());
            }
            String s = "Helloworld";
            if (isServer) {
                for (int y=0; y<s.length(); y++) {
                    int c = sis.read ();
                    if (c != (int)s.charAt (y)) {
                        throw new RuntimeException ("Unexpected character read");
                    }
                }
                sos.write ("Hello".getBytes ());
                server.sendUrgentData (101);
                sos.write ("World".getBytes ());
            }
            if (isClient) {
                s="Hello";
                for (int y=0; y<s.length(); y++) {
                    int c = clis.read ();
                    if (c != (int)s.charAt (y)) {
                        throw new RuntimeException ("Unexpected character read");
                    }
                }
                if (clis.read() != 101) {
                    throw new RuntimeException ("OOB byte not received");
                }
                s="World";
                for (int y=0; y<s.length(); y++) {
                    int c = clis.read ();
                    if (c != (int)s.charAt (y)) {
                        throw new RuntimeException ("Unexpected character read");
                    }
                }
            }
        } finally {
            if (listener != null) listener.close();
            if (client != null) client.close ();
            if (server != null) server.close ();
        }
    }
}
