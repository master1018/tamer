@TestTargetClass(ExemptionMechanismSpi.class)
public class ExemptionMechanismSpiTest extends TestCase {
class Mock_ExemptionMechanismSpi extends MyExemptionMechanismSpi{
    @Override
    protected byte[] engineGenExemptionBlob() throws ExemptionMechanismException {
        return super.engineGenExemptionBlob();
    }
    @Override
    protected int engineGenExemptionBlob(byte[] output, int outputOffset) throws ShortBufferException, ExemptionMechanismException {
        return super.engineGenExemptionBlob(output, outputOffset);
    }
    @Override
    protected int engineGetOutputSize(int inputLen) {
        return super.engineGetOutputSize(inputLen);
    }
    @Override
    protected void engineInit(Key key) throws InvalidKeyException, ExemptionMechanismException {
        super.engineInit(key);
    }
    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
        super.engineInit(key, params);
    }
    @Override
    protected void engineInit(Key key, AlgorithmParameters params) throws InvalidKeyException, InvalidAlgorithmParameterException, ExemptionMechanismException {
        super.engineInit(key, params);
    }
}
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ExemptionMechanismSpi",
        args = {}
    )
    public void testExemptionMechanismSpi01() throws Exception {
        Mock_ExemptionMechanismSpi emSpi = new Mock_ExemptionMechanismSpi(){};
        int len = MyExemptionMechanismSpi.getLength();
        byte [] bbRes = emSpi.engineGenExemptionBlob();
        assertEquals("Incorrect length", bbRes.length, len);
        assertEquals("Incorrect result", 
                emSpi.engineGenExemptionBlob(new byte[1], len), len);
        assertEquals("Incorrect output size", 10, emSpi.engineGetOutputSize(100));
        Key key = null;
        AlgorithmParameters params = null;        
        AlgorithmParameterSpec parSpec = null;
        try {
            emSpi.engineInit(key);
            fail("InvalidKeyException must be thrown");
        } catch (InvalidKeyException e) {
        }
        try {
            emSpi.engineInit(key, params);
            fail("InvalidKeyException must be thrown");
        } catch (InvalidKeyException e) {
        }
        try {
            emSpi.engineInit(key, parSpec);
            fail("InvalidKeyException must be thrown");
        } catch (InvalidKeyException e) {
        }
        key = ((MyExemptionMechanismSpi)emSpi).new tmp1Key("Proba", new byte[0]);
        try {
            emSpi.engineInit(key);
            fail("ExemptionMechanismException must be thrown");
        } catch (ExemptionMechanismException e) {
        }
        try {
            emSpi.engineInit(key, params);
            fail("ExemptionMechanismException must be thrown");
        } catch (ExemptionMechanismException e) {
        }
        try {
            emSpi.engineInit(key, parSpec);
            fail("ExemptionMechanismException must be thrown");
        } catch (ExemptionMechanismException e) {
        }
        key = ((MyExemptionMechanismSpi)emSpi).new tmpKey("Proba", new byte[0]);
        emSpi.engineInit(key);
        emSpi.engineInit(key, AlgorithmParameters.getInstance("DH"));
        emSpi.engineInit(key, new RSAKeyGenParameterSpec(10, new BigInteger ("10")));
        assertEquals("Incorrect result", 10, emSpi.engineGetOutputSize(100));
    }
}
