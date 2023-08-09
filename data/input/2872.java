public class SerializeWithException {
   public static void main (String argv[]) {
       System.err.println("\nRegression test for testing of " +
            "serialization when there is exceptions on the I/O stream \n");
       try {
           int i = 123456;
           byte b = 12;
           short s = 45;
           char c = 'A';
           long l = 1234567890000L;
           float f = 3.14159f;
           double d = f*2;
           boolean z = true;
           String string = "The String";
           PrimitivesTest prim = new PrimitivesTest();
           for (int offset = 0; offset < 200; offset++) {
               ExceptionOutputStream ostream;
               boolean expect_exception = false;
               IOException exception = null;
               try {
                   expect_exception = true;
                   exception = null;
                   ostream = new ExceptionOutputStream();
                   ostream.setExceptionOffset(offset);
                   ObjectOutputStream p = new ObjectOutputStream(ostream);
                   p.writeInt(i);
                   p.writeByte(b);
                   p.writeShort(s);
                   p.writeChar(c);
                   p.writeLong(l);
                   p.writeFloat(f);
                   p.writeDouble(d);
                   p.writeBoolean(z);
                   p.writeUTF(string);
                   p.writeObject(string);
                   p.writeObject(prim);
                   p.flush();
                   expect_exception = false;
               } catch (IOException ee) {
                   exception = ee;
               }
               if (expect_exception && exception == null) {
                   System.err.println("\nIOException did not occur at " +
                        "offset " + offset);
                   throw new Error();
               }
               if (!expect_exception && exception != null) {
                   System.err.println("\n " + exception.toString() +
                       " not expected at offset " + offset);
                   throw new Error();
               }
           }
           System.err.println("\nTEST PASSED");
       } catch (Exception e) {
           System.err.print("TEST FAILED: ");
           e.printStackTrace();
           throw new Error();
       }
    }
}
class ExceptionOutputStream extends OutputStream {
    private int exceptionOffset = 0;
    private int currentOffset = 0;
    public void write(int b) throws IOException {
        if (currentOffset >= exceptionOffset) {
            throw new IOException("Debug exception");
        }
        currentOffset++;
    }
    public void setExceptionOffset(int offset) {
        exceptionOffset = offset;
        currentOffset = 0;
    }
}
