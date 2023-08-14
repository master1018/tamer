public class PublicKeyFactory
{
    public static AsymmetricKeyParameter createKey(
        SubjectPublicKeyInfo    keyInfo)
        throws IOException
    {
        AlgorithmIdentifier     algId = keyInfo.getAlgorithmId();
        if (algId.getObjectId().equals(PKCSObjectIdentifiers.rsaEncryption)
            || algId.getObjectId().equals(X509ObjectIdentifiers.id_ea_rsa))
        {
            RSAPublicKeyStructure   pubKey = new RSAPublicKeyStructure((ASN1Sequence)keyInfo.getPublicKey());
            return new RSAKeyParameters(false, pubKey.getModulus(), pubKey.getPublicExponent());
        }
        else if (algId.getObjectId().equals(PKCSObjectIdentifiers.dhKeyAgreement)
                 || algId.getObjectId().equals(X9ObjectIdentifiers.dhpublicnumber))
        {
            DHParameter params = new DHParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger  derY = (DERInteger)keyInfo.getPublicKey();
            return new DHPublicKeyParameters(derY.getValue(), new DHParameters(params.getP(), params.getG()));
        }
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_dsa)
                 || algId.getObjectId().equals(OIWObjectIdentifiers.dsaWithSHA1))
        {
            DSAParameter    params = new DSAParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger      derY = (DERInteger)keyInfo.getPublicKey();
            return new DSAPublicKeyParameters(derY.getValue(), new DSAParameters(params.getP(), params.getQ(), params.getG()));
        }
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
}
