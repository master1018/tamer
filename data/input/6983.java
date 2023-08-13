public class NullContent {
    public static void main(String[] args) throws Exception {
        RefAddr addr1 = new StringRefAddr("Type1", null);
        RefAddr addr2 = new StringRefAddr("Type2", "type2");
        Reference ref = new Reference("com.sun.test.Class", addr1);
        ref.add(addr2);
        try {
            addr1.toString();
            addr2.toString();
            addr1.hashCode();
            addr2.hashCode();
            ref.toString();
        } catch (Exception e) {
            throw new Exception(
                    "Test failed:  does not accept null content: " + e);
        }
    }
}
