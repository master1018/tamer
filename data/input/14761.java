public class PutAndPutAll {
    public static void main(String[] args) throws Exception {
        Attributes at = new Attributes();
        try{
            at.put("this is not an Attributes.Name", "value");
            throw new Exception("put should check for non Attributes.Name names");
        } catch (ClassCastException e) {
        }
        try{
            at.put(new Attributes.Name("name"), new Integer(0));
            throw new Exception("put should check for non String values");
        } catch (ClassCastException e) {
        }
        try {
            at.putAll(new HashMap());
            throw new Exception("putAll should check for non Attributes maps");
        } catch (ClassCastException e) {
        }
    }
}
