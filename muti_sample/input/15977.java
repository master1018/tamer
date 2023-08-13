public class PBEKeyTest {
    public static void main(String[] args) throws Exception {
        SecretKeyFactory fac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        char[] pass = new char[] { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };
        PBEKeySpec spec = new PBEKeySpec(pass);
        SecretKey skey = fac.generateSecret(spec);
        KeySpec spec1 = fac.getKeySpec(skey, PBEKeySpec.class);
        SecretKey skey1 = fac.generateSecret(spec1);
        if (!skey.equals(skey1))
            throw new Exception("Equal keys not equal");
        System.out.println(new String(((PBEKeySpec)spec1).getPassword()));
        pass = new char[] { 'p', 'a', 's', 's', 'w', 'o', 'r', '\u0019' };
        spec = new PBEKeySpec(pass);
        try {
            skey = fac.generateSecret(spec);
            throw new Exception("Expected exception not thrown");
        } catch (Exception e) {
            System.out.println("Expected exception thrown");
        }
    }
}
