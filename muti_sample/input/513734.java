public class DESedeWrapEngine
    implements Wrapper
{
   private CBCBlockCipher engine;
   private KeyParameter param;
   private ParametersWithIV paramPlusIV;
   private byte[] iv;
   private boolean forWrapping;
   private static final byte[] IV2 = { (byte) 0x4a, (byte) 0xdd, (byte) 0xa2,
                                       (byte) 0x2c, (byte) 0x79, (byte) 0xe8,
                                       (byte) 0x21, (byte) 0x05 };
    Digest  sha1 = new SHA1Digest();
    byte[]  digest = new byte[20];
    public void init(boolean forWrapping, CipherParameters param)
    {
        this.forWrapping = forWrapping;
        this.engine = new CBCBlockCipher(new DESedeEngine());
        if (param instanceof KeyParameter)
        {
            this.param = (KeyParameter)param;
            if (this.forWrapping)
            {
                this.iv = new byte[8];
                SecureRandom sr = new SecureRandom();
                sr.nextBytes(iv);
                this.paramPlusIV = new ParametersWithIV(this.param, this.iv);
            }
        }
        else if (param instanceof ParametersWithIV)
        {
            this.paramPlusIV = (ParametersWithIV)param;
            this.iv = this.paramPlusIV.getIV();
            this.param = (KeyParameter)this.paramPlusIV.getParameters();
            if (this.forWrapping)
            {
                if ((this.iv == null) || (this.iv.length != 8))
                {
                    throw new IllegalArgumentException("IV is not 8 octets");
                }
            }
            else
            {
                throw new IllegalArgumentException(
                        "You should not supply an IV for unwrapping");
            }
        }
    }
   public String getAlgorithmName() 
   {
      return "DESede";
   }
   public byte[] wrap(byte[] in, int inOff, int inLen) 
   {
      if (!forWrapping) 
      {
         throw new IllegalStateException("Not initialized for wrapping");
      }
      byte keyToBeWrapped[] = new byte[inLen];
      System.arraycopy(in, inOff, keyToBeWrapped, 0, inLen);
      byte[] CKS = calculateCMSKeyChecksum(keyToBeWrapped);
      byte[] WKCKS = new byte[keyToBeWrapped.length + CKS.length];
      System.arraycopy(keyToBeWrapped, 0, WKCKS, 0, keyToBeWrapped.length);
      System.arraycopy(CKS, 0, WKCKS, keyToBeWrapped.length, CKS.length);
      byte TEMP1[] = new byte[WKCKS.length];
      System.arraycopy(WKCKS, 0, TEMP1, 0, WKCKS.length);
      int noOfBlocks = WKCKS.length / engine.getBlockSize();
      int extraBytes = WKCKS.length % engine.getBlockSize();
      if (extraBytes != 0) 
      {
         throw new IllegalStateException("Not multiple of block length");
      }
      engine.init(true, paramPlusIV);
      for (int i = 0; i < noOfBlocks; i++) 
      {
         int currentBytePos = i * engine.getBlockSize();
         engine.processBlock(TEMP1, currentBytePos, TEMP1, currentBytePos);
      }
      byte[] TEMP2 = new byte[this.iv.length + TEMP1.length];
      System.arraycopy(this.iv, 0, TEMP2, 0, this.iv.length);
      System.arraycopy(TEMP1, 0, TEMP2, this.iv.length, TEMP1.length);
      byte[] TEMP3 = new byte[TEMP2.length];
      for (int i = 0; i < TEMP2.length; i++) 
      {
         TEMP3[i] = TEMP2[TEMP2.length - (i + 1)];
      }
      ParametersWithIV param2 = new ParametersWithIV(this.param, IV2);
      this.engine.init(true, param2);
      for (int i = 0; i < noOfBlocks + 1; i++) 
      {
         int currentBytePos = i * engine.getBlockSize();
         engine.processBlock(TEMP3, currentBytePos, TEMP3, currentBytePos);
      }
      return TEMP3;
   }
    public byte[] unwrap(byte[] in, int inOff, int inLen)
           throws InvalidCipherTextException 
    {
        if (forWrapping)
        {
            throw new IllegalStateException("Not set for unwrapping");
        }
        if (in == null)
        {
            throw new InvalidCipherTextException("Null pointer as ciphertext");
        }
        if (inLen % engine.getBlockSize() != 0)
        {
            throw new InvalidCipherTextException("Ciphertext not multiple of "
                    + engine.getBlockSize());
        }
      ParametersWithIV param2 = new ParametersWithIV(this.param, IV2);
      this.engine.init(false, param2);
      byte TEMP3[] = new byte[inLen];
      System.arraycopy(in, inOff, TEMP3, 0, inLen);
      for (int i = 0; i < (TEMP3.length / engine.getBlockSize()); i++) 
      {
         int currentBytePos = i * engine.getBlockSize();
         engine.processBlock(TEMP3, currentBytePos, TEMP3, currentBytePos);
      }
      byte[] TEMP2 = new byte[TEMP3.length];
      for (int i = 0; i < TEMP3.length; i++) 
      {
         TEMP2[i] = TEMP3[TEMP3.length - (i + 1)];
      }
      this.iv = new byte[8];
      byte[] TEMP1 = new byte[TEMP2.length - 8];
      System.arraycopy(TEMP2, 0, this.iv, 0, 8);
      System.arraycopy(TEMP2, 8, TEMP1, 0, TEMP2.length - 8);
      this.paramPlusIV = new ParametersWithIV(this.param, this.iv);
      this.engine.init(false, this.paramPlusIV);
      byte[] WKCKS = new byte[TEMP1.length];
      System.arraycopy(TEMP1, 0, WKCKS, 0, TEMP1.length);
      for (int i = 0; i < (WKCKS.length / engine.getBlockSize()); i++) 
      {
         int currentBytePos = i * engine.getBlockSize();
         engine.processBlock(WKCKS, currentBytePos, WKCKS, currentBytePos);
      }
      byte[] result = new byte[WKCKS.length - 8];
      byte[] CKStoBeVerified = new byte[8];
      System.arraycopy(WKCKS, 0, result, 0, WKCKS.length - 8);
      System.arraycopy(WKCKS, WKCKS.length - 8, CKStoBeVerified, 0, 8);
      if (!checkCMSKeyChecksum(result, CKStoBeVerified)) 
      {
         throw new InvalidCipherTextException(
            "Checksum inside ciphertext is corrupted");
      }
      return result;
   }
    private byte[] calculateCMSKeyChecksum(
        byte[] key)
    {
        byte[]  result = new byte[8];
        sha1.update(key, 0, key.length);
        sha1.doFinal(digest, 0);
        System.arraycopy(digest, 0, result, 0, 8);
        return result;
    }
    private boolean checkCMSKeyChecksum(
        byte[] key,
        byte[] checksum)
    {
        byte[] calculatedChecksum = calculateCMSKeyChecksum(key);
        if (checksum.length != calculatedChecksum.length)
        {
            return false;
        }
        for (int i = 0; i != checksum.length; i++)
        {
            if (checksum[i] != calculatedChecksum[i])
            {
                return false;
            }
        }
        return true;
    }
}
