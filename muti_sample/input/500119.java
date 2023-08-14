@TestTargetClass(OAEPParameterSpec.class)
public class OAEPParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "OAEPParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, javax.crypto.spec.PSource.class}
    )
    public void testOAEPParameterSpec() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;
        try {
            new OAEPParameterSpec(null, mgfName, mgfSpec, pSrc);
            fail("NullPointerException should be thrown in the case of "
                    + "null mdName.");
        } catch (NullPointerException e) {
        }
        try {
            new OAEPParameterSpec(mdName, null, mgfSpec, pSrc);
            fail("NullPointerException should be thrown in the case of "
                    + "null mgfName.");
        } catch (NullPointerException e) {
        }
        try {
            new OAEPParameterSpec(mdName, mgfName, mgfSpec, null);
            fail("NullPointerException should be thrown in the case of "
                    + "null pSrc.");
        } catch (NullPointerException e) {
        }
        assertTrue("The message digest algorithm name of "
                + "OAEPParameterSpec.DEFAULT field should be " + mdName,
                OAEPParameterSpec.DEFAULT.getDigestAlgorithm().equals(mdName));
        assertTrue("The mask generation function algorithm name of "
                + "OAEPParameterSpec.DEFAULT field should be " + mgfName,
                OAEPParameterSpec.DEFAULT.getMGFAlgorithm().equals(mgfName));
        assertTrue("The mask generation function parameters of "
                + "OAEPParameterSpec.DEFAULT field should be the same object "
                + "as MGF1ParameterSpec.SHA1",
                OAEPParameterSpec.DEFAULT.getMGFParameters()
                                                    == MGF1ParameterSpec.SHA1);
        assertTrue("The source of the encoding input P of "
                + "OAEPParameterSpec.DEFAULT field should be the same object "
                + "PSource.PSpecified.DEFAULT",
                OAEPParameterSpec.DEFAULT.getPSource()
                                                == PSource.PSpecified.DEFAULT);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDigestAlgorithm",
        args = {}
    )
    public void testGetDigestAlgorithm() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;
        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getDigestAlgorithm().equals(mdName));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMGFAlgorithm",
        args = {}
    )
    public void testGetMGFAlgorithm() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;
        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getMGFAlgorithm().equals(mgfName));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMGFParameters",
        args = {}
    )
    public void testGetMGFParameters() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;
        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getMGFParameters() == mgfSpec);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPSource",
        args = {}
    )
    public void testGetPSource() {
        String mdName = "SHA-1";
        String mgfName = "MGF1";
        AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
        PSource pSrc = PSource.PSpecified.DEFAULT;
        OAEPParameterSpec ps = new OAEPParameterSpec(mdName, mgfName,
                                                                mgfSpec, pSrc);
        assertTrue("The returned value does not equal to the "
                + "value specified in the constructor.",
                ps.getPSource() == pSrc);
    }
    public static Test suite() {
        return new TestSuite(OAEPParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
