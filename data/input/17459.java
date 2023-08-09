public final class DnDConstants {
    private DnDConstants() {} 
    public static final int ACTION_NONE         = 0x0;
    public static final int ACTION_COPY         = 0x1;
    public static final int ACTION_MOVE         = 0x2;
    public static final int ACTION_COPY_OR_MOVE = ACTION_COPY | ACTION_MOVE;
    public static final int ACTION_LINK         = 0x40000000;
    public static final int ACTION_REFERENCE    = ACTION_LINK;
}
