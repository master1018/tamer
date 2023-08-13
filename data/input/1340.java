public class InsertNullString {
    public static void main(String[] args) throws Exception {
        StringBuffer s = new StringBuffer("FOOBAR");
        try {
            String nullstr = null;
            s.insert(3, nullstr); 
            if (!s.toString().equals("FOOnullBAR")) {
                throw new Exception("StringBuffer.insert() did not insert!");
            }
        } catch (NullPointerException npe) {
            throw new Exception("StringBuffer.insert() of null String reference threw a NullPointerException");
        }
    }
}
