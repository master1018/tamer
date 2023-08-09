public class DHAgreement
{
    private DHPrivateKeyParameters  key;
    private DHParameters            dhParams;
    private BigInteger              privateValue;
    private SecureRandom            random;
    public void init(
        CipherParameters    param)
    {
        AsymmetricKeyParameter  kParam;
        if (param instanceof ParametersWithRandom)
        {
            ParametersWithRandom    rParam = (ParametersWithRandom)param;
            this.random = rParam.getRandom();
            kParam = (AsymmetricKeyParameter)rParam.getParameters();
        }
        else
        {
            this.random = new SecureRandom();
            kParam = (AsymmetricKeyParameter)param;
        }
        if (!(kParam instanceof DHPrivateKeyParameters))
        {
            throw new IllegalArgumentException("DHEngine expects DHPrivateKeyParameters");
        }
        this.key = (DHPrivateKeyParameters)kParam;
        this.dhParams = key.getParameters();
    }
    public BigInteger calculateMessage()
    {
        this.privateValue = new BigInteger(
                                    dhParams.getP().bitLength() - 1, 0, random);
        return dhParams.getG().modPow(privateValue, dhParams.getP());
    }
    public BigInteger calculateAgreement(
        DHPublicKeyParameters   pub,
        BigInteger              message)
    {
        if (!pub.getParameters().equals(dhParams))
        {
            throw new IllegalArgumentException("Diffie-Hellman public key has wrong parameters.");
        }
        return message.modPow(key.getX(), dhParams.getP()).multiply(pub.getY().modPow(privateValue, dhParams.getP())).mod(dhParams.getP());
    }
}
