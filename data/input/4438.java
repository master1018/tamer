public class ModifierFilter {
    public static final long PACKAGE = 0x8000000000000000L;
    public static final long ALL_ACCESS =
                PRIVATE | PROTECTED | PUBLIC | PACKAGE;
    private long oneOf;
    private long must;
    private long cannot;
    private static final int ACCESS_BITS = PRIVATE | PROTECTED | PUBLIC;
    public ModifierFilter(long oneOf) {
        this(oneOf, 0, 0);
    }
    public ModifierFilter(long oneOf, long must, long cannot) {
        this.oneOf = oneOf;
        this.must = must;
        this.cannot = cannot;
    }
    public boolean checkModifier(int modifierBits) {
        long fmod = ((modifierBits & ACCESS_BITS) == 0) ?
                        modifierBits | PACKAGE :
                        modifierBits;
        return ((oneOf == 0) || ((oneOf & fmod) != 0)) &&
                ((must & fmod) == must) &&
                ((cannot & fmod) == 0);
    }
} 
