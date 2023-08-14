public class CheckDiscard {
    CheckDiscard() throws Exception {
        DatagramSocket s = new DatagramSocket();
        Sender s1 = new Sender( s.getLocalPort() );
        Sender s2 = new Sender( s.getLocalPort() );
        InetAddress ia = InetAddress.getLocalHost();
        s.connect( ia, s1.getLocalPort() );
        (new Thread(s1)).start();
        (new Thread(s2)).start();
        byte b[] = new byte[512];
        DatagramPacket p = new DatagramPacket(b, b.length);
        s.setSoTimeout(4000);
        try {
            for (int i=0; i<20; i++) {
                s.receive(p);
                if ((p.getPort() != s1.getLocalPort()) ||
                    (!p.getAddress().equals(ia))) {
                    throw new Exception("Received packet from wrong sender");
                }
            }
        } catch (SocketTimeoutException e) {
        }
        Exception e;
        e = s1.getException();
        if (e != null) throw e;
        e = s2.getException();
        if (e != null) throw e;
    }
    public static void main(String args[]) throws Exception {
        new CheckDiscard();
    }
    public class Sender implements Runnable {
        Exception exc = null;
        DatagramSocket s;
        int port;
        Sender(int port) throws Exception {
            s = new DatagramSocket();
            this.port = port;
        }
        public int getLocalPort() {
            return s.getLocalPort();
        }
        public void setException(Exception e) {
            exc = e;
        }
        public Exception getException() {
            return exc;
        }
        public void run() {
            try {
                byte b[] = "Hello".getBytes();
                DatagramPacket p = new DatagramPacket(b, b.length);
                p.setAddress( InetAddress.getLocalHost() );
                p.setPort( port );
                for (int i=0; i<10; i++) {
                    s.send(p);
                    Thread.currentThread().sleep(1000);
                }
            } catch (Exception e) {
                setException(e);
            }
            s.close();
        }
    }
}
