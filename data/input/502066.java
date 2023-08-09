public class CipherKeyGenerator
{
    protected SecureRandom  random;
    protected int           strength;
    public void init(
        KeyGenerationParameters param)
    {
        this.random = param.getRandom();
        this.strength = (param.getStrength() + 7) / 8;
    }
    public byte[] generateKey()
    {
        byte[]  key = new byte[strength];
        random.nextBytes(key);
        return key;
    }
}
