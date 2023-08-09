public class PKIXCertPathBuilderSpi
    extends CertPathBuilderSpi
{
    public CertPathBuilderResult engineBuild(
        CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException 
    {
        if (!(params instanceof PKIXBuilderParameters))
        {
            throw new InvalidAlgorithmParameterException("params must be a PKIXBuilderParameters instance");
        }
        PKIXBuilderParameters pkixParams = (PKIXBuilderParameters)params;
        Collection targets;
        Iterator targetIter;
        List certPathList = new ArrayList();
        X509Certificate cert;
        Collection      certs;
        CertPath        certPath = null;
        Exception       certPathException = null;
        CertSelector certSelect = pkixParams.getTargetCertConstraints();
        if (certSelect == null)
        {
            throw new CertPathBuilderException("targetCertConstraints must be non-null for CertPath building");
        }
        try
        {
            targets = findCertificates(certSelect, pkixParams.getCertStores());
        }
        catch (CertStoreException e)
        {
            throw new CertPathBuilderException(e);
        }
        if (targets.isEmpty())
        {
            throw new CertPathBuilderException("no certificate found matching targetCertContraints");
        }
        CertificateFactory  cFact;
        CertPathValidator   validator;
        try
        {
            cFact = CertificateFactory.getInstance("X.509", "BC");
            validator = CertPathValidator.getInstance("PKIX", "BC");
        }
        catch (Exception e)
        {
            throw new CertPathBuilderException("exception creating support classes: " + e);
        }
        targetIter = targets.iterator();
        while (targetIter.hasNext())
        {
            cert = (X509Certificate)targetIter.next();
            certPathList.clear();
            while (cert != null)
            {
                certPathList.add(cert);
                if (findTrustAnchor(cert, pkixParams.getTrustAnchors()) != null)
                {
                    try
                    {
                        certPath = cFact.generateCertPath(certPathList);
                        PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult)validator.validate(certPath, pkixParams);
                        return new PKIXCertPathBuilderResult(certPath,
                                     result.getTrustAnchor(),
                                     result.getPolicyTree(),
                                     result.getPublicKey());
                    }
                    catch (CertificateException ex)
                    {
                        certPathException = ex;
                    }
                    catch (CertPathValidatorException ex)
                    {
                        certPathException = ex;
                    }
                    cert = null;
                }
                else
                {
                    try
                    {
                        X509Certificate issuer = findIssuer(cert, pkixParams.getCertStores());
                        if (issuer.equals(cert))
                        {
                            cert = null;
                        }
                        else
                        {
                            cert = issuer;
                        }
                    }
                    catch (CertPathValidatorException ex)
                    {
                        certPathException = ex;
                        cert = null;
                    }
                }
            }
        }
        if (certPath != null)
        {
            throw new CertPathBuilderException("found certificate chain, but could not be validated", certPathException);
        }
        throw new CertPathBuilderException("unable to find certificate chain");
    }
    final TrustAnchor findTrustAnchor(
        X509Certificate cert,
        Set             trustAnchors) 
        throws CertPathBuilderException
    {
        Iterator iter = trustAnchors.iterator();
        TrustAnchor trust = null;
        PublicKey trustPublicKey = null;
        Exception invalidKeyEx = null;
        X509CertSelector certSelectX509 = new X509CertSelector();
        try
        {
            certSelectX509.setSubject(cert.getIssuerX500Principal().getEncoded());
        }
        catch (IOException ex)
        {
            throw new CertPathBuilderException("can't get trust anchor principal",null);
        }
        while (iter.hasNext() && trust == null)
        {
            trust = (TrustAnchor)iter.next();
            if (trust.getTrustedCert() != null)
            {
                if (certSelectX509.match(trust.getTrustedCert()))
                {
                    trustPublicKey = trust.getTrustedCert().getPublicKey();
                }
                else
                {
                    trust = null;
                }
            }
            else if (trust.getCAName() != null
                        && trust.getCAPublicKey() != null)
            {
                try
                {
                    X500Principal certIssuer = cert.getIssuerX500Principal();
                    X500Principal caName = new X500Principal(trust.getCAName());
                    if (certIssuer.equals(caName))
                    {
                        trustPublicKey = trust.getCAPublicKey();
                    }
                    else
                    {
                        trust = null;
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    trust = null;
                }
            }
            else
            {
                trust = null;
            }
            if (trustPublicKey != null)
            {
                try
                {
                    cert.verify(trustPublicKey);
                }
                catch (Exception ex)
                {
                    invalidKeyEx = ex;
                    trust = null;
                }
            }
        }
        if (trust == null && invalidKeyEx != null)
        {
            throw new CertPathBuilderException("TrustAnchor found put certificate validation failed",invalidKeyEx);
        }
        return trust;
    }
    private final Collection findCertificates(
        CertSelector    certSelect,
        List            certStores) 
        throws CertStoreException
    {
        Set certs = new HashSet();
        Iterator iter = certStores.iterator();
        while (iter.hasNext())
        {
            CertStore   certStore = (CertStore)iter.next();
            certs.addAll(certStore.getCertificates(certSelect));
        }
        return certs;
    }
    private final X509Certificate findIssuer(
        X509Certificate cert,
        List certStores)
        throws CertPathValidatorException
    {
        Exception invalidKeyEx = null;
        X509CertSelector certSelect = new X509CertSelector();
        try
        {
            certSelect.setSubject(cert.getIssuerX500Principal().getEncoded());
        }
        catch (IOException ex)
        {
            throw new CertPathValidatorException("Issuer not found", null, null, -1);
        }
        Iterator iter;
        try
        {
            iter = findCertificates(certSelect, certStores).iterator();
        }
        catch (CertStoreException e)
        {
            throw new CertPathValidatorException(e);
        }
        X509Certificate issuer = null;
        while (iter.hasNext() && issuer == null)
        {
            issuer = (X509Certificate)iter.next();
            try
            {
                cert.verify(issuer.getPublicKey());
            }
            catch (Exception ex)
            {
                invalidKeyEx = ex;
                issuer = null;
            }
        }
        if (issuer == null && invalidKeyEx == null)
        {
           throw new CertPathValidatorException("Issuer not found", null, null, -1);
        }
        if (issuer == null && invalidKeyEx != null)
        {
            throw new CertPathValidatorException("issuer found but certificate validation failed",invalidKeyEx,null,-1);
        }
        return issuer;
    }
}
