public final class DHParameterGenerator
extends AlgorithmParameterGeneratorSpi {
    private int primeSize = 1024;
    private int exponentSize = 0;
    private SecureRandom random = null;
    protected void engineInit(int keysize, SecureRandom random) {
        if ((keysize < 512) || (keysize > 1024) || (keysize % 64 != 0)) {
            throw new InvalidParameterException("Keysize must be multiple "
                                                + "of 64, and can only range "
                                                + "from 512 to 1024 "
                                                + "(inclusive)");
        }
        this.primeSize = keysize;
        this.random = random;
    }
    protected void engineInit(AlgorithmParameterSpec genParamSpec,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException {
            if (!(genParamSpec instanceof DHGenParameterSpec)) {
                throw new InvalidAlgorithmParameterException
                    ("Inappropriate parameter type");
            }
            DHGenParameterSpec dhParamSpec = (DHGenParameterSpec)genParamSpec;
            primeSize = dhParamSpec.getPrimeSize();
            if ((primeSize<512) || (primeSize>1024) || (primeSize%64 != 0)) {
                throw new InvalidAlgorithmParameterException
                    ("Modulus size must be multiple of 64, and can only range "
                     + "from 512 to 1024 (inclusive)");
            }
            exponentSize = dhParamSpec.getExponentSize();
            if (exponentSize <= 0) {
                throw new InvalidAlgorithmParameterException
                    ("Exponent size must be greater than zero");
            }
            if (exponentSize >= primeSize) {
                throw new InvalidAlgorithmParameterException
                    ("Exponent size must be less than modulus size");
            }
    }
    protected AlgorithmParameters engineGenerateParameters() {
        AlgorithmParameters algParams = null;
        if (this.exponentSize == 0) {
            this.exponentSize = this.primeSize - 1;
        }
        if (this.random == null)
            this.random = SunJCE.RANDOM;
        try {
            AlgorithmParameterGenerator paramGen;
            DSAParameterSpec dsaParamSpec;
            paramGen = AlgorithmParameterGenerator.getInstance("DSA");
            paramGen.init(this.primeSize, random);
            algParams = paramGen.generateParameters();
            dsaParamSpec = (DSAParameterSpec)
                algParams.getParameterSpec(DSAParameterSpec.class);
            DHParameterSpec dhParamSpec;
            if (this.exponentSize > 0) {
                dhParamSpec = new DHParameterSpec(dsaParamSpec.getP(),
                                                  dsaParamSpec.getG(),
                                                  this.exponentSize);
            } else {
                dhParamSpec = new DHParameterSpec(dsaParamSpec.getP(),
                                                  dsaParamSpec.getG());
            }
            algParams = AlgorithmParameters.getInstance("DH", "SunJCE");
            algParams.init(dhParamSpec);
        } catch (InvalidParameterSpecException e) {
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage());
        }
        return algParams;
    }
}
