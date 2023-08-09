public class CloseTest {
    public static void main(String args[]) throws Exception {
        String msg = "HELLO";
        String service_args[] = new String[2];
        service_args[0] = String.valueOf(msg.length());
        service_args[1] = String.valueOf( 15*1000 );
        SocketChannel sc = Launcher.launchWithSocketChannel("EchoService", service_args);
        sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
        ByteBuffer bb = ByteBuffer.allocateDirect(50);
        sc.configureBlocking(false);
        Selector sel = sc.provider().openSelector();
        SelectionKey sk = sc.register(sel, SelectionKey.OP_READ);
        long to = 12 * 1000;
        for (;;) {
            long st = System.currentTimeMillis();
            sel.select(to);
            if (sk.isReadable()) {
                int n = sc.read(bb);
                if (n < 0) {
                    break;
                }
            }
            sel.selectedKeys().remove(sk);
            to -= System.currentTimeMillis() - st;
            if (to <= 0) {
                throw new RuntimeException("Timed out waiting for connection to close");
            }
        }
        sel.close();
        sc.close();
        bb.flip();
        if (bb.remaining() < msg.length()) {
            throw new RuntimeException("Premature EOF from echo service");
        }
        System.out.println("Test passed - service closed connection.");
    }
}
