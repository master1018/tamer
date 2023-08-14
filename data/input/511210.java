@TestTargetClass(KeyStoreBuilderParameters.class) 
public class KeyStoreBuilderParametersTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyStoreBuilderParameters",
        args = {java.security.KeyStore.Builder.class}
    )
    public void test_Constructor01() {
        KeyStore.Builder bld = null;
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(bld);
            assertNotNull(ksp.getParameters());
        } catch (NullPointerException npe) {
            fail("NullPointerException should not be thrown");
        }
        KeyStore.ProtectionParameter pp = new ProtectionParameterImpl();
        bld = KeyStore.Builder.newInstance("testType", null, pp);
        assertNotNull("Null object KeyStore.Builder", bld);
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(bld);
            assertNotNull(ksp.getParameters());
        } catch (Exception e) {
            fail("Unexpected exception was thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyStoreBuilderParameters",
        args = {java.util.List.class}
    )
    public void test_Constructor02() {
        List<String> ls = null;
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(ls);
            fail("NullPointerException should be thrown");
        } catch (NullPointerException npe) {
        }
        List<String> lsEmpty = new ArrayList<String>();
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(lsEmpty);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException iae) {
        }
        List<String> lsFiled = new ArrayList<String>();;
        lsFiled.add("Parameter1");
        lsFiled.add("Parameter2");
        try {
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(lsFiled);
            assertTrue("Not instanceof KeyStoreBuilderParameters object", 
                       ksp instanceof KeyStoreBuilderParameters); 
        } catch (Exception e) {
            fail("Unexpected exception was thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParameters",
        args = {}
    )
    public void test_getParameters() {
        String[] param = {"Parameter1", "Parameter2", "Parameter3"};
        List<String> ls = new ArrayList<String>();
        for (int i = 0; i < param.length; i++) {
            ls.add(param[i]);
        }
        KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(ls);
        try {
            List<String> res_list = ksp.getParameters();
            try {
                res_list.add("test");
            } catch (UnsupportedOperationException e) {
            }
            Object[] res = res_list.toArray(); 
            if (res.length == param.length) {
                for (int i = 0; i < res.length; i++) {
                    if (!param[i].equals(res[i])) {
                        fail("Parameters not equal");
                    }
                }
            } else {
                fail("Incorrect number of parameters");
            }
        } catch (Exception e) {
            fail("Unexpected exception was thrown");
        }
    }
    class ProtectionParameterImpl implements KeyStore.ProtectionParameter {
        ProtectionParameterImpl(){}
    }
}
