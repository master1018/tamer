@TestTargetClass(AlgorithmParametersSpi.class)
public class AlgorithmParametersSpiTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AlgorithmParametersSpi",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGetEncoded",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGetEncoded",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineGetParameterSpec",
            args = {java.lang.Class.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineInit",
            args = {java.security.spec.AlgorithmParameterSpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineInit",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineInit",
            args = {byte[].class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "engineToString",
            args = {}
        )
    })
    public void testAlgorithmParametersSpi() {
        byte[] bt = new byte[10];
        MyAlgorithmParametersSpi algParSpi = new MyAlgorithmParametersSpi();
        assertTrue(algParSpi instanceof AlgorithmParametersSpi);
        assertNotNull(algParSpi);
        algParSpi.engineInit(new MyAlgorithmParameterSpec());
        algParSpi.engineInit(bt);
        algParSpi.engineInit(bt, "Format");
        algParSpi.engineToString();
        algParSpi.engineGetEncoded();
        algParSpi.engineGetEncoded("Format");
        algParSpi.engineGetParameterSpec(java.lang.Class.class);
    }
    public class MyAlgorithmParametersSpi extends AlgorithmParametersSpi {
        protected void engineInit(AlgorithmParameterSpec paramSpec) {
        }
        protected void engineInit(byte[] params){
        }
        protected void engineInit(byte[] params, String format){
        }
        protected AlgorithmParameterSpec engineGetParameterSpec(Class paramSpec){
            return null;
        }
        protected byte[] engineGetEncoded(){
            return null;
        }
        protected byte[] engineGetEncoded(String format){
            return null;
        }
        protected String engineToString() {
            return null;
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(AlgorithmParametersSpiTest.class);
    }
    class MyAlgorithmParameterSpec implements AlgorithmParameterSpec {}
}
