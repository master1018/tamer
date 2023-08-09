class ClosedChannelList {
    static final long TIMEOUT = 10 * 1000; 
    static class Element {
        long expiry;
        SelectionKey key;
        Element (long l, SelectionKey key) {
            expiry = l;
            this.key = key;
        }
    }
    LinkedList list;
    public ClosedChannelList () {
        list = new LinkedList ();
    }
    public synchronized void add (SelectionKey key) {
        long exp = System.currentTimeMillis () + TIMEOUT;
        list.add (new Element (exp, key));
    }
    public synchronized void check () {
        check (false);
    }
    public synchronized void terminate () {
        check (true);
    }
    public synchronized void check (boolean forceClose) {
        Iterator iter = list.iterator ();
        long now = System.currentTimeMillis();
        while (iter.hasNext ()) {
            Element elm = (Element)iter.next();
            if (forceClose || elm.expiry <= now) {
                SelectionKey k = elm.key;
                try {
                    k.channel().close ();
                } catch (IOException e) {}
                k.cancel();
                iter.remove();
            }
        }
    }
}
