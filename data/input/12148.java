public class KeySets {
    static boolean compat;
    static abstract class Catch {
        abstract void go() throws Exception;
        Catch(Class xc) throws Exception {
            try {
                go();
            } catch (Exception x) {
                if (compat)
                    throw new Exception("Exception thrown", x);
                if (xc.isInstance(x))
                    return;
                throw new Exception("Wrong exception", x);
            }
            if (compat)
                return;
            throw new Exception("Not thrown as expected: "
                                + xc.getName());
        }
    }
    static void testClose() throws Exception {
        final Selector sel = Selector.open();
        sel.keys();
        sel.selectedKeys();
        sel.close();
        new Catch(ClosedSelectorException.class) {
                void go() throws Exception {
                    sel.keys();
                }};
        new Catch(ClosedSelectorException.class) {
                void go() throws Exception {
                    sel.selectedKeys();
                }};
    }
    static void testNoAddition(final Set s) throws Exception {
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    s.add(new Object());
                }};
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    ArrayList al = new ArrayList();
                    al.add(new Object());
                    s.addAll(al);
                }};
    }
    static interface Adder {
        void add() throws IOException;
    }
    static void testNoRemoval(final Set s, final Adder adder)
        throws Exception
    {
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    adder.add();
                    s.clear();
                }};
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    adder.add();
                    Iterator i = s.iterator();
                    i.next();
                    i.remove();
                }};
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    adder.add();
                    s.remove(s.iterator().next());
                }};
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    adder.add();
                    HashSet hs = new HashSet();
                    hs.addAll(s);
                    s.removeAll(hs);
                }};
        new Catch(UnsupportedOperationException.class) {
                void go() throws Exception {
                    adder.add();
                    s.retainAll(Collections.EMPTY_SET);
                }};
    }
    static SelectionKey reg(Selector sel) throws IOException {
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(false);
        return dc.register(sel, SelectionKey.OP_WRITE);
    }
    static void testMutability() throws Exception {
        final Selector sel = Selector.open();
        testNoRemoval(sel.keys(), new Adder() {
                public void add() throws IOException {
                    reg(sel);
                }
            });
        testNoAddition(sel.keys());
        sel.select();
        testNoAddition(sel.selectedKeys());
        SelectionKey sk = reg(sel);
        sel.select();
        int n = sel.selectedKeys().size();
        sel.selectedKeys().remove(sk);
        if (sel.selectedKeys().size() != n - 1)
            throw new Exception("remove failed");
        HashSet hs = new HashSet();
        hs.add(reg(sel));
        sel.select();
        sel.selectedKeys().retainAll(hs);
        if (sel.selectedKeys().isEmpty())
            throw new Exception("retainAll failed");
        sel.selectedKeys().removeAll(hs);
        if (!sel.selectedKeys().isEmpty())
            throw new Exception("removeAll failed");
        hs.clear();
        hs.add(reg(sel));
        sel.select();
        sel.selectedKeys().clear();
        if (!sel.selectedKeys().isEmpty())
            throw new Exception("clear failed");
    }
    public static void main(String[] args) throws Exception {
        String bl = System.getProperty("sun.nio.ch.bugLevel");
        compat = (bl != null) && bl.equals("1.4");
        testClose();
        testMutability();
    }
}
