public class PrivateKeyFactory
{
    public static AsymmetricKeyParameter createKey(
        PrivateKeyInfo    keyInfo)
        throws IOException
    {
        AlgorithmIdentifier     algId = keyInfo.getAlgorithmId();
        if (algId.getObjectId().equals(PKCSObjectIdentifiers.rsaEncryption))
        {
            RSAPrivateKeyStructure  keyStructure = new RSAPrivateKeyStructure((ASN1Sequence)keyInfo.getPrivateKey());
            return new RSAPrivateCrtKeyParameters(
                                        keyStructure.getModulus(),
                                        keyStructure.getPublicExponent(),
                                        keyStructure.getPrivateExponent(),
                                        keyStructure.getPrime1(),
                                        keyStructure.getPrime2(),
                                        keyStructure.getExponent1(),
                                        keyStructure.getExponent2(),
                                        keyStructure.getCoefficient());
        }
        else if (algId.getObjectId().equals(PKCSObjectIdentifiers.dhKeyAgreement))
        {
            DHParameter     params = new DHParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger      derX = (DERInteger)keyInfo.getPrivateKey();
            return new DHPrivateKeyParameters(derX.getValue(), new DHParameters(params.getP(), params.getG()));
        }
        else if (algId.getObjectId().equals(X9ObjectIdentifiers.id_dsa))
        {
            DSAParameter    params = new DSAParameter((ASN1Sequence)keyInfo.getAlgorithmId().getParameters());
            DERInteger      derX = (DERInteger)keyInfo.getPrivateKey();
            return new DSAPrivateKeyParameters(derX.getValue(), new DSAParameters(params.getP(), params.getQ(), params.getG()));
        }
        else
        {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
}
