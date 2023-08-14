public class CloseThenRegister {
    public static void main (String [] args) throws Exception {
        Selector sel = Selector.open();
        sel.close();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            ssc.bind(new InetSocketAddress(0));
            ssc.configureBlocking(false);
            ssc.register(sel, SelectionKey.OP_ACCEPT);
            throw new RuntimeException("register after close does not cause CSE!");
        } catch (ClosedSelectorException cse) {
        } finally {
            ssc.close();
        }
    }
}
