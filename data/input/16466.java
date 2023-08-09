public abstract class OriginServer implements Runnable {
    private ServerSocket server = null;
    Exception serverException = null;
    protected OriginServer(ServerSocket ss) throws Exception
    {
        server = ss;
        newListener();
        if (serverException != null)
            throw serverException;
    }
    public abstract byte[] getBytes();
    public void run()
    {
        Socket socket;
        try {
            socket = server.accept();
        } catch (IOException e) {
            System.out.println("Class Server died: " + e.getMessage());
            serverException = e;
            return;
        }
        try {
            DataOutputStream out =
                new DataOutputStream(socket.getOutputStream());
            try {
                BufferedReader in =
                    new BufferedReader(new InputStreamReader(
                                socket.getInputStream()));
                readRequest(in);
                byte[] bytecodes = getBytes();
                try {
                    out.writeBytes("HTTP/1.0 200 OK\r\n");
                    out.writeBytes("Content-Length: " + bytecodes.length +
                                   "\r\n");
                    out.writeBytes("Content-Type: text/html\r\n\r\n");
                    out.write(bytecodes);
                    out.flush();
                } catch (IOException ie) {
                    serverException = ie;
                    return;
                }
            } catch (Exception e) {
                out.writeBytes("HTTP/1.0 400 " + e.getMessage() + "\r\n");
                out.writeBytes("Content-Type: text/html\r\n\r\n");
                out.flush();
            }
        } catch (IOException ex) {
            System.out.println("error writing response: " + ex.getMessage());
            serverException = ex;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                serverException = e;
            }
        }
    }
    private void newListener()
    {
        (new Thread(this)).start();
    }
    private static void readRequest(BufferedReader in)
        throws IOException
    {
        String line = null;
        System.out.println("Server received: ");
        do {
            if (line != null)
                System.out.println(line);
            line = in.readLine();
        } while ((line.length() != 0) &&
                (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
    }
}
