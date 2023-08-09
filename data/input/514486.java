public class AttributeCertificateIssuer
    implements CertSelector
{
    final ASN1Encodable  form;
    AttributeCertificateIssuer(
        AttCertIssuer issuer)
    {
        form = issuer.getIssuer();
    }
    public AttributeCertificateIssuer(
        X500Principal principal) 
        throws IOException 
    {        
        this(new X509Principal(principal.getEncoded()));
    }
    public AttributeCertificateIssuer(
        X509Principal principal) 
    {        
        form = new V2Form(new GeneralNames(new DERSequence(new GeneralName(principal))));
    }
    private Object[] getNames()
    {
        GeneralNames    name;
        if (form instanceof V2Form)
        {
            name = ((V2Form)form).getIssuerName();
        }
        else
        {
            name = (GeneralNames)form;
        }
        GeneralName[]   names = name.getNames();
        List        l = new ArrayList(names.length);
        for (int i = 0; i != names.length; i++)
        {
            if (names[i].getTagNo() == GeneralName.directoryName)
            {
                try
                {
                    l.add(new X500Principal(((ASN1Encodable)names[i].getName()).getEncoded()));
                }
                catch (IOException e)
                {
                    throw new RuntimeException("badly formed Name object");
                }
            }
        }
        return l.toArray(new Object[l.size()]);
    }
    public Principal[] getPrincipals()
    {
        Object[]    p = this.getNames();
        List        l = new ArrayList();
        for (int i = 0; i != p.length; i++)
        {
            if (p[i] instanceof Principal)
            {
                l.add(p[i]);
            }
        }
        return (Principal[])l.toArray(new Principal[l.size()]);
    }
    private boolean matchesDN(X500Principal subject, GeneralNames targets)
    {
        GeneralName[]   names = targets.getNames();
        for (int i = 0; i != names.length; i++)
        {
            GeneralName gn = names[i];
            if (gn.getTagNo() == GeneralName.directoryName)
            {
                try
                {
                    if (new X500Principal(((ASN1Encodable)gn.getName()).getEncoded()).equals(subject))
                    {
                        return true;
                    }
                }
                catch (IOException e)
                {
                }
            }
        }
        return false;
    }
    public Object clone()
    {
        return new AttributeCertificateIssuer(AttCertIssuer.getInstance(form));
    }
    public boolean match(Certificate cert)
    {
        if (!(cert instanceof X509Certificate))
        {
            return false;
        }
        X509Certificate x509Cert = (X509Certificate)cert;
        if (form instanceof V2Form)
        {
            V2Form issuer = (V2Form)form;
            if (issuer.getBaseCertificateID() != null)
            {
                return issuer.getBaseCertificateID().getSerial().getValue().equals(x509Cert.getSerialNumber())
                    && matchesDN(x509Cert.getIssuerX500Principal(), issuer.getBaseCertificateID().getIssuer());
            }
            GeneralNames name = issuer.getIssuerName();
            if (matchesDN(x509Cert.getSubjectX500Principal(), name))
            {
                return true;
            }
        }
        else
        {
            GeneralNames name = (GeneralNames)form;
            if (matchesDN(x509Cert.getSubjectX500Principal(), name))
            {
                return true;
            }
        }
        return false;
    }
}
