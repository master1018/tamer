public class DHKeyPairGenerator
    implements AsymmetricCipherKeyPairGenerator
{
    private DHKeyGeneratorHelper helper = DHKeyGeneratorHelper.INSTANCE;
    private DHKeyGenerationParameters param;
    public void init(
        KeyGenerationParameters param)
    {
        this.param = (DHKeyGenerationParameters)param;
    }
    public AsymmetricCipherKeyPair generateKeyPair()
    {
        BigInteger      p, x, y;
        DHParameters    dhParams = param.getParameters();
        p = dhParams.getP();
        x = helper.calculatePrivate(p, param.getRandom(), dhParams.getJ()); 
        y = helper.calculatePublic(p, dhParams.getG(), x);
        return new AsymmetricCipherKeyPair(
                new DHPublicKeyParameters(y, dhParams),
                new DHPrivateKeyParameters(x, dhParams));
    }
}
