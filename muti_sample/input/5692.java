public class WriteBounds{
    private static void dotest(byte[] b, int off, int len,
                               ByteArrayOutputStream baos)
        throws Exception
    {
        if (b != null) {
            System.err.println("ByteArrayOutStream.write -- b.length = " +
                               b.length + " off = " + off + " len = " + len);
        }
        else{
            System.err.println("ByteArrayOutStream.write - b is null off = " +
                               off + " len = " + len);
        }
        try {
            baos.write(b, off, len);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("IndexOutOfBoundsException is thrown -- OKAY");
        } catch (NullPointerException e) {
            System.err.println("NullPointerException is thrown -- OKAY");
        } catch (Throwable e){
            throw new RuntimeException("Unexpected Exception is thrown");
        }
    }
    public static void main( String argv[] ) throws Exception {
        ByteArrayOutputStream y1;
        byte array1[]={1 , 2 , 3 , 4 , 5};     
        y1 = new ByteArrayOutputStream(5);
        dotest(array1, 0, Integer.MAX_VALUE , y1);
        dotest(array1, 0, array1.length+100, y1);
        dotest(array1, -1, 2, y1);
        dotest(array1, 0, -1, y1);
        dotest(null, 0, 2, y1);
    }
}
