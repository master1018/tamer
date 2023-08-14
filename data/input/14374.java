public class LDAPCertStoreHelper
    implements CertStoreHelper
{
    public LDAPCertStoreHelper() { }
    @Override
    public CertStore getCertStore(URI uri)
        throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
    {
        return LDAPCertStore.getInstance(LDAPCertStore.getParameters(uri));
    }
    @Override
    public X509CertSelector wrap(X509CertSelector selector,
                                 X500Principal certSubject,
                                 String ldapDN)
        throws IOException
    {
        return new LDAPCertStore.LDAPCertSelector(selector, certSubject, ldapDN);
    }
    @Override
    public X509CRLSelector wrap(X509CRLSelector selector,
                                Collection<X500Principal> certIssuers,
                                String ldapDN)
        throws IOException
    {
        return new LDAPCertStore.LDAPCRLSelector(selector, certIssuers, ldapDN);
    }
}
