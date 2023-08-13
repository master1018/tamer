public class AppleEvent implements Serializable {
    public static final int BUY   = 0;
    public static final int EAT   = 1;
    public static final int THROW = 2;
    private final int what;
    private final Date when;
    public AppleEvent(int what) {
        this.what = what;
        this.when = new Date();
    }
    public String toString() {
        String desc = "[";
        switch (what) {
        case BUY:
            desc += "BUY";
            break;
        case EAT:
            desc += "EAT";
            break;
        case THROW:
            desc += "THROW";
            break;
        }
        desc += " @ " + when + "]";
        return desc;
    }
}
