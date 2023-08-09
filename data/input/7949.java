public class ConstructorNull {
   public static void main( String[] argv ) throws Exception {
       byte[] data = {10,20};
       int b1,b2;
       ByteArrayInputStream is = new ByteArrayInputStream(data);
       try {
           SequenceInputStream sis = new SequenceInputStream(null,is);
           int b = sis.read();
           throw new RuntimeException("No exception with null stream");
       } catch(NullPointerException e) {
           System.err.println("Test passed: NullPointerException thrown");
       }
   }
}
