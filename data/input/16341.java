final public class PagedResultsResponseControl extends BasicControl {
    public static final String OID = "1.2.840.113556.1.4.319";
    private static final long serialVersionUID = -8819778744844514666L;
    private int resultSize;
    private byte[] cookie;
    public PagedResultsResponseControl(String id, boolean criticality,
        byte[] value) throws IOException {
        super(id, criticality, value);
        BerDecoder ber = new BerDecoder(value, 0, value.length);
        ber.parseSeq(null);
        resultSize = ber.parseInt();
        cookie = ber.parseOctetString(Ber.ASN_OCTET_STR, null);
    }
    public int getResultSize() {
        return resultSize;
    }
    public byte[] getCookie() {
        if (cookie.length == 0) {
            return null;
        } else {
            return cookie;
        }
    }
}
