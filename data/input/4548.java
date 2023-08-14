class GetPeerHostClient extends Thread
{
    SSLSocket s;
    String server;
    public GetPeerHostClient ()
    {
        try {
            SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory
                                        .getDefault();
            server = InetAddress.getLocalHost().getHostName();
            s = (SSLSocket) factory.createSocket(server, 9999);
            System.out.println("CLIENT: connected to the server- " + server);
        } catch (Exception e) {
                System.err.println("Unexpected exceptions: " + e);
                e.printStackTrace();
          }
    }
    public void run ()
    {
        try {
            s.startHandshake(); 
            PrintWriter out = new PrintWriter(
                              new BufferedWriter(
                              new OutputStreamWriter(
                              s.getOutputStream())));
            out.println("GET http:
            out.println();
            out.flush();
        } catch (Exception e) {
                System.err.println("Unexpected exceptions: " + e);
                e.printStackTrace();
                return;
          }
     }
}
