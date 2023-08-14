public class Zygote {
    public static final int DEBUG_ENABLE_DEBUGGER   = 1;
    public static final int DEBUG_ENABLE_CHECKJNI   = 1 << 1;
    public static final int DEBUG_ENABLE_ASSERT     = 1 << 2;
    public static final int DEBUG_ENABLE_SAFEMODE   = 1 << 3;
    public static boolean systemInSafeMode = false;
    private Zygote() {}
    native public static int fork();
    native public static int forkAndSpecialize(int uid, int gid, int[] gids,
            int debugFlags, int[][] rlimits);
    @Deprecated
    public static int forkAndSpecialize(int uid, int gid, int[] gids,
            boolean enableDebugger, int[][] rlimits) {
        int debugFlags = enableDebugger ? DEBUG_ENABLE_DEBUGGER : 0;
        return forkAndSpecialize(uid, gid, gids, debugFlags, rlimits);
    }
    native public static int forkSystemServer(int uid, int gid,
            int[] gids, int debugFlags, int[][] rlimits,
            long permittedCapabilities, long effectiveCapabilities);
    public static int forkSystemServer(int uid, int gid,
            int[] gids, int debugFlags, int[][] rlimits) {
        throw new UnsupportedOperationException();
    }
    @Deprecated
    public static int forkSystemServer(int uid, int gid, int[] gids,
            boolean enableDebugger, int[][] rlimits) {
        int debugFlags = enableDebugger ? DEBUG_ENABLE_DEBUGGER : 0;
        return forkAndSpecialize(uid, gid, gids, debugFlags, rlimits);
    }
}
