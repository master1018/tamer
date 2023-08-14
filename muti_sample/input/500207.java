public class PolicyQualifierInfo {
    private final byte[] encoded;
    private final String policyQualifierId;
    private final byte[] policyQualifier;
    public PolicyQualifierInfo(byte[] encoded) throws IOException {
        if (encoded == null) {
            throw new NullPointerException(Messages.getString("security.0A")); 
        }
        if (encoded.length == 0) {
            throw new IOException(Messages.getString("security.69")); 
        }
        this.encoded = new byte[encoded.length];
        System.arraycopy(encoded, 0, this.encoded, 0, this.encoded.length);
        Object[] decoded = (Object[]) org.apache.harmony.security.x509.PolicyQualifierInfo.ASN1
                .decode(this.encoded);
        policyQualifierId = ObjectIdentifier.toString((int[]) decoded[0]);
        policyQualifier = (byte[]) decoded[1];
    }
    public final byte[] getEncoded() {
        byte[] ret = new byte[encoded.length];
        System.arraycopy(encoded, 0, ret, 0, encoded.length);
        return ret;
    }
    public final String getPolicyQualifierId() {
        return policyQualifierId;
    }
    public final byte[] getPolicyQualifier() {
        if (policyQualifier == null) {
            return null;
        }
        byte[] ret = new byte[policyQualifier.length];
        System.arraycopy(policyQualifier, 0, ret, 0, policyQualifier.length);
        return ret;
    }
    public String toString() {
        StringBuilder sb =
            new StringBuilder("PolicyQualifierInfo: [\npolicyQualifierId: "); 
        sb.append(policyQualifierId);
        sb.append("\npolicyQualifier: \n"); 
        sb.append(Array.toString(policyQualifier, " ")); 
        sb.append("]"); 
        return sb.toString();
    }
}
