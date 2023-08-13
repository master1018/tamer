public class ApplyWildcardTest {
    private static final String positiveTests[][] = {
        { "d:k=*", "d:k=\"\"" },
        { "d:k=*", "d:k=" },
        { "d:k=*", "d:k=v" },
        { "d:k=a*b", "d:k=axyzb" },
        { "d:k=a*b,*", "d:k=axyzb,k2=v2" },
        { "d:*,k=a*b", "d:k=axyzb,k2=v2" },
        { "d:k=?", "d:k=v" },
        { "d:k=a?b", "d:k=axb" },
        { "d:k=a?b,*", "d:k=axb,k2=v2" },
        { "d:*,k=a?b", "d:k=axb,k2=v2" },
        { "d:k=?*", "d:k=axyzb" },
        { "d:k=a?bc*d", "d:k=axbcyzd" },
        { "d:k=a?bc*d,*", "d:k=axbcyzd,k2=v2" },
        { "d:*,k=a?bc*d", "d:k=axbcyzd,k2=v2" },
        { "d:k1=?,k2=*", "d:k1=a,k2=ab" },
        { "d:k1=a?b,k2=c*d", "d:k1=axb,k2=cyzd" },
        { "d:k1=a?b,k2=c*d,*", "d:k1=axb,k2=cyzd,k3=v3" },
        { "d:*,k1=a?b,k2=c*d", "d:k1=axb,k2=cyzd,k3=v3" },
        { "d:k=\"*\"", "d:k=\"\"" },
        { "d:k=\"*\"", "d:k=\"v\"" },
        { "d:k=\"a*b\"", "d:k=\"axyzb\"" },
        { "d:k=\"a*b\",*", "d:k=\"axyzb\",k2=\"v2\"" },
        { "d:*,k=\"a*b\"", "d:k=\"axyzb\",k2=\"v2\"" },
        { "d:k=\"?\"", "d:k=\"v\"" },
        { "d:k=\"a?b\"", "d:k=\"axb\"" },
        { "d:k=\"a?b\",*", "d:k=\"axb\",k2=\"v2\"" },
        { "d:*,k=\"a?b\"", "d:k=\"axb\",k2=\"v2\"" },
        { "d:k=\"?*\"", "d:k=\"axyzb\"" },
        { "d:k=\"a?bc*d\"", "d:k=\"axbcyzd\"" },
        { "d:k=\"a?bc*d\",*", "d:k=\"axbcyzd\",k2=\"v2\"" },
        { "d:*,k=\"a?bc*d\"", "d:k=\"axbcyzd\",k2=\"v2\"" },
        { "d:k1=\"?\",k2=\"*\"", "d:k1=\"a\",k2=\"ab\"" },
        { "d:k1=\"a?b\",k2=\"c*d\"", "d:k1=\"axb\",k2=\"cyzd\"" },
        { "d:k1=\"a?b\",k2=\"c*d\",*", "d:k1=\"axb\",k2=\"cyzd\",k3=\"v3\"" },
        { "d:*,k1=\"a?b\",k2=\"c*d\"", "d:k1=\"axb\",k2=\"cyzd\",k3=\"v3\"" },
    };
    private static final String negativeTests[][] = {
        { "d:k=\"*\"", "d:k=" },
        { "d:k=*", "d:k=,k2=" },
        { "d:k=*", "d:k=v,k2=v2" },
        { "d:k=a*b", "d:k=axyzbc" },
        { "d:k=a*b,*", "d:k=axyzbc,k2=v2" },
        { "d:*,k=a*b", "d:k=axyzbc,k2=v2" },
        { "d:k=?", "d:k=xyz" },
        { "d:k=a?b", "d:k=ab" },
        { "d:k=a?b,*", "d:k=ab,k2=v2" },
        { "d:*,k=a?b", "d:k=ab,k2=v2" },
        { "d:k=?*", "d:k=axyzb,k2=v2" },
        { "d:k=a?bc*d", "d:k=abcd" },
        { "d:k=a?bc*d,*", "d:k=abcd,k2=v2" },
        { "d:*,k=a?bc*d", "d:k=abcd,k2=v2" },
        { "d:k1=?,k2=*", "d:k1=ab,k2=ab" },
        { "d:k1=a?b,k2=c*d", "d:k1=ab,k2=cd" },
        { "d:k1=a?b,k2=c*d,*", "d:k1=ab,k2=cd,k3=v3" },
        { "d:*,k1=a?b,k2=c*d", "d:k1=ab,k2=cd,k3=v3" },
        { "d:k=\"*\"", "d:k=\"\",k2=\"\"" },
        { "d:k=\"*\"", "d:k=\"v\",k2=\"v2\"" },
        { "d:k=\"a*b\"", "d:k=\"axyzbc\"" },
        { "d:k=\"a*b\",*", "d:k=\"axyzbc\",k2=\"v2\"" },
        { "d:*,k=\"a*b\"", "d:k=\"axyzbc\",k2=\"v2\"" },
        { "d:k=\"?\"", "d:k=\"xyz\"" },
        { "d:k=\"a?b\"", "d:k=\"ab\"" },
        { "d:k=\"a?b\",*", "d:k=\"ab\",k2=\"v2\"" },
        { "d:*,k=\"a?b\"", "d:k=\"ab\",k2=\"v2\"" },
        { "d:k=\"?*\"", "d:k=\"axyzb\",k2=\"v2\"" },
        { "d:k=\"a?bc*d\"", "d:k=\"abcd\"" },
        { "d:k=\"a?bc*d\",*", "d:k=\"abcd\",k2=\"v2\"" },
        { "d:*,k=\"a?bc*d\"", "d:k=\"abcd\",k2=\"v2\"" },
        { "d:k1=\"?\",k2=\"*\"", "d:k1=\"ab\",k2=\"ab\"" },
        { "d:k1=\"a?b\",k2=\"c*d\"", "d:k1=\"ab\",k2=\"cd\"" },
        { "d:k1=\"a?b\",k2=\"c*d\",*", "d:k1=\"ab\",k2=\"cd\",k3=\"v3\"" },
        { "d:*,k1=\"a?b\",k2=\"c*d\"", "d:k1=\"ab\",k2=\"cd\",k3=\"v3\"" },
    };
    private static int runPositiveTests() {
        int error = 0;
        for (int i = 0; i < positiveTests.length; i++) {
            System.out.println("----------------------------------------------");
            try {
                ObjectName on1 = ObjectName.getInstance(positiveTests[i][0]);
                ObjectName on2 = ObjectName.getInstance(positiveTests[i][1]);
                System.out.println("\"" + on1 + "\".apply(\"" + on2 + "\")");
                boolean result = on1.apply(on2);
                System.out.println("Result = " + result);
                if (result == false) {
                    error++;
                    System.out.println("Test failed!");
                } else {
                    System.out.println("Test passed!");
                }
            } catch (Exception e) {
                error++;
                System.out.println("Got Unexpected Exception = " + e.toString());
                System.out.println("Test failed!");
            }
            System.out.println("----------------------------------------------");
        }
        return error;
    }
    private static int runNegativeTests() {
        int error = 0;
        for (int i = 0; i < negativeTests.length; i++) {
            System.out.println("----------------------------------------------");
            try {
                ObjectName on1 = ObjectName.getInstance(negativeTests[i][0]);
                ObjectName on2 = ObjectName.getInstance(negativeTests[i][1]);
                System.out.println("\"" + on1 + "\".apply(\"" + on2 + "\")");
                boolean result = on1.apply(on2);
                System.out.println("Result = " + result);
                if (result == true) {
                    error++;
                    System.out.println("Test failed!");
                } else {
                    System.out.println("Test passed!");
                }
            } catch (Exception e) {
                error++;
                System.out.println("Got Unexpected Exception = " + e.toString());
                System.out.println("Test failed!");
            }
            System.out.println("----------------------------------------------");
        }
        return error;
    }
    public static void main(String[] args) throws Exception {
        int error = 0;
        System.out.println("----------------------------------------------");
        System.out.println("Test ObjectName.apply(null)");
        try {
            new ObjectName("d:k=v").apply(null);
            error++;
            System.out.println("Didn't get expected NullPointerException!");
            System.out.println("Test failed!");
        } catch (NullPointerException e) {
            System.out.println("Got expected exception '" + e.toString() + "'");
            System.out.println("Test passed!");
        } catch (Exception e) {
            error++;
            System.out.println("Got unexpected exception '" + e.toString() + "'");
            System.out.println("Test failed!");
        }
        System.out.println("----------------------------------------------");
        System.out.println("----------------------------------------------");
        System.out.println("Test ObjectName.apply(domain_pattern)");
        try {
            if (new ObjectName("d:k=v").apply(new ObjectName("*:k=v"))) {
                error++;
                System.out.println("Got 'true' expecting 'false'");
                System.out.println("Test failed!");
            } else {
                System.out.println("Got expected return value 'false'");
                System.out.println("Test passed!");
            }
        } catch (Exception e) {
            error++;
            System.out.println("Got unexpected exception = " + e.toString());
            System.out.println("Test failed!");
        }
        System.out.println("----------------------------------------------");
        System.out.println("----------------------------------------------");
        System.out.println("Test ObjectName.apply(key_property_list_pattern)");
        try {
            if (new ObjectName("d:k=v").apply(new ObjectName("d:k=v,*"))) {
                error++;
                System.out.println("Got 'true' expecting 'false'");
                System.out.println("Test failed!");
            } else {
                System.out.println("Got expected return value 'false'");
                System.out.println("Test passed!");
            }
        } catch (Exception e) {
            error++;
            System.out.println("Got unexpected exception = " + e.toString());
            System.out.println("Test failed!");
        }
        System.out.println("----------------------------------------------");
        System.out.println("----------------------------------------------");
        System.out.println("Test ObjectName.apply(key_property_value_pattern)");
        try {
            if (new ObjectName("d:k=v").apply(new ObjectName("d:k=*"))) {
                error++;
                System.out.println("Got 'true' expecting 'false'");
                System.out.println("Test failed!");
            } else {
                System.out.println("Got expected return value 'false'");
                System.out.println("Test passed!");
            }
        } catch (Exception e) {
            error++;
            System.out.println("Got unexpected exception = " + e.toString());
            System.out.println("Test failed!");
        }
        System.out.println("----------------------------------------------");
        error += runPositiveTests();
        error += runNegativeTests();
        if (error > 0) {
            final String msg = "Test FAILED! Got " + error + " error(s)";
            System.out.println(msg);
            throw new IllegalArgumentException(msg);
        } else {
            System.out.println("Test PASSED!");
        }
    }
}
