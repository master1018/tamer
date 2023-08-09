public class AuthorityKeyIdentifierStructure
    extends AuthorityKeyIdentifier
{
    public AuthorityKeyIdentifierStructure(
        byte[]  encodedValue)
        throws IOException
    {
        super((ASN1Sequence)X509ExtensionUtil.fromExtensionValue(encodedValue));
    }
    private static ASN1Sequence fromCertificate(
        X509Certificate certificate)
        throws CertificateParsingException
    {
        try
        {
            if (certificate.getVersion() != 3)
            {
                GeneralName          genName = new GeneralName(PrincipalUtil.getIssuerX509Principal(certificate));
                SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                        (ASN1Sequence)new ASN1InputStream(certificate.getPublicKey().getEncoded()).readObject());
                return (ASN1Sequence)new AuthorityKeyIdentifier(
                               info, new GeneralNames(genName), certificate.getSerialNumber()).toASN1Object();
            }
            else
            {
                GeneralName             genName = new GeneralName(PrincipalUtil.getIssuerX509Principal(certificate));
                byte[]                  ext = certificate.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
                if (ext != null)
                {
                    ASN1OctetString     str = (ASN1OctetString)X509ExtensionUtil.fromExtensionValue(ext);
                    return (ASN1Sequence)new AuthorityKeyIdentifier(
                                    str.getOctets(), new GeneralNames(genName), certificate.getSerialNumber()).toASN1Object();
                }
                else
                {
                    SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                            (ASN1Sequence)new ASN1InputStream(certificate.getPublicKey().getEncoded()).readObject());
                    return (ASN1Sequence)new AuthorityKeyIdentifier(
                            info, new GeneralNames(genName), certificate.getSerialNumber()).toASN1Object();
                }
            }
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("Exception extracting certificate details: " + e.toString());
        }
    }
    private static ASN1Sequence fromKey(
        PublicKey pubKey)
        throws InvalidKeyException
    {
        try
        {
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                                        (ASN1Sequence)new ASN1InputStream(pubKey.getEncoded()).readObject());
            return (ASN1Sequence)new AuthorityKeyIdentifier(info).toASN1Object();
        }
        catch (Exception e)
        {
            throw new InvalidKeyException("can't process key: " + e);
        }
    }
    public AuthorityKeyIdentifierStructure(
        X509Certificate certificate)
        throws CertificateParsingException
    {
        super(fromCertificate(certificate));
    }
    public AuthorityKeyIdentifierStructure(
        PublicKey pubKey) 
        throws InvalidKeyException
    {
        super(fromKey(pubKey));
    }
}
