class DispatcherN implements Dispatcher {
    private Selector sel;
    DispatcherN() throws IOException {
        sel = Selector.open();
    }
    public void run() {
        for (;;) {
            try {
                dispatch();
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }
    private Object gate = new Object();
    private void dispatch() throws IOException {
        sel.select();
        for (Iterator i = sel.selectedKeys().iterator(); i.hasNext(); ) {
            SelectionKey sk = (SelectionKey)i.next();
            i.remove();
            Handler h = (Handler)sk.attachment();
            h.handle(sk);
        }
        synchronized (gate) { }
    }
    public void register(SelectableChannel ch, int ops, Handler h)
            throws IOException {
        synchronized (gate) {
            sel.wakeup();
            ch.register(sel, ops, h);
        }
    }
}
