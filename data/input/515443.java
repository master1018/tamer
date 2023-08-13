public class RSAEngine
    implements AsymmetricBlockCipher
{
    private RSAKeyParameters        key;
    private boolean                 forEncryption;
    private int                     shift;
    public void init(
        boolean             forEncryption,
        CipherParameters    param)
    {
        this.key = (RSAKeyParameters)param;
        this.forEncryption = forEncryption;
        int     bitSize = key.getModulus().bitLength();
        if (bitSize % 8 == 0)    
        {
            this.shift = 0;
        }
        else
        {
            this.shift = (8 - (bitSize % 8));
        }
    }
    public int getInputBlockSize()
    {
        int     bitSize = key.getModulus().bitLength();
        if (forEncryption)
        {
            return (bitSize + 7) / 8 - 1;
        }
        else
        {
            return (bitSize + 7) / 8;
        }
    }
    public int getOutputBlockSize()
    {
        int     bitSize = key.getModulus().bitLength();
        if (forEncryption)
        {
            return (bitSize + 7) / 8;
        }
        else
        {
            return (bitSize + 7) / 8 - 1;
        }
    }
    public byte[] processBlock(
        byte[]  in,
        int     inOff,
        int     inLen)
    {
        if (inLen > (getInputBlockSize() + 1))
        {
            throw new DataLengthException("input too large for RSA cipher.\n");
        }
        else if (inLen == (getInputBlockSize() + 1) && (in[inOff] & (0x80 >> shift)) != 0)
        {
            throw new DataLengthException("input too large for RSA cipher.\n");
        }
        byte[]  block;
        if (inOff != 0 || inLen != in.length)
        {
            block = new byte[inLen];
            System.arraycopy(in, inOff, block, 0, inLen);
        }
        else
        {
            block = in;
        }
        BigInteger  input = new BigInteger(1, block);
        byte[]      output;
        if (key instanceof RSAPrivateCrtKeyParameters)
        {
            RSAPrivateCrtKeyParameters crtKey = (RSAPrivateCrtKeyParameters)key;
            BigInteger p = crtKey.getP();
            BigInteger q = crtKey.getQ();
            BigInteger dP = crtKey.getDP();
            BigInteger dQ = crtKey.getDQ();
            BigInteger qInv = crtKey.getQInv();
            BigInteger mP, mQ, h, m;
            mP = (input.remainder(p)).modPow(dP, p);
            mQ = (input.remainder(q)).modPow(dQ, q);
            h = mP.subtract(mQ);
            h = h.multiply(qInv);
            h = h.mod(p);               
            m = h.multiply(q);
            m = m.add(mQ);
            output = m.toByteArray();
        }
        else
        {
            output = input.modPow(
                        key.getExponent(), key.getModulus()).toByteArray();
        }
        if (forEncryption)
        {
            if (output[0] == 0 && output.length > getOutputBlockSize())        
            {
                byte[]  tmp = new byte[output.length - 1];
                System.arraycopy(output, 1, tmp, 0, tmp.length);
                return tmp;
            }
            if (output.length < getOutputBlockSize())     
            {
                byte[]  tmp = new byte[getOutputBlockSize()];
                System.arraycopy(output, 0, tmp, tmp.length - output.length, output.length);
                return tmp;
            }
        }
        else
        {
            if (output[0] == 0)        
            {
                byte[]  tmp = new byte[output.length - 1];
                System.arraycopy(output, 1, tmp, 0, tmp.length);
                return tmp;
            }
        }
        return output;
    }
}
