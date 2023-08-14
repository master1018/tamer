class RSAUtil
{
    static boolean isRsaOid(
        DERObjectIdentifier algOid)
    {
        return algOid.equals(PKCSObjectIdentifiers.rsaEncryption)
            || algOid.equals(X509ObjectIdentifiers.id_ea_rsa)
            || algOid.equals(PKCSObjectIdentifiers.id_RSASSA_PSS)
            || algOid.equals(PKCSObjectIdentifiers.id_RSAES_OAEP);
    }
    static RSAKeyParameters generatePublicKeyParameter(
        RSAPublicKey    key)
    {
        return new RSAKeyParameters(false, key.getModulus(), key.getPublicExponent());
    }
    static RSAKeyParameters generatePrivateKeyParameter(
        RSAPrivateKey    key)
    {
        if (key instanceof RSAPrivateCrtKey)
        {
            RSAPrivateCrtKey    k = (RSAPrivateCrtKey)key;
            return new RSAPrivateCrtKeyParameters(k.getModulus(),
                k.getPublicExponent(), k.getPrivateExponent(),
                k.getPrimeP(), k.getPrimeQ(), k.getPrimeExponentP(),                            k.getPrimeExponentQ(), k.getCrtCoefficient());
        }
        else
        {
            RSAPrivateKey    k = key;
            return new RSAKeyParameters(true, k.getModulus(), k.getPrivateExponent());
        }
    }
}
