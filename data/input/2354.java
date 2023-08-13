public class CloseInvalidatesKeys {
    public static void main (String [] args) throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        try {
            ch.configureBlocking(false);
            Selector sel = Selector.open();
            SelectionKey key = ch.register(sel, SelectionKey.OP_WRITE);
            sel.close();
            if (key.isValid())
                throw new Exception("Key valid after selector closed");
        } finally {
            ch.close();
        }
    }
}
