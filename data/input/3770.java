public class Trivial {
    public static void main(String[] args) throws Exception {
        SocketChannel sc = SocketChannel.open();
        Selector sel = Selector.open();
        try {
            if (sc.keyFor(sel) != null)
                throw new Exception("keyFor != null");
            sc.configureBlocking(false);
            SelectionKey sk = sc.register(sel, SelectionKey.OP_READ, args);
            if (sc.keyFor(sel) != sk)
                throw new Exception("keyFor returned " + sc.keyFor(sel));
            if (sk.attachment() != args)
                throw new Exception("attachment() returned " + sk.attachment());
            Trivial t = new Trivial();
            sk.attach(t);
            if (sk.attachment() != t)
                throw new Exception("Wrong attachment");
            sk.isReadable();
            sk.isWritable();
            sk.isConnectable();
            sk.isAcceptable();
        } finally {
            sel.close();
            sc.close();
        }
    }
}
