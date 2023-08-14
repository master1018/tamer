public class DHParametersGenerator
{
    private int             size;
    private int             certainty;
    private SecureRandom    random;
    private static BigInteger ONE = BigInteger.valueOf(1);
    private static BigInteger TWO = BigInteger.valueOf(2);
    public void init(
        int             size,
        int             certainty,
        SecureRandom    random)
    {
        this.size = size;
        this.certainty = certainty;
        this.random = random;
    }
    public DHParameters generateParameters()
    {
        BigInteger      g, p, q;
        int             qLength = size - 1;
        for (;;)
        {
            q = new BigInteger(qLength, certainty, random);
            p = q.multiply(TWO).add(ONE);
            if (p.isProbablePrime(certainty))
            {
                break;
            }
        }
        for (;;)
        {
            g = new BigInteger(qLength, random);
            if (g.modPow(TWO, p).equals(ONE))
            {
                continue;
            }
            if (g.modPow(q, p).equals(ONE))
            {
                continue;
            }
            break;
        }
        return new DHParameters(p, g, q, 2);
    }
}
