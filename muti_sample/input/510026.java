public class AttributeCertificateHolder 
    implements CertSelector
{
    final Holder   holder;
    AttributeCertificateHolder(
        ASN1Sequence seq)
    {
        holder = Holder.getInstance(seq);
    }
    public AttributeCertificateHolder(
        X509Principal issuerName,
        BigInteger    serialNumber)
    {
        holder = new org.bouncycastle.asn1.x509.Holder(new IssuerSerial(
                new GeneralNames(new DERSequence(new GeneralName(issuerName))),
                new DERInteger(serialNumber)));        
    }
    public AttributeCertificateHolder(
        X500Principal issuerName,
        BigInteger    serialNumber)
    {
        this(X509Util.convertPrincipal(issuerName), serialNumber);
    }
    public AttributeCertificateHolder(
        X509Certificate cert) 
        throws CertificateParsingException
    {        
        X509Principal   name;
        try
        {
            name = PrincipalUtil.getIssuerX509Principal(cert);
        }
        catch (Exception e)
        {
            throw new CertificateParsingException(e.getMessage());
        }
        holder = new Holder(new IssuerSerial(generateGeneralNames(name), new DERInteger(cert.getSerialNumber())));
    }
    public AttributeCertificateHolder(
        X509Principal principal) 
    {        
        holder = new Holder(generateGeneralNames(principal));
    }
    public AttributeCertificateHolder(
        X500Principal principal) 
    {
        this(X509Util.convertPrincipal(principal));
    }
    private GeneralNames generateGeneralNames(X509Principal principal)
    {
        return new GeneralNames(new DERSequence(new GeneralName(principal)));
    }
    private boolean matchesDN(X509Principal subject, GeneralNames targets)
    {
        GeneralName[]   names = targets.getNames();
        for (int i = 0; i != names.length; i++)
        {
            GeneralName gn = names[i];
            if (gn.getTagNo() == GeneralName.directoryName)
            {
                try
                {
                    if (new X509Principal(((ASN1Encodable)gn.getName()).getEncoded()).equals(subject))
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
    private Object[] getNames(
        GeneralName[] names)
    {        
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
    private Principal[] getPrincipals(
        GeneralNames    names)
    {
        Object[]    p = this.getNames(names.getNames());
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
    public Principal[] getEntityNames()
    {
        if (holder.getEntityName() != null)
        {
            return getPrincipals(holder.getEntityName());
        }
        return null;
    }
    public Principal[] getIssuer()
    {
        if (holder.getBaseCertificateID() != null)
        {
            return getPrincipals(holder.getBaseCertificateID().getIssuer());
        }
        return null;
    }
    public BigInteger getSerialNumber()
    {
        if (holder.getBaseCertificateID() != null)
        {
            return holder.getBaseCertificateID().getSerial().getValue();
        }
        return null;
    }
    public Object clone()
    {
        return new AttributeCertificateHolder((ASN1Sequence)holder.toASN1Object());
    }
    public boolean match(Certificate cert)
    {
        if (!(cert instanceof X509Certificate))
        {
            return false;
        }
        X509Certificate x509Cert = (X509Certificate)cert;
        try
        {
            if (holder.getBaseCertificateID() != null)
            {
                return holder.getBaseCertificateID().getSerial().getValue().equals(x509Cert.getSerialNumber())
                    && matchesDN(PrincipalUtil.getIssuerX509Principal(x509Cert), holder.getBaseCertificateID().getIssuer());
            }
            if (holder.getEntityName() != null)
            {
                if (matchesDN(PrincipalUtil.getSubjectX509Principal(x509Cert), holder.getEntityName()))
                {
                    return true;
                }
            }
        }
        catch (CertificateEncodingException e)
        {
            return false;
        }
        return false;
    }
}
