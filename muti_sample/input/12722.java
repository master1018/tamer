public class SkipBytes{
    private static void doTest(RandomAccessFile raf, int start, int num_to_skip)
        throws Exception
    {
        raf.seek(start);
        long cur_ptr = raf.getFilePointer();
        int length = (int) raf.length();
        System.err.println("\nCurrent pointer = " + cur_ptr + " length = " +
                           length + " num_to_skip = " + num_to_skip);
        int num_skipped = raf.skipBytes(num_to_skip);
        System.err.println("After skipBytes -- no. skipped = " + num_skipped);
        if (num_to_skip <= 0) {
            if (num_skipped != 0){
                System.err.println("Negative Skip Test Failed");
                throw new RuntimeException("Negative Skip Test Failed");
            }
            else {
                System.err.println("Negative Skip Test Succeeded");
            }
        }
        cur_ptr = raf.getFilePointer();
        System.err.println("Current pointer = " + cur_ptr);
        if (cur_ptr > length) {
            System.err.println("Past EOF Skip Test Failed");
            throw new RuntimeException("Past EOF Skip Test Failed");
        }
        else {
            System.err.println("Past EOF Skip Test Succeeded");
        }
        int byte_read = raf.read();
        if ( (cur_ptr == length) &&
             (byte_read != -1) ) {
            System.err.println("byte_read = " + byte_read +
                               " Read Test Failed ......");
            throw new RuntimeException("Read Test Failed");
        }
        else {
            System.err.println("byte_read = " + byte_read +
                               " Read Test Succeeded");
        }
    }
    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("input.txt" , "rw");
        try {
            int length = (int)raf.length();
            doTest(raf , 0 , 2*length);
            doTest(raf , 0 , length);
            doTest(raf , 0 , length/2);
            doTest(raf , length/2 , -2);
            doTest(raf , length , 0);
            doTest(raf , 0 , -1);
        } finally{
            raf.close();
        }
    }
}
