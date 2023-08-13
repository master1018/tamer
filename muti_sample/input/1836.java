public class DomainComponentEncoding {
    public static void main(String[] args) throws Exception {
        testDN("cn=hello, dc=com, dc=example");
        testDN("cn=hello, dc=\"com\", dc=example");
    }
    private static void testDN(String dn) throws Exception {
        X500Principal p = new X500Principal(dn);
        byte[] encoded = p.getEncoded();
        DerInputStream dis = new DerInputStream(encoded);
        DerValue[] nameseq = dis.getSequence(3);
        boolean passed = false;
        for (int i = 0; i < nameseq.length; i++) {
            DerInputStream is = new DerInputStream(nameseq[i].toByteArray());
            DerValue[] ava = is.getSet(3);
            for (int j = 0; j < ava.length; j++) {
                ObjectIdentifier oid = ava[j].data.getOID();
                if (oid.equals(X500Name.DOMAIN_COMPONENT_OID)) {
                    DerValue value = ava[j].data.getDerValue();
                    if (value.getTag() == DerValue.tag_IA5String) {
                        passed = true;
                        break;
                    } else {
                        throw new SecurityException
                                ("Test failed, expected DOMAIN_COMPONENT tag '" +
                                DerValue.tag_IA5String +
                                "', got '" +
                                value.getTag() + "'");
                    }
                }
            }
            if (passed) {
                break;
            }
        }
        if (passed) {
            System.out.println("Test passed");
        } else {
            throw new SecurityException("Test failed");
        }
    }
}
