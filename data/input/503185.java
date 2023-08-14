public class TBSCertList {
    private final int version; 
    private final AlgorithmIdentifier signature; 
    private final Name issuer;
    private final Date thisUpdate;
    private final Date nextUpdate;
    private final List revokedCertificates;
    private final Extensions crlExtensions;
    private byte[] encoding;
    public static class RevokedCertificate {
        private final BigInteger userCertificate;
        private final Date revocationDate;
        private final Extensions crlEntryExtensions;
        private boolean issuerRetrieved;
        private X500Principal issuer;
        private byte[] encoding;
        public RevokedCertificate(BigInteger userCertificate,
                Date revocationDate, Extensions crlEntryExtensions) {
            this.userCertificate = userCertificate;
            this.revocationDate = revocationDate;
            this.crlEntryExtensions = crlEntryExtensions;
        }
        public Extensions getCrlEntryExtensions() {
            return crlEntryExtensions;
        }
        public BigInteger getUserCertificate() {
            return userCertificate;
        }
        public Date getRevocationDate() {
            return revocationDate;
        }
        public X500Principal getIssuer() {
            if (crlEntryExtensions == null) {
                return null;
            }
            if (!issuerRetrieved) {
                try {
                    issuer =  
                        crlEntryExtensions.valueOfCertificateIssuerExtension();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                issuerRetrieved = true;
            }
            return issuer;
        }
        public byte[] getEncoded() {
            if (encoding == null) {
                encoding = ASN1.encode(this);
            }
            return encoding;
        }
        public boolean equals(Object rc) {
            if (!(rc instanceof RevokedCertificate)) {
                return false;
            }
            RevokedCertificate rcert = (RevokedCertificate) rc;
            return userCertificate.equals(rcert.userCertificate)
                && ((revocationDate.getTime() / 1000) 
                        == (rcert.revocationDate.getTime() / 1000))
                && ((crlEntryExtensions == null)
                    ? rcert.crlEntryExtensions == null
                    : crlEntryExtensions.equals(rcert.crlEntryExtensions));
        }
        public int hashCode() {
        	return userCertificate.hashCode() * 37 + (int)revocationDate.getTime() / 1000
        	+ (crlEntryExtensions == null ? 0 : crlEntryExtensions.hashCode());
        }
        public void dumpValue(StringBuffer buffer, String prefix) {
            buffer.append(prefix).append("Certificate Serial Number: ") 
                .append(userCertificate).append('\n');
            buffer.append(prefix).append("Revocation Date: ") 
                .append(revocationDate);
            if (crlEntryExtensions != null) {
                buffer.append('\n').append(prefix)
                    .append("CRL Entry Extensions: ["); 
                crlEntryExtensions.dumpValue(buffer, prefix + "  "); 
                buffer.append(prefix).append(']');
            }
        }
        public static final ASN1Sequence ASN1 = new ASN1Sequence(
                new ASN1Type[] {ASN1Integer.getInstance(), Time.ASN1,
                Extensions.ASN1}) {
            {
                setOptional(2);
            }
            protected Object getDecodedObject(BerInputStream in) {
                Object[] values = (Object[]) in.content;
                return new RevokedCertificate(
                            new BigInteger((byte[]) values[0]),
                            (Date) values[1],
                            (Extensions) values[2]
                        );
            }
            protected void getValues(Object object, Object[] values) {
                RevokedCertificate rcert = (RevokedCertificate) object;
                values[0] = rcert.userCertificate.toByteArray();
                values[1] = rcert.revocationDate;
                values[2] = rcert.crlEntryExtensions;
            }
        };
    }
    public TBSCertList(AlgorithmIdentifier signature, 
            Name issuer, Date thisUpdate) {
        this.version = 1; 
        this.signature = signature; 
        this.issuer = issuer;
        this.thisUpdate = thisUpdate;
        this.nextUpdate = null;
        this.revokedCertificates = null;
        this.crlExtensions = null;
    }
    public TBSCertList(int version, AlgorithmIdentifier signature, 
            Name issuer, Date thisUpdate, Date nextUpdate, 
            List revokedCertificates, Extensions crlExtensions) {
        this.version = version; 
        this.signature = signature; 
        this.issuer = issuer;
        this.thisUpdate = thisUpdate;
        this.nextUpdate = nextUpdate;
        this.revokedCertificates = revokedCertificates;
        this.crlExtensions = crlExtensions;
    }
    private TBSCertList(int version, AlgorithmIdentifier signature, 
            Name issuer, Date thisUpdate, Date nextUpdate, 
            List revokedCertificates, Extensions crlExtensions,
            byte[] encoding) {
        this.version = version; 
        this.signature = signature; 
        this.issuer = issuer;
        this.thisUpdate = thisUpdate;
        this.nextUpdate = nextUpdate;
        this.revokedCertificates = revokedCertificates;
        this.crlExtensions = crlExtensions;
        this.encoding = encoding;
    }
    public int getVersion() {
        return version;
    }
    public AlgorithmIdentifier getSignature() {
        return signature;
    }
    public Name getIssuer() {
        return issuer;
    }
    public Date getThisUpdate() {
        return thisUpdate;
    }
    public Date getNextUpdate() {
        return nextUpdate;
    }
    public List getRevokedCertificates() {
        return revokedCertificates;
    }
    public Extensions getCrlExtensions() {
        return crlExtensions;
    }
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }
    public boolean equals(Object tbs) {
        if (!(tbs instanceof TBSCertList)) {
            return false;
        }
        TBSCertList tbscert = (TBSCertList) tbs;
        return (version == tbscert.version)
            && (signature.equals(tbscert.signature))
            && (Arrays.equals(issuer.getEncoded(), tbscert.issuer.getEncoded()))
            && ((thisUpdate.getTime() / 1000) 
                    == (tbscert.thisUpdate.getTime() / 1000))
            && ((nextUpdate == null) 
                    ? tbscert.nextUpdate == null
                    : ((nextUpdate.getTime() / 1000) 
                        == (tbscert.nextUpdate.getTime() / 1000)))
            && ((((revokedCertificates == null) 
                            || (tbscert.revokedCertificates == null))
                    && (revokedCertificates == tbscert.revokedCertificates))
                || (revokedCertificates.containsAll(tbscert.revokedCertificates)
                    && (revokedCertificates.size() 
                        == tbscert.revokedCertificates.size())))
            && ((crlExtensions == null)
                    ? tbscert.crlExtensions == null
                    : crlExtensions.equals(tbscert.crlExtensions));
    }
    public int hashCode() {
    	return ((version * 37 + signature.hashCode()) * 37
    		+ issuer.getEncoded().hashCode()) * 37
    		+ (int)thisUpdate.getTime() / 1000;
    }
    public void dumpValue(StringBuffer buffer) {
        buffer.append("X.509 CRL v").append(version); 
        buffer.append("\nSignature Algorithm: ["); 
        signature.dumpValue(buffer);
        buffer.append(']');
        buffer.append("\nIssuer: ").append(issuer.getName(X500Principal.RFC2253)); 
        buffer.append("\n\nThis Update: ").append(thisUpdate); 
        buffer.append("\nNext Update: ").append(nextUpdate).append('\n'); 
        if (revokedCertificates != null) {
            buffer.append("\nRevoked Certificates: ") 
                .append(revokedCertificates.size()).append(" ["); 
            int number = 1;
            for (Iterator it = revokedCertificates.iterator();it.hasNext();) {
                buffer.append("\n  [").append(number++).append(']'); 
                ((RevokedCertificate) it.next()).dumpValue(buffer, "  "); 
                buffer.append('\n');
            }
            buffer.append("]\n"); 
        }
        if (crlExtensions != null) {
            buffer.append("\nCRL Extensions: ") 
                .append(crlExtensions.size()).append(" ["); 
            crlExtensions.dumpValue(buffer, "  "); 
            buffer.append("]\n"); 
        }
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(), 
            AlgorithmIdentifier.ASN1,  
            Name.ASN1, 
            Time.ASN1, 
            Time.ASN1, 
            new ASN1SequenceOf(RevokedCertificate.ASN1), 
            new ASN1Explicit(0, Extensions.ASN1) 
                }) {
        {
            setOptional(0);
            setOptional(4);
            setOptional(5);
            setOptional(6);
        }
        protected Object getDecodedObject(BerInputStream in) 
                        throws IOException {
            Object[] values = (Object[]) in.content;
            return new TBSCertList(
                        (values[0] == null) 
                            ? 1
                            : ASN1Integer.toIntValue(values[0])+1,
                        (AlgorithmIdentifier) values[1],
                        (Name) values[2], 
                        (Date) values[3], 
                        (Date) values[4], 
                        (List) values[5],
                        (Extensions) values[6],
                        in.getEncoded()
                    );
        }
        protected void getValues(Object object, Object[] values) {
            TBSCertList tbs = (TBSCertList) object;
            values[0] = (tbs.version > 1)
                ? ASN1Integer.fromIntValue(tbs.version - 1) : null;
            values[1] = tbs.signature;
            values[2] = tbs.issuer; 
            values[3] = tbs.thisUpdate;
            values[4] = tbs.nextUpdate;
            values[5] = tbs.revokedCertificates;
            values[6] = tbs.crlExtensions;
        }
    };
}
