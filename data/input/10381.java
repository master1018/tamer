public class CardPermission extends Permission {
    private static final long serialVersionUID = 7146787880530705613L;
    private final static int A_CONNECT              = 0x01;
    private final static int A_EXCLUSIVE            = 0x02;
    private final static int A_GET_BASIC_CHANNEL    = 0x04;
    private final static int A_OPEN_LOGICAL_CHANNEL = 0x08;
    private final static int A_RESET                = 0x10;
    private final static int A_TRANSMIT_CONTROL     = 0x20;
    private final static int A_ALL                  = 0x3f;
    private final static int[] ARRAY_MASKS = {
        A_ALL,
        A_CONNECT,
        A_EXCLUSIVE,
        A_GET_BASIC_CHANNEL,
        A_OPEN_LOGICAL_CHANNEL,
        A_RESET,
        A_TRANSMIT_CONTROL,
    };
    private final static String S_CONNECT              = "connect";
    private final static String S_EXCLUSIVE            = "exclusive";
    private final static String S_GET_BASIC_CHANNEL    = "getBasicChannel";
    private final static String S_OPEN_LOGICAL_CHANNEL = "openLogicalChannel";
    private final static String S_RESET                = "reset";
    private final static String S_TRANSMIT_CONTROL     = "transmitControl";
    private final static String S_ALL                  = "*";
    private final static String[] ARRAY_STRINGS = {
        S_ALL,
        S_CONNECT,
        S_EXCLUSIVE,
        S_GET_BASIC_CHANNEL,
        S_OPEN_LOGICAL_CHANNEL,
        S_RESET,
        S_TRANSMIT_CONTROL,
    };
    private transient int mask;
    private volatile String actions;
    public CardPermission(String terminalName, String actions) {
        super(terminalName);
        if (terminalName == null) {
            throw new NullPointerException();
        }
        mask = getMask(actions);
    }
    private static int getMask(String actions) {
        if ((actions == null) || (actions.length() == 0)) {
            throw new IllegalArgumentException("actions must not be empty");
        }
        for (int i = 0; i < ARRAY_STRINGS.length; i++) {
            if (actions == ARRAY_STRINGS[i]) {
                return ARRAY_MASKS[i];
            }
        }
        if (actions.endsWith(",")) {
            throw new IllegalArgumentException("Invalid actions: '" + actions + "'");
        }
        int mask = 0;
        String[] split = actions.split(",");
    outer:
        for (String s : split) {
            for (int i = 0; i < ARRAY_STRINGS.length; i++) {
                if (ARRAY_STRINGS[i].equalsIgnoreCase(s)) {
                    mask |= ARRAY_MASKS[i];
                    continue outer;
                }
            }
            throw new IllegalArgumentException("Invalid action: '" + s + "'");
        }
        return mask;
    }
    private static String getActions(int mask) {
        if (mask == A_ALL) {
            return S_ALL;
        }
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ARRAY_MASKS.length; i++) {
            int action = ARRAY_MASKS[i];
            if ((mask & action) == action) {
                if (first == false) {
                    sb.append(",");
                } else {
                    first = false;
                }
                sb.append(ARRAY_STRINGS[i]);
            }
        }
        return sb.toString();
    }
    public String getActions() {
        if (actions == null) {
            actions = getActions(mask);
        }
        return actions;
    }
    public boolean implies(Permission permission) {
        if (permission instanceof CardPermission == false) {
            return false;
        }
        CardPermission other = (CardPermission)permission;
        if ((this.mask & other.mask) != other.mask) {
            return false;
        }
        String thisName = getName();
        if (thisName.equals("*")) {
            return true;
        }
        if (thisName.equals(other.getName())) {
            return true;
        }
        return false;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CardPermission == false) {
            return false;
        }
        CardPermission other = (CardPermission)obj;
        return this.getName().equals(other.getName()) && (this.mask == other.mask);
    }
    public int hashCode() {
        return getName().hashCode() + 31 * mask;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        if (actions == null) {
            getActions();
        }
        s.defaultWriteObject();
    }
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        mask = getMask(actions);
    }
}
