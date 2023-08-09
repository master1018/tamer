public class Pin {
    final static int NUM_BLOCKS = 128;
    final static int BLOCK_SIZE = 32768;
    public static void main(String[] args) throws Exception {
        SoftReference[] blocks = new SoftReference[NUM_BLOCKS];
        byte[] block;
        System.err.println("Filling array with " + NUM_BLOCKS +
                           " SoftReferences to blocks of " +
                           BLOCK_SIZE + " bytes.");
        for (int i = 0; i < NUM_BLOCKS; ++ i) {
            block = new byte[BLOCK_SIZE];
            SoftReference ref = new SoftReference(block);
            blocks[i] = ref;
        }
        block = null;
        System.err.println("Allowing SoftReferences to be enqueued.");
        System.gc();
        Thread.sleep(1000);
        System.err.println("Invoking get() on SoftReferences.");
        for (int i = 0; i < NUM_BLOCKS; ++ i) {
            block = (byte[]) blocks[i].get();
        }
        block = null;
        System.err.println("Forcing desperate garbage collection...");
        java.util.Vector chain = new java.util.Vector();
        try {
            while (true) {
                System.gc();
                int[] hungry = new int[65536];
                chain.addElement(hungry);
                Thread.sleep(100);              
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Got OutOfMemoryError, as expected.");
        }
        int emptyCount = 0, fullCount = 0;
        System.err.print("Examining contents of array:");
        for (int i = 0; i < NUM_BLOCKS; ++ i) {
            block = (byte[]) blocks[i].get();
            if (block == null) {
                emptyCount++;
            } else {
                fullCount++;
            }
        }
        System.err.println(" " + emptyCount + " empty, " +
                           fullCount + " full.");
        if (emptyCount == 0)
            throw new Exception("No SoftReference instances were cleared");
    }
}
