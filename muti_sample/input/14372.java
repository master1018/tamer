public class IPAddressUtilTest {
    final static Boolean good = Boolean.TRUE;
    final static Boolean bad = Boolean.FALSE;
    static Object [][] assertions = {
        {"224.0.1.0", good},
        {"238.255.255.255", good},
        {"239.255.255.255", good},
        {"238.255.255.2550", bad},
        {"256.255.255.255", bad},
        {"238.255.2550.255", bad},
        {"238.2550.255.255", bad},
        {"2380.255.255.255", bad},
        {"10::10", good},
        {"10::10.1", bad},
        {"10::10.1.2", bad},
        {"10::10.1.2.3", good},
        {"10::10.1.2.3.4", bad},
        {"10::10.", bad},
        {"10::.10.", bad},
        {"10::.10", bad}
    };
    public static void main(String[] args) {
        for (int i=0; i<assertions.length; i++) {
            String addr = (String) assertions[i][0];
            boolean expected = ((Boolean) assertions[i][1]).booleanValue();
            boolean result = true;
            if (!IPAddressUtil.isIPv4LiteralAddress(addr) &&
                !IPAddressUtil.isIPv6LiteralAddress(addr)) {
                result = false;
            }
            if (result != expected) {
                throw new RuntimeException ("wrong result for " + addr);
            }
        }
    }
}
