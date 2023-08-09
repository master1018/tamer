public class DSASigner
    implements DSA
{
    DSAKeyParameters key;
    SecureRandom    random;
    public void init(
        boolean                 forSigning,
        CipherParameters        param)
    {
        if (forSigning)
        {
            if (param instanceof ParametersWithRandom)
            {
                ParametersWithRandom    rParam = (ParametersWithRandom)param;
                this.random = rParam.getRandom();
                this.key = (DSAPrivateKeyParameters)rParam.getParameters();
            }
            else
            {
                this.random = new SecureRandom();
                this.key = (DSAPrivateKeyParameters)param;
            }
        }
        else
        {
            this.key = (DSAPublicKeyParameters)param;
        }
    }
    public BigInteger[] generateSignature(
        byte[] message)
    {
        BigInteger      m = new BigInteger(1, message);
        DSAParameters   params = key.getParameters();
        BigInteger      k;
        int                  qBitLength = params.getQ().bitLength();
        do 
        {
            k = new BigInteger(qBitLength, random);
        }
        while (k.compareTo(params.getQ()) >= 0);
        BigInteger  r = params.getG().modPow(k, params.getP()).mod(params.getQ());
        k = k.modInverse(params.getQ()).multiply(
                    m.add(((DSAPrivateKeyParameters)key).getX().multiply(r)));
        BigInteger  s = k.mod(params.getQ());
        BigInteger[]  res = new BigInteger[2];
        res[0] = r;
        res[1] = s;
        return res;
    }
    public boolean verifySignature(
        byte[]      message,
        BigInteger  r,
        BigInteger  s)
    {
        BigInteger      m = new BigInteger(1, message);
        DSAParameters   params = key.getParameters();
        BigInteger      zero = BigInteger.valueOf(0);
        if (zero.compareTo(r) >= 0 || params.getQ().compareTo(r) <= 0)
        {
            return false;
        }
        if (zero.compareTo(s) >= 0 || params.getQ().compareTo(s) <= 0)
        {
            return false;
        }
        BigInteger  w = s.modInverse(params.getQ());
        BigInteger  u1 = m.multiply(w).mod(params.getQ());
        BigInteger  u2 = r.multiply(w).mod(params.getQ());
        u1 = params.getG().modPow(u1, params.getP());
        u2 = ((DSAPublicKeyParameters)key).getY().modPow(u2, params.getP());
        BigInteger  v = u1.multiply(u2).mod(params.getP()).mod(params.getQ());
        return v.equals(r);
    }
}
