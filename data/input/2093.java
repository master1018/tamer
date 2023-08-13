public class NegativeInitSize {
    public static void main(String[] args) throws Exception {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(-1);
        } catch (IllegalArgumentException e) {
        } catch (Exception e){
            System.out.println(e.getMessage());
            throw new Exception
                ("ByteArrayOutputStream failed to detect negative init size");
        }
        CharArrayReader CAR = new CharArrayReader("test".toCharArray());
        try {
            PushbackReader pbr = new PushbackReader(CAR, -1);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            throw new Exception
                ("PushbackReader failed to detect negative init size");
        }
        try {
            PushbackInputStream pbis = new PushbackInputStream(null, -1);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            throw new Exception
                ("PushbackInputStream failed to detect negative init size");
        }
        ByteArrayOutputStream goodbos = new ByteArrayOutputStream();
        try {
            BufferedOutputStream bos = new BufferedOutputStream(goodbos, -1);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            throw new Exception
                ("BufferedOutputStream failed to detect negative init size");
        }
        byte[] ba = { 123 };
        ByteArrayInputStream goodbis = new ByteArrayInputStream(ba);
        try {
            BufferedInputStream bis = new BufferedInputStream(goodbis, -1);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            throw new Exception
                ("BufferedInputStream failed to detect negative init size");
        }
        try {
            CharArrayWriter caw = new CharArrayWriter(-1);
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            throw new Exception
                ("CharArrayWriter failed to detect negative init size");
        }
    }
}
