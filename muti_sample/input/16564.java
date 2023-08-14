public class CompareToEqualsTests {
    public static void main(String args[])
                throws Exception {
        String names1[] = new String [] {
                "ou=Sales+cn=Bob", "ou=Sales+cn=Bob", "ou=Sales+cn=Bob",
                "ou=Sales+cn=Scott+c=US", "cn=config"};
        String names2[] = new String [] {
                "ou=Sales+cn=Bob", "cn=Bob+ou=Sales", "ou=Sales+cn=Scott",
                "ou=Sales+cn=Scott", "Cn=COnFIG"};
        int expectedResults[] = {0, 0, -1, -1, 0};
        for (int i = 0; i < names1.length; i++) {
            checkResults(new LdapName(names1[i]),
                        new LdapName(names2[i]), expectedResults[i]);
        }
        byte[] value = "abcxyz".getBytes();
        Rdn rdn1 = new Rdn("binary", value);
        ArrayList rdns1 = new ArrayList();
        rdns1.add(rdn1);
        LdapName l1 = new LdapName(rdns1);
        Rdn rdn2 = new Rdn("binary", value);
        ArrayList rdns2 = new ArrayList();
        rdns2.add(rdn2);
        LdapName l2 = new LdapName(rdns2);
        checkResults(l1, l2, 0);
        l2 = new LdapName("binary=#61626378797A");
        checkResults(l1, l2, 0);
        l2 = new LdapName("binary=#61626378797B");
        checkResults(l1, l2, -1);
        System.out.println("Tests passed");
    }
    static void checkResults(LdapName name1, LdapName name2, int expectedResult)
                throws Exception {
        System.out.println("Checking name1: " + name1 +
                " and name2: " + name2);
        boolean isEquals = (expectedResult == 0) ? true : false;
        int result = name1.compareTo(name2);
        if ((isEquals && (result != expectedResult)) ||
                isPositive(result) != isPositive(expectedResult)) {
            throw new Exception(
                "Comparison test failed for name1:" +
                name1 + " name2:" + name2 +
                ", expected (1 => positive, -1 => negetive): " +
                expectedResult + " but got: " + result);
        }
        if (name1.equals(name2) != isEquals) {
            throw new Exception("Equality test failed for name1: " +
                        name1 + " name2:" + name2 + ", expected: " +
                        isEquals);
        }
        if (isEquals && (name1.hashCode() != name2.hashCode())) {
           System.out.println("name1.hashCode(): " + name1.hashCode() +
                                " name2.hashCode(): " + name2.hashCode());
            throw new Exception("hashCode test failed for name1:" +
                        name1 + " name2:" + name2);
        }
    }
    static boolean isPositive(int n) {
        return (n >= 0) ? true : false;
    }
}
