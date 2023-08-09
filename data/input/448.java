public class BigIntEqualsHashCode {
    public static void main(String[] args) throws Exception {
        BigInt bi1 = new BigInt(12345678);
        BigInt bi2 = new BigInt(12345678);
        if ( (bi1.equals(bi2)) == (bi1.hashCode()==bi2.hashCode()) )
            System.out.println("PASSED");
        else
            throw new Exception ("FAILED equals()/hashCode() contract");
    }
}
