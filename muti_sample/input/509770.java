public class PrincipalUtil
{
    public static X509Principal getIssuerX509Principal(
        X509Certificate cert)
        throws CertificateEncodingException
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                cert.getTBSCertificate());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            TBSCertificateStructure tbsCert = new TBSCertificateStructure(
                                            (ASN1Sequence)aIn.readObject());
            return new X509Principal(tbsCert.getIssuer());
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }
    public static X509Principal getSubjectX509Principal(
        X509Certificate cert)
        throws CertificateEncodingException
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                cert.getTBSCertificate());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            TBSCertificateStructure tbsCert = new TBSCertificateStructure(
                                            (ASN1Sequence)aIn.readObject());
            return new X509Principal(tbsCert.getSubject());
        }
        catch (IOException e)
        {
            throw new CertificateEncodingException(e.toString());
        }
    }
    public static X509Principal getIssuerX509Principal(
        X509CRL crl)
        throws CRLException
    {
        try
        {
            ByteArrayInputStream    bIn = new ByteArrayInputStream(
                crl.getTBSCertList());
            ASN1InputStream         aIn = new ASN1InputStream(bIn);
            TBSCertList tbsCertList = new TBSCertList(
                                            (ASN1Sequence)aIn.readObject());
            return new X509Principal(tbsCertList.getIssuer());
        }
        catch (IOException e)
        {
            throw new CRLException(e.toString());
        }
    }
}
