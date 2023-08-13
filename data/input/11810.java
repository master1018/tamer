public class NegativeBufferSize {
    public static void main(String argv[]) throws Exception {
        try {
            MyStringWriter s = new MyStringWriter(-1);
        } catch(IllegalArgumentException e){
            return;
        }
        throw new Exception("StringWriter constructor must not accept < 0 "
                            + " buffer sizes");
    }
}
class MyStringWriter extends StringWriter {
    MyStringWriter(int bufsize){
        super(bufsize);
    }
}
