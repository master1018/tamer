public class TrustAnchor {
    private final X500Principal caPrincipal;
    private final String caName;
    private final PublicKey caPublicKey;
    private final X509Certificate trustedCert;
    private final byte[] nameConstraints;
    public TrustAnchor(X509Certificate trustedCert, byte[] nameConstraints) {
        if (trustedCert == null) {
            throw new NullPointerException(Messages.getString("security.5C")); 
        }
        this.trustedCert = trustedCert;
        if (nameConstraints != null) {
            this.nameConstraints = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    this.nameConstraints, 0, this.nameConstraints.length);
            processNameConstraints();
        } else {
            this.nameConstraints = null;
        }
        this.caName = null;
        this.caPrincipal = null;
        this.caPublicKey = null;
    }
    public TrustAnchor(String caName, PublicKey caPublicKey,
            byte[] nameConstraints) {
        if (caName == null) {
            throw new NullPointerException(Messages.getString("security.5D")); 
        }
        this.caName = caName;
        if (caPublicKey == null) {
            throw new NullPointerException(Messages.getString("security.5E")); 
        }
        this.caPublicKey = caPublicKey;
        if (nameConstraints != null) {
            this.nameConstraints = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    this.nameConstraints, 0, this.nameConstraints.length);
            processNameConstraints();
        } else {
            this.nameConstraints = null;
        }
        this.trustedCert = null;
        if (caName.length() == 0) {
            throw new IllegalArgumentException(
                    Messages.getString("security.5F")); 
        }
        this.caPrincipal = new X500Principal(this.caName);
    }
    public TrustAnchor(X500Principal caPrincipal,
            PublicKey caPublicKey, byte[] nameConstraints) {
        if (caPrincipal == null) {
            throw new NullPointerException(Messages.getString("security.60")); 
        }
        this.caPrincipal = caPrincipal;
        if (caPublicKey == null) {
            throw new NullPointerException(Messages.getString("security.5E")); 
        }
        this.caPublicKey = caPublicKey;
        if (nameConstraints != null) {
            this.nameConstraints = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    this.nameConstraints, 0, this.nameConstraints.length);
            processNameConstraints();
        } else {
            this.nameConstraints = null;
        }
        this.trustedCert = null;
        this.caName = caPrincipal.getName();
    }
    public final byte[] getNameConstraints() {
        if (nameConstraints == null) {
            return null;
        }
        byte[] ret = new byte[nameConstraints.length];
            System.arraycopy(nameConstraints, 0,
                    ret, 0, nameConstraints.length);
        return ret;
    }
    public final X509Certificate getTrustedCert() {
        return trustedCert;
    }
    public final X500Principal getCA() {
        return caPrincipal;
    }
    public final String getCAName() {
        return caName;
    }
    public final PublicKey getCAPublicKey() {
        return caPublicKey;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("TrustAnchor: [\n"); 
        if (trustedCert != null) {
            sb.append("Trusted CA certificate: "); 
            sb.append(trustedCert);
            sb.append("\n"); 
        }
        if (caPrincipal != null) {
            sb.append("Trusted CA Name: "); 
            sb.append(caPrincipal);
            sb.append("\n"); 
        }
        if (caPublicKey != null) {
            sb.append("Trusted CA Public Key: "); 
            sb.append(caPublicKey);
            sb.append("\n"); 
        }
        if (nameConstraints != null) {
            sb.append("Name Constraints:\n"); 
            sb.append(Array.toString(nameConstraints, "    ")); 
        }
        sb.append("\n]"); 
        return sb.toString();
    }
    private void processNameConstraints() {
        try {
            NameConstraints.ASN1.decode(nameConstraints);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
