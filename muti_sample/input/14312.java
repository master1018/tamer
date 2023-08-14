public class NonBlocking {
    public static void main(String[] args) throws Exception {
        test1();
    }
    static void test1() throws Exception {
        Pipe p = Pipe.open();
        try {
            p.sink().configureBlocking(false);
            if (p.sink().isBlocking())
                throw new Exception("Sink still blocking");
            p.source().configureBlocking(false);
            if (p.source().isBlocking())
                throw new Exception("Source still blocking");
        } finally {
            p.sink().close();
            p.source().close();
        }
    }
}
