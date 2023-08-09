public class TestCardPermission {
    public static void main(String[] args) throws Exception {
        CardPermission perm;
        test("*");
        test("connect");
        test("reset");
        test("exclusive");
        test("transmitControl");
        test("getBasicChannel");
        test("openLogicalChannel");
        test("connect,reset");
        test("Reset,coNnect", "connect,reset");
        test("exclusive,*,connect", "*");
        test("connect,reset,exclusive,transmitControl,getBasicChannel,openLogicalChannel", "*");
        invalid(null);
        invalid("");
        invalid("foo");
        invalid("connect, reset");
        invalid("connect,,reset");
        invalid("connect,");
        invalid(",connect");
        invalid("");
    }
    private static void invalid(String s) throws Exception {
        try {
            CardPermission c = new CardPermission("*", s);
            throw new Exception("Created invalid action: " + c);
        } catch (IllegalArgumentException e) {
            System.out.println("OK: " + e);
        }
    }
    private static void test(String actions) throws Exception {
        test(actions, actions);
    }
    private static void test(String actions, String canon) throws Exception {
        CardPermission p = new CardPermission("*", actions);
        System.out.println(p);
        String a = p.getActions();
        if (canon.equals(a) == false) {
            throw new Exception("Canonical actions mismatch: " + canon + " != " + a);
        }
    }
}
