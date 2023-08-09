public class CloseTimeoutChannel {
    public static void main(String args[]) throws Exception {
        int port = -1;
        try {
            ServerSocketChannel listener=ServerSocketChannel.open();
            listener.socket().bind(new InetSocketAddress(0));
            port = listener.socket().getLocalPort();
            AcceptorThread thread=new AcceptorThread(listener);
            thread.start();
        } catch (IOException e) {
            System.out.println("Mysterious IO problem");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            System.out.println("Establishing connection");
            Socket socket=SocketChannel.open(
                new InetSocketAddress("127.0.0.1", port)).socket();
            OutputStream out=socket.getOutputStream();
            InputStream in=socket.getInputStream();
            System.out.println("1. Writing byte 1");
            out.write((byte)1);
            int n=read(socket, in);
            System.out.println("Read byte "+n+"\n");
            System.out.println("3. Writing byte 3");
            out.write((byte)3);
            System.out.println("Closing");
            socket.close();
        } catch (IOException e) {
            System.out.println("Mysterious IO problem");
            e.printStackTrace();
            System.exit(1);
        }
    }
    private static int read(Socket s, InputStream in) throws IOException {
        try {
            s.setSoTimeout(8000);     
            return in.read();
        } finally {
            s.setSoTimeout(0);
        }
    }
    static class AcceptorThread extends Thread {
        final String INDENT="\t\t\t\t";
        ServerSocketChannel _listener;
        AcceptorThread(ServerSocketChannel listener) {
            _listener=listener;
        }
        public void run() {
            try {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) { }
                System.out.println(INDENT+"Listening on port "+
                    _listener.socket().getLocalPort());
                ByteBuffer buf=ByteBuffer.allocate(5);
                Socket client=_listener.accept().socket();;
                System.out.println(INDENT+"Accepted client");
                OutputStream out=client.getOutputStream();
                InputStream in=client.getInputStream();
                int n=in.read();
                System.out.println(INDENT+"Read byte "+n+"\n");
                System.out.println(INDENT+"2. Writing byte 2");
                out.write((byte)2);
                n=in.read();
                System.out.println(INDENT+"Read byte "+n+"\n");
                n=in.read();
                System.out.println(INDENT+"Read byte "
                                   +(n<0 ? "EOF" : Integer.toString(n)));
                System.out.println(INDENT+"Closing");
                client.close();
            } catch (IOException e) {
                System.out.println(INDENT+"Error accepting!");
            } finally {
                try { _listener.close(); } catch (IOException ignore) { }
            }
        }
    }
}
