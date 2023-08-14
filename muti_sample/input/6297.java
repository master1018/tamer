class TestArray implements Serializable {
    private static final int ARR_SIZE = 5;
    private String[] strArr = new String[ARR_SIZE];
    public TestArray() {
        for (int i = 0; i < ARR_SIZE; i++) {
            strArr[i] = "test" + i;
        }
    }
}
public class MisplacedArrayClassDesc {
    public static final void main(String[] args) throws Exception {
       System.err.println("\nRegression test for CR6313687");
       TestArray object = new TestArray();
       try {
           ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
           ObjectOutputStream out = new ObjectOutputStream(bos) ;
           out.writeObject(object);
           out.close();
           byte[] buf = bos.toByteArray();
           for (int i = 0; i < buf.length; i++) {
               if (buf[i] == ObjectOutputStream.TC_ARRAY) {
                   buf[i] = ObjectOutputStream.TC_OBJECT;
                   break;
               }
           }
           ByteArrayInputStream bais = new ByteArrayInputStream(buf);
           ObjectInputStream in = new ObjectInputStream(bais);
           TestArray ta = (TestArray) in.readObject();
           in.close();
       } catch (InstantiationError e) {
            throw new Error();
       } catch (InvalidClassException e) {
            System.err.println("\nTest passed");
            return;
       }
       throw new Error();
    }
}
