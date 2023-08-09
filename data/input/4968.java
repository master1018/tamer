public class WakeupOverflow {
    public static void main( String[] args ) throws Exception {
        Selector selector = Selector.open();
        try {
            for(int i=0; i<10000; i++) {
                selector.wakeup();
            }
        } finally {
            selector.close();
        }
    }
}
