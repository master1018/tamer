public final class Config
{
    public static final boolean DEBUG = ConfigBuildFlags.DEBUG;
    @Deprecated
    public static final boolean RELEASE = !DEBUG;
    @Deprecated
    public static final boolean PROFILE = false;
    @Deprecated
    public static final boolean LOGV = false;
    @Deprecated
    public static final boolean LOGD = true;
}
