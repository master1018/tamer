public class SubjectKeyIdentifierStructure
    extends SubjectKeyIdentifier
{
    private AuthorityKeyIdentifier authKeyID;
    public SubjectKeyIdentifierStructure(
        byte[]  encodedValue)
        throws IOException
    {
        super((ASN1OctetString)X509ExtensionUtil.fromExtensionValue(encodedValue));
    }
    private static ASN1OctetString fromPublicKey(
        PublicKey pubKey)
        throws CertificateParsingException
    {
        try
        {
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(
                (ASN1Sequence)new ASN1InputStream(pubKey.getEncoded()).readObject());
            return (ASN1OctetString)(new SubjectKeyIdentifier(info).toASN1Object());
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("Exception extracting certificate details: " + e.toString());
        }
    }
    public SubjectKeyIdentifierStructure(
        PublicKey pubKey)
        throws CertificateParsingException
    {
        super(fromPublicKey(pubKey));
    }
}
