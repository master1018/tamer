public class JCEMac
    extends MacSpi implements PBE
{
    private Mac macEngine;
    private int                     pbeType = PKCS12;
    private int                     pbeHash = SHA1;
    private int                     keySize = 160;
    protected JCEMac(
        Mac macEngine)
    {
        this.macEngine = macEngine;
    }
    protected JCEMac(
        Mac macEngine,
        int pbeType,
        int pbeHash,
        int keySize)
    {
        this.macEngine = macEngine;
        this.pbeType = pbeType;
        this.pbeHash = pbeHash;
        this.keySize = keySize;
    }
    protected void engineInit(
        Key                     key,
        AlgorithmParameterSpec  params)
        throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        CipherParameters        param;
        if (key == null)
        {
            throw new InvalidKeyException("key is null");
        }
        if (key instanceof JCEPBEKey)
        {
            JCEPBEKey   k = (JCEPBEKey)key;
            if (k.getParam() != null)
            {
                param = k.getParam();
            }
            else if (params instanceof PBEParameterSpec)
            {
                param = PBE.Util.makePBEMacParameters(k, params);
            }
            else
            {
                throw new InvalidAlgorithmParameterException("PBE requires PBE parameters to be set.");
            }
        }
        else if (params instanceof IvParameterSpec)
        {
            param = new ParametersWithIV(new KeyParameter(key.getEncoded()), ((IvParameterSpec)params).getIV());
        }
        else if (params == null)
        {
            param = new KeyParameter(key.getEncoded());
        }
        else
        {
            throw new InvalidAlgorithmParameterException("unknown parameter type.");
        }
        macEngine.init(param);
    }
    protected int engineGetMacLength() 
    {
        return macEngine.getMacSize();
    }
    protected void engineReset() 
    {
        macEngine.reset();
    }
    protected void engineUpdate(
        byte    input) 
    {
        macEngine.update(input);
    }
    protected void engineUpdate(
        byte[]  input,
        int     offset,
        int     len) 
    {
        macEngine.update(input, offset, len);
    }
    protected byte[] engineDoFinal() 
    {
        byte[]  out = new byte[engineGetMacLength()];
        macEngine.doFinal(out, 0);
        return out;
    }
    public static class DES
        extends JCEMac
    {
        public DES()
        {
            super(new CBCBlockCipherMac(new DESEngine()));
        }
    }
    public static class DESede
        extends JCEMac
    {
        public DESede()
        {
            super(new CBCBlockCipherMac(new DESedeEngine()));
        }
    }
    public static class DESCFB8
        extends JCEMac
    {
        public DESCFB8()
        {
            super(new CFBBlockCipherMac(new DESEngine()));
        }
    }
    public static class DESedeCFB8
        extends JCEMac
    {
        public DESedeCFB8()
        {
            super(new CFBBlockCipherMac(new DESedeEngine()));
        }
    }
    public static class DESede64
        extends JCEMac
    {
        public DESede64()
        {
            super(new CBCBlockCipherMac(new DESedeEngine(), 64));
        }
    }
    public static class DES9797Alg3
        extends JCEMac
    {
        public DES9797Alg3()
        {
            super(new ISO9797Alg3Mac(new DESEngine()));
        }
    }
    public static class MD5
        extends JCEMac
    {
        public MD5()
        {
            super(new HMac(new MD5Digest()));
        }
    }
    public static class SHA1
        extends JCEMac
    {
        public SHA1()
        {
            super(new HMac(new SHA1Digest()));
        }
    }
    public static class SHA224
        extends JCEMac
    {
        public SHA224()
        {
            super(new HMac(new SHA224Digest()));
        }
    }
    public static class SHA256
        extends JCEMac
    {
        public SHA256()
        {
            super(new HMac(new SHA256Digest()));
        }
    }
    public static class SHA384
        extends JCEMac
    {
        public SHA384()
        {
            super(new HMac(new SHA384Digest()));
        }
    }
    public static class OldSHA384
        extends JCEMac
    {
        public OldSHA384()
        {
            super(new OldHMac(new SHA384Digest()));
        }
    }
    public static class SHA512
        extends JCEMac
    {
        public SHA512()
        {
            super(new HMac(new SHA512Digest()));
        }
    }
    public static class OldSHA512
        extends JCEMac
    {
        public OldSHA512()
        {
            super(new OldHMac(new SHA512Digest()));
        }
    }
    public static class PBEWithSHA
        extends JCEMac
    {
        public PBEWithSHA()
        {
            super(new HMac(new SHA1Digest()), PKCS12, SHA1, 160);
        }
    }
}
