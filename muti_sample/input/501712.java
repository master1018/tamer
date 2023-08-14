    static class Util
    {
        static private PBEParametersGenerator makePBEGenerator(
            int                     type,
            int                     hash)
        {
            PBEParametersGenerator  generator;
            if (type == PKCS5S1)
            {
                switch (hash)
                {
                case MD5:
                    generator = new PKCS5S1ParametersGenerator(new MD5Digest());
                    break;
                case SHA1:
                    generator = new PKCS5S1ParametersGenerator(new SHA1Digest());
                    break;
                default:
                    throw new IllegalStateException("PKCS5 scheme 1 only supports only MD5 and SHA1.");
                }
            }
            else if (type == PKCS5S2)
            {
                generator = new PKCS5S2ParametersGenerator();
            }
            else if (type == PKCS12)
            {
                switch (hash)
                {
                case MD5:
                    generator = new PKCS12ParametersGenerator(new MD5Digest());
                    break;
                case SHA1:
                    generator = new PKCS12ParametersGenerator(new SHA1Digest());
                    break;
                case SHA256:
                    generator = new PKCS12ParametersGenerator(new SHA256Digest());
                    break;
                default:
                    throw new IllegalStateException("unknown digest scheme for PBE encryption.");
                }
            }
            else
            {
                generator = new OpenSSLPBEParametersGenerator();
            }
            return generator;
        }
        static CipherParameters makePBEParameters(
            JCEPBEKey               pbeKey,
            AlgorithmParameterSpec  spec,
            String                  targetAlgorithm)
        {
            if ((spec == null) || !(spec instanceof PBEParameterSpec))
            {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
            PBEParameterSpec        pbeParam = (PBEParameterSpec)spec;
            PBEParametersGenerator  generator = makePBEGenerator(pbeKey.getType(), pbeKey.getDigest());
            byte[]                  key = pbeKey.getEncoded();
            CipherParameters        param;
            if (pbeKey.shouldTryWrongPKCS12())
            {
                key = new byte[2];
            }
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());
            if (pbeKey.getIvSize() != 0)
            {
                param = generator.generateDerivedParameters(pbeKey.getKeySize(), pbeKey.getIvSize());
            }
            else
            {
                param = generator.generateDerivedParameters(pbeKey.getKeySize());
            }
            if (targetAlgorithm.startsWith("DES"))
            {
                if (param instanceof ParametersWithIV)
                {
                    KeyParameter    kParam = (KeyParameter)((ParametersWithIV)param).getParameters();
                    DESParameters.setOddParity(kParam.getKey());
                }
                else
                {
                    KeyParameter    kParam = (KeyParameter)param;
                    DESParameters.setOddParity(kParam.getKey());
                }
            }
            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }
            return param;
        }
        static CipherParameters makePBEMacParameters(
            JCEPBEKey               pbeKey,
            AlgorithmParameterSpec  spec)
        {
            if ((spec == null) || !(spec instanceof PBEParameterSpec))
            {
                throw new IllegalArgumentException("Need a PBEParameter spec with a PBE key.");
            }
            PBEParameterSpec        pbeParam = (PBEParameterSpec)spec;
            PBEParametersGenerator  generator = makePBEGenerator(pbeKey.getType(), pbeKey.getDigest());
            byte[]                  key = pbeKey.getEncoded();
            CipherParameters        param;
            if (pbeKey.shouldTryWrongPKCS12())
            {
                key = new byte[2];
            }
            generator.init(key, pbeParam.getSalt(), pbeParam.getIterationCount());
            param = generator.generateDerivedMacParameters(pbeKey.getKeySize());
            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }
            return param;
        }
        static CipherParameters makePBEParameters(
            PBEKeySpec              keySpec,
            int                     type,
            int                     hash,
            int                     keySize,
            int                     ivSize)
        {    
            PBEParametersGenerator  generator = makePBEGenerator(type, hash);
            byte[]                  key;
            CipherParameters        param;
            if (type == PKCS12)
            {
                key = PBEParametersGenerator.PKCS12PasswordToBytes(keySpec.getPassword());
            }
            else
            {   
                key = PBEParametersGenerator.PKCS5PasswordToBytes(keySpec.getPassword());
            }
            generator.init(key, keySpec.getSalt(), keySpec.getIterationCount());
            if (ivSize != 0)
            {
                param = generator.generateDerivedParameters(keySize, ivSize);
            }
            else
            {
                param = generator.generateDerivedParameters(keySize);
            }
            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }
            return param;
        }
        static CipherParameters makePBEMacParameters(
            PBEKeySpec              keySpec,
            int                     type,
            int                     hash,
            int                     keySize)
        {
            PBEParametersGenerator  generator = makePBEGenerator(type, hash);
            byte[]                  key;
            CipherParameters        param;
            if (type == PKCS12)
            {
                key = PBEParametersGenerator.PKCS12PasswordToBytes(keySpec.getPassword());
            }
            else
            {   
                key = PBEParametersGenerator.PKCS5PasswordToBytes(keySpec.getPassword());
            }
            generator.init(key, keySpec.getSalt(), keySpec.getIterationCount());
            param = generator.generateDerivedMacParameters(keySize);
            for (int i = 0; i != key.length; i++)
            {
                key[i] = 0;
            }
            return param;
        }
    }
}
