public class EmailAddressEncoding {
    public static void main(String[] args) throws Exception {
        X500Principal p = new X500Principal
                ("c=us, emailaddress=foo@bar, cn=foobar");
        byte[] encoded = p.getEncoded();
        DerInputStream dis = new DerInputStream(encoded);
        DerValue[] nameseq = dis.getSequence(3);
        boolean passed = false;
        for (int i = 0; i < nameseq.length; i++) {
            DerInputStream is = new DerInputStream(nameseq[i].toByteArray());
            DerValue[] ava = is.getSet(3);
            for (int j = 0; j < ava.length; j++) {
                ObjectIdentifier oid = ava[j].data.getOID();
                if (oid.equals(PKCS9Attribute.EMAIL_ADDRESS_OID)) {
                    DerValue value = ava[j].data.getDerValue();
                    if (value.getTag() == DerValue.tag_IA5String) {
                        passed = true;
                        break;
                    } else {
                        throw new SecurityException
                                ("Test failed, expected EMAIL_ADDRESS tag '" +
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
