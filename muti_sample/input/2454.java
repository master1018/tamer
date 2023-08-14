public class ReuseBuf {
    static String msgs[] = {"Hello World", "Java", "Good Bye"};
    static int port;
    static class ServerThread extends Thread{
        DatagramSocket ds;
        public ServerThread() {
            try {
                ds = new DatagramSocket();
                port = ds.getLocalPort();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        public void run() {
            byte b[] = new byte[100];
            DatagramPacket dp = new DatagramPacket(b,b.length);
            while (true) {
                try {
                    ds.receive(dp);
                    String reply = new String(dp.getData(), dp.getOffset(), dp.getLength());
                    ds.send(new DatagramPacket(reply.getBytes(),reply.length(),
                                               dp.getAddress(),dp.getPort()));
                    if (reply.equals(msgs[msgs.length-1])) {
                        break;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
            ds.close();
        }
    }
    public static void main(String args[]) throws Exception {
        ServerThread st = new ServerThread();
        st.start();
        DatagramSocket ds = new DatagramSocket();
        byte b[] = new byte[100];
        DatagramPacket dp = new DatagramPacket(b,b.length);
        for (int i = 0; i < msgs.length; i++) {
            ds.send(new DatagramPacket(msgs[i].getBytes(),msgs[i].length(),
                                       InetAddress.getByName("LocalHost"),
                                       port));
            ds.receive(dp);
            if (!msgs[i].equals(new String(dp.getData(), dp.getOffset(), dp.getLength()))) {
                throw new RuntimeException("Msg expected: "+msgs[i] +msgs[i].length()+
                                           "msg received: "+new String(dp.getData(), dp.getOffset(), dp.getLength())+dp.getLength());
            }
        }
        ds.close();
        System.out.println("Test Passed!!!");
    }
}
