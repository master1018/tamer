final public class PolicyUtils {
    private PolicyUtils() {
    }
    public final static int NOPLAINTEXT = 0x0001;
    public final static int NOACTIVE = 0x0002;
    public final static int NODICTIONARY = 0x0004;
    public final static int FORWARD_SECRECY = 0x0008;
    public final static int NOANONYMOUS = 0x0010;
    public final static int PASS_CREDENTIALS = 0x0200;
    public static boolean checkPolicy(int flags, Map props) {
        if (props == null) {
            return true;
        }
        if ("true".equalsIgnoreCase((String)props.get(Sasl.POLICY_NOPLAINTEXT))
            && (flags&NOPLAINTEXT) == 0) {
            return false;
        }
        if ("true".equalsIgnoreCase((String)props.get(Sasl.POLICY_NOACTIVE))
            && (flags&NOACTIVE) == 0) {
            return false;
        }
        if ("true".equalsIgnoreCase((String)props.get(Sasl.POLICY_NODICTIONARY))
            && (flags&NODICTIONARY) == 0) {
            return false;
        }
        if ("true".equalsIgnoreCase((String)props.get(Sasl.POLICY_NOANONYMOUS))
            && (flags&NOANONYMOUS) == 0) {
            return false;
        }
        if ("true".equalsIgnoreCase((String)props.get(Sasl.POLICY_FORWARD_SECRECY))
            && (flags&FORWARD_SECRECY) == 0) {
            return false;
        }
        if ("true".equalsIgnoreCase((String)props.get(Sasl.POLICY_PASS_CREDENTIALS))
            && (flags&PASS_CREDENTIALS) == 0) {
            return false;
        }
        return true;
    }
    public static String[] filterMechs(String[] mechs, int[] policies,
        Map props) {
        if (props == null) {
            return mechs.clone();
        }
        boolean[] passed = new boolean[mechs.length];
        int count = 0;
        for (int i = 0; i< mechs.length; i++) {
            if (passed[i] = checkPolicy(policies[i], props)) {
                ++count;
            }
        }
        String[] answer = new String[count];
        for (int i = 0, j=0; i< mechs.length; i++) {
            if (passed[i]) {
                answer[j++] = mechs[i];
            }
        }
        return answer;
    }
}
