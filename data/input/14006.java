public class RdnToAttrsTest {
    public static void main(String args[])
                throws Exception {
    Rdn rdn = new Rdn("cn = commonName1 + cn = commonName2");
    String attrStr = rdn.toAttributes().toString();
    System.out.println("attributes=" + attrStr);
    if ("{cn=cn: commonName1, commonName2}".equals(attrStr)) {
        System.out.println("The test PASSED");
    } else {
        throw new Exception("The test failed");
    }
}
}
